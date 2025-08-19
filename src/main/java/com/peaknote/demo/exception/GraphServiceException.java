package com.peaknote.demo.exception;

/**
 * Graph service related exceptions
 */
public class GraphServiceException extends PeakNoteException {
    
    public GraphServiceException(String message) {
        super("GRAPH_SERVICE_ERROR", message);
    }
    
    public GraphServiceException(String message, Throwable cause) {
        super("GRAPH_SERVICE_ERROR", message, cause);
    }
} 