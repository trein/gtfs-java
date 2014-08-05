package com.trein.gtfs.etl.job;

import java.sql.Time;
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
import com.trein.gtfs.csv.vo.GtfsStop;
import com.trein.gtfs.csv.vo.GtfsStopTime;
import com.trein.gtfs.csv.vo.GtfsTransfer;
import com.trein.gtfs.csv.vo.GtfsTrip;
import com.trein.gtfs.orm.entities.Agency;
import com.trein.gtfs.orm.entities.AvailabilityType;
import com.trein.gtfs.orm.entities.Calendar;
import com.trein.gtfs.orm.entities.CalendarDate;
import com.trein.gtfs.orm.entities.DirectionType;
import com.trein.gtfs.orm.entities.ExceptionType;
import com.trein.gtfs.orm.entities.Location;
import com.trein.gtfs.orm.entities.Route;
import com.trein.gtfs.orm.entities.RouteType;
import com.trein.gtfs.orm.entities.Shape;
import com.trein.gtfs.orm.entities.Stop;
import com.trein.gtfs.orm.entities.StopLocationType;
import com.trein.gtfs.orm.entities.StopTime;
import com.trein.gtfs.orm.entities.Transfer;
import com.trein.gtfs.orm.entities.TransferType;
import com.trein.gtfs.orm.entities.Trip;
import com.trein.gtfs.orm.entities.WheelchairType;
import com.trein.gtfs.orm.repository.AgencyRepository;
import com.trein.gtfs.orm.repository.CalendarDateRepository;
import com.trein.gtfs.orm.repository.CalendarRepository;
import com.trein.gtfs.orm.repository.RouteRepository;
import com.trein.gtfs.orm.repository.ShapeRepository;
import com.trein.gtfs.orm.repository.StopRepository;
import com.trein.gtfs.orm.repository.StopTimeRepository;
import com.trein.gtfs.orm.repository.TransferRepository;
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
    @Autowired
    private StopRepository stopRepository;
    @Autowired
    private StopTimeRepository stopTimeRepository;
    @Autowired
    private TransferRepository transferRepository;

    private int currentCount;
    
    public GtfsItemWriter() {
    }

    @Override
    public void write(List<? extends GtfsItem> items) {
        for (GtfsItem item : items) {

            this.currentCount++;
            LOGGER.debug("Received item [{}]", item);
            if ((this.currentCount % 10000) == 0) {
                LOGGER.info("Writing entry [{}] into database", String.valueOf(this.currentCount));
            }
            
            if (item.getEntityClass().equals(GtfsAgency.class)) {
                persistAgency(item);
            } else if (item.getEntityClass().equals(GtfsCalendarDate.class)) {
                persistCalendarDate(item);
            } else if (item.getEntityClass().equals(GtfsCalendar.class)) {
                persistCalendar(item);
            } else if (item.getEntityClass().equals(GtfsRoute.class)) {
                persistRoute(item);
            } else if (item.getEntityClass().equals(GtfsShape.class)) {
                persistShape(item);
            } else if (item.getEntityClass().equals(GtfsTrip.class)) {
                persistTrip(item);
            } else if (item.getEntityClass().equals(GtfsStop.class)) {
                persistStop(item);
            } else if (item.getEntityClass().equals(GtfsStopTime.class)) {
                persistStopTime(item);
            } else if (item.getEntityClass().equals(GtfsTransfer.class)) {
                persistTransfer(item);
            }
            // GtfsFareAttribute.class, GtfsFareRule.class, GtfsFrequency.class, GtfsFeedInfo.class
        }
    }

    private void persistTransfer(GtfsItem item) {
        GtfsTransfer transfer = item.getEntity();
        Stop fromStop = this.stopRepository.findByStopId(transfer.getFromStopId());
        Stop toStop = this.stopRepository.findByStopId(transfer.getToStopId());
        TransferType transferType = TransferType.fromCode(transfer.getTransferType().intValue());
        long transferTime = (transfer.getMinTransferTimeSecs() != null) ? transfer.getMinTransferTimeSecs().longValue() : 0;
        Transfer entity = new Transfer(fromStop, toStop, transferType, transferTime);
        
        if (!DRY_RUN) {
            this.transferRepository.save(entity);
        }
    }

    private void persistStopTime(GtfsItem item) {
        GtfsStopTime stop = item.getEntity();
        Stop innerStop = this.stopRepository.findByStopId(stop.getStopId());
        Trip trip = this.tripRepository.findByTripId(stop.getTripId());
        Time arrival = Time.valueOf(stop.getArrivalTime());
        Time departure = Time.valueOf(stop.getDepartureTime());
        AvailabilityType pickupType = AvailabilityType.fromCode(stop.getPickupType().intValue());
        AvailabilityType dropoffType = AvailabilityType.fromCode(stop.getPickupType().intValue());
        double distance = (stop.getShapeDistanceTraveled() != null) ? stop.getShapeDistanceTraveled().doubleValue() : 0;
        int sequence = stop.getStopSequence().intValue();
        String headsign = stop.getStopHeadsign();
        StopTime entity = new StopTime(trip, arrival, departure, innerStop, sequence, headsign, pickupType, dropoffType, distance);

        if (!DRY_RUN) {
            this.stopTimeRepository.save(entity);
        }
    }

    private void persistStop(GtfsItem item) {
        GtfsStop stop = item.getEntity();
        Location location = new Location(stop.getLat().doubleValue(), stop.getLng().doubleValue());
        StopLocationType type = StopLocationType.fromCode(stop.getLocationType().intValue());
        WheelchairType wheelchairType = WheelchairType.fromCode(stop.getWheelchairType().intValue());
        int parentStop = (stop.getParentStation() != null) ? stop.getParentStation().intValue() : 0;
        String id = stop.getId();
        String code = stop.getCode();
        String name = stop.getName();
        String desc = stop.getDesc();
        String url = stop.getUrl();
        String timezone = stop.getTimezone();
        String zoneId = stop.getZoneId();
        Stop entity = new Stop(id, code, name, desc, location, zoneId, url, type, parentStop, timezone, wheelchairType);

        if (!DRY_RUN) {
            this.stopRepository.save(entity);
        }
    }
    
    private void persistTrip(GtfsItem item) {
        GtfsTrip trip = item.getEntity();
        Route route = this.routeRepository.findByRouteId(trip.getRouteId());
        List<Shape> shapes = this.shapeRepository.findByShapeId(trip.getShapeId());
        DirectionType direction = DirectionType.fromCode(trip.getDirectionType().intValue());
        WheelchairType wheelchairType = WheelchairType.fromCode(trip.getWheelchairType().intValue());
        int blockId = (trip.getBlockId() != null) ? trip.getBlockId().intValue() : 0;
        String serviceId = trip.getServiceId();
        String headsign = trip.getHeadsign();
        String shortName = trip.getShortName();
        Trip entity = new Trip(trip.getId(), route, serviceId, headsign, shortName, direction, blockId, shapes, wheelchairType);
        
        if (!DRY_RUN) {
            this.tripRepository.save(entity);
        }
    }

    private void persistShape(GtfsItem item) {
        GtfsShape shape = item.getEntity();
        Location location = new Location(shape.getLat().doubleValue(), shape.getLng().doubleValue());
        double distanceTraveled = (shape.getDistanceTraveled() != null) ? shape.getDistanceTraveled().doubleValue() : 0;
        Shape entity = new Shape(shape.getId(), location, shape.getSequence().longValue(), distanceTraveled);
        
        if (!DRY_RUN) {
            this.shapeRepository.save(entity);
        }
    }

    private void persistRoute(GtfsItem item) {
        GtfsRoute route = item.getEntity();
        Agency agency = this.agencyRepository.findByAgencyId(route.getAgencyId());
        RouteType type = RouteType.fromCode(route.getRouteType().intValue());
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
    }

    private void persistCalendar(GtfsItem item) {
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
            Calendar entity = new Calendar(serviceId, monday, tuesday, wednesday, thursday, friday, saturday, sunday, startDate,
                    endDate);

            if (!DRY_RUN) {
                this.calendarRepository.save(entity);
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Error storing calendar date", e);
        }
    }

    private void persistCalendarDate(GtfsItem item) {
        try {
            GtfsCalendarDate calendarDate = item.getEntity();
            ExceptionType exception = ExceptionType.fromCode(calendarDate.getExceptionType().intValue());
            Date date = CALENDAR_FORMAT.parse(calendarDate.getDate());
            String serviceId = calendarDate.getServiceId();
            CalendarDate entity = new CalendarDate(serviceId, date, exception);
            
            if (!DRY_RUN) {
                this.calendarDateRepository.save(entity);
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Error storing calendar date", e);
        }
    }

    private void persistAgency(GtfsItem item) {
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
    }
}
