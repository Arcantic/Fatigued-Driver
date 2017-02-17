package com.fatigue.driver.app;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

public class MindwaveHelperFragment extends android.support.v4.app.Fragment {

    private static final String TAG = MindwaveHelperFragment.class.getSimpleName();
    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;
    public BluetoothAdapter mBluetoothAdapter;
    //private Complex[] fftComplexArrayResults;
    public TgStreamReader tgStreamReader;
    private TgStreamHandler callback;
    private Handler LinkDetectedHandler;
    private Complex[] rawComplexArray;
    private int badPacketCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initConnection();
        System.out.println("##############");
        System.out.println("initConnection");
        System.out.println("##############");
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


            LinkDetectedHandler = this.initLinkDetectedHandler();
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


    private TgStreamHandler initCallback() {
        return new TgStreamHandler() {

            @Override
            public void onStatesChanged(int connectionStates) {
                // TODO Auto-generated method stub
                Log.d(TAG, "connectionStates change to: " + connectionStates);
                switch (connectionStates) {

                    case ConnectionStates.STATE_INIT:
                        if (GlobalSettings.verboseDebug) showToast("STATE_INIT", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_CONNECTING:
                        if (GlobalSettings.verboseDebug) showToast("STATE_CONNECTING", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_CONNECTED:
                        tgStreamReader.start();
                        showToast("Connected", Toast.LENGTH_SHORT);
                        break;

                    case ConnectionStates.STATE_WORKING:
                        //(9) demo of recording raw data , stop() will call stopRecordRawData,
                        //or you can add a button to control it.
                        //You can change the save path by calling setRecordStreamFilePath(String filePath) before startRecordRawData
                        tgStreamReader.startRecordRawData();
                        if (GlobalSettings.verboseDebug) showToast("STATE_WORKING", Toast.LENGTH_SHORT);
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

                Message msg = LinkDetectedHandler.obtainMessage();
                msg.what = MSG_UPDATE_STATE;
                msg.arg1 = connectionStates;
                LinkDetectedHandler.sendMessage(msg);
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
                Message msg = LinkDetectedHandler.obtainMessage();
                msg.what = MSG_UPDATE_BAD_PACKET;
                msg.arg1 = badPacketCount;
                LinkDetectedHandler.sendMessage(msg);
            }

            @Override
            public void onDataReceived(int datatype, int data, Object obj) {
                // You can handle the received data here
                // You can feed the raw data to algo sdk here if necessary.

                Message msg = LinkDetectedHandler.obtainMessage();
                msg.what = datatype;
                msg.arg1 = data;
                msg.obj = obj;
                LinkDetectedHandler.sendMessage(msg);

                //Log.i(TAG,"onDataReceived");
            }
        };
    }

    private Handler initLinkDetectedHandler() {
        return new Handler() {
            int count = 0;
            double rawReal;
            double rawImaginary;
            Complex[] rawComplexArray = new Complex[512];
            double[] rawRealArray= new double[rawComplexArray.length];
            double[] rawRealNormalizedArray = new double[rawComplexArray.length];
            double magnitudeArray[] = new double[rawComplexArray.length];


            @Override
            public void handleMessage(Message msg) {
                // jsnieves:COMMENT:Handles various MindDataTypes
                switch (msg.what) {

                    case MindDataType.CODE_MEDITATION:
                        Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
                        //tv_meditation.setText("" + msg.arg1);
                        break;

                    case MindDataType.CODE_ATTENTION:
                        Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                        //tv_attention.setText("" + msg.arg1);
                        break;

                    case 3: //jsnieves:COMMENT:0x03 HEART_RATE (0-255) 1Hz on EGO.
                        Log.d(TAG, "CODE_HEARTRATE " + msg.arg1);
                        //tv_heartrate.setText("" + msg.arg1);
                        break;
                /* jsnieves:COMMENT:unfortunately, this is not a native feature of our headset model.
                   "http://developer.neurosky.com/docs/doku.php?id=thinkgear_communications_protocol"
                   TODO:revisit and consider manual implementations */

                    case MindDataType.CODE_POOR_SIGNAL:
                        int poorSignal = msg.arg1;
                        //if (isDebugVerbose || isDebug) Log.d(TAG, "poorSignal:" + poorSignal);
                        //tv_ps.setText("" + msg.arg1);
                        break;

                    case MindDataType.CODE_FILTER_TYPE: //jsnieves:COMMENT:Enum FilterType : FILTER_50HZ(4), FILTER_60HZ(5)
                        Log.d(TAG, "CODE_FILTER_TYPE: " + msg.arg1);
                        break;

                    case MSG_UPDATE_BAD_PACKET:
                        //tv_badpacket.setText("" + msg.arg1);
                        break;

                    case MindDataType.CODE_RAW:
                        //jsnieves:COMMENT:Logs
                        //Log.i(TAG, "Raw " + msg.arg1);
                        //jsnieves:COMMENT:Graph update
                        //updateWaveView(msg.arg1);

                        rawReal = (double) msg.arg1;
                        rawImaginary = 0.0;
                        System.out.println("msg.arg1: " + msg.arg1);
                        System.out.println("rawReal: " + rawReal);

                        //Complex temprawComplex = new Complex(rawReal, rawImaginary);

                        //rawComplexArray[count] = new Complex(rawReal, rawImaginary);
                        rawRealArray[count] = rawReal;

                        //System.out.println("rawComplexArray" + "["+ count + "]" + rawComplexArray[count].re() + rawComplexArray[count].im());


                        count++;

                        //==
                        if (count >= 512) {

                            count = 0;
                            //Complex[] temp = rawComplexArray;
                            //jsnieves:COMMENT:compute FFT, store results in fftComplexArrayResults
                            //fftComplexArrayResults = FFT.fft(rawComplexArray);

                            //fftComplexArrayResults = new Complex[512];


                            //TODO: SD HERE

                            double sd = Util.calcSD(rawRealArray);
                            double mean = Util.calcMean(rawRealArray);
                            for(int i =0; i< rawRealArray.length ; i++){
                                rawRealNormalizedArray[i] = (rawRealArray[i] - mean)/ sd;
                                rawComplexArray[i] = new Complex(rawRealNormalizedArray[i], rawImaginary);
                            }
                            Complex[] fftComplexArrayResults = FFT.fft(rawComplexArray);
                            //caleb magnitude testing
                            magnitudeArray = Magnitude.mag(fftComplexArrayResults);
                            //double normalization[] = Magnitude.norm(magnitudeArray);
                            //printing output of magnitude array
                            for (int i = 0; i < magnitudeArray.length; i++) {
                                System.out.println("MAGNITUDE" + "[" + i + "]: " + magnitudeArray[i]);
                            }


                            rawComplexArray = new Complex[512];
                        }
                        break;

                    case MindDataType.CODE_EEGPOWER:
                        EEGPower power = (EEGPower) msg.obj;
                        if (power.isValidate()) {


                            /*

                            //jsnieves:BEGIN:simple conversions dB
                            //(rawValue * (1.8/4096)) / 2000 ... convert TGAT-based EEG sensor values (such as TGAT, TGAM, MindWave, MindWave Mobile) to voltage values
                            //TODO:Test/complete
                            //jsnieves:END:simple conversions


                            //jsnieves:BEGIN:Log
                            //TODO:retag as RAW Power
                            //jsnieves:COMMENT:Gamma (low and middle)

                            //Log.i(TAG, "lowGammaRaw " + power.lowGamma);
                            //Log.i(TAG, "lowGammaVolts " + powerToVolts(power.lowGamma));
                            //Log.i(TAG, "middleGammaRaw " + power.middleGamma);
                            //Log.i(TAG, "middleGammaVolts " + powerToVolts(power.middleGamma));

                            //jsnieves:COMMENT:Beta (low and high)
                            //Log.i(TAG, "lowBetaRaw " + power.lowBeta);
                            //Log.i(TAG, "lowBetaVolts " + powerToVolts(power.lowBeta));
                            //Log.i(TAG, "highBetaRaw " + power.highBeta);
                            //Log.i(TAG, "highBetaVolts " + powerToVolts(power.highBeta));

                            //jsnieves:COMMENT:Alpha (low and high)
                            Log.i(TAG, "lowAlphaRaw " + power.lowAlpha);
                            Log.i(TAG, "lowAlphaVolts " + powerToVolts(power.lowAlpha));
                            Log.i(TAG, "highAlphaRaw " + power.highAlpha);
                            Log.i(TAG, "highAlphaVolts " + powerToVolts(power.highAlpha));

                            //jsnieves:COMMENT:Theta

                            //Log.i(TAG, "ThetaRaw " + power.theta);
                            //Log.i(TAG, "ThetaVolts " + powerToVolts(power.theta));

                            //jsnieves:COMMENT:Delta
                            //Log.i(TAG, "DeltaRaw " + power.delta);
                            //Log.i(TAG, "DeltaVolts " + powerToVolts(power.delta));

                            //jsnieves:END:Log

                            */

                            //TODO perhaps store power.delta to a value (as it is in constant flux)
                            //TODO: set lowest to "gone" in layout, due to information being mostly irrelevant and almost always ending up at ZERO
                            //TODO: check to make sure textView contains a number and not a String

                        }
                        break;

                    default:
                        //jsnieves:COMMENT:Handle and Log any/all other messages
                        //Log.d(TAG, "UNKNOWN DataType Result: " + msg.arg1);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }




    public void showToast(final String msg, final int timeStyle) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {Toast.makeText(getActivity(), msg, timeStyle).show();
            }
        });
    }

}




