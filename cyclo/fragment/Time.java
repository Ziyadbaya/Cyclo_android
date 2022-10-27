package com.dev.cyclo.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;

import com.dev.cyclo.Main;
import com.dev.cyclo.R;


public class Time extends Fragment {

    /**
     * Chronometer to count your time to finish the ride
     */
    private Chronometer chrono;

    /**
     * Every fragment must have an empty public constructor
     */
    public Time() {

    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Inflate the layout for the fragment, set the chrono and the visibility following user's preferences
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_time, container, false);
        chrono = fragment.findViewById(R.id.time);

        if (!Main.config.getConfigData().isProgressBarShown()){
            fragment.setVisibility(View.GONE);
        }
        return fragment;
    }

    /**
     * Get the initial time used for the start of the Chronometer
     * @return Long BaseTime
     */
    public long getBaseTime(){
        return chrono.getBase();
    }

    /**
     * Stop the Chronometer
     */
    public void stopTime(){
        chrono.stop();
    }

    /**
     * Initialize and start the Chronometer
     */
    public void startTime(){
        long elapsedRealtime = SystemClock.elapsedRealtime();
        chrono.setBase(elapsedRealtime);
        chrono.start();
    }

    /**
     * Compute the delta time between the current time and the inital time of the chronometer
     * @return Long deltaTime
     */
    public long deltaTime(){
        return SystemClock.elapsedRealtime()-getBaseTime();
    }
}