package com.college.sms.model;

import java.util.Objects;

/**
 * Represents a Student in the Course Registration System.
 * Demonstrates OOP Encapsulation with private attributes, 
 * constructors, and controlled access through getters and setters.
 */
public class Student {
    private String studentId;
    private String name;
    private String email;
    private String department;
    private int semester;
    private String phone;

    // Default constructor
    public Student() {
    }

    // Parameterized constructor
    public Student(String studentId, String name, String email, String department, int semester, String phone) {
        setStudentId(studentId);
        setName(name);
        setEmail(email);
        setDepartment(department);
        setSemester(semester);
        setPhone(phone);
    }

    // --- Getters and Setters (Encapsulation) ---

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty.");
        }
        this.studentId = studentId.trim().toUpperCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty.");
        }
        this.name = name.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        this.email = email.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be empty.");
        }
        this.department = department.trim();
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        if (semester < 1 || semester > 12) {
            throw new IllegalArgumentException("Semester must be between 1 and 12.");
        }
        this.semester = semester;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        this.phone = phone.trim();
    }

    // Display summary method
    public String getDisplayDetails() {
        return String.format("[%s] %s | Dept: %s | Sem: %d | Email: %s | Phone: %s",
                studentId, name, department, semester, email, phone);
    }

    @Override
    public String toString() {
        return studentId + " - " + name + " (" + department + ")";
    }

    // Uniqueness of Student based on studentId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
}
