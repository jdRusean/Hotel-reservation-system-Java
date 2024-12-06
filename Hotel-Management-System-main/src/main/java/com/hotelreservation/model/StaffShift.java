package com.hotelreservation.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a work shift assigned to a staff member.
 */
public class StaffShift {
    private String shiftId;
    private String staffId;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate date;

    /**
     * Default constructor for StaffShift.
     */
    public StaffShift() {
        // Default constructor required for JavaFX
    }

    public StaffShift(String shiftId, String staffId, LocalTime startTime, LocalTime endTime, LocalDate date) {
        this.shiftId = shiftId;
        this.staffId = staffId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
    }

    // Getters and Setters
    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
