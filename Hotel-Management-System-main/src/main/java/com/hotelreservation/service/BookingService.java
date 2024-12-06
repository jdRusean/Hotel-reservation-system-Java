package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Booking;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service for managing bookings/reservations.
 */
public class BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    /**
     * Retrieves all bookings with guest and room details.
     *
     * @return List of bookings
     */
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, g.firstName || ' ' || g.lastName as guestName, r.roomNumber " +
                    "FROM Bookings b " +
                    "JOIN Guests g ON b.guestId = g.guestId " +
                    "JOIN Rooms r ON b.roomId = r.roomId " +
                    "ORDER BY b.checkInDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Booking booking = createBookingFromResultSet(rs);
                booking.setGuestName(rs.getString("guestName"));
                booking.setRoomNumber(rs.getString("roomNumber"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving bookings", e);
        }

        return bookings;
    }

    /**
     * Creates a new booking.
     *
     * @param booking The booking to create
     * @return true if successful, false otherwise
     */
    public boolean createBooking(Booking booking) {
        String sql = "INSERT INTO Bookings (bookingId, guestId, roomId, checkInDate, checkOutDate, " +
                    "totalAmount, status, promoCode, discountAmount, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Generate a new UUID for the booking if not provided
            if (booking.getBookingId() == null || booking.getBookingId().isEmpty()) {
                booking.setBookingId(UUID.randomUUID().toString());
            }

            pstmt.setString(1, booking.getBookingId());
            pstmt.setString(2, booking.getGuestId());
            pstmt.setString(3, booking.getRoomId());
            pstmt.setDate(4, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(5, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setDouble(6, booking.getTotalAmount());
            pstmt.setString(7, booking.getStatus());
            pstmt.setString(8, booking.getPromoCode());
            pstmt.setDouble(9, booking.getDiscountAmount());
            pstmt.setString(10, booking.getNotes());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating booking", e);
            return false;
        }
    }

    /**
     * Updates an existing booking.
     *
     * @param booking The booking to update
     * @return true if successful, false otherwise
     */
    public boolean updateBooking(Booking booking) {
        String sql = "UPDATE Bookings SET guestId = ?, roomId = ?, checkInDate = ?, checkOutDate = ?, " +
                    "totalAmount = ?, status = ?, promoCode = ?, discountAmount = ?, notes = ? " +
                    "WHERE bookingId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, booking.getGuestId());
            pstmt.setString(2, booking.getRoomId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setDouble(5, booking.getTotalAmount());
            pstmt.setString(6, booking.getStatus());
            pstmt.setString(7, booking.getPromoCode());
            pstmt.setDouble(8, booking.getDiscountAmount());
            pstmt.setString(9, booking.getNotes());
            pstmt.setString(10, booking.getBookingId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating booking", e);
            return false;
        }
    }

    /**
     * Updates the status of a booking.
     *
     * @param bookingId The ID of the booking
     * @param status The new status
     * @return true if successful, false otherwise
     */
    public boolean updateBookingStatus(String bookingId, String status) {
        String sql = "UPDATE Bookings SET status = ? WHERE bookingId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, bookingId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating booking status", e);
            return false;
        }
    }

    /**
     * Searches for bookings based on various criteria.
     *
     * @param searchText Text to search in guest name or booking ID
     * @param status Status to filter by (optional)
     * @param date Date to filter by (optional)
     * @return List of matching bookings
     */
    public List<Booking> searchBookings(String searchText, String status, LocalDate date) {
        List<Booking> bookings = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT b.*, g.firstName || ' ' || g.lastName as guestName, r.roomNumber " +
            "FROM Bookings b " +
            "JOIN Guests g ON b.guestId = g.guestId " +
            "JOIN Rooms r ON b.roomId = r.roomId " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (searchText != null && !searchText.isEmpty()) {
            sql.append(" AND (LOWER(g.firstName || ' ' || g.lastName) LIKE ? OR LOWER(b.bookingId) LIKE ?)");
            String searchPattern = "%" + searchText.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND b.status = ?");
            params.add(status);
        }

        if (date != null) {
            sql.append(" AND (b.checkInDate = ? OR b.checkOutDate = ?)");
            params.add(Date.valueOf(date));
            params.add(Date.valueOf(date));
        }

        sql.append(" ORDER BY b.checkInDate DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Booking booking = createBookingFromResultSet(rs);
                    booking.setGuestName(rs.getString("guestName"));
                    booking.setRoomNumber(rs.getString("roomNumber"));
                    bookings.add(booking);
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching bookings", e);
        }

        return bookings;
    }

    /**
     * Gets statistics about bookings.
     *
     * @return Array containing [total bookings, active bookings, today's check-ins]
     */
    public int[] getBookingStats() {
        int[] stats = new int[3];
        String sql = "SELECT " +
                    "(SELECT COUNT(*) FROM Bookings) as total, " +
                    "(SELECT COUNT(*) FROM Bookings WHERE status IN ('CONFIRMED', 'CHECKED_IN')) as active, " +
                    "(SELECT COUNT(*) FROM Bookings WHERE checkInDate = CURRENT_DATE) as todayCheckins";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats[0] = rs.getInt("total");
                stats[1] = rs.getInt("active");
                stats[2] = rs.getInt("todayCheckins");
            }
        } catch (SQLException e) {
            logger.error("Error getting booking stats", e);
        }

        return stats;
    }

    private Booking createBookingFromResultSet(ResultSet rs) throws SQLException {
        return new Booking(
            rs.getString("bookingId"),
            rs.getString("guestId"),
            rs.getString("roomId"),
            rs.getDate("checkInDate").toLocalDate(),
            rs.getDate("checkOutDate").toLocalDate(),
            rs.getDouble("totalAmount"),
            rs.getString("status"),
            rs.getString("promoCode"),
            rs.getDouble("discountAmount"),
            rs.getString("notes")
        );
    }
}
