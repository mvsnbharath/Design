package com.company.model;

import com.company.Constants;

import java.util.HashMap;
import java.util.Random;

public class ParkingLot {
    private String name;
    private int compactSpotCount = 0;
    private int largeSpotCount = 0;
    private int electricSpotCount = 0;
    private int maxCompactCount = 0;
    private int maxLargeCount = 0;
    private int maxElectricCount = 0;
//        private ParkingRate parkingRate;
//        private HashMap<String, EntrancePanel> entrancePanels;
//    private HashMap<String, ExitPanel> exitPanels;
    private HashMap<String, ParkingFloor> parkingFloors = new HashMap<>();

    public ParkingLot() {
        // Add 2 floors
        name = "My Parking Lot";
        ParkingFloor parkingFloor1 = new ParkingFloor("Floor1");
        updateCount(parkingFloor1);
//        System.out.println(toString());
        System.out.println("===Parking Floor 1 Created===");

        ParkingFloor parkingFloor2 = new ParkingFloor("Floor2");
        updateCount(parkingFloor2);
//        System.out.println(toString());
        System.out.println("===Parking Floor 2 Created===");

        parkingFloors.put(parkingFloor1.getName(), parkingFloor1);
        parkingFloors.put(parkingFloor2.getName(), parkingFloor2);

        displayBoard();
        System.out.println("===Parking Lot Created===");
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCompactSpotCount() {
        return compactSpotCount;
    }

    public void setCompactSpotCount(int compactSpotCount) {
        this.compactSpotCount = compactSpotCount;
    }

    public int getLargeSpotCount() {
        return largeSpotCount;
    }

    public void setLargeSpotCount(int largeSpotCount) {
        this.largeSpotCount = largeSpotCount;
    }

    public int getElectricSpotCount() {
        return electricSpotCount;
    }

    public void setElectricSpotCount(int electricSpotCount) {
        this.electricSpotCount = electricSpotCount;
    }

    public int getMaxCompactCount() {
        return maxCompactCount;
    }

    public int getMaxLargeCount() {
        return maxLargeCount;
    }

    public int getMaxElectricCount() {
        return maxElectricCount;
    }

    public HashMap<String, ParkingFloor> getParkingFloors() {
        return parkingFloors;
    }

    public void setParkingFloors(HashMap<String, ParkingFloor> parkingFloors) {
        this.parkingFloors = parkingFloors;
    }


    private void updateCount(ParkingFloor parkingFloor) {
        maxCompactCount += parkingFloor.getMaxSpots()[0];
        maxLargeCount += parkingFloor.getMaxSpots()[1];
        maxElectricCount += parkingFloor.getMaxSpots()[2];

//        compactSpotCount += parkingFloor.getMaxSpots()[0];
//        largeSpotCount += parkingFloor.getMaxSpots()[1];
//        electricSpotCount += parkingFloor.getMaxSpots()[2];
    }

    private boolean isFull(Constants.VehicleType vehicleType) {
        if (vehicleType == Constants.VehicleType.CAR) {
            return getCompactSpotCount() >= maxCompactCount;
        } else if (vehicleType == Constants.VehicleType.VAN) {
            return getLargeSpotCount() >= maxLargeCount;
        } else {
            return getElectricSpotCount() >= maxLargeCount;
        }

    }

    private void updateOverallSpotCount(Constants.VehicleType vehicleType, String updateType) {
        if (vehicleType == Constants.VehicleType.CAR) {
            if (updateType == "entrance"){
                compactSpotCount++;
            }else{
                compactSpotCount--;
            }
        } else if (vehicleType == Constants.VehicleType.VAN) {
            if (updateType == "entrance"){
                largeSpotCount++;
            }else{
                largeSpotCount--;
            }
        } else {
            if (updateType == "entrance"){
                electricSpotCount++;
            }else{
                electricSpotCount--;
            }
        }
    }

    public ParkingTicket getNewParkingTicket(Vehicle vehicle) throws Exception {
        if (isFull(vehicle.getVehicleType())) {
            throw new Exception("Parking is full for the Vehicle type: " + vehicle.getVehicleType());
        }
        String floor = getFreeFloor(vehicle.getVehicleType());
        ParkingTicket ticket = new ParkingTicket(floor);
        ticket.setFloor(floor);
        vehicle.setParkingTicket(ticket);

        //Overall count
        updateOverallSpotCount(vehicle.getVehicleType(), "entrance");
        //Floor Count
        updateParkingFloor(floor, vehicle.getVehicleType(), "entrance");
        displayBoard();
        return ticket;
    }

    public void payBill(Vehicle vehicle) {
        int bill = new Random().nextInt(10) ;
        ParkingTicket parkingTicket = vehicle.getParkingTicket();
        parkingTicket.setBill(bill);
        System.out.println(" Bill payed  by "+ vehicle.getLicenseNumber() + " = "+ Integer.toString(bill));
        String floor = parkingTicket.getFloor();

        //Overall count
        updateOverallSpotCount(vehicle.getVehicleType(), "exit");
        //FloorCount
        updateParkingFloor(floor, vehicle.getVehicleType(), "exit");
        displayBoard();
    }

    private void displayBoard() {
        for (String key : parkingFloors.keySet()) {
            ParkingFloor parkingFloor = parkingFloors.get(key);
            parkingFloor.showDisplayBoard();
        }

    }

    private void updateParkingFloor(String floor, Constants.VehicleType vehicleType, String updateType) {
        ParkingFloor parkingFloor = parkingFloors.get(floor);
        int[] freeSPots = parkingFloor.getFreeSpots();
        if (updateType == "entrance"){
            freeSPots[vehicleType.ordinal()] -= 1;
        }else{
            freeSPots[vehicleType.ordinal()] += 1;
        }
        parkingFloor.setFreeSpots(freeSPots);
        return;
    }

    private String
    getFreeFloor(Constants.VehicleType vehicleType) {
        for (String key : parkingFloors.keySet()) {
            ParkingFloor parkingFloor = parkingFloors.get(key);
            if (parkingFloor.getFreeSpots()[vehicleType.ordinal()] > 0) {
                return key;
            }
        }
        return "no Parking";
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "name='" + name + '\'' +
                ", compactSpotCount=" + compactSpotCount +
                ", largeSpotCount=" + largeSpotCount +
                ", electricSpotCount=" + electricSpotCount +
                ", maxCompactCount=" + maxCompactCount +
                ", maxLargeCount=" + maxLargeCount +
                ", maxElectricCount=" + maxElectricCount +
                '}';
    }
}
