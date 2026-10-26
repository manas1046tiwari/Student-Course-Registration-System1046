package com.college.sms.exception;

/**
 * Base checked exception class for Student Management System business errors.
 */
public class SMSException extends Exception {
    public SMSException(String message) {
        super(message);
    }

    public SMSException(String message, Throwable cause) {
        super(message, cause);
    }
}
