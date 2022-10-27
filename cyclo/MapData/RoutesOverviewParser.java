package com.dev.cyclo.MapData;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.dev.cyclo.Logger;
import com.dev.cyclo.MapData.model.Route;
import com.dev.cyclo.MapData.model.Waypoint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * RoutesOverview file parser : get the route overview json file in asset directory
 * Parse the JSON and join data to create Java Waypoint, Route objects and return a list of Route useful for rideChoice
 * @see Waypoint
 * @see Route
 */

public class RoutesOverviewParser extends AsyncTask<String, Integer, ArrayList<Route>> {
    private final TaskLoadedCallback taskCallback;
    private final Logger logger;
    private final ArrayList<Waypoint> waypoints = new ArrayList<>();
    private final ArrayList<Route> routes = new ArrayList<>();

    public RoutesOverviewParser(@NonNull Context mContext) {
        this.logger = new Logger(this.getClass());
        logger.log(Logger.Severity.Info, "RoutesOverviewParser : Created", "[MapData]");
        this.taskCallback = (TaskLoadedCallback) mContext;
    }

    // executed with the .execute(String jsonData)
    // Parsing the data in background
    @NonNull
    @Override
    protected ArrayList<Route> doInBackground(@NonNull String... jsonData) {
        JSONArray jRoutes;
        JSONArray jWaypoints;
        JSONObject jWaypoint;
        JSONObject jRoute;

        try {
            JSONObject jObject = new JSONObject(jsonData[0]);
            //load the waypoints in a JSONArray
            jWaypoints = jObject.getJSONArray("waypoints");
            //load the routes in a JSONArray
            jRoutes = jObject.getJSONArray("routes");
            //Complete a list of Waypoint called this.waypoints with all the waypoints in the JSONArray jWaypoints
            for (int i = 0; i < jWaypoints.length(); i++) {
                jWaypoint = (JSONObject) jWaypoints.get(i); //begin at 1 to 10
                Waypoint wp = new Waypoint((int) jWaypoint.get("id"),(String) jWaypoint.get("name"));
                this.waypoints.add(wp);
            }
            //Complete a list of Route called this.routes with all the routes in the JSONArray  jRoutes
            for (int i = 0; i < jRoutes.length(); i++) {
                jRoute = (JSONObject) jRoutes.get(i);
                Route route = new Route((int) jRoute.get("id"), (int) jRoute.get("start_address_id"), (int) jRoute.get("end_address_id"), (double) jRoute.get("distance"));
                //then use the this.waypoints list to add directly the corresponding waypoints in the this.routes list
                //like a join in sql, it is useful to only take the this.routes element in RideChoice
                route.setStartWP(this.waypoints.get(route.getStartWPid()-1));
                route.setEndWP(this.waypoints.get(route.getEndWPid()-1));
                this.routes.add(route);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Logger.Severity.Error, "RoutesOverviewParser : error : " +e, "[MapData]");

        }
        return this.routes;
    }

    // Executes only at the end of this class and communicate with the onTaskDone in RideChoice
    @Override
    protected void onPostExecute(@NonNull ArrayList<Route> routes) {
        taskCallback.onTaskDone(routes);
    }
}
