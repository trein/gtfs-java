package com.googlecode.jcsv;

public class CSVStrategy {
    
    /**
     * The default CSV strategy. - delimiter ; - quote character " - comment indicator # - do not
     * skip header - ignore empty lines
     */
    public static final CSVStrategy DEFAULT = new CSVStrategy(';', '"', '#', false, true);
    
    /**
     * The USA/UK csv standard. - delimiter , - quote character " - comment indicator # - do not
     * skip header - ignore empty lines
     */
    public static final CSVStrategy UK_DEFAULT = new CSVStrategy(',', '"', '#', false, true);
    
    private final char delimiter;
    private final char quoteCharacter;
    private final char commentIndicator;
    private final boolean skipHeader;
    private final boolean ignoreEmptyLines;
    
    /**
     * Creates a CSVStrategy.
     * 
     * @param delimiter
     * @param quoteCharacter
     * @param commentIndicator
     * @param skipHeader
     * @param ignoreEmptyLines
     */
    public CSVStrategy(char delimiter, char quoteCharacter, char commentIndicator, boolean skipHeader, boolean ignoreEmptyLines) {
	this.delimiter = delimiter;
	this.quoteCharacter = quoteCharacter;
	this.commentIndicator = commentIndicator;
	this.skipHeader = skipHeader;
	this.ignoreEmptyLines = ignoreEmptyLines;
    }
    
    /**
     * Returns the delimiter character.
     */
    public char getDelimiter() {
	return this.delimiter;
    }
    
    /**
     * Returns the quote character.
     */
    public char getQuoteCharacter() {
	return this.quoteCharacter;
    }
    
    /**
     * Returns the comment indicator.
     */
    public char getCommentIndicator() {
	return this.commentIndicator;
    }
    
    /**
     * Skip the header?
     * 
     * @return true, if the csv header should be skipped.
     */
    public boolean isSkipHeader() {
	return this.skipHeader;
    }
    
    /**
     * Ignore empty lines?
     * 
     * @return true, if empty lines should be ignored.
     */
    public boolean isIgnoreEmptyLines() {
	return this.ignoreEmptyLines;
    }
}
