package com.dev.cyclo;

import android.app.Activity;
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

import androidx.core.content.ContextCompat;

import com.dev.cyclo.ride.model.SavedRace;
import com.dev.cyclo.ui.login.LoginActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * This object show the past races of the user
 */
public class Historique extends Activity {


    TextView incDates;
    TextView decDates;
    ImageView homeButton;
    LinearLayout historiqueData;
    String username;
    LinkedList<SavedRace> savedRaces;
    float factor;

    private final Logger logger = new Logger(this.getClass());

    protected void onCreate(Bundle savedInstanceState) {
        //Hiding the action and the status bars
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        Bundle menuBundle = getIntent().getExtras();
        this.username = menuBundle.getString("username");

        setContentView(R.layout.historique);

        incDates = findViewById(R.id.incDatesButton);
        decDates = findViewById(R.id.decDatesButton);
        homeButton = findViewById(R.id.homeButton);
        historiqueData = findViewById(R.id.historiqueData);
        this.factor = historiqueData.getContext().getResources().getDisplayMetrics().density;

        incDates.setOnClickListener(v -> {
            incDates.setAlpha(1);
            decDates.setAlpha((float)0.5);
        });
        decDates.setOnClickListener(v -> {
            decDates.setAlpha(1);
            incDates.setAlpha((float)0.5);
        });

        homeButton.setOnClickListener(this::home);
        try {
            savedRaces = (LinkedList<SavedRace>) LoginActivity.mongo.getSavedRace(this.username);
            setUpGraphicsItems();
        }
        catch (Exception e){
            logger.log(Logger.Severity.Error, e.toString());
        }



    }
    //Function to finish the Historique activity and go back to the last page
    public void home(View v){
        logger.log(Logger.Severity.Debug, "Going back to the last page from RideChoice page");
        this.finish();
    }
    private void setUpGraphicsItems(){
        if (savedRaces.size() !=0) {
            for (SavedRace savedRace : savedRaces) {
                LinearLayout parcours = new LinearLayout(this);
                parcours.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 160 * (int) factor));
                parcours.setOrientation(LinearLayout.VERTICAL);
                parcours.setBackground(ContextCompat.getDrawable(this, R.drawable.background_grey_stroke));
                parcours.setGravity(Gravity.CENTER);

                LinearLayout firstLine = new LinearLayout(this);
                firstLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80 * (int) factor));
                firstLine.setOrientation(LinearLayout.HORIZONTAL);
                firstLine.setGravity(Gravity.CENTER);

                LinearLayout secondLine = new LinearLayout(this);
                secondLine.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 80 * (int) factor));
                secondLine.setOrientation(LinearLayout.HORIZONTAL);
                secondLine.setGravity(Gravity.CENTER);

                TextView parcoursNumber = new TextView(this);
                parcoursNumber.setLayoutParams(new LinearLayout.LayoutParams(300 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String numberParcours = "Parcours " + savedRace.getRouteId();
                parcoursNumber.setText(numberParcours);
                parcoursNumber.setTextColor(getColor(R.color.black));
                parcoursNumber.setTextSize(25);
                parcoursNumber.setTypeface(Typeface.DEFAULT_BOLD);
                parcoursNumber.setGravity(Gravity.CENTER);
                firstLine.addView(parcoursNumber);

                TextView distance = new TextView(this);
                distance.setLayoutParams(new LinearLayout.LayoutParams(150 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String distanceText = savedRace.getTotalDistance() + "km";
                distance.setText(distanceText);
                distance.setTextColor(getColor(R.color.black));
                distance.setTextSize(25);
                distance.setTypeface(Typeface.DEFAULT_BOLD);
                distance.setGravity(Gravity.CENTER);
                firstLine.addView(distance);

                TextView date = new TextView(this);
                date.setLayoutParams(new LinearLayout.LayoutParams(450 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String strDate = (String) android.text.format.DateFormat.format("dd-MM-yyyy kk:mm", savedRace.getDate());
                String dateText = "" + strDate;
                date.setText(dateText);
                date.setTextColor(getColor(R.color.black));
                date.setTextSize(25);
                date.setTypeface(Typeface.DEFAULT_BOLD);
                date.setGravity(Gravity.CENTER);
                firstLine.addView(date);

                TextView temps = new TextView(this);
                temps.setLayoutParams(new LinearLayout.LayoutParams(300 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                double time = savedRace.getTime();
                double timeSecond = time / 1000;
                int timeMinute = (int) timeSecond / 60;
                int timeHour = timeMinute / 60;
                DecimalFormat df = new DecimalFormat("#");
                df.setRoundingMode(RoundingMode.DOWN);
                timeSecond = timeSecond - timeHour * 3600 - timeMinute * 60;
                String timeSecondStr = df.format(timeSecond);
                String msgTime;
                if (timeHour != 0) {
                    msgTime = timeHour + " h " + timeMinute + " min " + timeSecondStr + " s";
                } else {
                    msgTime = timeMinute + " min " + timeSecondStr + " s";
                }
                temps.setText(msgTime);
                temps.setTextColor(getColor(R.color.black));
                temps.setTextSize(25);
                temps.setTypeface(Typeface.DEFAULT_BOLD);
                temps.setGravity(Gravity.CENTER);
                secondLine.addView(temps);

                TextView opponent = new TextView(this);
                opponent.setLayoutParams(new LinearLayout.LayoutParams(450 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String opponentText;
                if (!savedRace.getOpponentUsername().equals("null")) {
                    opponentText = "Course contre " + savedRace.getOpponentUsername();
                } else {
                    opponentText = "";
                }
                opponent.setText(opponentText);
                opponent.setTextColor(getColor(R.color.black));
                opponent.setTextSize(25);
                opponent.setTypeface(Typeface.DEFAULT_BOLD);
                opponent.setGravity(Gravity.CENTER);
                secondLine.addView(opponent);

                TextView winner = new TextView(this);
                winner.setLayoutParams(new LinearLayout.LayoutParams(150 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
                String winnerText;
                if (!savedRace.getOpponentUsername().equals("null")) {
                    if (savedRace.isResult()) {
                        winnerText = "Gagnée";
                    } else {
                        winnerText = "Perdue";
                    }
                } else {
                    winnerText = "";
                }
                winner.setText(winnerText);
                winner.setTextColor(getColor(R.color.black));
                winner.setTextSize(25);
                winner.setTypeface(Typeface.DEFAULT_BOLD);
                winner.setGravity(Gravity.CENTER);
                secondLine.addView(winner);

                parcours.addView(firstLine);
                parcours.addView(secondLine);
                historiqueData.addView(parcours);
            }
        }
    }
}
