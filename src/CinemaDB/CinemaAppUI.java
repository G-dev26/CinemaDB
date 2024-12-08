package CinemaDB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CinemaAppUI {
    private static Connection connection = DatabaseConnection.getConnection();

    public static void main(String[] args) {
        if (connection == null) {
            JOptionPane.showMessageDialog(null, "Failed to connect to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SwingUtilities.invokeLater(CinemaAppUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Cinema Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(6, 1, 5, 5));

        JLabel titleLabel = new JLabel("Cinema Reservation System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(titleLabel);

        JButton viewMoviesButton = new JButton("View Movies");
        JButton addBookingButton = new JButton("Add Booking");
        JButton cancelBookingButton = new JButton("Cancel Booking");
        JButton viewBookingsButton = new JButton("View Bookings");
        JButton exitButton = new JButton("Exit");

        frame.add(viewMoviesButton);
        frame.add(addBookingButton);
        frame.add(cancelBookingButton);
        frame.add(viewBookingsButton);
        frame.add(exitButton);

        // Button actions
        viewMoviesButton.addActionListener(e -> showViewMoviesDialog(frame));
        addBookingButton.addActionListener(e -> showAddBookingDialog(frame));
        cancelBookingButton.addActionListener(e -> showCancelBookingDialog(frame));
        viewBookingsButton.addActionListener(e -> showBookingsDialog(frame));
        exitButton.addActionListener(e -> System.exit(0));

        frame.setVisible(true);
    }

    private static void showViewMoviesDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Available Movies", true);
        dialog.setSize(400, 300);

        JTextArea moviesArea = new JTextArea();
        moviesArea.setEditable(false);

        try {
            String sql = "SELECT * FROM Movies";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            StringBuilder moviesText = new StringBuilder("Available Movies:\n");
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
        dialog.setSize(600, 600);  // Adjust size for more information
        dialog.setLayout(new BorderLayout(10, 10));

        // Display available movies with showtimes and available seats
        JTextArea moviesArea = new JTextArea();
        moviesArea.setEditable(false);
        moviesArea.setBorder(BorderFactory.createTitledBorder("Available Movies and Showtimes"));

        // Create input panel for user data
        JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField();
        JLabel phoneLabel = new JLabel("Phone Number:");
        JTextField phoneField = new JTextField();
        JLabel movieIdLabel = new JLabel("Movie ID:");
        JTextField movieIdField = new JTextField();
        JLabel showtimeLabel = new JLabel("Showtime ID:");
        JTextField showtimeIdField = new JTextField();  // For automatically filling in Showtime ID
        JLabel seatsLabel = new JLabel("Seats to Book:");
        JTextField seatsField = new JTextField();

        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(phoneLabel);
        inputPanel.add(phoneField);
        inputPanel.add(movieIdLabel);
        inputPanel.add(movieIdField);
        inputPanel.add(showtimeLabel);
        inputPanel.add(showtimeIdField);  // Display Showtime ID (auto-generated)
        inputPanel.add(seatsLabel);
        inputPanel.add(seatsField);

        // Add Save and Cancel buttons
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add components to the dialog
        dialog.add(new JScrollPane(moviesArea), BorderLayout.NORTH);
        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Populate available movies and showtimes
        try {
            String movieQuery = "SELECT m.movie_id, m.title, s.showtime_id, s.show_time, " +
                                "IFNULL(s.available_seats, 150) AS available_seats " +
                                "FROM movies m " +
                                "LEFT JOIN showtimes s ON m.movie_id = s.movie_id " +
                                "ORDER BY m.movie_id, s.show_time"; // Organize by movie_id and show_time
            Statement movieStatement = connection.createStatement();
            ResultSet movieResultSet = movieStatement.executeQuery(movieQuery);

            StringBuilder moviesText = new StringBuilder();
            while (movieResultSet.next()) {
                int movieId = movieResultSet.getInt("movie_id");
                String title = movieResultSet.getString("title");
                int showtimeId = movieResultSet.getInt("showtime_id");
                String showTime = movieResultSet.getString("show_time");
                int availableSeats = movieResultSet.getInt("available_seats");

                String movieInfo = String.format("Movie ID: %d, Title: %s, Showtime: %s, Available Seats: %d\n",
                        movieId, title, showTime, availableSeats);
                moviesText.append(movieInfo);
            }
            moviesArea.setText(moviesText.toString());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error retrieving movie/showtime information: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Add save action
        saveButton.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            String phoneNumber = phoneField.getText().trim();
            String movieIdText = movieIdField.getText().trim();
            String seatsText = seatsField.getText().trim();

            // Check for empty fields
            if (fullName.isEmpty() || phoneNumber.isEmpty() || movieIdText.isEmpty() || seatsText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled out!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Full Name (only letters and spaces allowed)
            if (!fullName.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(dialog, "Full name should only contain letters and spaces.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Phone Number (only digits and optional '+' sign allowed)
            if (!phoneNumber.matches("[+0-9]+")) {
                JOptionPane.showMessageDialog(dialog, "Phone number should only contain digits", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Movie ID (only digits allowed)
            if (!movieIdText.matches("[0-9]+")) {
                JOptionPane.showMessageDialog(dialog, "Movie ID should only contain digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Movie ID range (1-5)
            int movieId = Integer.parseInt(movieIdText);
            if (movieId < 1 || movieId > 5) {
                JOptionPane.showMessageDialog(dialog, "Movie ID must be between 1 and 5.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // The Showtime ID is automatically assigned the same value as the Movie ID
            String showtimeIdText = String.valueOf(movieId);
            showtimeIdField.setText(showtimeIdText);

            // Validate seats: Should be a positive number
            if (seatsText.isEmpty() || !seatsText.matches("\\d+") || Integer.parseInt(seatsText) <= 0) {
                JOptionPane.showMessageDialog(dialog, "Seats to book should be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check available seats for the chosen showtime
            String checkSeatsSql = "SELECT available_seats FROM showtimes WHERE showtime_id = ? AND available_seats >= ?";
            try {
                PreparedStatement checkSeatsStmt = connection.prepareStatement(checkSeatsSql);
                checkSeatsStmt.setInt(1, movieId); // Use the same ID for showtime
                checkSeatsStmt.setInt(2, Integer.parseInt(seatsText));
                ResultSet checkResult = checkSeatsStmt.executeQuery();

                if (!checkResult.next()) {
                    JOptionPane.showMessageDialog(dialog, "Not enough available seats for this showtime. Please choose another one.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Proceed with booking
                String insertBookingSql = "INSERT INTO bookings (movie_id, showtime_id, full_name, phone_number, seats_booked) " +
                                          "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertBookingStmt = connection.prepareStatement(insertBookingSql);
                insertBookingStmt.setInt(1, movieId);
                insertBookingStmt.setInt(2, movieId); // Use the same ID for showtime
                insertBookingStmt.setString(3, fullName);
                insertBookingStmt.setString(4, phoneNumber);
                insertBookingStmt.setInt(5, Integer.parseInt(seatsText));
                insertBookingStmt.executeUpdate();

                // Update available seats
                String updateSeatsSql = "UPDATE showtimes SET available_seats = available_seats - ? WHERE showtime_id = ?";
                PreparedStatement updateSeatsStmt = connection.prepareStatement(updateSeatsSql);
                updateSeatsStmt.setInt(1, Integer.parseInt(seatsText));
                updateSeatsStmt.setInt(2, movieId);
                updateSeatsStmt.executeUpdate();

                JOptionPane.showMessageDialog(dialog, "Booking successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error processing booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
    
    private static void resetBookingIds() throws SQLException {
        // Reset the AUTO_INCREMENT to 1
        String resetQuery = "ALTER TABLE bookings AUTO_INCREMENT = 1";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(resetQuery);
        System.out.println("Booking IDs have been reset to 1.");
    }

    private static void showCancelBookingDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Cancel Booking", true);
        dialog.setSize(300, 150);
        dialog.setLayout(new GridLayout(3, 1, 5, 5));

        JLabel bookingIdLabel = new JLabel("Booking ID to Cancel:");
        JTextField bookingIdField = new JTextField();
        JButton cancelButton = new JButton("Cancel Booking");

        dialog.add(bookingIdLabel);
        dialog.add(bookingIdField);
        dialog.add(cancelButton);

        cancelButton.addActionListener(e -> {
            String bookingIdText = bookingIdField.getText().trim();

            if (bookingIdText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a booking ID!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int bookingId = Integer.parseInt(bookingIdText);

                // Retrieve the booking information
                String selectBookingSql = "SELECT showtime_id, seats_booked FROM bookings WHERE booking_id = ?";
                PreparedStatement selectBookingStmt = connection.prepareStatement(selectBookingSql);
                selectBookingStmt.setInt(1, bookingId);
                ResultSet bookingResult = selectBookingStmt.executeQuery();

                if (!bookingResult.next()) {
                    JOptionPane.showMessageDialog(dialog, "No booking found with the given ID!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int showtimeId = bookingResult.getInt("showtime_id");
                int seatsBooked = bookingResult.getInt("seats_booked");

                // Delete the booking
                String deleteBookingSql = "DELETE FROM bookings WHERE booking_id = ?";
                PreparedStatement deleteBookingStmt = connection.prepareStatement(deleteBookingSql);
                deleteBookingStmt.setInt(1, bookingId);
                deleteBookingStmt.executeUpdate();

                // Update available seats
                String updateSeatsSql = "UPDATE showtimes SET available_seats = available_seats + ? WHERE showtime_id = ?";
                PreparedStatement updateSeatsStmt = connection.prepareStatement(updateSeatsSql);
                updateSeatsStmt.setInt(1, seatsBooked);
                updateSeatsStmt.setInt(2, showtimeId);
                updateSeatsStmt.executeUpdate();

                // Reset the booking IDs to 1
                resetBookingIds();  // Reset booking IDs after cancellation

                JOptionPane.showMessageDialog(dialog, "Booking cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Error cancelling booking: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }


    private static void showBookingsDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "View Bookings", true);
        dialog.setSize(600, 400);  // Adjust size for better visibility
        dialog.setLayout(new BorderLayout(10, 10));

        // Define column names for the table
        String[] columnNames = {"Booking ID", "Movie ID", "Showtime ID", "Name", "Seats"};

        // Create an empty 2D Object array to hold the data for the table
        Object[][] data = new Object[0][5]; // No data initially, will be populated later

        // Create JTable with the columns and data
        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);  // Ensure the table fills the entire viewport

        // Adjust column widths for better readability
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // Booking ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Movie ID
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Showtime ID
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Name
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Seats

        // Create JScrollPane to make the table scrollable
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Fetch the current bookings from the database
        try {
            // Create a statement that supports scrollable result sets
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            String query = "SELECT b.booking_id, b.movie_id, b.showtime_id, b.full_name, b.seats_booked " +
                           "FROM bookings b " +
                           "ORDER BY b.booking_id";  // Sorting by booking ID
            ResultSet rs = stmt.executeQuery(query);

            // Count how many rows to create dynamically
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }

            // Create a new 2D array for data to store the fetched rows
            Object[][] rows = new Object[rowCount][5];
            int rowIndex = 0;
            rs.beforeFirst(); // Reset the result set pointer to the beginning
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                int movieId = rs.getInt("movie_id");
                int showtimeId = rs.getInt("showtime_id");
                String name = rs.getString("full_name");
                int seatsBooked = rs.getInt("seats_booked");

                // Manually populate the data into the 2D array
                rows[rowIndex] = new Object[]{bookingId, movieId, showtimeId, name, seatsBooked};
                rowIndex++;
            }

            // Create a new table with the populated data
            JTable populatedTable = new JTable(rows, columnNames);
            populatedTable.setFillsViewportHeight(true);  // Ensure the table fills the entire viewport

            // Adjust column widths for better readability
            populatedTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Booking ID
            populatedTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Movie ID
            populatedTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Showtime ID
            populatedTable.getColumnModel().getColumn(3).setPreferredWidth(200); // Name
            populatedTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Seats

            // Set the newly populated table in the scroll pane
            JScrollPane populatedScrollPane = new JScrollPane(populatedTable);
            dialog.add(populatedScrollPane, BorderLayout.CENTER);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(dialog, "Error retrieving bookings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Add a close button to close the dialog
        JPanel buttonPanel = new JPanel();
        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}
