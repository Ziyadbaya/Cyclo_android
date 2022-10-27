package com.dev.cyclo;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This object is the interface between a (simulated) bluetooth connection and all activities
 */
public class CycleData implements Serializable {

    public enum arduinoFlag {real, deprecated}

    /** use to store received data */
    double speed; //in km/h
    double cadence; //in rotation/min
    double average; //in km/h
    double arduinoTime; //in milliseconds;
    arduinoFlag flag;

    public CycleData(double speed, double cadence, double average, double arduinoTime, arduinoFlag flag) {
        this.speed = speed;
        this.cadence = cadence;
        this.average = average;
        this.arduinoTime = arduinoTime;
        this.flag = flag;
    }

    public CycleData() {
        this(0, 0, 0, 0, arduinoFlag.deprecated);
    }

    /** method use to turn a string send via a real bluetooth connection into usable data
     * @param str String create on arduino and send via bluetooth
     * @return CycleData object corresponding or null
     */
    public static CycleData getCycleDataFromArduinoString(String str){
        String[] data;
        data = str.split(";");
        try {
            String strFlag = data[4];

            arduinoFlag flag;
            if (strFlag.equals("real")) {
                flag = arduinoFlag.real;
            } else {
                flag = arduinoFlag.deprecated;
            }
            return new CycleData(Float.parseFloat(data[0]), Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3]), flag);
        }
        catch (Exception e){
            return null;
        }
    }

    /** Should be use only by the bluetooth simulator to set data
     * @param speed in km/h
     * @param cadence in rotation/min
     */
    public void setCycleData(double speed, double cadence, double average, double arduinoTime, arduinoFlag flag) {
        this.speed = speed;
        this.cadence = cadence;
        this.average = average;
        this.arduinoTime = arduinoTime;
        this.flag = flag;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCadence() {
        return cadence;
    }

    public double getAverage() {
        return average;
    }

    public double getArduinoTime() {
        return arduinoTime;
    }

    public arduinoFlag getFlag() {
        return flag;
    }


    @NotNull
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        return "CycleData{" +
                "speed=" + df.format(speed) +
                ", cadence=" + df.format(cadence) +
                ", average=" + df.format(average) +
                ", arduinoTime=" + arduinoTime +
                ", flag=" + flag +
                '}';
    }
}
