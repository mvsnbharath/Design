package com.company;

import com.company.model.ParkingLot;
import com.company.model.ParkingTicket;
import com.company.model.Vehicle;

public class Main {

    public static void main(String[] args) throws Exception {

        ParkingLot parkingLot = new ParkingLot();
        
        //Vehicle 1 - Niharika
        Vehicle niharikaVehicle = new Vehicle();
        niharikaVehicle.setVehicleType(Constants.VehicleType.CAR);
        niharikaVehicle.setLicenseNumber("1234");

        System.out.println("Initiate Ticket generation");
        ParkingTicket parkingTicket = parkingLot.getNewParkingTicket(niharikaVehicle);
        niharikaVehicle.setParkingTicket(parkingTicket);
        System.out.println("Ticket generated for Vehicle : "+ niharikaVehicle.toString());

        //Vehicle 2 - Raja
        Vehicle rajaVehicle = new Vehicle();
        rajaVehicle.setVehicleType(Constants.VehicleType.CAR);
        rajaVehicle.setLicenseNumber("5678");

        System.out.println("Initiate Ticket generation");
        ParkingTicket parkingTicket2 = parkingLot.getNewParkingTicket(rajaVehicle);
        rajaVehicle.setParkingTicket(parkingTicket2);
        System.out.println("Ticket generated for Vehicle : "+ rajaVehicle.toString());

        parkingLot.payBill(niharikaVehicle);
        parkingLot.payBill(rajaVehicle);
    }
}
