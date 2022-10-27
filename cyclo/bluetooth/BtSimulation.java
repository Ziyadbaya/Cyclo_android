package com.dev.cyclo.bluetooth;

import android.os.Handler;
import android.os.Looper;
import com.dev.cyclo.CycleData;
import com.dev.cyclo.Logger;
import com.dev.cyclo.Main;

import java.util.Arrays;

/** This class handle a thread as a normal bluetooth thread do
 */
public class BtSimulation implements IBluetooth {

    /**
     * This object is the one to simulate in the bluetooth connection
     * @see CycleData
     * */
    CycleData cycleData;

    private static final double wheelDiameter = Main.config.getConfigData().getWheel_diameter();

    /** This is use to wake up and put to sleep the thread */
    private final Handler handlerUpdate;

    private final Thread thread;

    /**
     * Create a new thread with his handler to simulate a bluetooth connection
     * Launch this thread
     */
    public BtSimulation() {

        handlerUpdate = new Handler(Looper.getMainLooper());
        cycleData = new CycleData();
        Logger logger = new Logger(this.getClass());

        thread = new Thread() {
            /**
             * Choose the new coherent cadence
             * then update cadence and speed
             * then put asleep the tread for the good period of time
             */
            public void run() {
                double cadence = getDouble(cycleData.getCadence() * Main.config.getConfigData().getCadence_min_volatility(),
                        cycleData.getCadence() * Main.config.getConfigData().getCadence_max_volatility());
                double cadence_min = Main.config.getConfigData().getCadence_min();
                if (cadence < cadence_min) {
                    cadence = cadence_min;
                }
                double cadence_max = Main.config.getConfigData().getCadence_max();
                if (cadence > cadence_max) {
                    cadence = cadence_max;
                }
                double nextTop = 60 / cadence; //in seconds
                //Log.i("[BLUETOOTH SIM]", "next top in :" + nextTop + "seconds");

                double speed = (wheelDiameter / nextTop) * 3.6; //in km/h
                cycleData.setCycleData(speed, cadence, (speed + cycleData.getSpeed())/2, cycleData.getArduinoTime() + (nextTop * 1000), CycleData.arduinoFlag.real );
                updateDataCycleInRide(cycleData);


                try {
                    handlerUpdate.postDelayed(this, (long) nextTop * 1000); //next call thread (this) + delay
                } catch (Exception e) {
                    // in case of a thread failure
                    logger.log(Logger.Severity.Error, "Tread.run()" + Arrays.toString(e.getStackTrace()), "[BLUETOOTH SIM]");
                }
            }
        };

    }

    /** Generated a random double between a and b
     * @param a should be the lowest number
     * @param b should be the highest number
     * @return a random double between a and b
     */
    private double getDouble(double a, double b){
        return a + (b -a) * Math.random();
    }


    /**
     * Start the thread that generate and update data
     */
    @Override
    public void startBluetooth(){
        thread.start(); //lunch the tread with the first run() call
    }

    /**
     * Stop the thread that generate and update data
     */
    public void stopBluetooth(){
        handlerUpdate.getLooper().quitSafely();
    }

}
