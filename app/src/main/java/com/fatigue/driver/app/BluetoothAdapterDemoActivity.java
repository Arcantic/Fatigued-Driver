package com.fatigue.driver.app; /*
 |  CREATED on 10/10/16.
*/


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.AlgoSdk.NskAlgoSdk;
import com.neurosky.connection.ConnectionStates;
import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;
import com.neurosky.connection.TgStreamHandler;
import com.neurosky.connection.TgStreamReader;

/**
 * This activity demonstrates how to use the constructor:
 * public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
 * and related functions:
 * (1) Make sure that the device supports Bluetooth and Bluetooth is on
 * (2) setGetDataTimeOutTime
 * (3) startLog
 * (4) Using connect() and start() to replace connectAndStart()
 * (5) isBTConnected
 * (6) Use close() to release resource
 * (7) Demo of TgStreamHandler
 * (8) Demo of MindDataType
 * (9) Demo of recording raw data
 */

//jsnieves:ENTIRE class copied from package com.neurosky.mindwavemobiledemo; (TGStreamDemo_MindWaveMobile)
// /home/systems/nieves/AndroidStudioProjects/Samples/TGStreamDemo_MindWaveMobile


public class BluetoothAdapterDemoActivity extends Activity {

    private static final String TAG = BluetoothAdapterDemoActivity.class.getSimpleName();
    private static final int MSG_UPDATE_BAD_PACKET = 1001;
    private static final int MSG_UPDATE_STATE = 1002;
    //jsnieves:COMMENT:added rawdata arrays
    public Complex[] rawComplexArray = new Complex[512];
    public Complex[] fftComplexArrayResults = new Complex[512];
    DrawWaveView waveView = null;
    private TgStreamReader tgStreamReader;
    private BluetoothAdapter mBluetoothAdapter;
    private TextView tv_ps = null;
    private TextView tv_attention = null;
    private TextView tv_meditation = null;
    //jsnieves:END
    private TextView tv_delta = null;
    //jsnieves:BEGIN
    private TextView tv_delta_lowest = null;
    private TextView tv_delta_highest = null;
    private TextView tv_theta = null;
    private TextView tv_lowalpha = null;
    private TextView tv_highalpha = null;
    private TextView tv_lowbeta = null;
    private TextView tv_highbeta = null;
    private TextView tv_lowgamma = null;
    private TextView tv_middlegamma = null;
    private TextView tv_badpacket = null;
    private Button btn_start = null;
    //jsnieves:END:add heartrate
    private Button btn_stop = null;
    //jsnieves:END:from Algo_SDK_Sample
    //jsnieves:BEGIN:add heartrate
    private TextView tv_heartrate = null;
    private LinearLayout wave_layout;
    private int badPacketCount = 0;
    //jsnieves:BEGIN:from Algo_SDK_Sample
    private NskAlgoSdk nskAlgoSdk;
    private boolean isPressing = false;
    private Handler LinkDetectedHandler = new Handler() {
        //jsnieves:COMMENT:init variables for FFT
        int count = 0;
        double rawReal, rawImaginary;

        @Override
        public void handleMessage(Message msg) {
            // (8) demo of MindDataType
            switch (msg.what) {
                case MindDataType.CODE_RAW:
                    //jsnieves:COMMENT:Logs
                    //Log.i(TAG, "Raw " + msg.arg1);

                    rawReal = (double) msg.arg1;
                    rawImaginary = 0.0;
                    //jsnieves:COMMENT:Graph update
                    updateWaveView(msg.arg1);

                    rawComplexArray[count] = new Complex(rawReal, rawImaginary);
                    count++;

                    if (count >= 512) {
                        //jsnieves:COMMENT:Log
                        //Log.i(TAG, "512 Reached");

                        count = 0;
                        Complex[] temp = rawComplexArray;
                        //jsnieves:COMMENT:compute FFT, store results in fftComplexArrayResults
                        fftComplexArrayResults = FFT.fft(rawComplexArray);

                        //jsnieves:COMMENT:Log results
                        for (int i = 0; i < fftComplexArrayResults.length; i++) {
                            Log.i(TAG, " rawComplexArray[" + i + "]" + " = " + temp[i]);
                            Log.i(TAG, " fftComplexArrayResults[" + i + "]" + " = " + fftComplexArrayResults[i]);
                        }
                    }
                    break;
                case MindDataType.CODE_MEDITATION:
                    Log.d(TAG, "HeadDataType.CODE_MEDITATION " + msg.arg1);
                    tv_meditation.setText("" + msg.arg1);
                    break;
                case MindDataType.CODE_ATTENTION:
                    Log.d(TAG, "CODE_ATTENTION " + msg.arg1);
                    tv_attention.setText("" + msg.arg1);
                    break;

                //jsnieves:BEGIN:heart
                case 3: //0x03 HEART_RATE (0-255) Once/s on EGO. "http://developer.neurosky.com/docs/doku.php?id=thinkgear_communications_protocol"
                    Log.d(TAG, "CODE_HEARTRATE " + msg.arg1); //jsnieves:COMMENT:may not be a feature of our headset... (does not appear to function properly)
                    tv_heartrate.setText("" + msg.arg1);
                    break;
                //jsnieves:END:heart

                case MindDataType.CODE_EEGPOWER:
                    EEGPower power = (EEGPower) msg.obj;
                    if (power.isValidate()) {
                        tv_delta.setText("" + power.delta);
                        tv_theta.setText("" + power.theta);
                        tv_lowalpha.setText("" + power.lowAlpha);
                        tv_highalpha.setText("" + power.highAlpha);
                        tv_lowbeta.setText("" + power.lowBeta);
                        tv_highbeta.setText("" + power.highBeta);
                        tv_lowgamma.setText("" + power.lowGamma);
                        tv_middlegamma.setText("" + power.middleGamma);

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
                        Log.i(TAG, "ThetaRaw " + power.theta);
                        Log.i(TAG, "ThetaVolts " + powerToVolts(power.theta));

                        //jsnieves:COMMENT:Delta
                        //Log.i(TAG, "DeltaRaw " + power.delta);
                        //Log.i(TAG, "DeltaVolts " + powerToVolts(power.delta));

                        //jsnieves:END:Log

                        //jsnieve:BEGIN
                        //updates lowest and highest values
                        //TODO perhaps store power.delta to a value (as it is in constant flux)
                        //TODO: set lowest to "gone" in layout, due to information being mostly irrelevant and almost always ending up at ZERO
                        //TODO: check to make sure textView contains a number and not a String
                        if (power.delta < Integer.valueOf(tv_delta_lowest.getText().toString())) {
                            tv_delta_lowest.setText("" + power.delta);
                        } else if (power.delta > Integer.valueOf(tv_delta_highest.getText().toString())) {
                            tv_delta_highest.setText("" + power.delta);
                        }
                        //jsnieves:END
                    }
                    break;
                case MindDataType.CODE_POOR_SIGNAL:
                    int poorSignal = msg.arg1;
                    Log.d(TAG, "poorSignal:" + poorSignal);
                    tv_ps.setText("" + msg.arg1);
                    break;

                //jsnieves:BEGIN:add MindDataType CODE_FILTER_TYPE:from:Stream SDK PDF
                //Enum FilterType   FILTER_50HZ(4), FILTER_60HZ(5)
                case MindDataType.CODE_FILTER_TYPE:
                    Log.d(TAG, "CODE_FILTER_TYPE: " + msg.arg1);
                    break;
                //jsnieves:END:add MindDataType CODE_FILTER_TYPE


                case MSG_UPDATE_BAD_PACKET:
                    tv_badpacket.setText("" + msg.arg1);

                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    // (7) demo of TgStreamHandler
    private TgStreamHandler callback = new TgStreamHandler() {

        //jsnieves:COMMENT:BT states

        @Override
        public void onStatesChanged(int connectionStates) {
            // TODO Auto-generated method stub
            Log.d(TAG, "connectionStates change to: " + connectionStates);
            switch (connectionStates) {
                    //jsnieves:BEGIN:added additional state from ConnectionStates.class, sorted all states by their numeric equivalents
                case ConnectionStates.STATE_INIT:
                    showToast("STATE_INIT", Toast.LENGTH_SHORT);
                    break;
                    //jsnieves:END
                case ConnectionStates.STATE_CONNECTING:
                    //jsnieves:BEGIN
                    showToast("STATE_CONNECTING", Toast.LENGTH_SHORT);
                    //jsnieves:END
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
                    //jsnieves:BEGIN
                    showToast("STATE_WORKING", Toast.LENGTH_SHORT);
                    //jsnieves:END
                    break;
                case ConnectionStates.STATE_STOPPED:
                    // We have to call tgStreamReader.stop() and tgStreamReader.close() much more than
                    // tgStreamReader.connectAndstart(), because we have to prepare for that.
                    //jsnieves:BEGIN
                    showToast("STATE_STOPPED", Toast.LENGTH_SHORT);
                    //jsnieves:END
                    break;
                case ConnectionStates.STATE_DISCONNECTED:
                    //jsnieves:BEGIN
                    showToast("STATE_DISCONNECTED", Toast.LENGTH_SHORT);
                    //jsnieves:END
                    break;
                //jsnieves:BEGIN:added additional state from ConnectionStates.class
                case ConnectionStates.STATE_COMPLETE:
                    showToast("STATE_COMPLETE", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_RECORDING_START:
                    showToast("STATE_RECORDING_START", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_RECORDING_END:
                    showToast("STATE_RECORDING_END", Toast.LENGTH_SHORT);
                    break;
                //jsnieves:END:added additional state from ConnectionStates.class
                case ConnectionStates.STATE_GET_DATA_TIME_OUT:
                    //(9) demo of recording raw data, exception handling
                    tgStreamReader.stopRecordRawData();

                    showToast("Get data time out!", Toast.LENGTH_SHORT);
                    break;
                case ConnectionStates.STATE_FAILED:
                    // It always happens when open the BluetoothSocket error or timeout
                    // Maybe the device is not working normal.
                    // Maybe you have to try again

                    //jsnieves:BEGIN
                    showToast("STATE_FAILED", Toast.LENGTH_SHORT);
                    //jsnieves:END
                    break;
                case ConnectionStates.STATE_ERROR:
                    //jsnieves:BEGIN
                    showToast("STATE_ERROR", Toast.LENGTH_SHORT);
                    //jsnieves:END
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.test_first_view);

        initView();
        //jsnieves:BEGIN
        initNskAlgoSdk();
        //jsnieves:END
        setUpDrawWaveView();

        try {
            // (1) Make sure that the device supports Bluetooth and Bluetooth is on
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Toast.makeText(
                        this,
                        "Please enable your Bluetooth and re-run this program !",
                        Toast.LENGTH_LONG).show();
                finish();
//				return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "error:" + e.getMessage());
            return;
        }

        // Example of constructor public TgStreamReader(BluetoothAdapter ba, TgStreamHandler tgStreamHandler)
        tgStreamReader = new TgStreamReader(mBluetoothAdapter, callback);
        // (2) Demo of setGetDataTimeOutTime, the default time is 5s, please call it before connect() of connectAndStart()
        tgStreamReader.setGetDataTimeOutTime(6);
        // (3) Demo of startLog, you will get more sdk log by logcat if you call this function
        tgStreamReader.startLog();

        //jsnieves:TODO:set further tgStreamReader configs here

    }


    private void initView() {
        tv_ps = (TextView) findViewById(R.id.tv_ps);
        tv_attention = (TextView) findViewById(R.id.tv_attention);
        tv_meditation = (TextView) findViewById(R.id.tv_meditation);
        tv_delta = (TextView) findViewById(R.id.tv_delta);

        //jsnieves:BEGIN
        tv_delta_lowest = (TextView) findViewById(R.id.tv_delta_lowest);
        tv_delta_highest = (TextView) findViewById(R.id.tv_delta_highest);
        tv_heartrate = (TextView) findViewById(R.id.tv_heartrate);
        //jsnieves:END

        tv_theta = (TextView) findViewById(R.id.tv_theta);
        tv_lowalpha = (TextView) findViewById(R.id.tv_lowalpha);

        tv_highalpha = (TextView) findViewById(R.id.tv_highalpha);
        tv_lowbeta = (TextView) findViewById(R.id.tv_lowbeta);
        tv_highbeta = (TextView) findViewById(R.id.tv_highbeta);

        tv_lowgamma = (TextView) findViewById(R.id.tv_lowgamma);
        tv_middlegamma = (TextView) findViewById(R.id.tv_middlegamma);
        tv_badpacket = (TextView) findViewById(R.id.tv_badpacket);

        btn_start = (Button) findViewById(R.id.btn_start);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        wave_layout = (LinearLayout) findViewById(R.id.wave_layout);


        btn_start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
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
//				tgStreamReader.connectAndStart();
            }
        });

        btn_stop.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                tgStreamReader.stop();
                tgStreamReader.close();
            }

        });
    }


    private void initNskAlgoSdk() {
        //jsnieves:BEGIN:from Algo_SDK_Sample
        nskAlgoSdk = new NskAlgoSdk(); //added jni libs (*.so files)

        //jsnieves:BEGIN:from EEG algorithm SDK PDF
        nskAlgoSdk.setOnBPAlgoIndexListener(new NskAlgoSdk.OnBPAlgoIndexListener() {
            @Override
            public void onBPAlgoIndex(float delta, float theta, float alpha, float beta, float gamma) {
                Log.i(TAG, "NskAlgoBPAlgoIndexListener: BP: D[" + delta + " dB] T[" + theta + " dB] A[" +
                        alpha + " dB] B[" + beta + " dB] G[" + gamma + "]");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        showToast("TEST123", Toast.LENGTH_SHORT);

                        // change UI elements here
                    }
                });
            }
        });

        //jsnieves:END:from EEG algorithm SDK PDF

        //jsnieves:END:from Algo_SDK_Sample
        //TODO:implement eye detection


    }

    public void stop() {
        if (tgStreamReader != null) {
            tgStreamReader.stop();
            tgStreamReader.close();
        }
    }

    @Override
    protected void onDestroy() {
        //(6) use close() to release resource
        if (tgStreamReader != null) {
            tgStreamReader.close();
            tgStreamReader = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stop();
    }

    public void setUpDrawWaveView() {
        //jsnieves:init waveview graph

        waveView = new DrawWaveView(getApplicationContext());
        wave_layout.addView(waveView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        waveView.setValue(2048, 2048, -2048);
    }

    public void updateWaveView(int data) {
        if (waveView != null) {
            waveView.updateData(data);
        }
    }

    public void showToast(final String msg, final int timeStyle) {
        BluetoothAdapterDemoActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }

        });
    }


    public double powerToVolts(int rawValue) {
        //jsnieves:BEGIN:method to calc dB from RAW
        return ((rawValue * (1.8 / 4096)) / 2000);
        //jsnieves:END:method to calc dB from RAW
    }


    public void sendCommandtoDevice(byte[] command){
        //jsnieves:COMMENT:method to send firmware byte commands to EEG
        if (tgStreamReader != null) {
            //tgStreamReader.sendCommandtoDevice(byte[] command);

        }
    }
}
