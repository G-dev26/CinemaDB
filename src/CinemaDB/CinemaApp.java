package CinemaDB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CinemaApp {
    private static Connection connection = DatabaseConnection.getConnection();

    public static void main(String[] args) {
        if (connection == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        // Create the main application window
        JFrame frame = new JFrame("Cinema Reservation System");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Buttons for the main menu
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        JButton viewMoviesButton = new JButton("View Movies");
        JButton addBookingButton = new JButton("Add Booking");
        JButton viewBookingsButton = new JButton("View Bookings");
        JButton cancelBookingButton = new JButton("Cancel Booking");

        // Add buttons to panel
        panel.add(viewMoviesButton);
        panel.add(addBookingButton);
        panel.add(viewBookingsButton);
        panel.add(cancelBookingButton);

        frame.add(panel);
        frame.setVisible(true);

        // Button Actions
        viewMoviesButton.addActionListener(e -> showMoviesDialog(frame));
        addBookingButton.addActionListener(e -> showAddBookingDialog(frame));
        viewBookingsButton.addActionListener(e -> showBookingsDialog(frame));
        cancelBookingButton.addActionListener(e -> showCancelBookingDialog(frame));
    }

    private static void showMoviesDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Available Movies", true);
        dialog.setSize(500, 400);
        JTextArea moviesArea = new JTextArea();
        moviesArea.setEditable(false);

        try {
            String sql = "SELECT * FROM Movies";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            StringBuilder moviesText = new StringBuilder();
            while (resultSet.next()) {
                moviesText.append(String.format("%d: %s (%s) - %d min, Released: %s\n",
                        resultSet.getInt("movie_id"),
                        resultSet.getString("title"),
                        resultSet.getString("genre"),
                        resultSet.getInt("duration_minutes"),
                        resultSet.getDate("release_date")));
            }
            moviesArea.setText(moviesText.toString());
        } catch (SQLException e) {
            moviesArea.setText("Error retrieving movies: " + e.getMessage());
        }

        dialog.add(new JScrollPane(moviesArea));
        dialog.setVisible(true);
    }

    private static void showAddBookingDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Add Booking", true);
        dialog.setSize(400, 500);
        dialog.setLayout(new BorderLayout(10, 10));

        // Display available movies
        JTextArea moviesArea = new JTextArea();
        moviesArea.setEditable(false);
        moviesArea.setBorder(BorderFactory.createTitledBorder("Available Movies"));

        try {
            String sql = "SELECT * FROM Movies";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            StringBuilder moviesText = new StringBuilder();
            while (resultSet.next()) {
                moviesText.append(String.format("%d: %s (%s) - %d min, Released: %s\n",
                        resultSet.getInt("movie_id"),
                        resultSet.getString("title"),
                        resultSet.getString("genre"),
                        resultSet.getInt("duration_minutes"),
                        resultSet.getDate("release_date")));
            }
            moviesArea.setText(moviesText.toString());
        } catch (SQLException e) {
            moviesArea.setText("Error retrieving movies: " + e.getMessage());
        }

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField();
        JLabel movieIdLabel = new JLabel("Movie ID:");
        JTextField movieIdField = new JTextField();
        JLabel seatsLabel = new JLabel("Seats to Book:");
        JTextField seatsField = new JTextField();

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(movieIdLabel);
        inputPanel.add(movieIdField);
        inputPanel.add(seatsLabel);
        inputPanel.add(seatsField);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components
        dialog.add(new JScrollPane(moviesArea), BorderLayout.NORTH);
        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Save button logic
        saveButton.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            String movieIdText = movieIdField.getText().trim();
            String seatsText = seatsField.getText().trim();

            if (fullName.isEmpty() || movieIdText.isEmpty() || seatsText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled out!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!fullName.matches("^[a-zA-Z\\s]+$")) {
                JOptionPane.showMessageDialog(dialog, "Full name must contain only letters and spaces.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int movieId = Integer.parseInt(movieIdText);
                int seats = Integer.parseInt(seatsText);

                String sql = "INSERT INTO Bookings (movie_id, customer_name, seats_booked) VALUES (?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, movieId);
                statement.setString(2, fullName);
                statement.setInt(3, seats);

                statement.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Booking added successfully!");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Movie ID and seats must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private static void showBookingsDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Bookings", true);
        dialog.setSize(500, 400);
        JTextArea bookingsArea = new JTextArea();
        bookingsArea.setEditable(false);

        try {
            String sql = "SELECT b.booking_id, b.customer_name, m.title, b.seats_booked " +
                         "FROM Bookings b JOIN Movies m ON b.movie_id = m.movie_id";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            StringBuilder bookingsText = new StringBuilder();
            while (resultSet.next()) {
                bookingsText.append(String.format("%d: %s booked %s (%d seats)\n",
                        resultSet.getInt("booking_id"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("title"),
                        resultSet.getInt("seats_booked")));
            }
            bookingsArea.setText(bookingsText.toString());
        } catch (SQLException e) {
            bookingsArea.setText("Error retrieving bookings: " + e.getMessage());
        }

        dialog.add(new JScrollPane(bookingsArea));
        dialog.setVisible(true);
    }

    private static void showCancelBookingDialog(JFrame parent) {
        String bookingIdText = JOptionPane.showInputDialog(parent, "Enter Booking ID to cancel:");
        if (bookingIdText != null && !bookingIdText.trim().isEmpty()) {
            try {
                int bookingId = Integer.parseInt(bookingIdText.trim());
                String sql = "DELETE FROM Bookings WHERE booking_id = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, bookingId);

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(parent, "Booking cancelled successfully!");
                } else {
                    JOptionPane.showMessageDialog(parent, "No booking found with ID: " + bookingId, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(parent, "Booking ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(parent, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Booking ID cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
