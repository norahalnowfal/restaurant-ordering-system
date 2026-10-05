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
```

## Main Class

The application starts from:

```text
com.mycompany.projectgul2.ProjectGUL2
```

## Database Connection

The project connects to a local MySQL database through `DBConnection.java`.

Default configuration used in the project:

```text
Database: app
Username: root
Password: 112233
```

> The database schema is not included in this repository, so the required tables must already exist locally for the application to run correctly.

## How to Run

1. Install **Java 22**.
2. Install and run **MySQL**.
3. Make sure the required database is available locally.
4. Open the project in an IDE such as NetBeans, IntelliJ IDEA, or VS Code.
5. Build the Maven project.
6. Run `ProjectGUL2.java`.

Using Maven:

```bash
mvn clean compile
mvn exec:java
```

## Author

**Norah Alnowfal**

Software Development
