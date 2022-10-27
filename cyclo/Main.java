package com.dev.cyclo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;

import com.dev.cyclo.config.Config;
import com.dev.cyclo.ui.login.LoginActivity;

import org.json.JSONException;

public class Main extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Config config = null;
    //public static ConfigData configData = null;
    public static String path;
    @SuppressLint("StaticFieldLeak")
    public static Context mainContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // path to the public file directory of this application
        path = this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
       //Log.e("[documents path string]", path);

        // create a new logger for this run of the application
        Logger logger = new Logger(this.getClass());
        logger.log(Logger.Severity.Debug, "Launching a new main");

        mainContext = this;
        // set configData which will be used everywhere in the application
        try {
            config = new Config(mainContext);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.d("CONFIG", config.getConfigData().getFileNameWithoutExtension());


        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}