package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Promo;
import com.hotelreservation.util.DatabaseConnection;

/**
 * Service for managing promotional offers.
 */
public class PromoService {
    private static final Logger logger = LoggerFactory.getLogger(PromoService.class);

    /**
     * Retrieves all promos from the database.
     *
     * @return List of promos.
     */
    public List<Promo> getAllPromos() {
        List<Promo> promos = new ArrayList<>();
        String sql = "SELECT * FROM Promos ORDER BY validFrom DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Promo promo = new Promo(
                        rs.getString("promoId"),
                        rs.getString("code"),
                        rs.getString("description"),
                        rs.getDouble("discountAmount"),
                        rs.getDate("validFrom") != null ? rs.getDate("validFrom").toLocalDate() : null,
                        rs.getDate("validUntil") != null ? rs.getDate("validUntil").toLocalDate() : null,
                        rs.getBoolean("active")
                );
                promos.add(promo);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving promos", e);
        }

        return promos;
    }

    /**
     * Creates a new promo.
     *
     * @param promo The promo to create
     * @return true if successful, false otherwise
     */
    public boolean createPromo(Promo promo) {
        String sql = "INSERT INTO Promos (promoId, code, description, discountAmount, validFrom, validUntil, active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Generate a new UUID for the promo if not provided
            if (promo.getPromoId() == null || promo.getPromoId().isEmpty()) {
                promo.setPromoId(UUID.randomUUID().toString());
            }

            pstmt.setString(1, promo.getPromoId());
            pstmt.setString(2, promo.getCode());
            pstmt.setString(3, promo.getDescription());
            pstmt.setDouble(4, promo.getDiscountAmount());
            pstmt.setDate(5, promo.getValidFrom() != null ? Date.valueOf(promo.getValidFrom()) : null);
            pstmt.setDate(6, promo.getValidUntil() != null ? Date.valueOf(promo.getValidUntil()) : null);
            pstmt.setBoolean(7, promo.isActive());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating promo", e);
            return false;
        }
    }

    /**
     * Updates an existing promo.
     *
     * @param promo The promo to update
     * @return true if successful, false otherwise
     */
    public boolean updatePromo(Promo promo) {
        String sql = "UPDATE Promos SET code = ?, description = ?, discountAmount = ?, " +
                    "validFrom = ?, validUntil = ?, active = ? WHERE promoId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, promo.getCode());
            pstmt.setString(2, promo.getDescription());
            pstmt.setDouble(3, promo.getDiscountAmount());
            pstmt.setDate(4, promo.getValidFrom() != null ? Date.valueOf(promo.getValidFrom()) : null);
            pstmt.setDate(5, promo.getValidUntil() != null ? Date.valueOf(promo.getValidUntil()) : null);
            pstmt.setBoolean(6, promo.isActive());
            pstmt.setString(7, promo.getPromoId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating promo", e);
            return false;
        }
    }

    /**
     * Deletes a promo.
     *
     * @param promoId The ID of the promo to delete
     * @return true if successful, false otherwise
     */
    public boolean deletePromo(String promoId) {
        String sql = "DELETE FROM Promos WHERE promoId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, promoId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting promo", e);
            return false;
        }
    }

    /**
     * Gets a promo by its code.
     *
     * @param code The promo code
     * @return The promo if found and valid, null otherwise
     */
    public Promo getValidPromoByCode(String code) {
        String sql = "SELECT * FROM Promos WHERE code = ? AND active = true " +
                    "AND (validFrom IS NULL OR validFrom <= CURRENT_DATE) " +
                    "AND (validUntil IS NULL OR validUntil >= CURRENT_DATE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Promo(
                            rs.getString("promoId"),
                            rs.getString("code"),
                            rs.getString("description"),
                            rs.getDouble("discountAmount"),
                            rs.getDate("validFrom") != null ? rs.getDate("validFrom").toLocalDate() : null,
                            rs.getDate("validUntil") != null ? rs.getDate("validUntil").toLocalDate() : null,
                            rs.getBoolean("active")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving promo by code", e);
        }

        return null;
    }

    /**
     * Toggles the active status of a promo.
     *
     * @param promoId The ID of the promo
     * @param active The new active status
     * @return true if successful, false otherwise
     */
    public boolean togglePromoStatus(String promoId, boolean active) {
        String sql = "UPDATE Promos SET active = ? WHERE promoId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, active);
            pstmt.setString(2, promoId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error toggling promo status", e);
            return false;
        }
    }
}
