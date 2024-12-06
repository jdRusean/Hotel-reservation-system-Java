package com.hotelreservation.controller;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.App;
import com.hotelreservation.model.Staff;
import com.hotelreservation.model.StaffShift;
import com.hotelreservation.service.ShiftService;
import com.hotelreservation.service.StaffService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;

/**
 * Controller for the profile view.
 */
public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Text staffPositionText;
    @FXML private Label staffIdLabel;
    @FXML private Label staffNameLabel;
    @FXML private Label positionLabel;
    @FXML private Label shiftTimeLabel;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private Staff currentStaff;
    private final StaffService staffService;
    private final ShiftService shiftService;

    public ProfileController() {
        this.staffService = new StaffService();
        this.shiftService = new ShiftService();
    }

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize will be called when view is loaded
        // Actual data will be set when setStaff is called
    }

    /**
     * Sets the current staff member and updates the UI.
     *
     * @param staff The staff member whose profile is being viewed
     */
    public void setStaff(Staff staff) {
        this.currentStaff = staff;
        updateProfileDisplay();
    }

    /**
     * Updates the profile display with the current staff member's information.
     */
    private void updateProfileDisplay() {
        if (currentStaff != null) {
            staffPositionText.setText(currentStaff.getPosition());
            staffIdLabel.setText(currentStaff.getStaffId());
            staffNameLabel.setText(String.format("%s %s %s", 
                currentStaff.getFirstName(),
                currentStaff.getMiddleName(),
                currentStaff.getLastName()).trim());
            positionLabel.setText(currentStaff.getPosition());
            
            // Get and display current shift
            StaffShift currentShift = shiftService.getCurrentShift(currentStaff.getStaffId());
            if (currentShift != null) {
                String shiftTime = String.format("%s - %s",
                    currentShift.getStartTime().format(TIME_FORMATTER),
                    currentShift.getEndTime().format(TIME_FORMATTER));
                shiftTimeLabel.setText(shiftTime);
            } else {
                shiftTimeLabel.setText("No shift scheduled");
            }
        }
    }

    /**
     * Handles the change password action.
     */
    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            App.showErrorAlert("Error", "All fields are required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            App.showErrorAlert("Error", "New passwords do not match");
            return;
        }

        // Verify current password
        Staff authenticatedStaff = staffService.authenticateStaff(currentStaff.getStaffId(), currentPassword);
        if (authenticatedStaff == null) {
            App.showErrorAlert("Error", "Current password is incorrect");
            return;
        }

        // Change password
        boolean success = staffService.resetStaffPassword(currentStaff.getStaffId(), newPassword);
        if (success) {
            App.showInfoAlert("Success", "Password changed successfully");
            clearPasswordFields();
        } else {
            App.showErrorAlert("Error", "Failed to change password");
        }
    }

    /**
     * Clears all password fields.
     */
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
}
