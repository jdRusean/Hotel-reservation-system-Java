package com.hotelreservation.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.App;
import com.hotelreservation.model.Guest;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.GuestService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Controller for managing guests.
 */
public class ManageGuestsController {
    private static final Logger logger = LoggerFactory.getLogger(ManageGuestsController.class);

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortByComboBox;
    @FXML private TableView<Guest> guestsTable;
    @FXML private TableColumn<Guest, String> guestIdColumn;
    @FXML private TableColumn<Guest, String> firstNameColumn;
    @FXML private TableColumn<Guest, String> lastNameColumn;
    @FXML private TableColumn<Guest, String> contactNumberColumn;
    @FXML private TableColumn<Guest, Void> actionsColumn;

    private Staff currentStaff;
    private final GuestService guestService;
    private ObservableList<Guest> guestsList;
    private FilteredList<Guest> filteredGuests;

    /**
     * Sets the current staff member.
     *
     * @param staff the staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        // Refresh the table after setting staff
        loadGuests();
    }

    public ManageGuestsController() {
        this.guestService = new GuestService();
    }

    @FXML
    private void initialize() {
        setupTable();
        setupSortComboBox();
        setupSearch();
        loadGuests();
    }

    private void setupTable() {
        // Initialize columns
        guestIdColumn.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        contactNumberColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));
        
        // Setup actions column
        setupActionsColumn();

        // Enable table selection
        guestsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                buttons.setAlignment(Pos.CENTER);
                editButton.getStyleClass().add("primary-button");
                deleteButton.getStyleClass().add("danger-button");

                editButton.setOnAction(event -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    showEditGuestDialog(guest);
                });

                deleteButton.setOnAction(event -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    handleDeleteGuest(guest);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    private void setupSortComboBox() {
        sortByComboBox.setItems(FXCollections.observableArrayList(
            "Guest ID", "First Name", "Last Name", "Contact Number"
        ));
        sortByComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> handleSort(newValue)
        );
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (filteredGuests != null) {
                filteredGuests.setPredicate(guest -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    String lowerCaseFilter = newValue.toLowerCase();
                    return guest.getGuestId().toLowerCase().contains(lowerCaseFilter)
                        || guest.getFirstName().toLowerCase().contains(lowerCaseFilter)
                        || guest.getLastName().toLowerCase().contains(lowerCaseFilter)
                        || guest.getContactNumber().toLowerCase().contains(lowerCaseFilter);
                });
            }
        });
    }

    private void loadGuests() {
        try {
            List<Guest> guests = guestService.getAllGuests();
            guestsList = FXCollections.observableArrayList(guests);
            filteredGuests = new FilteredList<>(guestsList, p -> true);
            SortedList<Guest> sortedGuests = new SortedList<>(filteredGuests);
            sortedGuests.comparatorProperty().bind(guestsTable.comparatorProperty());
            guestsTable.setItems(sortedGuests);
        } catch (Exception e) {
            logger.error("Error loading guests", e);
            App.showErrorAlert("Error", "Failed to load guests: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        // Search is handled automatically through the FilteredList
        String searchText = searchField.getText();
        logger.info("Searching for guests with query: {}", searchText);
    }

    private void handleSort(String sortBy) {
        if (sortBy == null) return;

        guestsTable.getSortOrder().clear();
        switch (sortBy) {
            case "Guest ID":
                guestsTable.getSortOrder().add(guestIdColumn);
                break;
            case "First Name":
                guestsTable.getSortOrder().add(firstNameColumn);
                break;
            case "Last Name":
                guestsTable.getSortOrder().add(lastNameColumn);
                break;
            case "Contact Number":
                guestsTable.getSortOrder().add(contactNumberColumn);
                break;
        }
    }

    @FXML
    private void showAddGuestDialog() {
        Dialog<Guest> dialog = createGuestDialog(null);
        Optional<Guest> result = dialog.showAndWait();
        
        result.ifPresent(guest -> {
            try {
                boolean success = guestService.addGuest(guest);
                if (success) {
                    loadGuests(); // Refresh the table
                    App.showInfoAlert("Success", "Guest added successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to add guest");
                }
            } catch (Exception e) {
                logger.error("Error adding guest", e);
                App.showErrorAlert("Error", "Failed to add guest: " + e.getMessage());
            }
        });
    }

    private void showEditGuestDialog(Guest guest) {
        Dialog<Guest> dialog = createGuestDialog(guest);
        Optional<Guest> result = dialog.showAndWait();
        
        result.ifPresent(updatedGuest -> {
            try {
                boolean success = guestService.updateGuest(updatedGuest);
                if (success) {
                    loadGuests(); // Refresh the table
                    App.showInfoAlert("Success", "Guest updated successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to update guest");
                }
            } catch (Exception e) {
                logger.error("Error updating guest", e);
                App.showErrorAlert("Error", "Failed to update guest: " + e.getMessage());
            }
        });
    }

    private Dialog<Guest> createGuestDialog(Guest guest) {
        Dialog<Guest> dialog = new Dialog<>();
        dialog.setTitle(guest == null ? "Add New Guest" : "Edit Guest");
        dialog.setHeaderText(null);

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField contactNumberField = new TextField();

        if (guest != null) {
            firstNameField.setText(guest.getFirstName());
            lastNameField.setText(guest.getLastName());
            contactNumberField.setText(guest.getContactNumber());
        }

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Contact Number:"), 0, 2);
        grid.add(contactNumberField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Guest newGuest = guest == null ? new Guest() : guest;
                newGuest.setFirstName(firstNameField.getText());
                newGuest.setLastName(lastNameField.getText());
                newGuest.setContactNumber(contactNumberField.getText());
                return newGuest;
            }
            return null;
        });

        return dialog;
    }

    private void handleDeleteGuest(Guest guest) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Guest");
        alert.setHeaderText("Delete Guest");
        alert.setContentText("Are you sure you want to delete this guest?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = guestService.deleteGuest(guest.getGuestId());
                if (success) {
                    loadGuests(); // Refresh the table
                    App.showInfoAlert("Success", "Guest deleted successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to delete guest");
                }
            } catch (Exception e) {
                logger.error("Error deleting guest", e);
                App.showErrorAlert("Error", "Failed to delete guest: " + e.getMessage());
            }
        }
    }
}
