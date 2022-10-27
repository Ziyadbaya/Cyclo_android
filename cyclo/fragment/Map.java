package com.dev.cyclo.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.dev.cyclo.MapData.RouteData;
import com.dev.cyclo.R;
import com.dev.cyclo.ui.login.LoginActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class is the map fragment, all the view of the map is here with all the static settings
 * with few methods to update markers positions, polyline etc...
 * @see Fragment
 * @see OnMapReadyCallback
 */

public class Map extends Fragment implements OnMapReadyCallback {

    public static int FREE_ZOOM = 0;
    public static int FOLLOWING_ZOOM = 1;
    public static int ANIMATED_ZOOM = 2;

    private GoogleMap mMap;
    private final Logger logger = new Logger(this.getClass());
    private final boolean isAlone;

    private LatLng origPos;
    private LatLng cameraPos;
    private LatLng opponentPosition;
    private int selected_zoom_mode = FREE_ZOOM;
    private Marker currentMarker, opponentMarker;
    private final RouteData routeData;
    private MarkerOptions origLandmark;
    private MarkerOptions destLandmark;
    private MarkerOptions currentLandmark;

    private String opponentName;



    public Map(@NonNull RouteData routeData) {
        this.routeData = routeData;
        this.isAlone = false;
    }
    public Map(@NonNull RouteData routeData, @NonNull String opponentName) {
        this.routeData = routeData;
        this.isAlone = true;
        this.opponentName = opponentName;
    }

    /** direct creation of useful graphical elements */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(Objects.requireNonNull(getContext()));       //needed to put BitMap
        //set some crucial positions
        origPos = routeData.getOrigPosition();
        LatLng destPos = routeData.getDestPosition();
        LatLng currentPos = routeData.getOrigPosition();
        //the starting global zoom with the whole route visible has to be centered on this position :
        cameraPos = new LatLng((origPos.latitude + destPos.latitude) / 2, (origPos.longitude + destPos.longitude) / 2);
        //landmark of the crucial previous positions
        origLandmark = new MarkerOptions().position(origPos).title("Départ").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        destLandmark = new MarkerOptions().position(destPos).title("Arrivée").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentLandmark = new MarkerOptions().position(currentPos).title("Ma position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_map, container, false);
        return fragment.findViewById(R.id.map);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
    }

    /**
     * This method is automatically called when the map is set
     * From now, elements like markers can be added to the map and not before
     * @param googleMap the google map element automatically given
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //initialization of map
        mMap = googleMap;
        mMap.setMapType(Main.config.getConfigData().getDefaultMapType());     //Satellite mode or Plan mode
        mMap.getUiSettings().setZoomControlsEnabled(true);      //plus and minus buttons
        mMap.getUiSettings().setZoomGesturesEnabled(true);      //finger gestures to zoom enabled
        mMap.setBuildingsEnabled(true);     //nothing seen, not sure is operational
        mMap.addMarker(origLandmark);       //addMarker takes a MarkerOptions (and returns a Marker)
        mMap.addMarker(destLandmark);
        //the starting global zoom is centered on the cameraPosition (center of the route)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPos));
        //the level of zoom is calculated with bounds of the route to see the whole route with 200 of margin
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(routeData.getBoundLatLngSW(), routeData.getBoundLatLngNE()), 200));
        //when you add a markerOptions, it returns a marker
        //only Marker element returned here is changeable/removable (not MarkerOptions)
        currentMarker = mMap.addMarker(currentLandmark);        //useful for future updates
        //set the polyLines of the route (one for each step)
        this.setPolyline(routeData.getStepsPolylineList());
        //if you are connected to the database and you want to do a race with someone
        if (LoginActivity.online && this.isAlone) {
            //adds the opponent markerOptions with his name and saves the marker for future updates
            this.addOpponentPosition(this.opponentName);
        }
        logger.log(Logger.Severity.Debug, "MapReady and markers/polyLines set", "[MapData]");
    }

    /** updates the position of the current user on the map */
    public void updatePosition(@NonNull LatLng position) {
        currentMarker.setPosition(position);        //customize the saved marker
    }

    /** center the zoom on this position */
    public void setFollowingZoom(@NonNull LatLng position){
        this.cameraPos = position;
        //moveCamera is an immediate action
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cameraPos));
    }

    /** center the zoom and orientate it toward the direction where the user will go
     * @param position the center of the zoom view
     * @param bearing the angle between the north and where we want the zoom to look at
     * @param tilt the angle between the vertical line which starts at the position and the line between the position and the zoom position
     */
    public void setAnimatedZoom(@NonNull LatLng position, float bearing, float tilt){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)      // Sets the center of the map to Mountain View
                .zoom(Main.config.getConfigData().getZoom_scale())                   // Sets the zoom
                .bearing(bearing)                // Sets the horizontal orientation of the camera
                .tilt(tilt)                   // Sets the tilt of the camera (vertical plan)
                .build();                   // Creates a CameraPosition from the builder
        //a moveCamera is immediate and an animateCamera is smooth and avoid brutal changes in the view
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),200,null);   //200 is a smooth delay
    }

    /** set an opponent's MarkerOptions on the map with his name and set the Marker result to change his position after */
    private void addOpponentPosition(String name){
        this.opponentPosition = origPos;
        //MarkerOptions
        MarkerOptions opponentLandmark = new MarkerOptions().position(opponentPosition).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        this.opponentMarker = this.mMap.addMarker(opponentLandmark);
    }

    /** update the opponent position and marker */
    public void updateOpponentPosition(@NonNull LatLng position){
        this.opponentPosition = position;
        this.opponentMarker.setPosition(position);

    }

    /** change map Type by checking the current map Type selected */
    public void changeMapType(){
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    /** change zoom type by checking the current zoom type selected */
    public void changeZoomType() {
        if (this.selected_zoom_mode == FREE_ZOOM) {
            this.selected_zoom_mode = FOLLOWING_ZOOM;
        } else {
            if (this.selected_zoom_mode == FOLLOWING_ZOOM) {
                this.selected_zoom_mode = ANIMATED_ZOOM;
            } else {
                this.selected_zoom_mode = FREE_ZOOM;
            }
        }
    }

    /**  this method sets on the map the polylines of the chosen route
     * @param polylineOptions list of polyline : one for each step of the route (alternated color to distinguish them)
     */
    public void setPolyline(@NonNull ArrayList<PolylineOptions> polylineOptions) {
        for (PolylineOptions polylineOption : polylineOptions) {
            mMap.addPolyline(polylineOption);
        }
    }

    public int getSelected_zoom_mode() {
        return selected_zoom_mode;
    }

    public void setSelected_zoom_mode(int selected_zoom_mode) {
        this.selected_zoom_mode = selected_zoom_mode;
    }
}

