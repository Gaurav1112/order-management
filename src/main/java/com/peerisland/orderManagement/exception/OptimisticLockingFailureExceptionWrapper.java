package com.peerisland.orderManagement.exception;

/**
 * Wrapper for Spring's OptimisticLockingFailureException to make
 * it more descriptive and to allow easier exception handling in services/schedulers.
 */
public class OptimisticLockingFailureExceptionWrapper extends RuntimeException {

    public OptimisticLockingFailureExceptionWrapper(String message, Throwable cause) {
        super(message, cause);
    }

    public OptimisticLockingFailureExceptionWrapper(String message) {
        super(message);
    }

    public OptimisticLockingFailureExceptionWrapper(Throwable cause) {
        super(cause);
    }
}
