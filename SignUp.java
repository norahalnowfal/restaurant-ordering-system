package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SignUp extends JFrame {
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField phoneField = new JTextField(20);
    private final JComboBox<String> roleCombo = new JComboBox<>(new String[] {"customer", "owner"});

    public SignUp(JFrame parent) {
        setTitle("Sign Up");
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        add(roleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new JButton("Register") {
            {
                addActionListener(e -> handleRegister());
            }
        });
        buttonPanel.add(new JButton("Cancel") {
            {
                addActionListener(e -> dispose());
            }
        });
        add(buttonPanel, gbc);
    }

    private void handleRegister() {
        // ✅ Field validation
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

        if (!email.matches(emailRegex)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }

        if (email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    String checkSql = "SELECT id FROM users WHERE email = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                    checkStmt.setString(1, email);
                    if (checkStmt.executeQuery().next()) return false;

                    String insertSql = "INSERT INTO users (email, password, phone, role) VALUES (?, ?, ?, ?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                    insertStmt.setString(1, email);
                    insertStmt.setString(2, password);
                    insertStmt.setString(3, phone);
                    insertStmt.setString(4, (String) roleCombo.getSelectedItem());
                    return insertStmt.executeUpdate() > 0;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(SignUp.this, "Registration successful!");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(SignUp.this, "Email already exists.");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(SignUp.this, "Error: " + e.getMessage());
                }
            }
        }.execute();
    }
}