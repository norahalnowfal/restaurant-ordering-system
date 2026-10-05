# Restaurant Ordering System

A Java desktop application for restaurant ordering and management, built with **Java Swing**, **MySQL**, and **Maven**.

The system supports three user roles:

- **Customer**
- **Restaurant Owner**
- **Admin**

## Features

### Customer

- Create an account and log in
- Browse restaurants
- Add items to cart
- Checkout and place orders
- Track orders
- Submit feedback and reviews

### Restaurant Owner

- View owned restaurants
- Manage restaurant menu items
- Add, edit, and delete items
- View customer reviews

### Admin

- Manage restaurants
- Manage users
- View reports

## Technologies Used

- Java 22
- Java Swing
- MySQL
- JDBC
- Maven
- MySQL Connector/J 8.0.33

## Project Structure

```text
src/main/java/com/mycompany/projectgul2/
├── AdminDashboard.java
├── CartAndCheckout.java
├── CartManager.java
├── CustomerDashboard.java
├── DBConnection.java
├── FeedbackFrame.java
├── OrderTracking.java
├── ProjectGUL2.java
├── RestaurantBrowser.java
├── RestaurantOwnerDashboard.java
├── SignUp.java
├── User.java
└── ViewReviewsFrame.java
