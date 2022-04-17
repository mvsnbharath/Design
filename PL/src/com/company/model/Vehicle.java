package com.company.model;

import com.company.Constants;

public class Vehicle {
    private String licenseNumber;
    private Constants.VehicleType vehicleType;
    private ParkingTicket parkingTicket;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public Constants.VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(Constants.VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public ParkingTicket getParkingTicket() {
        return parkingTicket;
    }

    public void setParkingTicket(ParkingTicket parkingTicket) {
        this.parkingTicket = parkingTicket;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "licenseNumber='" + licenseNumber + '\'' +
                ", vehicleType=" + vehicleType +
                ", parkingTicket=" + parkingTicket +
                '}';
    }
}
