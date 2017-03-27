package com.fatigue.driver.app;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

public class MindwaveHelperFragment_New extends android.support.v4.app.Fragment {

    private static final String TAG = MindwaveHelperFragment_New.class.getSimpleName();

    //private boolean isDebugVerbose = true;  //jsnieves:migrated to GlobalSettings
    //private boolean isDebug = true;

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;
    boolean isRecordingAlert;
    boolean isRecordingFatigue = !isRecordingAlert;
    boolean isRecordingRawData;
    boolean isRecordingRawNormData;
    boolean isCalcFFT;

    //boolean isRecording=true; //see TgStreamReader volatile
    boolean isRecordingFFT;
    boolean isRecordingMagnitudeArray;
    boolean isRecordingMagnitudeAveragedArray;
    private int badPacketCount;
    private BluetoothAdapter mBluetoothAdapter;
    private TgStreamReader tgStreamReader;
    private TgStreamHandler callback;
    private LinkDetectedHandler_New linkDetectedHandler =null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasWritePermission = ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
        if (!hasWritePermission) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 112);
        }
        Activity parentActivity = getActivity();
        System.out.println(parentActivity.toString());
        //this.initLogCreate();
        linkDetectedHandler = new LinkDetectedHandler_New(getContext());
        this.initConnection();
        //System.out.println("##initConnection##");


    }


    public LinkDetectedHandler_New getLinkDetectedHandler(){
        return this.linkDetectedHandler;
    }

    public boolean initConnection() {

        try {

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            // (1) Make sure that the device supports Bluetooth and that it is on
            if (mBluetoothAdapter == null) {
                Toast.makeText(getActivity(), "A Bluetooth adapter was NOT found on your device.", Toast.LENGTH_LONG).show();
                return false;
            } else if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getActivity(), "Please turn ON your Bluetooth and restart the program.", Toast.LENGTH_LONG).show(); //finish();
                return false;
            }

            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getActivity(), "Bluetooth enabled.", Toast.LENGTH_LONG).show(); //finish();
            }

            //linkDetectedHandler = this.initLinkDetectedHandler();
            callback = this.initCallback();

            // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
            // (2) Demo of setGetDataTimeOutTime, the default time is 5s, please call it before connect() of connectAndStart()
            // (3) Demo of startLog, you will get more sdk log by logcat if you call this function

            tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);
            //tgStreamReader.setGetDataTimeOutTime(6);
            //tgStreamReader.startLog();

            badPacketCount = 0;

            // (5) demo of isBTConnected
            if (tgStreamReader != null && tgStreamReader.isBTConnected()) {

                // Prepare for connecting
                tgStreamReader.stop();
                tgStreamReader.close();
            }

            // (4) Demo of  using connect() and start() to replace connectAndStart(),
            // please call start() when the state is changed to STATE_CONNECTED
            tgStreamReader.connect();
            // tgStreamReader.connectAndStart();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return false;
        }

        return true;

    }

    private TgStreamHandler initCallback() { //TODO:migrate to Modified?
        return new TgStreamHandler() {

            @Override
            public void onStatesChanged(int connectionStates) {
                // TODO Auto-generated method stub
                Log.d(TAG, "connectionStates change to: " + connectionStates);
                switch (connectionStates) {

                    case ConnectionStates.STATE_INIT:
                        if (GlobalSettings.isDebugVerbose) showToast("STATE_INIT", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_CONNECTING:
                        if (GlobalSettings.isDebugVerbose) showToast("STATE_CONNECTING", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_CONNECTED:
                        tgStreamReader.start(); //remove
                        linkDetectedHandler.isConnected = true;
                        //final Activity activity = getActivity();
                        //activity.findViewById(R.id.btn_start).setEnabled(true);
                        showToast("Connected", Toast.LENGTH_SHORT);

                        break;

                    case ConnectionStates.STATE_WORKING:
                        //(9) demo of recording raw data , stop() will call stopRecordRawData,
                        //or you can add a button to control it.
                        //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData

                        //jsnieves:COMMENT:needed anymore??


                        //3.13.17
                        tgStreamReader.startRecordRawData();


                        //??

                        if (GlobalSettings.isDebugVerbose) showToast("STATE_WORKING", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_STOPPED:
                        // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                        // tgStreamReader.connectAndstart(), because we have to prepare for that.
                        //if (isDebugVerbose) showToast("STATE_STOPPED", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_DISCONNECTED:
                        showToast("STATE_DISCONNECTED", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_COMPLETE:
                        //if (isDebugVerbose) showToast("STATE_COMPLETE", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_RECORDING_START:
                        //if (isDebugVerbose || isDebug) showToast("STATE_RECORDING_START", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_RECORDING_END:
                        //if (isDebugVerbose || isDebug) showToast("STATE_RECORDING_END", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                        //(9) demo of recording raw data, exception handling


                        tgStreamReader.stopRecordRawData();


                        showToast("Get data time out!", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_FAILED:
                        // It always happens when open the BluetoothSocket error or timeout
                        // Maybe the device is not working normal.
                        // Maybe you have to try again

                        showToast("STATE_FAILED", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_ERROR:
                        showToast("STATE_ERROR", Toast.LENGTH_SHORT);
                        break;
                }

                Message msg = linkDetectedHandler.obtainMessage();
                msg.what = MSG_UPDATE_STATE;
                msg.arg1 = connectionStates;
                linkDetectedHandler.sendMessage(msg);
            }

            @Override
            public void onRecordFail(int flag) {
                // You can handle the record error message here
                Log.e(TAG, "onRecordFail: " + flag);
            }

            @Override
            public void onChecksumFail(byte[] payload, int length, int checksum) {
                // You can handle the bad packets here.
                badPacketCount++;
                Message msg = linkDetectedHandler.obtainMessage();
                msg.what = MSG_UPDATE_BAD_PACKET;
                msg.arg1 = badPacketCount;
                linkDetectedHandler.sendMessage(msg);
            }

            @Override
            public void onDataReceived(int datatype, int data, Object obj) {
                // You can handle the received data here
                // You can feed the raw data to algo sdk here if necessary.

                Message msg = linkDetectedHandler.obtainMessage();
                msg.what = datatype;
                msg.arg1 = data;
                msg.obj = obj;
                linkDetectedHandler.sendMessage(msg);

                //Log.i(TAG,"onDataReceived");
            }
        };
    }

    private LinkDetectedHandler initLinkDetectedHandler() {
        return new LinkDetectedHandler(getContext());
    }

    @Override
    public void onStop() {
        tgStreamReader.stop();
        tgStreamReader.close();
        super.onStop();

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 112:
                if (grantResults.length <= 0 || grantResults[0] != 0) {
                    Toast.makeText(getActivity().getParent(), "Write access denied. App functionality may be limited. Please consider granting this permission.", Toast.LENGTH_SHORT).show();
                }
            default:
        }
    }

    //jsnieves:migrate to Util
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state) || "mounted_ro".equals(state);
    }


    public Complex[] makeComplexArray(final double[] rawNormArray){
        Complex[] complexArray=new Complex[rawNormArray.length];

        double imaginary = 0.0;

        for(int i=0 ; i< rawNormArray.length; i++){
            complexArray[i]=new Complex(rawNormArray[i], imaginary);
        }

        return complexArray;
    }

    public void startRecordAll(){
        //isRecording = true;
        setRecordingRawData(true);
        //set
        //set
        //set
    }

    public void setRecordingRawData(boolean b){
        this.isRecordingRawData = b;
    }

    public void stopRecordAll(){
        isRecordingRawData = false;
        isRecordingRawNormData = false;
        isRecordingFFT = false;
        isRecordingMagnitudeArray = false;
        isRecordingMagnitudeAveragedArray = false;
        //  isRecording = false;
    }

//!! fixed potential NullPointer if user pressed back too quick after launching this Fragment
    public void showToast(final String msg, final int timeStyle) {
        try {
            final Activity activity = getActivity();

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, msg, timeStyle).show();
                }
            });
        } catch (Exception e) {
        }


        //above code replaces below
        /*
        try{getActivity().runOnUiThread(new Runnable() {
            public void run() {Toast.makeText(getActivity(), msg, timeStyle).show();
            }
        });} catch (Exception e){}*/


    }
}


