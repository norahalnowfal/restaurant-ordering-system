package com.mycompany.projectgul2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AdminDashboard extends JFrame {
    private final User user;

    public AdminDashboard(User user) {
        this.user = user;
        setTitle("Admin Dashboard - " + user.email);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.add(new JLabel("Admin Dashboard - " + user.email, SwingConstants.CENTER));
        headerPanel.setFont(new Font("Arial", Font.BOLD, 16));

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel restaurantsPanel = new JPanel(new BorderLayout());
        restaurantsPanel.add(new ManageRestaurantsPanel(), BorderLayout.CENTER);
        tabbedPane.addTab("Restaurants", restaurantsPanel);

        JPanel usersPanel = new JPanel(new BorderLayout());
        usersPanel.add(new ManageUsersPanel(), BorderLayout.CENTER);
        tabbedPane.addTab("Users", usersPanel);

        JPanel reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.add(new ViewReportsPanel(), BorderLayout.CENTER);
        tabbedPane.addTab("Reports", reportsPanel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new ProjectGUL2().setVisible(true);
        });

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(logoutButton, BorderLayout.SOUTH);
    }

    private class ManageRestaurantsPanel extends JPanel {
    private JTable restaurantsTable;

    public ManageRestaurantsPanel() {
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();

        JButton refreshBtn = new JButton("Refresh");
        JButton addBtn = new JButton("Add Restaurant");
        JButton editBtn = new JButton("Edit Restaurant");
        JButton deleteBtn = new JButton("Delete Restaurant");

        refreshBtn.addActionListener(e -> loadRestaurants());

        addBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter restaurant name:");
            String category = JOptionPane.showInputDialog(this, "Enter restaurant category:");
            if (name != null && category != null) {
                addRestaurant(name, category);
            }
        });

        editBtn.addActionListener(e -> {
            int selectedRow = restaurantsTable.getSelectedRow();
            if (selectedRow != -1) {
                int restaurantId = (int) restaurantsTable.getValueAt(selectedRow, 0);
                String name = JOptionPane.showInputDialog(this, "Enter new name:", restaurantsTable.getValueAt(selectedRow, 1));
                String category = JOptionPane.showInputDialog(this, "Enter new category:", restaurantsTable.getValueAt(selectedRow, 2));
                if (name != null && category != null) {
                    updateRestaurant(restaurantId, name, category);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a restaurant to edit.");
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = restaurantsTable.getSelectedRow();
            if (selectedRow != -1) {
                int restaurantId = (int) restaurantsTable.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this restaurant?");
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteRestaurant(restaurantId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a restaurant to delete.");
            }
        });

        buttonPanel.add(refreshBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        add(buttonPanel, BorderLayout.NORTH);

        restaurantsTable = new JTable();
        add(new JScrollPane(restaurantsTable), BorderLayout.CENTER);
        loadRestaurants();
    }

    private void loadRestaurants() {
        new SwingWorker<DefaultTableModel, Void>() {
            @Override
            protected DefaultTableModel doInBackground() throws Exception {
                DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Name", "Category", "Orders Count"}, 0);

                String sql = "SELECT s.id, s.name, s.category, COUNT(o.order_id) as order_count " +
                             "FROM shops s LEFT JOIN orders o ON s.id = o.shop_id " +
                             "GROUP BY s.id, s.name, s.category";

                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getInt("order_count")
                        });
                    }
                }
                return model;
            }

            @Override
            protected void done() {
                try {
                    restaurantsTable.setModel(get());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ManageRestaurantsPanel.this,
                        "Error loading restaurants: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void addRestaurant(String name, String category) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO shops (name, category) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.executeUpdate();
            loadRestaurants();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding restaurant: " + e.getMessage());
        }
    }

    private void updateRestaurant(int id, String name, String category) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE shops SET name = ?, category = ? WHERE id = ?")) {
            stmt.setString(1, name);
            stmt.setString(2, category);
            stmt.setInt(3, id);
            stmt.executeUpdate();
            loadRestaurants();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating restaurant: " + e.getMessage());
        }
    }

    private void deleteRestaurant(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM shops WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            loadRestaurants();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting restaurant: " + e.getMessage());
        }
    }
}

    private class ManageUsersPanel extends JPanel {
        private JTable usersTable;

        public ManageUsersPanel() {
            setLayout(new BorderLayout());
            JPanel buttonPanel = new JPanel();

            JButton refreshBtn = new JButton("Refresh");
            JButton deleteBtn = new JButton("Delete User");
            JButton updateBtn = new JButton("Update User");

            refreshBtn.addActionListener(e -> loadUsers());

            deleteBtn.addActionListener(e -> {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow != -1) {
                    int userId = (int) usersTable.getValueAt(selectedRow, 0);
                    int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this user?");
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteUser(userId);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a user to delete.");
                }
            });

            updateBtn.addActionListener(e -> {
                int selectedRow = usersTable.getSelectedRow();
                if (selectedRow != -1) {
                    int userId = (int) usersTable.getValueAt(selectedRow, 0);
                    String newEmail = JOptionPane.showInputDialog(this, "Enter new email:");
                    String newPhone = JOptionPane.showInputDialog(this, "Enter new phone:");
                    String newRole = JOptionPane.showInputDialog(this, "Enter new role:");
                    if (newEmail != null && newPhone != null && newRole != null) {
                        updateUser(userId, newEmail, newPhone, newRole);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a user to update.");
                }
            });

            buttonPanel.add(refreshBtn);
            buttonPanel.add(deleteBtn);
            buttonPanel.add(updateBtn);
            add(buttonPanel, BorderLayout.NORTH);

            usersTable = new JTable();
            add(new JScrollPane(usersTable), BorderLayout.CENTER);
            loadUsers();
        }

        private void loadUsers() {
            new SwingWorker<DefaultTableModel, Void>() {
                @Override
                protected DefaultTableModel doInBackground() throws Exception {
                    DefaultTableModel model = new DefaultTableModel(
                        new Object[]{"ID", "Email", "Phone", "Role"}, 0);

                    String sql = "SELECT id, email, phone, role FROM users";

                    try (Connection conn = DBConnection.getConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                rs.getInt("id"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("role")
                            });
                        }
                    }
                    return model;
                }

                @Override
                protected void done() {
                    try {
                        usersTable.setModel(get());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ManageUsersPanel.this,
                            "Error loading users: " + e.getMessage());
                    }
                }
            }.execute();
        }

        private void deleteUser(int userId) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage());
            }
        }

        private void updateUser(int userId, String email, String phone, String role) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE users SET email = ?, phone = ?, role = ? WHERE id = ?")) {
                stmt.setString(1, email);
                stmt.setString(2, phone);
                stmt.setString(3, role);
                stmt.setInt(4, userId);
                stmt.executeUpdate();
                loadUsers();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error updating user: " + e.getMessage());
            }
        }
    }

    private class ViewReportsPanel extends JPanel {
        private JTable reportsTable;

        public ViewReportsPanel() {
            setLayout(new BorderLayout());

            JComboBox<String> reportType = new JComboBox<>(new String[]{
                "Sales by Restaurant",
                "Popular Items",
                "User Activity"
            });

            JButton generateBtn = new JButton("Generate Report");
            generateBtn.addActionListener(e -> generateReport(
                (String) reportType.getSelectedItem()));

            JPanel controlPanel = new JPanel();
            controlPanel.add(new JLabel("Report Type:"));
            controlPanel.add(reportType);
            controlPanel.add(generateBtn);

            add(controlPanel, BorderLayout.NORTH);

            reportsTable = new JTable();
            add(new JScrollPane(reportsTable), BorderLayout.CENTER);
        }

        private void generateReport(String reportType) {
            new SwingWorker<DefaultTableModel, Void>() {
                @Override
                protected DefaultTableModel doInBackground() throws Exception {
                    DefaultTableModel model = new DefaultTableModel();
                    String sql = "";

                    switch (reportType) {
                        case "Sales by Restaurant":
                            sql = "SELECT s.name, SUM(o.total) as total_sales, COUNT(o.order_id) as orders " +
                                  "FROM orders o JOIN shops s ON o.shop_id = s.id " +
                                  "GROUP BY s.name ORDER BY total_sales DESC";
                            model.setColumnIdentifiers(new Object[]{"Restaurant", "Total Sales", "Orders"});
                            break;

                        case "Popular Items":
                            sql = "SELECT i.name, SUM(oi.quantity) as total_ordered, s.name as restaurant " +
                                  "FROM order_items oi JOIN items i ON oi.item_id = i.id " +
                                  "JOIN shops s ON i.shop_id = s.id " +
                                  "GROUP BY i.name, s.name ORDER BY total_ordered DESC LIMIT 20";
                            model.setColumnIdentifiers(new Object[]{"Item", "Quantity Sold", "Restaurant"});
                            break;

                        case "User Activity":
                            sql = "SELECT u.email, COUNT(o.order_id) as orders, SUM(o.total) as total_spent " +
                                  "FROM users u LEFT JOIN orders o ON u.id = o.user_id " +
                                  "GROUP BY u.email ORDER BY orders DESC";
                            model.setColumnIdentifiers(new Object[]{"User", "Orders", "Total Spent"});
                            break;
                    }

                    try (Connection conn = DBConnection.getConnection();
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        while (rs.next()) {
                            Vector<Object> row = new Vector<>();
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                row.add(rs.getObject(i));
                            }
                            model.addRow(row);
                        }
                    }
                    return model;
                }

                @Override
                protected void done() {
                    try {
                        reportsTable.setModel(get());
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(ViewReportsPanel.this,
                            "Error generating report: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }
}
