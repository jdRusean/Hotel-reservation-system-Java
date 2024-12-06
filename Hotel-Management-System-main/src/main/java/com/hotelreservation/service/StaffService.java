package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Staff;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service for managing staff operations.
 */
public class StaffService {
    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);

    /**
     * Retrieves all staff members from the database.
     *
     * @return List of all staff members.
     */
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM Staffs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Staff staff = new Staff(
                        rs.getString("staffId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("middleName"),
                        rs.getString("password"),
                        rs.getString("position")
                );
                staffList.add(staff);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff list", e);
        }

        return staffList;
    }

    /**
     * Updates the details of a staff member.
     *
     * @param staff The updated Staff object.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE Staff SET firstName = ?, lastName = ?, middleName = ?, position = ? WHERE staffId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getMiddleName());
            pstmt.setString(4, staff.getPosition()); // This is position not role
            pstmt.setString(5, staff.getStaffId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating staff", e);
            return false;
        }
    }

    /**
     * Deletes a staff member by their staffId.
     *
     * @param staffId The ID of the staff member to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteStaff(String staffId) {
        String sql = "DELETE FROM Staffs WHERE staffId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting staff", e);
            return false;
        }
    }

    /**
     * Authenticates a staff member by their staffId and password.
     *
     * @param staffId  The staffId of the staff member.
     * @param password The password of the staff member.
     * @return Staff object if authentication is successful, null otherwise.
     */
    public Staff authenticateStaff(String staffId, String password) {
        String sql = "SELECT * FROM Staffs WHERE staffId = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);  // Use staffId here, not username
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            logger.error("Error authenticating staff", e);
        }

        return null;
    }

    /**
     * Changes the password for the staff member.
     *
     * @param staffId   The staffId of the staff member.
     * @param newPassword The new password for the staff member.
     * @return true if the password was updated successfully, false otherwise.
     */
    public boolean resetStaffPassword(String staffId, String newPassword) {
        String sql = "UPDATE Staff SET password = ? WHERE staffId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setString(2, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error resetting staff password", e);
            return false;
        }
    }

    /**
     * Updates the shift details for a staff member.
     *
     * @param staff The Staff object with the updated shift details.
     * @return true if the shift was updated successfully, false otherwise.
     */
    public boolean updateStaffShift(Staff staff) {
        // Implement your shift update logic here
        // Placeholder return value
        return true;
    }

    /**
     * Retrieves a staff member by their ID.
     *
     * @param staffId The ID of the staff member to retrieve.
     * @return Staff object if found, null otherwise.
     */
    public Staff getStaffById(String staffId) {
        String sql = "SELECT * FROM Staffs WHERE staffId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);

            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff by ID", e);
        }

        return null;
    }

    /**
     * Resets a staff member's password to the default value.
     *
     * @param staffId The ID of the staff member whose password should be reset
     * @return true if the password was reset successfully, false otherwise
     */
    public boolean resetPassword(String staffId) {
        // Default password is "password123"
        String defaultPassword = "password123";
        return resetStaffPassword(staffId, defaultPassword);
    }

    /**
     * Adds a new staff member to the database.
     *
     * @param staff The Staff object containing the new staff member's details.
     * @return true if the staff was added successfully, false otherwise.
     */
    public boolean addStaff(Staff staff) {
        String sql = "INSERT INTO Staffs (staffId, firstName, lastName, middleName, password, position) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staff.getStaffId());
            pstmt.setString(2, staff.getFirstName());
            pstmt.setString(3, staff.getLastName());
            pstmt.setString(4, staff.getMiddleName());
            pstmt.setString(5, staff.getPassword());
            pstmt.setString(6, staff.getPosition());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding new staff member", e);
            return false;
        }
    }
}
