package com.dev.cyclo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dev.cyclo.realm.RemoteData;
import com.dev.cyclo.ride.Ride;
import com.dev.cyclo.ui.login.LoggedInUserView;
import com.dev.cyclo.ui.login.LoginActivity;

import java.util.*;

/**
 * This object allows the user to choose if he wants to do the race alone or with an opponent that he can choose
 */
public class OpponentChoice extends Activity {

    private final Logger logger = new Logger(this.getClass());
    private int idRoute;
    private String username;
    private String opponentUsername;
    private Boolean poursuite;
    private LoggedInUserView userview;
    private Boolean invited = false;


    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opponent_choice);

        Bundle rideChoiceBundle = getIntent().getExtras();
        this.idRoute = -1; // or other values
        this.username = "";
        if (rideChoiceBundle != null) {
            this.idRoute = rideChoiceBundle.getInt("idRoute");
            this.username = rideChoiceBundle.getString("username");
            this.poursuite = rideChoiceBundle.getBoolean("Poursuite");
            Intent intent = getIntent();

            this.userview = (LoggedInUserView) intent.getSerializableExtra("UserView");
            if (this.poursuite == true) {
                System.out.println("Second poursuite is True");
            }
            else{
                System.out.println("Second poursuite is False");
            }
        } else {
            logger.log(Logger.Severity.Info, "don't receive well intent parameter ");
        }
        logger.log(Logger.Severity.Info, "username : " + this.username+ " idRoute : " + this.idRoute);


        logger.log(Logger.Severity.Debug, "Launching OpponentChoice page");

        LinearLayout layoutFantome = findViewById(R.id.layoutFantome);
        ImageView leftArrow = findViewById(R.id.leftArrow);
        ImageView rightArrow = findViewById(R.id.rightArrow);
        ImageView homeButton = findViewById(R.id.homeButton);
        TextView soloRideButton = findViewById(R.id.soloRideButton);
        TextView choiceOpponentButton = findViewById(R.id.chooseOpponentButton);
        homeButton.setOnClickListener(this::home);

        //Getting the list of the possible opponents
        List<String> namesList = new LinkedList<>();
        if(LoginActivity.online){
            Map<String, Boolean> users = LoginActivity.mongo.getAllUserName();
            Set<String> keySet = users.keySet();
            for(String username : keySet){
                if(users.get(username)){
                    if(!username.equals(this.username)){
                        namesList.add(username);
                    }
                }
            }
             }
        else {
            //not called because opponentChoice is called only with online mode now
            namesList = new LinkedList<>(Collections.singleton("Offline"));
        }

        logger.log(Logger.Severity.Error, namesList.toString());

        int tailleListe = namesList.size();
        final int[] i = {0};

        //Setting the graphic interface with the choice of the opponent
        if (tailleListe != 0) {
            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            String distanceText = namesList.get(i[0]);
            name.setText(distanceText);
            name.setTextSize(25);
            name.setTextColor(getColor(R.color.black));
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setGravity(Gravity.CENTER);
            layoutFantome.setGravity(Gravity.CENTER);
            layoutFantome.addView(name);

            this.opponentUsername = namesList.get(i[0]);
            name.setText(this.opponentUsername);

            List<String> finalNamesList = namesList;
            leftArrow.setOnClickListener(v -> {
                if (i[0] > 0) {
                    i[0] -= 1;
                } else {
                    i[0] = tailleListe - 1;
                }
                logger.log(Logger.Severity.Error, "indice :" + i[0]);
                this.opponentUsername = finalNamesList.get(i[0]);
                name.setText(this.opponentUsername);
            });
            List<String> finalNamesList1 = namesList;
            rightArrow.setOnClickListener(v -> {
                if (i[0] < tailleListe - 1) {
                    i[0] += 1;
                } else {
                    i[0] = 0;
                }
                this.opponentUsername = finalNamesList1.get(i[0]);
                name.setText(this.opponentUsername);
            });
            choiceOpponentButton.setOnClickListener(v -> {
                RemoteData remoteData = LoginActivity.mongo.getRemoteData(this.opponentUsername);
                this.invited = remoteData.isReady();
                RemoteData CurrentUser = LoginActivity.mongo.getRemoteData(this.username);
                CurrentUser.setReady(true);
                Intent intent = new Intent(this, Ride.class);
                Bundle rideBundle = new Bundle();
                rideBundle.putInt("idRoute", idRoute); //Your id
                rideBundle.putString("username", this.username);
                rideBundle.putBoolean("reset",true);
                rideBundle.putBoolean("invited",this.invited);
                rideBundle.putString("opponentUsername", this.opponentUsername);
                rideBundle.putBoolean("Poursuite", this.poursuite);
                intent.putExtra("UserView", this.userview);
                intent.putExtras(rideBundle); //Put your id to your next Intent
                startActivity(intent);
            });
        }
        else {
            TextView name = new TextView(this);
            name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            String distanceText = "Aucun utilisateur connecté";
            name.setText(distanceText);
            name.setTextSize(25);
            name.setTextColor(getColor(R.color.black));
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setGravity(Gravity.CENTER);
            layoutFantome.setGravity(Gravity.CENTER);
            layoutFantome.addView(name);

            choiceOpponentButton.setVisibility(View.GONE);
        }
        soloRideButton.setOnClickListener(v->{
            Intent intent = new Intent(this, Ride.class);
            Bundle rideBundle = new Bundle();
            rideBundle.putInt("idRoute", idRoute); //Your id
            rideBundle.putString("username", this.username);
            rideBundle.putBoolean("reset",true);
            rideBundle.putBoolean("Poursuite", this.poursuite);
            intent.putExtra("UserView", this.userview);
            intent.putExtras(rideBundle); //Put your id to your next Intent
            startActivity(intent);
        });

    }
    //Going back to the previous page
    public void home(View view){
        logger.log(Logger.Severity.Debug, "Going back to the last page from OpponentChoice page");
        this.finish();
    }

}
