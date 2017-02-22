package com.fatigue.driver.app;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import com.neurosky.connection.DataType.MindDataType;
import com.neurosky.connection.EEGPower;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class LinkDetectedHandler extends Handler {
    int count;
    int innerLoops=0;

    //double rawReal;
    //double rawImaginary;

    Complex[] rawComplexArray = new Complex[GlobalSettings.samplingSizeInterval]; //GlobalSettings.samplingSizeInterval set to 512 (at least for now)
    //double[] rawRealArray= new double[GlobalSettings.samplingSizeInterval];
    double[] rawNormalizedArray = new double[GlobalSettings.samplingSizeInterval];
    double[] magnitudeArray = new double[GlobalSettings.samplingSizeInterval];
    double[][] magnitude2DArray = new double[(int)GlobalSettings.COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS][GlobalSettings.samplingSizeInterval];
    double[] magnitudeAveragedArray = new double[GlobalSettings.samplingSizeInterval];

    boolean isRecordingRawData;
    boolean isRecordingRawNormData;
    boolean isCalcFFT;
    boolean isCalcMagnitude;
    boolean isRecordingFFT;
    boolean isRecordingMagnitudeArray;
    boolean isRecordingMagnitudeAveragedArray;
    boolean isRecordingRawComplexArray;

    BufferedOutputStream rawBufferedOutputStream;
    BufferedOutputStream magnitudeBufferedOutputStream;
    BufferedOutputStream rawNormBufferedOutputStream;
    BufferedOutputStream magnitudeIntervalCollectionAveragedBOS;
    BufferedOutputStream rawComplexBufferedOutputStream;
    BufferedOutputStream fftComplexArrayResultsBufferedOutputStream;
    double[] rawDataDoublesArray;
    String stringBuilder ="";
    Context context;
    String rawRealStringBuilder ="";
    private File appDir;
    private File usersDir;
    private File currentUserDir;
    private File currentUserTrainingDir;
    private File currentUserTestingDir;
    private File currentUserEvaluationDir;
    private File currentUserRawDir;
    private File rawLogDir;
    private File rawLogFile;
    private File magLogFile;
    private File magAvgLogFile;
    private File rawNormLogFile;
    private File rawComplexLogFile;
    private File fftComplexLogFile;


    public LinkDetectedHandler(Context context){
        this.context = context;
        count = 0;
        isRecordingRawData = true;
        isRecordingRawNormData = true;
        isCalcFFT = true;
        isRecordingFFT = true;
        isRecordingMagnitudeArray = true;
        isRecordingMagnitudeAveragedArray = true;
        isCalcMagnitude = true;
        isRecordingRawComplexArray=true;

        initLogCreate();
        //isTrialRunning = false;
        rawDataDoublesArray = new double[GlobalSettings.samplingSizeInterval];

    }

    @Override
    public void handleMessage(Message msg) {
        // jsnieves:COMMENT:Handles various MindDataTypes
        switch (msg.what) {

            case MindDataType.CODE_MEDITATION:
                break;
            case MindDataType.CODE_ATTENTION:
                break;

            case MindDataType.CODE_POOR_SIGNAL:
                int poorSignal = msg.arg1;
                //if (isDebugVerbose || isDebug) Log.d(TAG, "poorSignal:" + poorSignal);
                //tv_ps.setText("" + msg.arg1);
                break;

            case MindDataType.CODE_FILTER_TYPE: //jsnieves:COMMENT:Enum FilterType : FILTER_50HZ(4), FILTER_60HZ(5)
                //Log.d(TAG, "CODE_FILTER_TYPE: " + msg.arg1);
                break;

            //case MSG_UPDATE_BAD_PACKET:
            //    break;

            case MindDataType.CODE_RAW:
                rawDataDoublesArray[count] = (double) msg.arg1;
                count++;

                if(count>=512){
                    count=0;
                    processRawPacket(rawDataDoublesArray);
                    //send to logger(rawDataDoublesArray);
                }
                break;

            case MindDataType.CODE_EEGPOWER:
                EEGPower power = (EEGPower) msg.obj;
                //if (power.isValidate()) {
                    //TODO perhaps store power.delta to a value (as it is in constant flux)
                    //TODO: set lowest to "gone" in layout, due to information being mostly irrelevant and almost always ending up at ZERO
                    //TODO: check to make sure textView contains a number and not a String

                //}
                break;

            default:
                //jsnieves:COMMENT:Handle and Log any/all other messages
                //Log.d(TAG, "UNKNOWN DataType Result: " + msg.arg1);
                break;
        }
        super.handleMessage(msg);
    }

    public void processRawPacket(final double[] rawPacketArray) {

        Complex[] fftComplexArrayResults = null;
        innerLoops++;

        if (isRecordingRawData) {
            logPacket(rawPacketArray, rawBufferedOutputStream);

        } if (isRecordingRawNormData) {
            rawNormalizedArray = Util.normalizeRawArray(rawPacketArray);
            logPacket(rawNormalizedArray, rawNormBufferedOutputStream);

        } if (isCalcFFT) {
            rawComplexArray = makeComplexArray(rawNormalizedArray);

            if (isRecordingRawComplexArray) {
                logPacket(rawComplexArray, rawComplexBufferedOutputStream);
            }

            fftComplexArrayResults = FFT.fft(rawComplexArray);

            if (isRecordingFFT) {
                logPacket(fftComplexArrayResults, fftComplexArrayResultsBufferedOutputStream);
            }

        } if (isCalcMagnitude && fftComplexArrayResults != null) {
            if (innerLoops <= GlobalSettings.COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS) { //TODO:modularize for alert/fatigue
                magnitudeArray = Magnitude.mag(fftComplexArrayResults); //1 instance
                magnitude2DArray[innerLoops - 1] = magnitudeArray;
                if (isRecordingMagnitudeArray) {
                    logPacket(magnitudeArray, magnitudeBufferedOutputStream);
                }

            } else {
                magnitudeAveragedArray = Util.calcDoubleArrayMean(magnitude2DArray);
                innerLoops = 0;
                if (isRecordingMagnitudeAveragedArray) {
                    logPacket(magnitudeAveragedArray, magnitudeIntervalCollectionAveragedBOS);
                }
            }
        }

        //TODO:write band grouped data here
//                        //returns averaged BP for each band
//                        //bandPowerAveragedArray = groupAvgeragedMagnitudeArrayByBandPower(magnitudeAveragedArray);
//
        //{delta(1-4Hz), theta, alpha, low.beta, mid.beta, high.beta, low.gamma (32-50Hz)}
//                        //writeArrayToOutputStreamLog(bandPowerAveragedArray, magnitudeIntervalCollectionAveragedBOS);


    }





    public void logPacket(final double[] doubleArray, final BufferedOutputStream bos){

        new Thread(new Runnable() {
            String stringBuilder = "";
            public void run() {
                stringBuilder +="# [" + Util.currentDateAsString() + "] (" + Util.currentMilliTimestampLogAsString() + ") #\r\n"; // \n
                try {
                    for(double d : doubleArray){
                        stringBuilder+= d + "\r\n"; //\r\n
                    }

                    bos.write(stringBuilder.getBytes());
                    bos.flush();
                    //jsnieves:COMMENT:close onDestroy?

                } catch (Exception ex) {
                    //IOException
                    ex.printStackTrace();
                }
            }
        }).start();


    }

    public void logPacket(final Object array, final BufferedOutputStream bos){
        //jsnieves:COMMENT:array is either double[] or Complex[]

        new Thread(new Runnable() {

            double[] doubleArray;

            public void run() {

                //System.out.println("!!!@!!!");
                //String stringBuilder = "";

                stringBuilder +="# [" + Util.currentDateAsString() + "] (" + Util.currentMilliTimestampLogAsString() + ") #\r\n"; // \n
                try {
                    if(array instanceof Complex[]){
                        Complex[] complexArray = (Complex[]) array;
                        for(Complex c : complexArray) {
                            stringBuilder += c.re() + "\r\n";
                        }
                    } else if (array instanceof double[]){
                        doubleArray = (double[]) array;
                        for(double d : doubleArray) {
                            stringBuilder += d + "\r\n";
                        }
                    } else {
                        System.out.println("ERROR");

                    }

                    bos.write(stringBuilder.getBytes());
                    bos.flush();
                    //jsnieves:COMMENT:close onDestroy?

                } catch (Exception ex) {
                    //IOException
                    ex.printStackTrace();
                }
            }
        }).start();
    }


    private void initLogCreate() {

        final String time = Util.currentMinuteTimestampAsString();

        String tempUserName = "User1"; //TODO: create/call method getCurrentUserName() or similar here
        String currentUserName = tempUserName; //TODO: properly assign

        String rawFileName = time + "_" + GlobalSettings.rawLogFileName;
        String rawNormFileName = time + "_" + GlobalSettings.rawNormLogFileName;
        String magFileName = time + "_" + GlobalSettings.magLogFileName;
        String magAvgLogFileName = time + "_" + GlobalSettings.magAvgLogFileName;
        String fftComplexLogFileName= time + "_" + GlobalSettings.fftComplexLogFileName;
        String rawComplexLogFileName= time + "_" + GlobalSettings.rawComplexLogFileName;


        //File var19 = new File(Environment.getExternalStorageDirectory() + "/neurosky/Console_log/");

        if (Util.isExternalStorageWritable()) {
            appDir = Environment.getExternalStorageDirectory().getAbsoluteFile(); //TODO rename app rootDir
            appDir = new File(appDir + File.separator + com.fatigue.driver.app.GlobalSettings.getAppRootFolderName()); // + File.separator +folderDate

            usersDir = new File(appDir + File.separator + "Users");
            currentUserDir = new File(usersDir + File.separator + tempUserName); // + File.separator +folderDate

            currentUserTrainingDir = new File(currentUserDir + File.separator + "Training");
            currentUserTestingDir = new File(currentUserDir + File.separator + "Testing");
            currentUserEvaluationDir = new File(currentUserDir + File.separator + "Evaluation");
            currentUserRawDir = new File(currentUserDir + File.separator + GlobalSettings.rawFolderName);

            //logDir = Environment.getExternalStorageDirectory().getAbsoluteFile(); //TODO rename app rootDir

            rawLogFile = new File( currentUserRawDir.getPath(), rawFileName);
            magLogFile = new File( currentUserRawDir.getPath(), magFileName);
            rawNormLogFile = new File( currentUserRawDir.getPath(), rawNormFileName);
            magAvgLogFile = new File( currentUserRawDir.getPath(), magAvgLogFileName);

            rawComplexLogFile = new File( currentUserRawDir.getPath(), rawComplexLogFileName);
            fftComplexLogFile = new File( currentUserRawDir.getPath(), fftComplexLogFileName);


            if (!appDir.exists()) {
                usersDir.mkdirs();
            }

            if (!currentUserDir.exists()) {
                currentUserDir.mkdir();
                currentUserTrainingDir.mkdir();
                currentUserTestingDir.mkdir();
                currentUserEvaluationDir.mkdir();
                currentUserRawDir.mkdir();
            }
        }
        else {
            showToast("ERROR: external storage is NOT writable", Toast.LENGTH_SHORT);
        }


        try{
            if(!rawLogFile.exists()){
                rawLogFile.createNewFile();
                System.out.println("rawLogFile Created");
            }
        } catch( IOException ex ){
            //jsnieves:TODO:Handle this

        }

        try {
            rawBufferedOutputStream = new BufferedOutputStream( new FileOutputStream(rawLogFile), 102400 );
            magnitudeBufferedOutputStream= new BufferedOutputStream( new FileOutputStream(magLogFile), 102400 );
            rawNormBufferedOutputStream = new BufferedOutputStream( new FileOutputStream(rawNormLogFile), 102400 );

            magnitudeIntervalCollectionAveragedBOS= new BufferedOutputStream( new FileOutputStream(magAvgLogFile), 102400 );
            //rawNormBufferedOutputStream= new BufferedOutputStream( new FileOutputStream(rawNormLogFile), 102400 );

            rawComplexBufferedOutputStream = new BufferedOutputStream( new FileOutputStream(rawComplexLogFile), 102400 );

            fftComplexArrayResultsBufferedOutputStream = new BufferedOutputStream( new FileOutputStream(fftComplexLogFile), 102400 );


        } catch( FileNotFoundException ex ){
            //jsnieves:TODO:Handle this
            System.out.println("rawBufferedOutputStream FAILED");
        }

    }

    public Complex[] makeComplexArray(final double[] rawNormArray){
        Complex[] complexArray=new Complex[rawNormArray.length];

        double imaginary = 0.0;

        for(int i=0 ; i< rawNormArray.length; i++){
            complexArray[i]=new Complex(rawNormArray[i], imaginary);
        }

        return complexArray;
    }

    public void showToast(final String msg, final int timeStyle) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {Toast.makeText(context, msg, timeStyle).show();
            }
        });
    }
}