package com.hotelreservation.model;

public class Settings {
    private boolean darkMode;
    private String resolution; // Format: "widthxheight" e.g. "1920x1080"
    private String userId;

    // Constructor
    public Settings(boolean darkMode, String resolution, String userId) {
        this.darkMode = darkMode;
        this.resolution = resolution;
        this.userId = userId;
    }

    // Getters and Setters
    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Helper methods for resolution
    public int getWidth() {
        return Integer.parseInt(resolution.split("x")[0]);
    }

    public int getHeight() {
        return Integer.parseInt(resolution.split("x")[1]);
    }
}
