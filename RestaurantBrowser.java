package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestaurantBrowser extends JFrame {
    private final int userId;
    private JList<String> restaurantList;
    private JList<String> dishesList;
    private DefaultListModel<String> restaurantModel;
    private DefaultListModel<String> dishesModel;
    private List<Restaurant> restaurants = new ArrayList<>();
    private List<Dish> dishes = new ArrayList<>(); // Added missing dishes list

    public RestaurantBrowser(int userId) {
        this.userId = userId;
        setTitle("Restaurant Browser");
        setSize(800, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setupUI();
        loadRestaurants();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Restaurant panel
        JPanel restaurantPanel = new JPanel(new BorderLayout());
        restaurantPanel.setBorder(BorderFactory.createTitledBorder("Restaurants"));
        restaurantModel = new DefaultListModel<>();
        restaurantList = new JList<>(restaurantModel);
        restaurantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadDishes(restaurantList.getSelectedIndex());
            }
        });
        restaurantPanel.add(new JScrollPane(restaurantList), BorderLayout.CENTER);

        // Dishes panel
        JPanel dishesPanel = new JPanel(new BorderLayout());
        dishesPanel.setBorder(BorderFactory.createTitledBorder("Dishes"));
        dishesModel = new DefaultListModel<>();
        dishesList = new JList<>(dishesModel);
        dishesPanel.add(new JScrollPane(dishesList), BorderLayout.CENTER);

        // Add to cart button
        JButton addToCartButton = new JButton("Add Selected to Cart");
        addToCartButton.addActionListener(e -> addToCart());

        // Main layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, restaurantPanel, dishesPanel);
        splitPane.setResizeWeight(0.3);
        add(splitPane, BorderLayout.CENTER);
        add(addToCartButton, BorderLayout.SOUTH);
    }

    private void loadRestaurants() {
        new SwingWorker<List<Restaurant>, Void>() {
            @Override
            protected List<Restaurant> doInBackground() throws Exception {
                List<Restaurant> results = new ArrayList<>();
                String sql = "SELECT id, name FROM shops";
                try (Connection conn = DBConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        results.add(new Restaurant(
                            rs.getInt("id"),
                            rs.getString("name")
                        ));
                    }
                }
                return results;
            }

            @Override
            protected void done() {
                try {
                    restaurants = get();
                    restaurantModel.clear();
                    restaurants.forEach(r -> restaurantModel.addElement(r.name));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RestaurantBrowser.this, 
                        "Error loading restaurants: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void loadDishes(int restaurantIndex) {
        if (restaurantIndex < 0 || restaurantIndex >= restaurants.size()) return;
        
        new SwingWorker<List<Dish>, Void>() {
            @Override
            protected List<Dish> doInBackground() throws Exception {
                List<Dish> results = new ArrayList<>();
                String sql = "SELECT id, name, price FROM items WHERE shop_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, restaurants.get(restaurantIndex).id);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        results.add(new Dish(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price")
                        ));
                    }
                }
                return results;
            }

            @Override
            protected void done() {
                try {
                    dishes = get(); // Store dishes list
                    dishesModel.clear();
                    dishes.forEach(d -> dishesModel.addElement(
                        String.format("%s - %.2f SR", d.name, d.price)
                    ));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(RestaurantBrowser.this,
                        "Error loading dishes: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void addToCart() {
        int dishIndex = dishesList.getSelectedIndex();
        if (dishIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a dish first");
            return;
        }

        String[] options = {"1", "2", "3", "4", "5"};
        String quantity = (String) JOptionPane.showInputDialog(
            this,
            "Select quantity:",
            "Add to Cart",
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        );

        if (quantity != null) {
            Dish selectedDish = dishes.get(dishIndex);
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO cart_items (user_id, item_id, quantity, price) VALUES (?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setInt(2, selectedDish.id);
                stmt.setInt(3, Integer.parseInt(quantity));
                stmt.setDouble(4, selectedDish.price);
                stmt.executeUpdate();
                
                JOptionPane.showMessageDialog(this, 
                    String.format("Added %s x%s to cart", selectedDish.name, quantity));
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error adding to cart: " + e.getMessage());
            }
        }
    }

    private static class Restaurant {
        final int id;
        final String name;

        Restaurant(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private static class Dish {
        final int id;
        final String name;
        final double price;

        Dish(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }
}