package com.hotelreservation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import com.hotelreservation.service.LoginService;
import com.hotelreservation.App;
import com.hotelreservation.model.Staff;

/**
 * Controller for the login view.
 */
public class LoginController {

    @FXML
    private TextField staffIdField;

    @FXML
    private PasswordField passwordField;

    private LoginService loginService;

    /**
     * Initializes the LoginController and the LoginService.
     */
    public LoginController() {
        this.loginService = new LoginService();
    }

    /**
     * Handles the login action.
     * Validates the staff ID and password, and proceeds to the main view if successful.
     */
    @FXML
    private void handleLogin() {
        String staffId = staffIdField.getText();
        String password = passwordField.getText();

        if (loginService.verifyLogin(staffId, password)) {
            try {
                Staff authenticatedStaff = loginService.getStaffById(staffId);
                App.showMainView(authenticatedStaff);
            } catch (Exception e) {
                App.showErrorAlert("Error", "Failed to load main view: " + e.getMessage());
            }
        } else {
            App.showErrorAlert("Login Failed", "Invalid Staff ID or Password");
        }
    }
}
