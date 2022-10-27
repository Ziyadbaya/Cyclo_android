package com.dev.cyclo.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.dev.cyclo.Logger;

import java.io.*;

/**
 * This is the Bluetooth Thread which receives data from the bluetooth socket (communication tunnel with arduino set in BtLink)
 * and send data to the bluetooth handler in parameters of the constructor
 * @see Thread
 * @see BluetoothSocket
 */


public class BtDataThread extends Thread {

    private final InputStream mInputStream;
    public static final int RESPONSE_MESSAGE = 10;
    private final Handler mBtHandler;

    public BtDataThread(@NonNull BluetoothSocket socket, @NonNull Handler bluetoothHandler) {

        Logger logger = new Logger(this.getClass());

        //temporary input stream
        InputStream tmpIn = null;

        this.mBtHandler = bluetoothHandler;

        logger.log(Logger.Severity.Info, "Creating thread", "[THREAD-CT]");
        try {          //get the input stream of the bluetooth socket
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            logger.log(Logger.Severity.Error, e.getMessage(), "[THREAD-CT]");
        }
        // input stream finally used in the Runnable loop
        mInputStream = tmpIn;
        logger.log(Logger.Severity.Info, "IO's obtained", "[THREAD-CT]");
    }

    /** the runnable function in the thread */
    public void run() {

        Logger logger = new Logger(this.getClass());

        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(mInputStream)); //receive data from socket input
        while (true) {                          //loop
            try {
                String resp = br.readLine();    //readLine of received data
                logger.log(Logger.Severity.Info, resp);
                Message msg = new Message();    //format in Message for the bluetooth handler
                msg.what = RESPONSE_MESSAGE;
                msg.obj = resp;
                mBtHandler.sendMessage(msg);    //send the message to the bluetooth handler
            } catch (IOException e) {
                break;
            }
        }
        logger.log(Logger.Severity.Info, "While loop ended", "[THREAD-CT]");
    }
}

