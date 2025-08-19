package com.peaknote.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.peaknote.demo.exception.PeakNoteException;

/**
 * Error handling utility class
 * Provides common error handling methods and logging utilities
 */
public class ErrorHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    
    /**
     * Safely execute operation, catch exceptions and log them
     * 
     * @param operation Description of the operation to execute
     * @param runnable The operation to execute
     * @return Whether the operation was successful
     */
    public static boolean safeExecute(String operation, Runnable runnable) {
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            log.error("❌ Operation execution failed: {} - {}", operation, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Safely execute operation, catch exceptions and log them, return default value
     * 
     * @param operation Description of the operation to execute
     * @param supplier The operation to execute
     * @param defaultValue Default return value
     * @return Operation result or default value
     */
    public static <T> T safeExecute(String operation, java.util.function.Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("❌ Operation execution failed: {} - {}", operation, e.getMessage(), e);
            return defaultValue;
        }
    }
    
    /**
     * Validate that parameter is not null
     * 
     * @param value The value to validate
     * @param paramName Parameter name
     * @throws PeakNoteException if parameter is null
     */
    public static void validateNotNull(Object value, String paramName) {
        if (value == null) {
            throw new PeakNoteException("Parameter cannot be null: " + paramName);
        }
    }
    
    /**
     * Validate that string is not empty
     * 
     * @param value The string to validate
     * @param paramName Parameter name
     * @throws PeakNoteException if string is empty
     */
    public static void validateNotEmpty(String value, String paramName) {
        if (value == null || value.trim().isEmpty()) {
            throw new PeakNoteException("String parameter cannot be empty: " + paramName);
        }
    }
    
    /**
     * Log warning message
     * 
     * @param message Warning message
     * @param args Message parameters
     */
    public static void logWarning(String message, Object... args) {
        log.warn("⚠️ " + message, args);
    }
    
    /**
     * Log error message
     * 
     * @param message Error message
     * @param args Message parameters
     */
    public static void logError(String message, Object... args) {
        log.error("❌ " + message, args);
    }
    
    /**
     * Log success message
     * 
     * @param message Success message
     * @param args Message parameters
     */
    public static void logSuccess(String message, Object... args) {
        log.info("✅ " + message, args);
    }
    
    /**
     * Log info message
     * 
     * @param message Info message
     * @param args Message parameters
     */
    public static void logInfo(String message, Object... args) {
        log.info("ℹ️ " + message, args);
    }
} 