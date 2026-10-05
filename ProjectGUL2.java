package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ProjectGUL2 extends JFrame {
	private final JTextField emailField = new JTextField(20);
	private final JPasswordField passwordField = new JPasswordField(20);
	private final JComboBox<String> roleCombo = new JComboBox<>(new String[] {"customer", "admin", "owner"});

	public ProjectGUL2() {
		setTitle("Login System");
		setSize(400, 300);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
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
		add(new JLabel("Role:"), gbc);
		gbc.gridx = 1;
		add(roleCombo, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new JButton("Login") {
			{
				addActionListener(e -> handleLogin());
			}
		});
		buttonPanel.add(new JButton("Sign Up") {
			{
				addActionListener(e -> new SignUp(ProjectGUL2.this).setVisible(true));
			}
		});
		add(buttonPanel, gbc);
	}

	private void handleLogin() {
		new SwingWorker<User, Void>() {
			@Override
			protected User doInBackground() throws Exception {
				try (Connection conn = DBConnection.getConnection()) {
					String sql = "SELECT id, email, role, phone FROM users WHERE email = ? AND password = ? AND role = ?";
					PreparedStatement stmt = conn.prepareStatement(sql);
					stmt.setString(1, emailField.getText());
					stmt.setString(2, new String(passwordField.getPassword()));
					stmt.setString(3, (String) roleCombo.getSelectedItem());
					ResultSet rs = stmt.executeQuery();

					if (rs.next()) {
						return new User(
						           rs.getInt("id"),
						           rs.getString("email"),
						           rs.getString("role"),
						           rs.getString("phone")
						       );
					}
					return null;
				}
			}

			@Override
			protected void done() {
				try {
					User user = get();
					if (user != null) {
						dispose();
						switch (user.role) {
						case "customer":
							new CustomerDashboard(user).setVisible(true);
							break;
						case "admin":
							new AdminDashboard(user).setVisible(true);
							break;
						case "owner":
							new RestaurantOwnerDashboard(user).setVisible(true);
							break;
						}
					} else {
						JOptionPane.showMessageDialog(ProjectGUL2.this, "Invalid credentials");
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(ProjectGUL2.this, "Error: " + ex.getMessage());
				}
			}
		} .execute();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new ProjectGUL2().setVisible(true));
	}
}