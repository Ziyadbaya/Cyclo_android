package com.dev.cyclo.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dev.cyclo.CycleData;
import com.dev.cyclo.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 *  create/handle the communication socket and handle the bluetooth receiving thread
 * @see BtDataThread
 */

@SuppressLint("Registered")
public class BtLink extends Activity implements IBluetooth{

    //UUID : Universal ID for our kind of bluetooth dongle (DSD TECH HC-05)
    private static final UUID MY_UUID= UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private final static int REQUEST_ENABLE_BT = 1;
    //identity of the bluetooth dongle
    private static String MODULE_MAC;
    final Logger logger= new Logger(this.getClass());
    private final BluetoothAdapter mBtAdapter;                 //bluetooth adapter (material interface)
    CycleData cycleData;                   //cycle data updated
    private BluetoothSocket mBtSocket;             //API for bluetooth communication


    public BtLink()  {
        logger.log(Logger.Severity.Info, "Creating listeners", "[BLUETOOTH]");
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter(); //set default smartphone bluetooth interface
    }


    void arduinoLog(String msg, Logger logger){
        if(!msg.startsWith("LOG")){
            throw new Error("ask to log a non-loggable message : " + msg);
        }
        try {
            String[] str = msg.split(";");
            switch (str[1]) {
                case "Verbose":
                    logger.log(Logger.Severity.Verbose, str[3], str[2]);
                    break;
                case "Debug":
                    logger.log(Logger.Severity.Debug, str[3], str[2]);
                    break;
                case "Info":
                    logger.log(Logger.Severity.Info, str[3], str[2]);
                    break;
                case "Warn":
                    logger.log(Logger.Severity.Warn, str[3], str[2]);
                    break;
                case "Error":
                    logger.log(Logger.Severity.Error, str[3], str[2]);
                    break;
                default:
                    Log.wtf(str[3], str[2]);
                    break;
            }
        }
        catch (Exception e){
            logger.log(Logger.Severity.Error, "cannot log : " + msg + Arrays.toString(e.getStackTrace()) + e.getMessage());
        }
    }

    /** this method checks if bluetooth is enabled (so connexion interface/adapter too) */
    public boolean isBtAdapterEnabled(){
        return this.mBtAdapter.isEnabled();
    }

    /**
     * this method initiates the communication process and launches the bluetooth thread
     * */
    public void initiateBluetoothProcess() { //connexion process
        if (mBtAdapter.isEnabled()) {         //attempt to connect to bluetooth module
            BluetoothSocket tmp;
            //bluetooth On, need to read paired devices
            Set<BluetoothDevice> pairedDevices;
            pairedDevices = mBtAdapter.getBondedDevices();
            //look for one called DSD TECH HC-05 and set the MAC address
            for (BluetoothDevice bt : pairedDevices) {
                if (bt.getName().equals("DSD TECH HC-05") || bt.getName().equals("JEFF")) {
                    MODULE_MAC = bt.getAddress(); //set MAC address (compulsory)
                }
            }
            //set the current bluetooth device (sender of data)
            //future bluetooth dongle connected
            BluetoothDevice mBtDevice = mBtAdapter.getRemoteDevice(MODULE_MAC);
            try {
                //create socket (communication protocols according to material : UUID)
                tmp = mBtDevice.createRfcommSocketToServiceRecord(MY_UUID);
                mBtSocket = tmp;
                mBtSocket.connect();    //connexion to the socket
                logger.log(Logger.Severity.Info, "Connected to: " + mBtDevice.getName(), "[BLUETOOTH]");
            } catch (IOException e) {
                try {
                    mBtSocket.close();  //if error in connexion to the socket, closes it
                } catch (IOException c) {
                    return;
                }
            }
            //creation of the handler
            if (mBtSocket.isConnected()) {
                logger.log(Logger.Severity.Info, "Creating handler", "[BLUETOOTH]");
                //creation of the bluetooth handler which receives messages with the same loop than the main
                //this receives messages from thread (loop);
                Handler mHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == BtDataThread.RESPONSE_MESSAGE) { //could be deleted if only simplex com
                            if (((String) msg.obj).startsWith("LOG")) {
                                arduinoLog((String) msg.obj, logger);
                            } else {
                                cycleData = CycleData.getCycleDataFromArduinoString((String) msg.obj);
                                if (cycleData != null) {
                                    updateDataCycleInRide(cycleData);
                                }
                            }
                        }
                    }
                };
                logger.log(Logger.Severity.Info, "Creating and running Thread", "[BLUETOOTH]");
                // custom thread created with previous bluetooth socket and handler
                BtDataThread btDataThread = new BtDataThread(mBtSocket, mHandler);
                btDataThread.start();   //run this thread
            }
            else {
                logger.log(Logger.Severity.Error, "Socket not connected : no Device", "[BLUETOOTH]");
            }
        }
    }

    /**
     * This method checks if the bt is enabled and create an intent if not or launch the connexion process if yes
     */
    public void startBluetooth(){

        logger.log(Logger.Severity.Info, "Creating listeners", "[BLUETOOTH]"); //View/ToolWindows/Logcat + regex

        if (!(this.isBtAdapterEnabled())) {
            //if bluetooth adapter (interface) is not enabled then create Intent (Pop Up) for user to turn it on
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            logger.log(Logger.Severity.Info, "Bluetooth Off : Intent", "[BLUETOOTH]");
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        } else { //Bluetooth already On
            this.initiateBluetoothProcess(); //run the connexion process
        }
    }


    /**
     * This method do nothing, this application never kill a real bluetooth thread
     */
    @Override
    public void stopBluetooth() {}

    /** If Intent (like a Pop Up) is displayed (due to bluetooth off) and then accepted, run the connexion process */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @NonNull Intent data) {  //receive the confirmation of the user
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            this.initiateBluetoothProcess(); //connexion process
            logger.log(Logger.Severity.Info, "Bluetooth Intent : ON", "[BLUETOOTH]");
        }
    }
}
