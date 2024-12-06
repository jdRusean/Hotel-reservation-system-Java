package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.StaffLog;
import com.hotelreservation.util.DatabaseConnection;

public class StaffLogService {
    private static final Logger logger = LoggerFactory.getLogger(StaffLogService.class);

    /**
     * Retrieves all staff logs from the database.
     *
     * @return List of all staff logs.
     */
    public List<StaffLog> getAllStaffLogs() {
        List<StaffLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM StaffLogs ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                StaffLog log = new StaffLog(
                    rs.getString("logId"),
                    rs.getString("staffId"),
                    rs.getString("action"),
                    rs.getString("details"),
                    rs.getTimestamp("timestamp").toLocalDateTime()
                );
                logs.add(log);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff logs", e);
        }

        return logs;
    }

    /**
     * Adds a new log entry to the database.
     *
     * @param log The log entry to add
     * @return true if successful, false otherwise
     */
    public boolean addLog(StaffLog log) {
        String sql = "INSERT INTO StaffLogs (staffId, action, details, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, log.getStaffId());
            pstmt.setString(2, log.getAction());
            pstmt.setString(3, log.getDetails());
            pstmt.setTimestamp(4, Timestamp.valueOf(log.getTimestamp()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding staff log", e);
            return false;
        }
    }

    /**
     * Gets logs for a specific staff member.
     *
     * @param staffId The ID of the staff member
     * @return List of logs for the staff member
     */
    public List<StaffLog> getStaffLogs(String staffId) {
        List<StaffLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM StaffLogs WHERE staffId = ? ORDER BY timestamp DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StaffLog log = new StaffLog(
                        rs.getString("logId"),
                        rs.getString("staffId"),
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getTimestamp("timestamp").toLocalDateTime()
                    );
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving staff logs for staff: " + staffId, e);
        }

        return logs;
    }

    /**
     * Clears old logs from the database.
     *
     * @param daysToKeep Number of days of logs to keep
     * @return true if successful, false otherwise
     */
    public boolean clearOldLogs(int daysToKeep) {
        String sql = "DELETE FROM StaffLogs WHERE timestamp < DATEADD('DAY', -?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, daysToKeep);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error clearing old logs", e);
            return false;
        }
    }
}
