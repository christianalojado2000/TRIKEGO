package com.example.trike;

public class Booking {
    private String passengerName;
    private String phoneNumber;
    private String guardianNumber;
    private String pickupAddress;
    private String location;
    private String driverName;
    private String status;
    private Object eta;
    private String bookingId;
    private String terminal;

    public Booking() {

    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getGuardianNumber() {
        return guardianNumber;
    }

    public void setGuardianNumber(String guardianNumber) {
        this.guardianNumber = guardianNumber;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getEta() {
        return eta != null ? eta.toString() : "N/A";
    }

    public void setEta(Object eta) {
        this.eta = eta;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }
}
