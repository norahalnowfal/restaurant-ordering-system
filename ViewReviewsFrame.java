package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ViewReviewsFrame extends JFrame {
	private final JTextArea reviewsArea = new JTextArea();
	private final int userId;
	private final boolean isOwner;

	public ViewReviewsFrame(int userId, boolean isOwner) {
		this.userId = userId;
		this.isOwner = isOwner;
		setTitle("Reviews");
		setSize(600, 400);
		setLocationRelativeTo(null);

		reviewsArea.setEditable(false);
		add(new JScrollPane(reviewsArea));

		JButton loadBtn = new JButton("Load Reviews");
		loadBtn.addActionListener(e -> loadReviews());
		add(loadBtn, BorderLayout.SOUTH);
	}

	private void loadReviews() {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try (Connection conn = DBConnection.getConnection()) {
					String sql = isOwner
					             ? "SELECT r.rating, r.comment, u.email FROM reviews r JOIN users u ON r.user_id = u.id"
					             : "SELECT rating, comment FROM reviews WHERE user_id = ?";

					PreparedStatement stmt = conn.prepareStatement(sql);
					if (!isOwner) stmt.setInt(1, userId);

					ResultSet rs = stmt.executeQuery();
					StringBuilder sb = new StringBuilder();

					while (rs.next()) {
						if (isOwner) {
							sb.append("User: ").append(rs.getString("email")).append("\n");
						}
						sb.append("Rating: ").append(rs.getInt("rating")).append("/5\n")
						.append("Comment: ").append(rs.getString("comment")).append("\n\n");
					}

					reviewsArea.setText(sb.toString());
				}
				return null;
			}
		} .execute();
	}
}