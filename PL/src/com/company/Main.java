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
        ParkingTicket nihaParkingTicket = parkingLot.getNewParkingTicket(niharikaVehicle);
        niharikaVehicle.setParkingTicket(nihaParkingTicket);
        System.out.println("Ticket generated for Vehicle : "+ niharikaVehicle.toString());

        //Vehicle 2 - Raja
        Vehicle rajaVehicle = new Vehicle();
        rajaVehicle.setVehicleType(Constants.VehicleType.CAR);
        rajaVehicle.setLicenseNumber("5678");

        System.out.println("Initiate Ticket generation");
        ParkingTicket rajaParkingTicket = parkingLot.getNewParkingTicket(rajaVehicle);
        rajaVehicle.setParkingTicket(rajaParkingTicket);
        System.out.println("Ticket generated for Vehicle : "+ rajaVehicle.toString());

        parkingLot.clearSpotAndPayBill(niharikaVehicle);
        parkingLot.clearSpotAndPayBill(rajaVehicle);
    }
}
