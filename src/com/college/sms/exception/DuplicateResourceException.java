package com.college.sms.exception;

/**
 * Thrown when attempting to insert an entity with an ID that already exists,
 * or when duplicate registrations are detected.
 */
public class DuplicateResourceException extends SMSException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
