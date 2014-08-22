package com.trein.gtfs.service.exception;

public class QueryOutOfLimitsException extends ResourceNotFoundException {
    
    private static final long serialVersionUID = -3605583458706181165L;
    
    public QueryOutOfLimitsException() {
	super("Requisições passaram do limite estabelecido. Por favor, tente mais tarde."); //$NON-NLS-1$
    }
}
