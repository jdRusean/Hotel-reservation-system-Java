package com.hotelreservation.model;

/**
 * Represents a guest in the hotel.
 */
public class Guest {
    private String guestId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String password;
    private String contactNumber;

    // No-args constructor
    public Guest() {
        this.guestId = "";
        this.firstName = "";
        this.lastName = "";
        this.middleName = "";
        this.password = "";
        this.contactNumber = "";
    }

    // Full constructor
    public Guest(String guestId, String firstName, String lastName, String middleName, String password, String contactNumber) {
        this.guestId = guestId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.password = password;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
