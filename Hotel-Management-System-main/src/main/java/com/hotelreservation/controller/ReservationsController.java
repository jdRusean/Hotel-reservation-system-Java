package com.hotelreservation.controller;

import com.hotelreservation.App;
import com.hotelreservation.model.Booking;
import com.hotelreservation.model.Guest;
import com.hotelreservation.model.Room;
import com.hotelreservation.model.Staff;
import com.hotelreservation.service.BookingService;
import com.hotelreservation.service.GuestService;
import com.hotelreservation.service.RoomService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for managing reservations.
 */
public class ReservationsController {
    private static final Logger logger = LoggerFactory.getLogger(ReservationsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TableView<Booking> reservationsTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> guestNameColumn;
    @FXML private TableColumn<Booking, String> roomNumberColumn;
    @FXML private TableColumn<Booking, LocalDate> checkInColumn;
    @FXML private TableColumn<Booking, LocalDate> checkOutColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Double> totalAmountColumn;
    @FXML private TableColumn<Booking, Void> actionsColumn;
    @FXML private Text totalReservationsText;
    @FXML private Text activeReservationsText;
    @FXML private Text todayCheckInsText;

    private Staff currentStaff;
    private final BookingService bookingService;
    private final GuestService guestService;
    private final RoomService roomService;
    private ObservableList<Booking> bookingsList;

    public ReservationsController() {
        this.bookingService = new BookingService();
        this.guestService = new GuestService();
        this.roomService = new RoomService();
    }

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        loadBookings();
        updateStats();
    }

    private void setupFilters() {
        // Setup status filter
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "Confirmed", "Checked In", "Checked Out", "Cancelled"
        ));
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> handleSearch());

        // Setup date filter
        dateFilter.setOnAction(e -> handleSearch());
    }

    private void setupTable() {
        // Initialize columns
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        guestNameColumn.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("finalAmount"));

        // Format date columns
        checkInColumn.setCellFactory(column -> new TableCell<>() {
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

        checkOutColumn.setCellFactory(column -> new TableCell<>() {
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
            private final Button checkInButton = new Button("Check In");
            private final Button checkOutButton = new Button("Check Out");
            private final Button cancelButton = new Button("Cancel");
            private final HBox buttons = new HBox(5);

            {
                checkInButton.getStyleClass().add("primary-button");
                checkOutButton.getStyleClass().add("primary-button");
                cancelButton.getStyleClass().add("danger-button");

                checkInButton.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    handleCheckIn(booking);
                });

                checkOutButton.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    handleCheckOut(booking);
                });

                cancelButton.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    handleCancelBooking(booking);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Booking booking = getTableView().getItems().get(getIndex());
                    buttons.getChildren().clear();

                    switch (booking.getStatus()) {
                        case "CONFIRMED":
                            if (booking.getCheckInDate().equals(LocalDate.now())) {
                                buttons.getChildren().addAll(checkInButton, cancelButton);
                            } else {
                                buttons.getChildren().add(cancelButton);
                            }
                            break;
                        case "CHECKED_IN":
                            buttons.getChildren().add(checkOutButton);
                            break;
                    }

                    buttons.setAlignment(Pos.CENTER);
                    setGraphic(buttons);
                }
            }
        });
    }

    /**
     * Sets the current staff member.
     *
     * @param staff the staff member to set
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        loadBookings();
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            bookingsList = FXCollections.observableArrayList(bookings);
            reservationsTable.setItems(bookingsList);
            updateStats();
        } catch (Exception e) {
            logger.error("Error loading bookings", e);
            App.showErrorAlert("Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        try {
            String searchText = searchField.getText();
            String status = statusFilter.getValue().equals("All") ? null : statusFilter.getValue().toUpperCase();
            LocalDate date = dateFilter.getValue();

            List<Booking> bookings = bookingService.searchBookings(searchText, status, date);
            bookingsList = FXCollections.observableArrayList(bookings);
            reservationsTable.setItems(bookingsList);
        } catch (Exception e) {
            logger.error("Error searching bookings", e);
            App.showErrorAlert("Error", "Failed to search bookings: " + e.getMessage());
        }
    }

    @FXML
    private void showNewReservationDialog() {
        Dialog<Booking> dialog = new Dialog<>();
        dialog.setTitle("New Reservation");
        dialog.setHeaderText(null);

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Add form fields
        ComboBox<Guest> guestComboBox = new ComboBox<>();
        ComboBox<Room> roomComboBox = new ComboBox<>();
        DatePicker checkInPicker = new DatePicker();
        DatePicker checkOutPicker = new DatePicker();
        TextField promoCodeField = new TextField();

        // Setup guest combo box
        List<Guest> guests = guestService.getAllGuests();
        guestComboBox.setItems(FXCollections.observableArrayList(guests));
        guestComboBox.setConverter(new StringConverter<Guest>() {
            @Override
            public String toString(Guest guest) {
                return guest == null ? "" : guest.getFirstName() + " " + guest.getLastName();
            }

            @Override
            public Guest fromString(String string) {
                return null;
            }
        });

        // Setup date pickers
        checkInPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        checkOutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate checkIn = checkInPicker.getValue();
                setDisable(empty || date.isBefore(LocalDate.now()) || 
                          (checkIn != null && date.isBefore(checkIn)));
            }
        });

        // Update available rooms when dates change
        checkInPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && checkOutPicker.getValue() != null) {
                updateAvailableRooms(roomComboBox, newVal, checkOutPicker.getValue());
            }
        });

        checkOutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && checkInPicker.getValue() != null) {
                updateAvailableRooms(roomComboBox, checkInPicker.getValue(), newVal);
            }
        });

        // Add fields to grid
        grid.add(new Label("Guest:"), 0, 0);
        grid.add(guestComboBox, 1, 0);
        grid.add(new Label("Check In:"), 0, 1);
        grid.add(checkInPicker, 1, 1);
        grid.add(new Label("Check Out:"), 0, 2);
        grid.add(checkOutPicker, 1, 2);
        grid.add(new Label("Room:"), 0, 3);
        grid.add(roomComboBox, 1, 3);
        grid.add(new Label("Promo Code:"), 0, 4);
        grid.add(promoCodeField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Guest guest = guestComboBox.getValue();
                Room room = roomComboBox.getValue();
                LocalDate checkIn = checkInPicker.getValue();
                LocalDate checkOut = checkOutPicker.getValue();
                String promoCode = promoCodeField.getText();

                if (guest == null || room == null || checkIn == null || checkOut == null) {
                    App.showErrorAlert("Error", "Please fill in all required fields");
                    return null;
                }

                // Calculate total amount
                long nights = checkIn.until(checkOut).getDays();
                double totalAmount = room.getRate() * nights;

                return new Booking(null, guest.getGuestId(), room.getRoomId(),
                        checkIn, checkOut, totalAmount, "CONFIRMED",
                        promoCode, 0.0, "");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(booking -> {
            try {
                boolean success = bookingService.createBooking(booking);
                if (success) {
                    loadBookings();
                    App.showInfoAlert("Success", "Reservation created successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to create reservation");
                }
            } catch (Exception e) {
                logger.error("Error creating booking", e);
                App.showErrorAlert("Error", "Failed to create reservation: " + e.getMessage());
            }
        });
    }

    private void updateAvailableRooms(ComboBox<Room> roomComboBox, LocalDate checkIn, LocalDate checkOut) {
        List<Room> availableRooms = roomService.getAvailableRooms(checkIn, checkOut);
        roomComboBox.setItems(FXCollections.observableArrayList(availableRooms));
        roomComboBox.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                return room == null ? "" : room.getDisplayString();
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });
    }

    private void handleCheckIn(Booking booking) {
        try {
            boolean success = bookingService.updateBookingStatus(booking.getBookingId(), "CHECKED_IN");
            if (success) {
                success = roomService.updateRoomStatus(booking.getRoomId(), "OCCUPIED");
                if (success) {
                    loadBookings();
                    App.showInfoAlert("Success", "Guest checked in successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to update room status");
                }
            } else {
                App.showErrorAlert("Error", "Failed to check in guest");
            }
        } catch (Exception e) {
            logger.error("Error checking in guest", e);
            App.showErrorAlert("Error", "Failed to check in guest: " + e.getMessage());
        }
    }

    private void handleCheckOut(Booking booking) {
        try {
            boolean success = bookingService.updateBookingStatus(booking.getBookingId(), "CHECKED_OUT");
            if (success) {
                success = roomService.updateRoomStatus(booking.getRoomId(), "AVAILABLE");
                if (success) {
                    loadBookings();
                    App.showInfoAlert("Success", "Guest checked out successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to update room status");
                }
            } else {
                App.showErrorAlert("Error", "Failed to check out guest");
            }
        } catch (Exception e) {
            logger.error("Error checking out guest", e);
            App.showErrorAlert("Error", "Failed to check out guest: " + e.getMessage());
        }
    }

    private void handleCancelBooking(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Reservation");
        alert.setHeaderText("Cancel Reservation");
        alert.setContentText("Are you sure you want to cancel this reservation?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean success = bookingService.updateBookingStatus(booking.getBookingId(), "CANCELLED");
                if (success) {
                    loadBookings();
                    App.showInfoAlert("Success", "Reservation cancelled successfully");
                } else {
                    App.showErrorAlert("Error", "Failed to cancel reservation");
                }
            } catch (Exception e) {
                logger.error("Error cancelling booking", e);
                App.showErrorAlert("Error", "Failed to cancel reservation: " + e.getMessage());
            }
        }
    }

    private void updateStats() {
        int[] stats = bookingService.getBookingStats();
        totalReservationsText.setText(String.valueOf(stats[0]));
        activeReservationsText.setText(String.valueOf(stats[1]));
        todayCheckInsText.setText(String.valueOf(stats[2]));
    }
}
