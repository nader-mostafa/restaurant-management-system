package ui;

// ده الملف المسؤول عن واجهة المستخدم الخاصة بشاشة تسجيل الدخول (Login)

import models.Models.Admin;
import models.Models.Employee;
import models.Models.User;
import services.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginFrame() {
        setTitle("Restaurant Management System");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main Container
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        // Title
        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSub = new JLabel("Sign in to continue");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Inputs
        txtUsername = new JTextField();
        txtUsername.putClientProperty("JTextField.placeholderText", "Username");
        txtUsername.putClientProperty("JComponent.roundRect", true);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        txtPassword = new JPasswordField();
        txtPassword.putClientProperty("JTextField.placeholderText", "Password");
        txtPassword.putClientProperty("JComponent.roundRect", true);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Login Button
        btnLogin = new JButton("Login");
        btnLogin.putClientProperty("JButton.buttonType", "roundRect");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185)); // Professional blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnLogin.addActionListener(e -> attemptLogin());

        // Assembly
        mainPanel.add(lblTitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(lblSub);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(txtUsername);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(txtPassword);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        mainPanel.add(btnLogin);

        add(mainPanel);
    }

    private void attemptLogin() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        User user = AuthService.login(username, password);
        if (user != null) {
            dispose();
            if (user instanceof Admin) {
                new AdminDashboard((Admin) user).setVisible(true);
            } else if (user instanceof Employee) {
                new EmployeeDashboard((Employee) user).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials! Please try again.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
