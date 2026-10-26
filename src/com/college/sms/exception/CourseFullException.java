package com.college.sms.exception;

/**
 * Thrown when trying to register for a course whose active enrollment
 * has reached maximum seating capacity.
 */
public class CourseFullException extends SMSException {
    public CourseFullException(String message) {
        super(message);
    }
}
