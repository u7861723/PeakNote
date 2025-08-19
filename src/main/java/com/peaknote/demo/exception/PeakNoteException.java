package com.peaknote.demo.exception;

/**
 * Custom exception base class for PeakNote application
 */
public class PeakNoteException extends RuntimeException {
    
    private final String errorCode;
    
    public PeakNoteException(String message) {
        super(message);
        this.errorCode = "PEAKNOTE_ERROR";
    }
    
    public PeakNoteException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "PEAKNOTE_ERROR";
    }
    
    public PeakNoteException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PeakNoteException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 