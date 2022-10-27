package com.dev.cyclo.config;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Java object composed of attributes of the config.json file in assets or config.txt file in local storage
 * Created and completed by Config element launched in the Main as a static object
 * There are only getters and setters
 * Enable global access to user_parameters (Preferences) and to dev_parameters (variables to change for developpers)
 * @see Config
 */
public class ConfigData {
    private boolean isSpeedIndicatorShown;
    private boolean isCadenceIndicatorShown;
    private boolean isProgressBarShown;
    private long mongodb_request_delay;
    private boolean isBtSimulatorActivated;
    private boolean isGeneratorModeActivated;
    private double wheel_diameter;
    private double cadence_min;
    private double cadence_max;
    private double cadence_min_volatility;
    private double cadence_max_volatility;
    private int defaultMapType;
    private int animated_zoom_tilt;
    private int width_polyline;
    private int zoom_scale;
    private String fileNameWithoutExtension;
    private LatLng origLatLng;
    private LatLng destLatLng;

    public boolean isSpeedIndicatorShown() {
        return isSpeedIndicatorShown;
    }

    public void setSpeedIndicatorShown(boolean speedIndicatorShown) {
        isSpeedIndicatorShown = speedIndicatorShown;
    }

    public boolean isCadenceIndicatorShown() {
        return isCadenceIndicatorShown;
    }

    public void setCadenceIndicatorShown(boolean cadenceIndicatorShown) {
        isCadenceIndicatorShown = cadenceIndicatorShown;
    }

    public boolean isProgressBarShown() {
        return isProgressBarShown;
    }

    public void setProgressBarShown(boolean progressBarShown) {
        isProgressBarShown = progressBarShown;
    }

    public long getMongodb_request_delay() {
        return mongodb_request_delay;
    }

    public void setMongodb_request_delay(long mongodb_request_delay) {
        this.mongodb_request_delay = mongodb_request_delay;
    }

    public boolean isBtSimulatorActivated() {
        return isBtSimulatorActivated;
    }

    public void setBtSimulatorActivated(boolean btSimulatorActivated) {
        isBtSimulatorActivated = btSimulatorActivated;
    }

    public boolean isGeneratorModeActivated() {
        return isGeneratorModeActivated;
    }

    public void setGeneratorModeActivated(boolean generatorModeActivated) {
        isGeneratorModeActivated = generatorModeActivated;
    }

    public double getWheel_diameter() {
        return wheel_diameter;
    }

    public void setWheel_diameter(double wheel_diameter) {
        this.wheel_diameter = wheel_diameter;
    }

    public double getCadence_min() {
        return cadence_min;
    }

    public void setCadence_min(double cadence_min) {
        this.cadence_min = cadence_min;
    }

    public double getCadence_max() {
        return cadence_max;
    }

    public void setCadence_max(double cadence_max) {
        this.cadence_max = cadence_max;
    }

    public double getCadence_min_volatility() {
        return cadence_min_volatility;
    }

    public void setCadence_min_volatility(double cadence_min_volatility) {
        this.cadence_min_volatility = cadence_min_volatility;
    }

    public double getCadence_max_volatility() {
        return cadence_max_volatility;
    }

    public void setCadence_max_volatility(double cadence_max_volatility) {
        this.cadence_max_volatility = cadence_max_volatility;
    }

    public int getDefaultMapType() {
        return defaultMapType;
    }

    public void setDefaultMapType(int defaultMapType) {
        this.defaultMapType = defaultMapType;
    }

    public int getAnimated_zoom_tilt() {
        return animated_zoom_tilt;
    }

    public void setAnimated_zoom_tilt(int animated_zoom_tilt) {
        this.animated_zoom_tilt = animated_zoom_tilt;
    }

    public int getWidth_polyline() {
        return width_polyline;
    }

    public void setWidth_polyline(int width_polyline) {
        this.width_polyline = width_polyline;
    }

    public int getZoom_scale() {
        return zoom_scale;
    }

    public void setZoom_scale(int zoom_scale) {
        this.zoom_scale = zoom_scale;
    }

    @NonNull
    public String getFileNameWithoutExtension() {
        return fileNameWithoutExtension;
    }

    public void setFileNameWithoutExtension(@NonNull String fileNameWithoutExtension) {
        this.fileNameWithoutExtension = fileNameWithoutExtension;
    }

    @NonNull
    public LatLng getOrigLatLng() {
        return origLatLng;
    }

    public void setOrigLatLng(@NonNull LatLng origLatLng) {
        this.origLatLng = origLatLng;
    }

    @NonNull
    public LatLng getDestLatLng() {
        return destLatLng;
    }

    public void setDestLatLng(@NonNull LatLng destLatLng) {
        this.destLatLng = destLatLng;
    }
}
