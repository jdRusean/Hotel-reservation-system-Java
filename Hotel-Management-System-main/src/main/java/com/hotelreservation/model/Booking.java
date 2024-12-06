package com.hotelreservation.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a booking/reservation in the hotel.
 */
public class Booking {
    private String bookingId;
    private String guestId;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double totalAmount;
    private String status; // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    private String promoCode;
    private double discountAmount;
    private String notes;

    // Guest details cache for display
    private String guestName;
    private String roomNumber;

    /**
     * Creates a new Booking instance.
     */
    public Booking(String bookingId, String guestId, String roomId, LocalDate checkInDate, LocalDate checkOutDate,
                  double totalAmount, String status, String promoCode, double discountAmount, String notes) {
        this.bookingId = bookingId;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.promoCode = promoCode;
        this.discountAmount = discountAmount;
        this.notes = notes;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestId) {
        this.guestId = guestId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    /**
     * Calculates the number of nights for this booking.
     *
     * @return The number of nights
     */
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Gets the final amount after applying discount.
     *
     * @return The final amount
     */
    public double getFinalAmount() {
        return totalAmount - discountAmount;
    }

    /**
     * Checks if this booking is active (confirmed or checked in).
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return status.equals("CONFIRMED") || status.equals("CHECKED_IN");
    }

    /**
     * Checks if this booking is for today's check-in.
     *
     * @return true if check-in is today, false otherwise
     */
    public boolean isTodayCheckIn() {
        return checkInDate.equals(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId='" + bookingId + '\'' +
                ", guestId='" + guestId + '\'' +
                ", roomId='" + roomId + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", promoCode='" + promoCode + '\'' +
                ", discountAmount=" + discountAmount +
                ", notes='" + notes + '\'' +
                '}';
    }
}
