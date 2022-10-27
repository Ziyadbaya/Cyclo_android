package com.dev.cyclo.config;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;
import com.google.android.gms.maps.model.LatLng;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Set a ConfigData instance which will be put as a global variable in the main to be available everywhere.
 * @see ConfigData
 */
public class Config {


    private final Logger logger;
    private final Context mContext;
    private final ConfigData configData = new ConfigData();
    private JSONObject jsonConfigData;


    /**
     * Constructor of Config element, do all the processing :
     * 1 - Check if a local config directory exists and if a config.txt file is into
     *      Path : sdcard/Android/data/com.dev.cyclo/files/Documents/config/config.txt
     * 2 - If no, create the directory and copy the default config file saved in assets files
     *      (config.json only readable) to a new config.txt (writeable directly on the tablet or by the app)
     *      The config.txt file is the same after several reload of the app.
     * 3 - Then, load the config.txt file (default or custom) and create the ConfigData instance
     *
     * @param context the context of the Main to find and read correctly the assets directory
     */
    public Config(@NonNull Context context) throws JSONException {

        String configAssetFilename = "config.json";
        String configLocalFilename = "config.txt";
        this.logger = new Logger(this.getClass());
        this.mContext = context;

        File configLocalFile;
        // is config.txt in config directory in local storage or no
        if (!this.isConfigInLocalDir(configLocalFilename)) {
            //no config.txt file, create/copy a default one from the config.json file in assets directory (read only)
            //return the File just created
            configLocalFile = this.copyConfigFile(configAssetFilename, configLocalFilename);
        } else {
            //File created with the path of the existing file
            configLocalFile = new File(Main.path + "/config",configLocalFilename);
        }
        //creation of JSONObject from the config TXT file on the device
        loadConfigJSONFromFile(configLocalFile);
        //set attributes of ConfigData from the JSONObject received
        setConfigData(this.jsonConfigData);
    }

    @NonNull
    public ConfigData getConfigData() {
        return configData;
    }

    /*
     * set the attribute configData by parsing JSONObject config data
     * @see ConfigData
     * @param jsonConfigData the JSONObject of the config data used to fill in the ConfigData attribute
     * @throws JSONException JSON exception
     */
    private void setConfigData(JSONObject jsonConfigData) throws JSONException {
        //look at the config.json in the assets directory to understand the architecture of the JSONObject used
        JSONObject user_config = jsonConfigData.getJSONObject("user_config");
        JSONObject dev_config = jsonConfigData.getJSONObject("dev_config");
        this.configData.setSpeedIndicatorShown(user_config.getBoolean("show_speed_indicator"));
        this.configData.setCadenceIndicatorShown(user_config.getBoolean("show_cadence_indicator"));
        this.configData.setProgressBarShown(user_config.getBoolean("show_progress_bar"));
        this.configData.setMongodb_request_delay(dev_config.getLong("mongodb_request_delay"));
        this.configData.setBtSimulatorActivated(dev_config.getBoolean("bt_simulator"));
        JSONObject simulator_parameters = dev_config.getJSONObject("simulator_parameters");
        this.configData.setWheel_diameter(simulator_parameters.getDouble("wheel_diameter"));
        this.configData.setCadence_min(simulator_parameters.getDouble("cadence_min"));
        this.configData.setCadence_max(simulator_parameters.getDouble("cadence_max"));
        this.configData.setCadence_min_volatility(simulator_parameters.getDouble("cadence_min_volatility"));
        this.configData.setCadence_max_volatility(simulator_parameters.getDouble("cadence_max_volatility"));
        JSONObject map_parameters = dev_config.getJSONObject("map_parameters");
        this.configData.setDefaultMapType(map_parameters.getInt("defaultMapType"));
        this.configData.setAnimated_zoom_tilt(map_parameters.getInt("animated_zoom_tilt"));
        this.configData.setWidth_polyline(map_parameters.getInt("width_polyline"));
        this.configData.setZoom_scale(map_parameters.getInt("zoom_scale"));
        this.configData.setGeneratorModeActivated(dev_config.getBoolean("generator_mode"));
        JSONObject generator_mode = dev_config.getJSONObject("generator_parameters");
        this.configData.setFileNameWithoutExtension(generator_mode.getString("fileNameWithoutExtension"));
        double origLat = generator_mode.getDouble("origLat");
        double origLng = generator_mode.getDouble("origLng");
        LatLng origPos = new LatLng(origLat, origLng);
        this.configData.setOrigLatLng(origPos);
        double destLat = generator_mode.getDouble("destLat");
        double destLng = generator_mode.getDouble("destLng");
        LatLng destPos = new LatLng(destLat, destLng);
        this.configData.setDestLatLng(destPos);
    }

    /*
     * check if there is a file on the local storage of the device
     * at sdcard/Android/data/com.dev.cyclo/files/Documents/config
     * @param filename the name of the file to check
     * @return boolean of "if the file is on the local storage"
     */
    private boolean isConfigInLocalDir(String filename) {
        String path = Main.path;
        File config = new File(path, "config");
        if (!config.exists()) { //if config directory doesn't exist
            return false;
        } else {
            path += "/config";
            File file = new File(path, filename);
            return file.exists();
        }
    }

    /*
     * copy default JSON config file from assets directory and paste it as TXT config file in local storage
     * JSON config file from assets dir is only readable whereas local TXT file is writeable directly on the device
     * @param configAssetFilename the name of the default config file (JSON) in the assets dir to copy
     * @param configLocalFilename the name of the local config file (TXT) in which the fct pastes the default config file
     * @return File Object of the local config file just filled in
     */
    private File copyConfigFile(String configAssetFilename, String configLocalFilename) {
        String data = "";
        try {
            //load JSONObject from the file called configAssetFilename in the assets directory
            data = loadJSONFromAsset(configAssetFilename);
        } catch (Exception e) {
            logger.log(Logger.Severity.Error, e.toString());
        }

        //create file which will be called configLocalFilename in local storage
        File configFile = this.createConfigFile(configLocalFilename);
        File absoluteLocalFile = null;
        try {
            //add data to new file in local storage
            absoluteLocalFile = configFile.getAbsoluteFile();   //FileWriter needs AbsoluteFile
            FileWriter fileWriter = new FileWriter(absoluteLocalFile, true);
            writeJSON(fileWriter, data);    //write in the File
        } catch (IOException e) {
            e.printStackTrace();
        }
        return absoluteLocalFile;
    }

    /*
     * load the JSON file from assets dir
     * @param filename the JSON file name to load
     * @return JSON (like String)
     */
    private String loadJSONFromAsset(@NonNull String filename) {
        return getString(filename, mContext);
    }

    @Nullable
    public static String getString(@NonNull String filename, Context mContext) {
        String json;
        try {
            //need the right context set in the constructor and in the Main
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

    /*
     * create the config file (TXT) and the config directory if needed on the local storage
     * @param configLocalFileName the name of the local config file which will be created
     * @return File Object of the file juste created
     */
    private File createConfigFile(String configLocalFileName) {
        String path = Main.path;
        //Check and create the config directory
        File configDir = new File(path, "config");
        if (!configDir.exists()) {              //if the config directory doesn't exist
            if (!configDir.mkdir()) {               // creating a new config directory
                logger.log(Logger.Severity.Error, "Cannot create config directory", "[Config]");
            }
        }
        path += "/config";
        //Check and create the config file named configLocalFileName in this directory
        File configFile = new File(path, configLocalFileName);
        try {
            if (!configFile.createNewFile()) {
                logger.log(Logger.Severity.Error, "Try to create a config TXT file that already exists", "[Config]");
            }
        } catch (IOException e) {
            logger.log(Logger.Severity.Error, "Error in createConfigFile in local storage : " + e,"[Config]");
        }
        return configFile;
    }

    /*
     * Write the JSON on the File configured in the FileWriter and add the String out
     * @param fw the FileWriter in which the File to write in is
     * @param out the String data to write in the File
     */
    private void writeJSON(FileWriter fw, String out) {
        try {
            fw.append(out);
            fw.flush();
        } catch (IOException e) {
            logger.log(Logger.Severity.Error, "writeJSON error : " + e,"[Config]");
        }
    }

    /*
     * load the local config file on the local storage
     * (after detecting it or after creating it in the storage)
     * @param absoluteLocalFile the config File to read
     * @throws JSONException JSON exception
     */
    private void loadConfigJSONFromFile(File absoluteLocalFile) throws JSONException {
        JSONObject configJSON = null;
        try {
            //FileReader needs an absoluteFile

            FileReader fileReader = new FileReader(absoluteLocalFile);
            //get the json from fileReader
            configJSON = readTXT(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //update the jsonConfigData attribute
        this.jsonConfigData = configJSON;
    }

    /*
     * read the TXT config file and return a JSONObject of data in the TXT file
     * @param fr FileReader made of File linked to the TXT config file in local storage
     * @return JSONObject config data
     * @throws JSONException JSON exception
     */
    private JSONObject readTXT(FileReader fr) throws JSONException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(fr);
            while ((line = bufferedReader.readLine()) != null) stringBuilder.append(line);
        } catch (FileNotFoundException e) {
            logger.log(Logger.Severity.Error, "Error file not found in readTXT : " + e);
        } catch (IOException e) {
            logger.log(Logger.Severity.Error, "Error readTXT : " + e);
        }
        return new JSONObject(stringBuilder.toString());
    }

    /**
     * change the speedIndicator boolean in the java ConfigData element AND in the local config file
     * so your parameters are saved for next launch of the app
     * @param bool the boolean you want to show or not the speedIndicator
     * @throws JSONException JSON exception
     */
    public void modifySpeedIndicatorVisibility(boolean bool) throws JSONException {

        this.configData.setSpeedIndicatorShown(bool);
        JSONObject temporaryJSONconfigUser =(JSONObject) this.jsonConfigData.get("user_config");
        temporaryJSONconfigUser.put("show_speed_indicator",bool);
        this.jsonConfigData.put("user_config", temporaryJSONconfigUser);
        modifyConfigFile("config.txt");
    }

    /**
     * change the cadenceIndicator boolean in the java ConfigData element AND in the local config file
     * so your parameters are saved for next launch of the app
     * @param bool the boolean you want to show or not the cadenceIndicator
     * @throws JSONException JSON exception
     */
    public void modifyCadenceIndicatorVisibility(boolean bool) throws JSONException {

        this.configData.setCadenceIndicatorShown(bool);
        JSONObject temporaryJSONconfigUser =(JSONObject) this.jsonConfigData.get("user_config");
        temporaryJSONconfigUser.put("show_cadence_indicator",bool);
        this.jsonConfigData.put("user_config", temporaryJSONconfigUser);
        modifyConfigFile("config.txt");
    }

    /**
     * change the progressBar boolean in the java ConfigData element AND in the local config file
     * so your parameters are saved for next launch of the app
     * @param bool the boolean you want to show or not the progressBar
     * @throws JSONException JSON exception
     */
    public void modifyProgressBarVisibility(boolean bool) throws JSONException {
        this.configData.setProgressBarShown(bool);
        JSONObject temporaryJSONconfigUser =(JSONObject) this.jsonConfigData.get("user_config");
        temporaryJSONconfigUser.put("show_progress_bar",bool);
        this.jsonConfigData.put("user_config", temporaryJSONconfigUser);
        modifyConfigFile("config.txt");
    }

    /*
     * modify the local config file to save your parameters
     * @param configLocalFilename the name of the local config file
     */
    private void modifyConfigFile(String configLocalFilename) {
        JSONObject data = this.jsonConfigData;
        File configDirectory = new File(Main.path, "config");
        //delete the current local config file
        deleteDirectory(configDirectory.getAbsoluteFile());
        //create a new one with the same name and same position (the upper directory called config too)
        File configFile = createConfigFile(configLocalFilename);
        try {
            File absoluteLocalFile = configFile.getAbsoluteFile();
            //write in the config file the java JSON config data just changed before
            FileWriter fileWriter = new FileWriter(absoluteLocalFile, true);
            writeJSON(fileWriter, data.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Delete the common file/dir and file in it
     * @param directory to delete
     */
    private void deleteDirectory(File directory){
        File[] files = directory.listFiles();
        if(files != null){
            for (File file : files) {
                if(file.isDirectory()){
                    deleteDirectory(file);
                }
                else{
                    if (file.delete()) {
                        Log.v("Config", "Delete :" + file);
                    }
                    else{
                        Log.wtf("Config", "impossible to delete :" + file);
                    }
                }
            }
        }
        if(directory.delete()){
            Log.v("[Logger]", "Delete : " + directory);
        }
        else {
            Log.wtf("[Logger]", "Cannot delete : " + directory);
        }
    }
}

