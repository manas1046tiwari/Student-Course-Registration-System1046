package com.college.sms.gui;

import com.college.sms.exception.SMSException;
import com.college.sms.model.Registration;
import com.college.sms.service.RegistrationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Registrations Directory GUI Panel.
 * Displays all course registrations with search, status filtering (All, Active, Cancelled),
 * and the ability to cancel an active registration (freeing up the course seat).
 */
public class ViewRegistrationsPanel extends JPanel implements RefreshablePanel {
    private final RegistrationService registrationService;
    private final MainFrame mainFrame;

    // Filters
    private JComboBox<String> cmbStatusFilter;
    private JTextField txtSearch;

    // Table
    private DefaultTableModel tableModel;
    private JTable registrationTable;

    public ViewRegistrationsPanel(RegistrationService registrationService, MainFrame mainFrame) {
        this.registrationService = registrationService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- Top: Filter and Search Bar ---
        JPanel topFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topFilterPanel.setBackground(Color.WHITE);
        topFilterPanel.setBorder(UITheme.createCardBorder("Filter & Search Registrations"));

        JLabel lblStatus = new JLabel("Status Filter:");
        lblStatus.setFont(UITheme.FONT_SUBHEADER);

        String[] statuses = {"ALL", "ACTIVE", "CANCELLED"};
        cmbStatusFilter = new JComboBox<>(statuses);
        cmbStatusFilter.setFont(UITheme.FONT_REGULAR);
        cmbStatusFilter.setBackground(Color.WHITE);
        cmbStatusFilter.addActionListener(e -> applyFilterAndSearch());

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UITheme.FONT_SUBHEADER);
        txtSearch = new JTextField(18);
        UITheme.styleTextField(txtSearch);
        txtSearch.addActionListener(e -> applyFilterAndSearch());

        JButton btnSearch = UITheme.createButton("Search", UITheme.PRIMARY);
        btnSearch.addActionListener(e -> applyFilterAndSearch());

        JButton btnReset = UITheme.createButton("Reset", UITheme.TEXT_MUTED);
        btnReset.addActionListener(e -> {
            cmbStatusFilter.setSelectedIndex(0);
            txtSearch.setText("");
            refreshData();
        });

        topFilterPanel.add(lblStatus);
        topFilterPanel.add(cmbStatusFilter);
        topFilterPanel.add(Box.createHorizontalStrut(15));
        topFilterPanel.add(lblSearch);
        topFilterPanel.add(txtSearch);
        topFilterPanel.add(btnSearch);
        topFilterPanel.add(btnReset);

        add(topFilterPanel, BorderLayout.NORTH);

        // --- Center: Registrations Table ---
        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));
        tableContainer.setBackground(Color.WHITE);
        tableContainer.setBorder(UITheme.createCardBorder("Enrollment Records"));

        String[] columns = {"Reg ID", "Student ID", "Student Name", "Course Code", "Course Title", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        registrationTable = new JTable(tableModel);
        UITheme.styleTable(registrationTable);

        // Custom status column color renderer
        registrationTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if ("ACTIVE".equalsIgnoreCase(String.valueOf(value))) {
                    setForeground(UITheme.SUCCESS);
                    setFont(UITheme.FONT_BOLD);
                } else {
                    setForeground(UITheme.DANGER);
                    setFont(UITheme.FONT_REGULAR);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(registrationTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom: Actions Panel ---
        JPanel bottomActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomActionPanel.setOpaque(false);

        JButton btnCancel = UITheme.createButton("Cancel Selected Registration", UITheme.DANGER);
        btnCancel.setFont(UITheme.FONT_HEADER);
        btnCancel.addActionListener(e -> handleCancelRegistration());

        JButton btnRefresh = UITheme.createButton("Refresh Records", UITheme.PRIMARY);
        btnRefresh.addActionListener(e -> refreshData());

        bottomActionPanel.add(btnRefresh);
        bottomActionPanel.add(btnCancel);
        tableContainer.add(bottomActionPanel, BorderLayout.SOUTH);

        add(tableContainer, BorderLayout.CENTER);
    }

    private void handleCancelRegistration() {
        int selectedRow = registrationTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a registration from the table to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String regId = tableModel.getValueAt(selectedRow, 0).toString();
        String studentName = tableModel.getValueAt(selectedRow, 2).toString();
        String courseTitle = tableModel.getValueAt(selectedRow, 4).toString();
        String currentStatus = tableModel.getValueAt(selectedRow, 6).toString();

        if (Registration.STATUS_CANCELLED.equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this,
                    "Registration '" + regId + "' is already cancelled.",
                    "Already Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel registration '" + regId + "'?\n\n" +
                        "Student: " + studentName + "\n" +
                        "Course: " + courseTitle + "\n\n" +
                        "This will free up one seat in the course catalog.",
                "Confirm Course Drop / Cancellation",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                registrationService.cancelRegistration(regId);
                JOptionPane.showMessageDialog(this,
                        "Registration '" + regId + "' was cancelled successfully.\nCourse seat has been released.",
                        "Registration Cancelled", JOptionPane.INFORMATION_MESSAGE);

                mainFrame.refreshAllPanels();
            } catch (SMSException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void applyFilterAndSearch() {
        String filter = cmbStatusFilter.getSelectedItem().toString();
        String query = txtSearch.getText().trim().toLowerCase();

        List<Registration> list = registrationService.getAllRegistrations();
        List<Registration> filtered = new ArrayList<>();

        for (Registration r : list) {
            boolean matchesFilter = filter.equals("ALL") || r.getStatus().equalsIgnoreCase(filter);
            if (!matchesFilter) continue;

            boolean matchesQuery = query.isEmpty() ||
                    r.getRegistrationId().toLowerCase().contains(query) ||
                    (r.getStudentId() != null && r.getStudentId().toLowerCase().contains(query)) ||
                    (r.getStudent() != null && r.getStudent().getName().toLowerCase().contains(query)) ||
                    (r.getCourseCode() != null && r.getCourseCode().toLowerCase().contains(query)) ||
                    (r.getCourse() != null && r.getCourse().getCourseTitle().toLowerCase().contains(query));

            if (matchesQuery) {
                filtered.add(r);
            }
        }

        populateTable(filtered);
    }

    private void populateTable(List<Registration> list) {
        tableModel.setRowCount(0);
        for (Registration r : list) {
            tableModel.addRow(new Object[]{
                    r.getRegistrationId(),
                    r.getStudentId(),
                    r.getStudent() != null ? r.getStudent().getName() : "N/A",
                    r.getCourseCode(),
                    r.getCourse() != null ? r.getCourse().getCourseTitle() : "N/A",
                    r.getRegistrationDate(),
                    r.getStatus()
            });
        }
    }

    @Override
    public void refreshData() {
        applyFilterAndSearch();
    }
}
