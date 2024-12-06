package com.hotelreservation.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the login view.
 */
public class LoginViewModel {
    private final StringProperty staffId = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();

    public String getStaffId() {
        return staffId.get();
    }

    public void setStaffId(String staffId) {
        this.staffId.set(staffId);
    }

    public StringProperty staffIdProperty() {
        return staffId;
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public StringProperty passwordProperty() {
        return password;
    }
}
