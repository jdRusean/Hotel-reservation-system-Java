package com.hotelreservation.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.App;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.LogoutService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Controller for the main view.
 */
public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private StackPane contentArea;
    private Staff currentStaff;
    @FXML private Button manageGuestsButton;
    @FXML private Button staffManagementButton;
    @FXML private Button staffLogsButton;
    @FXML private Button promosButton;

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            App.showErrorAlert("Error", "Failed to load view: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        // Initialize UI components
    }

    @FXML
    private void showProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/profile-view.fxml"));
            Node view = loader.load();
            ProfileController controller = loader.getController();
            controller.setStaff(currentStaff);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            App.showErrorAlert("Error", "Failed to load profile view: " + e.getMessage());
        }
    }

    @FXML
    private void manageGuests() {
        if (hasManagerAccess()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/manage-guests-view.fxml"));
                Node view = loader.load();
                ManageGuestsController controller = loader.getController();
                controller.setCurrentStaff(currentStaff);
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                App.showErrorAlert("Error", "Failed to load manage guests view: " + e.getMessage());
            }
        } else {
            App.showErrorAlert("Access Denied", "You don't have permission to access this feature.");
        }
    }

    @FXML
    private void showNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/notifications-view.fxml"));
            Node view = loader.load();
            NotificationsController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            App.showErrorAlert("Error", "Failed to load notifications view: " + e.getMessage());
        }
    }

    @FXML
    private void showPromos() {
        if (hasAdminAccess()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/promos-view.fxml"));
                Node view = loader.load();
                PromosController controller = loader.getController();
                controller.setCurrentStaff(currentStaff);
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                App.showErrorAlert("Error", "Failed to load promos view: " + e.getMessage());
            }
        } else {
            App.showErrorAlert("Access Denied", "You don't have permission to access this feature.");
        }
    }

    @FXML
    private void showReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/reservations-view.fxml"));
            Node view = loader.load();
            ReservationsController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            App.showErrorAlert("Error", "Failed to load reservations view: " + e.getMessage());
        }
    }

    @FXML
    private void showAvailability() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/availability-view.fxml"));
            Node view = loader.load();
            AvailabilityController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            App.showErrorAlert("Error", "Failed to load availability calendar: " + e.getMessage());
        }
    }

    @FXML
    private void manageStaff() {
        if (hasManagerAccess()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/staff-management-view.fxml"));
                Node view = loader.load();
                StaffManagementController controller = loader.getController();
                controller.setCurrentStaff(currentStaff);
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                App.showErrorAlert("Error", "Failed to load staff management view: " + e.getMessage());
            }
        } else {
            App.showErrorAlert("Access Denied", "You don't have permission to access this feature.");
        }
    }

    @FXML
    private void showStaffLogs() {
        if (hasAdminAccess()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotelreservation/view/staff-logs-view.fxml"));
                Node view = loader.load();
                StaffLogsController controller = loader.getController();
                controller.setCurrentStaff(currentStaff);
                contentArea.getChildren().setAll(view);
            } catch (IOException e) {
                App.showErrorAlert("Error", "Failed to load staff logs view: " + e.getMessage());
            }
        } else {
            App.showErrorAlert("Access Denied", "You don't have permission to access this feature.");
        }
    }

    @FXML
    private void showSettings() {
        try {
            logger.info("Loading settings view...");
            // Clear existing content first
            contentArea.getChildren().clear();
            
            // Load new settings view
            String fxmlPath = "/com/hotelreservation/view/settings-view.fxml";
            logger.info("Loading FXML from: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            
            // Set up controller
            SettingsController controller = loader.getController();
            controller.setCurrentStaff(currentStaff);
            
            // Add view to content area
            contentArea.getChildren().setAll(view);
            logger.info("Settings view loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load settings view", e);
            App.showErrorAlert("Error", "Failed to load settings view: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            LogoutService logoutService = new LogoutService();
            if (logoutService.logout(currentStaff)) {
                App.showLoginView();
            } else {
                App.showErrorAlert("Error", "Failed to process logout");
            }
        } catch (Exception e) {
            App.showErrorAlert("Error", "Failed to logout: " + e.getMessage());
            logger.error("Error during logout", e);
        }
    }

    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        updateUIBasedOnRole();
    }

    private void updateUIBasedOnRole() {
        if (currentStaff != null) {
            boolean hasManagerAccess = hasManagerAccess();
            boolean hasAdminAccess = hasAdminAccess();

            if (manageGuestsButton != null) {
                manageGuestsButton.setVisible(hasManagerAccess);
                manageGuestsButton.setManaged(hasManagerAccess);
            }
            if (staffManagementButton != null) {
                staffManagementButton.setVisible(hasManagerAccess);
                staffManagementButton.setManaged(hasManagerAccess);
            }
            if (staffLogsButton != null) {
                staffLogsButton.setVisible(hasAdminAccess);
                staffLogsButton.setManaged(hasAdminAccess);
            }
            if (promosButton != null) {
                promosButton.setVisible(hasAdminAccess);
                promosButton.setManaged(hasAdminAccess);
            }
        }
    }

    private boolean hasManagerAccess() {
        if (currentStaff == null) return false;
        String position = currentStaff.getPosition().toUpperCase();
        return position.equals("ADMIN") || position.equals("MANAGER");
    }

    private boolean hasAdminAccess() {
        if (currentStaff == null) return false;
        return currentStaff.getPosition().toUpperCase().equals("ADMIN");
    }
}
