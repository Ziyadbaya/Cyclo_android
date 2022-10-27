package com.dev.cyclo.bluetooth;

import com.dev.cyclo.CycleData;
import com.dev.cyclo.ride.Ride;

public interface IBluetooth {


    /**
     * Method to call to start receiving data
     */
    void startBluetooth();

    /**
     * Method to call to stop receiving data
     */
    void stopBluetooth();


    /** Method to call to update in Ride class
     * @param cycleData the fresh cycleData
     * @see CycleData
     */
    default void updateDataCycleInRide(CycleData cycleData){
        Ride.updateCycleData(cycleData);
    }

}
