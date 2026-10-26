package com.college.sms.gui;

import com.college.sms.exception.SMSException;
import com.college.sms.model.Student;
import com.college.sms.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Student Management GUI Panel.
 * Handles adding, updating, deleting, searching, and viewing students in a styled JTable.
 */
public class StudentPanel extends JPanel implements RefreshablePanel {
    private final StudentService studentService;
    private final MainFrame mainFrame;

    // Form inputs
    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtEmail;
    private JComboBox<String> cmbDept;
    private JSpinner spnSemester;
    private JTextField txtPhone;

    // Search input
    private JTextField txtSearch;

    // Table
    private DefaultTableModel tableModel;
    private JTable studentTable;

    public StudentPanel(StudentService studentService, MainFrame mainFrame) {
        this.studentService = studentService;
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
        searchPanel.setBorder(UITheme.createCardBorder("Search Students"));

        JLabel lblSearch = new JLabel("Filter by ID, Name or Department:");
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

        // Trigger search on Enter key
        txtSearch.addActionListener(e -> performSearch());

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnReset);
        add(searchPanel, BorderLayout.NORTH);

        // --- Center: Split between Form (West) and Table (Center) ---
        JPanel centerContainer = new JPanel(new BorderLayout(15, 0));
        centerContainer.setOpaque(false);

        // Left Form Panel
        JPanel formCard = new JPanel(new BorderLayout(10, 10));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(UITheme.createCardBorder("Student Information Form"));
        formCard.setPreferredSize(new Dimension(340, 0));

        JPanel fieldsGrid = new JPanel(new GridLayout(6, 2, 8, 12));
        fieldsGrid.setOpaque(false);
        fieldsGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtId = new JTextField();
        UITheme.styleTextField(txtId);

        txtName = new JTextField();
        UITheme.styleTextField(txtName);

        txtEmail = new JTextField();
        UITheme.styleTextField(txtEmail);

        String[] departments = {"Computer Science", "Information Technology", "Electronics & Comm", "Mechanical", "Civil", "Electrical"};
        cmbDept = new JComboBox<>(departments);
        cmbDept.setFont(UITheme.FONT_REGULAR);
        cmbDept.setBackground(Color.WHITE);

        spnSemester = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        spnSemester.setFont(UITheme.FONT_REGULAR);

        txtPhone = new JTextField();
        UITheme.styleTextField(txtPhone);

        fieldsGrid.add(new JLabel("Student ID: *"));
        fieldsGrid.add(txtId);
        fieldsGrid.add(new JLabel("Full Name: *"));
        fieldsGrid.add(txtName);
        fieldsGrid.add(new JLabel("Email Address: *"));
        fieldsGrid.add(txtEmail);
        fieldsGrid.add(new JLabel("Department: *"));
        fieldsGrid.add(cmbDept);
        fieldsGrid.add(new JLabel("Semester (1-12): *"));
        fieldsGrid.add(spnSemester);
        fieldsGrid.add(new JLabel("Phone Number: *"));
        fieldsGrid.add(txtPhone);

        formCard.add(fieldsGrid, BorderLayout.CENTER);

        // Action Buttons at bottom of form
        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton btnAdd = UITheme.createButton("Add Student", UITheme.SUCCESS);
        btnAdd.addActionListener(e -> handleAddStudent());

        JButton btnUpdate = UITheme.createButton("Update", UITheme.PRIMARY);
        btnUpdate.addActionListener(e -> handleUpdateStudent());

        JButton btnDelete = UITheme.createButton("Delete", UITheme.DANGER);
        btnDelete.addActionListener(e -> handleDeleteStudent());

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
        tableCard.setBorder(UITheme.createCardBorder("Enrolled Students Directory"));

        String[] columns = {"Student ID", "Full Name", "Email", "Department", "Semester", "Phone"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        UITheme.styleTable(studentTable);

        // Row selection listener to populate form fields
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerContainer.add(tableCard, BorderLayout.CENTER);

        add(centerContainer, BorderLayout.CENTER);
    }

    private void populateFormFromSelection() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow >= 0) {
            txtId.setText(tableModel.getValueAt(selectedRow, 0).toString());
            txtId.setEditable(false); // ID should not be changed during update
            txtName.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtEmail.setText(tableModel.getValueAt(selectedRow, 2).toString());

            String dept = tableModel.getValueAt(selectedRow, 3).toString();
            cmbDept.setSelectedItem(dept);

            try {
                int sem = Integer.parseInt(tableModel.getValueAt(selectedRow, 4).toString());
                spnSemester.setValue(sem);
            } catch (NumberFormatException e) {
                spnSemester.setValue(1);
            }

            txtPhone.setText(tableModel.getValueAt(selectedRow, 5).toString());
        }
    }

    private void handleAddStudent() {
        try {
            validateInputs();
            Student student = new Student(
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    cmbDept.getSelectedItem().toString(),
                    (Integer) spnSemester.getValue(),
                    txtPhone.getText().trim()
            );

            studentService.addStudent(student);
            JOptionPane.showMessageDialog(this,
                    "Student '" + student.getName() + "' (" + student.getStudentId() + ") added successfully!",
                    "Student Added", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            mainFrame.refreshAllPanels();
        } catch (SMSException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation / Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateStudent() {
        try {
            validateInputs();
            Student student = new Student(
                    txtId.getText().trim(),
                    txtName.getText().trim(),
                    txtEmail.getText().trim(),
                    cmbDept.getSelectedItem().toString(),
                    (Integer) spnSemester.getValue(),
                    txtPhone.getText().trim()
            );

            studentService.updateStudent(student);
            JOptionPane.showMessageDialog(this,
                    "Student '" + student.getStudentId() + "' updated successfully!",
                    "Student Updated", JOptionPane.INFORMATION_MESSAGE);

            clearForm();
            mainFrame.refreshAllPanels();
        } catch (SMSException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteStudent() {
        String studentId = txtId.getText().trim();
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table or enter a Student ID to delete.",
                    "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student '" + studentId + "'?\nThis action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                studentService.deleteStudent(studentId);
                JOptionPane.showMessageDialog(this, "Student '" + studentId + "' was deleted successfully.",
                        "Student Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                mainFrame.refreshAllPanels();
            } catch (SMSException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete Student", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performSearch() {
        String query = txtSearch.getText().trim();
        List<Student> results = studentService.searchStudents(query);
        populateTable(results);
    }

    private void clearForm() {
        txtId.setText("");
        txtId.setEditable(true);
        txtName.setText("");
        txtEmail.setText("");
        cmbDept.setSelectedIndex(0);
        spnSemester.setValue(1);
        txtPhone.setText("");
        studentTable.clearSelection();
    }

    private void validateInputs() throws SMSException {
        if (txtId.getText().trim().isEmpty()) {
            throw new SMSException("Student ID cannot be empty.");
        }
        if (txtName.getText().trim().isEmpty()) {
            throw new SMSException("Student name cannot be empty.");
        }
        String email = txtEmail.getText().trim();
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            throw new SMSException("Please enter a valid email address (e.g. student@college.edu).");
        }
        if (txtPhone.getText().trim().isEmpty()) {
            throw new SMSException("Phone number cannot be empty.");
        }
    }

    private void populateTable(List<Student> studentList) {
        tableModel.setRowCount(0);
        for (Student s : studentList) {
            tableModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getName(),
                    s.getEmail(),
                    s.getDepartment(),
                    s.getSemester(),
                    s.getPhone()
            });
        }
    }

    @Override
    public void refreshData() {
        populateTable(studentService.getAllStudents());
    }
}
