package com.dev.cyclo.ride;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dev.cyclo.CycleData;
import com.dev.cyclo.IButton;
import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.dev.cyclo.MapData.FetchFile;
import com.dev.cyclo.MapData.FetchURL;
import com.dev.cyclo.MapData.RouteData;
import com.dev.cyclo.MapData.TaskLoadedCallback;
import com.dev.cyclo.Menu;
import com.dev.cyclo.OnSwipeTouchListener;
import com.dev.cyclo.OpponentChoice;
import com.dev.cyclo.fragment.ButtonQuit;
import com.dev.cyclo.fragment.ButtonTypeMap;
import com.dev.cyclo.fragment.ButtonTypeZoom;
import com.dev.cyclo.fragment.ContainerIndicators;
import com.dev.cyclo.fragment.Distance;
import com.dev.cyclo.fragment.Map;
import com.dev.cyclo.fragment.Time;
import com.dev.cyclo.realm.MongoRequest;
import com.dev.cyclo.realm.RemoteData;
import com.dev.cyclo.ride.model.SavedRace;
import com.dev.cyclo.ui.login.LoggedInUserView;
import com.dev.cyclo.ui.login.LoginActivity;
import com.google.android.gms.maps.model.LatLng;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.dev.cyclo.R.id;
import static com.dev.cyclo.R.layout;
import static com.dev.cyclo.R.string;

import org.bson.Document;

/**
 * Launch the screen display of the ride
 * @author all
 */
public class Ride extends AppCompatActivity implements IButton.OnButtonClickedListener, TaskLoadedCallback {

    public static int poursuiteId = 12;
    public static int poursuiteIdLeft = 14;
    public static String poursuiteChoice = "";
    public static int depth = 1;
    public boolean choiceMade = false;
    public static int coursesTaken = 1;
    public static boolean invitedd = false;
    public static int nextIdRoute;
    public static int indx = 1;
    public boolean ended = false;

    /**
     * Fragments managing the Ui of distance, time, container with speed and cadence, the map in the background
     * @see Distance
     * @see Time
     * @see ContainerIndicators
     * @see Map
     */
    private Distance distance;
    private Time chrono;
    private ContainerIndicators speedCadence;
    private Map map;

    private static CycleData cycleData = new CycleData();
    ButtonTypeMap typeMap;
    ButtonTypeZoom typeZoom;
    /**
     * Parameters to display the route and interact with the user about the display of the route
     *  idRoute must be the route chosen by the user
     *  routeGeneratorMode when activated, create a route between two fixed points
     *  two buttons to change the vie : type of the zoom or type of the map
     * @see RouteData
     * @see ButtonTypeMap
     * @see ButtonTypeZoom
     */
    int idRoute;
    private RouteData routeData = null;
    private Dialog wait;

    /**
     * Parameters for the pop-up displayed at the end of the ride and to compute the values displayed in the pop-up
     */
    private TextView totalDistance;
    private TextView totalTime;
    private TextView maxSpeed;
    private TextView averageSpeed;
    private TextView result;
    double somme = 0;
    int compteur = 0;
    double speedMax = 0;

    /**
     * These two objects appear after swiping up or left to hide some indicators of the ride
     */
    private ConstraintLayout arrow;
    private ConstraintLayout arrowDistance;


    /**
     * Parameters updated during the ride by the user
     * @see Course
     * @see CycleData
     * @see RemoteData
     */
    private Course course = null;
    /**
     * Create the Dialog object for the start of the ride and the wait of the opponent if there is one
     */
    private Dialog start;

    public static RemoteData currentUserRemoteData;

    public static RemoteData opponentRemoteData;
    private LatLng opponentPosition;


    /**
     * Parameters of the current user
     */
    private boolean aloneRide = false;
    private boolean userSuccess;

    /**
     * Parameters of the opponent
     * @see RemoteData
     */
    private String username;
    private String opponentUsername;
    private Boolean poursuite;
    private LoggedInUserView userview;
    //to manage the end of the ride when the user quits before the end
    private boolean uiThreadQuit;

    /**
     * Log for the developer
     */
    Logger logger = new Logger(this.getClass());

    /**
     * Update the data about the position of the opponent during the ride
     * @param data data
     * @see RemoteData remoteData
     */
    public static void updateOpponentData(RemoteData data){
        opponentRemoteData = data;
    }

    /**
     * Create the Ride Activity and initialize it
     * @param savedInstanceState savedInstanceState
     * @see FetchFile
     * @see FetchURL
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_ride);     //set content UI view

        uiThreadQuit = false;

        //receive data from the calling class
        Bundle opponentChoiceBundle = getIntent().getExtras();
        this.idRoute = -1; // or other values
        this.username = "";
        this.opponentUsername ="";
        if (opponentChoiceBundle != null) {
            this.idRoute = opponentChoiceBundle.getInt("idRoute");
            this.poursuite = opponentChoiceBundle.getBoolean("Poursuite");
            this.invitedd = opponentChoiceBundle.getBoolean("invited");
            if(opponentChoiceBundle.getBoolean("reset")){
                this.poursuiteId = 12;
                this.depth = 1;
                this.poursuiteChoice = "";
                this.coursesTaken = 1;
            }

            Intent intent = getIntent();

            this.userview = (LoggedInUserView) intent.getSerializableExtra("UserView");
            if (opponentChoiceBundle.containsKey("username")){
                this.username = opponentChoiceBundle.getString("username");
            }
            if (opponentChoiceBundle.containsKey("opponentUsername")) {         //calling class had an opponent
                this.opponentUsername = opponentChoiceBundle.getString("opponentUsername");
            }
            else {      //no opponent so the user will ride alone
                this.aloneRide = true;
            }
        } else {
            logger.log(Logger.Severity.Error, "don't receive well intent parameter ");
        }
        logger.log(Logger.Severity.Debug, "username : " + this.username +" opponent username : "+ this.opponentUsername + " idRoute : " + this.idRoute);

        //The next step after the onCreate() is the reception of data asked by the FetchFile or the FetchURL (same return)
        // -> reception by the onTaskDone method
        // data are all the data the google map api returned to the request "how to go from a place to an other one by bicycling ?"
        // they are saved in a json file (FetchFile) or asked to Google Map directly with two positions (FetchURL)
        // to understand better the process, look just after to the TUTORIAL
        if (!Main.config.getConfigData().isGeneratorModeActivated()) {
            new FetchFile(this, false).execute("route" + idRoute + ".json");
            logger.log(Logger.Severity.Debug, "Course : FetchFile called", "COURSE");
        } else {
            //TUTORIAL : Create a custom route between two points and save it :
            //Method : in the config file at DeviceFileExplorer/sdcard/Android/data/com.dev.cyclo/files/Documents/config/config.txt
            // activate the generator mode (true), choose the name of the file you want (route11 for instance)
            // choose two locations and then build and run the app
            //click on any route in the RideChoice, it doesn't matter, it will launch the ride on this new custom route and everything will be ok
            //
            // By the way, it will create a JSON file (called route11.json in our example) at DeviceFileExplorer/sdcard/Android/data/com.dev.cyclo/files/Documents/customRoutes
            // so you can add it in the assets directory of your android project, then you have to complete the routesOverview.json file in assets
            // then build and you have a new route in your app and in the RideChoice activity
            LatLng origPos = Main.config.getConfigData().getOrigLatLng();       //set an origin position
            LatLng destPos = Main.config.getConfigData().getDestLatLng();       //set a destination position
            new FetchURL(this, Main.config.getConfigData().getFileNameWithoutExtension()).execute(getUrl(origPos, destPos));
            logger.log(Logger.Severity.Debug, "Corse : FetchURL called", "COURSE");
        }
        if (LoginActivity.online) {
            try {
                Document TestRemoteData = LoginActivity.mongo.getRemoteDataDoc(username);
                System.out.println(TestRemoteData);
                System.out.println(TestRemoteData.get("invited").getClass());
                currentUserRemoteData = new RemoteData(username, 0, 0, this.poursuiteId, true, false);
                if (!aloneRide) {
                    opponentRemoteData = new RemoteData(opponentUsername, 0, 0, 0, false, false);
                }
                System.out.println("^^^^^^^^^^"+this.invitedd);

            }
            catch (Exception e) {
                System.out.println("****************"+e);

            }



            /*System.out.println("*********" + TestRemoteData);

            if ( TestRemoteData.getInvite() == false ){

            }
            else {
                currentUserRemoteData = TestRemoteData;
                opponentRemoteData = LoginActivity.mongo.getRemoteData(opponentUsername);
            }

            System.out.println("ofduhbzhoufzoufehzoufhuz"+currentUserRemoteData.getInvite());
            System.out.println("Opponent" + opponentRemoteData.getInvite());*/
        }
    }

    /**
     * Launched once the FetchFile or FetchUrl has returned a result (a RouteData) and create a Course element (for calculation) and start the course
     * @param values abstract file which is a list of elements and the first is a RouteData sent by the return of Fetch called in the onCreate
     * @see com.dev.cyclo.MapData.RouteData
     * @see Course
     */
    @Override
    public void onTaskDone(@NonNull Object... values) {
        //FetchURL or FetchFile send the same abstract object which needs to be casted in a routeData object
        this.routeData = (RouteData) values[0];
        this.course = new Course();
        //routeData is the global static data route to follow to go to a point to an other one
        //course is the dynamic class we will use to calculate the newUpdatedPosition of the user on the routeData's static route
        this.course.setRouteData(routeData);
        //by the way, routeData returned by the Google map api (FetchURL or FetchFile) has a totalDistance rounded
        // so to be more precise, setRouteData has a recalculated one
        double recalculatedTotalDistance = this.course.getRouteData().getTotalDistance();
        //so the routeData one has to be more precised to, in order to reach the end of the race at the right position
        this.routeData.setTotalDistance(recalculatedTotalDistance);

        if (LoginActivity.online) {
            currentUserRemoteData.setLatLng(routeData.getOrigPosition());
            currentUserRemoteData.setIdRoute(this.poursuiteId);
            if(!aloneRide){
                opponentRemoteData.setLatLng(routeData.getOrigPosition());
            }
        }
        this.configureAndShowFragments(this.routeData);
        start = new Dialog(this);
        wait = new Dialog(this);
        wait.dismiss();



        startCourse();

    }

    /**
     * A function that gathers all configureAndShowQuitFragment
     * @param routeData routeData
     */
    private void configureAndShowFragments(RouteData routeData) {
        configureAndShowIndicatorFragment();
        configureAndShowTimeFragment();
        configureAndShowDistanceFragment(routeData.getTotalDistance());
        configureAndShowMapFragment(routeData);
        configureAndShowQuitFragment();
        configureAndShowMapTypeFragment();
        configureAndShowZoomTypeFragment();
    }


    /**
     * Configure the fragment to quit
     * @see ButtonQuit
     */
    private void configureAndShowQuitFragment() {
        // A - Get FragmentManager (Support) and Try to find existing instance of fragment in FrameLayout container
        ButtonQuit quit = (ButtonQuit) getSupportFragmentManager().findFragmentById(id.container_quit);

        if (quit == null) {
            // B - Create new main fragment
            quit = new ButtonQuit();
            // C - Add it to FrameLayout container

            getSupportFragmentManager().beginTransaction()
                    .add(id.container_quit, quit)
                    .commit();
        }
    }

    /**
     * Configure the map type button fragment
     * @see ButtonTypeMap
     */
    private void configureAndShowMapTypeFragment() {
        // A - Get FragmentManager (Support) and Try to find existing instance of fragment in FrameLayout container
        typeMap = (ButtonTypeMap) getSupportFragmentManager().findFragmentById(id.container_typemap);

        if (typeMap == null) {
            // B - Create new main fragment
            typeMap = new ButtonTypeMap();
            // C - Add it to FrameLayout container

            getSupportFragmentManager().beginTransaction()
                    .add(id.container_typemap, typeMap)
                    .commit();
        }
    }

    /**
     * Configure the zoom button fragment
     * @see ButtonTypeZoom
     */
    private void configureAndShowZoomTypeFragment() {
        // A - Get FragmentManager (Support) and Try to find existing instance of fragment in FrameLayout container
        typeZoom = (ButtonTypeZoom) getSupportFragmentManager().findFragmentById(id.container_typemap);

        if (typeZoom == null) {
            // B - Create new main fragment
            typeZoom = new ButtonTypeZoom();
            // C - Add it to FrameLayout container

            getSupportFragmentManager().beginTransaction()
                    .add(id.container_typemap, typeZoom)
                    .commit();
        }
    }

    /**
     * Configure the time fragment
     * @see Time
     */
    private void configureAndShowTimeFragment() {
        // A - Get FragmentManager (Support) and Try to find existing instance of fragment in FrameLayout container
        chrono = (Time) getSupportFragmentManager().findFragmentById(id.container_time);

        if (chrono == null) {
            // B - Create new main fragment
            chrono = new Time();
            // C - Add it to FrameLayout container
            getSupportFragmentManager().beginTransaction()
                    .add(id.container_time, chrono)
                    .commit();
        }
    }

    /**
     * Configure the indicators fragment
     * @see ContainerIndicators
     */
    private void configureAndShowIndicatorFragment() {
        // A - Get FragmentManager (Support) and Try to find existing instance of fragment in FrameLayout container
        speedCadence = (ContainerIndicators) getSupportFragmentManager().findFragmentById(id.container_i);

        if (speedCadence == null) {
            // B - Create new main fragment
            speedCadence = new ContainerIndicators();
            // C - Add it to FrameLayout container
            getSupportFragmentManager().beginTransaction()
                    .add(id.container_i, speedCadence)
                    .commit();

        }
    }

    /**
     * Configure the distance fragment
     * @param totalDistance the total distance of the ride
     * @see Distance
     */
    private void configureAndShowDistanceFragment(double totalDistance) {
        // A - Get FragmentManager (Support) and Try to find existing instance of fragment in FrameLayout container
        distance = (Distance) getSupportFragmentManager().findFragmentById(id.container_distance);

        logger.log(Logger.Severity.Debug, "new distance");
        distance = new Distance(totalDistance);

            // C - Add it to FrameLayout container

            getSupportFragmentManager().beginTransaction()
                    .add(id.container_distance, distance)
                    .commit();

    }

    /**
     * Configure the map fragment
     * @param routeData routeData
     * @see Map
     */
    private void configureAndShowMapFragment(RouteData routeData) {
        map = (Map) getSupportFragmentManager().findFragmentById(id.container_map);
        if (map == null) {
            // B - Create new main fragment
            if (aloneRide){
                map = new Map(routeData);
            }
            else {
                map = new Map(routeData, this.opponentUsername);
            }
            // C - Add it to FrameLayout container
            getSupportFragmentManager().beginTransaction()
                    .add(id.container_map, map)
                    .commit();
        }
    }

    /**
     * Only in the case of RouteGeneratorMode activated
     * Create the url for Google Maps API which returns the json with the route
     * @param origin lat/long of the origin
     * @param dest lat/long of the destination
     * @return the url send to Google Maps API
     */
    private String getUrl(LatLng origin, LatLng dest) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + "bicycling";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBfDmZdo0e_veXFWBuODurz0SiksLVq9Nw";
    }

    /**
     * Set the swipe actions to hide and show the indicators during the ride
     */
    @SuppressLint("ClickableViewAccessibility")
    protected void setSwipe(){
        arrow = new ConstraintLayout(this);
        arrow = findViewById(id.arrow);
        FrameLayout container_i = findViewById(id.container_i);

        arrowDistance = new ConstraintLayout(this);
        arrowDistance = findViewById(id.arrow_distance);
        FrameLayout container_distance = findViewById(id.container_distance);
        FrameLayout containerTime = findViewById(id.container_time);
        FrameLayout touchZone = findViewById(id.touch_zone_up_down);


        speedCadence.getView().setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                Log.d("swipe gauche ", "réalisé");
                container_i.setVisibility(View.GONE);
                arrow.setVisibility(View.VISIBLE);
            }

        });
        arrow.setOnClickListener(v -> {
            container_i.setVisibility(View.VISIBLE);
            arrow.setVisibility(View.GONE);
        });


        touchZone.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeUp() {
                if (container_distance.getVisibility()==View.VISIBLE) {
                    Log.d("swipe up première fois ", "réalisé");
                    container_distance.setVisibility(View.GONE);
                    arrowDistance.setVisibility(View.VISIBLE);
                    //containerTime.animate().translationY(-container_distance.getWidth());
                    //arrowDistance.animate().translationY(-container_distance.getWidth());
                }
                else {
                    if (containerTime.getVisibility() == View.VISIBLE) {
                        Log.d("swipe up deuxième fois ", "réalisé");
                        containerTime.setVisibility(View.GONE);
                        //arrowDistance.animate().translationY(-containerTime.getWidth());
                    }
                }
            }

        });
        arrowDistance.setOnClickListener(v -> {
            if (container_distance.getVisibility()==View.GONE) {
                container_distance.setVisibility(View.VISIBLE);
                //containerTime.animate().translationY(container_distance.getWidth());
                //arrowDistance.animate().translationY(container_distance.getWidth());
                arrowDistance.setVisibility(View.GONE);

            }
            if (containerTime.getVisibility()==View.GONE){
                container_distance.setVisibility(View.VISIBLE);
                containerTime.setVisibility(View.VISIBLE);
                //containerTime.animate().translationY(container_distance.getWidth());
                //arrowDistance.animate().translationY(container_distance.getWidth());
                arrowDistance.setVisibility(View.GONE);

            }
        });
    }

    /**
     * Finish the activity when ButtonQuit clicked
     * @param view view
     */
    @Override
    public void onButtonClicked(@NonNull View view) {
        uiThreadQuit = true;
        endRide(getLayoutInflater(), false, false);
        if(LoginActivity.online) {
            LoginActivity.mongo.rideQuit(username);
        }
        returnNo();
    }

    /**
     * Change the type of the map (satellite or simple map) when ButtonMapType clicked
     * @param view view
     */
    public void onButtonMapTypeClicked(@NonNull View view) {
        this.map.changeMapType();
        if (view.findViewById(id.typemapPlan).getVisibility() == View.VISIBLE){
            view.findViewById(id.typemapPlan).setVisibility(View.GONE);
            view.findViewById(id.typemapSatellite).setVisibility(View.VISIBLE);}
        else {
            view.findViewById(id.typemapPlan).setVisibility(View.VISIBLE);
            view.findViewById(id.typemapSatellite).setVisibility(View.GONE);
        }
    }

    /**
     * Change the type of the zoom (free, following and animated) when ButtonZoomType clicked
     * @param view view
     */
    public void onButtonZoomTypeClicked(@NonNull View view) {
        this.map.changeZoomType();
        if (view.findViewById(id.freeZoom).getVisibility() == View.VISIBLE){
            view.findViewById(id.freeZoom).setVisibility(View.GONE);
            view.findViewById(id.centerZoom).setVisibility(View.VISIBLE);
            view.findViewById(id.crossZoom).setVisibility(View.GONE);
        }
        else {
            if (view.findViewById(id.centerZoom).getVisibility() == View.VISIBLE) {
                view.findViewById(id.freeZoom).setVisibility(View.GONE);
                view.findViewById(id.centerZoom).setVisibility(View.GONE);
                view.findViewById(id.crossZoom).setVisibility(View.VISIBLE);
            }
            else {
                view.findViewById(id.freeZoom).setVisibility(View.VISIBLE);
                view.findViewById(id.centerZoom).setVisibility(View.GONE);
                view.findViewById(id.crossZoom).setVisibility(View.GONE);
            }
        }
    }

    /**
     * Update the speed and the cadence of the user during the ride
     * @param newCycleData newCycleData
     * @see CycleData
     */
    public static void updateCycleData(@NonNull CycleData newCycleData) {
        cycleData = newCycleData;
    }

    /**
     * Update the position on the map of the user and the opponent
     * Update by the way the zoom if needed
     */
    private void updateMapPosition() {
        //calculate with course, the new position of the user with his progress compared to the distance of the route (proportional)
        LatLng updatedNewPosition = this.course.updatedNewPosition(this.distance.getProgress());
        map.updatePosition(updatedNewPosition);     //update the marker position on the map

        if (LoginActivity.online) {
            //update user own position
            currentUserRemoteData.setLatLng(updatedNewPosition);
            if(!aloneRide) {
                //ask for and receive the opponent position
                this.opponentPosition = opponentRemoteData.getLatLng();
                this.nextIdRoute = opponentRemoteData.getIdRoute();


                //update the opponent marker position on the map
                map.updateOpponentPosition(opponentPosition);
            }
        }

        //zoom update
        int selectedMode = map.getSelected_zoom_mode();
        if (selectedMode == Map.FOLLOWING_ZOOM){        //following zoom update
            map.setFollowingZoom(updatedNewPosition);
        }
        else {
            if (selectedMode == Map.ANIMATED_ZOOM){     //animated zoom update
                map.setAnimatedZoom(updatedNewPosition, this.course.getBearing(), this.course.getTilt());
            }
        }
    }

    /**
     * Update all text view of the activity
     * @param cycleData new data to use
     * @see CycleData
     */
    private void updateDataView(CycleData cycleData) {

        //logger.log(Logger.Severity.Verbose, "updateDataView" + cycleData);
        compteur += 1;
        double Speed = cycleData.getSpeed();
        double Cadence = cycleData.getCadence();
        somme += Speed;
        if (speedMax < Speed) {
            speedMax = Speed;
            //logger.log(Logger.Severity.Verbose, "on est dans speedmax");
        }
        //logger.log(Logger.Severity.Verbose, String.valueOf(Speed));
        speedCadence.updateIndicators(Speed, Cadence);
        //update distance
        distance.updateDistance(Speed);
    }



    // Where to add the newendride
    public void startRideThread(){

        if (this.poursuite.equals(true)) {
            System.out.println("This is poursuite");
            Handler handlerUpdate = new Handler();

            Runnable updateData = new Runnable() {
                @Override
                public void run() {
                    if (distance.getEnd() || uiThreadQuit) {
                        endRide(getLayoutInflater(), !uiThreadQuit,true);

                        distance.reset();
                        logger.log(Logger.Severity.Warn, "update thread killed");
                    }
                    else if ( (distance.getProgress() >= (routeData.getTotalDistance() - 0.03) && distance.getProgress() <= (routeData.getTotalDistance() - 0.02)  ) && !choiceMade && invitedd == false) {
                        System.out.println(distance.getProgress());
                        choiceMade = true;
                        preEndRide(getLayoutInflater(), !uiThreadQuit,true);
                        updateDataView(cycleData);
                        updateMapPosition();    //update the map and position of all its markers
                        handlerUpdate.postDelayed(this, 25); //next call thread (this) + delay
                    }
                    else if (opponentRemoteData.getIdRoute() == 0 || (invitedd == true && (course.distanceBetweenTwoPosition(currentUserRemoteData.getLatLng(), opponentRemoteData.getLatLng()) < 0.001) && indx > 1 && !(opponentRemoteData.getLatLng().equals(routeData.getOrigPosition()))) ) {

                        endRidePoursuite(getLayoutInflater(), !uiThreadQuit);

                        distance.reset();
                        logger.log(Logger.Severity.Warn, "update thread killed");
                    }
                    else if (opponentRemoteData.getIdRoute() == 0 || (invitedd == false && (course.distanceBetweenTwoPosition(opponentRemoteData.getLatLng(), currentUserRemoteData.getLatLng() ) < 0.001) && indx > 1 && !(opponentRemoteData.getLatLng().equals(routeData.getOrigPosition()))) ) {

                        endRidePoursuite(getLayoutInflater(), !uiThreadQuit);

                        distance.reset();
                        logger.log(Logger.Severity.Warn, "update thread killed");
                    }
                    else{
                        System.out.println("***************"+invitedd);
                        indx += 1;
                        updateDataView(cycleData);
                        updateMapPosition();    //update the map and position of all its markers
                        handlerUpdate.postDelayed(this, 25); //next call thread (this) + delay
                    }
                }
            };


            handlerUpdate.post(updateData);
            start.dismiss();

            distance.start();
            chrono.startTime();

            logger.log(Logger.Severity.Debug, "end of demarrage.onClicked");



        }
        else {
            System.out.println("This is not Poursuite");
            Handler handlerUpdate = new Handler();

            Runnable updateData = new Runnable() {
                @Override
                public void run() {
                    if (distance.getEnd() || uiThreadQuit) {
                        endRide(getLayoutInflater(), !uiThreadQuit,false);
                        distance.reset();
                        logger.log(Logger.Severity.Warn, "update thread killed");
                    }
                    else{
                        updateDataView(cycleData);
                        updateMapPosition();    //update the map and position of all its markers
                        handlerUpdate.postDelayed(this, 25); //next call thread (this) + delay
                    }
                }
            };


            handlerUpdate.post(updateData);
            start.dismiss();

            distance.start();
            chrono.startTime();

            logger.log(Logger.Severity.Debug, "end of demarrage.onClicked");
        }


    }

    View.OnClickListener OfflineOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            wait.dismiss();
            logger.log(Logger.Severity.Warn, "try to start a ride");
            setSwipe();

            start.dismiss();
            startRideThread();
            logger.log(Logger.Severity.Warn, "ride thread started");
        }
    };


    View.OnClickListener OnlineOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            logger.log(Logger.Severity.Debug, "try to start a ride");
            setSwipe();

            //currentUserRemoteData.setReady(true);


            System.out.println(username+ " is invited : "+invitedd);


            LoginActivity.mongo.setReady(username);
            LoginActivity.mongo.getRemoteDataByThread(opponentUsername);


            start.dismiss();
            wait.show();
            logger.log(Logger.Severity.Debug, "start dismiss, wait show");

            logger.log(Logger.Severity.Debug, opponentUsername + " is ready " + opponentRemoteData.isReady());

            while(!opponentRemoteData.isReady()){
                wait.hide();
                wait.show();
            }
            logger.log(Logger.Severity.Debug, opponentUsername + " is ready " + opponentRemoteData.isReady());

            LoginActivity.mongo.updateRemoteDataByThread(username);
            logger.log(Logger.Severity.Debug, "wait dismiss");

            wait.dismiss();
            start.dismiss();

            if (invitedd == true){
                try {
                    System.out.println("i am starting to sleep");
                    Thread.sleep(1000);
                    System.out.println("i woke up");
                } catch (InterruptedException e) {
                    System.out.println("i am heeeeeeeeeeeeeeeeeeeeeeeeeeeeeere");
                    e.printStackTrace();
                }
            }


            startRideThread();
            logger.log(Logger.Severity.Debug, "ride thread started");
        }
    };


    /**
     * Start the ride by :
     * - creating a button Start
     * - launching the thread of display
     */
    public void startCourse() {

        //for the benchmark
        MongoRequest.nbUpdate = 0;
        MongoRequest.timeUpdate = 0;
        MongoRequest.nbGet = 0;
        MongoRequest.timeGet = 0;

        TextView demarrage;

        start.setContentView(layout.start);
        demarrage = start.findViewById(id.demarrage);
        start.setCanceledOnTouchOutside(false);
        start.setCancelable(false);

        wait.setContentView(layout.wait);
        TextView attente = wait.findViewById(id.attente);
        wait.setCanceledOnTouchOutside(false);
        wait.setCancelable(false);
        String waitingText = "En attente de " + opponentUsername;
        attente.setText(waitingText);
        attente.setVisibility(View.VISIBLE);

        if(LoginActivity.online && !aloneRide){
            demarrage.setOnClickListener(OnlineOnClickListener);
        }
        else {
            demarrage.setOnClickListener(OfflineOnClickListener);
        }

        wait.show();
        start.show();
        logger.log(Logger.Severity.Debug, "start.show()");

    }

    public double distanceLeft() {
        LatLng updatedNewPosition = this.course.updatedNewPosition(this.distance.getProgress());
        double distanceLeft = course.distanceBetweenTwoPosition(updatedNewPosition, routeData.getDestPosition());
        return distanceLeft;
    }


    public void preEndRide(LayoutInflater inflater, boolean withPopup, boolean poursuite) {
        if (this.coursesTaken < 3){
            @SuppressLint("InflateParams") View popUpView = inflater.inflate(layout.pop_up_p, null);

            averageSpeed = popUpView.findViewById(id.avr_speed);
            Button ok = popUpView.findViewById(id.ok_button);
            Button left = popUpView.findViewById(id.ok_button2);

            Button no = popUpView.findViewById(id.no_button);
            ok.setText("Right");
            left.setText("Left");

            averageSpeed.setText("Continue ?");


            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            PopupWindow popupWindow = new PopupWindow(this);
            popupWindow.setContentView(popUpView);
            popupWindow.setHeight(height);
            popupWindow.setWidth(width);
            popupWindow.setFocusable(false);// lets taps outside the popup also dismiss it

            //setData(totalRoundDistance, chrono.deltaTime(), speedMax, somme / compteur);
            popupWindow.showAtLocation(popUpView, Gravity.CENTER, 0, 0);

            ok.setOnClickListener(v -> {
                popupWindow.dismiss();
                returnOk();
                currentUserRemoteData.setIdRoute(poursuiteId);
            });
            left.setOnClickListener(v -> {
                popupWindow.dismiss();
                returnLeft();
                currentUserRemoteData.setIdRoute(poursuiteId);
            });
            no.setOnClickListener(v -> {
                popupWindow.dismiss();
                returnNo();
            });
        }
        else {
            this.coursesTaken = 4;
        }

    }



    /**
     * Manage the end of the ride with a pop-up which resumes the main indicators of the ride
     * @param inflater layout inflater
     */
    private void endRide(LayoutInflater inflater, boolean withPopup, boolean poursuite) {
        distance.reset();
        chrono.stopTime();
        double totalRoundDistance = Math.round(routeData.getTotalDistance() * 100.0) / 100.0;   //precision of 0.01

        if (LoginActivity.online) {
            if (aloneRide) {    //if user playing alone
                this.opponentUsername = "null";     //opponent user name set at null
                userSuccess = true;     //arbitrary put success at true
                //logger.log(Logger.Severity.Debug, "userSuccess true");
            } else {
                //success only if the other player is at least 5 m far from the arrival
                userSuccess = (course.distanceBetweenTwoPosition(currentUserRemoteData.getLatLng(), opponentRemoteData.getLatLng()) < 0.001);

                //reset user own remote data

                if(!poursuite){
                    System.out.println("we somehow entered heeere");
                    currentUserRemoteData.setIdRoute(0);
                    currentUserRemoteData.setReady(false);
                    LoginActivity.mongo.rideQuit(username);
                }



                //set that he is not in a ride anymore
            }
        }

        if(withPopup) {
            if (!poursuite) {
                @SuppressLint("InflateParams") View popUpView = inflater.inflate(layout.pop_up, null);
                result = popUpView.findViewById(id.result);
                totalDistance = popUpView.findViewById(id.total_distance);
                totalTime = popUpView.findViewById(id.total_time);
                maxSpeed = popUpView.findViewById(id.max_speed);
                averageSpeed = popUpView.findViewById(id.avr_speed);
                Button ok = popUpView.findViewById(id.ok_button);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                PopupWindow popupWindow = new PopupWindow(this);
                popupWindow.setContentView(popUpView);
                popupWindow.setHeight(height);
                popupWindow.setWidth(width);
                popupWindow.setFocusable(false);// lets taps outside the popup also dismiss it

                setData(totalRoundDistance, chrono.deltaTime(), speedMax, somme / compteur);
                darkenBackground();
                popupWindow.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
                if (LoginActivity.online) {
                    SavedRace savedRace = new SavedRace(
                            this.username,
                            this.idRoute,
                            totalRoundDistance,
                            this.chrono.deltaTime(),
                            somme / compteur,
                            this.speedMax,
                            new Date(),
                            this.opponentUsername,
                            userSuccess
                    );
                    LoginActivity.mongo.addSavedRace(savedRace);
                    logger.log(Logger.Severity.Debug, savedRace.toString());
                }
                ok.setOnClickListener(v -> {
                    popupWindow.dismiss();
                    returnNo();
                });
            }
            else {
                    if (LoginActivity.online) {
                        SavedRace savedRace = new SavedRace(
                                this.username,
                                this.idRoute,
                                totalRoundDistance,
                                this.chrono.deltaTime(),
                                somme / compteur,
                                this.speedMax,
                                new Date(),
                                this.opponentUsername,
                                userSuccess
                        );
                        LoginActivity.mongo.addSavedRace(savedRace);
                        logger.log(Logger.Severity.Debug, savedRace.toString());
                    }
                    if (this.coursesTaken <= 3) {
                    if(this.invitedd == false) {
                        Intent intent = new Intent(this, Ride.class);
                        Bundle rideBundle = new Bundle();
                        rideBundle.putInt("idRoute", this.poursuiteId); //Your id
                        rideBundle.putBoolean("invited",this.invitedd);
                        rideBundle.putString("username", this.username);
                        rideBundle.putString("opponentUsername", this.opponentUsername);
                        rideBundle.putBoolean("Poursuite", true);
                        rideBundle.putBoolean("Poursuite", true);
                        intent.putExtra("UserView", this.userview);
                        intent.putExtras(rideBundle); //Put your id to your next Intent
                        startActivity(intent);
                        }
                    else {
                        Intent intent = new Intent(this, Ride.class);
                        Bundle rideBundle = new Bundle();
                        System.out.println("***************************** next id route is : " + this.nextIdRoute);
                        rideBundle.putInt("idRoute", this.nextIdRoute); //Your id
                        rideBundle.putBoolean("invited",this.invitedd);
                        rideBundle.putString("username", this.username);
                        rideBundle.putString("opponentUsername", this.opponentUsername);
                        rideBundle.putBoolean("Poursuite", true);
                        intent.putExtra("UserView", this.userview);
                        intent.putExtras(rideBundle); //Put your id to your next Intent
                        startActivity(intent);
                    }
                    }
                    else {
                        returnNo();
                    }

                }


            }



        logger.log(Logger.Severity.Debug, "Ride end normally");
    }

    private void endRidePoursuite(LayoutInflater inflater, boolean withPopup) {
        distance.reset();
        chrono.stopTime();
        double totalRoundDistance = Math.round(routeData.getTotalDistance() * 100.0) / 100.0;   //precision of 0.01

        if (LoginActivity.online) {
            if (aloneRide) {    //if user playing alone
                this.opponentUsername = "null";     //opponent user name set at null
                userSuccess = true;     //arbitrary put success at true
                //logger.log(Logger.Severity.Debug, "userSuccess true");
            } else {
                //success only if the other player is at least 5 m far from the arrival
                if (invitedd) {
                    userSuccess = true;
                }
                else {
                    userSuccess = false;
                }


                //reset user own remote data
                currentUserRemoteData.setIdRoute(0);
                currentUserRemoteData.setReady(false);
                LoginActivity.mongo.rideQuit(username);



                //set that he is not in a ride anymore
            }
        }

        if(withPopup) {
                @SuppressLint("InflateParams") View popUpView = inflater.inflate(layout.pop_up, null);
                result = popUpView.findViewById(id.result);
                totalDistance = popUpView.findViewById(id.total_distance);
                totalTime = popUpView.findViewById(id.total_time);
                maxSpeed = popUpView.findViewById(id.max_speed);
                averageSpeed = popUpView.findViewById(id.avr_speed);
                Button ok = popUpView.findViewById(id.ok_button);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                PopupWindow popupWindow = new PopupWindow(this);
                popupWindow.setContentView(popUpView);
                popupWindow.setHeight(height);
                popupWindow.setWidth(width);
                popupWindow.setFocusable(false);// lets taps outside the popup also dismiss it

                setData(totalRoundDistance, chrono.deltaTime(), speedMax, somme / compteur);
                darkenBackground();
                popupWindow.showAtLocation(popUpView, Gravity.CENTER, 0, 0);
                if (LoginActivity.online) {
                    SavedRace savedRace = new SavedRace(
                            this.username,
                            this.idRoute,
                            totalRoundDistance,
                            this.chrono.deltaTime(),
                            somme / compteur,
                            this.speedMax,
                            new Date(),
                            this.opponentUsername,
                            userSuccess
                    );
                    LoginActivity.mongo.addSavedRace(savedRace);
                    logger.log(Logger.Severity.Debug, savedRace.toString());
                }
                ok.setOnClickListener(v -> {
                    popupWindow.dismiss();
                    returnNo();
                });


        }



        logger.log(Logger.Severity.Debug, "Ride end normally");
    }

    /**
     * Set all the text view on the end pop-up
     * @param distance distance
     * @param time time
     * @param speed speed
     * @param avrSpeed averageSpeed
     */
    public void setData(double distance, long time, double speed, double avrSpeed) {
        long timeSecond = time / 1000;
        int timeMinute = (int) timeSecond / 60;
        int timeHour = timeMinute / 60;
        timeSecond = timeSecond - timeHour * 3600 - timeMinute * 60;
        String msgTime = "Temps : " + timeHour + " h " + timeMinute + " min " + timeSecond + " s";
        totalTime.setText(msgTime);
        String msgDistance = "Distance : " + distance + " km ";
        totalDistance.setText(msgDistance);
        DecimalFormat df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.DOWN);
        String msgSpeed = "Vitesse max : " + df.format(speed) + " km/h";
        maxSpeed.setText(msgSpeed);
        String msgAvrSpeed = "Vitesse moyenne : " + df.format(avrSpeed) + " km/h";
        averageSpeed.setText(msgAvrSpeed);
        if (LoginActivity.online && !aloneRide){
            if (userSuccess) {
                result.setText(string.success_sentence);
            }
            else {
                result.setText(string.fail_sentence);
            }
        }
    }

    /**
     * Darken the background of the end pop-up
     */
    private void darkenBackground() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.5f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
    }

    /**
     * Finish the activity once the ride finished
     */
    public void returnOk() {
        if (LoginActivity.online) {
            logger.log(Logger.Severity.Info, "time get : " + MongoRequest.timeGet);
            logger.log(Logger.Severity.Info, "nb get : " + MongoRequest.nbGet);
            logger.log(Logger.Severity.Info, "time update : " + MongoRequest.timeUpdate);
            logger.log(Logger.Severity.Info, "nb update : " + MongoRequest.nbUpdate);
        }
        Ride.poursuiteChoice = "right";
        if (this.depth == 1) {
            this.depth = this.depth + 1;
            this.poursuiteId = this.poursuiteId + 4;
            this.coursesTaken += 1;

        }
        else {
            this.poursuiteId = this.poursuiteId + 2;
            this.coursesTaken += 1;
        }


    }

    public void returnLeft() {
        if (LoginActivity.online) {
            logger.log(Logger.Severity.Info, "time get : " + MongoRequest.timeGet);
            logger.log(Logger.Severity.Info, "nb get : " + MongoRequest.nbGet);
            logger.log(Logger.Severity.Info, "time update : " + MongoRequest.timeUpdate);
            logger.log(Logger.Severity.Info, "nb update : " + MongoRequest.nbUpdate);
        }
        Ride.poursuiteChoice = "left";
        if (this.depth == 1) {
            this.depth = this.depth + 1;
            this.poursuiteId = this.poursuiteId + 1;
            this.coursesTaken += 1;
        }
        else {
            this.poursuiteId = this.poursuiteId + 1;
            this.coursesTaken += 1;
        }

    }

    public void Continuer() {
        if (LoginActivity.online) {
            logger.log(Logger.Severity.Info, "time get : " + MongoRequest.timeGet);
            logger.log(Logger.Severity.Info, "nb get : " + MongoRequest.nbGet);
            logger.log(Logger.Severity.Info, "time update : " + MongoRequest.timeUpdate);
            logger.log(Logger.Severity.Info, "nb update : " + MongoRequest.nbUpdate);
        }
        if (Ride.poursuiteChoice.equals("left")) {
            this.poursuiteIdLeft = this.poursuiteIdLeft + 1;
            Intent intent = new Intent(this, Ride.class);
            Bundle rideBundle = new Bundle();
            rideBundle.putInt("idRoute", this.poursuiteIdLeft); //Your id
            rideBundle.putString("username", this.username);
            rideBundle.putBoolean("Poursuite", this.poursuite);
            intent.putExtra("UserView", this.userview);
            intent.putExtras(rideBundle); //Put your id to your next Intent
            startActivity(intent);
        }
        else{
            this.poursuiteId = this.poursuiteId + 1;
            Intent intent = new Intent(this, Ride.class);
            Bundle rideBundle = new Bundle();
            rideBundle.putInt("idRoute", this.poursuiteId); //Your id
            rideBundle.putString("username", this.username);
            rideBundle.putBoolean("Poursuite", this.poursuite);
            intent.putExtra("UserView", this.userview);
            intent.putExtras(rideBundle); //Put your id to your next Intent
            startActivity(intent);
        }

    }

    public void returnNo() {
        if (LoginActivity.online) {
            logger.log(Logger.Severity.Info, "time get : " + MongoRequest.timeGet);
            logger.log(Logger.Severity.Info, "nb get : " + MongoRequest.nbGet);
            logger.log(Logger.Severity.Info, "time update : " + MongoRequest.timeUpdate);
            logger.log(Logger.Severity.Info, "nb update : " + MongoRequest.nbUpdate);
        }
        currentUserRemoteData.setIdRoute(0);
        currentUserRemoteData.setReady(false);
        LoginActivity.mongo.rideQuit(username);
        Intent intent = new Intent(this, Menu.class);
        intent.putExtra("UserView", this.userview);
        startActivity(intent);
    }

}
