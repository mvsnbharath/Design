package com.company.model;

import java.time.Instant;
import java.util.Random;

public class ParkingTicket {
    private Instant timestamp;
    private String floor;
    private int bill;

    public ParkingTicket(String floor) {
        this.timestamp = Instant.now();
        this.bill = 0;
        this.floor = floor;
        System.out.println(toString());
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getBill() {
        return bill;
    }

    public void setBill(int bill) {
        this.bill = bill;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }


    public void payBill(Vehicle vehicle) {
        int bill = new Random().nextInt(10) ;
        ParkingTicket parkingTicket = vehicle.getParkingTicket();
        parkingTicket.setBill(bill);
        System.out.println(" Bill payed  by "+ vehicle.getLicenseNumber() + " = "+ Integer.toString(bill));
    }

    @Override
    public String toString() {
        return "ParkingTicket{" +
                "timestamp=" + timestamp +
                ", floor='" + floor + '\'' +
                ", bill=" + bill +
                '}';
    }
}
