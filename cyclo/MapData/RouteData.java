package com.dev.cyclo.MapData;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

/** RouteData is the java model of the JSON file sent by Google Maps for the route between two locations
 * Enable the use of data more easily for every class
 * For static data */

public class RouteData {
    /** LatLng of NorthEast and SouthWest bounds of the route (useful for zoom position) */
    private LatLng boundLatLngNE, boundLatLngSW;
    /** ArrayList of one Arraylist of positions for each step */
    private final ArrayList<ArrayList<LatLng>> stepsPositionsList;
    /** ArrayList of the distance of each step in a route  */
    private final ArrayList<Double> stepsDistanceList;
    /** ArrayList of one polyline configured for each step */
    private final ArrayList<PolylineOptions> stepsPolylineList;
    /** Arraylist of maneuver for each step : how to move to take this step coming from the previous one
     * useful for future implementation of direction choose with swipes etc... */
    private ArrayList<String> stepsManeuverList;
    /** total Distance of the route */
    private double totalDistance = 0;
    private LatLng origPosition, destPosition;


    public RouteData() {
        this.stepsDistanceList = new ArrayList<>();
        this.stepsPolylineList = new ArrayList<>();
        this.stepsPositionsList = new ArrayList<>();
        this.stepsManeuverList = new ArrayList<>();
    }

    @NonNull
    public ArrayList<ArrayList<LatLng>> getStepsPositionsList() {
        return stepsPositionsList;
    }

    @NonNull
    public ArrayList<Double> getStepsDistanceList() {
        return stepsDistanceList;
    }

    @NonNull
    public ArrayList<PolylineOptions> getStepsPolylineList() {
        return stepsPolylineList;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    @NonNull
    public LatLng getBoundLatLngNE() {
        return boundLatLngNE;
    }

    public void setBoundLatLngNE(@NonNull LatLng boundLatLngNE) {
        this.boundLatLngNE = boundLatLngNE;
    }

    @NonNull
    public LatLng getBoundLatLngSW() {
        return boundLatLngSW;
    }

    public void setBoundLatLngSW(@NonNull LatLng boundLatLngSW) {
        this.boundLatLngSW = boundLatLngSW;
    }

    public void addStepDistance(double distance){
        this.stepsDistanceList.add(distance);
    }

    public void addStepPositions(@NonNull ArrayList<LatLng> positionList){
        this.stepsPositionsList.add(positionList);
    }

    public void addStepPolyline(@NonNull PolylineOptions poly){
        this.stepsPolylineList.add(poly);
    }

    @NonNull
    public LatLng getOrigPosition() {
        return origPosition;
    }

    public void setOrigPosition(@NonNull LatLng origPosition) {
        this.origPosition = origPosition;
    }

    @NonNull
    public LatLng getDestPosition() {
        return destPosition;
    }

    public void setDestPosition(@NonNull LatLng destPosition) {
        this.destPosition = destPosition;
    }

    public void addStepManeuver(@NonNull String maneuver){
        this.stepsManeuverList.add(maneuver);
    }

    @NonNull
    public ArrayList<String> getStepsManeuverList() {
        return stepsManeuverList;
    }

    public void setStepsManeuverList(@NonNull ArrayList<String> stepsManeuverList) {
        this.stepsManeuverList = stepsManeuverList;
    }
    /*selection of useful maneuvers to complete
    public void updateManeuverList(){
            // selection of only useful maneuvers
            //only some of them are really clear to understand and useful to make the app more interactive
            //example : https://stackoverflow.com/questions/17941812/google-directions-api
    }
    */
}