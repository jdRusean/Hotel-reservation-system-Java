package com.hotelreservation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Settings;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.SettingsService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

public class SettingsController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
    
    private final SettingsService settingsService = new SettingsService();

    @FXML private ToggleButton darkModeToggle;
    @FXML private ComboBox<String> resolutionComboBox;

    private Staff currentStaff;
    private Settings currentSettings;

    @FXML
    public void initialize() {
        setupResolutionOptions();
        loadCurrentSettings();
        setupListeners();
    }

    private void setupResolutionOptions() {
        resolutionComboBox.setItems(FXCollections.observableArrayList(
            "1920x1080",
            "1600x900",
            "1366x768",
            "1280x720"
        ));
    }

    private void loadCurrentSettings() {
        if (currentStaff != null) {
            currentSettings = settingsService.loadSettings(currentStaff.getStaffId());
            if (currentSettings != null) {
                darkModeToggle.setSelected(currentSettings.isDarkMode());
                resolutionComboBox.setValue(currentSettings.getResolution());
                applySettings(currentSettings);
            } else {
                // Default settings
                currentSettings = new Settings(false, "1366x768", currentStaff.getStaffId());
                settingsService.saveSettings(currentSettings);
                // Apply default settings
                darkModeToggle.setSelected(false);
                resolutionComboBox.setValue("1366x768");
            }
        } else {
            // Set default values when no staff is set
            darkModeToggle.setSelected(false);
            resolutionComboBox.setValue("1366x768");
            logger.info("No staff set, using default settings");
        }
    }

    private void setupListeners() {
        // Apply dark mode immediately when toggled
        darkModeToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            applyDarkMode(newVal);
            if (currentSettings != null) {
                currentSettings.setDarkMode(newVal);
                saveSettings();
            }
        });

        // Apply resolution when changed
        resolutionComboBox.setOnAction(e -> {
            String resolution = resolutionComboBox.getValue();
            applyResolution(resolution);
            if (currentSettings != null) {
                currentSettings.setResolution(resolution);
                saveSettings();
            }
            showRestartDialog();
        });
    }

    private void applySettings(Settings settings) {
        applyDarkMode(settings.isDarkMode());
        applyResolution(settings.getResolution());
    }

    private void applyDarkMode(boolean isDarkMode) {
        Scene scene = darkModeToggle.getScene();
        if (scene != null) {
            String theme = isDarkMode ? "dark" : "light";
            String css = getClass().getResource("/com/hotelreservation/styles/" + theme + ".css").toExternalForm();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(css);
        }
    }

    private void applyResolution(String resolution) {
        Stage stage = (Stage) resolutionComboBox.getScene().getWindow();
        if (stage != null) {
            String[] dimensions = resolution.split("x");
            if (dimensions.length == 2) {
                try {
                    double width = Double.parseDouble(dimensions[0]);
                    double height = Double.parseDouble(dimensions[1]);
                    stage.setWidth(width);
                    stage.setHeight(height);
                    stage.centerOnScreen();
                } catch (NumberFormatException e) {
                    logger.error("Invalid resolution format: " + resolution, e);
                }
            }
        }
    }

    private void saveSettings() {
        if (currentSettings != null) {
            if (!settingsService.saveSettings(currentSettings)) {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to save settings");
            }
        }
    }

    private void showRestartDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resolution Changed");
        alert.setHeaderText(null);
        alert.setContentText("The resolution will be applied when you restart the application.");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets the current staff member and loads their settings.
     *
     * @param staff The staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        loadCurrentSettings();
    }

    @FXML
    private void handleSaveSettings() {
        boolean isDarkMode = darkModeToggle.isSelected();
        String resolution = resolutionComboBox.getValue();
        
        if (currentStaff != null) {
            if (currentSettings == null) {
                currentSettings = new Settings(isDarkMode, resolution, currentStaff.getStaffId());
            } else {
                currentSettings.setDarkMode(isDarkMode);
                currentSettings.setResolution(resolution);
            }
            
            if (settingsService.saveSettings(currentSettings)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Settings saved successfully");
                // Apply settings immediately
                applyDarkMode(isDarkMode);
                applyResolution(resolution);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Failed to save settings");
            }
        } else {
            // Even without a staff member, we can still apply the settings temporarily
            applyDarkMode(isDarkMode);
            applyResolution(resolution);
            logger.info("Applied settings without saving (no staff member)");
        }
    }
}
