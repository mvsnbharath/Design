package com.company.model;

import java.util.Arrays;
import java.util.HashMap;

public class ParkingDisplayBoard {
    private String id;
    private int[] freeSpots;

    public ParkingDisplayBoard(String id, int[] freeSpots){
        this.id = id;
        this.freeSpots = freeSpots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ParkingDisplayBoard{" +
                "id='" + id + '\'' +
                ", Cars=" + Integer.toString(freeSpots[0]) +
                ", Vans=" + Integer.toString(freeSpots[1]) +
                ", Electric=" + Integer.toString(freeSpots[2]) +
                '}';
    }
}
