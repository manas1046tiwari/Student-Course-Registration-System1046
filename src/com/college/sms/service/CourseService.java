package com.college.sms.service;

import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.exception.SMSException;
import com.college.sms.exception.ValidationException;
import com.college.sms.model.Course;

import com.college.sms.storage.FileStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service managing business logic for Course entities.
 * Handles storage in ArrayList, validation, search, and referential integrity checks.
 */
public class CourseService {
    private final List<Course> courses = new ArrayList<>();
    private RegistrationService registrationService;
    private FileStorage fileStorage;

    public CourseService() {
    }

    /**
     * Connects RegistrationService to perform referential integrity checks.
     */
    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Connects FileStorage for persistence.
     */
    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    private void saveToFile() throws SMSException {
        if (fileStorage != null) {
            fileStorage.saveCourses(courses);
        }
    }

    /**
     * Populates in-memory courses list from storage.
     */
    public void loadInitialData(List<Course> loadedList) {
        courses.clear();
        if (loadedList != null) {
            courses.addAll(loadedList);
        }
    }

    /**
     * Adds a new course to the system.
     * Validates fields, ensures unique courseCode, and saves to file.
     */
    public void addCourse(Course course) throws SMSException {
        if (course == null) {
            throw new ValidationException("Course cannot be null.");
        }
        validateCourseData(course);

        if (findCourseByCode(course.getCourseCode()) != null) {
            throw new DuplicateResourceException("Course code '" + course.getCourseCode() + "' already exists.");
        }

        courses.add(course);
        saveToFile();
    }

    /**
     * Updates an existing course's details.
     */
    public void updateCourse(Course updatedCourse) throws SMSException {
        if (updatedCourse == null) {
            throw new ValidationException("Course cannot be null.");
        }
        validateCourseData(updatedCourse);

        Course existingCourse = getCourseByCode(updatedCourse.getCourseCode());

        // Check if new capacity is smaller than active enrollments
        if (registrationService != null) {
            int activeEnrolled = registrationService.getActiveRegistrationCountForCourse(updatedCourse.getCourseCode());
            if (updatedCourse.getMaxCapacity() < activeEnrolled) {
                throw new ValidationException("Cannot reduce course capacity to " + updatedCourse.getMaxCapacity() +
                        ". There are currently " + activeEnrolled + " students actively enrolled.");
            }
        }

        existingCourse.setCourseTitle(updatedCourse.getCourseTitle());
        existingCourse.setCredits(updatedCourse.getCredits());
        existingCourse.setInstructor(updatedCourse.getInstructor());
        existingCourse.setMaxCapacity(updatedCourse.getMaxCapacity());
        saveToFile();
    }

    /**
     * Deletes a course.
     * Prevents deletion if students are actively registered for this course.
     */
    public void deleteCourse(String courseCode) throws SMSException {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new ValidationException("Course code cannot be empty.");
        }

        Course course = getCourseByCode(courseCode);

        // Referential integrity check: prevent orphan registrations
        if (registrationService != null && registrationService.hasActiveRegistrationsForCourse(course.getCourseCode())) {
            throw new ValidationException("Cannot delete course '" + course.getCourseCode() +
                    "' because students are actively enrolled in it. Cancel all active registrations first.");
        }

        courses.remove(course);
        saveToFile();
    }

    /**
     * Finds a course by code without throwing an exception (returns null if not found).
     */
    public Course findCourseByCode(String courseCode) {
        if (courseCode == null) return null;
        for (Course course : courses) {
            if (course.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                return course;
            }
        }
        return null;
    }

    /**
     * Gets a course by code, throwing ResourceNotFoundException if missing.
     */
    public Course getCourseByCode(String courseCode) throws ResourceNotFoundException {
        Course course = findCourseByCode(courseCode);
        if (course == null) {
            throw new ResourceNotFoundException("Course with code '" + courseCode + "' was not found.");
        }
        return course;
    }

    /**
     * Searches courses by courseCode, title, or instructor (case-insensitive).
     */
    public List<Course> searchCourses(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCourses();
        }
        String lowerQuery = query.trim().toLowerCase();
        List<Course> results = new ArrayList<>();
        for (Course course : courses) {
            if (course.getCourseCode().toLowerCase().contains(lowerQuery) ||
                course.getCourseTitle().toLowerCase().contains(lowerQuery) ||
                course.getInstructor().toLowerCase().contains(lowerQuery)) {
                results.add(course);
            }
        }
        return results;
    }

    /**
     * Returns an unmodifiable view of all courses.
     */
    public List<Course> getAllCourses() {
        return Collections.unmodifiableList(new ArrayList<>(courses));
    }

    /**
     * Returns total number of registered courses.
     */
    public int getCourseCount() {
        return courses.size();
    }

    /**
     * Calculates remaining available seats for a course.
     */
    public int getAvailableSeats(String courseCode) throws ResourceNotFoundException {
        Course course = getCourseByCode(courseCode);
        if (registrationService == null) {
            return course.getMaxCapacity();
        }
        int activeEnrollments = registrationService.getActiveRegistrationCountForCourse(courseCode);
        return Math.max(0, course.getMaxCapacity() - activeEnrollments);
    }

    /**
     * Validates basic course fields.
     */
    private void validateCourseData(Course course) throws ValidationException {
        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            throw new ValidationException("Course code is required.");
        }
        if (course.getCourseCode().contains(",") || course.getCourseCode().contains(" ")) {
            throw new ValidationException("Course code cannot contain spaces or commas.");
        }
        if (course.getCourseTitle() == null || course.getCourseTitle().trim().isEmpty()) {
            throw new ValidationException("Course title is required.");
        }
        if (course.getCredits() < 1 || course.getCredits() > 10) {
            throw new ValidationException("Credits must be between 1 and 10.");
        }
        if (course.getInstructor() == null || course.getInstructor().trim().isEmpty()) {
            throw new ValidationException("Instructor name is required.");
        }
        if (course.getMaxCapacity() < 1) {
            throw new ValidationException("Maximum capacity must be at least 1.");
        }
    }
}
