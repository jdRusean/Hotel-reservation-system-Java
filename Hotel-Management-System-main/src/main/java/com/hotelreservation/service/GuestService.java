package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Guest;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service for managing guest operations.
 */
public class GuestService {
    private static final Logger logger = LoggerFactory.getLogger(GuestService.class);

    /**
     * Retrieves all guests from the database.
     *
     * @return List of guests.
     */
    public List<Guest> getAllGuests() {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM Guests";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Guest guest = new Guest(
                        rs.getString("guestId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("middleName"),
                        rs.getString("password"),
                        rs.getString("contactNumber")
                );
                guests.add(guest);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving guests", e);
        }

        return guests;
    }

    /**
     * Adds a new guest to the database.
     *
     * @param guest The guest to add
     * @return true if successful, false otherwise
     */
    public boolean addGuest(Guest guest) {
        String sql = "INSERT INTO Guests (guestId, firstName, lastName, middleName, password, contactNumber) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Generate a new UUID for the guest if not provided
            if (guest.getGuestId() == null || guest.getGuestId().isEmpty()) {
                guest.setGuestId(UUID.randomUUID().toString());
            }

            pstmt.setString(1, guest.getGuestId());
            pstmt.setString(2, guest.getFirstName());
            pstmt.setString(3, guest.getLastName());
            pstmt.setString(4, guest.getMiddleName());
            pstmt.setString(5, guest.getPassword());
            pstmt.setString(6, guest.getContactNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding guest", e);
            return false;
        }
    }

    /**
     * Updates guest information.
     *
     * @param guest The Guest object with updated details.
     * @return true if successful, false otherwise.
     */
    public boolean updateGuest(Guest guest) {
        String sql = "UPDATE Guests SET firstName = ?, lastName = ?, middleName = ?, contactNumber = ? WHERE guestId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guest.getFirstName());
            pstmt.setString(2, guest.getLastName());
            pstmt.setString(3, guest.getMiddleName());
            pstmt.setString(4, guest.getContactNumber());
            pstmt.setString(5, guest.getGuestId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating guest", e);
            return false;
        }
    }

    /**
     * Deletes a guest by their ID.
     *
     * @param guestId The ID of the guest to delete.
     * @return true if successful, false otherwise.
     */
    public boolean deleteGuest(String guestId) {
        String sql = "DELETE FROM Guests WHERE guestId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting guest", e);
            return false;
        }
    }

    /**
     * Searches for guests by a query (e.g., name or contact).
     *
     * @param query The search query.
     * @return List of guests matching the query.
     */
    public List<Guest> searchGuests(String query) {
        List<Guest> guests = new ArrayList<>();
        String sql = "SELECT * FROM Guests WHERE firstName LIKE ? OR lastName LIKE ? OR contactNumber LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Guest guest = new Guest(
                            rs.getString("guestId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("middleName"),
                            rs.getString("password"),
                            rs.getString("contactNumber")
                    );
                    guests.add(guest);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching guests", e);
        }

        return guests;
    }
}
