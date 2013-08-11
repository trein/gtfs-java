package com.trein.gtfs.etl.reader;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

public class CSVItemReaderBuilder<T> {
    
    private static final String DELIMITER = ",";
    private static final int LINES_TO_SKIP = 1;
    
    private final MultiResourceItemReader<T> reader;
    
    @Autowired
    public CSVItemReaderBuilder(FieldSetMapper<T> context, Resource... resources) {
	this.reader = new MultiResourceItemReader<T>();
	this.reader.setDelegate(createDelegate(context));
	this.reader.setResources(resources);
    }
    
    private FlatFileItemReader<T> createDelegate(FieldSetMapper<T> context) {
	FlatFileItemReader<T> itemReader = new FlatFileItemReader<T>();
	itemReader.setLineMapper(createMapper(context));
	itemReader.setLinesToSkip(LINES_TO_SKIP);
	return itemReader;
    }
    
    private DefaultLineMapper<T> createMapper(FieldSetMapper<T> context) {
	DefaultLineMapper<T> lineMapper = new DefaultLineMapper<T>();
	lineMapper.setLineTokenizer(new DelimitedLineTokenizer(DELIMITER));
	lineMapper.setFieldSetMapper(context);
	return lineMapper;
    }
    
    public MultiResourceItemReader<T> build() {
	return this.reader;
    }
}
