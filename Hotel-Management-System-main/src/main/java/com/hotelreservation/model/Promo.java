package com.hotelreservation.model;

import java.time.LocalDate;

/**
 * Represents a promotional offer in the hotel.
 */
public class Promo {
    private String promoId;
    private String code;
    private String description;
    private double discountAmount;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private boolean active;

    /**
     * Creates a new Promo instance.
     *
     * @param promoId The unique identifier for the promo
     * @param code The promo code
     * @param description The description of the promo
     * @param discountAmount The discount amount
     * @param validFrom The start date of the promo
     * @param validUntil The end date of the promo
     * @param active Whether the promo is currently active
     */
    public Promo(String promoId, String code, String description, double discountAmount,
                LocalDate validFrom, LocalDate validUntil, boolean active) {
        this.promoId = promoId;
        this.code = code;
        this.description = description;
        this.discountAmount = discountAmount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.active = active;
    }

    // Getters and Setters
    public String getPromoId() {
        return promoId;
    }

    public void setPromoId(String promoId) {
        this.promoId = promoId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Checks if the promo is currently valid based on the current date.
     *
     * @return true if the promo is valid, false otherwise
     */
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return active && 
               (validFrom == null || !now.isBefore(validFrom)) && 
               (validUntil == null || !now.isAfter(validUntil));
    }

    /**
     * Gets the status of the promo (Active/Inactive, Valid/Expired).
     *
     * @return The status string
     */
    public String getStatus() {
        if (!active) {
            return "Inactive";
        }
        if (isValid()) {
            return "Active";
        }
        LocalDate now = LocalDate.now();
        if (validFrom != null && now.isBefore(validFrom)) {
            return "Upcoming";
        }
        return "Expired";
    }

    @Override
    public String toString() {
        return "Promo{" +
                "promoId='" + promoId + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", discountAmount=" + discountAmount +
                ", validFrom=" + validFrom +
                ", validUntil=" + validUntil +
                ", active=" + active +
                '}';
    }
}
