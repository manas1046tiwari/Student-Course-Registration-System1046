package com.college.sms.gui;

import com.college.sms.service.CourseService;
import com.college.sms.service.RegistrationService;
import com.college.sms.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application window after successful login.
 * Hosts the header bar, user status, logout option, and a multi-tabbed interface
 * for Dashboard, Students, Courses, Registration, and Registrations Directory.
 */
public class MainFrame extends JFrame {
    private final StudentService studentService;
    private final CourseService courseService;
    private final RegistrationService registrationService;

    private JTabbedPane tabbedPane;
    private final List<RefreshablePanel> registeredPanels = new ArrayList<>();

    public MainFrame(StudentService studentService, CourseService courseService, RegistrationService registrationService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.registrationService = registrationService;

        setTitle("Student Course Registration & Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null); // Center on screen

        // Intercept close to ask confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        initComponents();
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(UITheme.BACKGROUND);

        // --- Top Header Bar ---
        JPanel headerBar = new JPanel(new BorderLayout(15, 0));
        headerBar.setBackground(UITheme.PRIMARY_DARK);
        headerBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Title and Subtitle
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel lblAppTitle = new JLabel("Student Course Registration and Management System");
        lblAppTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblAppTitle.setForeground(Color.WHITE);

        JLabel lblAppSub = new JLabel("College OOP Mini Project - Second Year B.Tech");
        lblAppSub.setFont(UITheme.FONT_SMALL);
        lblAppSub.setForeground(new Color(191, 219, 254));

        titlePanel.add(lblAppTitle);
        titlePanel.add(lblAppSub);
        headerBar.add(titlePanel, BorderLayout.WEST);

        // Right side: User Profile & Actions
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        headerRight.setOpaque(false);

        JLabel lblUser = new JLabel("Logged in as: Admin");
        lblUser.setFont(UITheme.FONT_SUBHEADER);
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = UITheme.createButton("Logout", new Color(239, 68, 68));
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.setPreferredSize(new Dimension(85, 32));
        btnLogout.addActionListener(e -> handleLogout());

        JButton btnExit = UITheme.createButton("Exit", new Color(100, 116, 139));
        btnExit.setFont(UITheme.FONT_SMALL);
        btnExit.setPreferredSize(new Dimension(75, 32));
        btnExit.addActionListener(e -> confirmExit());

        headerRight.add(lblUser);
        headerRight.add(btnLogout);
        headerRight.add(btnExit);
        headerBar.add(headerRight, BorderLayout.EAST);

        contentPane.add(headerBar, BorderLayout.NORTH);

        // --- Center Tabbed Pane ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_HEADER);
        tabbedPane.setBackground(Color.WHITE);

        // 1. Dashboard Tab
        DashboardPanel dashboardPanel = new DashboardPanel(studentService, courseService, registrationService, this);
        tabbedPane.addTab("  Dashboard  ", dashboardPanel);
        registeredPanels.add(dashboardPanel);

        // 2. Students Tab
        StudentPanel studentPanel = new StudentPanel(studentService, this);
        tabbedPane.addTab("  Students  ", studentPanel);
        registeredPanels.add(studentPanel);

        // 3. Courses Tab
        CoursePanel coursePanel = new CoursePanel(courseService, this);
        tabbedPane.addTab("  Courses  ", coursePanel);
        registeredPanels.add(coursePanel);

        // 4. Registration Tab
        RegistrationPanel registrationPanel = new RegistrationPanel(studentService, courseService, registrationService, this);
        tabbedPane.addTab("  Register Course  ", registrationPanel);
        registeredPanels.add(registrationPanel);

        // 5. View Registrations Tab
        ViewRegistrationsPanel viewRegPanel = new ViewRegistrationsPanel(registrationService, this);
        tabbedPane.addTab("  View Registrations  ", viewRegPanel);
        registeredPanels.add(viewRegPanel);

        // Auto-refresh panel when switching tabs
        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            if (selected >= 0 && selected < registeredPanels.size()) {
                registeredPanels.get(selected).refreshData();
            }
        });

        contentPane.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(contentPane);
    }

    /**
     * Programmatically switches to the specified tab index.
     */
    public void switchToTab(int index) {
        if (index >= 0 && index < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    /**
     * Refreshes all tabs to synchronize additions, edits, deletions, and registrations.
     */
    public void refreshAllPanels() {
        for (RefreshablePanel panel : registeredPanels) {
            panel.refreshData();
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(studentService, courseService, registrationService);
                loginFrame.setVisible(true);
            });
        }
    }

    private void confirmExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to exit the application?",
                "Exit System", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
