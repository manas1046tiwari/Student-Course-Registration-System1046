package com.college.sms.exception;

/**
 * Thrown when input validation fails (e.g. empty fields, negative numbers, invalid formats).
 */
public class ValidationException extends SMSException {
    public ValidationException(String message) {
        super(message);
    }
}
