package com.dev.cyclo.ride;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.dev.cyclo.MapData.RouteData;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * this class enables the update of the position and the zoom
 * it calculates the precise position of the user with the proportional progress (distance)
 * by knowing the total distance of the route.
 * @see RouteData
 */

@SuppressLint("Registered")
public class Course extends Activity {
    private final Logger logger = new Logger(this.getClass());
    private final float tilt = Main.config.getConfigData().getAnimated_zoom_tilt();     //inclination of zoom
    //List of distance from the beginning for each step in steps = list of list of double
    private final ArrayList<ArrayList<Double>> positionInStepsCumulativeDist = new ArrayList<>();
    private RouteData routeData = null;
    private float bearing;      //the direction the user is heading towards for the zoom
    // for instance [[0,0.1 ,0.4],[0.4, 0.6, 1.4],[1.4, 1.9, 2.7]]

    public Course() {
    }

    /*Pre processing method which create the positionInStepsCumulativeDist which is for instance :
    [[0,0.1 ,0.4],[0.4, 0.6, 1.4],[1.4, 1.9, 2.7]] in order to know between which points the user is
    Then it calculates the precise total distance of the route
    */
    private void updateCumulativePositionsDistance(){
        for (int i = 0; i < this.routeData.getStepsPositionsList().size(); i++){
            //all the positions of the step i
            ArrayList<LatLng> positions = this.routeData.getStepsPositionsList().get(i);
            //cumulativeDistance from the beginning for this step
            ArrayList<Double> cumulativePositionsDist = new ArrayList<>();

            if (i == 0){    //if it is the first step, put 0 for the first element of the cumulativePositionsDist
                cumulativePositionsDist.add((double) 0);
            }
            else {      //for all other cases, the first cumulative distance is the last one of the previous step
                cumulativePositionsDist.add(positionInStepsCumulativeDist.get(i-1).get(positionInStepsCumulativeDist.get(i-1).size()-1));  //use last value to add on a non null value
            }
            //then for all others points : the cumulativeDistance of position i is
            // the cumulativeDistance of the previous position + the distance between them
            for (int k=1; k < positions.size(); k++){
                double distance = distanceBetweenTwoPosition(positions.get(k-1),positions.get(k));
                cumulativePositionsDist.add(distance + cumulativePositionsDist.get(k-1));
            }
            //finally you add the cumulativePositionsDist to the general data positionInStepsCumulativeDist
            positionInStepsCumulativeDist.add(cumulativePositionsDist);
        }
        //to conclude the pre processing calculations, recalculate the true total distance of the route
        int lastStepIndex = positionInStepsCumulativeDist.size()-1;
        int lastDistOfLastStepIndex = positionInStepsCumulativeDist.get(lastStepIndex).size()-1;
        double vectorCalculatedTotalDistance = positionInStepsCumulativeDist.get(lastStepIndex).get(lastDistOfLastStepIndex);
        routeData.setTotalDistance(vectorCalculatedTotalDistance);
    }

    /**
     * Method which takes the progress distance and calculate the proportional position in the route
     * Deduces the two positions between which the user is and calculates where it is between them (proportional)
     * @param progress calculated distance done by the current user
     * @return newCurrentPosition of the user
     */

    @NonNull
    public LatLng updatedNewPosition(double progress){
        int stepId,positionId;  //the step id in which the user is, the positionId he has just passed
        double lowBoundDistance;        //
        double distanceSegment;         //distance between the position he has just passed and the next one
        double shift;           //distance between the progress and the cumulativeDistance of the just passed position
        LatLng destPos, origPos;    //of the segment
        double longitudeCurrentPt, latitudeCurrentPt;
        int stepsNumber = positionInStepsCumulativeDist.size();

        int i=0, j=0;
        //Find the two cumulativeDistance between which the user progress is
        while ((i < positionInStepsCumulativeDist.size()-1)
                && !((positionInStepsCumulativeDist.get(i).get(0) < progress)
                && (progress < positionInStepsCumulativeDist.get(i).get(positionInStepsCumulativeDist.get(i).size()-1)))){
            i++;
        }
        stepId = i;     // the id of the step in which the user is
        //find the two cumulativeDistance in a step between which the user progress is
        while ((j < positionInStepsCumulativeDist.get(stepId).size()-1)
                && !((positionInStepsCumulativeDist.get(stepId).get(j) < progress)
                && (progress < positionInStepsCumulativeDist.get(stepId).get(j+1)))){
            j++;
        }
        //get the list of position of the step in which the user is
        ArrayList<LatLng> step = routeData.getStepsPositionsList().get(stepId);
        positionId = j;
        //get the position the user just passed
        lowBoundDistance = positionInStepsCumulativeDist.get(stepId).get(positionId);

        //if it is the last position of a step
        if (positionId == positionInStepsCumulativeDist.get(stepId).size()-1){
            //if it is the last step
            if (stepId == stepsNumber-1){
                int positionsNumber = positionInStepsCumulativeDist.get(stepId).size();
                //set the return position to the last one of the route (again and again at the end of the ride)
                return step.get(positionsNumber-1);
            }
            //if it is the last position of a step and there is a step after
            //calculate distance segment between this point and the next one (in the other step)
            distanceSegment = positionInStepsCumulativeDist.get(stepId+1).get(1)-lowBoundDistance;
            //set the next position after the one just passed
            destPos = routeData.getStepsPositionsList().get(stepId+1).get(positionId+1);
        }
        else {
            //if there is a next position in the same step
            //calculate distance segment between this point and the next one (in the same step)
            distanceSegment = positionInStepsCumulativeDist.get(stepId).get(positionId+1)-lowBoundDistance;
            //set the next position after the one just passed
            destPos = step.get(positionId+1);

        }
        shift = progress-lowBoundDistance;

        origPos = step.get(positionId);
        if (distanceSegment == 0){
            longitudeCurrentPt = origPos.longitude;
            latitudeCurrentPt = origPos.latitude;
        }
        else {
            longitudeCurrentPt = origPos.longitude + ((destPos.longitude - origPos.longitude) * shift) / distanceSegment;   //proportional
            latitudeCurrentPt = origPos.latitude + ((destPos.latitude - origPos.latitude) * shift) / distanceSegment;
        }
        //new position proportionally calculated
        LatLng currentPosition = new LatLng(latitudeCurrentPt,longitudeCurrentPt);

        //calculation processing to have the bearing for the zoom
        double dLon = (currentPosition.longitude-origPos.longitude);
        double y = Math.sin(dLon) * Math.cos(currentPosition.latitude);
        double x = Math.cos(origPos.latitude)*Math.sin(currentPosition.latitude) - Math.sin(origPos.latitude)*Math.cos(currentPosition.latitude)*Math.cos(dLon);
        double brng = Math.toDegrees((Math.atan2(y, x)));
        this.bearing =(float) brng;

        return currentPosition;
    }

    @NonNull
    public RouteData getRouteData() {
        return routeData;
    }

    public void setRouteData(@NonNull RouteData routeData) {
        this.routeData = routeData;
        //update the cumulative Position distance and set a more precise total distance in the routeData by the way
        updateCumulativePositionsDistance();
        }

    public float getBearing() {
        return bearing;
    }

    public float getTilt() {
        return tilt;
    }

    //found on web, useful to know distance between two points LatLng
    public double distanceBetweenTwoPosition(@NonNull LatLng StartP, @NonNull LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }
}
