package com.dev.cyclo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dev.cyclo.Main;
import com.dev.cyclo.ride.Course;
import com.dev.cyclo.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class Distance extends Fragment {


    /**
     * Display attributes : the progress bar and the text view
     */
    private ProgressBar distance_bar;
    private TextView distance_text;

    /**
     * Parameters needed to compute the distance and display it in the progress bar
     */
    private double progress = 0;
    private Long currentTime;
    private Long lastTime;

    /**
     * Handler to update the progress bar during the course
     */
    private final Handler handler = new Handler();

    /**
     * Total distance of the course (parameter from Course)
     * @see Course
     */
    private double distanceCourse;

    /**
     * Boolean to know if you are at the end of the course
     */
    private boolean end;

    public Distance(double distanceCourse) {
        this.distanceCourse = distanceCourse;
        end = false;
    }

    public Distance(){}


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    /**
     * Inflate the layout for this fragment, set the attributes for future update
     * Set the visibility following the user's parameters
     * @see com.dev.cyclo.Parameters
     * @see com.dev.cyclo.config.Config
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return the view of the fragment
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_distance, container, false);
        distance_bar = fragment.findViewById(R.id.distance_bar);
        distance_text = fragment.findViewById(R.id.distance_text);
        distance_bar.setMax(100);

        if (!Main.config.getConfigData().isProgressBarShown()){
            fragment.setVisibility(View.GONE);
        }
        return fragment;
    }



    public void setCurrentTime() {
        this.currentTime = SystemClock.elapsedRealtime();
    }


    public void setLastTime(){
        this.lastTime = SystemClock.elapsedRealtime();
    }

    /**
     * Compute the progress made between two instants
     * Update the progress bar and the text view the distance already done during the ride
     * @param Speed double
     */
    @SuppressLint("SetTextI18n")
    public void updateDistance(double Speed){
        if(lastTime != null) {
            lastTime = currentTime;
            currentTime = SystemClock.elapsedRealtime();

            float deltaT = (float) (currentTime - lastTime);
            double delta = (Speed) * deltaT / 1e6 / 3.6;
            progress += delta;
        }


        handler.post(() -> {
            int progress_bar = (int) (progress/distanceCourse*100);
            distance_bar.setProgress(progress_bar);
            DecimalFormat df = new DecimalFormat("#0.00");
            df.setRoundingMode(RoundingMode.DOWN);
            String dfProgress = df.format(progress);
            distance_text.setText(dfProgress+" km");
            if(progress >= distanceCourse)  {
                    distance_bar.setProgress(100);
                    distance_text.setText("Course finie !");
                    end = true;
                }
        });
    }

    /**
     * Get the progress on the distance between two updates
     * @return double progress
     */
    public double getProgress(){
        return progress;
    }

    /**
     * True if the ride is finished
     * @return boolean end
     */
    public boolean getEnd(){ return end;}




    /**
     * to be called at the end of the ride
     */
    public void reset(){
        end = false;
        progress = 0;
        distanceCourse = 0;
    }

    /**
     * to be called at to beginning of a ride
     */
    public void start(){
        this.setCurrentTime();
        this.setLastTime();
    }

}