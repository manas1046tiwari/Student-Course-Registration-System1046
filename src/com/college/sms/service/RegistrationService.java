package com.college.sms.service;

import com.college.sms.exception.CourseFullException;
import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.exception.SMSException;
import com.college.sms.exception.ValidationException;
import com.college.sms.model.Course;
import com.college.sms.model.Registration;
import com.college.sms.model.Student;

import com.college.sms.storage.FileStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service managing business logic for Course Registrations.
 * Coordinates between StudentService and CourseService to enforce enrollment rules,
 * prevent duplicate enrollments, manage capacity limits, and maintain referential integrity.
 */
public class RegistrationService {
    private final List<Registration> registrations = new ArrayList<>();
    private final StudentService studentService;
    private final CourseService courseService;
    private FileStorage fileStorage;
    private int idCounter = 1001;

    /**
     * Initializes RegistrationService and wires bi-directional references
     * so that StudentService and CourseService can verify referential integrity.
     */
    public RegistrationService(StudentService studentService, CourseService courseService) {
        if (studentService == null || courseService == null) {
            throw new IllegalArgumentException("StudentService and CourseService cannot be null.");
        }
        this.studentService = studentService;
        this.courseService = courseService;

        // Wire dependencies to allow referential integrity checks during deletions
        this.studentService.setRegistrationService(this);
        this.courseService.setRegistrationService(this);
    }

    /**
     * Connects FileStorage for persistence.
     */
    public void setFileStorage(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    private void saveToFile() throws SMSException {
        if (fileStorage != null) {
            fileStorage.saveRegistrations(registrations);
        }
    }

    /**
     * Populates in-memory registrations list from storage.
     */
    public void loadInitialData(List<Registration> loadedList) {
        registrations.clear();
        if (loadedList != null) {
            registrations.addAll(loadedList);
        }
    }

    /**
     * Registers a student for a course with an auto-generated registration ID.
     */
    public Registration registerStudent(String studentId, String courseCode) throws SMSException {
        String today = LocalDate.now().toString();
        return registerStudent(studentId, courseCode, today);
    }

    /**
     * Registers a student for a course with an auto-generated sequential registration ID and custom date.
     */
    public Registration registerStudent(String studentId, String courseCode, String registrationDate) throws SMSException {
        String generatedId = generateRegistrationId();
        return registerStudent(generatedId, studentId, courseCode, registrationDate);
    }

    /**
     * Registers a student for a course with a specific registration ID and date.
     * Enforces:
     * 1. ID and field presence
     * 2. Student existence
     * 3. Course existence
     * 4. Uniqueness of registration ID
     * 5. Prevention of duplicate student-course enrollment
     * 6. Course seating capacity limit
     */
    public Registration registerStudent(String registrationId, String studentId, String courseCode, String registrationDate) throws SMSException {
        if (registrationId == null || registrationId.trim().isEmpty()) {
            throw new ValidationException("Registration ID is required.");
        }
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ValidationException("Student ID is required.");
        }
        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new ValidationException("Course code is required.");
        }
        if (registrationDate == null || registrationDate.trim().isEmpty()) {
            throw new ValidationException("Registration date is required.");
        }

        // 1. Ensure registration ID is unique
        if (findRegistrationById(registrationId) != null) {
            throw new DuplicateResourceException("Registration ID '" + registrationId + "' already exists.");
        }

        // 2. Validate Student exists
        Student student = studentService.getStudentById(studentId.trim());

        // 3. Validate Course exists
        Course course = courseService.getCourseByCode(courseCode.trim());

        // 4. Prevent duplicate registration of the same student in the same course
        if (isAlreadyRegistered(student.getStudentId(), course.getCourseCode())) {
            throw new DuplicateResourceException("Student '" + student.getName() + " (" + student.getStudentId() + ")' " +
                    "is already actively enrolled in course '" + course.getCourseTitle() + " (" + course.getCourseCode() + ")'.");
        }

        // 5. Check seating capacity
        int activeCount = getActiveRegistrationCountForCourse(course.getCourseCode());
        if (activeCount >= course.getMaxCapacity()) {
            throw new CourseFullException("Cannot enroll: Course '" + course.getCourseTitle() + " (" + course.getCourseCode() + ")' " +
                    "is already at maximum capacity (" + course.getMaxCapacity() + " seats).");
        }

        // 6. Create and store registration
        Registration registration = new Registration(
                registrationId.trim(),
                student,
                course,
                registrationDate.trim(),
                Registration.STATUS_ACTIVE
        );

        registrations.add(registration);
        saveToFile();
        return registration;
    }

    /**
     * Cancels an existing active registration by its registration ID.
     */
    public void cancelRegistration(String registrationId) throws SMSException {
        if (registrationId == null || registrationId.trim().isEmpty()) {
            throw new ValidationException("Registration ID is required.");
        }

        Registration registration = getRegistrationById(registrationId);

        if (!registration.isActive()) {
            throw new ValidationException("Registration '" + registrationId + "' is already cancelled.");
        }

        registration.cancel();
        saveToFile();
    }

    /**
     * Cancels a student's active registration for a specific course.
     */
    public void cancelRegistration(String studentId, String courseCode) throws SMSException {
        Registration registration = findActiveRegistration(studentId, courseCode);
        if (registration == null) {
            throw new ResourceNotFoundException("No active registration found for student '" +
                    studentId + "' in course '" + courseCode + "'.");
        }
        registration.cancel();
        saveToFile();
    }

    /**
     * Finds a registration by its ID (returns null if not found).
     */
    public Registration findRegistrationById(String registrationId) {
        if (registrationId == null) return null;
        for (Registration reg : registrations) {
            if (reg.getRegistrationId().equalsIgnoreCase(registrationId.trim())) {
                return reg;
            }
        }
        return null;
    }

    /**
     * Retrieves a registration by ID, throwing ResourceNotFoundException if missing.
     */
    public Registration getRegistrationById(String registrationId) throws ResourceNotFoundException {
        Registration reg = findRegistrationById(registrationId);
        if (reg == null) {
            throw new ResourceNotFoundException("Registration with ID '" + registrationId + "' was not found.");
        }
        return reg;
    }

    /**
     * Finds the active registration connecting a student ID and course code.
     */
    public Registration findActiveRegistration(String studentId, String courseCode) {
        if (studentId == null || courseCode == null) return null;
        for (Registration reg : registrations) {
            if (reg.isDuplicateOf(studentId, courseCode)) {
                return reg;
            }
        }
        return null;
    }

    /**
     * Checks if a student is already actively enrolled in a course.
     */
    public boolean isAlreadyRegistered(String studentId, String courseCode) {
        return findActiveRegistration(studentId, courseCode) != null;
    }

    /**
     * Checks if a student has any active registrations (used for referential integrity before student deletion).
     */
    public boolean hasActiveRegistrationsForStudent(String studentId) {
        if (studentId == null) return false;
        for (Registration reg : registrations) {
            if (reg.isActive() && reg.getStudentId() != null && reg.getStudentId().equalsIgnoreCase(studentId.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a course has any active registrations (used for referential integrity before course deletion).
     */
    public boolean hasActiveRegistrationsForCourse(String courseCode) {
        if (courseCode == null) return false;
        for (Registration reg : registrations) {
            if (reg.isActive() && reg.getCourseCode() != null && reg.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts how many active students are enrolled in a specific course.
     */
    public int getActiveRegistrationCountForCourse(String courseCode) {
        if (courseCode == null) return 0;
        int count = 0;
        for (Registration reg : registrations) {
            if (reg.isActive() && reg.getCourseCode() != null && reg.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns all registrations (both ACTIVE and CANCELLED).
     */
    public List<Registration> getAllRegistrations() {
        return Collections.unmodifiableList(new ArrayList<>(registrations));
    }

    /**
     * Returns only currently active registrations.
     */
    public List<Registration> getActiveRegistrations() {
        List<Registration> activeList = new ArrayList<>();
        for (Registration reg : registrations) {
            if (reg.isActive()) {
                activeList.add(reg);
            }
        }
        return Collections.unmodifiableList(activeList);
    }

    /**
     * Returns all registrations for a specific student.
     */
    public List<Registration> getRegistrationsForStudent(String studentId) {
        if (studentId == null) return Collections.emptyList();
        List<Registration> result = new ArrayList<>();
        for (Registration reg : registrations) {
            if (reg.getStudentId() != null && reg.getStudentId().equalsIgnoreCase(studentId.trim())) {
                result.add(reg);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns all registrations for a specific course.
     */
    public List<Registration> getRegistrationsForCourse(String courseCode) {
        if (courseCode == null) return Collections.emptyList();
        List<Registration> result = new ArrayList<>();
        for (Registration reg : registrations) {
            if (reg.getCourseCode() != null && reg.getCourseCode().equalsIgnoreCase(courseCode.trim())) {
                result.add(reg);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Generates a unique sequential registration ID.
     */
    private synchronized String generateRegistrationId() {
        String id;
        do {
            id = "REG-" + (idCounter++);
        } while (findRegistrationById(id) != null);
        return id;
    }
}
