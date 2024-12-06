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

import com.hotelreservation.model.Notification;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service class for managing notifications.
 */
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Retrieves all notifications from the database.
     *
     * @return List of notifications.
     */
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notifications ORDER BY createdAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Notification notification = new Notification(
                        rs.getString("notificationId"),
                        rs.getString("staffId"),
                        rs.getString("message"),
                        rs.getTimestamp("createdAt").toLocalDateTime(),
                        rs.getBoolean("read")
                );
                notifications.add(notification);
            }
        } catch (SQLException e) {
            logger.error("Error fetching notifications", e);
        }

        return notifications;
    }

    /**
     * Sends a new notification.
     *
     * @param message  The message content of the notification.
     * @param senderId The ID of the user sending the notification.
     * @return true if successful, false otherwise.
     */
    public boolean sendNotification(String message, String senderId) {
        String sql = "INSERT INTO Notifications (notificationId, staffId, message, createdAt, read) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String notificationId = java.util.UUID.randomUUID().toString();
            Timestamp now = new Timestamp(System.currentTimeMillis());

            pstmt.setString(1, notificationId);
            pstmt.setString(2, senderId);
            pstmt.setString(3, message);
            pstmt.setTimestamp(4, now);
            pstmt.setBoolean(5, false); // Initially unread

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error sending notification", e);
            return false;
        }
    }

    /**
     * Marks a notification as read.
     *
     * @param notificationId The ID of the notification to mark as read.
     * @return true if successful, false otherwise.
     */
    public boolean markAsRead(String notificationId) {
        String sql = "UPDATE Notifications SET read = true WHERE notificationId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking notification as read", e);
            return false;
        }
    }

    /**
     * Gets unread notifications count for a staff member.
     *
     * @param staffId The ID of the staff member.
     * @return The number of unread notifications.
     */
    public int getUnreadCount(String staffId) {
        String sql = "SELECT COUNT(*) FROM Notifications WHERE read = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting unread notifications count", e);
        }

        return 0;
    }
}
