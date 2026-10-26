package com.college.sms.storage;

import com.college.sms.exception.SMSException;
import com.college.sms.model.Course;
import com.college.sms.model.Registration;
import com.college.sms.model.Student;
import com.college.sms.service.CourseService;
import com.college.sms.service.RegistrationService;
import com.college.sms.service.StudentService;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent storage of Student, Course, and Registration records
 * using CSV text files.
 * Demonstrates Java I/O using BufferedReader, BufferedWriter, FileReader,
 * FileWriter, and try-with-resources.
 */
public class FileStorage {
    private final File dataDir;
    private final File studentsFile;
    private final File coursesFile;
    private final File registrationsFile;

    public FileStorage(String directoryPath) {
        this.dataDir = new File(directoryPath);
        this.studentsFile = new File(dataDir, "students.csv");
        this.coursesFile = new File(dataDir, "courses.csv");
        this.registrationsFile = new File(dataDir, "registrations.csv");
    }

    /**
     * Ensures the data folder exists.
     */
    public void initStorage() {
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Created data storage directory: " + dataDir.getAbsolutePath());
            }
        }
    }

    /**
     * Checks if storage files already exist.
     */
    public boolean hasExistingData() {
        return studentsFile.exists() && coursesFile.exists();
    }

    // =========================================================================
    // STUDENT FILE HANDLING
    // =========================================================================

    /**
     * Saves student records to students.csv using BufferedWriter and FileWriter.
     * Throws SMSException if disk write fails so UI is notified.
     */
    public void saveStudents(List<Student> students) throws SMSException {
        initStorage();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(studentsFile))) {
            // Write CSV Header
            writer.write("studentId,name,email,department,semester,phone");
            writer.newLine();

            for (Student s : students) {
                String line = String.format("%s,%s,%s,%s,%d,%s",
                        escapeCsv(s.getStudentId()),
                        escapeCsv(s.getName()),
                        escapeCsv(s.getEmail()),
                        escapeCsv(s.getDepartment()),
                        s.getSemester(),
                        escapeCsv(s.getPhone()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new SMSException("File write error for " + studentsFile.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads student records from students.csv using BufferedReader and FileReader.
     */
    public List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();
        if (!studentsFile.exists()) {
            return students;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(studentsFile))) {
            String line = reader.readLine(); // Read header line
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 6) {
                    System.err.println("Warning: Malformed line " + lineNum + " in " + studentsFile.getName() + " (skipped): " + line);
                    continue;
                }

                try {
                    String id = tokens.get(0);
                    String name = tokens.get(1);
                    String email = tokens.get(2);
                    String dept = tokens.get(3);
                    int sem = Integer.parseInt(tokens.get(4));
                    String phone = tokens.get(5);

                    students.add(new Student(id, name, email, dept, sem, phone));
                } catch (IllegalArgumentException ex) {
                    System.err.println("Warning: Invalid data at line " + lineNum + " in " + studentsFile.getName() + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + studentsFile.getName() + ": " + e.getMessage());
        }

        return students;
    }

    // =========================================================================
    // COURSE FILE HANDLING
    // =========================================================================

    /**
     * Saves course records to courses.csv using BufferedWriter and FileWriter.
     * Throws SMSException if disk write fails so UI is notified.
     */
    public void saveCourses(List<Course> courses) throws SMSException {
        initStorage();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(coursesFile))) {
            // Write CSV Header
            writer.write("courseCode,courseTitle,credits,instructor,maxCapacity");
            writer.newLine();

            for (Course c : courses) {
                String line = String.format("%s,%s,%d,%s,%d",
                        escapeCsv(c.getCourseCode()),
                        escapeCsv(c.getCourseTitle()),
                        c.getCredits(),
                        escapeCsv(c.getInstructor()),
                        c.getMaxCapacity());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new SMSException("File write error for " + coursesFile.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads course records from courses.csv using BufferedReader and FileReader.
     */
    public List<Course> loadCourses() {
        List<Course> courses = new ArrayList<>();
        if (!coursesFile.exists()) {
            return courses;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(coursesFile))) {
            String line = reader.readLine(); // Read header line
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 5) {
                    System.err.println("Warning: Malformed line " + lineNum + " in " + coursesFile.getName() + " (skipped): " + line);
                    continue;
                }

                try {
                    String code = tokens.get(0);
                    String title = tokens.get(1);
                    int credits = Integer.parseInt(tokens.get(2));
                    String instructor = tokens.get(3);
                    int capacity = Integer.parseInt(tokens.get(4));

                    courses.add(new Course(code, title, credits, instructor, capacity));
                } catch (IllegalArgumentException ex) {
                    System.err.println("Warning: Invalid data at line " + lineNum + " in " + coursesFile.getName() + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + coursesFile.getName() + ": " + e.getMessage());
        }

        return courses;
    }

    // =========================================================================
    // REGISTRATION FILE HANDLING
    // =========================================================================

    /**
     * Saves registration records to registrations.csv using BufferedWriter and FileWriter.
     * Throws SMSException if disk write fails so UI is notified.
     */
    public void saveRegistrations(List<Registration> registrations) throws SMSException {
        initStorage();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(registrationsFile))) {
            // Write CSV Header
            writer.write("registrationId,studentId,courseCode,registrationDate,status");
            writer.newLine();

            for (Registration r : registrations) {
                String line = String.format("%s,%s,%s,%s,%s",
                        escapeCsv(r.getRegistrationId()),
                        escapeCsv(r.getStudentId()),
                        escapeCsv(r.getCourseCode()),
                        escapeCsv(r.getRegistrationDate()),
                        escapeCsv(r.getStatus()));
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new SMSException("File write error for " + registrationsFile.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Loads registration records from registrations.csv and re-associates them
     * with existing Student and Course instances.
     */
    public List<Registration> loadRegistrations(StudentService studentService, CourseService courseService) {
        List<Registration> registrations = new ArrayList<>();
        if (!registrationsFile.exists()) {
            return registrations;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(registrationsFile))) {
            String line = reader.readLine(); // Header
            int lineNum = 1;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.trim().isEmpty()) continue;

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 5) {
                    System.err.println("Warning: Malformed line " + lineNum + " in " + registrationsFile.getName() + " (skipped): " + line);
                    continue;
                }

                try {
                    String regId = tokens.get(0);
                    String studentId = tokens.get(1);
                    String courseCode = tokens.get(2);
                    String date = tokens.get(3);
                    String status = tokens.get(4);

                    Student student = studentService.findStudentById(studentId);
                    Course course = courseService.findCourseByCode(courseCode);

                    if (student != null && course != null) {
                        Registration reg = new Registration(regId, student, course, date, status);
                        registrations.add(reg);
                    } else {
                        System.err.println("Warning: Skipping orphaned registration " + regId +
                                " (Student: " + studentId + ", Course: " + courseCode + ")");
                    }
                } catch (IllegalArgumentException ex) {
                    System.err.println("Warning: Invalid registration at line " + lineNum + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + registrationsFile.getName() + ": " + e.getMessage());
        }

        return registrations;
    }

    // =========================================================================
    // CSV HELPER METHODS (Escaping & Safe Parsing)
    // =========================================================================

    /**
     * Safely escapes fields containing commas, double quotes, or newlines.
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Parses a CSV line honoring double-quoted tokens that contain embedded commas.
     */
    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Check for escaped quote ("")
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }
}
