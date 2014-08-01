package com.trein.gtfs.etl.job;

import java.io.IOException;
import java.io.InputStream;
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

    private static final String ENTITY_KEY = "gtfs_class";
    
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
    @SuppressWarnings("unchecked")
    @Override
    public GtfsItem read() throws Exception, UnexpectedInputException, ParseException {
        Object parsedEntity = this.reader.readNext();
        boolean hasReachedEndOfFile = parsedEntity == null;
        
        // If there is no resource, then this is the first item, set the current
        // resource to 0 and open the first delegate.
        if (hasReachedEndOfFile) {
            this.reader.close();
            
            while (this.entityIterator.hasNext()) {
                this.currentCount = 0;
                this.currentEntityClass = (Class<Object>) this.entityIterator.next();
                if (isValidEntity()) {
                    this.reader = createNextEntityReader();
                    parsedEntity = this.reader.readNext();
                    break;
                }
            }
        }
        
        // Signals there are no resources to read -> just return null on first read
        if (parsedEntity == null) { return null; }
        
        this.currentCount++;
        if ((this.currentCount % 10000) == 0) {
            LOGGER.info("Handling entry [{}] from file [{}]", this.currentEntityClass, String.valueOf(this.currentCount));
        }
        
        return new GtfsItem(this.currentEntityClass, parsedEntity);
    }
    
    private boolean isValidEntity() {
        GtfsFile annotation = this.currentEntityClass.getAnnotation(GtfsFile.class);
        String path = getEntityFilePath();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        
        if (stream == null) {
            if (!annotation.optional()) {
                throw new IllegalStateException("mandatory GTFS file not found");
            } else {
                LOGGER.warn("Optional GTFS file [{}] not found", path);
            }
        }
        return stream != null;
    }
    
    private CSVReader<?> createNextEntityReader() {
        LOGGER.info("Loading file [{}]", this.currentEntityClass);
        String path = getEntityFilePath();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        Reader csv = new InputStreamReader(stream);
        ValueProcessorProvider processor = new ValueProcessorProvider();
        CSVHeaderAwareEntryParser<Object> entryParser = new CSVHeaderAwareEntryParser<Object>(this.currentEntityClass, processor);
        return new CSVHeaderAwareReaderBuilder<Object>(csv).entryParser(entryParser).build();
    }
    
    private String getEntityFilePath() {
        GtfsFile annotation = this.currentEntityClass.getAnnotation(GtfsFile.class);
        String filename = annotation.value();
        String path = this.baseDir + filename;
        return path;
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
    @SuppressWarnings("unchecked")
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
                    break;
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
