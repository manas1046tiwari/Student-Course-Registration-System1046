package com.college.sms.model;

import java.util.Objects;

/**
 * Represents the association between a Student and a Course.
 * Demonstrates the OOP relationship of Association (Has-A):
 * A Registration HAS-A Student and HAS-A Course.
 */
public class Registration {
    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private String registrationId;
    private Student student;
    private Course course;
    private String registrationDate;
    private String status;

    // Default constructor
    public Registration() {
        this.status = STATUS_ACTIVE;
    }

    // Parameterized constructor (defaulting to ACTIVE status)
    public Registration(String registrationId, Student student, Course course, String registrationDate) {
        this(registrationId, student, course, registrationDate, STATUS_ACTIVE);
    }

    // Full parameterized constructor
    public Registration(String registrationId, Student student, Course course, String registrationDate, String status) {
        setRegistrationId(registrationId);
        setStudent(student);
        setCourse(course);
        setRegistrationDate(registrationDate);
        setStatus(status);
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        if (registrationId == null || registrationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration ID cannot be empty.");
        }
        this.registrationId = registrationId.trim().toUpperCase();
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("A registration must be associated with a valid Student.");
        }
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("A registration must be associated with a valid Course.");
        }
        this.course = course;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        if (registrationDate == null || registrationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration date cannot be empty.");
        }
        this.registrationDate = registrationDate.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status == null || (!status.equalsIgnoreCase(STATUS_ACTIVE) && !status.equalsIgnoreCase(STATUS_CANCELLED))) {
            throw new IllegalArgumentException("Status must be either ACTIVE or CANCELLED.");
        }
        this.status = status.toUpperCase();
    }

    // --- Business Logic Methods ---

    /**
     * Checks if this registration is currently active.
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(this.status);
    }

    /**
     * Cancels this registration by changing status to CANCELLED.
     */
    public void cancel() {
        this.status = STATUS_CANCELLED;
    }

    /**
     * Helper method to identify if this active registration already connects
     * the specified student ID and course code.
     * Used by collections and services to prevent duplicate registrations.
     */
    public boolean isDuplicateOf(String studentId, String courseCode) {
        if (!isActive()) {
            return false;
        }
        boolean studentMatches = student != null && student.getStudentId().equalsIgnoreCase(studentId);
        boolean courseMatches = course != null && course.getCourseCode().equalsIgnoreCase(courseCode);
        return studentMatches && courseMatches;
    }

    // Convenience delegates for accessing IDs directly
    public String getStudentId() {
        return student != null ? student.getStudentId() : null;
    }

    public String getCourseCode() {
        return course != null ? course.getCourseCode() : null;
    }

    public String getDisplayDetails() {
        String studentInfo = (student != null) ? student.getName() + " (" + student.getStudentId() + ")" : "N/A";
        String courseInfo = (course != null) ? course.getCourseTitle() + " (" + course.getCourseCode() + ")" : "N/A";
        return String.format("[%s] Student: %s | Course: %s | Date: %s | Status: %s",
                registrationId, studentInfo, courseInfo, registrationDate, status);
    }

    @Override
    public String toString() {
        return registrationId + ": " + getStudentId() + " -> " + getCourseCode() + " [" + status + "]";
    }

    // Uniqueness of Registration based on registrationId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registration that = (Registration) o;
        return Objects.equals(registrationId, that.registrationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationId);
    }
}
