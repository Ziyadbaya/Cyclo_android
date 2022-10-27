package com.dev.cyclo.MapData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AsyncTask which received the url to send to google map api and then do it
 * takes the return JSON file and send it to a PointsParser and save it in local storage file named in parameters by the way
 * @see PointsParser
 */

public class FetchURL extends AsyncTask<String, String, String> {
    @SuppressLint("StaticFieldLeak")
    private final Context mContext;
    private static File file;
    private final Logger logger;
    private FileWriter fw;

    public FetchURL(@NonNull Context mContext, @NotNull String filename) {
        this.mContext = mContext;
        this.logger = new Logger(this.getClass());
        if(file == null){
            newJSONFile(filename);      //create the new JSON file in which JSON map response will be saved
        }
        try {
            fw = new FileWriter(file.getAbsoluteFile(), true);      // create the writer to do it
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    // launched by .execute(String url) after the creation
    @NonNull
    @Override
    protected String doInBackground(@NonNull String... strings) {   //done automatically nearly after the creator has finished
        // For storing data from web service
        String data;
        data = "";
        try {
            // Fetching the data from web service
            data = downloadUrl(strings[0]);
            logger.log(Logger.Severity.Info, "Background task data ","[FetchURL]");
        } catch (Exception e) {
            logger.log(Logger.Severity.Error, "Background Task" + e,"[FetchURL]");
        }
        return data;
    }


    @Override
    protected void onPostExecute(@NonNull String response) {
        super.onPostExecute(response);         //when doInBackGround request receives something, it's received here (String s)
        PointsParser parserTask = new PointsParser(mContext);
        // Invokes the thread for parsing the JSON data and create the routeData object sent to Ride class then
        parserTask.execute(response);
    }

    /* http communication to download the json file from web services */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {    //reading line until the last one which is empty
                sb.append(line);        //add to the StringBuilder the content of the line
            }
            data = sb.toString();
            writeJSON(data);    //add theses elements in the json file
            br.close();
        } catch (Exception e) {
            logger.log(Logger.Severity.Error, "Exception downloading URL: " + e,"[FetchURL]");
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /* Create a new jsonFile with the filename in parameter
     * if needed, create the directory customRoutes to put route json file into
     * it's generally located in approximately DeviceFileExplorer/sdcard/Android/data/com.dev.cyclo/files/Documents/customRoutes
     */
    private void newJSONFile(String filename) {
        String path = Main.path;
        File customRoutes = new File(path, "customRoutes");
        if (!customRoutes.exists()) { //if the json file doesn't exist
            if (!customRoutes.mkdir()) { // creating a new log file
                logger.log(Logger.Severity.Error, "Cannot create customRoutes directory","[FetchURL]");
            }
        }
        path += "/customRoutes";

        file = new File(path, filename + ".json");
        try {
            if (!file.createNewFile()) {
                logger.log(Logger.Severity.Error, "Try to create a route json file that already exists","[FetchURL]");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* write method to add data in our created json file (with the fileWriter file
     * @see FileWriter
     */
    private void writeJSON( String out) {
        try {
            fw.append(out);
            fw.flush();
        } catch (IOException e) {
            logger.log(Logger.Severity.Error, "Try to create a json file that already exists","[FetchURL]");
        }
    }
}

