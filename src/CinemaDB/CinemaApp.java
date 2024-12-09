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
        
        // Center the JFrame on the screen
        frame.setLocationRelativeTo(null);

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
        dialog.setLocationRelativeTo(parent); // Center the dialog relative to parent
        dialog.setVisible(true);
    }

    private static void showAddBookingDialog(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Add Booking", true);
        dialog.setSize(400, 500); // Adjusted size for better fit

       // Input panel for user data
       JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));

       JLabel nameLabel = new JLabel("Full Name:");
       JTextField nameField = new JTextField();

       JLabel phoneLabel = new JLabel("Phone Number:");
       JTextField phoneField = new JTextField();

       JLabel movieIdLabel = new JLabel("Movie ID:");
       JTextField movieIdField = new JTextField();

       JLabel showtimeIdLabel = new JLabel("Showtime ID:");
       JTextField showtimeIdField = new JTextField();

       JLabel seatsLabel = new JLabel("Seats to Book:");
       JTextField seatsField = new JTextField();

       inputPanel.add(nameLabel);
       inputPanel.add(nameField);
       inputPanel.add(phoneLabel);
       inputPanel.add(phoneField);
       inputPanel.add(movieIdLabel);
       inputPanel.add(movieIdField);

       inputPanel.add(showtimeIdLabel);
       inputPanel.add(showtimeIdField);

       inputPanel.add(seatsLabel);
       inputPanel.add(seatsField);

       // Buttons for Save and Cancel
       JPanel buttonPanel = new JPanel();
       
       JButton saveButton = new JButton("Save");
       JButton cancelButton = new JButton("Cancel");
       
       buttonPanel.add(saveButton);
       buttonPanel.add(cancelButton);

       dialog.setLayout(new BorderLayout());
       
       dialog.add(inputPanel, BorderLayout.CENTER);
       dialog.add(buttonPanel, BorderLayout.SOUTH);

       // Populate available movies and showtimes
       try {
           String movieQuery = "SELECT m.movie_id, m.title, s.showtime_id, s.show_time, " +
                   "IFNULL(s.available_seats, 150) AS available_seats " +
                   "FROM Movies m " +
                   "LEFT JOIN Showtimes s ON m.movie_id = s.movie_id " +
                   "ORDER BY m.movie_id, s.show_time";
           Statement movieStatement = connection.createStatement();
           ResultSet movieResultSet = movieStatement.executeQuery(movieQuery);

           StringBuilder moviesText = new StringBuilder();
           while (movieResultSet.next()) {
               int movieId = movieResultSet.getInt("movie_id");
               String title = movieResultSet.getString("title");
               int showtimeId = movieResultSet.getInt("showtime_id");
               String showTime = movieResultSet.getString("show_time");
               int availableSeats = movieResultSet.getInt("available_seats");

               String movieInfo = String.format("Movie ID: %d, Title: %s, Showtime ID: %d, Showtime: %s, Available Seats: %d\n",
                       movieId, title, showtimeId, showTime, availableSeats);
               moviesText.append(movieInfo);
           }
           // Display available movies in a text area or other component as needed.
           JTextArea moviesArea = new JTextArea(moviesText.toString());
           moviesArea.setEditable(false); 
           dialog.add(new JScrollPane(moviesArea), BorderLayout.NORTH); // Add to dialog
           
           // Add action listener to Movie ID field
           movieIdField.addActionListener(e -> {
               String selectedMovieIdText = movieIdField.getText().trim();
               if (!selectedMovieIdText.isEmpty()) {
                   int selectedMovieId;
                   try {
                       selectedMovieId = Integer.parseInt(selectedMovieIdText);
                       updateShowtimeOptions(selectedMovieId, showtimeIdField); // Update options based on selected Movie ID
                   } catch (NumberFormatException ex) {
                       JOptionPane.showMessageDialog(dialog,"Invalid Movie ID. Please enter a number.","Error",JOptionPane.ERROR_MESSAGE); 
                   }
               }
           });

       } catch (SQLException e) {
           JOptionPane.showMessageDialog(dialog,"Error retrieving movie/showtime information: "+ e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); 
       }

   // Save button logic
   saveButton.addActionListener(e -> {
       String fullName = nameField.getText().trim();
       String phoneNumber = phoneField.getText().trim();
       String movieIdText = movieIdField.getText().trim();
       String showtimeIdText=showtimeIdField.getText().trim(); 
       String seatsText=seatsField.getText().trim();

       if (fullName.isEmpty() || phoneNumber.isEmpty() || movieIdText.isEmpty() || seatsText.isEmpty() || showtimeIdText.isEmpty()) {
           JOptionPane.showMessageDialog(dialog,"All fields must be filled out!","Error",JOptionPane.ERROR_MESSAGE); 
           return; 
       }

       if (!fullName.matches("[a-zA-Z ]+")) { 
           JOptionPane.showMessageDialog(dialog,"Full name should only contain letters and spaces.","Error",JOptionPane.ERROR_MESSAGE); 
           return; 
       }

       if (!phoneNumber.matches("[+0-9]+")) { 
           JOptionPane.showMessageDialog(dialog,"Phone number should only contain digits and optional '+' sign.","Error",JOptionPane.ERROR_MESSAGE); 
           return; 
       }

      try { 
          int movieId=Integer.parseInt(movieIdText); 
          int showtimeId=Integer.parseInt(showtimeIdText); 
          int seats=Integer.parseInt(seatsText); 

          // Check if enough seats are available for the selected showtime
          String checkSeatsSql="SELECT available_seats FROM Showtimes WHERE showtime_id=? AND available_seats>=?"; 
          PreparedStatement checkSeatsStmt=connection.prepareStatement(checkSeatsSql); 
          checkSeatsStmt.setInt(1,showtimeId); 
          checkSeatsStmt.setInt(2,seats); 

          ResultSet checkResultSet=checkSeatsStmt.executeQuery(); 
          if (!checkResultSet.next()) { 
              JOptionPane.showMessageDialog(dialog,"Not enough available seats for this showtime.","Error",JOptionPane.ERROR_MESSAGE); 
              return; 
          }

         // Proceed with booking
         String insertSql="INSERT INTO Bookings (movie_id, showtime_id, customer_name ,phone_number ,seats_booked) VALUES (?, ?, ?, ?, ?)"; 
         PreparedStatement insertStmt=connection.prepareStatement(insertSql); 
         insertStmt.setInt(1,movieId); 
         insertStmt.setInt(2 ,showtimeId); 
         insertStmt.setString(3 ,fullName); 
         insertStmt.setString(4 ,phoneNumber); 
         insertStmt.setInt(5 ,seats); 

         insertStmt.executeUpdate(); 

         JOptionPane.showMessageDialog(dialog,"Booking added successfully!"); 
         dialog.dispose(); 

     } catch (NumberFormatException ex) { 
         JOptionPane.showMessageDialog(dialog,"Movie ID and seats must be valid numbers.","Error",JOptionPane.ERROR_MESSAGE); 
     } catch (SQLException ex) { 
         JOptionPane.showMessageDialog(dialog,"Error adding booking: "+ ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); 
     } 
   });

   cancelButton.addActionListener(e -> dialog.dispose());
   dialog.setLocationRelativeTo(parent); // Center the dialog relative to parent
   dialog.setVisible(true);
}

// Method to update the Showtime ID based on selected Movie ID
private static void updateShowtimeOptions(int movieId,JTextField showtimeIdField) { 
   try { 
      String queryShowtimes="SELECT showtime_id FROM Showtimes WHERE movie_id=?"; 
      PreparedStatement pstmt=connection.prepareStatement(queryShowtimes); 
      pstmt.setInt(1,movieId); 

      ResultSet rsShowtimes=pstmt.executeQuery(); 

      StringBuilder optionsBuilder=new StringBuilder(); 

      while (rsShowtimes.next()) { 
         int showtimeId=rsShowtimes.getInt("showtime_id"); 
         optionsBuilder.append(showtimeId).append(", "); // Collecting all valid IDs
      } 

      if (optionsBuilder.length()>0) { 
         optionsBuilder.setLength(optionsBuilder.length()-2); // Remove last comma and space
         showtimeIdField.setText(optionsBuilder.toString()); // Display available Showtime IDs
      } else { 
         showtimeIdField.setText(""); // Clear if no showtimes found
         JOptionPane.showMessageDialog(null,"No available showtimes for this Movie ID.","Info",JOptionPane.INFORMATION_MESSAGE);  
      } 

   } catch (SQLException e) { 
      JOptionPane.showMessageDialog(null,"Error retrieving showtimes: "+ e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);  
   }  
}

private static void showBookingsDialog(JFrame parent) { 
   JDialog dialog=new JDialog(parent,"Bookings",true); 
   dialog.setSize(500,400);

   JTextArea bookingsArea=new JTextArea(); bookingsArea.setEditable(false);

   try { 
      String sql="SELECT b.booking_id,b.customer_name,m.title,b.seats_booked FROM Bookings b JOIN Movies m ON b.movie_id=m.movie_id"; Statement statement=connection.createStatement(); ResultSet resultSet=statement.executeQuery(sql);

      StringBuilder bookingsText=new StringBuilder(); while(resultSet.next()){ bookingsText.append(String.format("%d: %s booked %s (%d seats)\n",
              resultSet.getInt("booking_id"),
              resultSet.getString("customer_name"),
              resultSet.getString("title"),
              resultSet.getInt("seats_booked")));
      }
      bookingsArea.setText(bookingsText.toString());
   } catch (SQLException e) {
      bookingsArea.setText("Error retrieving bookings: "+ e.getMessage());
   }

   dialog.add(new JScrollPane(bookingsArea));
   dialog.setLocationRelativeTo(parent); // Center the dialog relative to parent
   dialog.setVisible(true);
}

private static void showCancelBookingDialog(JFrame parent) {  
   String bookingIdText=JOptionPane.showInputDialog(parent,"Enter Booking ID to cancel:");
   if (bookingIdText != null && !bookingIdText.trim().isEmpty()) {  
      try {  
         int bookingId=Integer.parseInt(bookingIdText.trim());  
         String sql="DELETE FROM Bookings WHERE booking_id=?";  
         PreparedStatement statement=connection.prepareStatement(sql);  
         statement.setInt(1,bookingId);

         int rowsAffected=statement.executeUpdate();  
         if(rowsAffected>0){  
            JOptionPane.showMessageDialog(parent,"Booking cancelled successfully!");  
         } else{  
            JOptionPane.showMessageDialog(parent,"No booking found with ID: "+ bookingId,"Error",JOptionPane.ERROR_MESSAGE);  
         }  
      } catch(NumberFormatException ex){  
         JOptionPane.showMessageDialog(parent,"Booking ID must be a number.","Error",JOptionPane.ERROR_MESSAGE);  
      } catch(SQLException ex){  
         JOptionPane.showMessageDialog(parent,"Error cancelling booking: "+ ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);  
      }  
   } else{  
      JOptionPane.showMessageDialog(parent,"Booking ID cannot be empty.","Error",JOptionPane.ERROR_MESSAGE);  
   }  
}
}