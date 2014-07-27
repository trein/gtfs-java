package com.trein.gtfs.etl.job;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.util.ClassUtils;

import com.googlecode.jcsv.annotations.internal.ValueProcessorProvider;
import com.googlecode.jcsv.reader.CSVReader;
import com.trein.gtfs.csv.annotations.GtfsFile;
import com.trein.gtfs.csv.reader.CSVHeaderAwareEntryParser;
import com.trein.gtfs.csv.reader.CSVHeaderAwareReaderBuilder;
import com.trein.gtfs.csv.vo.GtfsAgency;
import com.trein.gtfs.csv.vo.GtfsCalendar;
import com.trein.gtfs.csv.vo.GtfsCalendarDate;
import com.trein.gtfs.csv.vo.GtfsRoute;
import com.trein.gtfs.csv.vo.GtfsShape;
import com.trein.gtfs.csv.vo.GtfsStop;
import com.trein.gtfs.csv.vo.GtfsStopTime;
import com.trein.gtfs.csv.vo.GtfsTransfer;
import com.trein.gtfs.csv.vo.GtfsTrip;

public class GtfsItemReader extends AbstractItemStreamItemReader<GtfsItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GtfsItemReader.class);
    
    private static final List<Class<?>> ENTITIES = Arrays.asList(GtfsAgency.class, GtfsCalendarDate.class, GtfsCalendar.class,
            GtfsRoute.class, GtfsShape.class, GtfsStopTime.class, GtfsStop.class, GtfsTransfer.class, GtfsTrip.class);
    
    private static final String ENTITY_KEY = "";
    
    private Iterator<Class<?>> entityIterator;
    private Class<Object> currentEntityClass;
    private CSVReader<?> reader;
    private boolean saveState = true;
    private final String baseDir;
    private int currentCount;
    
    public GtfsItemReader(String baseDir) {
        this.baseDir = baseDir;
        setExecutionContextName(ClassUtils.getShortName(MultiResourceItemReader.class));
    }
    
    /**
     * Reads the next item, jumping to next resource if necessary.
     */
    @Override
    public GtfsItem read() throws Exception, UnexpectedInputException, ParseException {
        Object parsedEntity = this.reader.readNext();
        boolean hasReachedEndOfFile = parsedEntity == null;

        // If there is no resource, then this is the first item, set the current
        // resource to 0 and open the first delegate.
        if (hasReachedEndOfFile) {
            if (this.entityIterator.hasNext()) {
                this.currentCount = 0;
                this.currentEntityClass = (Class<Object>) this.entityIterator.next();
                this.reader.close();
                this.reader = createNextEntityReader();
                parsedEntity = this.reader.readNext();
            } else {
                // Signals there are no resources to read -> just return null on first read
                return null;
            }
        }

        this.currentCount++;
        if ((this.currentCount % 10000) == 0) {
            LOGGER.debug("Handling entry [%s] from file [%s]", this.currentEntityClass, String.valueOf(this.currentCount));
        }

        return new GtfsItem(this.currentEntityClass, parsedEntity);
    }
    
    private CSVReader<?> createNextEntityReader() {
        String filename = this.currentEntityClass.getAnnotation(GtfsFile.class).value();
        LOGGER.debug("Handling file [%s]", this.currentEntityClass);

        String path = this.baseDir + filename;
        Reader csv = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path));
        
        ValueProcessorProvider processor = new ValueProcessorProvider();
        CSVHeaderAwareEntryParser<Object> entryParser = new CSVHeaderAwareEntryParser<Object>(this.currentEntityClass, processor);
        return new CSVHeaderAwareReaderBuilder<Object>(csv).entryParser(entryParser).build();
    }
    
    /**
     * Close the {@link #setDelegate(ResourceAwareItemReaderItemStream)} reader and reset instance
     * variable values.
     */
    @Override
    public void close() throws ItemStreamException {
        super.close();
        try {
            this.reader.close();
        } catch (IOException e) {
            throw new ItemStreamException(e.getMessage());
        }
    }
    
    /**
     * Figure out which resource to start with in case of restart, open the delegate and restore
     * delegate's position in the resource.
     */
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        super.open(executionContext);
        this.currentCount = 0;
        this.entityIterator = ENTITIES.iterator();
        
        if (executionContext.containsKey(getExecutionContextKey(ENTITY_KEY))) {
            String entityClassName = executionContext.getString(getExecutionContextKey(ENTITY_KEY));
            while (this.entityIterator.hasNext()) {
                Class<?> entityClass = this.entityIterator.next();
                if (entityClassName.equals(entityClass.getName())) {
                    this.currentEntityClass = (Class<Object>) entityClass;
                }
            }
        }

        if (this.currentEntityClass == null) {
            this.entityIterator = ENTITIES.iterator();
            this.currentEntityClass = (Class<Object>) this.entityIterator.next();
        }

        this.reader = createNextEntityReader();
    }
    
    /**
     * Store the current resource index and position in the resource.
     */
    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        super.update(executionContext);
        if (this.saveState) {
            executionContext.putString(getExecutionContextKey(ENTITY_KEY), this.currentEntityClass.getName());
        }
    }
    
    public void setSaveState(boolean saveState) {
        this.saveState = saveState;
    }
    
}
