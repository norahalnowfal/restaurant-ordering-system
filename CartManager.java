package com.mycompany.projectgul2;

import java.sql.*;

public class CartManager {
	public static void addToCart(int userId, int itemId, int quantity, double price) {

		String sql = "INSERT INTO cart_items (user_id, item_id, quantity, price) VALUES (?, ?, ?, ?) " +
		             "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";

		try (Connection conn = DBConnection.getConnection();
			        PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, userId);
			stmt.setInt(2, itemId);
			stmt.setInt(3, quantity);
			stmt.setDouble(4, price);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}