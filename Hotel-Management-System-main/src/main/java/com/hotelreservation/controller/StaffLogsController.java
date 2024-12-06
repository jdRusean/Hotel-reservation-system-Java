package com.hotelreservation.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.model.Staff;
import com.hotelreservation.model.StaffLog;
import com.hotelreservation.service.StaffLogService;
import com.hotelreservation.service.StaffService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class StaffLogsController {
    private static final Logger logger = LoggerFactory.getLogger(StaffLogsController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StaffLogService staffLogService = new StaffLogService();
    private final StaffService staffService = new StaffService();

    @FXML private TableView<StaffLog> logsTable;
    @FXML private TableColumn<StaffLog, LocalDateTime> timestampColumn;
    @FXML private TableColumn<StaffLog, String> staffIdColumn;
    @FXML private TableColumn<StaffLog, String> staffNameColumn;
    @FXML private TableColumn<StaffLog, String> actionColumn;
    @FXML private TableColumn<StaffLog, String> detailsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> actionFilter;
    @FXML private ComboBox<Staff> staffFilter;
    @FXML private ComboBox<String> filterPeriod;
    @FXML private Pagination logsPagination;

    private Staff currentStaff;
    private ObservableList<StaffLog> allLogs;
    private FilteredList<StaffLog> filteredLogs;

    @FXML
    public void initialize() {
        if (!isAdmin()) {
            showAccessDeniedAlert();
            return;
        }

        setupTableColumns();
        setupFilters();
        loadLogs();
        setupPagination();
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));

        // Format timestamp
        timestampColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DATE_TIME_FORMATTER.format(item));
                }
            }
        });

        // Show staff name
        staffNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    StaffLog log = getTableView().getItems().get(getIndex());
                    Staff staff = staffService.getStaffById(log.getStaffId());
                    setText(staff != null ? staff.getFullName() : "Unknown");
                }
            }
        });
    }

    private void setupFilters() {
        // Time period options
        filterPeriod.setItems(FXCollections.observableArrayList(
            "Today", "Last 7 Days", "Last 30 Days", "All Time"
        ));

        // Action type options
        actionFilter.setItems(FXCollections.observableArrayList(
            "Login", "Logout", "Password Reset", "Shift Change", "All"
        ));

        // Staff filter options
        List<Staff> staffList = staffService.getAllStaff();
        ObservableList<Staff> staffOptions = FXCollections.observableArrayList(staffList);
        staffFilter.setItems(staffOptions);
        staffFilter.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Staff staff) {
                return staff != null ? staff.getFullName() : "All Staff";
            }

            @Override
            public Staff fromString(String string) {
                return null;
            }
        });

        // Add listeners
        filterPeriod.setOnAction(e -> applyFilters());
        actionFilter.setOnAction(e -> applyFilters());
        staffFilter.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadLogs() {
        allLogs = FXCollections.observableArrayList(staffLogService.getAllStaffLogs());
        filteredLogs = new FilteredList<>(allLogs);
        logsTable.setItems(filteredLogs);
    }

    private void setupPagination() {
        int pageCount = (filteredLogs.size() + 19) / 20; // 20 items per page
        logsPagination.setPageCount(pageCount);
        logsPagination.setCurrentPageIndex(0);
        logsPagination.setPageFactory(this::createPage);
    }

    private TableView<StaffLog> createPage(int pageIndex) {
        int fromIndex = pageIndex * 20;
        int toIndex = Math.min(fromIndex + 20, filteredLogs.size());
        logsTable.setItems(FXCollections.observableArrayList(
            filteredLogs.subList(fromIndex, toIndex)));
        return logsTable;
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleExport() {
        // TODO: Implement export functionality
    }

    private void applyFilters() {
        filteredLogs.setPredicate(log -> {
            boolean matchesSearch = searchField.getText().isEmpty() ||
                log.getAction().toLowerCase().contains(searchField.getText().toLowerCase()) ||
                log.getDetails().toLowerCase().contains(searchField.getText().toLowerCase());

            boolean matchesAction = actionFilter.getValue() == null ||
                actionFilter.getValue().equals("All") ||
                log.getAction().equals(actionFilter.getValue());

            boolean matchesStaff = staffFilter.getValue() == null ||
                log.getStaffId().equals(staffFilter.getValue().getStaffId());

            boolean matchesPeriod = filterPeriod.getValue() == null ||
                isWithinSelectedPeriod(log.getTimestamp());

            return matchesSearch && matchesAction && matchesStaff && matchesPeriod;
        });

        setupPagination();
    }

    private boolean isWithinSelectedPeriod(LocalDateTime timestamp) {
        if (filterPeriod.getValue() == null) return true;
        
        LocalDateTime now = LocalDateTime.now();
        return switch (filterPeriod.getValue()) {
            case "Today" -> timestamp.toLocalDate().equals(now.toLocalDate());
            case "Last 7 Days" -> timestamp.isAfter(now.minusDays(7));
            case "Last 30 Days" -> timestamp.isAfter(now.minusDays(30));
            default -> true;
        };
    }

    private boolean isAdmin() {
        return currentStaff != null && 
               currentStaff.getPosition().toUpperCase().equals("ADMIN");
    }

    private void showAccessDeniedAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Access Denied");
        alert.setHeaderText(null);
        alert.setContentText("You do not have permission to view staff logs.");
        alert.showAndWait();
    }

    /**
     * Sets the current staff member.
     *
     * @param staff The staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        
        // Reload if user has admin access
        if (isAdmin()) {
            loadLogs();
        }
    }
}
