package com.trein.gtfs.service.exception;

public class ResourceNotFoundException extends Exception {
    
    private static final long serialVersionUID = 8062306968958714655L;
    
    public ResourceNotFoundException(String message) {
	super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable e) {
	super(message, e);
    }
    
}
