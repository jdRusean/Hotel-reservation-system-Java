package com.hotelreservation.model;

/**
 * Represents a room in the hotel.
 */
public class Room {
    private String roomId;
    private String roomNumber;
    private String type;
    private double rate;
    private int capacity;
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE
    private String description;
    private String amenities;
    private int floor;

    /**
     * Creates a new Room instance.
     */
    public Room(String roomId, String roomNumber, String type, double rate, int capacity,
                String status, String description, String amenities, int floor) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.type = type;
        this.rate = rate;
        this.capacity = capacity;
        this.status = status;
        this.description = description;
        this.amenities = amenities;
        this.floor = floor;
    }

    // Getters and Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    /**
     * Checks if the room is available.
     *
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return status.equals("AVAILABLE");
    }

    /**
     * Gets a formatted display string for the room.
     *
     * @return A formatted string with room details
     */
    public String getDisplayString() {
        return String.format("Room %s - %s (%d persons) - $%.2f/night",
                roomNumber, type, capacity, rate);
    }

    /**
     * Gets a formatted list of amenities.
     *
     * @return A formatted string with amenities
     */
    public String getFormattedAmenities() {
        if (amenities == null || amenities.isEmpty()) {
            return "No amenities listed";
        }
        return amenities.replace(",", ", ");
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId='" + roomId + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", type='" + type + '\'' +
                ", rate=" + rate +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", amenities='" + amenities + '\'' +
                ", floor=" + floor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId.equals(room.roomId);
    }

    @Override
    public int hashCode() {
        return roomId.hashCode();
    }
}
