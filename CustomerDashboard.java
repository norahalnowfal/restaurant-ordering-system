package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;

public class CustomerDashboard extends JFrame {
	private final User user;
	private JPanel centerPanel;

	public CustomerDashboard(User user) {
		this.user = user;
		setTitle("Customer Dashboard - " + user.email);
		setSize(500, 400);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setupUI();
	}

	private void setupUI() {
		setLayout(new BorderLayout());
		add(new JLabel("Welcome, " + user.email + "!", SwingConstants.CENTER), BorderLayout.NORTH);

		centerPanel = new JPanel(new GridLayout(4, 1, 10, 10));

		centerPanel.add(new JButton("My Cart") {
			{
				addActionListener(e -> new CartAndCheckout(user.id).setVisible(true));
			}
		});

		centerPanel.add(new JButton("Order Tracking") {
			{
				addActionListener(e -> new OrderTracking(user.id).setVisible(true));
			}
		});

		centerPanel.add(new JButton("Browse Restaurants") {
			{
				addActionListener(e -> new RestaurantBrowser(user.id).setVisible(true));
			}
		});

		centerPanel.add(new JButton("Feedback & Reviews") {
			{
				addActionListener(e -> new FeedbackFrame(user.id).setVisible(true));
			}
		});

		add(centerPanel, BorderLayout.CENTER);

		JButton logoutButton = new JButton("Logout");
		logoutButton.addActionListener(e -> {
			dispose();
			new ProjectGUL2().setVisible(true);
		});
		add(logoutButton, BorderLayout.SOUTH);
	}
}