package org.opentripplanner.standalone;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.impl.calendar.CalendarServiceDataFactoryImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.CalendarServiceData;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GenericMutableDao;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class GtfsGraphBuilderImpl implements GraphBuilder {
    
    private static final Logger LOG = LoggerFactory.getLogger(GtfsGraphBuilderImpl.class);
    
    private GtfsBundles _gtfsBundles;
    
    EntityHandler counter = new EntityCounter();
    
    private FareServiceFactory _fareServiceFactory;
    
    /** will be applied to all bundles which do not have the cacheDirectory property set */
    private File cacheDirectory;
    
    /** will be applied to all bundles which do not have the useCached property set */
    private Boolean useCached;
    
    Set<String> agencyIdsSeen = Sets.newHashSet();
    
    int nextAgencyId = 1; // used for generating agency IDs to resolve ID conflicts
    
    /**
     * Construct and set bundles all at once. TODO why is there a wrapper class around a list of
     * GTFS files? TODO why is there a wrapper around GTFS files at all?
     */
    public GtfsGraphBuilderImpl(List<GtfsBundle> gtfsBundles) {
        GtfsBundles gtfsb = new GtfsBundles();
        gtfsb.setBundles(gtfsBundles);
        this.setGtfsBundles(gtfsb);
    }

    public GtfsGraphBuilderImpl() {
    };
    
    @Override
    public List<String> provides() {
        List<String> result = new ArrayList<String>();
        result.add("transit");
        return result;
    }
    
    @Override
    public List<String> getPrerequisites() {
        return Collections.emptyList();
    }
    
    public void setGtfsBundles(GtfsBundles gtfsBundles) {
        this._gtfsBundles = gtfsBundles;
        /* check for dups */
        HashSet<String> bundles = new HashSet<String>();
        for (GtfsBundle bundle : gtfsBundles.getBundles()) {
            String key = bundle.getDataKey();
            if (bundles.contains(key)) { throw new RuntimeException("duplicate GTFS bundle " + key); }
            bundles.add(key);
        }
    }
    
    public void setFareServiceFactory(FareServiceFactory factory) {
        this._fareServiceFactory = factory;
    }
    
    @Override
    public void buildGraph(Graph graph, HashMap<Class<?>, Object> extra) {
        
        MultiCalendarServiceImpl service = new MultiCalendarServiceImpl();
        GtfsStopContext stopContext = new GtfsStopContext();

        try {
            for (GtfsBundle gtfsBundle : this._gtfsBundles.getBundles()) {
                // apply global defaults to individual GTFSBundles (if globals have been set)
                if ((this.cacheDirectory != null) && (gtfsBundle.cacheDirectory == null)) {
                    gtfsBundle.cacheDirectory = this.cacheDirectory;
                }
                if ((this.useCached != null) && (gtfsBundle.useCached == null)) {
                    gtfsBundle.useCached = this.useCached;
                }
                GtfsMutableRelationalDao dao = new GtfsRelationalDaoImpl();
                GtfsContext context = GtfsLibrary.createContext(dao, service);
                GTFSPatternHopFactory hf = new GTFSPatternHopFactory(context);
                hf.setStopContext(stopContext);
                hf.setFareServiceFactory(this._fareServiceFactory);
                hf.setMaxStopToShapeSnapDistance(gtfsBundle.getMaxStopToShapeSnapDistance());
                
                loadBundle(gtfsBundle, graph, dao);
                
                CalendarServiceDataFactoryImpl csfactory = new CalendarServiceDataFactoryImpl();
                csfactory.setGtfsDao(dao);
                CalendarServiceData data = csfactory.createData();
                service.addData(data, dao);
                
                hf.setDefaultStreetToStopTime(gtfsBundle.getDefaultStreetToStopTime());
                hf.run(graph);
                
                if (gtfsBundle.doesTransfersTxtDefineStationPaths()) {
                    hf.createTransfersTxtTransfers();
                }
                if (gtfsBundle.linkStopsToParentStations) {
                    hf.linkStopsToParentStations(graph);
                }
                if (gtfsBundle.parentStationTransfers) {
                    hf.createParentStationTransfers();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        // We need to save the calendar service data so we can use it later
        CalendarServiceData data = service.getData();
        graph.putService(CalendarServiceData.class, data);
        graph.updateTransitFeedValidity(data);
        
    }
    
    /****
     * Private Methods
     ****/
    
    private void loadBundle(GtfsBundle gtfsBundle, Graph graph, GtfsMutableRelationalDao dao) throws IOException {
        
        StoreImpl store = new StoreImpl(dao);
        store.open();
        LOG.info("reading {}", gtfsBundle.toString());
        
        GtfsReader reader = new GtfsReader();
        reader.setInputSource(gtfsBundle.getCsvInputSource());
        reader.setEntityStore(store);
        reader.setInternStrings(true);
        
        if (LOG.isDebugEnabled()) {
            reader.addEntityHandler(this.counter);
        }
        
        if (gtfsBundle.getDefaultBikesAllowed()) {
            reader.addEntityHandler(new EntityBikeability(true));
        }
        
        for (Class<?> entityClass : reader.getEntityClasses()) {
            LOG.info("reading entities: " + entityClass.getName());
            reader.readEntities(entityClass);
            store.flush();
            // NOTE that agencies are first in the list and read before all other entity types, so
            // it is effective to
            // set the agencyId here. Each feed ("bundle") is loaded by a separate reader, so there
            // is no risk of
            // agency mappings accumulating.
            if (entityClass == Agency.class) {
                String defaultAgencyId = null;
                for (Agency agency : reader.getAgencies()) {
                    String agencyId = agency.getId();
                    LOG.info("This Agency has the ID {}", agencyId);
                    // Somehow, when the agency's id field is missing, OBA replaces it with the
                    // agency's name.
                    // TODO Figure out how and why this is happening.
                    if ((agencyId == null) || this.agencyIdsSeen.contains(agencyId)) {
                        // Loop in case generated name is already in use.
                        String generatedAgencyId = null;
                        while ((generatedAgencyId == null) || this.agencyIdsSeen.contains(generatedAgencyId)) {
                            generatedAgencyId = "F" + this.nextAgencyId;
                            this.nextAgencyId++;
                        }
                        LOG.warn("The agency ID '{}' was already seen, or I think it's bad. Replacing with '{}'.", agencyId,
                                generatedAgencyId);
                        reader.addAgencyIdMapping(agencyId, generatedAgencyId); // NULL key should
                                                                                // work
                        agency.setId(generatedAgencyId);
                        agencyId = generatedAgencyId;
                    }
                    if (agencyId != null) {
                        this.agencyIdsSeen.add(agencyId);
                    }
                    if (defaultAgencyId == null) {
                        defaultAgencyId = agencyId;
                    }
                }
                reader.setDefaultAgencyId(defaultAgencyId); // not sure this is a good idea, setting
                                                            // it to the first-of-many IDs.
            }
        }
        
        for (ShapePoint shapePoint : store.getAllEntitiesForType(ShapePoint.class)) {
            shapePoint.getShapeId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (Route route : store.getAllEntitiesForType(Route.class)) {
            route.getId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (Stop stop : store.getAllEntitiesForType(Stop.class)) {
            stop.getId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (Trip trip : store.getAllEntitiesForType(Trip.class)) {
            trip.getId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (ServiceCalendar serviceCalendar : store.getAllEntitiesForType(ServiceCalendar.class)) {
            serviceCalendar.getServiceId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (ServiceCalendarDate serviceCalendarDate : store.getAllEntitiesForType(ServiceCalendarDate.class)) {
            serviceCalendarDate.getServiceId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (FareAttribute fareAttribute : store.getAllEntitiesForType(FareAttribute.class)) {
            fareAttribute.getId().setAgencyId(reader.getDefaultAgencyId());
        }
        for (Pathway pathway : store.getAllEntitiesForType(Pathway.class)) {
            pathway.getId().setAgencyId(reader.getDefaultAgencyId());
        }
        
        store.close();
        
    }
    
    private class StoreImpl implements GenericMutableDao {
        
        private final GtfsMutableRelationalDao dao;
        
        StoreImpl(GtfsMutableRelationalDao dao) {
            this.dao = dao;
        }
        
        @Override
        public void open() {
            this.dao.open();
        }
        
        @Override
        public <T> T getEntityForId(Class<T> type, Serializable id) {
            return this.dao.getEntityForId(type, id);
        }
        
        @Override
        public void saveEntity(Object entity) {
            this.dao.saveEntity(entity);
        }
        
        @Override
        public void flush() {
            this.dao.flush();
        }
        
        @Override
        public void close() {
            this.dao.close();
        }
        
        @Override
        public <T> void clearAllEntitiesForType(Class<T> type) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(T entity) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
            return this.dao.getAllEntitiesForType(type);
        }
        
        @Override
        public void saveOrUpdateEntity(Object entity) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void updateEntity(Object entity) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class EntityCounter implements EntityHandler {
        
        private final Map<Class<?>, Integer> _count = new HashMap<Class<?>, Integer>();
        
        @Override
        public void handleEntity(Object bean) {
            int count = incrementCount(bean.getClass());
            if ((count % 1000000) == 0) {
                if (LOG.isDebugEnabled()) {
                    String name = bean.getClass().getName();
                    int index = name.lastIndexOf('.');
                    if (index != -1) {
                        name = name.substring(index + 1);
                    }
                    LOG.debug("loading " + name + ": " + count);
                }
            }
        }
        
        private int incrementCount(Class<?> entityType) {
            Integer value = this._count.get(entityType);
            if (value == null) {
                value = 0;
            }
            value++;
            this._count.put(entityType, value);
            return value;
        }
        
    }
    
    private static class EntityBikeability implements EntityHandler {
        
        private final Boolean _defaultBikesAllowed;
        
        public EntityBikeability(Boolean defaultBikesAllowed) {
            this._defaultBikesAllowed = defaultBikesAllowed;
        }
        
        @Override
        public void handleEntity(Object bean) {
            if (!(bean instanceof Trip)) { return; }
            
            Trip trip = (Trip) bean;
            if (this._defaultBikesAllowed && (BikeAccess.fromTrip(trip) == BikeAccess.UNKNOWN)) {
                BikeAccess.setForTrip(trip, BikeAccess.ALLOWED);
            }
        }
    }
    
    @Override
    public void checkInputs() {
        for (GtfsBundle bundle : this._gtfsBundles.getBundles()) {
            bundle.checkInputs();
        }
    }
    
}
