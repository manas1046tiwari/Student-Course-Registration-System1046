package com.college.sms.service;

import com.college.sms.exception.DuplicateResourceException;
import com.college.sms.exception.ResourceNotFoundException;
import com.college.sms.exception.SMSException;
import com.college.sms.exception.ValidationException;
import com.college.sms.model.Student;

import com.college.sms.storage.FileStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service managing business logic for Student entities.
 * Handles storage in ArrayList, input validation, search, and referential integrity checks.
 */
public class StudentService {
    private final List<Student> students = new ArrayList<>();
    private RegistrationService registrationService;
    private FileStorage fileStorage;

    public StudentService() {
    }

    /**
     * Connects RegistrationService to perform referential integrity checks before deletion.
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
            fileStorage.saveStudents(students);
        }
    }

    /**
     * Populates in-memory students list from storage.
     */
    public void loadInitialData(List<Student> loadedList) {
        students.clear();
        if (loadedList != null) {
            students.addAll(loadedList);
        }
    }

    /**
     * Adds a new student to the system.
     * Validates input fields, ensures studentId is unique, and saves to file.
     */
    public void addStudent(Student student) throws SMSException {
        if (student == null) {
            throw new ValidationException("Student cannot be null.");
        }
        validateStudentData(student);

        if (findStudentById(student.getStudentId()) != null) {
            throw new DuplicateResourceException("Student ID '" + student.getStudentId() + "' already exists.");
        }

        students.add(student);
        saveToFile();
    }

    /**
     * Updates an existing student's personal and academic details.
     */
    public void updateStudent(Student updatedStudent) throws SMSException {
        if (updatedStudent == null) {
            throw new ValidationException("Student cannot be null.");
        }
        validateStudentData(updatedStudent);

        Student existing = getStudentById(updatedStudent.getStudentId());
        existing.setName(updatedStudent.getName());
        existing.setEmail(updatedStudent.getEmail());
        existing.setDepartment(updatedStudent.getDepartment());
        existing.setSemester(updatedStudent.getSemester());
        existing.setPhone(updatedStudent.getPhone());
        saveToFile();
    }

    /**
     * Deletes a student from the system.
     * Prevents deletion if the student has active course registrations.
     */
    public void deleteStudent(String studentId) throws SMSException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ValidationException("Student ID cannot be empty.");
        }

        Student student = getStudentById(studentId);

        // Referential integrity check: prevent orphan registrations
        if (registrationService != null && registrationService.hasActiveRegistrationsForStudent(student.getStudentId())) {
            throw new ValidationException("Cannot delete student '" + student.getStudentId() +
                    "' because they have active course registrations. Cancel all registrations first.");
        }

        students.remove(student);
        saveToFile();
    }

    /**
     * Finds a student by ID without throwing an exception (returns null if not found).
     */
    public Student findStudentById(String studentId) {
        if (studentId == null) return null;
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(studentId.trim())) {
                return student;
            }
        }
        return null;
    }

    /**
     * Retrieves a student by ID, throwing ResourceNotFoundException if missing.
     */
    public Student getStudentById(String studentId) throws ResourceNotFoundException {
        Student student = findStudentById(studentId);
        if (student == null) {
            throw new ResourceNotFoundException("Student with ID '" + studentId + "' was not found.");
        }
        return student;
    }

    /**
     * Searches students by ID, Name, or Department (case-insensitive substring match).
     */
    public List<Student> searchStudents(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllStudents();
        }
        String lowerQuery = query.trim().toLowerCase();
        List<Student> results = new ArrayList<>();
        for (Student student : students) {
            if (student.getStudentId().toLowerCase().contains(lowerQuery) ||
                student.getName().toLowerCase().contains(lowerQuery) ||
                student.getDepartment().toLowerCase().contains(lowerQuery)) {
                results.add(student);
            }
        }
        return results;
    }

    /**
     * Returns an unmodifiable view of all students.
     */
    public List<Student> getAllStudents() {
        return Collections.unmodifiableList(new ArrayList<>(students));
    }

    /**
     * Returns total number of students.
     */
    public int getStudentCount() {
        return students.size();
    }

    /**
     * Validates required student fields and basic formatting.
     */
    private void validateStudentData(Student student) throws ValidationException {
        if (student.getStudentId() == null || student.getStudentId().trim().isEmpty()) {
            throw new ValidationException("Student ID is required.");
        }
        if (student.getStudentId().contains(",") || student.getStudentId().contains(" ")) {
            throw new ValidationException("Student ID cannot contain spaces or commas.");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new ValidationException("Student name is required.");
        }
        if (student.getEmail() == null || !student.getEmail().contains("@") || !student.getEmail().contains(".")) {
            throw new ValidationException("A valid email address containing '@' and '.' is required.");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new ValidationException("Department is required.");
        }
        if (student.getSemester() < 1 || student.getSemester() > 12) {
            throw new ValidationException("Semester must be between 1 and 12.");
        }
        if (student.getPhone() == null || !student.getPhone().trim().matches("^[0-9+\\-\\s()]{7,15}$")) {
            throw new ValidationException("Phone number must contain between 7 and 15 digits/symbols (e.g. 9876543210).");
        }
    }
}
