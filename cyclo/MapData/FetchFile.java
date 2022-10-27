package com.dev.cyclo.MapData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.dev.cyclo.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** AsyncTask  which receives the name of the json file to load from the assets directory and to parse
 * Receive also a boolean if it is a route.json so you need to call PointsParser on it
 * or a routesOverview.json and you need to call RoutesOverviewParser on it
 * @see PointsParser
 * @see RoutesOverviewParser
 * */

public class FetchFile extends AsyncTask<String, String, String> {
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private final boolean isRoutesOverview;
    @SuppressLint("StaticFieldLeak")
    private Logger logger;

    public FetchFile(Context mContext, boolean isRoutesOverview) {
        this.mContext = mContext;
        //is it for routeOverview json
        this.isRoutesOverview = isRoutesOverview;
    }

    // launched with .execute(String filename) after the creation
    @NonNull
    @Override
    protected String doInBackground(@NonNull String... strings) {
        // For storing data from assets directory
        String data = "";
        try {
            // Fetching the data from file
            data = loadJSONFromAsset(strings[0]);
        } catch (Exception e) {
            logger.log(Logger.Severity.Error, e.toString());
        }
        return data;
    }

    // return of the doInBackground call used here
    @Override
    protected void onPostExecute(@NonNull String s) {
        super.onPostExecute(s);
        //if it's a route to load, call a PointsParser
        if (!isRoutesOverview) {
            PointsParser parserTask = new PointsParser(mContext);
            // Invokes the thread for parsing the JSON data
            parserTask.execute(s);
        }
        //if it's a routesOverview, call the RoutesOverviewParser
        else {
            RoutesOverviewParser rParserTask = new RoutesOverviewParser(mContext);
            rParserTask.execute(s);
        }
    }

    // load the json file from the assets directory
    private String loadJSONFromAsset(@NonNull String filename) {
        String json;
        try {
            InputStream is = mContext.getApplicationContext().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}

