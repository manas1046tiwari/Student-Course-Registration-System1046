package com.college.sms.gui;

import com.college.sms.model.Registration;
import com.college.sms.service.CourseService;
import com.college.sms.service.RegistrationService;
import com.college.sms.service.StudentService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Overview Dashboard panel showing real-time summary statistics,
 * quick action buttons to navigate tabs, and a preview of recent registrations.
 */
public class DashboardPanel extends JPanel implements RefreshablePanel {
    private final StudentService studentService;
    private final CourseService courseService;
    private final RegistrationService registrationService;
    private final MainFrame mainFrame;

    // Stat labels
    private JLabel lblStudentCount;
    private JLabel lblCourseCount;
    private JLabel lblRegistrationCount;

    // Recent activity table
    private DefaultTableModel recentTableModel;
    private JTable recentTable;

    public DashboardPanel(StudentService studentService, CourseService courseService,
                          RegistrationService registrationService, MainFrame mainFrame) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.registrationService = registrationService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // --- Top Header ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("System Overview & Statistics");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.PRIMARY_DARK);

        JLabel lblSubtitle = new JLabel("Live summary counts and registration monitoring");
        lblSubtitle.setFont(UITheme.FONT_REGULAR);
        lblSubtitle.setForeground(UITheme.TEXT_MUTED);

        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(lblSubtitle, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // --- Center Content: KPI Cards + Quick Actions + Recent Registrations ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 1. Stat Cards Panel
        JPanel cardsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsGrid.setOpaque(false);
        cardsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblStudentCount = new JLabel("0", SwingConstants.CENTER);
        JPanel cardStudents = createStatCard("Total Students", lblStudentCount, new Color(59, 130, 246));

        lblCourseCount = new JLabel("0", SwingConstants.CENTER);
        JPanel cardCourses = createStatCard("Total Courses", lblCourseCount, new Color(16, 185, 129));

        lblRegistrationCount = new JLabel("0", SwingConstants.CENTER);
        JPanel cardRegistrations = createStatCard("Active Registrations", lblRegistrationCount, new Color(245, 158, 11));

        cardsGrid.add(cardStudents);
        cardsGrid.add(cardCourses);
        cardsGrid.add(cardRegistrations);
        centerPanel.add(cardsGrid);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Quick Action Bar
        JPanel quickActionsCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        quickActionsCard.setBackground(Color.WHITE);
        quickActionsCard.setBorder(UITheme.createCardBorder("Quick Navigation"));
        quickActionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JButton btnGoStudents = UITheme.createButton("Student Management", UITheme.PRIMARY);
        btnGoStudents.addActionListener(e -> mainFrame.switchToTab(1));

        JButton btnGoCourses = UITheme.createButton("Course Catalog", UITheme.PRIMARY);
        btnGoCourses.addActionListener(e -> mainFrame.switchToTab(2));

        JButton btnGoRegister = UITheme.createButton("Register Course", UITheme.SUCCESS);
        btnGoRegister.addActionListener(e -> mainFrame.switchToTab(3));

        JButton btnGoViewReg = UITheme.createButton("View Registrations", UITheme.PRIMARY_DARK);
        btnGoViewReg.addActionListener(e -> mainFrame.switchToTab(4));

        quickActionsCard.add(btnGoStudents);
        quickActionsCard.add(btnGoCourses);
        quickActionsCard.add(btnGoRegister);
        quickActionsCard.add(btnGoViewReg);
        centerPanel.add(quickActionsCard);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Recent Registrations Table Panel
        JPanel recentPanel = new JPanel(new BorderLayout(5, 10));
        recentPanel.setBackground(Color.WHITE);
        recentPanel.setBorder(UITheme.createCardBorder("Recent Course Registrations"));

        String[] columns = {"Registration ID", "Student ID", "Student Name", "Course Code", "Course Title", "Date", "Status"};
        recentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recentTable = new JTable(recentTableModel);
        UITheme.styleTable(recentTable);
        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        recentPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(recentPanel);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel lblCardTitle = new JLabel(title);
        lblCardTitle.setFont(UITheme.FONT_SUBHEADER);
        lblCardTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accentColor);

        // Accent strip on the left
        JPanel accentStrip = new JPanel();
        accentStrip.setPreferredSize(new Dimension(5, 0));
        accentStrip.setBackground(accentColor);

        card.add(accentStrip, BorderLayout.WEST);
        card.add(lblCardTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    @Override
    public void refreshData() {
        lblStudentCount.setText(String.valueOf(studentService.getStudentCount()));
        lblCourseCount.setText(String.valueOf(courseService.getCourseCount()));
        lblRegistrationCount.setText(String.valueOf(registrationService.getActiveRegistrations().size()));

        // Populate recent registrations
        recentTableModel.setRowCount(0);
        List<Registration> list = registrationService.getAllRegistrations();
        // Show up to the 10 most recent registrations (in reverse order)
        int count = 0;
        for (int i = list.size() - 1; i >= 0 && count < 10; i--, count++) {
            Registration r = list.get(i);
            recentTableModel.addRow(new Object[]{
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
}
