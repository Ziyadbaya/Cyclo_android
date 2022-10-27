package com.dev.cyclo;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.dev.cyclo.data.LoginRepository;
import com.dev.cyclo.ui.login.LoggedInUserView;
import com.dev.cyclo.ui.login.LoginActivity;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This object is the center page of the application. It's the first page that the user see after his connexion.
 */
public class Menu extends Parameters implements LifecycleOwner {


    private AppCompatActivity ac;

    LoggedInUserView userView;

    private TextView textProfil;
    private TextView textAmis;
    private TextView textProfilHide;
    private TextView textAmisHide;
    private String username;
    private String pimage;






    private LinearLayout layoutLeftSideData;
    private LinearLayout layoutRightSideData;

    private final Logger logger = new Logger(this.getClass());

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.R)
    protected void onCreate(Bundle savedInstanceState) {

        //ac = new AppCompatActivity(this.getTaskId());


        /*MenuActivityViewModel model = new ViewModelProvider(this.ac).get(MenuActivityViewModel.class);
        final Observer<String> nameObserver = new Observer<String>() {
            @Override
            public void onChanged(final String newName) {
                // Update the UI, in this case, a TextView.
                System.out.println("IT CHANNNNNGEEEEEEDDDD");
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        model.getListen().observe(this, nameObserver);


        model.listen.setValue("this is test2");*/

        logger.log(Logger.Severity.Debug, "Launching Menu");
        //Hiding the action and the status bars
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);//début de l'interaction graphique


        //This where you get the userView from the intent
        Intent intent = getIntent();
        userView = (LoggedInUserView) intent.getSerializableExtra("UserView");

        //Displaying the username
        username = this.userView.getDisplayName();
        TextView userName = findViewById(R.id.userName); //affichage du nom d'utilisateur
        userName.setText(userView.getDisplayName());


        ImageView userImage = findViewById(R.id.imageView);//affichage de l'avatar
        pimage = this.userView.getDisplayImage();

        if (!pimage.equals("noImage")) {
            byte[] decodedBytes = Base64.getDecoder().decode(pimage);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            userImage.setImageBitmap(bitmap);
        }
        else {
            userImage.setImageResource(R.drawable.enac);
        }







        textProfil = findViewById(R.id.textProfil);
        textProfilHide = findViewById(R.id.textProfilHide);
        textAmis = findViewById(R.id.textAmis);

        textAmisHide = findViewById(R.id.textAmisHide);
        layoutLeftSideData = findViewById(R.id.layoutLeftSideData);
        layoutRightSideData = findViewById(R.id.layoutRightSideData);

        TextView textTitreApp = findViewById(R.id.textTitreApp);
        LinearLayout buttonRide = findViewById(R.id.layoutBoutonParcoursBleu);
        LinearLayout buttonRide2 = findViewById(R.id.layoutBoutonPoursuiteBleu);
        ImageView viewToulouse = findViewById(R.id.viewToulouse);

        LinearLayout layoutHistorique = findViewById(R.id.layoutHistorique);
        LinearLayout layoutStatistics = findViewById(R.id.layoutStatistics);
        LinearLayout layoutParameters = findViewById(R.id.layoutParametres);
        LinearLayout layoutDeconnexion = findViewById(R.id.layoutDeconnexion);


        if(!LoginActivity.online){
            layoutHistorique.setVisibility(View.INVISIBLE);
            layoutStatistics.setVisibility(View.INVISIBLE);
            String inconnu = "Inconnu";
            userName.setText(inconnu);
        }

        createGraphics();

            //Closing the profil menu by clicking on the Profil button
            textProfil.setOnClickListener(v ->

    {
        logger.log(Logger.Severity.Debug, "Closing the Profil menu on the Menu page");
        textProfil.setVisibility(View.GONE);
        layoutLeftSideData.setVisibility(View.GONE);
        textProfilHide.setVisibility(View.VISIBLE);
    });
    //Opening the profil menu by clicking on the Profil button
        textProfilHide.setOnClickListener(v ->

    {
        logger.log(Logger.Severity.Debug, "Opening the Profil menu on the Menu page");
        textProfilHide.setVisibility(View.GONE);
        textProfil.setVisibility(View.VISIBLE);
        layoutLeftSideData.setVisibility(View.VISIBLE);

    });
    //Closing the friends menu by clicking on the Amis button
        textAmis.setOnClickListener(v ->

    {
        logger.log(Logger.Severity.Debug, "Closing the Amis menu on the Menu page");
        textAmis.setVisibility(View.GONE);
        layoutRightSideData.setVisibility(View.GONE);
        textAmisHide.setVisibility(View.VISIBLE);
    });
    //Opening the friends menu by clicking on the Amis button
        textAmisHide.setOnClickListener(v ->

    {
        logger.log(Logger.Severity.Debug, "Opening the Amis menu on the Menu page");
        textAmisHide.setVisibility(View.GONE);
        textAmis.setVisibility(View.VISIBLE);
        layoutRightSideData.setVisibility(View.VISIBLE);

    });
    //Closing both menus by clicking in the center of the page
        viewToulouse.setOnClickListener(v ->

    {
        textAmis.setVisibility(View.GONE);
        layoutRightSideData.setVisibility(View.GONE);
        textAmisHide.setVisibility(View.VISIBLE);
        textProfil.setVisibility(View.GONE);
        layoutLeftSideData.setVisibility(View.GONE);
        textProfilHide.setVisibility(View.VISIBLE);
    });

    //Execute the startRide function by clicking on the Parcours button
        buttonRide.setOnClickListener(this::startRide);
        buttonRide2.setOnClickListener(this::startRidePoursuite);
        textTitreApp.setOnClickListener(this::connexionPopUpTitre);

        layoutHistorique.setOnClickListener(this::openHistorique);
        layoutStatistics.setOnClickListener(this::openIndicators);
        layoutParameters.setOnClickListener(this::openParameters);
        layoutDeconnexion.setOnClickListener(this::logOut);
}


    //Function to launch the RideChoice page
    public void startRide(View v) {
        logger.log(Logger.Severity.Debug, "Launching the RideChoice page from Menu");
        Intent intent = new Intent(this, RideChoice.class);
        Bundle bundle = new Bundle();
        bundle.putString("username", username); //Your id
        bundle.putBoolean("Poursuite", false);
        intent.putExtra("UserView", this.userView);
        intent.putExtras(bundle); //Put your id to your next Intent
        startActivity(intent);
    }

    public void startRidePoursuite(View v) {
        logger.log(Logger.Severity.Debug, "Launching the RideChoice page from Menu");
        Intent intent = new Intent(this, RideChoice.class);
        Bundle bundle = new Bundle();
        bundle.putString("username", username); //Your id
        bundle.putBoolean("Poursuite", true);
        intent.putExtra("UserView", this.userView);
        intent.putExtras(bundle); //Put your id to your next Intent
        startActivity(intent);
    }

    //Function to launch the Historique page
    public void openHistorique(View v){
        logger.log(Logger.Severity.Debug, "Launching the Historique page from Menu");
        Bundle histoBundle = new Bundle();
        histoBundle.putString("username", username);
        Intent intent = new Intent(this, Historique.class);
        intent.putExtras(histoBundle);
        startActivity(intent);
    }
    //Function to launch the Indicators page
    public void openIndicators(View v){
        logger.log(Logger.Severity.Debug, "Launching the Indicators page from Menu");
        Intent intent = new Intent(this, Statistics.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
    //Function to launch the Parameters page
    public void openParameters(View v){
        logger.log(Logger.Severity.Debug, "Launching the Parameters page from Menu");
        Intent intent = new Intent(this, Parameters.class);
        startActivity(intent);
    }
    //Function to log out
    public void logOut(View v) {

        LoginRepository.getInstance(null).logout();

        /*if(LoginActivity.online) {
            try {
                LoginActivity.mongo.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    //Function launching a pop-up in case the user is not connected to internet
    public void connexionPopUpPoursuite(View v) {
        AlertDialog.Builder connectBuilder = new AlertDialog.Builder(this);

        connectBuilder.setMessage("Souhaitez-vous vous connecter pour faire des courses poursuites avec vos amis ?")
                .setTitle("Vous-êtes hors ligne !")
                .setPositiveButton("je me connecte", (dialog, id) -> Log.i("[MENU]", "user requests connexion"))
                .setNegativeButton("retour", (dialog, id) -> {
                    Log.i("[MENU]", "user doesn't want to be connected");
                    // CANCEL
                });
        // Create the AlertDialog object and return it
        connectBuilder.create();
        connectBuilder.show();
    }
    public void connexionPopUpTitre(View v) {
        AlertDialog.Builder connectBuilder = new AlertDialog.Builder(this);

        connectBuilder.setMessage("Antoine HENRY \nBenjamin BERTHAUD \nJustine SAINT-ETIENNE \nTimothee CAMBIER")
                .setTitle("Crédits de l'application")
                .setPositiveButton("Revenir", (dialog, id) -> Log.i("[MENU]", "Retour"));
        // Create the AlertDialog object and return it
        connectBuilder.create();
        connectBuilder.show();
    }
    public void createGraphics(){
        float factor = layoutRightSideData.getContext().getResources().getDisplayMetrics().density;
        layoutRightSideData.removeAllViews();

        TextView rafraichir = new TextView(this);
        rafraichir.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (60*factor)));
        String rafraichirText = "Rafraichir";
        rafraichir.setText(rafraichirText);
        rafraichir.setTextSize(25);
        rafraichir.setTextColor(getColor(R.color.black));
        rafraichir.setTypeface(Typeface.DEFAULT_BOLD);
        rafraichir.setGravity(Gravity.CENTER);
        rafraichir.setBackground(getDrawable(R.drawable.background_red));
        layoutRightSideData.addView(rafraichir);

        rafraichir.setOnClickListener(v -> {
            createGraphics();
        });

        //List<String> namesList;
        Map<String, Boolean> namesList = new ArrayMap<>();
        if(LoginActivity.online){
            namesList = LoginActivity.mongo.getAllUserName();
        }
        else {
            namesList.put("offline", false);
        }
        int tailleListe = namesList.size();
        Set<String> listKeys = namesList.keySet();
        Iterator<String> keys = listKeys.iterator();
        ArrayList<String> namesConnected = new ArrayList<>();
        ArrayList<String> namesNotConnected = new ArrayList<>();
        for(int i=0; i< tailleListe;i++){
            String key = keys.next();
            if (!key.equals(userView.getDisplayName())){
                if (namesList.get(key)){
                    namesConnected.add(key);
                }
                else{
                    namesNotConnected.add(key);
                }
            }
        }

        for (String name : namesConnected) {

            TextView nameUser = new TextView(this);
            nameUser.setLayoutParams(new LinearLayout.LayoutParams(200 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
            nameUser.setText(name);
            nameUser.setTextSize(25);
            nameUser.setTextColor(getColor(R.color.black));
            nameUser.setTypeface(Typeface.DEFAULT_BOLD);
            nameUser.setGravity(Gravity.CENTER);


            View green_point = new View(this);
            green_point.setLayoutParams(new LinearLayout.LayoutParams(30 * (int) factor, 30 * (int) factor));
            green_point.setBackground(getDrawable(R.drawable.background_green_point));

            setUpAmiLayout((int) factor, green_point, nameUser);

        }
        for (String name : namesNotConnected) {

            TextView nameUser = new TextView(this);
            nameUser.setLayoutParams(new LinearLayout.LayoutParams(200 * (int) factor, ViewGroup.LayoutParams.MATCH_PARENT));
            nameUser.setText(name);
            nameUser.setTextSize(25);
            nameUser.setTextColor(getColor(R.color.black));
            nameUser.setTypeface(Typeface.DEFAULT_BOLD);
            nameUser.setGravity(Gravity.CENTER);

            View red_point = new View(this);
            red_point.setLayoutParams(new LinearLayout.LayoutParams(30 * (int) factor, 30 * (int) factor));
            red_point.setBackground(getDrawable(R.drawable.background_red_point));

            setUpAmiLayout((int) factor, red_point, nameUser);
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void setUpAmiLayout(int factor, View point, TextView nameUser) {
        LinearLayout ami = new LinearLayout(this);
        ami.setBackground(getDrawable(R.drawable.background_grey_stroke));
        ami.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,80* factor));
        ami.setOrientation(LinearLayout.HORIZONTAL);
        ami.setGravity(Gravity.CENTER);
        ami.setWeightSum(1);
        ami.addView(nameUser);
        ami.addView(point);
        layoutRightSideData.addView(ami);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return null;
    }
}
