package com.dev.cyclo.MapData.model;

/**
 * Waypoint object
 */
public class Waypoint {
    private final int id;
    private final String name;

    public Waypoint(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
