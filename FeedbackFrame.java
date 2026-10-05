package com.mycompany.projectgul2;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class FeedbackFrame extends JFrame {
    private JComboBox<String> typeComboBox; // مطعم أو طبق
    private JComboBox<String> targetComboBox; // المطاعم أو الأطباق
    private JComboBox<Integer> ratingComboBox; // 1-5 نجوم
	private JTextArea commentArea;
	private JTextArea reviewsArea;
	private int userId;

	public FeedbackFrame(int userId) {
		this.userId = userId;
		setTitle("Feedback System");
		setSize(500, 600);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		JPanel topPanel = new JPanel(new GridLayout(6, 1, 5, 5));

		typeComboBox = new JComboBox<>(new String[] {"Restaurant", "Dish"});
		targetComboBox = new JComboBox<>();
		ratingComboBox = new JComboBox<>(new Integer[] {1, 2, 3, 4, 5});
		commentArea = new JTextArea(3, 20);

		JButton submitButton = new JButton("Submit Feedback");
		JButton loadReviewsButton = new JButton("Load Reviews");

		topPanel.add(new JLabel("Choose type:"));
		topPanel.add(typeComboBox);
		topPanel.add(new JLabel("Choose target:"));
		topPanel.add(targetComboBox);
		topPanel.add(new JLabel("Your Rating:"));
		topPanel.add(ratingComboBox);

		add(topPanel, BorderLayout.NORTH);

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(new JLabel("Your Comment:"), BorderLayout.NORTH);
		centerPanel.add(new JScrollPane(commentArea), BorderLayout.CENTER);

		add(centerPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
		bottomPanel.add(submitButton);
		bottomPanel.add(loadReviewsButton);
		add(bottomPanel, BorderLayout.SOUTH);

		reviewsArea = new JTextArea();
		reviewsArea.setEditable(false);
		add(new JScrollPane(reviewsArea), BorderLayout.EAST);

		// Listeners
		typeComboBox.addActionListener(e -> loadTargets());
		submitButton.addActionListener(e -> submitFeedback());
		loadReviewsButton.addActionListener(e -> loadReviews());

		loadTargets();
		setVisible(true);
	}

	private void loadTargets() {
		targetComboBox.removeAllItems();
		try (Connection conn = DBConnection.getConnection()) {
			String sql = typeComboBox.getSelectedItem().equals("Restaurant") ?
			             "SELECT id, name FROM shops" :
			             "SELECT id, name FROM items";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				targetComboBox.addItem(rs.getInt("id") + " - " + rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void submitFeedback() {
		try (Connection conn = DBConnection.getConnection()) {
			String selected = (String) targetComboBox.getSelectedItem();
			if (selected == null) {
				JOptionPane.showMessageDialog(this, "Please select a target first.");
				return;
			}
			int targetId = Integer.parseInt(selected.split(" - ")[0]);
			int rating = (int) ratingComboBox.getSelectedItem();
			String comment = commentArea.getText();
			String table = typeComboBox.getSelectedItem().equals("Restaurant") ?
			               "restaurant_reviews" : "dish_reviews";
			String column = typeComboBox.getSelectedItem().equals("Restaurant") ?
			                "shop_id" : "item_id";

			String sql = "INSERT INTO " + table + " (user_id, " + column + ", rating, comment) VALUES (?, ?, ?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, userId);
			stmt.setInt(2, targetId);
			stmt.setInt(3, rating);
			stmt.setString(4, comment);
			stmt.executeUpdate();

			JOptionPane.showMessageDialog(this, "Feedback submitted successfully!");
			commentArea.setText("");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadReviews() {
		reviewsArea.setText("");
		try (Connection conn = DBConnection.getConnection()) {
			String selected = (String) targetComboBox.getSelectedItem();
			if (selected == null) {
				JOptionPane.showMessageDialog(this, "Please select a target first.");
				return;
			}
			int targetId = Integer.parseInt(selected.split(" - ")[0]);
			String table = typeComboBox.getSelectedItem().equals("Restaurant") ?
			               "restaurant_reviews" : "dish_reviews";
			String column = typeComboBox.getSelectedItem().equals("Restaurant") ?
			                "shop_id" : "item_id";

			String sql = "SELECT rating, comment, review_time FROM " + table + " WHERE " + column + " = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, targetId);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				int rating = rs.getInt("rating");
				String comment = rs.getString("comment");
				Timestamp time = rs.getTimestamp("review_time");

				reviewsArea.append("Rating: " + rating + "/5\n");
				reviewsArea.append("Comment: " + comment + "\n");
				reviewsArea.append("At: " + time.toString() + "\n\n");
			}

			if (reviewsArea.getText().isEmpty()) {
				reviewsArea.setText("No reviews yet!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}