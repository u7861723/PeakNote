package com.peaknote.demo.exception;

/**
 * Subscription service related exceptions
 */
public class SubscriptionException extends PeakNoteException {
    
    public SubscriptionException(String message) {
        super("SUBSCRIPTION_ERROR", message);
    }
    
    public SubscriptionException(String message, Throwable cause) {
        super("SUBSCRIPTION_ERROR", message, cause);
    }
} 