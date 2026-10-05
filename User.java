package com.mycompany.projectgul2;

import java.sql.*;

public class User {
	public final int id;
	public final String email;
	public final String role;
	public final String phone;

	public User(int id, String email, String role, String phone) {
		this.id = id;
		this.email = email;
		this.role = role;
		this.phone = phone;
	}

	public static boolean register(String email, String password, String phone, String role) throws SQLException {
		String sql = "INSERT INTO users (email, password, phone, role) VALUES (?, ?, ?, ?)";
		try (Connection conn = DBConnection.getConnection();
			        PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, email);
			pst.setString(2, password);
			pst.setString(3, phone);
			pst.setString(4, role);
			return pst.executeUpdate() > 0;
		}
	}

	public static User login(String email, String password, String role) throws SQLException {
		String sql = "SELECT id, email, role, phone FROM users WHERE email = ? AND password = ? AND role = ?";
		try (Connection conn = DBConnection.getConnection();
			        PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, email);
			pst.setString(2, password);
			pst.setString(3, role);
			ResultSet rs = pst.executeQuery();
			return rs.next() ? new User(
			           rs.getInt("id"),
			           rs.getString("email"),
			           rs.getString("role"),
			           rs.getString("phone")
			       ) : null;
		}
	}

	public static boolean exists(String email) throws SQLException {
		String sql = "SELECT 1 FROM users WHERE email = ?";
		try (Connection conn = DBConnection.getConnection();
			        PreparedStatement pst = conn.prepareStatement(sql)) {
			pst.setString(1, email);
			return pst.executeQuery().next();
		}
	}
}