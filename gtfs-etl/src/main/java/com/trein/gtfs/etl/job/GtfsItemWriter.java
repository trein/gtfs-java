package com.trein.gtfs.etl.job;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.trein.gtfs.csv.vo.GtfsAgency;
import com.trein.gtfs.csv.vo.GtfsCalendar;
import com.trein.gtfs.csv.vo.GtfsCalendarDate;
import com.trein.gtfs.csv.vo.GtfsRoute;
import com.trein.gtfs.csv.vo.GtfsShape;
import com.trein.gtfs.csv.vo.GtfsTrip;
import com.trein.gtfs.orm.entities.Agency;
import com.trein.gtfs.orm.entities.Calendar;
import com.trein.gtfs.orm.entities.CalendarDate;
import com.trein.gtfs.orm.entities.DirectionType;
import com.trein.gtfs.orm.entities.ExceptionType;
import com.trein.gtfs.orm.entities.Location;
import com.trein.gtfs.orm.entities.Route;
import com.trein.gtfs.orm.entities.RouteType;
import com.trein.gtfs.orm.entities.Shape;
import com.trein.gtfs.orm.entities.Trip;
import com.trein.gtfs.orm.entities.WheelchairType;
import com.trein.gtfs.orm.repository.AgencyRepository;
import com.trein.gtfs.orm.repository.CalendarDateRepository;
import com.trein.gtfs.orm.repository.CalendarRepository;
import com.trein.gtfs.orm.repository.RouteRepository;
import com.trein.gtfs.orm.repository.ShapeRepository;
import com.trein.gtfs.orm.repository.TripRepository;

public class GtfsItemWriter implements ItemWriter<GtfsItem> {

    private static final SimpleDateFormat CALENDAR_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsItemWriter.class);
    private static final boolean DRY_RUN = false;
    
    @Autowired
    private AgencyRepository agencyRepository;
    @Autowired
    private CalendarDateRepository calendarDateRepository;
    @Autowired
    private CalendarRepository calendarRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private ShapeRepository shapeRepository;
    @Autowired
    private TripRepository tripRepository;
    
    public GtfsItemWriter() {
    }

    @Override
    public void write(List<? extends GtfsItem> items) {
        for (GtfsItem item : items) {
            LOGGER.info("Received item [{}]", item);
            
            if (item.getEntityClass().equals(GtfsAgency.class)) {
                GtfsAgency agency = item.getEntity();
                String id = agency.getId();
                String name = agency.getName();
                String url = agency.getUrl();
                String timezone = agency.getTimezone();
                String lang = agency.getLang();
                String phone = agency.getPhone();
                String fareUrl = agency.getFareUrl();
                Agency entity = new Agency(id, name, url, timezone, lang, phone, fareUrl);
                
                if (!DRY_RUN) {
                    this.agencyRepository.save(entity);
                }
            } else if (item.getEntityClass().equals(GtfsCalendarDate.class)) {
                try {
                    GtfsCalendarDate calendarDate = item.getEntity();
                    ExceptionType exception = ExceptionType.valueOf(calendarDate.getExceptionType().intValue());
                    Date date = CALENDAR_FORMAT.parse(calendarDate.getDate());
                    String serviceId = calendarDate.getServiceId();
                    CalendarDate entity = new CalendarDate(serviceId, date, exception);
                    
                    if (!DRY_RUN) {
                        this.calendarDateRepository.save(entity);
                    }
                } catch (ParseException e) {
                    throw new IllegalStateException("Error storing calendar date", e);
                }
            } else if (item.getEntityClass().equals(GtfsCalendar.class)) {
                try {
                    GtfsCalendar calendar = item.getEntity();
                    Date startDate = CALENDAR_FORMAT.parse(calendar.getStartDate());
                    Date endDate = CALENDAR_FORMAT.parse(calendar.getEndDate());
                    boolean monday = calendar.getMonday().intValue() == 1;
                    boolean tuesday = calendar.getTuesday().intValue() == 1;
                    boolean wednesday = calendar.getWednesday().intValue() == 1;
                    boolean thursday = calendar.getThursday().intValue() == 1;
                    boolean friday = calendar.getFriday().intValue() == 1;
                    boolean saturday = calendar.getSaturday().intValue() == 1;
                    boolean sunday = calendar.getSunday().intValue() == 1;
                    String serviceId = calendar.getServiceId();
                    Calendar entity = new Calendar(serviceId, monday, tuesday, wednesday, thursday, friday, saturday, sunday,
                            startDate, endDate);

                    if (!DRY_RUN) {
                        this.calendarRepository.save(entity);
                    }
                } catch (ParseException e) {
                    throw new IllegalStateException("Error storing calendar date", e);
                }
            } else if (item.getEntityClass().equals(GtfsRoute.class)) {
                GtfsRoute route = item.getEntity();
                Agency agency = this.agencyRepository.findByAgencyId(route.getAgencyId());
                RouteType type = RouteType.valueOf(route.getRouteType().intValue());
                String routeId = route.getId();
                String shortName = route.getShortName();
                String longName = route.getLongName();
                String desc = route.getDesc();
                String url = route.getUrl();
                String hexPathColor = route.getHexPathColor();
                String hexTextColor = route.getHexTextColor();
                Route entity = new Route(routeId, agency, shortName, longName, desc, type, url, hexPathColor, hexTextColor);
                
                if (!DRY_RUN) {
                    this.routeRepository.save(entity);
                }
            } else if (item.getEntityClass().equals(GtfsShape.class)) {
                GtfsShape shape = item.getEntity();
                Location location = new Location(shape.getLat().doubleValue(), shape.getLng().doubleValue());
                double distanceTraveled = (shape.getDistanceTraveled() != null) ? shape.getDistanceTraveled().doubleValue() : 0;
                Shape entity = new Shape(shape.getId(), location, shape.getSequence().longValue(), distanceTraveled);
                
                if (!DRY_RUN) {
                    this.shapeRepository.save(entity);
                }
            } else if (item.getEntityClass().equals(GtfsTrip.class)) {
                GtfsTrip trip = item.getEntity();
                Route route = this.routeRepository.findByRouteId(trip.getRouteId());
                Shape shape = this.shapeRepository.findByShapeId(trip.getShapeId());
                DirectionType direction = DirectionType.valueOf(trip.getDirectionType().intValue());
                WheelchairType wheelchairType = WheelchairType.valueOf(trip.getWheelchairType().intValue());
                int blockId = (trip.getBlockId() != null) ? trip.getBlockId().intValue() : 0;
                String serviceId = trip.getServiceId();
                String headsign = trip.getHeadsign();
                String shortName = trip.getShortName();
                Trip entity = new Trip(trip.getId(), route, serviceId, headsign, shortName, direction, blockId, shape,
                        wheelchairType);
                
                if (!DRY_RUN) {
                    this.tripRepository.save(entity);
                }
            }
        }
    }
}

// GtfsTrip, GtfsStopTime.class, GtfsStop.class, GtfsTransfer.class
