package com.hotelreservation;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.controller.MainController;
import com.hotelreservation.model.Staff;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Main application class for the Hotel Reservation System.
 * Responsible for launching the application and displaying views.
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static Stage primaryStage;

    /**
     * Starts the application by showing the login view.
     *
     * @param stage the primary stage for the application
     * @throws IOException if an error occurs while loading the view
     */
    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Starting Hotel Reservation System");
        primaryStage = stage;
        showLoginView();
    }

    /**
     * Displays the login view where staff members can authenticate.
     *
     * @throws IOException if an error occurs while loading the login view
     */
    public static void showLoginView() throws IOException {
        logger.info("Showing login view");
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/hotelreservation/view/login-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hotel Reservation System - Login");
        primaryStage.show();
    }

    /**
     * Displays the main view for authenticated staff.
     *
     * @param authenticatedStaff the staff member who has been authenticated
     * @throws IOException if an error occurs while loading the main view
     */
    public static void showMainView(Staff authenticatedStaff) throws IOException {
        logger.info("Showing main view for staff: {}", authenticatedStaff.getStaffId());
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/hotelreservation/view/main-view.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setCurrentStaff(authenticatedStaff);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hotel Reservation System");
        primaryStage.setFullScreen(true); // Set the stage to fullscreen
        primaryStage.show();
    }

    /**
     * Displays an error alert with the specified title and content.
     *
     * @param title the title of the alert
     * @param content the content of the alert
     */
    public static void showErrorAlert(String title, String content) {
        logger.error("Error alert: {} - {}", title, content);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays an informational alert with the specified title and content.
     *
     * @param title the title of the alert
     * @param content the content of the alert
     */
    public static void showInfoAlert(String title, String content) {
        logger.info("Info alert: {} - {}", title, content);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
