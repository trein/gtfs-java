package com.googlecode.jcsv.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import com.googlecode.jcsv.CSVStrategy;

/**
 * The CSVTokenizer specifies the behaviour how the CSVReaderImpl parses each line into a List of
 * Strings.
 */
public interface CSVTokenizer {
    /**
     * Splits the line into tokens, using the CSVStrategy, passed by the CSVReaderImpl.
     * 
     * @param line the current line
     * @param reader the reader may be used to read further lines if the line ends with an open
     *        quotation
     * @return the tokens
     */
    public List<String> tokenizeLine(String line, CSVStrategy strategy, BufferedReader reader) throws IOException;
}
