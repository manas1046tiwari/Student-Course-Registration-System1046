package com.college.sms.gui;

import com.college.sms.service.CourseService;
import com.college.sms.service.RegistrationService;
import com.college.sms.service.StudentService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Clean and secure Login window for the Course Registration System.
 * Authenticates administrator credentials before launching the main dashboard.
 */
public class LoginFrame extends JFrame {
    private final StudentService studentService;
    private final CourseService courseService;
    private final RegistrationService registrationService;

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame(StudentService studentService, CourseService courseService, RegistrationService registrationService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.registrationService = registrationService;

        setTitle("Login - Student Course Registration System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 480);
        setResizable(false);
        setLocationRelativeTo(null); // Center on screen

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BACKGROUND);

        // --- Top Header ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(UITheme.PRIMARY_DARK);
        topPanel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel lblTitle = new JLabel("College Portal");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("Student Course Registration System");
        lblSubtitle.setFont(UITheme.FONT_SUBHEADER);
        lblSubtitle.setForeground(new Color(191, 219, 254));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(lblTitle);
        topPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        topPanel.add(lblSubtitle);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // --- Center Form Card ---
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel formCard = new JPanel(new GridLayout(4, 1, 6, 10));
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        formCard.setPreferredSize(new Dimension(360, 220));

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UITheme.FONT_SUBHEADER);
        lblUser.setForeground(UITheme.TEXT_DARK);

        txtUsername = new JTextField();
        UITheme.styleTextField(txtUsername);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_SUBHEADER);
        lblPass.setForeground(UITheme.TEXT_DARK);

        txtPassword = new JPasswordField();
        txtPassword.setFont(UITheme.FONT_REGULAR);
        txtPassword.setCaretColor(UITheme.PRIMARY);
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));

        // Submit on Enter key inside password field
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        formCard.add(lblUser);
        formCard.add(txtUsername);
        formCard.add(lblPass);
        formCard.add(txtPassword);

        formContainer.add(formCard);
        mainPanel.add(formContainer, BorderLayout.CENTER);

        // --- Bottom Actions Panel ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(0, 40, 25, 40));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 12, 0));
        btnRow.setOpaque(false);

        JButton btnLogin = UITheme.createButton("Sign In", UITheme.PRIMARY);
        btnLogin.setPreferredSize(new Dimension(0, 40));
        btnLogin.addActionListener(e -> handleLogin());

        JButton btnExit = UITheme.createButton("Exit", UITheme.TEXT_MUTED);
        btnExit.setPreferredSize(new Dimension(0, 40));
        btnExit.addActionListener(e -> System.exit(0));

        btnRow.add(btnLogin);
        btnRow.add(btnExit);

        JLabel lblHint = new JLabel("Demo credentials: admin / admin123", SwingConstants.CENTER);
        lblHint.setFont(UITheme.FONT_SMALL);
        lblHint.setForeground(UITheme.TEXT_MUTED);
        lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(btnRow);
        bottomPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        bottomPanel.add(lblHint);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your username.", "Input Required", JOptionPane.WARNING_MESSAGE);
            txtUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your password.", "Input Required", JOptionPane.WARNING_MESSAGE);
            txtPassword.requestFocus();
            return;
        }

        // Validate credentials (beginner-friendly college authentication)
        if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
            // Success
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(studentService, courseService, registrationService);
                mainFrame.setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password.\n\nPlease check your credentials and try again.",
                    "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
            txtPassword.requestFocus();
        }
    }
}
