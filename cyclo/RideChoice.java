package com.dev.cyclo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.dev.cyclo.MapData.FetchFile;
import com.dev.cyclo.MapData.model.Route;
import com.dev.cyclo.MapData.TaskLoadedCallback;
import com.dev.cyclo.bluetooth.BtLink;
import com.dev.cyclo.bluetooth.BtSimulation;
import com.dev.cyclo.bluetooth.IBluetooth;
import com.dev.cyclo.data.model.LoggedInUser;
import com.dev.cyclo.ride.Ride;
import com.dev.cyclo.ride.model.SavedRace;
import com.dev.cyclo.ui.login.LoggedInUserView;
import com.dev.cyclo.ui.login.LoginActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * This object allows the user to choose which ride he wants to do
 */
public class RideChoice extends Activity implements TaskLoadedCallback {

    private LinearLayout layoutRoutes;
    float factor;

    IBluetooth bluetooth = null;
    boolean btSimulatorModeEnabled = true; // true : bluetooth mode or false : simulator mode

    private ArrayList<Route> routes = new ArrayList<>();
    private final Logger logger = new Logger(this.getClass());

    private String username;
    private Boolean poursuite;
    private LoggedInUserView userview;

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        logger.log(Logger.Severity.Debug, "new RideChoice page");
        //Hiding the action and the status bars
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        Bundle menuBundle = getIntent().getExtras();
        this.username = ""; // or other values
        if (menuBundle != null) {
            this.username = menuBundle.getString("username");
            this.poursuite = menuBundle.getBoolean("Poursuite");
            System.out.println("*******"+this.poursuite);
            Intent intent = getIntent();
            this.userview = (LoggedInUserView) intent.getSerializableExtra("UserView");

            if (this.poursuite == true) {
                System.out.println("First poursuite is True");
            }
            else{
                System.out.println("First poursuite is False");
            }
        } else {
            logger.log(Logger.Severity.Info, "don't receive well intent parameter ");
        }





        setContentView(R.layout.ride_choice);

        ImageView homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(this::home);
        layoutRoutes = findViewById(R.id.layoutRoutes);
        this.factor = layoutRoutes.getContext().getResources().getDisplayMetrics().density;


            new FetchFile(this, true).execute("routesOverview.json");
            logger.log(Logger.Severity.Debug, "Course : FetchFile called", "COURSE");


        //new FetchFile ...
        //FUNCTION CALLED IN THE ONTASKDONE AT THE END OF THE FILE
    }

    private String getRecords(List<SavedRace> races, int id){
        Stream<SavedRace> filteredRace = races.stream().filter(savedRace -> savedRace.getRouteId() == id);

        Optional<SavedRace> theRace = filteredRace.max(Comparator.comparingDouble(SavedRace::getTime));

        if(theRace.isPresent()){
            double time = theRace.get().getTime();
            double timeSecond = time / 1000;
            int timeMinute = (int) timeSecond / 60;
            int timeHour = timeMinute / 60;
            DecimalFormat df = new DecimalFormat("#");
            df.setRoundingMode(RoundingMode.DOWN);
            timeSecond = timeSecond - timeHour * 3600 - timeMinute * 60;
            String timeSecondStr = df.format(timeSecond);
            if (timeHour != 0){
                return timeHour + " h " + timeMinute + " min " + timeSecondStr + " s";
            }
            else {
                return timeMinute + " min " + timeSecondStr + " s";
            }
        }
        else {
            return "";
        }

    }

    private void setUpGraphicsItems(){

        List<SavedRace> races = null;

        if(LoginActivity.online){
            races = LoginActivity.mongo.getSavedRace(username);
        }

        ArrayList<Integer> parcoursList = new ArrayList();


        parcoursList.add(R.drawable.parcours1);
        parcoursList.add(R.drawable.parcours2);
        parcoursList.add(R.drawable.parcours3);
        parcoursList.add(R.drawable.parcours4);
        parcoursList.add(R.drawable.parcours5);
        parcoursList.add(R.drawable.parcours6);
        parcoursList.add(R.drawable.parcours7);
        parcoursList.add(R.drawable.parcours8);
        parcoursList.add(R.drawable.parcours9);
        parcoursList.add(R.drawable.parcours10);
        parcoursList.add(R.drawable.parcours11);
        parcoursList.add(R.drawable.parcours1);


        if (this.poursuite) {
            for (int i = 12; i<this.routes.size()+1; i++){

                Route route = this.routes.get(i-1);

                LinearLayout parcours = new LinearLayout(this);
                parcours.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,80*(int)factor));
                parcours.setOrientation(LinearLayout.HORIZONTAL);
                parcours.setBackground(ContextCompat.getDrawable(this,R.drawable.background_grey_stroke));
                parcours.setGravity(Gravity.CENTER);
                parcours.setClickable(true);
                parcours.setFocusable(true);

                TextView parcoursNumber = new TextView(this);
                LinearLayout.LayoutParams paramsNumberParcours = new LinearLayout.LayoutParams(180*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT);
                paramsNumberParcours.setMargins(20,0,80,0);
                parcoursNumber.setLayoutParams(paramsNumberParcours);
                String numberParcours = "Parcours "+i;
                parcoursNumber.setText(numberParcours);
                parcoursNumber.setTextSize(25);
                parcoursNumber.setTextColor(getColor(R.color.black));
                parcoursNumber.setTypeface(Typeface.DEFAULT_BOLD);
                parcoursNumber.setGravity(Gravity.CENTER);

                TextView distance = new TextView(this);
                distance.setLayoutParams(new LinearLayout.LayoutParams(280*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String distanceText = "Distance : " + route.getDistance() + " km";
                distance.setText(distanceText);
                distance.setTextSize(25);
                distance.setTextColor(getColor(R.color.black));
                distance.setTypeface(Typeface.DEFAULT_BOLD);
                distance.setGravity(Gravity.CENTER_VERTICAL);



                ImageView arrowDown = new ImageView(this);
                arrowDown.setLayoutParams(new LinearLayout.LayoutParams(80*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT));
                arrowDown.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);

                ImageView arrowUp = new ImageView(this);
                arrowUp.setLayoutParams(new LinearLayout.LayoutParams(80*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT));
                arrowUp.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                arrowUp.setVisibility(View.GONE);

                LinearLayout mapParcours = new LinearLayout(this);
                mapParcours.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,500*(int)factor));
                mapParcours.setOrientation(LinearLayout.VERTICAL);
                mapParcours.setBackground(ContextCompat.getDrawable(this,R.drawable.background_grey_stroke));
                mapParcours.setVisibility(View.GONE);

                ImageView viewParcours = new ImageView(this);
                LinearLayout.LayoutParams paramsViewParcours = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300*(int)factor);
                paramsViewParcours.setMargins(0,50,0,0);
                viewParcours.setLayoutParams(paramsViewParcours);
                viewParcours.setImageResource(parcoursList.get(i-1));



                LinearLayout DALayout = new LinearLayout(this);
                DALayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                DALayout.setOrientation(LinearLayout.HORIZONTAL);
                DALayout.setGravity(Gravity.CENTER);

                TextView start = new TextView(this);
                start.setLayoutParams(new LinearLayout.LayoutParams(450*(int)factor, 80*(int)factor));
                String startText = "Départ : " + route.getStartWP().getName();
                start.setText(startText);
                start.setTextSize(20);
                start.setTextColor(getColor(R.color.black));
                start.setTypeface(Typeface.DEFAULT_BOLD);
                start.setGravity(Gravity.CENTER);

                TextView finish = new TextView(this);
                finish.setLayoutParams(new LinearLayout.LayoutParams(450*(int)factor, 80*(int)factor));
                String finishText = "Arrivée : " + route.getEndWP().getName();
                finish.setText(finishText);
                finish.setTextSize(20);
                finish.setTextColor(getColor(R.color.black));
                finish.setTypeface(Typeface.DEFAULT_BOLD);
                finish.setGravity(Gravity.CENTER);

                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER);

                TextView backButton = new TextView(this);
                backButton.setText(getString(R.string.fermer));
                LinearLayout.LayoutParams paramsBackButton = new LinearLayout.LayoutParams(200*(int)factor, 60*(int)factor);
                paramsBackButton.setMargins(0,0,300,0);
                backButton.setLayoutParams(paramsBackButton);
                backButton.setTextSize(25);
                backButton.setTextColor(getColor(R.color.black));
                backButton.setTypeface(Typeface.DEFAULT_BOLD);
                backButton.setBackground(ContextCompat.getDrawable(this,R.drawable.background_white));
                backButton.setGravity(Gravity.CENTER);
                backButton.setClickable(true);
                backButton.setFocusable(true);

                TextView choiceButton = new TextView(this);
                choiceButton.setText(getString(R.string.choisir));
                LinearLayout.LayoutParams paramsChoiceButton = new LinearLayout.LayoutParams(200*(int)factor, 60*(int)factor);
                paramsChoiceButton.setMargins(300,0,0,0);
                choiceButton.setLayoutParams(paramsChoiceButton);
                choiceButton.setTextSize(25);
                choiceButton.setTextColor(getColor(R.color.black));
                choiceButton.setTypeface(Typeface.DEFAULT_BOLD);
                choiceButton.setBackground(ContextCompat.getDrawable(this,R.drawable.background_blue_noshadow));
                choiceButton.setGravity(Gravity.CENTER);
                choiceButton.setClickable(true);
                choiceButton.setFocusable(true);



                parcours.setOnClickListener(view -> {
                    if (mapParcours.getVisibility() == View.GONE){
                        mapParcours.setVisibility(View.VISIBLE);
                        arrowDown.setVisibility(View.GONE);
                        arrowUp.setVisibility(View.VISIBLE);}
                    else {mapParcours.setVisibility(View.GONE);
                        arrowUp.setVisibility(View.GONE);
                        arrowDown.setVisibility(View.VISIBLE);}

                });
                int finalI = i;

                choiceButton.setOnClickListener(view -> {
                    this.nextStep(finalI);
                    mapParcours.setVisibility(View.GONE);
                    arrowUp.setVisibility(View.GONE);
                    arrowDown.setVisibility(View.VISIBLE);
                });
                backButton.setOnClickListener(view -> {
                    mapParcours.setVisibility(View.GONE);
                    arrowUp.setVisibility(View.GONE);
                    arrowDown.setVisibility(View.VISIBLE);
                });



                parcours.addView(parcoursNumber);
                parcours.addView(distance);

                if(LoginActivity.online && races != null) {
                    TextView record = new TextView(this);
                    record.setLayoutParams(new LinearLayout.LayoutParams(350 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                    String recordText;
                    if (getRecords(races, route.getId()).equals("")){ recordText = "Pas de record établi"; }
                    else { recordText = "Record : " + getRecords(races, route.getId());}
                    record.setText(recordText);
                    record.setTextSize(25);
                    record.setTextColor(getColor(R.color.black));
                    record.setTypeface(Typeface.DEFAULT_BOLD);
                    record.setGravity(Gravity.CENTER_VERTICAL);


                    parcours.addView(record);
                }

                parcours.addView(arrowDown);
                parcours.addView(arrowUp);
                DALayout.addView(start);
                DALayout.addView(finish);
                mapParcours.addView(viewParcours);
                mapParcours.addView(DALayout);
                buttonLayout.addView(backButton);
                buttonLayout.addView(choiceButton);
                mapParcours.addView(buttonLayout);
                layoutRoutes.addView(parcours);
                layoutRoutes.addView(mapParcours);
            }
        }
        else {
            for (int i = 1; i<this.routes.size(); i++){

                Route route = this.routes.get(i-1);

                LinearLayout parcours = new LinearLayout(this);
                parcours.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,80*(int)factor));
                parcours.setOrientation(LinearLayout.HORIZONTAL);
                parcours.setBackground(ContextCompat.getDrawable(this,R.drawable.background_grey_stroke));
                parcours.setGravity(Gravity.CENTER);
                parcours.setClickable(true);
                parcours.setFocusable(true);

                TextView parcoursNumber = new TextView(this);
                LinearLayout.LayoutParams paramsNumberParcours = new LinearLayout.LayoutParams(180*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT);
                paramsNumberParcours.setMargins(20,0,80,0);
                parcoursNumber.setLayoutParams(paramsNumberParcours);
                String numberParcours = "Parcours "+i;
                parcoursNumber.setText(numberParcours);
                parcoursNumber.setTextSize(25);
                parcoursNumber.setTextColor(getColor(R.color.black));
                parcoursNumber.setTypeface(Typeface.DEFAULT_BOLD);
                parcoursNumber.setGravity(Gravity.CENTER);

                TextView distance = new TextView(this);
                distance.setLayoutParams(new LinearLayout.LayoutParams(280*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String distanceText = "Distance : " + route.getDistance() + " km";
                distance.setText(distanceText);
                distance.setTextSize(25);
                distance.setTextColor(getColor(R.color.black));
                distance.setTypeface(Typeface.DEFAULT_BOLD);
                distance.setGravity(Gravity.CENTER_VERTICAL);



                ImageView arrowDown = new ImageView(this);
                arrowDown.setLayoutParams(new LinearLayout.LayoutParams(80*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT));
                arrowDown.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24);

                ImageView arrowUp = new ImageView(this);
                arrowUp.setLayoutParams(new LinearLayout.LayoutParams(80*(int)factor, ViewGroup.LayoutParams.MATCH_PARENT));
                arrowUp.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                arrowUp.setVisibility(View.GONE);

                LinearLayout mapParcours = new LinearLayout(this);
                mapParcours.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,500*(int)factor));
                mapParcours.setOrientation(LinearLayout.VERTICAL);
                mapParcours.setBackground(ContextCompat.getDrawable(this,R.drawable.background_grey_stroke));
                mapParcours.setVisibility(View.GONE);

                ImageView viewParcours = new ImageView(this);
                LinearLayout.LayoutParams paramsViewParcours = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300*(int)factor);
                paramsViewParcours.setMargins(0,50,0,0);
                viewParcours.setLayoutParams(paramsViewParcours);
                viewParcours.setImageResource(parcoursList.get(i-1));



                LinearLayout DALayout = new LinearLayout(this);
                DALayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                DALayout.setOrientation(LinearLayout.HORIZONTAL);
                DALayout.setGravity(Gravity.CENTER);

                TextView start = new TextView(this);
                start.setLayoutParams(new LinearLayout.LayoutParams(450*(int)factor, 80*(int)factor));
                String startText = "Départ : " + route.getStartWP().getName();
                start.setText(startText);
                start.setTextSize(20);
                start.setTextColor(getColor(R.color.black));
                start.setTypeface(Typeface.DEFAULT_BOLD);
                start.setGravity(Gravity.CENTER);

                TextView finish = new TextView(this);
                finish.setLayoutParams(new LinearLayout.LayoutParams(450*(int)factor, 80*(int)factor));
                String finishText = "Arrivée : " + route.getEndWP().getName();
                finish.setText(finishText);
                finish.setTextSize(20);
                finish.setTextColor(getColor(R.color.black));
                finish.setTypeface(Typeface.DEFAULT_BOLD);
                finish.setGravity(Gravity.CENTER);

                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.CENTER);

                TextView backButton = new TextView(this);
                backButton.setText(getString(R.string.fermer));
                LinearLayout.LayoutParams paramsBackButton = new LinearLayout.LayoutParams(200*(int)factor, 60*(int)factor);
                paramsBackButton.setMargins(0,0,300,0);
                backButton.setLayoutParams(paramsBackButton);
                backButton.setTextSize(25);
                backButton.setTextColor(getColor(R.color.black));
                backButton.setTypeface(Typeface.DEFAULT_BOLD);
                backButton.setBackground(ContextCompat.getDrawable(this,R.drawable.background_white));
                backButton.setGravity(Gravity.CENTER);
                backButton.setClickable(true);
                backButton.setFocusable(true);

                TextView choiceButton = new TextView(this);
                choiceButton.setText(getString(R.string.choisir));
                LinearLayout.LayoutParams paramsChoiceButton = new LinearLayout.LayoutParams(200*(int)factor, 60*(int)factor);
                paramsChoiceButton.setMargins(300,0,0,0);
                choiceButton.setLayoutParams(paramsChoiceButton);
                choiceButton.setTextSize(25);
                choiceButton.setTextColor(getColor(R.color.black));
                choiceButton.setTypeface(Typeface.DEFAULT_BOLD);
                choiceButton.setBackground(ContextCompat.getDrawable(this,R.drawable.background_blue_noshadow));
                choiceButton.setGravity(Gravity.CENTER);
                choiceButton.setClickable(true);
                choiceButton.setFocusable(true);



                parcours.setOnClickListener(view -> {
                    if (mapParcours.getVisibility() == View.GONE){
                        mapParcours.setVisibility(View.VISIBLE);
                        arrowDown.setVisibility(View.GONE);
                        arrowUp.setVisibility(View.VISIBLE);}
                    else {mapParcours.setVisibility(View.GONE);
                        arrowUp.setVisibility(View.GONE);
                        arrowDown.setVisibility(View.VISIBLE);}

                });
                int finalI = i;

                choiceButton.setOnClickListener(view -> {
                    this.nextStep(finalI);
                    mapParcours.setVisibility(View.GONE);
                    arrowUp.setVisibility(View.GONE);
                    arrowDown.setVisibility(View.VISIBLE);
                });
                backButton.setOnClickListener(view -> {
                    mapParcours.setVisibility(View.GONE);
                    arrowUp.setVisibility(View.GONE);
                    arrowDown.setVisibility(View.VISIBLE);
                });



                parcours.addView(parcoursNumber);
                parcours.addView(distance);

                if(LoginActivity.online && races != null) {
                    TextView record = new TextView(this);
                    record.setLayoutParams(new LinearLayout.LayoutParams(350 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                    String recordText;
                    if (getRecords(races, route.getId()).equals("")){ recordText = "Pas de record établi"; }
                    else { recordText = "Record : " + getRecords(races, route.getId());}
                    record.setText(recordText);
                    record.setTextSize(25);
                    record.setTextColor(getColor(R.color.black));
                    record.setTypeface(Typeface.DEFAULT_BOLD);
                    record.setGravity(Gravity.CENTER_VERTICAL);


                    parcours.addView(record);
                }

                parcours.addView(arrowDown);
                parcours.addView(arrowUp);
                DALayout.addView(start);
                DALayout.addView(finish);
                mapParcours.addView(viewParcours);
                mapParcours.addView(DALayout);
                buttonLayout.addView(backButton);
                buttonLayout.addView(choiceButton);
                mapParcours.addView(buttonLayout);
                layoutRoutes.addView(parcours);
                layoutRoutes.addView(mapParcours);
            }
        }


    }
    public void home(@NonNull View view){
        logger.log(Logger.Severity.Debug, "Going back to the last page from RideChoice page");
        this.finish();
    }
    public void nextStep(int idRoute){
        logger.log(Logger.Severity.Debug, "Launching a ride from RideChoice");
        if(bluetooth == null) {
            if (!btSimulatorModeEnabled) {
                bluetooth = new BtLink();
                logger.log(Logger.Severity.Debug,"Vraie Bluetooth");
            } else {
                bluetooth = new BtSimulation();
                logger.log(Logger.Severity.Debug,"Bluetooth simulation");
            }
            bluetooth.startBluetooth();
        }
        if (LoginActivity.online) {
            Map<String, Boolean> users = new ArrayMap<>();
            try {
                 users = LoginActivity.mongo.getAllUserName();}
            catch (Exception e) {
                logger.log(Logger.Severity.Error,e.toString());
            }
            if (users.size() == 0){
                Intent intent = new Intent(this, Ride.class);
                Bundle rideChoiceBundle = new Bundle();
                rideChoiceBundle.putInt("idRoute", idRoute); //Your id
                intent.putExtras(rideChoiceBundle); //Put your id to your next Intent
                startActivity(intent);
            }
            else {
                Intent intent = new Intent(this, OpponentChoice.class);
                Bundle rideChoiceBundle = new Bundle();
                rideChoiceBundle.putInt("idRoute", idRoute); //Your id
                rideChoiceBundle.putString("username", this.username);
                rideChoiceBundle.putBoolean("Poursuite", this.poursuite);
                intent.putExtra("UserView", this.userview);
                intent.putExtras(rideChoiceBundle); //Put your id to your next Intent
                startActivity(intent);
            }
        }
        else {

            Intent intent = new Intent(this, Ride.class);
            Bundle rideChoiceBundle = new Bundle();
            rideChoiceBundle.putInt("idRoute", idRoute); //Your id
            intent.putExtras(rideChoiceBundle); //Put your id to your next Intent
            startActivity(intent);
        }
    }

    @Override
    public void onTaskDone(@NonNull Object... values) {
        this.routes = (ArrayList<Route>) values[0];
        setUpGraphicsItems();
    }
}
