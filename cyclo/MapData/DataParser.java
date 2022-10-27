package com.dev.cyclo.MapData;

import androidx.annotation.NonNull;

import com.dev.cyclo.Logger;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * the parser called by the PointsParser which receives the json file and parse it in a RouteData object
 * @see PointsParser
 * @see RouteData
 */

public class DataParser {
    Logger logger;

    @NonNull
    public RouteData parse(@NonNull JSONObject jObject) {
        //create the first routeData
        RouteData routeData = new RouteData();
        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        JSONObject jBounds;
        JSONObject northeast,southwest;
        JSONObject start_location, end_location;

        logger = new Logger(this.getClass());

        try {
            //to understand the access to data in the JSON object, look at route1.json for instance
            //access to all attributes at the same level
            //routes element = all the alternative routes (no motorway ...) : only one for us with our http request
            jRoutes = jObject.getJSONArray("routes");
            jBounds=jRoutes.getJSONObject(0).getJSONObject("bounds");
            northeast = jBounds.getJSONObject("northeast");
            southwest = jBounds.getJSONObject("southwest");
            LatLng northeastLatLng = new LatLng(northeast.getDouble("lat"), northeast.getDouble("lng"));
            LatLng southwestLatLng = new LatLng( southwest.getDouble("lat"), southwest.getDouble("lng"));
            routeData.setBoundLatLngNE(northeastLatLng);
            routeData.setBoundLatLngSW(southwestLatLng);

            //for each route in routes (only one generally)
            for (int i = 0; i < jRoutes.length(); i++) {
                // take all the legs of one route. A leg is when you put an intermediary point,
                // you have a first leg to this point and a second leg from this point to the end
                jLegs = jRoutes.getJSONObject(i).getJSONArray("legs");
                String distanceTotalTxt = jLegs.getJSONObject(0).getJSONObject("distance").getString("text");
                double distanceTotal = Double.parseDouble((distanceTotalTxt.split(" "))[0]);
                routeData.setTotalDistance(distanceTotal);
                start_location = jLegs.getJSONObject(0).getJSONObject("start_location");
                LatLng startLoc = new LatLng(start_location.getDouble("lat"), start_location.getDouble("lng"));
                routeData.setOrigPosition(startLoc);
                end_location = jLegs.getJSONObject(0).getJSONObject("end_location");
                LatLng endLoc = new LatLng(end_location.getDouble("lat"), end_location.getDouble("lng"));
                routeData.setDestPosition(endLoc);

                //in each leg, there are steps : it is the atomic list of positions possible
                //it's composed of a list of positions, the distance of the step and a maneuver
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    for (int k = 0; k < jSteps.length(); k++) {
                        String distanceTxt = jSteps.getJSONObject(k).getJSONObject("distance").getString("text");
                        String[] distance = distanceTxt.split(" ");
                        double distanceStep = Double.parseDouble(distance[0]);
                        String unit = distance[1];
                        if (unit.equals("m")) {
                            distanceStep *= 0.001;
                        }
                        routeData.addStepDistance(distanceStep);
                        try {
                            //the first step haven't not a maneuver
                            String maneuver = jSteps.getJSONObject(k).getString("maneuver");
                            routeData.addStepManeuver(maneuver);
                        } catch (JSONException e) {
                            routeData.addStepManeuver("start");
                        }
                        //get the polyline (the encryption of all the positions of the step
                        String polyline = jSteps.getJSONObject(k).getJSONObject("polyline").getString("points");
                        //decode the polyline encrypted
                        ArrayList<LatLng> list = decodePoly(polyline);
                        routeData.addStepPositions(list);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routeData;
    }


    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private ArrayList<LatLng> decodePoly(String encoded) {

        ArrayList<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dint = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dint;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}