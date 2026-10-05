package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantOwnerDashboard extends JFrame {
    private final User user;
    private JList<String> shopsList;
    private JList<String> itemsList;
    private DefaultListModel<String> shopsModel;
    private DefaultListModel<String> itemsModel;
    private List<Shop> shops = new ArrayList<>();
    private List<Item> currentItems = new ArrayList<>();

    public RestaurantOwnerDashboard(User user) {
        this.user = user;
        setTitle("Owner Dashboard - " + user.email);
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupUI();
        loadShops();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.add(new JLabel("Welcome Owner: " + user.email, SwingConstants.CENTER));
        add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);

        JPanel shopsPanel = new JPanel(new BorderLayout());
        shopsPanel.setBorder(BorderFactory.createTitledBorder("Your Shops"));
        shopsModel = new DefaultListModel<>();
        shopsList = new JList<>(shopsModel);
        shopsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadItems(shopsList.getSelectedIndex());
            }
        });
        shopsPanel.add(new JScrollPane(shopsList), BorderLayout.CENTER);

        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Shop Items"));
        itemsModel = new DefaultListModel<>();
        itemsList = new JList<>(itemsModel);
        itemsPanel.add(new JScrollPane(itemsList), BorderLayout.CENTER);

        JPanel itemButtonsPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton addItemBtn = new JButton("Add Item");
        JButton editItemBtn = new JButton("Edit Item");
        JButton deleteItemBtn = new JButton("Delete Item");
        
        addItemBtn.addActionListener(e -> addItem());
        editItemBtn.addActionListener(e -> editItem());
        deleteItemBtn.addActionListener(e -> deleteItem());
        
        itemButtonsPanel.add(addItemBtn);
        itemButtonsPanel.add(editItemBtn);
        itemButtonsPanel.add(deleteItemBtn);
        itemsPanel.add(itemButtonsPanel, BorderLayout.SOUTH);

        JButton reviewsBtn = new JButton("View Reviews");
        reviewsBtn.addActionListener(e -> viewReviews());
        shopsPanel.add(reviewsBtn, BorderLayout.SOUTH);

        splitPane.setLeftComponent(shopsPanel);
        splitPane.setRightComponent(itemsPanel);
        add(splitPane, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            dispose();
            new ProjectGUL2().setVisible(true);
        });
        add(logoutButton, BorderLayout.SOUTH);
    }

    private void loadShops() {
        new SwingWorker<List<Shop>, Void>() {
            @Override
            protected List<Shop> doInBackground() throws Exception {
                List<Shop> results = new ArrayList<>();
                String sql = "SELECT id, name, category FROM shops WHERE owner_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, user.id);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        results.add(new Shop(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category")
                        ));
                    }
                }
                return results;
            }

            @Override
            protected void done() {
                try {
                    shops = get();
                    shopsModel.clear();
                    shops.forEach(s -> shopsModel.addElement(s.name + " (" + s.category + ")"));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this,
                        "Error loading shops: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void loadItems(int shopIndex) {
        if (shopIndex < 0 || shopIndex >= shops.size()) return;
        
        new SwingWorker<List<Item>, Void>() {
            @Override
            protected List<Item> doInBackground() throws Exception {
                List<Item> results = new ArrayList<>();
                String sql = "SELECT id, name, price, description FROM items WHERE shop_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, shops.get(shopIndex).id);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        results.add(new Item(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("description")
                        ));
                    }
                }
                return results;
            }

            @Override
            protected void done() {
                try {
                    currentItems = get();
                    itemsModel.clear();
                    currentItems.forEach(i -> itemsModel.addElement(
                        String.format("%s - %.2f SR (%s)", 
                            i.name, i.price, 
                            i.description != null ? i.description : "No description")
                    ));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this,
                        "Error loading items: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void addItem() {
        int shopIndex = shopsList.getSelectedIndex();
        if (shopIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shop first");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextArea descriptionArea = new JTextArea(3, 20);
        
        panel.add(new JLabel("Item Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Item", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double price = Double.parseDouble(priceField.getText());
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        String sql = "INSERT INTO items (shop_id, name, price, description) VALUES (?, ?, ?, ?)";
                        try (Connection conn = DBConnection.getConnection();
                             PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, shops.get(shopIndex).id);
                            stmt.setString(2, nameField.getText());
                            stmt.setDouble(3, price);
                            stmt.setString(4, descriptionArea.getText());
                            return stmt.executeUpdate() > 0;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                                    "Item added successfully");
                                loadItems(shopIndex);
                            } else {
                                JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                                    "Failed to add item");
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this,
                                "Error adding item: " + e.getMessage());
                        }
                    }
                }.execute();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price");
            }
        }
    }

    private void editItem() {
        int shopIndex = shopsList.getSelectedIndex();
        int itemIndex = itemsList.getSelectedIndex();
        
        if (shopIndex < 0 || itemIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select both a shop and an item");
            return;
        }

        Item selectedItem = currentItems.get(itemIndex);
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField nameField = new JTextField(selectedItem.name);
        JTextField priceField = new JTextField(String.valueOf(selectedItem.price));
        JTextArea descriptionArea = new JTextArea(selectedItem.description, 3, 20);
        
        panel.add(new JLabel("Item Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Description:"));
        panel.add(new JScrollPane(descriptionArea));

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Item", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                double price = Double.parseDouble(priceField.getText());
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        String sql = "UPDATE items SET name = ?, price = ?, description = ? WHERE id = ?";
                        try (Connection conn = DBConnection.getConnection();
                             PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, nameField.getText());
                            stmt.setDouble(2, price);
                            stmt.setString(3, descriptionArea.getText());
                            stmt.setInt(4, selectedItem.id);
                            return stmt.executeUpdate() > 0;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                                    "Item updated successfully");
                                loadItems(shopIndex);
                            } else {
                                JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                                    "Failed to update item");
                            }
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this,
                                "Error updating item: " + e.getMessage());
                        }
                    }
                }.execute();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid price");
            }
        }
    }

    private void deleteItem() {
        int shopIndex = shopsList.getSelectedIndex();
        int itemIndex = itemsList.getSelectedIndex();
        
        if (shopIndex < 0 || itemIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select both a shop and an item");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this item?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Item itemToDelete = currentItems.get(itemIndex);
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    String sql = "DELETE FROM items WHERE id = ?";
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, itemToDelete.id);
                        return stmt.executeUpdate() > 0;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                                "Item deleted successfully");
                            loadItems(shopIndex);
                        } else {
                            JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                                "Failed to delete item");
                        }
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this,
                            "Error deleting item: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }

    private void viewReviews() {
        int shopIndex = shopsList.getSelectedIndex();
        if (shopIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a shop first");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "SELECT r.rating, r.comment, u.email, i.name as item_name, r.review_time " +
                                "FROM reviews r " +
                                "JOIN users u ON r.user_id = u.id " +
                                "JOIN items i ON r.order_id IN (SELECT order_id FROM order_items WHERE item_id = i.id) " +
                                "WHERE i.shop_id = ? " +
                                "ORDER BY r.review_time DESC";
                    
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, shops.get(shopIndex).id);
                    ResultSet rs = stmt.executeQuery();
                    
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append("Item: ").append(rs.getString("item_name")).append("\n")
                          .append("User: ").append(rs.getString("email")).append("\n")
                          .append("Rating: ").append(rs.getInt("rating")).append("/5\n")
                          .append("Comment: ").append(rs.getString("comment")).append("\n")
                          .append("Date: ").append(rs.getTimestamp("review_time")).append("\n\n");
                    }
                    
                    if (sb.length() == 0) {
                        sb.append("No reviews found for this shop's items");
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        JTextArea reviewsArea = new JTextArea(sb.toString());
                        reviewsArea.setEditable(false);
                        JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this, 
                            new JScrollPane(reviewsArea), 
                            "Reviews for " + shops.get(shopIndex).name, 
                            JOptionPane.INFORMATION_MESSAGE);
                    });
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RestaurantOwnerDashboard.this,
                        "Error loading reviews: " + e.getMessage());
                }
            }
        }.execute();
    }

    private static class Shop {
        final int id;
        final String name;
        final String category;

        Shop(int id, String name, String category) {
            this.id = id;
            this.name = name;
            this.category = category;
        }
    }

    private static class Item {
        final int id;
        final String name;
        final double price;
        final String description;

        Item(int id, String name, double price, String description) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.description = description;
        }
    }
}