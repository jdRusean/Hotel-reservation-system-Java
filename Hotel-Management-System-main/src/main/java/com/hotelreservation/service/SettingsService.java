package com.hotelreservation.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Settings;
import com.hotelreservation.util.DatabaseConnection;

public class SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

    /**
     * Loads settings for a specific user.
     *
     * @param userId The ID of the user whose settings to load
     * @return The user's settings, or null if not found
     */
    public Settings loadSettings(String userId) {
        String sql = "SELECT * FROM Settings WHERE userId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Settings(
                        rs.getBoolean("darkMode"),
                        rs.getString("resolution"),
                        rs.getString("userId")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading settings for user: " + userId, e);
        }

        return null;
    }

    /**
     * Saves settings for a user.
     *
     * @param settings The settings to save
     * @return true if successful, false otherwise
     */
    public boolean saveSettings(Settings settings) {
        String sql = "INSERT INTO Settings (userId, darkMode, resolution) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT (userId) " +
                    "DO UPDATE SET darkMode = EXCLUDED.darkMode, " +
                    "resolution = EXCLUDED.resolution";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, settings.getUserId());
            pstmt.setBoolean(2, settings.isDarkMode());
            pstmt.setString(3, settings.getResolution());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving settings", e);
            return false;
        }
    }

    /**
     * Applies dark mode setting.
     *
     * @param darkMode true to enable dark mode, false to disable
     * @param userId The ID of the user whose setting to update
     * @return true if successful, false otherwise
     */
    public boolean applyDarkMode(boolean darkMode, String userId) {
        Settings settings = loadSettings(userId);
        if (settings != null) {
            settings.setDarkMode(darkMode);
            return saveSettings(settings);
        }
        return false;
    }

    /**
     * Applies resolution setting.
     *
     * @param resolution The new resolution to apply
     * @param userId The ID of the user whose setting to update
     * @return true if successful, false otherwise
     */
    public boolean applyResolution(String resolution, String userId) {
        Settings settings = loadSettings(userId);
        if (settings != null) {
            settings.setResolution(resolution);
            return saveSettings(settings);
        }
        return false;
    }
}
