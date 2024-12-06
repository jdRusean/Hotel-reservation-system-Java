package com.hotelreservation.model;

import java.time.LocalDateTime;

/**
 * Represents a log entry of staff activity in the system.
 */
public class StaffLog {
    private String logId;
    private String staffId;
    private String action;
    private String details;
    private LocalDateTime timestamp;

    /**
     * Constructs a StaffLog object.
     *
     * @param logId     Unique identifier for the log entry
     * @param staffId   ID of the staff member who performed the action
     * @param action    Type of action performed (e.g., "Login", "Logout", "Password Reset")
     * @param timestamp Time when the action was performed
     */
    public StaffLog(String logId, String staffId, String action, LocalDateTime timestamp) {
        this.logId = logId;
        this.staffId = staffId;
        this.action = action;
        this.timestamp = timestamp;
    }

    /**
     * Constructs a StaffLog object with details.
     *
     * @param logId     Unique identifier for the log entry
     * @param staffId   ID of the staff member who performed the action
     * @param action    Type of action performed
     * @param details   Additional details about the action
     * @param timestamp Time when the action was performed
     */
    public StaffLog(String logId, String staffId, String action, String details, LocalDateTime timestamp) {
        this(logId, staffId, action, timestamp);
        this.details = details;
    }

    // Getters and Setters
    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details != null ? details : "";
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
