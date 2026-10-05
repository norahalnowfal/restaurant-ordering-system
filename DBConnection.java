package com.mycompany.projectgul2;

import java.sql.*;

public class DBConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/app";
	private static final String USER = "root";
	private static final String PASSWORD = "112233";

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}