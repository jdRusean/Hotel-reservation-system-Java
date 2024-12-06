package com.hotelreservation.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.App;
import com.hotelreservation.model.Promo;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.PromoService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Controller for managing promotional offers.
 */
public class PromosController {
    private static final Logger logger = LoggerFactory.getLogger(PromosController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML private VBox createPromoSection;
    @FXML private Text subtitleText;
    @FXML private TextField promoCodeField;
    @FXML private TextArea descriptionField;
    @FXML private TextField discountField;
    @FXML private DatePicker validFromPicker;
    @FXML private DatePicker validUntilPicker;
    @FXML private TableView<Promo> promosTable;
    @FXML private TableColumn<Promo, String> codeColumn;
    @FXML private TableColumn<Promo, String> descriptionColumn;
    @FXML private TableColumn<Promo, Double> discountColumn;
    @FXML private TableColumn<Promo, LocalDate> validFromColumn;
    @FXML private TableColumn<Promo, LocalDate> validUntilColumn;
    @FXML private TableColumn<Promo, String> statusColumn;
    @FXML private TableColumn<Promo, Void> actionsColumn;

    private Staff currentStaff;
    private final PromoService promoService;
    private ObservableList<Promo> promosList;

    public PromosController() {
        this.promoService = new PromoService();
    }

    @FXML
    private void initialize() {
        setupDatePickers();
        setupTable();
        loadPromos();
    }

    private void setupDatePickers() {
        // Set minimum date to today for both date pickers
        validFromPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        validUntilPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = validFromPicker.getValue();
                setDisable(empty || date.isBefore(LocalDate.now()) || 
                          (startDate != null && date.isBefore(startDate)));
            }
        });

        // Update validUntil min date when validFrom changes
        validFromPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && validUntilPicker.getValue() != null && 
                validUntilPicker.getValue().isBefore(newVal)) {
                validUntilPicker.setValue(newVal);
            }
        });
    }

    private void setupTable() {
        // Initialize columns
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discountAmount"));
        validFromColumn.setCellValueFactory(new PropertyValueFactory<>("validFrom"));
        validUntilColumn.setCellValueFactory(new PropertyValueFactory<>("validUntil"));
        statusColumn.setCellValueFactory(data -> {
            String status = data.getValue().getStatus();
            return javafx.beans.binding.Bindings.createStringBinding(() -> status);
        });

        // Format date columns
        validFromColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER));
                }
            }
        });

        validUntilColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(DATE_FORMATTER));
                }
            }
        });

        // Setup actions column
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleButton = new Button();
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, toggleButton, deleteButton);

            {
                buttons.setAlignment(Pos.CENTER);
                toggleButton.getStyleClass().add("primary-button");
                deleteButton.getStyleClass().add("danger-button");

                toggleButton.setOnAction(event -> {
                    Promo promo = getTableView().getItems().get(getIndex());
                    handleTogglePromo(promo);
                });

                deleteButton.setOnAction(event -> {
                    Promo promo = getTableView().getItems().get(getIndex());
                    handleDeletePromo(promo);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Promo promo = getTableView().getItems().get(getIndex());
                    toggleButton.setText(promo.isActive() ? "Deactivate" : "Activate");
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Sets the current staff member and updates the UI accordingly.
     *
     * @param staff the staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        updateUIBasedOnRole();
        loadPromos();
    }

    private void updateUIBasedOnRole() {
        if (currentStaff != null) {
            boolean isAdmin = currentStaff.getPosition().toUpperCase().equals("ADMIN");
            
            // Show/hide create promo section based on role
            createPromoSection.setVisible(isAdmin);
            createPromoSection.setManaged(isAdmin);

            // Update subtitle text
            String roleText = isAdmin ? "Create and manage promotional offers" : "View promotional offers";
            subtitleText.setText(roleText);
        }
    }

    private void loadPromos() {
        try {
            List<Promo> promos = promoService.getAllPromos();
            promosList = FXCollections.observableArrayList(promos);
            promosTable.setItems(promosList);
        } catch (Exception e) {
            logger.error("Error loading promos", e);
            App.showErrorAlert("Error", "Failed to load promos: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreatePromo() {
        if (!validateInputs()) {
            return;
        }

        try {
            String code = promoCodeField.getText().trim();
            String description = descriptionField.getText().trim();
            double discount = Double.parseDouble(discountField.getText().trim());
            LocalDate validFrom = validFromPicker.getValue();
            LocalDate validUntil = validUntilPicker.getValue();

            Promo promo = new Promo(null, code, description, discount, validFrom, validUntil, true);
            boolean success = promoService.createPromo(promo);

            if (success) {
                clearInputs();
                loadPromos();
                App.showInfoAlert("Success", "Promo created successfully");
            } else {
                App.showErrorAlert("Error", "Failed to create promo");
            }
        } catch (NumberFormatException e) {
            App.showErrorAlert("Error", "Invalid discount amount");
        } catch (Exception e) {
            logger.error("Error creating promo", e);
            App.showErrorAlert("Error", "Failed to create promo: " + e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (promoCodeField.getText().trim().isEmpty()) {
            App.showErrorAlert("Error", "Promo code is required");
            return false;
        }

        if (descriptionField.getText().trim().isEmpty()) {
            App.showErrorAlert("Error", "Description is required");
            return false;
        }

        try {
            double discount = Double.parseDouble(discountField.getText().trim());
            if (discount <= 0) {
                App.showErrorAlert("Error", "Discount amount must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            App.showErrorAlert("Error", "Invalid discount amount");
            return false;
        }

        if (validFromPicker.getValue() == null) {
            App.showErrorAlert("Error", "Valid from date is required");
            return false;
        }

        if (validUntilPicker.getValue() == null) {
            App.showErrorAlert("Error", "Valid until date is required");
            return false;
        }

        if (validUntilPicker.getValue().isBefore(validFromPicker.getValue())) {
            App.showErrorAlert("Error", "Valid until date must be after valid from date");
            return false;
        }

        return true;
    }

    private void clearInputs() {
        promoCodeField.clear();
        descriptionField.clear();
        discountField.clear();
        validFromPicker.setValue(null);
        validUntilPicker.setValue(null);
    }

    private void handleTogglePromo(Promo promo) {
        try {
            boolean newStatus = !promo.isActive();
            boolean success = promoService.togglePromoStatus(promo.getPromoId(), newStatus);
            
            if (success) {
                loadPromos();
                String status = newStatus ? "activated" : "deactivated";
                App.showInfoAlert("Success", "Promo " + status + " successfully");
            } else {
                App.showErrorAlert("Error", "Failed to update promo status");
            }
        } catch (Exception e) {
            logger.error("Error toggling promo status", e);
            App.showErrorAlert("Error", "Failed to update promo status: " + e.getMessage());
        }
    }

    private void handleDeletePromo(Promo promo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Promo");
        alert.setHeaderText("Delete Promo");
        alert.setContentText("Are you sure you want to delete this promo?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean success = promoService.deletePromo(promo.getPromoId());
                if (success) {
                    loadPromos();
                    App.showInfoAlert("Success", "Promo deleted successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to delete promo");
                }
            } catch (Exception e) {
                logger.error("Error deleting promo", e);
                App.showErrorAlert("Error", "Failed to delete promo: " + e.getMessage());
            }
        }
    }
}
