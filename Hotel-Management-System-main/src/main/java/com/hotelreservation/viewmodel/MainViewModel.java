package com.hotelreservation.viewmodel;

import com.hotelreservation.model.Staff;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * ViewModel for the main view.
 * Manages the data and state for the main view of the application.
 */
public class MainViewModel {
    
    private Staff currentStaff;
    private final BooleanProperty isDarkMode = new SimpleBooleanProperty(false);
    private final BooleanProperty isAdmin = new SimpleBooleanProperty(false);
    private final BooleanProperty isManager = new SimpleBooleanProperty(false);

    /**
     * Gets the current staff member.
     *
     * @return the current staff member
     */
    public Staff getCurrentStaff() {
        return currentStaff;
    }

    /**
     * Sets the current staff member and updates role-based properties.
     *
     * @param staff the staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        // Update role-based properties based on staff position
        updateRoleBasedProperties();
    }

    /**
     * Updates properties based on the current staff member's position.
     */
    private void updateRoleBasedProperties() {
        if (currentStaff != null) {
            String position = currentStaff.getPosition();
            isAdmin.set(position.equalsIgnoreCase("ADMIN"));
            isManager.set(position.equalsIgnoreCase("MANAGER"));
        }
    }

    /**
     * Gets the dark mode property.
     *
     * @return the dark mode property
     */
    public BooleanProperty darkModeProperty() {
        return isDarkMode;
    }

    /**
     * Gets whether dark mode is enabled.
     *
     * @return true if dark mode is enabled, false otherwise
     */
    public boolean isDarkMode() {
        return isDarkMode.get();
    }

    /**
     * Sets whether dark mode is enabled.
     *
     * @param darkMode true to enable dark mode, false to disable
     */
    public void setDarkMode(boolean darkMode) {
        isDarkMode.set(darkMode);
    }

    /**
     * Gets whether the current staff member is an admin.
     *
     * @return true if the current staff member is an admin, false otherwise
     */
    public boolean isAdmin() {
        return isAdmin.get();
    }

    /**
     * Gets whether the current staff member is a manager.
     *
     * @return true if the current staff member is a manager, false otherwise
     */
    public boolean isManager() {
        return isManager.get();
    }

    /**
     * Gets the admin property.
     *
     * @return the admin property
     */
    public BooleanProperty adminProperty() {
        return isAdmin;
    }

    /**
     * Gets the manager property.
     *
     * @return the manager property
     */
    public BooleanProperty managerProperty() {
        return isManager;
    }
}
