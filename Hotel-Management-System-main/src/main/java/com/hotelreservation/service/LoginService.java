package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Staff;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service class for handling user login and password management.
 */
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    /**
     * Verifies the login credentials of a user (by staffId).
     *
     * @param staffId The staff ID of the user.
     * @param password The password of the user.
     * @return true if the credentials are correct, false otherwise.
     */
    public boolean verifyLogin(String staffId, String password) {
        String sql = "SELECT * FROM Staffs WHERE staffId = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error verifying login", e);
            return false;
        }
    }

    /**
     * Retrieves a staff member by their ID.
     *
     * @param staffId The ID of the staff member to retrieve.
     * @return The Staff object if found, null otherwise.
     * @throws SQLException if a database error occurs
     */
    public Staff getStaffById(String staffId) throws SQLException {
        String sql = "SELECT * FROM Staffs WHERE staffId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Staff(
                    rs.getString("staffId"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("middleName"),
                    rs.getString("password"),
                    rs.getString("position")
                );
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error retrieving staff member", e);
            throw e;
        }
    }

    /**
     * Updates the password for a given staff member.
     *
     * @param staffId The ID of the staff member.
     * @param newPassword The new password for the staff member.
     * @return true if the password was updated successfully, false otherwise.
     */
    public boolean changePassword(String staffId, String newPassword) {
        String sql = "UPDATE Staff SET password = ? WHERE staffId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, staffId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error changing password", e);
            return false;
        }
    }

    /**
     * Resets the password for a staff member (admin can reset passwords).
     *
     * @param staffId The ID of the staff member.
     * @param newPassword The new password for the staff member.
     * @return true if the password was reset successfully, false otherwise.
     */
    public boolean resetPassword(String staffId, String newPassword) {
        return changePassword(staffId, newPassword);
    }
}
