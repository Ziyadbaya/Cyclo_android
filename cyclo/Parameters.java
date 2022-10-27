package com.dev.cyclo;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import org.json.JSONException;

/**
 * This object is the page were the user can change his profile or his preferences regarding the indicators displayed in the race page
 */
public class Parameters extends Activity {

    TextView profilButton;
    TextView indicateursButton;
    ImageView homeButton;
    SwitchCompat switchVitesse;
    SwitchCompat switchCadence;
    GridLayout gridIndicateurs;
    GridLayout gridProfil;

    private final Logger logger = new Logger(this.getClass());

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameters);

        switchVitesse = new SwitchCompat(this);
        switchCadence = new SwitchCompat(this);
        profilButton = findViewById(R.id.profilButton);
        indicateursButton = findViewById(R.id.indicateursButton);
        homeButton = findViewById(R.id.homeButton);
        switchVitesse = findViewById(R.id.switchVitesse);
        switchCadence = findViewById(R.id.switchCadence);
        gridIndicateurs = findViewById(R.id.indicateursGrid);
        gridProfil = findViewById(R.id.profilGrid);

        findViewById(R.id.retourButton).setOnClickListener(this::home);

        // to what to do on validate
        findViewById(R.id.validerButton).setOnClickListener(v -> {
            try {
                Main.config.modifySpeedIndicatorVisibility(switchVitesse.isChecked());
                Main.config.modifyCadenceIndicatorVisibility(switchCadence.isChecked());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            this.home(v);
        });


        switchVitesse.setChecked(Main.config.getConfigData().isSpeedIndicatorShown());
        switchCadence.setChecked(Main.config.getConfigData().isCadenceIndicatorShown());


        profilButton.setOnClickListener(v -> {
            profilButton.setAlpha(1);
            indicateursButton.setAlpha((float)0.5);
            gridIndicateurs.setVisibility(View.GONE);
            gridProfil.setVisibility(View.VISIBLE);
        });
        indicateursButton.setOnClickListener(v -> {
            profilButton.setAlpha((float)0.5);
            indicateursButton.setAlpha(1);
            gridIndicateurs.setVisibility(View.VISIBLE);
            gridProfil.setVisibility(View.GONE);
        });
        homeButton.setOnClickListener(this::home);

}
    public void home(@NonNull View v){
        logger.log(Logger.Severity.Debug, "Going back to the last page from Parameters page");
        this.finish();
    }
}
