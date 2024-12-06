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

import com.hotelreservation.model.Room;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service for managing rooms.
 */
public class RoomService {
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    /**
     * Retrieves all rooms.
     *
     * @return List of rooms
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM Rooms ORDER BY roomNumber";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = createRoomFromResultSet(rs);
                rooms.add(room);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving rooms", e);
        }

        return rooms;
    }

    /**
     * Gets available rooms for a date range.
     *
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return List of available rooms
     */
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.* FROM Rooms r " +
                    "WHERE r.status = 'AVAILABLE' " +
                    "AND r.roomId NOT IN (" +
                    "    SELECT b.roomId FROM Bookings b " +
                    "    WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                    "    AND (" +
                    "        (b.checkInDate <= ? AND b.checkOutDate > ?) OR " +
                    "        (b.checkInDate < ? AND b.checkOutDate >= ?) OR " +
                    "        (b.checkInDate >= ? AND b.checkOutDate <= ?)" +
                    "    )" +
                    ") " +
                    "ORDER BY r.roomNumber";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(checkIn));
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));
            pstmt.setDate(4, Date.valueOf(checkOut));
            pstmt.setDate(5, Date.valueOf(checkIn));
            pstmt.setDate(6, Date.valueOf(checkOut));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room room = createRoomFromResultSet(rs);
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting available rooms", e);
        }

        return rooms;
    }

    /**
     * Updates room status.
     *
     * @param roomId Room ID
     * @param status New status
     * @return true if successful, false otherwise
     */
    public boolean updateRoomStatus(String roomId, String status) {
        String sql = "UPDATE Rooms SET status = ? WHERE roomId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, roomId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating room status", e);
            return false;
        }
    }

    /**
     * Gets a room by its ID.
     *
     * @param roomId Room ID
     * @return Room if found, null otherwise
     */
    public Room getRoomById(String roomId) {
        String sql = "SELECT * FROM Rooms WHERE roomId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createRoomFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting room by ID", e);
        }

        return null;
    }

    /**
     * Gets a room by its room number.
     *
     * @param roomNumber Room number
     * @return Room if found, null otherwise
     */
    public Room getRoomByNumber(String roomNumber) {
        String sql = "SELECT * FROM Rooms WHERE roomNumber = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createRoomFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting room by number", e);
        }

        return null;
    }

    /**
     * Creates a new room.
     *
     * @param room Room to create
     * @return true if successful, false otherwise
     */
    public boolean createRoom(Room room) {
        String sql = "INSERT INTO Rooms (roomId, roomNumber, type, rate, capacity, status, " +
                    "description, amenities, floor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Generate a new UUID for the room if not provided
            if (room.getRoomId() == null || room.getRoomId().isEmpty()) {
                room.setRoomId(UUID.randomUUID().toString());
            }

            pstmt.setString(1, room.getRoomId());
            pstmt.setString(2, room.getRoomNumber());
            pstmt.setString(3, room.getType());
            pstmt.setDouble(4, room.getRate());
            pstmt.setInt(5, room.getCapacity());
            pstmt.setString(6, room.getStatus());
            pstmt.setString(7, room.getDescription());
            pstmt.setString(8, room.getAmenities());
            pstmt.setInt(9, room.getFloor());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating room", e);
            return false;
        }
    }

    /**
     * Updates an existing room.
     *
     * @param room Room to update
     * @return true if successful, false otherwise
     */
    public boolean updateRoom(Room room) {
        String sql = "UPDATE Rooms SET roomNumber = ?, type = ?, rate = ?, capacity = ?, " +
                    "status = ?, description = ?, amenities = ?, floor = ? WHERE roomId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getType());
            pstmt.setDouble(3, room.getRate());
            pstmt.setInt(4, room.getCapacity());
            pstmt.setString(5, room.getStatus());
            pstmt.setString(6, room.getDescription());
            pstmt.setString(7, room.getAmenities());
            pstmt.setInt(8, room.getFloor());
            pstmt.setString(9, room.getRoomId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating room", e);
            return false;
        }
    }

    private Room createRoomFromResultSet(ResultSet rs) throws SQLException {
        return new Room(
            rs.getString("roomId"),
            rs.getString("roomNumber"),
            rs.getString("type"),
            rs.getDouble("rate"),
            rs.getInt("capacity"),
            rs.getString("status"),
            rs.getString("description"),
            rs.getString("amenities"),
            rs.getInt("floor")
        );
    }
}
