package com.hotelreservation.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotelreservation.App;
import com.hotelreservation.model.Booking;
import com.hotelreservation.model.Room;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.BookingService;
import com.hotelreservation.service.RoomService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Popup;

/**
 * Controller for the availability calendar view.
 */
public class AvailabilityController {
    private static final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    private static final int DAYS_TO_SHOW = 14; // Show two weeks by default

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> roomTypeFilter;
    @FXML private ComboBox<Integer> floorFilter;
    @FXML private GridPane calendarGrid;
    @FXML private Popup roomDetailsPopup;
    @FXML private Text roomNumberText;
    @FXML private Text roomTypeText;
    @FXML private Text roomStatusText;
    @FXML private Text guestNameText;
    @FXML private Text dateRangeText;

    private Staff currentStaff;
    private final RoomService roomService;
    private final BookingService bookingService;
    private Map<String, Room> rooms;
    private Map<String, List<Booking>> bookings;
    private LocalDate currentStartDate;

    public AvailabilityController() {
        this.roomService = new RoomService();
        this.bookingService = new BookingService();
        this.currentStartDate = LocalDate.now();
    }

    @FXML
    private void initialize() {
        setupDatePickers();
        setupFilters();
        loadData();
        updateCalendar();
    }

    private void setupDatePickers() {
        // Set minimum date to today for both date pickers
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate startDate = startDatePicker.getValue();
                setDisable(empty || date.isBefore(LocalDate.now()) || 
                          (startDate != null && date.isBefore(startDate)));
            }
        });

        // Set default values
        startDatePicker.setValue(currentStartDate);
        endDatePicker.setValue(currentStartDate.plusDays(DAYS_TO_SHOW - 1));

        // Update calendar when dates change
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentStartDate = newVal;
                updateCalendar();
            }
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCalendar();
            }
        });
    }

    private void setupFilters() {
        // Setup room type filter
        Set<String> roomTypes = new HashSet<>();
        List<Room> allRooms = roomService.getAllRooms();
        allRooms.forEach(room -> roomTypes.add(room.getType()));
        roomTypeFilter.setItems(FXCollections.observableArrayList(roomTypes));
        roomTypeFilter.getItems().add(0, "All Types");
        roomTypeFilter.setValue("All Types");
        roomTypeFilter.setOnAction(e -> updateCalendar());

        // Setup floor filter
        Set<Integer> floors = allRooms.stream()
                .map(Room::getFloor)
                .collect(Collectors.toSet());
        floorFilter.setItems(FXCollections.observableArrayList(floors));
        floorFilter.getItems().add(0, 0); // 0 means all floors
        floorFilter.setValue(0);
        floorFilter.setOnAction(e -> updateCalendar());
    }

    private void loadData() {
        try {
            // Load rooms
            List<Room> allRooms = roomService.getAllRooms();
            rooms = allRooms.stream()
                    .collect(Collectors.toMap(Room::getRoomId, room -> room));

            // Load bookings for the date range
            LocalDate endDate = endDatePicker.getValue();
            if (endDate == null) {
                endDate = currentStartDate.plusDays(DAYS_TO_SHOW - 1);
            }
            List<Booking> allBookings = bookingService.searchBookings(null, null, currentStartDate);
            bookings = allBookings.stream()
                    .collect(Collectors.groupingBy(Booking::getRoomId));
        } catch (Exception e) {
            logger.error("Error loading data", e);
            App.showErrorAlert("Error", "Failed to load calendar data: " + e.getMessage());
        }
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Add column constraints
        ColumnConstraints firstCol = new ColumnConstraints();
        firstCol.setMinWidth(100);
        firstCol.setPrefWidth(150);
        calendarGrid.getColumnConstraints().add(firstCol);

        for (int i = 0; i < DAYS_TO_SHOW; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(80);
            col.setPrefWidth(100);
            calendarGrid.getColumnConstraints().add(col);
        }

        // Add date headers
        for (int i = 0; i < DAYS_TO_SHOW; i++) {
            LocalDate date = currentStartDate.plusDays(i);
            Label dateLabel = new Label(date.format(DATE_FORMATTER));
            dateLabel.setAlignment(Pos.CENTER);
            dateLabel.setMaxWidth(Double.MAX_VALUE);
            dateLabel.getStyleClass().add("calendar-header");
            calendarGrid.add(dateLabel, i + 1, 0);
        }

        // Filter rooms
        List<Room> filteredRooms = rooms.values().stream()
                .filter(room -> roomTypeFilter.getValue().equals("All Types") || 
                              room.getType().equals(roomTypeFilter.getValue()))
                .filter(room -> floorFilter.getValue() == 0 || 
                              room.getFloor() == floorFilter.getValue())
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .collect(Collectors.toList());

        // Add room rows
        int row = 1;
        for (Room room : filteredRooms) {
            // Add room number
            Label roomLabel = new Label(room.getRoomNumber());
            roomLabel.setAlignment(Pos.CENTER_LEFT);
            roomLabel.setMaxWidth(Double.MAX_VALUE);
            roomLabel.getStyleClass().add("room-header");
            calendarGrid.add(roomLabel, 0, row);

            // Add status cells
            for (int col = 0; col < DAYS_TO_SHOW; col++) {
                LocalDate date = currentStartDate.plusDays(col);
                StackPane cell = createCalendarCell(room, date);
                calendarGrid.add(cell, col + 1, row);
            }
            row++;
        }
    }

    private StackPane createCalendarCell(Room room, LocalDate date) {
        StackPane cell = new StackPane();
        cell.getStyleClass().add("calendar-cell");

        // Determine cell status
        String status = getCellStatus(room, date);
        cell.getStyleClass().add(status.toLowerCase());

        // Add click handler
        cell.setOnMouseClicked(e -> showRoomDetails(room, date, e));

        return cell;
    }

    private String getCellStatus(Room room, LocalDate date) {
        // Check room status first
        if (!room.getStatus().equals("AVAILABLE")) {
            return room.getStatus();
        }

        // Check bookings
        List<Booking> roomBookings = bookings.get(room.getRoomId());
        if (roomBookings != null) {
            for (Booking booking : roomBookings) {
                if (!booking.getCheckOutDate().isBefore(date) && 
                    !booking.getCheckInDate().isAfter(date)) {
                    if (booking.getStatus().equals("CHECKED_IN")) {
                        return "OCCUPIED";
                    } else if (booking.getStatus().equals("CONFIRMED")) {
                        return "RESERVED";
                    }
                }
            }
        }

        return "AVAILABLE";
    }

    private void showRoomDetails(Room room, LocalDate date, MouseEvent event) {
        roomNumberText.setText("Room " + room.getRoomNumber());
        roomTypeText.setText(room.getType());
        roomStatusText.setText("Status: " + getCellStatus(room, date));

        // Get booking details if any
        List<Booking> roomBookings = bookings.get(room.getRoomId());
        if (roomBookings != null) {
            for (Booking booking : roomBookings) {
                if (!booking.getCheckOutDate().isBefore(date) && 
                    !booking.getCheckInDate().isAfter(date)) {
                    guestNameText.setText("Guest: " + booking.getGuestName());
                    dateRangeText.setText(String.format("Stay: %s - %s",
                            booking.getCheckInDate().format(DATE_FORMATTER),
                            booking.getCheckOutDate().format(DATE_FORMATTER)));
                    break;
                }
            }
        } else {
            guestNameText.setText("");
            dateRangeText.setText("");
        }

        // Show popup
        roomDetailsPopup.show(calendarGrid.getScene().getWindow(),
                event.getScreenX(), event.getScreenY());
    }

    @FXML
    private void closeRoomDetails() {
        roomDetailsPopup.hide();
    }

    @FXML
    private void goToToday() {
        currentStartDate = LocalDate.now();
        startDatePicker.setValue(currentStartDate);
        endDatePicker.setValue(currentStartDate.plusDays(DAYS_TO_SHOW - 1));
        updateCalendar();
    }

    /**
     * Sets the current staff member.
     *
     * @param staff the staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
    }
}
