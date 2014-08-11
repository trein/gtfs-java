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
import com.trein.gtfs.csv.vo.GtfsFareAttribute;
import com.trein.gtfs.csv.vo.GtfsFareRule;
import com.trein.gtfs.csv.vo.GtfsFeedInfo;
import com.trein.gtfs.csv.vo.GtfsFrequency;
import com.trein.gtfs.csv.vo.GtfsRoute;
import com.trein.gtfs.csv.vo.GtfsShape;
import com.trein.gtfs.csv.vo.GtfsStop;
import com.trein.gtfs.csv.vo.GtfsStopTime;
import com.trein.gtfs.csv.vo.GtfsTransfer;
import com.trein.gtfs.csv.vo.GtfsTrip;
import com.trein.gtfs.jpa.entities.Agency;
import com.trein.gtfs.jpa.entities.AvailabilityType;
import com.trein.gtfs.jpa.entities.Calendar;
import com.trein.gtfs.jpa.entities.CalendarDate;
import com.trein.gtfs.jpa.entities.CurrencyType;
import com.trein.gtfs.jpa.entities.DirectionType;
import com.trein.gtfs.jpa.entities.ExactTimeType;
import com.trein.gtfs.jpa.entities.ExceptionType;
import com.trein.gtfs.jpa.entities.FareAttribute;
import com.trein.gtfs.jpa.entities.FareRule;
import com.trein.gtfs.jpa.entities.FareTransferType;
import com.trein.gtfs.jpa.entities.FeedInfo;
import com.trein.gtfs.jpa.entities.Frequency;
import com.trein.gtfs.jpa.entities.Location;
import com.trein.gtfs.jpa.entities.PaymentType;
import com.trein.gtfs.jpa.entities.Route;
import com.trein.gtfs.jpa.entities.RouteType;
import com.trein.gtfs.jpa.entities.Shape;
import com.trein.gtfs.jpa.entities.Stop;
import com.trein.gtfs.jpa.entities.StopLocationType;
import com.trein.gtfs.jpa.entities.StopTime;
import com.trein.gtfs.jpa.entities.Transfer;
import com.trein.gtfs.jpa.entities.TransferType;
import com.trein.gtfs.jpa.entities.Trip;
import com.trein.gtfs.jpa.entities.WheelchairType;
import com.trein.gtfs.jpa.repository.AgencyRepository;
import com.trein.gtfs.jpa.repository.CalendarDateRepository;
import com.trein.gtfs.jpa.repository.CalendarRepository;
import com.trein.gtfs.jpa.repository.FareAttributeRepository;
import com.trein.gtfs.jpa.repository.FareRuleRepository;
import com.trein.gtfs.jpa.repository.FeedInfoRepository;
import com.trein.gtfs.jpa.repository.FrequencyRepository;
import com.trein.gtfs.jpa.repository.RouteRepository;
import com.trein.gtfs.jpa.repository.ShapeRepository;
import com.trein.gtfs.jpa.repository.StopRepository;
import com.trein.gtfs.jpa.repository.StopTimeRepository;
import com.trein.gtfs.jpa.repository.TransferRepository;
import com.trein.gtfs.jpa.repository.TripRepository;

public class GtfsJpaItemWriter implements ItemWriter<GtfsItem> {

    private static final SimpleDateFormat CALENDAR_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsJpaItemWriter.class);
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
    @Autowired
    private FareAttributeRepository fareAttributeRepository;
    @Autowired
    private FareRuleRepository fareRuleRepository;
    @Autowired
    private FrequencyRepository frequencyRepository;
    @Autowired
    private FeedInfoRepository feedInfoRepository;

    private int currentCount;
    
    public GtfsJpaItemWriter() {
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
            } else if (item.getEntityClass().equals(GtfsFareAttribute.class)) {
                persistFareAttribute(item);
            } else if (item.getEntityClass().equals(GtfsFareRule.class)) {
                persistFareRule(item);
            } else if (item.getEntityClass().equals(GtfsFrequency.class)) {
                persistFrequency(item);
            } else if (item.getEntityClass().equals(GtfsFeedInfo.class)) {
                persistFeedInfo(item);
            }
        }
    }

    private void persistFeedInfo(GtfsItem item) {
        try {
            GtfsFeedInfo info = item.getEntity();
            Date startDate = CALENDAR_FORMAT.parse(info.getStartDate());
            Date endDate = CALENDAR_FORMAT.parse(info.getEndDate());
            String publisherName = info.getPublisherName();
            String url = info.getPusblisherUrl();
            String language = info.getLanguage();
            String version = info.getVersion();
            FeedInfo entity = new FeedInfo(publisherName, url, language, startDate, endDate, version);

            if (!DRY_RUN) {
                this.feedInfoRepository.save(entity);
            }
        } catch (ParseException e) {
            throw new IllegalStateException("Error storing feed info date", e);
        }
    }

    private void persistFrequency(GtfsItem item) {
        GtfsFrequency frequency = item.getEntity();
        Trip trip = this.tripRepository.findByTripId(frequency.getTripId());
        Time start = Time.valueOf(frequency.getStartTime());
        Time end = Time.valueOf(frequency.getEndTime());
        long headway = (frequency.getHeadwaySecs() != null) ? frequency.getHeadwaySecs().longValue() : 0;
        ExactTimeType exactTime = ExactTimeType.fromCode(frequency.getExactTime());
        Frequency entity = new Frequency(trip, start, end, headway, exactTime);
        
        if (!DRY_RUN) {
            this.frequencyRepository.save(entity);
        }
    }
    
    private void persistFareRule(GtfsItem item) {
        GtfsFareRule rule = item.getEntity();
        Route route = this.routeRepository.findByRouteId(rule.getRouteId());
        // TODO: save Fare
        FareRule entity = new FareRule(null, route, rule.getOriginZoneId(), rule.getDestinationZoneId(), rule.getContainsId());
        
        if (!DRY_RUN) {
            this.fareRuleRepository.save(entity);
        }
    }

    private void persistFareAttribute(GtfsItem item) {
        GtfsFareAttribute attribute = item.getEntity();
        double price = (attribute.getPrice() != null) ? attribute.getPrice().doubleValue() : 0;
        CurrencyType currencyType = CurrencyType.fromCode(attribute.getCurrencyType());
        PaymentType paymentType = PaymentType.fromCode(attribute.getPaymentType());
        FareTransferType transferType = FareTransferType.fromCode(attribute.getTransfers());
        double duration = (attribute.getTransferDuration() != null) ? attribute.getTransferDuration().doubleValue() : 0;
        // TODO: save Fare
        FareAttribute entity = new FareAttribute(null, price, currencyType, paymentType, transferType, duration);
        
        if (!DRY_RUN) {
            this.fareAttributeRepository.save(entity);
        }
    }

    private void persistTransfer(GtfsItem item) {
        GtfsTransfer transfer = item.getEntity();
        Stop fromStop = this.stopRepository.findByStopId(transfer.getFromStopId());
        Stop toStop = this.stopRepository.findByStopId(transfer.getToStopId());
        TransferType transferType = TransferType.fromCode(transfer.getTransferType());
        long transferTime = (transfer.getMinTransferTimeSecs() != null) ? transfer.getMinTransferTimeSecs().longValue() : 0;
        Transfer entity = new Transfer(fromStop, toStop, transferType, transferTime);
        
        if (!DRY_RUN) {
            this.transferRepository.save(entity);
        }
    }

    private void persistStopTime(GtfsItem item) {
        GtfsStopTime stopTime = item.getEntity();
        Stop innerStop = this.stopRepository.findByStopId(stopTime.getStopId());
        Trip trip = this.tripRepository.findByTripId(stopTime.getTripId());
        Time arrival = Time.valueOf((stopTime.getArrivalTime() == null) ? "00-00-00" : stopTime.getArrivalTime());
        Time departure = Time.valueOf((stopTime.getDepartureTime() == null) ? "00-00-00" : stopTime.getDepartureTime());
        AvailabilityType pickupType = AvailabilityType.fromCode(stopTime.getPickupType());
        AvailabilityType dropoffType = AvailabilityType.fromCode(stopTime.getDropoffType());
        double distance = (stopTime.getShapeDistanceTraveled() != null) ? stopTime.getShapeDistanceTraveled().doubleValue() : 0;
        int sequence = stopTime.getStopSequence().intValue();
        String headsign = stopTime.getStopHeadsign();
        StopTime entity = new StopTime(trip, arrival, departure, innerStop, sequence, headsign, pickupType, dropoffType, distance);

        if (!DRY_RUN) {
            this.stopTimeRepository.save(entity);
        }
    }

    private void persistStop(GtfsItem item) {
        GtfsStop stop = item.getEntity();
        Location location = new Location(stop.getLat().doubleValue(), stop.getLng().doubleValue());
        StopLocationType type = StopLocationType.fromCode(stop.getLocationType());
        WheelchairType wheelchairType = WheelchairType.fromCode(stop.getWheelchairType());
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
        DirectionType direction = DirectionType.fromCode(trip.getDirectionType());
        WheelchairType wheelchairType = WheelchairType.fromCode(trip.getWheelchairType());
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
        RouteType type = RouteType.fromCode(route.getRouteType());
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
            ExceptionType exception = ExceptionType.fromCode(calendarDate.getExceptionType());
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
