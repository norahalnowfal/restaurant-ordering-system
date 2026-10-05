package com.mycompany.projectgul2;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderTracking extends JFrame {
    private final DefaultListModel<String> orderModel = new DefaultListModel<>();
    private final JList<String> orderList = new JList<>(orderModel);
    private final int userId;

    public OrderTracking(int userId) {
        this.userId = userId;
        setTitle("Order Tracking");
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI();
        loadOrders();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        
        // Order list with title
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Your Orders"));
        listPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);
        add(listPanel, BorderLayout.CENTER);
        
        // Double-click listener for order details
        orderList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    showOrderDetails();
                }
            }
        });
    }

    private void loadOrders() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                List<String> orders = new ArrayList<>();
                try (Connection conn = DBConnection.getConnection()) {
                    String sql = "SELECT order_id, total FROM orders WHERE user_id = ?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, userId);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        orders.add(String.format("Order #%d - Total: %.2f SR", 
                                rs.getInt("order_id"), 
                                rs.getDouble("total")));
                    }
                }
                return orders;
            }

            @Override
            protected void done() {
                try {
                    orderModel.clear();
                    for (String order : get()) {
                        orderModel.addElement(order);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OrderTracking.this,
                            "Error loading orders: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void showOrderDetails() {
        String selected = orderList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an order first");
            return;
        }

        int orderId = Integer.parseInt(selected.split("#")[1].split(" - ")[0].trim());
        
        new SwingWorker<OrderDetails, Void>() {
            @Override
            protected OrderDetails doInBackground() throws Exception {
                try (Connection conn = DBConnection.getConnection()) {
                    // Get basic order info
                    String orderSql = "SELECT total FROM orders WHERE order_id = ?";
                    PreparedStatement orderStmt = conn.prepareStatement(orderSql);
                    orderStmt.setInt(1, orderId);
                    ResultSet orderRs = orderStmt.executeQuery();

                    if (!orderRs.next()) return null;

                    double total = orderRs.getDouble("total");
                    
                    // Get order items using items.price (fallback if order_items.price doesn't exist)
                    String itemsSql = "SELECT i.name, oi.quantity, COALESCE(oi.item_price, i.price) as price " +
                                     "FROM order_items oi JOIN items i ON oi.item_id = i.id " +
                                     "WHERE oi.order_id = ?";
                    PreparedStatement itemsStmt = conn.prepareStatement(itemsSql);
                    itemsStmt.setInt(1, orderId);
                    ResultSet itemsRs = itemsStmt.executeQuery();

                    List<OrderItem> items = new ArrayList<>();
                    while (itemsRs.next()) {
                        items.add(new OrderItem(
                            itemsRs.getString("name"),
                            itemsRs.getInt("quantity"),
                            itemsRs.getDouble("price")
                        ));
                    }

                    return new OrderDetails(orderId, "Completed", total, new Timestamp(System.currentTimeMillis()), items);
                }
            }

            @Override
            protected void done() {
                try {
                    OrderDetails details = get();
                    if (details != null) {
                        new OrderDetailsFrame(details).setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(OrderTracking.this,
                                "Order not found",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(OrderTracking.this,
                            "Error loading order details: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // Data holder classes
    private static class OrderDetails {
        final int orderId;
        final String status;
        final double total;
        final Timestamp orderDate;
        final List<OrderItem> items;

        OrderDetails(int orderId, String status, double total, Timestamp orderDate, List<OrderItem> items) {
            this.orderId = orderId;
            this.status = status;
            this.total = total;
            this.orderDate = orderDate;
            this.items = items;
        }
    }

    private static class OrderItem {
        final String name;
        final int quantity;
        final double price;

        OrderItem(String name, int quantity, double price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }

    // Order details window
    private static class OrderDetailsFrame extends JFrame {
        public OrderDetailsFrame(OrderDetails details) {
            setTitle("Order Details #" + details.orderId);
            setSize(500, 400);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setupUI(details);
        }

        private void setupUI(OrderDetails details) {
            setLayout(new BorderLayout(10, 10));

            // Order info panel
            JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
            infoPanel.setBorder(BorderFactory.createTitledBorder("Order Information"));
            
            infoPanel.add(new JLabel("Order ID:"));
            infoPanel.add(new JLabel(String.valueOf(details.orderId)));
            infoPanel.add(new JLabel("Status:"));
            infoPanel.add(new JLabel(details.status));
            infoPanel.add(new JLabel("Date:"));
            infoPanel.add(new JLabel(details.orderDate.toString()));
            infoPanel.add(new JLabel("Total:"));
            infoPanel.add(new JLabel(String.format("%.2f SR", details.total)));
            
            add(infoPanel, BorderLayout.NORTH);

            // Items list
            DefaultListModel<String> itemsModel = new DefaultListModel<>();
            for (OrderItem item : details.items) {
                itemsModel.addElement(String.format("%-30s x%-3d %6.2f SR", 
                    item.name, item.quantity, item.price * item.quantity));
            }
            
            JList<String> itemsList = new JList<>(itemsModel);
            itemsList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(itemsList);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));
            add(scrollPane, BorderLayout.CENTER);

            // Close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            add(closeButton, BorderLayout.SOUTH);
        }
    }
}