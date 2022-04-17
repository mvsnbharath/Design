package com.company.model;

import java.util.Arrays;

public class ParkingFloor {
    private String name;
    private final int[] maxSpots = {10,10,10};
    private int[] freeSpots;
    private ParkingDisplayBoard displayBoard;

    public ParkingFloor(String name){
        this.name = name;
        freeSpots = new int[]{10,10,10};
        this.displayBoard = new ParkingDisplayBoard(name, freeSpots);
        System.out.println(toString());
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getMaxSpots() {
        return maxSpots;
    }

    public int[] getFreeSpots() {
        return freeSpots;
    }

    public void setFreeSpots(int[] freeSpots) {
        this.freeSpots = freeSpots;
    }

    public ParkingDisplayBoard getDisplayBoard() {
        return displayBoard;
    }

    public void setDisplayBoard(ParkingDisplayBoard displayBoard) {
        this.displayBoard = displayBoard;
    }

    public void showDisplayBoard(){
        System.out.println(toString());
        return;
    }

    @Override
    public String toString() {
        return "ParkingFloor{" +
                "name='" + name + '\'' +
                ", maxSpots=" + Arrays.toString(maxSpots) +
                ", freeSpots=" + Arrays.toString(freeSpots) +
                ", displayBoard=" + displayBoard +
                '}';
    }
}
