package CinemaDB;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/CinemaDB";
    private static final String USER = "root";
    private static final String PASSWORD = "newpassword";  // Update with the correct password

    public static Connection getConnection() {
        try {
            // Load the MySQL JDBC driver (this may be unnecessary if you are using a recent version of MySQL Connector)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. Make sure the MySQL Connector/J is added to the build path.");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
