package com.college.sms.gui;

import com.college.sms.exception.SMSException;
import com.college.sms.model.Course;
import com.college.sms.model.Registration;
import com.college.sms.model.Student;
import com.college.sms.service.CourseService;
import com.college.sms.service.RegistrationService;
import com.college.sms.service.StudentService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Course Registration GUI Panel.
 * Uses JComboBox for student and course selection, displays real-time previews
 * of seat availability, and enrolls students into courses.
 */
public class RegistrationPanel extends JPanel implements RefreshablePanel {
    private final StudentService studentService;
    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final MainFrame mainFrame;

    // Selection Dropdowns
    private JComboBox<StudentComboItem> cmbStudents;
    private JComboBox<CourseComboItem> cmbCourses;
    private JTextField txtDate;

    // Live Detail Preview Labels
    private JLabel lblStudentDetail;
    private JLabel lblCourseDetail;
    private JLabel lblSeatsAvailable;

    public RegistrationPanel(StudentService studentService, CourseService courseService,
                             RegistrationService registrationService, MainFrame mainFrame) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 30, 20, 30));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Course Enrollment & Registration");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Select an enrolled student and an available course to complete registration.");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center: Registration Form and Live Previews ---
        JPanel mainContent = new JPanel(new GridBagLayout());
        mainContent.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 1. Selection Card
        JPanel formCard = new JPanel(new GridLayout(3, 2, 12, 16));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(UITheme.createCardBorder("Enrollment Selection"));

        cmbStudents = new JComboBox<>();
        cmbStudents.setFont(UITheme.FONT_REGULAR);
        cmbStudents.setBackground(Color.WHITE);
        cmbStudents.addActionListener(e -> updateStudentPreview());

        cmbCourses = new JComboBox<>();
        cmbCourses.setFont(UITheme.FONT_REGULAR);
        cmbCourses.setBackground(Color.WHITE);
        cmbCourses.addActionListener(e -> updateCoursePreview());

        txtDate = new JTextField(LocalDate.now().toString());
        UITheme.styleTextField(txtDate);

        formCard.add(new JLabel("Select Student: *"));
        formCard.add(cmbStudents);
        formCard.add(new JLabel("Select Course: *"));
        formCard.add(cmbCourses);
        formCard.add(new JLabel("Registration Date (YYYY-MM-DD): *"));
        formCard.add(txtDate);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mainContent.add(formCard, gbc);

        // 2. Preview Cards (Student & Course)
        JPanel previewContainer = new JPanel(new GridLayout(1, 2, 15, 0));
        previewContainer.setOpaque(false);

        // Student Preview Box
        JPanel studentPreviewCard = new JPanel(new BorderLayout(5, 5));
        studentPreviewCard.setBackground(Color.WHITE);
        studentPreviewCard.setBorder(UITheme.createCardBorder("Selected Student Details"));
        lblStudentDetail = new JLabel("<html><i>No student selected.</i></html>");
        lblStudentDetail.setFont(UITheme.FONT_REGULAR);
        lblStudentDetail.setBorder(new EmptyBorder(10, 10, 10, 10));
        studentPreviewCard.add(lblStudentDetail, BorderLayout.CENTER);

        // Course Preview Box
        JPanel coursePreviewCard = new JPanel(new BorderLayout(5, 5));
        coursePreviewCard.setBackground(Color.WHITE);
        coursePreviewCard.setBorder(UITheme.createCardBorder("Selected Course Details"));

        lblCourseDetail = new JLabel("<html><i>No course selected.</i></html>");
        lblCourseDetail.setFont(UITheme.FONT_REGULAR);
        lblCourseDetail.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblSeatsAvailable = new JLabel("Available Seats: -", SwingConstants.RIGHT);
        lblSeatsAvailable.setFont(UITheme.FONT_BOLD);
        lblSeatsAvailable.setForeground(UITheme.SUCCESS);
        lblSeatsAvailable.setBorder(new EmptyBorder(5, 10, 10, 10));

        coursePreviewCard.add(lblCourseDetail, BorderLayout.CENTER);
        coursePreviewCard.add(lblSeatsAvailable, BorderLayout.SOUTH);

        previewContainer.add(studentPreviewCard);
        previewContainer.add(coursePreviewCard);

        gbc.gridy = 1;
        mainContent.add(previewContainer, gbc);

        // 3. Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        actionPanel.setOpaque(false);

        JButton btnRegister = UITheme.createButton("Confirm & Register Course", UITheme.SUCCESS);
        btnRegister.setFont(UITheme.FONT_HEADER);
        btnRegister.setPreferredSize(new Dimension(260, 42));
        btnRegister.addActionListener(e -> handleRegister());

        JButton btnClear = UITheme.createButton("Clear Selection", UITheme.TEXT_MUTED);
        btnClear.setPreferredSize(new Dimension(160, 42));
        btnClear.addActionListener(e -> {
            if (cmbStudents.getItemCount() > 0) cmbStudents.setSelectedIndex(0);
            if (cmbCourses.getItemCount() > 0) cmbCourses.setSelectedIndex(0);
            txtDate.setText(LocalDate.now().toString());
        });

        actionPanel.add(btnRegister);
        actionPanel.add(btnClear);

        gbc.gridy = 2;
        mainContent.add(actionPanel, gbc);

        add(mainContent, BorderLayout.CENTER);
    }

    private void updateStudentPreview() {
        StudentComboItem item = (StudentComboItem) cmbStudents.getSelectedItem();
        if (item != null && item.student != null) {
            Student s = item.student;
            lblStudentDetail.setText("<html>" +
                    "<b>ID:</b> " + s.getStudentId() + "<br>" +
                    "<b>Name:</b> " + s.getName() + "<br>" +
                    "<b>Department:</b> " + s.getDepartment() + " (Sem " + s.getSemester() + ")<br>" +
                    "<b>Email:</b> " + s.getEmail() + "<br>" +
                    "<b>Phone:</b> " + s.getPhone() +
                    "</html>");
        } else {
            lblStudentDetail.setText("<html><i>No student selected.</i></html>");
        }
    }

    private void updateCoursePreview() {
        CourseComboItem item = (CourseComboItem) cmbCourses.getSelectedItem();
        if (item != null && item.course != null) {
            Course c = item.course;
            int availableSeats = 0;
            try {
                availableSeats = courseService.getAvailableSeats(c.getCourseCode());
            } catch (SMSException e) {
                availableSeats = 0;
            }

            lblCourseDetail.setText("<html>" +
                    "<b>Code:</b> " + c.getCourseCode() + "<br>" +
                    "<b>Title:</b> " + c.getCourseTitle() + "<br>" +
                    "<b>Credits:</b> " + c.getCredits() + " credits<br>" +
                    "<b>Instructor:</b> " + c.getInstructor() + "<br>" +
                    "<b>Max Capacity:</b> " + c.getMaxCapacity() + " seats" +
                    "</html>");

            if (availableSeats > 0) {
                lblSeatsAvailable.setText("Available Seats: " + availableSeats + " / " + c.getMaxCapacity());
                lblSeatsAvailable.setForeground(UITheme.SUCCESS);
            } else {
                lblSeatsAvailable.setText("COURSE FULL (0 / " + c.getMaxCapacity() + " seats)");
                lblSeatsAvailable.setForeground(UITheme.DANGER);
            }
        } else {
            lblCourseDetail.setText("<html><i>No course selected.</i></html>");
            lblSeatsAvailable.setText("Available Seats: -");
        }
    }

    private void handleRegister() {
        StudentComboItem sItem = (StudentComboItem) cmbStudents.getSelectedItem();
        CourseComboItem cItem = (CourseComboItem) cmbCourses.getSelectedItem();
        String dateStr = txtDate.getText().trim();

        if (sItem == null || sItem.student == null) {
            JOptionPane.showMessageDialog(this, "Please select a student to register.",
                    "Missing Student", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cItem == null || cItem.course == null) {
            JOptionPane.showMessageDialog(this, "Please select a course to register for.",
                    "Missing Course", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide a registration date.",
                    "Missing Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Registration reg = registrationService.registerStudent(
                    sItem.student.getStudentId(),
                    cItem.course.getCourseCode(),
                    dateStr
            );

            JOptionPane.showMessageDialog(this,
                    "Registration Successful!\n\n" +
                            "Registration ID: " + reg.getRegistrationId() + "\n" +
                            "Student: " + reg.getStudent().getName() + " (" + reg.getStudentId() + ")\n" +
                            "Course: " + reg.getCourse().getCourseTitle() + " (" + reg.getCourseCode() + ")\n" +
                            "Date: " + reg.getRegistrationDate(),
                    "Enrollment Completed", JOptionPane.INFORMATION_MESSAGE);

            mainFrame.refreshAllPanels();
        } catch (SMSException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshData() {
        // Refresh Students Combo Box
        StudentComboItem selectedStudent = (StudentComboItem) cmbStudents.getSelectedItem();
        cmbStudents.removeAllItems();
        List<Student> students = studentService.getAllStudents();
        for (Student s : students) {
            cmbStudents.addItem(new StudentComboItem(s));
        }
        if (selectedStudent != null) {
            for (int i = 0; i < cmbStudents.getItemCount(); i++) {
                if (cmbStudents.getItemAt(i).student.getStudentId().equals(selectedStudent.student.getStudentId())) {
                    cmbStudents.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Refresh Courses Combo Box
        CourseComboItem selectedCourse = (CourseComboItem) cmbCourses.getSelectedItem();
        cmbCourses.removeAllItems();
        List<Course> courses = courseService.getAllCourses();
        for (Course c : courses) {
            cmbCourses.addItem(new CourseComboItem(c, courseService));
        }
        if (selectedCourse != null) {
            for (int i = 0; i < cmbCourses.getItemCount(); i++) {
                if (cmbCourses.getItemAt(i).course.getCourseCode().equals(selectedCourse.course.getCourseCode())) {
                    cmbCourses.setSelectedIndex(i);
                    break;
                }
            }
        }

        updateStudentPreview();
        updateCoursePreview();
    }

    // Helper item wrapper for Student dropdown
    private static class StudentComboItem {
        final Student student;

        StudentComboItem(Student student) {
            this.student = student;
        }

        @Override
        public String toString() {
            if (student == null) return "Select a student...";
            return "[" + student.getStudentId() + "] " + student.getName() + " (" + student.getDepartment() + ")";
        }
    }

    // Helper item wrapper for Course dropdown
    private static class CourseComboItem {
        final Course course;
        final CourseService service;

        CourseComboItem(Course course, CourseService service) {
            this.course = course;
            this.service = service;
        }

        @Override
        public String toString() {
            if (course == null) return "Select a course...";
            int seats = 0;
            try {
                seats = service.getAvailableSeats(course.getCourseCode());
            } catch (SMSException e) {
                seats = 0;
            }
            return "[" + course.getCourseCode() + "] " + course.getCourseTitle() + " (" + seats + " seats left)";
        }
    }
}
