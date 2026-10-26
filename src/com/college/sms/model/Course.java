package com.college.sms.model;

import java.util.Objects;

/**
 * Represents an academic Course in the system.
 * Encapsulates course code, title, credits, instructor, and seating capacity.
 */
public class Course {
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String instructor;
    private int maxCapacity;

    // Default constructor
    public Course() {
    }

    // Parameterized constructor
    public Course(String courseCode, String courseTitle, int credits, String instructor, int maxCapacity) {
        setCourseCode(courseCode);
        setCourseTitle(courseTitle);
        setCredits(credits);
        setInstructor(instructor);
        setMaxCapacity(maxCapacity);
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Course code cannot be empty.");
        }
        this.courseCode = courseCode.trim().toUpperCase();
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        if (courseTitle == null || courseTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Course title cannot be empty.");
        }
        this.courseTitle = courseTitle.trim();
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        if (credits < 1 || credits > 10) {
            throw new IllegalArgumentException("Course credits must be between 1 and 10.");
        }
        this.credits = credits;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        if (instructor == null || instructor.trim().isEmpty()) {
            throw new IllegalArgumentException("Instructor name cannot be empty.");
        }
        this.instructor = instructor.trim();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        if (maxCapacity < 1) {
            throw new IllegalArgumentException("Course capacity must be at least 1 student.");
        }
        this.maxCapacity = maxCapacity;
    }

    // Display summary method
    public String getDisplayDetails() {
        return String.format("[%s] %s (%d Credits) | Instructor: %s | Max Seats: %d",
                courseCode, courseTitle, credits, instructor, maxCapacity);
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseTitle + " (" + credits + " Credits)";
    }

    // Uniqueness of Course based on courseCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(courseCode, course.courseCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseCode);
    }
}
