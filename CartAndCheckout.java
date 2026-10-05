package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartAndCheckout extends JFrame {
    private DefaultListModel<String> cartModel;
    private JList<String> cartList;
    private JLabel totalLabel;
    private final int userId;
    private List<CartItem> currentCartItems;

    public CartAndCheckout(int userId) {
        this.userId = userId;
        this.currentCartItems = new ArrayList<>();
        setTitle("Your Cart");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeComponents();
        loadCartItems();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        cartModel = new DefaultListModel<>();
        cartList = new JList<>(cartModel);
        add(new JScrollPane(cartList), BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedItem());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        totalLabel = new JLabel("Total: 0.00 SR");
        bottomPanel.add(totalLabel, BorderLayout.NORTH);

        JComboBox<String> paymentMethodBox = new JComboBox<>(new String[]{"card", "cash", "applePay"});
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkout());

        JPanel paymentPanel = new JPanel();
        paymentPanel.add(new JLabel("Payment Method:"));
        paymentPanel.add(paymentMethodBox);
        paymentPanel.add(checkoutButton);

        bottomPanel.add(paymentPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void deleteSelectedItem() {
        int selectedIndex = cartList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove this item from your cart?", "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            CartItem itemToDelete = currentCartItems.get(selectedIndex);
            deleteItemFromDatabase(itemToDelete);
        }
    }

    private void deleteItemFromDatabase(CartItem item) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String sql = "DELETE FROM cart_items WHERE user_id = ? AND item_id = " +
                             "(SELECT id FROM items WHERE name = ?)";
                
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    stmt.setString(2, item.name);
                    stmt.executeUpdate();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    loadCartItems();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CartAndCheckout.this,
                        "Error deleting item: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void loadCartItems() {
        new SwingWorker<List<CartItem>, Void>() {
            @Override
            protected List<CartItem> doInBackground() throws Exception {
                List<CartItem> items = new ArrayList<>();
                String sql = "SELECT i.name, c.quantity, c.price, i.id FROM cart_items c " +
                             "JOIN items i ON c.item_id = i.id WHERE c.user_id = ?";

                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        items.add(new CartItem(
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getInt("id")
                        ));
                    }
                }
                return items;
            }

            @Override
            protected void done() {
                try {
                    cartModel.clear();
                    currentCartItems.clear();
                    double total = 0;

                    for (CartItem item : get()) {
                        String itemText = String.format("%s x%d - %.2f SR",
                            item.name, item.quantity, item.price * item.quantity);
                        cartModel.addElement(itemText);
                        total += item.price * item.quantity;
                        currentCartItems.add(item);
                    }

                    totalLabel.setText(String.format("Total: %.2f SR", total));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CartAndCheckout.this,
                        "Error loading cart: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void checkout() {
        if (cartModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Add items before checkout.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            double total = Double.parseDouble(totalLabel.getText().replace("Total: ", "").replace(" SR", ""));
            
            String orderSql = "INSERT INTO orders (user_id, total) VALUES (?, ?)";
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setInt(1, userId);
            orderStmt.setDouble(2, total);
            orderStmt.executeUpdate();
            
            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = rs.next() ? rs.getInt(1) : -1;
            
            String itemsSql = "INSERT INTO order_items (order_id, item_id, quantity, item_price) " +
                             "SELECT ?, item_id, quantity, price FROM cart_items WHERE user_id = ?";
            PreparedStatement itemsStmt = conn.prepareStatement(itemsSql);
            itemsStmt.setInt(1, orderId);
            itemsStmt.setInt(2, userId);
            itemsStmt.executeUpdate();
            
            String clearCartSql = "DELETE FROM cart_items WHERE user_id = ?";
            PreparedStatement clearStmt = conn.prepareStatement(clearCartSql);
            clearStmt.setInt(1, userId);
            clearStmt.executeUpdate();
            
            conn.commit();
            
            JOptionPane.showMessageDialog(this, "Order #" + orderId + " placed successfully!");
            cartModel.clear();
            totalLabel.setText("Total: 0.00 SR");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + e.getMessage());
        }
    }

    private static class CartItem {
        final String name;
        final int quantity;
        final double price;
        final int itemId;

        CartItem(String name, int quantity, double price, int itemId) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
            this.itemId = itemId;
        }
    }
}