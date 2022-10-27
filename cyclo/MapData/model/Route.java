package com.dev.cyclo.MapData.model;

import androidx.annotation.NonNull;

/** Route is the short resume of one of our predefined routes, it is used for the Routes overview
 * It has an id (int), distance (double), Waypoint id of the start and end locations
 * @see Waypoint
 * and in order to have more complete data, attributes Waypoint of the start and the end locations are settable (optional)
 */
public class Route {
    private final int id;
    private final int startWPid;
    private final int endWPid;
    private Waypoint startWP;
    private Waypoint endWP;
    private final double distance;

    public Route(int id, int startWPid, int endWPid, double distance) {
        this.id = id;
        this.startWPid = startWPid;
        this.endWPid = endWPid;
        this.distance = distance;

    }

    public int getId() {
        return id;
    }

    public int getStartWPid() {
        return startWPid;
    }

    public int getEndWPid() {
        return endWPid;
    }

    @NonNull
    public Waypoint getStartWP() {
        return startWP;
    }

    public void setStartWP(@NonNull Waypoint startWP) {
        this.startWP = startWP;
    }

    @NonNull
    public Waypoint getEndWP() {
        return endWP;
    }

    public void setEndWP(@NonNull Waypoint endWP) {
        this.endWP = endWP;
    }

    public double getDistance() {
        return distance;
    }
}
