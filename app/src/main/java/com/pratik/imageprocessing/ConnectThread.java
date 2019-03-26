package com.pratik.imageprocessing;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class ConnectThread extends  Thread {


    private final BluetoothSocket mmSocket;
    static int flag =0;


    ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;


        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MainActivity.MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }


    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        MainActivity.mBluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
            flag =1;
            MyBluetoothService.ConnectedThread btService = new MyBluetoothService.ConnectedThread(mmSocket);
            btService.start();

        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }

        }

    }
    public void cancel() {
        try {
            mmSocket.close();
            flag =0;
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
