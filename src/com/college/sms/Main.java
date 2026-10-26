package com.college.sms;

import com.college.sms.gui.LoginFrame;
import com.college.sms.model.Course;
import com.college.sms.model.Registration;
import com.college.sms.model.Student;
import com.college.sms.service.CourseService;
import com.college.sms.service.RegistrationService;
import com.college.sms.service.StudentService;
import com.college.sms.storage.FileStorage;

import javax.swing.*;
import java.util.List;

/**
 * Main application launcher for Student Course Registration and Management System.
 * Coordinates FileStorage persistence, loads records from CSV files,
 * and launches the GUI on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        // Set modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Default fallback
            }
        }

        // Initialize Services
        StudentService studentService = new StudentService();
        CourseService courseService = new CourseService();
        RegistrationService registrationService = new RegistrationService(studentService, courseService);

        // Initialize File Storage Layer ("data" directory)
        FileStorage fileStorage = new FileStorage("data");
        fileStorage.initStorage();

        // Connect FileStorage to services for automatic persistence on data changes
        studentService.setFileStorage(fileStorage);
        courseService.setFileStorage(fileStorage);
        registrationService.setFileStorage(fileStorage);

        // Restore persistent records from CSV files, or seed starter data if first run
        if (fileStorage.hasExistingData()) {
            System.out.println("Existing data found. Loading records from CSV storage files...");
            List<Student> students = fileStorage.loadStudents();
            studentService.loadInitialData(students);

            List<Course> courses = fileStorage.loadCourses();
            courseService.loadInitialData(courses);

            List<Registration> registrations = fileStorage.loadRegistrations(studentService, courseService);
            registrationService.loadInitialData(registrations);

            System.out.println("Loaded " + students.size() + " students, " +
                    courses.size() + " courses, and " +
                    registrations.size() + " registrations from disk.");
        } else {
            System.out.println("First run detected. Seeding initial records and creating CSV files...");
            seedInitialData(studentService, courseService, registrationService);
        }

        // Launch Login GUI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(studentService, courseService, registrationService);
            loginFrame.setVisible(true);
        });
    }

    /**
     * Seeds initial academic records on first launch and saves them to CSV files.
     */
    private static void seedInitialData(StudentService studentService,
                                        CourseService courseService,
                                        RegistrationService registrationService) {
        try {
            // Seed Students
            studentService.addStudent(new Student("S101", "Aarav Sharma", "aarav.sharma@college.edu", "Computer Science", 4, "9876543210"));
            studentService.addStudent(new Student("S102", "Diya Patel", "diya.patel@college.edu", "Information Technology", 3, "9876543211"));
            studentService.addStudent(new Student("S103", "Rohan Verma", "rohan.verma@college.edu", "Electronics & Comm", 4, "9876543212"));
            studentService.addStudent(new Student("S104", "Ananya Reddy", "ananya.reddy@college.edu", "Computer Science", 2, "9876543213"));

            // Seed Courses
            courseService.addCourse(new Course("CS201", "Data Structures & Algorithms", 4, "Dr. K. Rao", 3));
            courseService.addCourse(new Course("CS202", "Database Management Systems", 3, "Prof. S. Gupta", 45));
            courseService.addCourse(new Course("IT301", "Web Development & Technologies", 3, "Dr. N. Iyer", 40));
            courseService.addCourse(new Course("EC105", "Digital Logic & Microprocessors", 4, "Prof. A. Mehta", 35));

            // Seed Initial Registrations
            registrationService.registerStudent("REG-1001", "S101", "CS201", "2026-10-20");
            registrationService.registerStudent("REG-1002", "S102", "CS201", "2026-10-21");
            registrationService.registerStudent("REG-1003", "S103", "CS202", "2026-10-22");

            System.out.println("Initial sample data seeded and saved to CSV successfully.");
        } catch (Exception e) {
            System.err.println("Notice: Could not load some initial sample data: " + e.getMessage());
        }
    }
}
