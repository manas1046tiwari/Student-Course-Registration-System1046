package com.college.sms.exception;

/**
 * Thrown when an entity (Student, Course, or Registration) cannot be found by its ID.
 */
public class ResourceNotFoundException extends SMSException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
