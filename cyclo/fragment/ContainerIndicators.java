package com.dev.cyclo.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.dev.cyclo.Main;
import com.dev.cyclo.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class ContainerIndicators extends Fragment {

    /**
     * Textview of the speed and the cadence as attributes
     */
    private TextView speed;
    private TextView cadence;

    /**
     * Every fragment must have an empty public constructor
     */
    public ContainerIndicators() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Inflate the layout for this fragment
     * Set the textviews for future update
     * Handle the visibility of the speed and/or the cadence in function of the user's parameters
     * @see com.dev.cyclo.Parameters
     * @see com.dev.cyclo.config.Config
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_container_indicators, container, false);
        speed = fragment.findViewById(R.id.speed);
        cadence = fragment.findViewById(R.id.cadence);

        TableRow speedTableRow = fragment.findViewById(R.id.speedTable);
        if (!Main.config.getConfigData().isSpeedIndicatorShown()){
            speedTableRow.setVisibility(View.GONE);
        }
        TableRow cadenceTableRow = fragment.findViewById(R.id.cadenceTable);
        if (!Main.config.getConfigData().isCadenceIndicatorShown()){
            cadenceTableRow.setVisibility(View.GONE);
        }
        if ((!Main.config.getConfigData().isSpeedIndicatorShown()) && (!Main.config.getConfigData().isCadenceIndicatorShown())){
            fragment.setVisibility(View.GONE);
        }
        return fragment;
    }

    /**
     * Update the indicators during the course with the good format
     * @param Speed double
     * @param Cadence double
     */
    public void updateIndicators(double Speed, double Cadence){

        DecimalFormat df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.DOWN);
        String dfSpeed = df.format(Speed);
        String dfCadence = df.format(Cadence);
        speed.setText(dfSpeed);
        cadence.setText(dfCadence);
    }
}