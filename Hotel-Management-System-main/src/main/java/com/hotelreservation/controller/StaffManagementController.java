package com.hotelreservation.controller;

import com.hotelreservation.model.Staff;
import com.hotelreservation.model.StaffShift;
import com.hotelreservation.service.StaffService;
import com.hotelreservation.service.ShiftService;
import com.hotelreservation.service.StaffLogService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaffManagementController {
    private static final Logger logger = LoggerFactory.getLogger(StaffManagementController.class);
    
    private final StaffService staffService = new StaffService();
    private final ShiftService shiftService = new ShiftService();
    private final StaffLogService staffLogService = new StaffLogService();

    @FXML private TableView<Staff> staffTable;
    @FXML private TableColumn<Staff, String> staffIdColumn;
    @FXML private TableColumn<Staff, String> nameColumn;
    @FXML private TableColumn<Staff, String> positionColumn;
    @FXML private TableColumn<Staff, String> currentShiftColumn;
    @FXML private TableColumn<Staff, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> positionFilter;
    @FXML private DatePicker shiftDate;
    @FXML private ComboBox<Staff> staffSelector;
    @FXML private ComboBox<String> shiftSelector;
    @FXML private TableView<StaffShift> shiftTable;
    @FXML private TitledPane passwordResetSection;
    @FXML private ComboBox<Staff> resetStaffSelector;

    private Staff currentStaff;
    private ObservableList<Staff> allStaff;
    private FilteredList<Staff> filteredStaff;
    private ObservableList<StaffShift> shifts;

    @FXML
    public void initialize() {
        // Check if user has admin/manager access
        if (!hasManagementAccess()) {
            showAccessDeniedAlert();
            return;
        }

        setupTableColumns();
        setupFilters();
        loadStaffData();
        setupShiftManagement();
        setupPasswordReset();
        loadShiftData();
    }

    private void handleEditStaff(Staff staff) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Edit Staff");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField(staff.getFirstName());
        TextField lastNameField = new TextField(staff.getLastName());
        TextField middleNameField = new TextField(staff.getMiddleName());
        ComboBox<String> positionField = new ComboBox<>(FXCollections.observableArrayList(
            "Manager", "Receptionist"
        ));
        positionField.setValue(staff.getPosition());

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Middle Name:"), 0, 2);
        grid.add(middleNameField, 1, 2);
        grid.add(new Label("Position:"), 0, 3);
        grid.add(positionField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                staff.setFirstName(firstNameField.getText());
                staff.setLastName(lastNameField.getText());
                staff.setMiddleName(middleNameField.getText());
                staff.setPosition(positionField.getValue());
                return staff;
            }
            return null;
        });

        Optional<Staff> result = dialog.showAndWait();
        result.ifPresent(updatedStaff -> {
            if (staffService.updateStaff(updatedStaff)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff updated successfully");
                loadStaffData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update staff");
            }
        });
    }

    private void handleDeleteStaff(Staff staff) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Staff");
        alert.setHeaderText("Delete Staff Member");
        alert.setContentText("Are you sure you want to delete " + staff.getFullName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (staffService.deleteStaff(staff.getStaffId())) {
                allStaff.remove(staff);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff deleted successfully");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete staff");
            }
        }
    }

    private void loadShiftData() {
        List<StaffShift> shiftList = shiftService.getShifts(LocalDate.now());
        shifts = FXCollections.observableArrayList(shiftList);
        shiftTable.setItems(shifts);
    }

    private void setupTableColumns() {
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        nameColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(
                () -> cellData.getValue().getFullName()
            )
        );
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        
        // Add action buttons
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setOnAction(e -> handleEditStaff(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleDeleteStaff(getTableView().getItems().get(getIndex())));
                
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupFilters() {
        positionFilter.setItems(FXCollections.observableArrayList(
            "All", "Manager", "Receptionist"
        ));
        positionFilter.setValue("All");

        // Add listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterStaff());
        positionFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterStaff());
    }

    private void loadStaffData() {
        allStaff = FXCollections.observableArrayList(staffService.getAllStaff());
        filteredStaff = new FilteredList<>(allStaff);
        staffTable.setItems(filteredStaff);
    }

    private void setupShiftManagement() {
        // Initialize shift types
        shiftSelector.setItems(FXCollections.observableArrayList(
            "Morning (6:00-14:00)", 
            "Afternoon (14:00-22:00)", 
            "Night (22:00-6:00)"
        ));

        // Initialize staff selector
        staffSelector.setItems(allStaff);
        staffSelector.setConverter(new javafx.util.StringConverter<Staff>() {
            @Override
            public String toString(Staff staff) {
                return staff != null ? staff.getFullName() : "";
            }

            @Override
            public Staff fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });

        // Set default date to today
        shiftDate.setValue(LocalDate.now());

        // Setup shift table columns
        TableColumn<StaffShift, String> shiftStaffColumn = new TableColumn<>("Staff");
        TableColumn<StaffShift, LocalDate> shiftDateColumn = new TableColumn<>("Date");
        TableColumn<StaffShift, LocalTime> startTimeColumn = new TableColumn<>("Start Time");
        TableColumn<StaffShift, LocalTime> endTimeColumn = new TableColumn<>("End Time");
        TableColumn<StaffShift, Void> shiftActionsColumn = new TableColumn<>("Actions");

        shiftStaffColumn.setCellValueFactory(cellData -> {
            Staff staff = staffService.getStaffById(cellData.getValue().getStaffId());
            return javafx.beans.binding.Bindings.createStringBinding(
                () -> staff != null ? staff.getFullName() : "Unknown"
            );
        });

        shiftDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Add action buttons to shift table
        shiftActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setOnAction(e -> {
                    StaffShift shift = getTableView().getItems().get(getIndex());
                    if (shiftService.deleteShift(shift.getShiftId())) {
                        loadShiftData();
                        showAlert(Alert.AlertType.INFORMATION, "Success", 
                                "Shift deleted successfully");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", 
                                "Failed to delete shift");
                    }
                });
                deleteButton.getStyleClass().add("delete-button");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        shiftTable.getColumns().setAll(
            shiftStaffColumn, shiftDateColumn, startTimeColumn, 
            endTimeColumn, shiftActionsColumn
        );
    }

    private void setupPasswordReset() {
        // Only show password reset section for admin
        passwordResetSection.setVisible(isAdmin());
        if (isAdmin()) {
            resetStaffSelector.setItems(allStaff);
            resetStaffSelector.setConverter(new javafx.util.StringConverter<Staff>() {
                @Override
                public String toString(Staff staff) {
                    return staff != null ? staff.getFullName() : "";
                }

                @Override
                public Staff fromString(String string) {
                    return null;
                }
            });
        }
    }

    private void filterStaff() {
        filteredStaff.setPredicate(staff -> {
            boolean matchesSearch = searchField.getText().isEmpty() ||
                staff.getFullName().toLowerCase().contains(searchField.getText().toLowerCase()) ||
                staff.getStaffId().toLowerCase().contains(searchField.getText().toLowerCase());

            boolean matchesPosition = positionFilter.getValue().equals("All") ||
                staff.getPosition().equals(positionFilter.getValue());

            return matchesSearch && matchesPosition;
        });
    }

    private boolean hasManagementAccess() {
        // Check if current user is admin or manager
        // This is a placeholder - implement actual role check
        return true;
    }

    private boolean isAdmin() {
        // Check if current user is admin
        // This is a placeholder - implement actual role check
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAccessDeniedAlert() {
        showAlert(Alert.AlertType.ERROR, "Access Denied", 
                 "You do not have permission to access staff management.");
    }

    @FXML
    private void showAddStaffDialog() {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Add New Staff");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField staffIdField = new TextField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        TextField middleNameField = new TextField();
        ComboBox<String> positionField = new ComboBox<>(FXCollections.observableArrayList(
            "Manager", "Receptionist"
        ));
        PasswordField passwordField = new PasswordField();

        grid.add(new Label("Staff ID:"), 0, 0);
        grid.add(staffIdField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Middle Name:"), 0, 3);
        grid.add(middleNameField, 1, 3);
        grid.add(new Label("Position:"), 0, 4);
        grid.add(positionField, 1, 4);
        grid.add(new Label("Password:"), 0, 5);
        grid.add(passwordField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Staff staff = new Staff();
                staff.setStaffId(staffIdField.getText());
                staff.setFirstName(firstNameField.getText());
                staff.setLastName(lastNameField.getText());
                staff.setMiddleName(middleNameField.getText());
                staff.setPosition(positionField.getValue());
                staff.setPassword(passwordField.getText());
                return staff;
            }
            return null;
        });

        Optional<Staff> result = dialog.showAndWait();
        result.ifPresent(staff -> {
            if (staffService.addStaff(staff)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Staff added successfully");
                loadStaffData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add staff");
            }
        });
    }

    @FXML
    private void handleSearch() {
        filterStaff();
    }

    @FXML
    private void handleAssignShift() {
        Staff selectedStaff = staffSelector.getValue();
        String selectedShift = shiftSelector.getValue();
        LocalDate selectedDate = shiftDate.getValue();

        if (selectedStaff == null || selectedShift == null || selectedDate == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select staff, shift, and date");
            return;
        }

        LocalTime startTime;
        LocalTime endTime;

        switch (selectedShift) {
            case "Morning (6:00-14:00)":
                startTime = LocalTime.of(6, 0);
                endTime = LocalTime.of(14, 0);
                break;
            case "Afternoon (14:00-22:00)":
                startTime = LocalTime.of(14, 0);
                endTime = LocalTime.of(22, 0);
                break;
            case "Night (22:00-6:00)":
                startTime = LocalTime.of(22, 0);
                endTime = LocalTime.of(6, 0);
                break;
            default:
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid shift selected");
                return;
        }

        StaffShift shift = new StaffShift();
        shift.setStaffId(selectedStaff.getStaffId());
        shift.setDate(selectedDate);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);

        if (shiftService.assignShift(shift)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Shift assigned successfully");
            loadShiftData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to assign shift");
        }
    }

    @FXML
    private void handleResetPassword() {
        Staff selectedStaff = resetStaffSelector.getValue();
        if (selectedStaff == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a staff member");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Reset Password");
        confirmation.setHeaderText("Reset Password for " + selectedStaff.getFullName());
        confirmation.setContentText("Are you sure you want to reset the password?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (staffService.resetPassword(selectedStaff.getStaffId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                    "Password has been reset to default");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to reset password");
            }
        }
    }

    /**
     * Sets the current staff member.
     *
     * @param staff The staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        // Store the current staff member for access control
        this.currentStaff = staff;
        
        // Update UI based on staff role
        if (staff != null) {
            boolean isAdmin = staff.getPosition().toUpperCase().equals("ADMIN");
            passwordResetSection.setVisible(isAdmin);
            passwordResetSection.setManaged(isAdmin);
        }
    }
}
