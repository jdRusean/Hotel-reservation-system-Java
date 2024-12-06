package com.hotelreservation.controller;

import com.hotelreservation.App;
import com.hotelreservation.model.Notification;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.NotificationService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for managing notifications.
 */
public class NotificationsController {
    private static final Logger logger = LoggerFactory.getLogger(NotificationsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @FXML private VBox sendNotificationSection;
    @FXML private Text subtitleText;
    @FXML private TextArea messageField;
    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, String> dateColumn;
    @FXML private TableColumn<Notification, String> senderColumn;
    @FXML private TableColumn<Notification, String> messageColumn;
    @FXML private TableColumn<Notification, String> statusColumn;
    @FXML private TableColumn<Notification, Void> actionsColumn;

    private Staff currentStaff;
    private final NotificationService notificationService;
    private ObservableList<Notification> notificationsList;

    public NotificationsController() {
        this.notificationService = new NotificationService();
    }

    @FXML
    private void initialize() {
        setupTable();
    }

    /**
     * Sets the current staff member and updates the UI accordingly.
     *
     * @param staff the staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        updateUIBasedOnRole();
        loadNotifications();
    }

    private void setupTable() {
        // Initialize columns
        dateColumn.setCellValueFactory(data -> {
            String formattedDate = data.getValue().getCreatedAt().format(DATE_FORMATTER);
            return javafx.beans.binding.Bindings.createStringBinding(() -> formattedDate);
        });
        senderColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        statusColumn.setCellValueFactory(data -> {
            String status = data.getValue().isRead() ? "Read" : "Unread";
            return javafx.beans.binding.Bindings.createStringBinding(() -> status);
        });
        
        // Setup actions column
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button markReadButton = new Button("Mark as Read");
            private final HBox buttons = new HBox(5, markReadButton);

            {
                buttons.setAlignment(Pos.CENTER);
                markReadButton.getStyleClass().add("primary-button");

                markReadButton.setOnAction(event -> {
                    Notification notification = getTableView().getItems().get(getIndex());
                    handleMarkAsRead(notification);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()).isRead()) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void updateUIBasedOnRole() {
        if (currentStaff != null) {
            boolean isManagerOrAdmin = currentStaff.getPosition().toUpperCase().equals("ADMIN") ||
                                    currentStaff.getPosition().toUpperCase().equals("MANAGER");
            
            // Show/hide send notification section based on role
            sendNotificationSection.setVisible(isManagerOrAdmin);
            sendNotificationSection.setManaged(isManagerOrAdmin);

            // Update subtitle text
            String roleText = isManagerOrAdmin ? "Send and manage notifications" : "View notifications";
            subtitleText.setText(roleText);
        }
    }

    private void loadNotifications() {
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            notificationsList = FXCollections.observableArrayList(notifications);
            notificationsTable.setItems(notificationsList);
        } catch (Exception e) {
            logger.error("Error loading notifications", e);
            App.showErrorAlert("Error", "Failed to load notifications: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendNotification() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            App.showErrorAlert("Error", "Please enter a message");
            return;
        }

        try {
            boolean success = notificationService.sendNotification(message, currentStaff.getStaffId());
            if (success) {
                messageField.clear();
                loadNotifications(); // Refresh the table
                App.showInfoAlert("Success", "Notification sent successfully");
            } else {
                App.showErrorAlert("Error", "Failed to send notification");
            }
        } catch (Exception e) {
            logger.error("Error sending notification", e);
            App.showErrorAlert("Error", "Failed to send notification: " + e.getMessage());
        }
    }

    private void handleMarkAsRead(Notification notification) {
        try {
            boolean success = notificationService.markAsRead(notification.getNotificationId());
            if (success) {
                loadNotifications(); // Refresh the table
            } else {
                App.showErrorAlert("Error", "Failed to mark notification as read");
            }
        } catch (Exception e) {
            logger.error("Error marking notification as read", e);
            App.showErrorAlert("Error", "Failed to mark notification as read: " + e.getMessage());
        }
    }
}
