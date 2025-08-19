# PeakNote Error Handling Mechanism

This document describes the error handling mechanism and best practices for the PeakNote backend application.

## Overview

The PeakNote application adopts a multi-layered error handling strategy to ensure application stability and maintainability.

## Error Handling Layers

### 1. Global Exception Handler (GlobalExceptionHandler)

Located at `com.peaknote.demo.config.GlobalExceptionHandler`, it provides unified exception handling for various exceptions in the application:

- **General Exceptions**: Handle uncaught exceptions
- **Runtime Exceptions**: Handle runtime errors
- **Illegal Argument Exceptions**: Handle parameter validation failures
- **Null Pointer Exceptions**: Handle null reference errors
- **Database Operation Exceptions**: Handle data access errors
- **Network Connection Exceptions**: Handle network-related issues
- **Custom Exceptions**: Handle PeakNote-specific exceptions

### 2. Custom Exception Classes

#### PeakNoteException (Base Class)
- Base class for all PeakNote custom exceptions
- Contains error code and message

#### GraphServiceException
- Handle Microsoft Graph API related errors
- Error code: `GRAPH_SERVICE_ERROR`

#### SubscriptionException
- Handle subscription service related errors
- Error code: `SUBSCRIPTION_ERROR`

### 3. Error Handling Utility Class (ErrorHandler)

Located at `com.peaknote.demo.util.ErrorHandler`, provides common error handling methods:

- `safeExecute()`: Safely execute operations with automatic exception catching
- `validateNotNull()`: Validate parameters are not null
- `validateNotEmpty()`: Validate strings are not empty
- Various logging methods

## Usage Examples

### Using try-catch in Service Classes

```java
public void someMethod() {
    try {
        // Business logic
        performOperation();
    } catch (Exception e) {
        log.error("❌ Operation failed: {}", e.getMessage(), e);
        throw new PeakNoteException("Operation failed", e);
    }
}
```

### Using Error Handling Utility Class

```java
import com.peaknote.demo.util.ErrorHandler;

public void someMethod() {
    // Parameter validation
    ErrorHandler.validateNotEmpty(userId, "User ID");
    
    // Safe execution
    boolean success = ErrorHandler.safeExecute("User sync", () -> {
        syncUser(userId);
    });
    
    if (!success) {
        log.warn("⚠️ User sync failed");
    }
}
```

### Exception Handling in Controllers

```java
@GetMapping("/data")
public ResponseEntity<?> getData(@RequestParam String id) {
    try {
        ErrorHandler.validateNotEmpty(id, "ID");
        Object data = service.getData(id);
        return ResponseEntity.ok(data);
    } catch (PeakNoteException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", e.getErrorCode(), "message", e.getMessage()));
    }
}
```

## Error Response Format

When exceptions occur, the API returns a unified error response format:

```json
{
    "error": "ERROR_CODE",
    "message": "Error description",
    "timestamp": 1234567890,
    "path": "/api/endpoint"
}
```

## Logging

The application uses structured logging with:

- **Error Level**: ❌ indicates errors
- **Warning Level**: ⚠️ indicates warnings
- **Success Level**: ✅ indicates success
- **Info Level**: ℹ️ indicates general information

## Best Practices

### 1. Exception Handling Principles

- Always catch specific exception types
- Provide meaningful error messages
- Log detailed error information
- Never ignore exceptions

### 2. Parameter Validation

- Validate parameters at the beginning of methods
- Use `ErrorHandler.validateNotNull()` and `ErrorHandler.validateNotEmpty()`
- Provide clear error messages

### 3. Resource Management

- Use try-with-resources statements
- Ensure resources are properly closed
- Handle resource acquisition failures

### 4. Asynchronous Operations

- Add timeout mechanisms for async operations
- Handle exceptions in async operations
- Provide cancellation mechanisms

## Configuration

Error handling related configurations are located at:

- `application.properties`: Log level configuration
- `logback-spring.xml`: Log format configuration

## Monitoring and Alerting

Recommended configurations:

- Error rate monitoring
- Exception type statistics
- Real-time alerts for critical errors
- Error trend analysis

## Troubleshooting

### Common Issues

1. **Exceptions being swallowed**: Check for empty catch blocks
2. **Incomplete logs**: Check log level configuration
3. **Inconsistent error response format**: Check global exception handler

### Debugging Tips

- Enable DEBUG log level
- Use breakpoints to debug exception handling flow
- Check exception stack traces

## Summary

The PeakNote error handling mechanism provides:

- Unified exception handling strategy
- Detailed error logging
- Friendly error response format
- Extensible custom exception system
- Practical error handling utilities

By following these best practices, you can build more robust and maintainable applications. 