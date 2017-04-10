package com.fatigue.driver.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.connection.DataType.MindDataType;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import libsvm.svm;
import libsvm.svm_model;


public class LinkDetectedHandler extends Handler {
    private static final String TAG = LinkDetectedHandler.class.getSimpleName();

    Context context;
    int count;
    int numOfTrialsCompleted=0;
    int numOfPacketsToConsumePerTrial;
    int numOfProcessedPacketsConsumed;
    int totalNumOfSinglePacketInstancesObservedCounter;
    int numOfSimulatedTrialsObservedCounter; // simulated
    int numOfActualTrialsCollectedGivenCurrentClassifierCounter;
    int totalNumOfAllTrialsCounter;
    int numOfActualAlertTrialsCollected;
    int numOfActualFatigueTrialsCollected;
    double[] rawDataDoublesArray;
    double[] rawNormalizedArray = new double[GlobalSettings.samplingSizeInterval];
    Complex[] rawComplexArray = new Complex[GlobalSettings.samplingSizeInterval]; //GlobalSettings.samplingSizeInterval set to 512 (at least for now)
    Complex[] fftComplexArrayResults;
    Complex[] complexLogArray;
    double[] doubleLogArray;
    double[][] singleTrialMagnitudePackets2DArray_Alert = new double[(int) GlobalSettings.NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_ALERT][GlobalSettings.samplingSizeInterval];
    double[][] singleTrialMagnitudePackets2DArray_Fatigue = new double[(int) GlobalSettings.NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_FATIGUE][GlobalSettings.samplingSizeInterval];
    double[] magnitudeArray = new double[GlobalSettings.samplingSizeInterval];
    double[] magnitudesCollectedOverTimeForASingleTrialAveragedArray = new double[GlobalSettings.samplingSizeInterval];
    double[] singleTrialMagnitudesAveragedArray_Alert = new double[GlobalSettings.samplingSizeInterval];
    double[] singleTrialMagnitudesAveragedArray_Fatigue = new double[GlobalSettings.samplingSizeInterval];
    //double[] singleTrialMagnitudesAveraged_Agnostic = new double[GlobalSettings.samplingSizeInterval];

    double[] featureValuesOfSingleTrialArray;
    double[][] featuresPriorToNormalization2DArrayAlert = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlert][GlobalSettings.numOfFeatures];
    double[][] featuresPriorToNormalization2DArrayFatigue = new double[GlobalSettings.calibrationNumOfTrialsToPerformFatigue][GlobalSettings.numOfFeatures];
    double[][] featuresNormalized2DArrayAlert = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlert][GlobalSettings.numOfFeatures];
    double[][] featuresNormalized2DArrayFatigued = new double[GlobalSettings.calibrationNumOfTrialsToPerformFatigue][GlobalSettings.numOfFeatures];
    double[][] featuresNormalizedWithClassifierAtIndexZero2DArrayAlert = new double[featuresNormalized2DArrayAlert.length][GlobalSettings.numOfFeatures + 1];
    double[][] featuresNormalizedWithClassifierAtIndexZero2DArrayFatigued = new double[featuresNormalized2DArrayFatigued.length][GlobalSettings.numOfFeatures + 1];
    double[] evalFeaturesNormalized = new double[GlobalSettings.numOfFeatures];

    double[][] preNormFeatures2DArray_Fatigue=new double[GlobalSettings.numOfTrialsToPerformForTraining_Fatigue][GlobalSettings.numOfFeatures];
    double[][] preGroupedPreNormMags_Fatigue=new double[GlobalSettings.numOfTrialsToPerformForTraining_Fatigue][GlobalSettings.samplingSizeInterval];
    double[][] preNormFeatures2DArray_Alert=new double[GlobalSettings.numOfTrialsToPerformForTraining_Alert][GlobalSettings.numOfFeatures];
    double[][] preGroupedPreNormMags_Alert=new double[GlobalSettings.numOfTrialsToPerformForTraining_Alert][GlobalSettings.samplingSizeInterval];

    double[] minTrainingData=new double[GlobalSettings.numOfFeatures];
    double[] maxTrainingData=new double[GlobalSettings.numOfFeatures];
    //double[][]singleTrialMagnitudeCollector2DArray=new double[(int) GlobalSettings.numOfPacketsToConsumePerTrial_Alert][GlobalSettings.samplingSizeInterval];
    //double[][] preAveragedSingleTrialMagnitudeCollector2DArray=
    double[][]singleTrialMagnitudeCollector2DArray_Alert=new double[GlobalSettings.numOfPacketsToConsumePerTrial_Alert][GlobalSettings.samplingSizeInterval];
    double[][]singleTrialMagnitudeCollector2DArray_Fatigue=new double[GlobalSettings.numOfPacketsToConsumePerTrial_Fatigue][GlobalSettings.samplingSizeInterval];

    boolean isRawDataLoggingEnabled;
    boolean is_RawNorm_RawComplex_FFTComplex_MagnitudesUngrouped_DataLoggingEnabled;
    boolean isProcessedRawDataToBeTransformedAsTrainingData;
    boolean isRecordingRawNormData; //TODO cleanup unused variables
    boolean isCalcFFT;
    boolean isCalcMagnitude;
    boolean isRecordingFFT;
    boolean isRecordingMagnitudeArray;
    boolean isRecordingMagnitudeAveragedArray;
    boolean isRecordingRawComplexArray;
    boolean isCalcTrialFeatures;
    boolean isDataLoggingEnabled;
    boolean isRawDataProcessingRequested;
    boolean isAllDataLoggingRequested;
    boolean isLogAndRecordSimulatedTrainingData;
    boolean isRecordNoData;
    boolean isTrialCollectionSimulated;
    boolean isTrialEyesClosed;
    boolean isTrialEyesOpenAlert;
    boolean isProcessedRawDataToBeTransformedAsFeaturesForEval;
    boolean isProcessedRawDataToBeTransformedAsFeaturesForContinuousEval;
    boolean isEvalEyesOpenAlert;
    boolean isConnected;
    boolean isEvaluationInProgress;
    boolean isEvaluationConiinuous;
    boolean isTrainingInProgress;
    boolean isFeatureDataToBeNormalized=true;
    boolean isRawDataToBeSDNormalized=true;
    boolean isProcessedRawDataToBeTransFormedAsFeatures=true;
    boolean isEvaluationContinuous;
    boolean SVM_accuracy_TEST =false;
    svm_model loadedEvalModel=null;

    TextView logOut;
    ScrollView sv_Log;
    SVMEvaluator svmEvaluator;
    double[] evalFeatures = new double[GlobalSettings.numOfFeatures];
    double[] evalFeaturesWithClassifierAtIndexZero = new double[GlobalSettings.numOfFeatures + 1];
    File currentUsersMinMaxDataFile=null;
    String time = Util.currentMinuteTimestampAsString();
    String date = Util.currentMonthDateUnderscoreFormat();
    String featuresTrainingDataMinMaxLogFileName = time + "_" + GlobalSettings.featuresMinMaxLogFileName;
    View rootView;
    File modelLogFileEvalCopy;
    Runnable scrollLogView;
    MindwaveHelperFragment mwHelper;
    private File sdCard;
    private File appDir;
    private File usersDir;
    private File currentUserDir;
    private File currentUserDatedDir;
    private File currentUserTrainingDir;
    private File currentUserTestingDir;
    private File currentUserEvaluationDir;
    private File currentUserRawDir;
    private File currentUserRawDirTimestamped;
    private File appLogFile;
    private File rawLogFile;
    private File magLogFile;
    private File magAvgLogFile;
    private File rawNormLogFile;
    private File rawComplexLogFile;
    private File fftComplexLogFile;
    private File bandPwrFeaturesLogFile;
    private File bandPwrFeaturesAvgLogFile;
    private File svmTrainingDataLogFile;
    private File minMaxFeaturesTrainingDataLogFile;
    private BufferedOutputStream appLogBufferedOutputStream;
    private BufferedOutputStream rawBufferedOutputStream;
    private BufferedOutputStream magnitudeBufferedOutputStream;
    private BufferedOutputStream rawNormBufferedOutputStream;
    private BufferedOutputStream magnitudeIntervalCollectionAveragedBOS;
    private BufferedOutputStream rawComplexBufferedOutputStream;
    private BufferedOutputStream fftComplexArrayResultsBufferedOutputStream;
    private BufferedOutputStream bandPwrFeaturesAvgLogFileNameBufferedOutputStream;
    private BufferedOutputStream bandPwrFeaturesLogFileNameBufferedOutputStream;
    private BufferedOutputStream svmTrainingDataBufferedOutputStream;
    private BufferedOutputStream featuresTrainingDataMinMaxBufferedOutputStream;
    private StringBuilder rawStringBuilder = new StringBuilder();
    private StringBuilder magnitudeIntervalCollectionAveragedStringBuilder = new StringBuilder();
    private StringBuilder rawNormalizedStringBuilder = new StringBuilder();
    private StringBuilder rawComplexStringbuilder = new StringBuilder();
    private StringBuilder fftStringBuilder = new StringBuilder();
    private StringBuilder magnitudesStringBuilder = new StringBuilder();
    private LinearLayout wave_layout;
    private DrawWaveView waveView;

    public LinkDetectedHandler(Context context) {
        this.context = context;
        rootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);


        this.mwHelper=mwHelper;

        logOut = (TextView) rootView.findViewById(R.id.tv_log);
        sv_Log = (ScrollView) rootView.findViewById(R.id.scrollViewLog);
        wave_layout = (LinearLayout) rootView.findViewById(R.id.wave_layout);

        count = 0;
        totalNumOfSinglePacketInstancesObservedCounter = 0;
        numOfProcessedPacketsConsumed = 0;
        numOfSimulatedTrialsObservedCounter = 0;
        numOfActualTrialsCollectedGivenCurrentClassifierCounter = 0;
        totalNumOfAllTrialsCounter = 0;

        isConnected = false;
        isRawDataLoggingEnabled = true; //TODO pull these from GlobalSettings
        isRecordingRawNormData = true;
        isCalcFFT = true;
        isRecordingFFT = true;
        isRecordingMagnitudeArray = true;
        isRecordingMagnitudeAveragedArray = true;
        isCalcMagnitude = true;
        isRecordingRawComplexArray = true;
        isCalcTrialFeatures = true;
        is_RawNorm_RawComplex_FFTComplex_MagnitudesUngrouped_DataLoggingEnabled = true;
        isProcessedRawDataToBeTransformedAsTrainingData = true;

        isDataLoggingEnabled = GlobalSettings.isLogData;
        isRawDataProcessingRequested = GlobalSettings.isProcessRawData;
        isAllDataLoggingRequested = GlobalSettings.isLogAllData;
        isLogAndRecordSimulatedTrainingData = GlobalSettings.isLogAndRecordTrainingData;
        isRecordNoData = GlobalSettings.isRecordNoData;

        isTrialCollectionSimulated = true; //jsnieves:COMMENT:needs to be manually fired for training data collection to start/con't/resume

        rawDataDoublesArray = new double[GlobalSettings.samplingSizeInterval];


        scrollLogView = new Runnable () {
            @Override
            public void run() {
                sv_Log.smoothScrollTo(0, logOut.getBottom());
            }
        };


        initLogCreate();
        initLoadSvmModelFile();
        //initLoadMinMaxValues();
        initDrawWaveView();

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
                //int poorSignal = msg.arg1;
                // if (isDebugVerbose || isDebug) Log.d(TAG, "poorSignal:" + poorSignal);
                //logAppEvent("Poor Signal" + msg.arg1);
                break;
            case MindDataType.CODE_FILTER_TYPE: // jsnieves:COMMENT:Enum FilterType : FILTER_50HZ(4),
                // FILTER_60HZ(5)
                // Log.d(TAG, "CODE_FILTER_TYPE: " + msg.arg1);
                break;
            // case MSG_UPDATE_BAD_PACKET:
            // break;

            case MindDataType.CODE_RAW:
                // order matters here
                rawDataDoublesArray[count] = (double) msg.arg1;
                count++;
                if (count >= GlobalSettings.samplingSizeInterval) {
                    count = 0;
                    processRawPacket(rawDataDoublesArray);
                }
                // WAVEVIEW
                updateWaveView(msg.arg1);
                break;

            case MindDataType.CODE_EEGPOWER:
                //EEGPower power = (EEGPower) msg.obj; //TODO if using, make global declaration

                //if (power.isValidate()) {    //}
                //TODO perhaps store power.delta to a value (as it is in constant flux)
                //TODO: set lowest to "gone" in layout, due to information being mostly irrelevant and almost always ending up at ZERO
                //TODO: check to make sure textView contains a number and not a String

                break;

            default:
                //jsnieves:COMMENT:Handle and Log any/all other messages
                //Log.d(TAG, "UNKNOWN DataType Result: " + msg.arg1);
                //logAppEvent("UNKNOWN DataType Result: " + msg.arg1);
                break;
        }
        super.handleMessage(msg);
    }

    public void processRawPacket(final double[] rawPacketArray) {
        //if(isDataLoggingEnabled){ //jsnieves:COMMENT: Our most likely scenario


        if (isRawDataLoggingEnabled) { //TODO Accuracy Tweak (disable)
            logPacket(rawPacketArray, rawBufferedOutputStream, rawStringBuilder);
        }
        if (isRawDataProcessingRequested) {
            if (isRawDataToBeSDNormalized) { //TODO Accuracy Tweak (do norm)
                rawNormalizedArray = Util.normalizeRawArray(rawPacketArray); //jsnieves:COMMENT: Normalize to remove DC component? //TODO check with Caleb
            }
            rawComplexArray = Util.makeComplexArray(rawNormalizedArray); //jsnieves:COMMENT: Create Complex array
            fftComplexArrayResults = FFT.fft(rawComplexArray);  //jsnieves:COMMENT: Calc FFT ComplexArray
            magnitudeArray = Magnitude.mag(fftComplexArrayResults);  //jsnieves:COMMENT: Find Magnitude values
            if (is_RawNorm_RawComplex_FFTComplex_MagnitudesUngrouped_DataLoggingEnabled) {
                logPacket(rawNormalizedArray, rawNormBufferedOutputStream, rawNormalizedStringBuilder);
                logPacket(rawComplexArray, rawComplexBufferedOutputStream, rawComplexStringbuilder);
                logPacket(fftComplexArrayResults, fftComplexArrayResultsBufferedOutputStream, fftStringBuilder);
                logPacket(magnitudeArray, magnitudeBufferedOutputStream, magnitudesStringBuilder);
            }

            totalNumOfSinglePacketInstancesObservedCounter++;


        }

        if (isEvaluationInProgress | isProcessedRawDataToBeTransformedAsFeaturesForEval ) {/*remove*/


            if (SVM_accuracy_TEST) {


            } else {

                evalFeatures = Util.groupMagnitudesByBandwidth(magnitudeArray);
                evalFeaturesWithClassifierAtIndexZero[0] = isEvalEyesOpenAlert ? GlobalSettings.EYES_OPEN : GlobalSettings.EYES_CLOSED;


                if (minTrainingData != null && maxTrainingData != null) {
                    evalFeaturesNormalized = Util.normalizeArrayWithKnownMinMax(evalFeatures, minTrainingData, maxTrainingData);
                    System.arraycopy(evalFeaturesNormalized, 0, evalFeaturesWithClassifierAtIndexZero, 1, evalFeaturesWithClassifierAtIndexZero.length - 1);

                } else {

                    System.arraycopy(evalFeatures, 0, evalFeaturesWithClassifierAtIndexZero, 1, evalFeaturesWithClassifierAtIndexZero.length - 1);
                }

                if (loadedEvalModel != null) {
                    svmEvaluator.evaluate(evalFeaturesWithClassifierAtIndexZero, loadedEvalModel);
                }

            }


        } else if (isProcessedRawDataToBeTransformedAsTrainingData) {

            if (numOfProcessedPacketsConsumed < (isTrialEyesOpenAlert ? GlobalSettings.NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_ALERT : GlobalSettings.NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_FATIGUE)) {

                if (isTrialEyesOpenAlert) {
                    singleTrialMagnitudePackets2DArray_Alert[numOfProcessedPacketsConsumed] = magnitudeArray; //rename ! to single trial Collector or something similar
                    //System.out.println("Alert magnitudeArray test printout index 0: " + magnitudeArray[0]);
                } else {
                    singleTrialMagnitudePackets2DArray_Fatigue[numOfProcessedPacketsConsumed] = magnitudeArray; //rename ! to single trial Collector or something similar
                    //System.out.println("Fatigue magnitudeArray test printout index 0: " + magnitudeArray[0]);
                }

                numOfProcessedPacketsConsumed++;
                logAppEvent((isTrialEyesOpenAlert ? "Alert" : "Fatigue") + " Mag. calc. (" + numOfProcessedPacketsConsumed + ") from pkt (#" + totalNumOfSinglePacketInstancesObservedCounter + ")");

            } else {

                numOfProcessedPacketsConsumed = 0;

                if (isTrialEyesOpenAlert) {
                    magnitudesCollectedOverTimeForASingleTrialAveragedArray = Util.calcDoubleArrayMean(singleTrialMagnitudePackets2DArray_Alert); //TOO recheck here, should we calc features first then average?
                } else {
                    magnitudesCollectedOverTimeForASingleTrialAveragedArray = Util.calcDoubleArrayMean(singleTrialMagnitudePackets2DArray_Fatigue);
                }

                logPacket(magnitudesCollectedOverTimeForASingleTrialAveragedArray, magnitudeIntervalCollectionAveragedBOS, magnitudeIntervalCollectionAveragedStringBuilder);

                featureValuesOfSingleTrialArray = Util.groupMagnitudesByBandwidth(magnitudesCollectedOverTimeForASingleTrialAveragedArray); //COMMENT: "Don't hold me to that" -Caleb 2017 (printout)

                //logAppEvent("Trial instance [# " + numOfActualTrialsCollectedGivenCurrentClassifierCounter + "] magnitudesCollectedOverTimeForASingleTrialAveragedArray " + "LOGGED");
                //logAppEvent("Trial instance [# " + numOfActualTrialsCollectedGivenCurrentClassifierCounter + "] featureValuesOfSingleTrialArray " + "LOGGED");
                logAppEvent("<Features Calculated>");

                if (isTrialCollectionSimulated) {
                    //Process but drop single trial
                    numOfSimulatedTrialsObservedCounter++;
                    logAppEvent("SIMULATED trial creation [# " + numOfSimulatedTrialsObservedCounter + "]\n");

                } else {
                    isTrialCollectionSimulated = true;



                    if (isTrialEyesOpenAlert && numOfActualAlertTrialsCollected < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {

                        featuresPriorToNormalization2DArrayAlert[numOfActualAlertTrialsCollected] = featureValuesOfSingleTrialArray;
                        numOfActualAlertTrialsCollected++;
                        logAppEvent("ACTUAL Alert trial [# " + numOfActualAlertTrialsCollected + "] " + "COMPLETED\n");

                    } else if (!isTrialEyesOpenAlert && numOfActualFatigueTrialsCollected < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {

                        featuresPriorToNormalization2DArrayFatigue[numOfActualFatigueTrialsCollected] = featureValuesOfSingleTrialArray;
                        numOfActualFatigueTrialsCollected++;
                        logAppEvent("ACTUAL Fatigue trial [# " + numOfActualFatigueTrialsCollected + "] " + "COMPLETED\n");

                        if ((numOfActualAlertTrialsCollected == numOfActualFatigueTrialsCollected) && (numOfActualFatigueTrialsCollected == GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue)) {


                            double[][] featuresPriorToNormalization2DArray_ALL = new double[featuresPriorToNormalization2DArrayAlert.length*2][featuresPriorToNormalization2DArrayAlert[0].length*2];
                            double[][] featuresNormalized2DArray_ALL = new double[featuresPriorToNormalization2DArrayAlert.length*2][featuresPriorToNormalization2DArrayAlert[0].length*2];


                            for (int i = 0; i < featuresNormalized2DArrayAlert.length; i++) {
                                featuresPriorToNormalization2DArray_ALL[i] = featuresPriorToNormalization2DArrayAlert[i];
                                featuresPriorToNormalization2DArray_ALL[i + featuresNormalized2DArrayAlert.length] = featuresPriorToNormalization2DArrayFatigue[i];
                            }

                            featuresNormalized2DArray_ALL = Util.normalizeFeaturesWithBound(featuresPriorToNormalization2DArray_ALL,minMaxFeaturesTrainingDataLogFile);



                            for (int i = 0; i < featuresNormalized2DArrayAlert.length; i++) {
                                featuresNormalized2DArrayAlert[i] = featuresNormalized2DArray_ALL[i];
                                featuresNormalized2DArrayFatigued[i]= featuresNormalized2DArray_ALL[i+featuresNormalized2DArrayAlert.length];
                            }

                            for (int i = 0; i < featuresNormalizedWithClassifierAtIndexZero2DArrayAlert.length; i++) {
                                featuresNormalizedWithClassifierAtIndexZero2DArrayAlert[i][0] = GlobalSettings.EYES_OPEN;
                                featuresNormalizedWithClassifierAtIndexZero2DArrayFatigued[i][0] = GlobalSettings.EYES_CLOSED;

                                for (int j = 0; j < GlobalSettings.numOfFeatures; j++) {
                                    featuresNormalizedWithClassifierAtIndexZero2DArrayAlert[i][j + 1] = featuresNormalized2DArrayAlert[i][j]; //jsnieves:COMMENT: index starts at 1
                                    featuresNormalizedWithClassifierAtIndexZero2DArrayFatigued[i][j + 1] = featuresNormalized2DArrayFatigued[i][j]; //same as above
                                }
                            }

                            //jsnieves:COMMENT: Combine Alert and Fatigue trials into single array
                            int aLen = featuresNormalizedWithClassifierAtIndexZero2DArrayAlert.length;
                            int fLen = featuresNormalizedWithClassifierAtIndexZero2DArrayFatigued.length;

                            double[][] allFeaturesNormalized2DArray = new double[aLen + fLen][GlobalSettings.numOfFeatures + 1]; //jsnieves:COMMENT: FINALLL
                            //jsnieves:COMMENT: need to shift by one index first
                            System.arraycopy(featuresNormalizedWithClassifierAtIndexZero2DArrayAlert, 0, allFeaturesNormalized2DArray, 0, aLen);
                            System.arraycopy(featuresNormalizedWithClassifierAtIndexZero2DArrayFatigued, 0, allFeaturesNormalized2DArray, aLen, fLen);

                            logSVMTrainingDataAll(allFeaturesNormalized2DArray, svmTrainingDataBufferedOutputStream);

                            logAppEvent("All SVM training data stored");

                        }
                    } else {

                        logAppEvent("ERROR Here\n");
                    }
                }
            }

        } else if (GlobalSettings.isEvaluatingRealtimeData) {

        }
    }

    //TODO pass in StringBuilder object

    public void ignorelogPacket(final double[] doubleArray, final BufferedOutputStream bos, final StringBuilder stringBuilder) {
        if (bos != null) {
            new Thread(new Runnable() {
                StringBuilder stringBuilder = new StringBuilder();

                public void run() {
                    stringBuilder.setLength(0);
                    stringBuilder.append("# [" + Util.currentDateAsString() + "] (" + Util.currentMilliTimestampLogAsString() + ") #\r\n");

                    try {
                        for (double d : doubleArray) {
                            stringBuilder.append(d + "\r\n");
                        }

                        bos.write(stringBuilder.toString().getBytes());
                        bos.flush();

                        //jsnieves:COMMENT:close onDestroy?
                        stringBuilder.setLength(0);

                    } catch (Exception ex) {
                        //IOException
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //TODO:elim method

    public synchronized void logPacket(final Object array, final BufferedOutputStream bos, final StringBuilder stringBuilder) {
        //jsnieves:COMMENT:array is either double[] or Complex[]
        //TODO: //jsnieves:COMMENT: modular method (accepts all Obj types, implement try catch and delete above method)
        if (bos != null) {
            new Thread(new Runnable() {

                //StringBuilder stringBuilder = new StringBuilder();

                public void run() {

                    stringBuilder.append("# [" + Util.currentDateAsString() + "] (" + Util.currentMilliTimestampLogAsString() + ") # " + bos.toString() + "\r\n"); // \n
                    try {
                        if (array instanceof double[]) {
                            doubleLogArray = (double[]) array;
                            for (double d : doubleLogArray) {
                                stringBuilder.append(d + "\r\n");
                            }
                        } else if (array instanceof Complex[]) {
                            complexLogArray = (Complex[]) array;
                            for (Complex c : complexLogArray) {
                                stringBuilder.append(c.re() + "\r\n");
                            }
                        } else {
                            System.out.println("ERROR logging data packet");
                        }

                        bos.write(stringBuilder.toString().getBytes());
                        bos.flush();
                        stringBuilder.setLength(0);

                        //jsnieves:COMMENT:close onDestroy?

                    } catch (Exception ex) {
                        //IOException
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void logSVMTrainingDataAll(final double[][] svmFeatAvgGroupedNorm2DArray, final BufferedOutputStream bos) {

        new Thread(new Runnable() {

            public void run() {

                System.out.println("# Reached end of all Trials, logSVMTrainingDataAll #  #");

                StringBuilder svmStringBuilderAll = new StringBuilder();
                String svmStringBuildCollector;

                try {

                    //svmStringBuilder.append((isTrialEyesClosed ? 1 : 0) + " "); //"My b" -Jason (2017)

                    for (int i = 0; i < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2; i++) {

                        svmStringBuilderAll.append((int) svmFeatAvgGroupedNorm2DArray[i][0] + " ");

                        for (int j = 0; j < GlobalSettings.numOfFeatures; j++) {

                            svmStringBuilderAll.append(j + ":" + svmFeatAvgGroupedNorm2DArray[i][j + 1]);
                            if (j < GlobalSettings.numOfFeatures - 1) {
                                svmStringBuilderAll.append(" ");

                            } else {
                                svmStringBuilderAll.append("\r\n");
                            }
                        }
                    }

                    svmStringBuildCollector = svmStringBuilderAll.toString();
                    svmStringBuilderAll.setLength(0); //clears this

                    bos.write(svmStringBuildCollector.getBytes());
                    bos.flush();

                    isCalcTrialFeatures = false; //TODO remove
                    // END of e.g. 100 trial collections and stored to svm_train
                    //TODO organize
                    //create model file

                    double[][] trainingDataset;// = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2][GlobalSettings.numOfFeatures + 1];
                    trainingDataset = svmFeatAvgGroupedNorm2DArray;

                    svm_model model = SVMTrainer.createModel(trainingDataset); //TEMP TODO uncomment

                    String modelFileName = time + "_"+GlobalSettings.svmModelFileName;//SVM_MODEL.txt as of now
                    File modelLogFile = new File(currentUserTrainingDir.getPath(), modelFileName);
                    String modelLogFileString = modelLogFile.toString();


                    modelLogFileEvalCopy = new File(currentUserEvaluationDir.getPath(), modelFileName);

                    //try just building string here
                    //jsnieves:COMMENT:save model to file

                    svm.svm_save_model(modelLogFileString, model); //TEMP TODO uncomment
                    svm.svm_save_model(modelLogFileEvalCopy.toString(), model); //TEMP TODO uncomment


                    //Util.getFeaturesMinMaxValues();
                    //TODO store min max features values here

                    logAppEvent("SVM model file Created!");

                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }
        }).start();
    }

    public void logSVMTrainingData(final double[][] svmFeatAvgGroupedNorm2DArray, final BufferedOutputStream bos) {
        //jsnieves:COMMENT:array is either double[] or Complex[]
        //TODO: //jsnieves:COMMENT: modular method (accepts all Obj types, implement try catch and delete above method)

        new Thread(new Runnable() {

            double[] doubleArray;
            boolean isTrialEyesClosed = true; //TODO

            public void run() {

                System.out.println("# Reached end of all Trials, logSVMTrainingData #  #");

                StringBuilder svmStringBuilder = new StringBuilder();
                String svmStringBuildCollector;

                try {

                    //svmStringBuilder.append((isTrialEyesClosed ? 1 : 0) + " "); //"My b" --Jason 2017

                    for (int i = 0; i < GlobalSettings.calibrationNumOfTrialsToPerformTotal; i++) {

                        svmStringBuilder.append((isTrialEyesClosed ? 1 : 0) + " ");

                        for (int j = 0; j < GlobalSettings.numOfFeatures; j++) {

                            svmStringBuilder.append(j + ":" + svmFeatAvgGroupedNorm2DArray[i][j]);
                            if (j < GlobalSettings.numOfFeatures - 1) {
                                svmStringBuilder.append(" ");

                            } else {
                                svmStringBuilder.append("\r\n");
                            }
                        }
                    }

                    svmStringBuildCollector = svmStringBuilder.toString();
                    svmStringBuilder.setLength(0); //clears this

                    bos.write(svmStringBuildCollector.getBytes());
                    bos.flush();

                    isCalcTrialFeatures = false; //END of e.g. 100 trial collections and stored to svm_train

                    //TODO organize

                    //TODO create model file
                    double[][] trainingDataset = new double[GlobalSettings.calibrationNumOfTrialsToPerformTotal][GlobalSettings.numOfFeatures + 1];
                    for (int i = 1; i < svmFeatAvgGroupedNorm2DArray.length; i++) {
                        trainingDataset[i][0] = isTrialEyesClosed ? 1 : 0;
                        for (int j = 1; j <= GlobalSettings.numOfFeatures; j++) {

                            trainingDataset[i][j] = svmFeatAvgGroupedNorm2DArray[i][j - 1];
                        }
                    }

                    //svm_model model = SVMTrainer.createModel(svmFeatAvgGroupedNorm2DArray);
                    svm_model model = SVMTrainer.createModel(trainingDataset);
                    String modelFileName = time + "_" + GlobalSettings.svmModelFileName;//SVM_MODEL.txt as of now
                    File modelLogFile = new File(currentUserTrainingDir.getPath(), modelFileName);
                    String modelLogFileString = modelLogFile.toString();
                    //try just building string here

                    //jsnieves:COMMENT:save model to file
                    svm.svm_save_model(modelLogFileString, model);
                    //copy to Evaluation folder too?

                    logAppEvent("SVM model file Created!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void initLogCreate() {

        //new File(Environment.getExternalStorageDirectory() + "/DF/abc/");

        if (Util.isExternalStorageReadable() && Util.isExternalStorageWritable()) {

            String tempUserName = "User7"; //TODO: create/call method getCurrentUserName() or similar here
            String currentUserName = tempUserName; //TODO: properly assign
            String appLogFileName = GlobalSettings.appLogFileName;

            time = Util.currentMinuteTimestampAsString();
            date = Util.currentMonthDateUnderscoreFormat();

            String rawFileName = time + "_" + GlobalSettings.rawLogFileName;
            String rawNormFileName = time + "_" + GlobalSettings.rawNormLogFileName;
            String magFileName = time + "_" + GlobalSettings.magLogFileName;
            String magAvgLogFileName = time + "_" + GlobalSettings.magAvgLogFileName;
            String fftComplexLogFileName = time + "_" + GlobalSettings.fftComplexLogFileName;
            String rawComplexLogFileName = time + "_" + GlobalSettings.rawComplexLogFileName;
            String bandPwrFeaturesLogFileName = time + "_" + GlobalSettings.bandPwrFeaturesLogFileName;
            String bandPwrFeaturesAvgLogFileName = time + "_" + GlobalSettings.bandPwrFeaturesAvgLogFileName;
            String svmTrainingDataLogFileName = time + "_" + GlobalSettings.svmTrainingDataLogFileName;


            sdCard = Environment.getExternalStorageDirectory().getAbsoluteFile(); //TODO rename XX app rootDir XX environment dir path
            appDir = new File(sdCard + File.separator + GlobalSettings.getAppRootFolderName()); // + File.separator +folderDate
            usersDir = new File(appDir + File.separator + "Users");
            currentUserDir = new File(usersDir + File.separator + tempUserName); // + File.separator +folderDate
            currentUserDatedDir = new File(currentUserDir + File.separator + date);
            currentUserRawDir = new File(currentUserDir + File.separator + GlobalSettings.rawFolderName);
            currentUserRawDirTimestamped = new File(currentUserRawDir + File.separator + time);
            currentUserTrainingDir = new File(currentUserDir + File.separator + "Training");
            currentUserEvaluationDir = new File(currentUserDir + File.separator + "Evaluation");

            appLogFile = new File(currentUserDir.getPath(), appLogFileName);
            rawLogFile = new File(currentUserRawDirTimestamped.getPath(), rawFileName);
            rawNormLogFile = new File(currentUserRawDirTimestamped.getPath(), rawNormFileName);
            rawComplexLogFile = new File(currentUserRawDirTimestamped.getPath(), rawComplexLogFileName);
            fftComplexLogFile = new File(currentUserRawDirTimestamped.getPath(), fftComplexLogFileName);
            magLogFile = new File(currentUserRawDirTimestamped.getPath(), magFileName);
            magAvgLogFile = new File(currentUserRawDirTimestamped.getPath(), magAvgLogFileName);
            bandPwrFeaturesLogFile = new File(currentUserRawDirTimestamped.getPath(), bandPwrFeaturesLogFileName);
            bandPwrFeaturesAvgLogFile = new File(currentUserRawDirTimestamped.getPath(), bandPwrFeaturesAvgLogFileName);
            svmTrainingDataLogFile = new File(currentUserTrainingDir.getPath(), svmTrainingDataLogFileName);

            minMaxFeaturesTrainingDataLogFile = new File(currentUserTrainingDir.getPath(), featuresTrainingDataMinMaxLogFileName);


            if (appDir.exists()) {
                //Util.deleteDir(usersDir); //temporary
                //TODO temporary deletion

                usersDir.mkdirs();
                logAppEvent("usersDir Created");
            }

            if (!currentUserDir.exists()) {
                currentUserDir.mkdir();
                logAppEvent("currentUserDir Created");
                currentUserTrainingDir.mkdir();
                logAppEvent("currentUserTrainingDir Created");
                //currentUserTestingDir.mkdir();
                //logAppEvent("currentUserTestingDir Created");
                currentUserEvaluationDir.mkdir();
                logAppEvent("currentUserEvaluationDir Created");
                currentUserRawDir.mkdir();
                logAppEvent("currentUserRawDir Created");
            }
            currentUserRawDirTimestamped.mkdir(); //always make this
            logAppEvent("currentUserRawDirTimestamped Created");
        } else {
            showToast("ERROR: external storage is NOT writable", Toast.LENGTH_SHORT);
        }

        try {
            if (!rawLogFile.exists()) { //TODO: delete as File creation will create this already
                rawLogFile.createNewFile();
                logAppEvent("rawLogFile Created");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            //jsnieves:TODO:Handle this
        }

        try {
            appLogBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(appLogFile), 102400);

            rawBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(rawLogFile), 102400);
            rawNormBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(rawNormLogFile), 102400);
            rawComplexBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(rawComplexLogFile), 102400);
            fftComplexArrayResultsBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fftComplexLogFile), 102400);
            magnitudeIntervalCollectionAveragedBOS = new BufferedOutputStream(new FileOutputStream(magAvgLogFile), 102400);
            magnitudeBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(magLogFile), 102400);

            bandPwrFeaturesAvgLogFileNameBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fftComplexLogFile), 102400);//TODO log this
            bandPwrFeaturesLogFileNameBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fftComplexLogFile), 102400);

            svmTrainingDataBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(svmTrainingDataLogFile), 102400);
            featuresTrainingDataMinMaxBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(minMaxFeaturesTrainingDataLogFile), 102400);

        } catch (FileNotFoundException ex) {
            //jsnieves:TODO:Handle this
            System.out.println("rawBufferedOutputStream FAILED");
        }
    }

    public void logAppEvent(final String msg) {
        System.out.println(msg);
        logOut.append("\r\n" + "[" + Util.currentMilliTimestampLogAsString() + "] " + msg);
        focusOnView();

        //TODO have this save to a log file
    }

    private final void focusOnView() {
        sv_Log.post(scrollLogView);

    }

    public void fireTrialCollectorInstance(boolean eyesOpen) {

        //if(!isTrialCurrentlyInProgress){}//TODO keep
        count = 0;
        logAppEvent("ACTUAL trial instance fired off");
        isTrialCollectionSimulated = false;
        numOfProcessedPacketsConsumed = 0;
        isTrialEyesOpenAlert = eyesOpen;
        isProcessedRawDataToBeTransformedAsTrainingData = true;
        isTrainingInProgress = true;

    }


    public void fireTrialCollectorInitializer() {
        minMaxFeaturesTrainingDataLogFile = new File(currentUserTrainingDir.getPath(), featuresTrainingDataMinMaxLogFileName);
        numOfActualTrialsCollectedGivenCurrentClassifierCounter = 0; //ensure we are gathering data from here on out and not just averaging (i.e. -- e.g. the last 3 seconds of baseline with 1 second where the user was following instructions)
        numOfProcessedPacketsConsumed = 0;
        numOfActualAlertTrialsCollected = 0;
        numOfActualFatigueTrialsCollected = 0;

        //isTrialEyesOpenAlert= eyesOpen;
        //isTrialCollectionSimulated=true;
        //numOfSimulatedTrialsObservedCounter = 0;
    }

    public void fireContinuousEvalTesting(boolean eyesOpen) {
        //To measure app detection response time

        isProcessedRawDataToBeTransformedAsFeaturesForEval = true;
        isEvaluationInProgress = true;
        isProcessedRawDataToBeTransformedAsFeaturesForContinuousEval = true;
        //isEvaluationInProgressContinuous = true;

    }

    public void stopEvalTesting(boolean eyesOpen) {

        isProcessedRawDataToBeTransformedAsFeaturesForEval = false;
        isEvaluationInProgress = false;
        isTrialCollectionSimulated = true; //? TODO check

    }

    public void fireEvalTestingInstance(boolean eyesOpen) {

        isEvalEyesOpenAlert = eyesOpen;

        count = 0;
        isProcessedRawDataToBeTransformedAsFeaturesForEval = true;
        isEvaluationInProgress = true;
        isProcessedRawDataToBeTransformedAsFeaturesForContinuousEval = true; //TODO remove
        isEvaluationContinuous = true;
    }

    public void fireEvalInitializer() {

        //TODO fix below
        initLoadMinMaxValues();
        evalFeatures = new double[magnitudeArray.length + 1];
        //svmEvaluator = new SVMEvaluator(this);






        try {


            BufferedReader bufferedReaderInputModel = new BufferedReader(new FileReader(modelLogFileEvalCopy));
            System.out.println("LOADED: " + modelLogFileEvalCopy.getName()); //TODO here dynamically load


            //        svm_model loadedEvalModel = svm.svm_load_model(currentUserEvaluationDir.getPath() + GlobalSettings.svmModelFileName);
            loadedEvalModel = svm.svm_load_model(bufferedReaderInputModel);


        } catch (IOException ex) {
            ex.printStackTrace();
            showToast("FILE NOT FOUND", Toast.LENGTH_SHORT);
        }

    }


    public void loadSVMModelFile(Uri svmModelFile){

        try {

            BufferedReader bufferedReaderInputModel = new BufferedReader(new FileReader(modelLogFileEvalCopy));

            System.out.println("LOADED: " + modelLogFileEvalCopy.getName()); //TODO here dynamically load

            loadedEvalModel = svm.svm_load_model(bufferedReaderInputModel);


        } catch (IOException ex) {
            ex.printStackTrace();
            showToast("FILE NOT FOUND", Toast.LENGTH_SHORT);
        }




    }

    private void showFileChooser() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("*/*");      //all files
        intent.setType("*/*");   //XML file only
        //intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            mwHelper.startActivityForResult(Intent.createChooser(intent, "Select SVM model File"), 123);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            showToast("Please install a File Manager.", Toast.LENGTH_SHORT);
        }
    }


    public void setIsTrialCollectionSimulated(boolean b) {
        isTrialCollectionSimulated = b;
    }

    public void initDrawWaveView() {

        waveView = new DrawWaveView(context);
        wave_layout.addView(waveView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        waveView.setValue(2048, 2048, -2048);
    }

    public void updateWaveView(int data) {
        if (waveView != null) {
            waveView.updateData(data);
        }
    }
    public void initLoadSvmModelFile(){


    }

    public void initLoadMinMaxValues(){

        StringBuffer datax = new StringBuffer("");
        try {
            currentUsersMinMaxDataFile = new File(currentUserTrainingDir + File.separator + time +"_"+ GlobalSettings.featuresMinMaxLogFileName);

            FileInputStream fIn = new FileInputStream(currentUsersMinMaxDataFile);
            //FileInputStream fIn = context.openFileInput( Environment.getExternalStorageDirectory().getAbsoluteFile().toString() + "/" + GlobalSettings.getAppRootFolderName() + "/" + "Users" + "/" + GlobalSettings.userName + "/" + "Evaluation" + "/" + GlobalSettings.featuresMinMaxLogFileName);
            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;
            int i=0;
            double[] minMaxTrainingData=new double[GlobalSettings.numOfFeatures*2];
            String readString = buffreader.readLine ( ) ;
            while ( readString != null  && i < GlobalSettings.numOfFeatures*2) {
                minMaxTrainingData[i]=Double.valueOf(readString);
                i++;
                datax.append(readString);
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;

            //only for two-class
            for(int j =0;j< GlobalSettings.numOfFeatures;j++){
                minTrainingData[j]=minMaxTrainingData[j*2];
                maxTrainingData[j]=minMaxTrainingData[(j*2)+1];
            }



        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }


    }



    public String getTime() {
        return time;
    }

    public void showToast(final String msg, final int timeStyle) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, msg, timeStyle).show();
            }
        });
    }
}