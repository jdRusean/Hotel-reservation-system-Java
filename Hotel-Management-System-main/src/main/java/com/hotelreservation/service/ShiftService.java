package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.StaffShift;
import com.hotelreservation.util.DatabaseConnection;

public class ShiftService {
    private static final Logger logger = LoggerFactory.getLogger(ShiftService.class);

    public List<StaffShift> getShifts(LocalDate date) {
        List<StaffShift> shifts = new ArrayList<>();
        String sql = "SELECT * FROM StaffShifts WHERE date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StaffShift shift = new StaffShift(
                        rs.getString("shiftId"),
                        rs.getString("staffId"),
                        rs.getTime("startTime").toLocalTime(),
                        rs.getTime("endTime").toLocalTime(),
                        rs.getDate("date").toLocalDate()
                    );
                    shifts.add(shift);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving shifts", e);
        }

        return shifts;
    }

    public boolean assignShift(StaffShift shift) {
        String sql = "INSERT INTO StaffShifts (staffId, startTime, endTime, date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, shift.getStaffId());
            pstmt.setTime(2, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(3, Time.valueOf(shift.getEndTime()));
            pstmt.setDate(4, Date.valueOf(shift.getDate()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error assigning shift", e);
            return false;
        }
    }

    public boolean updateShift(StaffShift shift) {
        String sql = "UPDATE StaffShifts SET startTime = ?, endTime = ?, date = ? WHERE shiftId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTime(1, Time.valueOf(shift.getStartTime()));
            pstmt.setTime(2, Time.valueOf(shift.getEndTime()));
            pstmt.setDate(3, Date.valueOf(shift.getDate()));
            pstmt.setString(4, shift.getShiftId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating shift", e);
            return false;
        }
    }

    public boolean deleteShift(String shiftId) {
        String sql = "DELETE FROM StaffShifts WHERE shiftId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, shiftId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting shift", e);
            return false;
        }
    }

    /**
     * Gets the current shift for a staff member.
     *
     * @param staffId The ID of the staff member
     * @return The current StaffShift if one exists, null otherwise
     */
    public StaffShift getCurrentShift(String staffId) {
        String sql = "SELECT * FROM StaffShifts WHERE staffId = ? " +
                    "AND date = CURRENT_DATE " +
                    "AND startTime <= CURRENT_TIME " +
                    "AND endTime >= CURRENT_TIME";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, staffId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new StaffShift(
                        rs.getString("shiftId"),
                        rs.getString("staffId"),
                        rs.getTime("startTime").toLocalTime(),
                        rs.getTime("endTime").toLocalTime(),
                        rs.getDate("date").toLocalDate()
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting current shift for staff: " + staffId, e);
        }

        return null;
    }
}
