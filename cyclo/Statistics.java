package com.dev.cyclo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.GridLayout;
import com.dev.cyclo.ride.model.SavedRace;
import com.dev.cyclo.ui.login.LoginActivity;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * This object is the page were the user can see his statistics like the number of races done.
 */
public class Statistics extends Activity {

    ImageView homeButton;
    TextView nbCourses;
    TextView nbCoursesGagnees;
    TextView nbCoursesPerdues;
    TextView nbKm;
    TextView tempsTotal;
    GridLayout gridResume;

    private final Logger logger = new Logger(this.getClass());

    protected void onCreate(Bundle savedInstanceState) {
        //Hiding the action and the status bars
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.indicators);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        List<SavedRace> races = LoginActivity.mongo.getSavedRace(username);


        homeButton = findViewById(R.id.homeButton);
        gridResume = findViewById(R.id.resumeGrid);

        nbCourses = findViewById(R.id.nbCoursesEffectuees);
        nbCoursesGagnees = findViewById(R.id.nbCoursesGagnees);
        nbCoursesPerdues = findViewById(R.id.nbCoursesPerdues);
        nbKm = findViewById(R.id.nbKm);
        tempsTotal = findViewById(R.id.tempsTotal);

        String nbRide = Integer.toString(races.size());

        homeButton.setOnClickListener(this::home);
        nbCourses.setText(nbRide);
        nbCoursesGagnees.setText(getNbWinRace(races));
        nbCoursesPerdues.setText(getNbLostRace(races));
        nbKm.setText(getTotalKm(races));
        tempsTotal.setText(getTotalTime(races));

    }
    //Function to go back to the previous page
    public void home(View v){
        logger.log(Logger.Severity.Debug, "Going back to the last page from Statistics page");
        this.finish();
    }
    // Getting the number of races that the user did
    private String getNbLostRace(List<SavedRace> races){
        return Long.toString (races.stream().filter(SavedRace::isResult).count());
    }
    //Getting the number of races that the user won
    private String getNbWinRace(List<SavedRace> races){
        return Long.toString(races.stream().filter(savedRace -> !savedRace.isResult()).count());
    }
   //Getting the number of km did by the user by adding the distance of all races done
    private String getTotalKm(List<SavedRace> races){
        double totalKm = 0;

        for(SavedRace race : races){
            totalKm += race.getTotalDistance();
        }
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.DOWN);
        String totalKmStr = df.format(totalKm);
        return totalKmStr +" km";
    }
    //Getting the time that the user spend in races by adding the time of all races
    private String getTotalTime(List<SavedRace> races){
        double time = 0;

        for(SavedRace race : races){
            time += race.getTime();
        }
        double timeSecond = time / 1000;
        int timeMinute = (int) timeSecond / 60;
        int timeHour = timeMinute / 60;
        DecimalFormat df = new DecimalFormat("#");
        df.setRoundingMode(RoundingMode.DOWN);
        timeSecond = timeSecond - timeHour * 3600 - timeMinute * 60;
        String timeSecondStr = df.format(timeSecond);
        if (timeHour != 0){ return timeHour + "h " + timeMinute + "min "; }
        else { return timeMinute + "min " + timeSecondStr + "s";}
    }

}
