package com.college.sms.gui;

import com.college.sms.exception.SMSException;
import com.college.sms.model.Course;
import com.college.sms.service.CourseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Course Management GUI Panel.
 * Handles adding, updating, deleting, searching, and viewing academic courses
 * with real-time seat availability calculation.
 */
public class CoursePanel extends JPanel implements RefreshablePanel {
    private final CourseService courseService;
    private final MainFrame mainFrame;

    // Form inputs
    private JTextField txtCode;
    private JTextField txtTitle;
    private JSpinner spnCredits;
    private JTextField txtInstructor;
    private JSpinner spnCapacity;

    // Search input
    private JTextField txtSearch;

    // Table
    private DefaultTableModel tableModel;
    private JTable courseTable;

    public CoursePanel(CourseService courseService, MainFrame mainFrame) {
        this.courseService = courseService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- Top Search Bar ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(UITheme.createCardBorder("Search Course Catalog"));

        JLabel lblSearch = new JLabel("Filter by Code, Title or Instructor:");
        lblSearch.setFont(UITheme.FONT_SUBHEADER);
        txtSearch = new JTextField(20);
        UITheme.styleTextField(txtSearch);

        JButton btnSearch = UITheme.createButton("Search", UITheme.PRIMARY);
        btnSearch.addActionListener(e -> performSearch());

        JButton btnReset = UITheme.createButton("Reset", UITheme.TEXT_MUTED);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            refreshData();
        });

        txtSearch.addActionListener(e -> performSearch());

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);
        add(searchPanel, BorderLayout.NORTH);

        // --- Center: Form (West) and Table (Center) ---
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        // Left Form Panel
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(UITheme.createCardBorder("Course Information Form"));
        formCard.setPreferredSize(new Dimension(340, 0));

        JPanel fieldsGrid = new JPanel(new GridLayout(5, 2, 8, 14));
        fieldsGrid.setOpaque(false);
        fieldsGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtCode = new JTextField();
        UITheme.styleTextField(txtCode);

        txtTitle = new JTextField();
        UITheme.styleTextField(txtTitle);

        spnCredits = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        spnCredits.setFont(UITheme.FONT_REGULAR);

        txtInstructor = new JTextField();
        UITheme.styleTextField(txtInstructor);

        spnCapacity = new JSpinner(new SpinnerNumberModel(40, 1, 500, 1));
        spnCapacity.setFont(UITheme.FONT_REGULAR);

        fieldsGrid.add(new JLabel("Course Code: *"));
        fieldsGrid.add(txtCode);
        fieldsGrid.add(new JLabel("Course Title: *"));
        fieldsGrid.add(txtTitle);
        fieldsGrid.add(new JLabel("Credits (1-10): *"));
        fieldsGrid.add(spnCredits);
        fieldsGrid.add(new JLabel("Instructor: *"));
        fieldsGrid.add(txtInstructor);
        fieldsGrid.add(new JLabel("Max Capacity: *"));
        fieldsGrid.add(spnCapacity);

        formCard.add(fieldsGrid, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnAdd = UITheme.createButton("Add Course", UITheme.SUCCESS);
        btnAdd.addActionListener(e -> handleAddCourse());

        JButton btnUpdate = UITheme.createButton("Update", UITheme.PRIMARY);
        btnUpdate.addActionListener(e -> handleUpdateCourse());

        JButton btnDelete = UITheme.createButton("Delete", UITheme.DANGER);
        btnDelete.addActionListener(e -> handleDeleteCourse());

        JButton btnClear = UITheme.createButton("Clear Form", UITheme.TEXT_MUTED);
        btnClear.addActionListener(e -> clearForm());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        formCard.add(buttonsPanel, BorderLayout.SOUTH);
        centerContainer.add(formCard, BorderLayout.WEST);

        // Right Table Panel
        JPanel tableCard = new JPanel(new BorderLayout(5, 5));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(UITheme.createCardBorder("Course Catalog & Seat Availability"));

        String[] columns = {"Code", "Course Title", "Credits", "Instructor", "Capacity", "Available Seats"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        UITheme.styleTable(courseTable);

        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerContainer.add(tableCard, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void populateFormFromSelection() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtCode.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtCode.setEditable(false); // Do not alter Course Code on update
            txtTitle.setText(tableModel.getValueAt(selectedRow, 1).toString());

            try {
                int credits = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString());
                spnCredits.setValue(credits);
            } catch (NumberFormatException e) {
                spnCredits.setValue(3);
            }

            txtInstructor.setText(tableModel.getValueAt(selectedRow, 3).toString());

            try {
                int capacity = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
                spnCapacity.setValue(capacity);
            } catch (NumberFormatException e) {
                spnCapacity.setValue(40);
            }
        }
    }

    private void handleAddCourse() {
        try {
            validateInputs();
            Course course = new Course(
                    txtCode.getText().trim(),
                    txtTitle.getText().trim(),
                    (Integer) spnCredits.getValue(),
                    txtInstructor.getText().trim(),
                    (Integer) spnCapacity.getValue()
            );

            courseService.addCourse(course);
            JOptionPane.showMessageDialog(this,
                    "Course '" + course.getCourseTitle() + "' (" + course.getCourseCode() + ") added successfully!",
                    "Course Added", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            mainFrame.refreshAllPanels();
        } catch (SMSException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation / Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateCourse() {
        try {
            validateInputs();
            Course course = new Course(
                    txtCode.getText().trim(),
                    txtTitle.getText().trim(),
                    (Integer) spnCredits.getValue(),
                    txtInstructor.getText().trim(),
                    (Integer) spnCapacity.getValue()
            );

            courseService.updateCourse(course);
            JOptionPane.showMessageDialog(this,
                    "Course '" + course.getCourseCode() + "' updated successfully!",
                    "Course Updated", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            mainFrame.refreshAllPanels();
        } catch (SMSException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteCourse() {
        String courseCode = txtCode.getText().trim();
        if (courseCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a course from the table or enter a Course Code to delete.",
                    "No Course Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course '" + courseCode + "'?\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                courseService.deleteCourse(courseCode);
                JOptionPane.showMessageDialog(this, "Course '" + courseCode + "' was deleted successfully.",
                        "Course Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                mainFrame.refreshAllPanels();
            } catch (SMSException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Course", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        List<Course> results = courseService.searchCourses(query);
        populateTable(results);
    }

    private void clearForm() {
        txtCode.setText("");
        txtCode.setEditable(true);
        txtTitle.setText("");
        spnCredits.setValue(3);
        txtInstructor.setText("");
        spnCapacity.setValue(40);
        courseTable.clearSelection();
    }

    private void validateInputs() throws SMSException {
        if (txtCode.getText().trim().isEmpty()) {
            throw new SMSException("Course code cannot be empty.");
        }
        if (txtTitle.getText().trim().isEmpty()) {
            throw new SMSException("Course title cannot be empty.");
        }
        if (txtInstructor.getText().trim().isEmpty()) {
            throw new SMSException("Instructor name cannot be empty.");
        }
    }

    private void populateTable(List<Course> courseList) {
        tableModel.setRowCount(0);
        for (Course c : courseList) {
            int availableSeats = 0;
            try {
                availableSeats = courseService.getAvailableSeats(c.getCourseCode());
            } catch (SMSException e) {
                availableSeats = 0;
            }

            tableModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseTitle(),
                    c.getCredits(),
                    c.getInstructor(),
                    c.getMaxCapacity(),
                    availableSeats
            });
        }
    }

    @Override
    public void refreshData() {
        populateTable(courseService.getAllCourses());
    }
}
