package com.dev.cyclo.MapData;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class handle the parsing of the json object originally sent by google map API,
 * it receives the JSON from the FetchFile or FetchURL,
 * launches the DataParser which returns a RouteData object (it is the parser),
 * and finally modifies RouteData in order to set various PolyLines of different color to better see them
 * @see DataParser
 * @see RouteData
 */

public class PointsParser extends AsyncTask<String, Integer, RouteData> {
    private final TaskLoadedCallback taskCallback;
    private final Logger logger = new Logger(this.getClass());
    private RouteData routeData;

    public PointsParser(@NonNull Context mContext) {
        logger.log(Logger.Severity.Info, "PointsParser : Created", "[MAPDATA]");
        this.taskCallback = (TaskLoadedCallback) mContext;
    }

    // Parsing the data received from the FetchFile/FetchURL in background
    @NonNull
    @Override
    public RouteData doInBackground(@NonNull String... jsonData) {
        try {
            JSONObject jObject = new JSONObject(jsonData[0]);
            DataParser parser = new DataParser();       //new DataParser
            routeData = parser.parse(jObject);  // to traduce json file in RouteData object
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Logger.Severity.Error, "PointsParser : error DataParser : " +e, "[MAPDATA]");

        }
        return routeData;
    }

    // Executes after all and communicate with the onTaskDone of the context given : Ride
    @Override
    protected void onPostExecute(@NonNull RouteData routeData) {
        int color = Color.MAGENTA;  //starting color

        // Traversing through all the steps
        ArrayList<ArrayList<LatLng>> stepsPositionsList;
        try {
            // Taking the list of positions of each step
            stepsPositionsList = routeData.getStepsPositionsList();
            //Traversing through all the positions of the step
            for (ArrayList<LatLng> latLngs : stepsPositionsList) {
                //alternate the color to better see the different steps
                if (color == Color.MAGENTA) {
                    color = Color.BLUE;
                } else {
                    color = Color.MAGENTA;
                }
                //create an empty polyline
                PolylineOptions polyline = new PolylineOptions();
                polyline.addAll(latLngs);
                polyline.width(Main.config.getConfigData().getWidth_polyline());
                polyline.color(color);
                routeData.addStepPolyline(polyline);    //add the polyline to the routeData
            }
        }
        catch (Exception e){
            logger.log(Logger.Severity.Error,"PointsParser : Exception Error : " + e, "MAPDATA");
        }
        taskCallback.onTaskDone(routeData);     //return the routeData to the context : Ride
    }
}
