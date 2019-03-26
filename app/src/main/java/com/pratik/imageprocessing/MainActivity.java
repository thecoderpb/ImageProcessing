package com.pratik.imageprocessing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_COARSE_LOCATION = 2 ;
    private static final int REQUEST_ENABLE_BT = 1 ;
    public static final int RECEIVE_MESSAGE = 1;
    private  static  int  CAM_REQUEST =3;

    public static Handler h;
    public ConnectThread connectThread;;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConstraintLayout constraintLayout;

    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device1;

    /**  Compute task sends request to server. Request are sent using compute.execute() in onCreate method**/

    public class ComputeTask extends AsyncTask<String ,Void,String>{

        @Override
        protected String doInBackground(String... urls) {

            String result ="";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data!=-1){
                    char cur = (char) data;
                    result+=cur;
                    data=reader.read();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         constraintLayout = findViewById(R.id.constrainLayout);

       //Request for bluetooth permission. Location permission is necessary to scan bluetooth devices in area

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
        }
        if((ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.BLUETOOTH ))!=PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.BLUETOOTH},REQUEST_ENABLE_BT);

        }else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_ENABLE_BT);
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent,CAM_REQUEST);


        ComputeTask computeTask = new ComputeTask();
        computeTask.execute("https://ssh.cloud.google.com/projects/image-processing-1552311157526/zones/us-east1-b/instances/instance-1?authuser=0&hl=en_US&projectNumber=621519050825");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == CAM_REQUEST && resultCode == RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(image);
        }
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK ) {
            receiver();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth unsupported or unavailable", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

            }

        }
    }

    public void receiver(){

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        //checkReqBTs();

        if (mBluetoothAdapter != null &&  mBluetoothAdapter.startDiscovery()) {

            Snackbar.make(constraintLayout,"Discovery in progress...",Snackbar.LENGTH_LONG)
                    .setAction("Action",null).show();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            registerReceiver(mReceiver, filter);

        }
    }

    public void ifConn(){
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if(ConnectThread.flag == 1)
                    Snackbar.make(constraintLayout,"Connected",Snackbar.LENGTH_SHORT).show();
            }
        }, 3000);
    }

    private void checkReqBT(BluetoothDevice device){

            device1 = device;
            Snackbar.make(constraintLayout, "Connecting to "+device.getName(), Snackbar.LENGTH_LONG).show();
            connectThread = new ConnectThread(device);
            connectThread.start();
            ifConn();
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                if(device == null){
                    receiver();
                }else
                    checkReqBT(device);
            }
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                connectThread.cancel();
                Toast.makeText(MainActivity.this, "Device disconnected.Attempting to reconnect", Toast.LENGTH_SHORT).show();

                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConnectThread connectThread = new ConnectThread(device1);
                        connectThread.start(); }
                }, 12000);

                if(connectThread.flag == 1)
                    Snackbar.make(constraintLayout,"Connected",Snackbar.LENGTH_SHORT).show();


            }
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                ifConn();
            }
            mBluetoothAdapter.cancelDiscovery();



        }
    };
}
