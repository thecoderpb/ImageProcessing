package com.pratik.imageprocessing;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import static android.content.ContentValues.TAG;
import static com.pratik.imageprocessing.MainActivity.RECEIVE_MESSAGE;
import static com.pratik.imageprocessing.MainActivity.h;

public class MyBluetoothService extends Thread {


    void display(){

    }

    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private byte[] mmBuffer;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            //OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            /*
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }*/

            mmInStream = tmpIn;

        }

        public void run() {
            mmBuffer = new byte[256];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    h.obtainMessage(RECEIVE_MESSAGE, numBytes, -1, mmBuffer).sendToTarget();
                    Log.i("asdf", "printing");

                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                } catch (NullPointerException e) {
                    Log.i("asdf", "Null pointer exception");
                    break;
                }
            }
        }
    }

}
