package com.hotelreservation.model;

/**
 * Represents a staff member in the hotel, including their details such as staff ID, name, position, and password.
 */
public class Staff {

    private String staffId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String password;
    private String position;

    /**
     * Default constructor for Staff.
     */
    public Staff() {
        // Default constructor required for JavaFX
    }

    /**
     * Constructs a Staff object with the specified details.
     *
     * @param staffId     the unique ID for the staff member
     * @param firstName   the first name of the staff member
     * @param lastName    the last name of the staff member
     * @param middleName  the middle name of the staff member (optional)
     * @param password    the password for the staff member's account
     * @param position    the position of the staff member in the hotel
     */
    public Staff(String staffId, String firstName, String lastName, String middleName, String password, String position) {
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.password = password;
        this.position = position;
    }

    // Getters and setters

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
