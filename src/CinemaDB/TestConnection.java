package CinemaDB;
import java.sql.*;

import java.sql.*;

public class TestConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/CinemaDB";  // Update if needed
        String user = "root";  // Default MySQL root user
        String password = "";  // Default password for root (empty string if none set)

        try {
            // Load the MySQL JDBC driver (not always necessary with newer versions)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Try to establish the connection
            Connection conn = DriverManager.getConnection(url, user, password);
            if (conn != null) {
                System.out.println("Connection established!");
            } else {
                System.out.println("Failed to connect!");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("JDBC Driver not found. Please add it to your classpath.");
            e.printStackTrace();
        }
    }
}
