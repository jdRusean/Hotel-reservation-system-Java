package com.hotelreservation.model;

import java.time.LocalDateTime;

/**
 * Represents a notification in the system.
 */
public class Notification {
    private String notificationId;
    private String staffId;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    /**
     * Creates a new Notification instance.
     *
     * @param notificationId The unique identifier for the notification
     * @param staffId The ID of the staff member who sent the notification
     * @param message The notification message
     * @param createdAt The timestamp when the notification was created
     * @param read Whether the notification has been read
     */
    public Notification(String notificationId, String staffId, String message, LocalDateTime createdAt, boolean read) {
        this.notificationId = notificationId;
        this.staffId = staffId;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId='" + notificationId + '\'' +
                ", staffId='" + staffId + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", read=" + read +
                '}';
    }
}
