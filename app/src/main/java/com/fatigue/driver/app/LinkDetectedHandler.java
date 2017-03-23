package com.fatigue.driver.app;

import android.app.Activity;
import android.content.Context;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import libsvm.svm;
import libsvm.svm_model;


public class LinkDetectedHandler extends Handler {
    private static final String TAG = LinkDetectedHandler.class.getSimpleName();

    Context context;
    int count;
    int trialBuilderNumOfSinglePacketInstancesConsumedCounter;
    int totalNumOfSinglePacketInstancesObservedCounter;
    int numOfSimulatedTrialsObservedCounter; //simulated
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
    double[][] magnitude2DArraySingleTrialAlert = new double[(int)GlobalSettings.COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS][GlobalSettings.samplingSizeInterval];
    double[][] magnitude2DArraySingleTrialFatigue = new double[(int)GlobalSettings.COLLECTION_INTERVAL_DURATION_FATIGUE_IN_SECONDS][GlobalSettings.samplingSizeInterval];
    double[] magnitudesArray = new double[GlobalSettings.samplingSizeInterval];
    double[] magnitudesCollectedOverTimeForASingleTrialAveragedArray = new double[GlobalSettings.samplingSizeInterval];
    double[] featureValuesOfSingleTrialArray;
    double[][] featuresPriorToNormalization2DArrayAlert = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlert][GlobalSettings.numOfFeatures];
    double[][] featuresPriorToNormalization2DArrayFatigue = new double[GlobalSettings.calibrationNumOfTrialsToPerformFatigue][GlobalSettings.numOfFeatures];
    double[][] featuresNormalized2DArrayAlert= new double[GlobalSettings.calibrationNumOfTrialsToPerformAlert][GlobalSettings.numOfFeatures];
    double[][] featuresNormalized2DArrayFatigued= new double[GlobalSettings.calibrationNumOfTrialsToPerformFatigue][GlobalSettings.numOfFeatures];
    double[][] featuresNormalizedWithClassifierAtIndexZero2DArrayAlert=new double[featuresNormalized2DArrayAlert.length][GlobalSettings.numOfFeatures+1];
    double[][] featuresNormalizedWithClassifierAtIndexZero2DArrayFatigued =new double[featuresNormalized2DArrayFatigued.length][GlobalSettings.numOfFeatures+1];
    boolean isRawDataLoggingEnabled;
    boolean isProcessedRawDataLoggingEnabled;
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
    svm_model loadedEvalModel;
    boolean isConnected;
    TextView logOut;
    ScrollView sv_Log;
    SVMEvaluator svmEvaluator;
    double[] evalFeatures=new double[GlobalSettings.numOfFeatures];
    double[] evalFeaturesWithClassifierAtIndexZero=new double[GlobalSettings.numOfFeatures+1];
    private File appDir;
    private File usersDir;
    private File currentUserDir; //User.user_name;

    //double[][] featuresPriorToNormalization2DArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue][GlobalSettings.numOfFeatures];
    //double[][] featuresAvgGroupedNorm2DArray= new double[GlobalSettings.calibrationNumOfTrialsToPerformTotal][GlobalSettings.numOfFeatures];
    //double[][] allFeaturesAvgGroupedNorm2DArray= new double[GlobalSettings.calibrationNumOfTrialsToPerformTotal *2][GlobalSettings.numOfFeatures+1];
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
    private StringBuilder rawNormalizedStringBuilder = new StringBuilder();
    private StringBuilder rawComplexStringbuilder = new StringBuilder();
    private StringBuilder fftStringBuilder = new StringBuilder();
    private StringBuilder magnitudesStringBuilder = new StringBuilder();
    private LinearLayout wave_layout;
    private DrawWaveView waveView;

    public LinkDetectedHandler(Context context){
        this.context = context;

        count = 0;
        trialBuilderNumOfSinglePacketInstancesConsumedCounter = 0;
        totalNumOfSinglePacketInstancesObservedCounter = 0;
        numOfSimulatedTrialsObservedCounter = 0;
        numOfActualTrialsCollectedGivenCurrentClassifierCounter =0;
        totalNumOfAllTrialsCounter =0;

        isConnected=false;
        isRawDataLoggingEnabled = true; //TODO pull these from GlobalSettings
        isRecordingRawNormData = true;
        isCalcFFT = true;
        isRecordingFFT = true;
        isRecordingMagnitudeArray = true;
        isRecordingMagnitudeAveragedArray = true;
        isCalcMagnitude = true;
        isRecordingRawComplexArray=true;
        isCalcTrialFeatures=true;
        isProcessedRawDataLoggingEnabled = true;
        isProcessedRawDataToBeTransformedAsTrainingData = true;

        isDataLoggingEnabled = GlobalSettings.isLogData;
        isRawDataProcessingRequested = GlobalSettings.isProcessRawData;
        isAllDataLoggingRequested = GlobalSettings.isLogAllData;
        isLogAndRecordSimulatedTrainingData = GlobalSettings.isLogAndRecordTrainingData;
        isRecordNoData = GlobalSettings.isRecordNoData;

        isTrialCollectionSimulated = true; //jsnieves:COMMENT:needs to be manually fired for training data collection to start/con't/resume

        View rootView = ((Activity)context).getWindow().getDecorView().findViewById(android.R.id.content);
        logOut = (TextView) rootView.findViewById(R.id.tv_log);
        sv_Log = (ScrollView) rootView.findViewById(R.id.scrollViewLog);
        wave_layout = (LinearLayout) rootView.findViewById(R.id.wave_layout);
        //waveView;

        rawDataDoublesArray = new double[GlobalSettings.samplingSizeInterval];

        initLogCreate();
        // was before rawDataDoublesArray

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
                int poorSignal = msg.arg1;
                //if (isDebugVerbose || isDebug) Log.d(TAG, "poorSignal:" + poorSignal);
                //tv_ps.setText("" + msg.arg1);
                //logAppEvent.append();
                break;

            case MindDataType.CODE_FILTER_TYPE: //jsnieves:COMMENT:Enum FilterType : FILTER_50HZ(4), FILTER_60HZ(5)
                //Log.d(TAG, "CODE_FILTER_TYPE: " + msg.arg1);
                break;

            //case MSG_UPDATE_BAD_PACKET:
            //    break;

            case MindDataType.CODE_RAW:
                rawDataDoublesArray[count] = (double) msg.arg1;
                count++;

                //WAVEVIEW
                updateWaveView(msg.arg1);
                //WAVEVIEW

                if(count>=512){
                    count=0;
                    processRawPacket(rawDataDoublesArray);
                    //send to logger(rawDataDoublesArray);
                }
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

        if (isRawDataLoggingEnabled) {
            logPacket(rawPacketArray, rawBufferedOutputStream);
        }

        if (isRawDataProcessingRequested) {

            rawNormalizedArray = Util.normalizeRawArray(rawPacketArray); //jsnieves:COMMENT: Normalize to remove DC component?? TODO check with Caleb
            rawComplexArray = Util.makeComplexArray(rawNormalizedArray); //jsnieves:COMMENT: Create Complex array
            fftComplexArrayResults = FFT.fft(rawComplexArray);  //jsnieves:COMMENT: Calc FFT ComplexArray
            magnitudesArray = Magnitude.mag(fftComplexArrayResults);  //jsnieves:COMMENT: Find Magnitude values

            //Log results?
            if (isProcessedRawDataLoggingEnabled) {
                logPacket(rawNormalizedArray, rawNormBufferedOutputStream, rawNormalizedStringBuilder);
                logPacket(rawComplexArray, rawComplexBufferedOutputStream, rawComplexStringbuilder);
                logPacket(fftComplexArrayResults, fftComplexArrayResultsBufferedOutputStream, fftStringBuilder);
                logPacket(magnitudesArray, magnitudeBufferedOutputStream, magnitudesStringBuilder);
            }

            totalNumOfSinglePacketInstancesObservedCounter++; //jsnieves:COMMENT: 1 second of data

        }

        //if(isAppLoggingEvents){} !!!!!! (jsnieves:COMMENT)

        if(isProcessedRawDataToBeTransformedAsFeaturesForEval && loadedEvalModel!=null){

            evalFeaturesWithClassifierAtIndexZero[0] = isEvalEyesOpenAlert ? GlobalSettings.EYES_OPEN : GlobalSettings.EYES_CLOSED;
            evalFeatures = Util.groupMagnitudesBandwidth(magnitudesArray);
            System.arraycopy(evalFeatures, 0, evalFeaturesWithClassifierAtIndexZero, 1, evalFeaturesWithClassifierAtIndexZero.length-1);

            if(loadedEvalModel!=null) {
                svmEvaluator.evaluate(evalFeaturesWithClassifierAtIndexZero, loadedEvalModel);
            }

            if(!isProcessedRawDataToBeTransformedAsFeaturesForContinuousEval){
                isProcessedRawDataToBeTransformedAsFeaturesForEval=false; //wait for next eval instance to be fired
            }

        } else if (isProcessedRawDataToBeTransformedAsTrainingData) {

            if (trialBuilderNumOfSinglePacketInstancesConsumedCounter < (isTrialEyesOpenAlert ? GlobalSettings.NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_ALERT : GlobalSettings.NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_FATIGUE)) {
                //jsnieves:COMMENT: continue collection
                //make generic here, look at later (re check)

                if (isTrialEyesOpenAlert) {
                    magnitude2DArraySingleTrialAlert[trialBuilderNumOfSinglePacketInstancesConsumedCounter] = magnitudesArray; //rename ! to single trial Collector or something similar
                    //System.out.println("Alert magnitudesArray test printout index 0: " + magnitudesArray[0]);
                } else {
                    magnitude2DArraySingleTrialFatigue[trialBuilderNumOfSinglePacketInstancesConsumedCounter] = magnitudesArray; //rename ! to single trial Collector or something similar
                    //System.out.println("Fatigue magnitudesArray test printout index 0: " + magnitudesArray[0]);
                }

                trialBuilderNumOfSinglePacketInstancesConsumedCounter++;
                logAppEvent((isTrialEyesOpenAlert ? "Alert" : "Fatigue")+" Mag. calc. (" + trialBuilderNumOfSinglePacketInstancesConsumedCounter + ") from pkt (#" + totalNumOfSinglePacketInstancesObservedCounter+")");

            } else {
                //No no, process and log but do not add to 2DArray
                //All trial instances have been collected here

                //reset for next trial collection
                trialBuilderNumOfSinglePacketInstancesConsumedCounter = 0;

                if(isTrialEyesOpenAlert) {
                    magnitudesCollectedOverTimeForASingleTrialAveragedArray = Util.calcDoubleArrayMean(magnitude2DArraySingleTrialAlert); //TOO recheck here, should we calc features first then average?
                }else {
                    magnitudesCollectedOverTimeForASingleTrialAveragedArray = Util.calcDoubleArrayMean(magnitude2DArraySingleTrialFatigue);
                }

                logPacket(magnitudesCollectedOverTimeForASingleTrialAveragedArray, magnitudeIntervalCollectionAveragedBOS);

                featureValuesOfSingleTrialArray = Util.groupMagnitudesBandwidth(magnitudesCollectedOverTimeForASingleTrialAveragedArray); //COMMENT: "Don't hold me to that" -Caleb 2017 (printout)

                //logAppEvent("Trial instance [# " + numOfActualTrialsCollectedGivenCurrentClassifierCounter + "] magnitudesCollectedOverTimeForASingleTrialAveragedArray " + "LOGGED");
                //!!!TODO: logPacket(featureValuesOfSingleTrialArray, XXXBOS);
                //logAppEvent("Trial instance [# " + numOfActualTrialsCollectedGivenCurrentClassifierCounter + "] featureValuesOfSingleTrialArray " + "LOGGED");

                if (isTrialCollectionSimulated) {
                    //Process but drop single trial
                    numOfSimulatedTrialsObservedCounter++;
                    logAppEvent("<Features Calculated>");
                    logAppEvent("SIMULATED trial creation [# " + numOfSimulatedTrialsObservedCounter + "]\n");

                } else {
                    isTrialCollectionSimulated = true; //jsnieves:COMMENT: transition period or otherwise

                    //logAppEvent("ACTUAL " + (isTrialEyesOpenAlert ? "Alert" : "Fatigue") + " trial [# " + numOfActualTrialsCollectedGivenCurrentClassifierCounter + "] " + "COMPLETED\n");

                    if (isTrialEyesOpenAlert && numOfActualAlertTrialsCollected < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {

                        featuresPriorToNormalization2DArrayAlert[numOfActualAlertTrialsCollected] = featureValuesOfSingleTrialArray;
                        numOfActualAlertTrialsCollected++;
                        logAppEvent("ACTUAL Alert trial [# " + numOfActualAlertTrialsCollected + "] " + "COMPLETED\n");

                    } else if (!isTrialEyesOpenAlert && numOfActualFatigueTrialsCollected < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {

                        featuresPriorToNormalization2DArrayFatigue[numOfActualFatigueTrialsCollected] = featureValuesOfSingleTrialArray;
                        numOfActualFatigueTrialsCollected++;
                        logAppEvent("ACTUAL Fatigue trial [# " + numOfActualFatigueTrialsCollected + "] " + "COMPLETED\n");

                        if ((numOfActualAlertTrialsCollected == numOfActualFatigueTrialsCollected) && (numOfActualFatigueTrialsCollected == GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue)) {
                            //jsnieves:COMMENT: All trials should completed here

                            //jsnieves:COMMENT: Normalize between 0 and 1 //TODO TEMP features not normalized

                            featuresNormalized2DArrayAlert = Util.normalizeFeaturesWithBound(featuresPriorToNormalization2DArrayAlert);
                            featuresNormalized2DArrayFatigued = Util.normalizeFeaturesWithBound(featuresPriorToNormalization2DArrayFatigue);

                            //featuresNormalized2DArrayAlert = featuresPriorToNormalization2DArrayAlert; //TODO normalization
                            //featuresNormalized2DArrayFatigued = featuresPriorToNormalization2DArrayFatigue;

                            //jsnieves:COMMENT: Conform Array to svm standard
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

        } else if (GlobalSettings.isEvaluatingRealtimeData){
            //can't be training and evaluating so else if used here
            //if(isEvaluatingRealtimeData && svm_model!=null ...etc)

            //TODO Eval coding
            //reverse order of if block

        }
    }

    public void logPacket ( final double[] doubleArray, final BufferedOutputStream bos){
        if(bos!=null){
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

    public synchronized void logPacket ( final Object array, final BufferedOutputStream bos, final StringBuilder stringBuilder){
        //jsnieves:COMMENT:array is either double[] or Complex[]
        //TODO: //jsnieves:COMMENT: modular method (accepts all Obj types, implement try catch and delete above method)
        if(bos!=null){
            new Thread(new Runnable() {

                //StringBuilder stringBuilder = new StringBuilder();

                public void run() {

                    stringBuilder.append("# [" + Util.currentDateAsString() + "] (" + Util.currentMilliTimestampLogAsString() + ") # " + bos.toString() + "\r\n"); // \n
                    try {
                        if (array instanceof Complex[]) {
                            complexLogArray = (Complex[]) array;
                            for (Complex c : complexLogArray) {
                                stringBuilder.append(c.re() + "\r\n");
                            }
                        } else if (array instanceof double[]) {
                            doubleLogArray = (double[]) array;
                            for (double d : doubleLogArray) {
                                stringBuilder.append(d + "\r\n");
                            }
                        } else {
                            System.out.println("ERROR");
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

    public void logSVMTrainingDataAll ( final double[][] svmFeatAvgGroupedNorm2DArray, final BufferedOutputStream bos){

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
                            if (j < GlobalSettings.numOfFeatures-1) {
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

                    String modelFileName = GlobalSettings.svmModelFileName;//SVM_MODEL.txt as of now
                    File modelLogFile = new File(currentUserTrainingDir.getPath(), modelFileName);
                    String modelLogFileString = modelLogFile.toString();


                    File modelLogFileEvalCopy = new File(currentUserEvaluationDir.getPath(), modelFileName);

                    //try just building string here
                    //jsnieves:COMMENT:save model to file

                    svm.svm_save_model(modelLogFileString, model); //TEMP TODO uncomment
                    svm.svm_save_model(modelLogFileEvalCopy.toString(), model); //TEMP TODO uncomment


                    //copy to Evaluation folder too?

                    logAppEvent("SVM model file Created!");

                    /*int svCoefLength = model.sv_coef.length;
                    for(int i=0;i<svCoefLength;i++){
                        for(int j=0;j<model.l;j++) {
                            System.out.println("model.sv_coef[" + i + "] : " + model.sv_coef[i][j]);
                        }
                    }*/

                    /*double[][] trainingDataset = new double[GlobalSettings.calibrationNumOfTrialsToPerformTotal][GlobalSettings.numOfFeatures+1];
                    for(int i =1;i<svmFeatAvgGroupedNorm2DArray.length;i++){
                        trainingDataset[i][0]= isTrialEyesClosed ? 1:0;
                        for(int j=1 ;j<= GlobalSettings.numOfFeatures;j++) {

                            trainingDataset[i][j] = svmFeatAvgGroupedNorm2DArray[i][j-1];
                        }
                    }*/

                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }
        }).start();
    }

    public void logSVMTrainingData ( final double[][] svmFeatAvgGroupedNorm2DArray, final BufferedOutputStream bos){
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
                    String modelFileName = GlobalSettings.svmModelFileName;//SVM_MODEL.txt as of now
                    File modelLogFile = new File(currentUserTrainingDir.getPath(), modelFileName);
                    String modelLogFileString = modelLogFile.toString();
                    //try just building string here

                    //jsnieves:COMMENT:save model to file
                    svm.svm_save_model(modelLogFileString, model);
                    //copy to Evaluation folder too?

                    logAppEvent("SVM model file Created!");

                    //int svCoefLength = model.sv_coef.length;
                    //for(int i=0;i<svCoefLength;i++){

                    //  for(int j=0;j<model.l;j++) {
//                            System.out.println("model.sv_coef[" + i + "] : " + model.sv_coef[i][j]);
//                        }                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public void initLogCreate () {

        final String time = Util.currentMinuteTimestampAsString();

        String tempUserName = "User1"; //TODO: create/call method getCurrentUserName() or similar here
        String currentUserName = tempUserName; //TODO: properly assign

        String appLogFileName = GlobalSettings.appLogFileName;
        String rawFileName = time + "_" + GlobalSettings.rawLogFileName;
        String rawNormFileName = time + "_" + GlobalSettings.rawNormLogFileName;
        String magFileName = time + "_" + GlobalSettings.magLogFileName;
        String magAvgLogFileName = time + "_" + GlobalSettings.magAvgLogFileName;
        String fftComplexLogFileName = time + "_" + GlobalSettings.fftComplexLogFileName;
        String rawComplexLogFileName = time + "_" + GlobalSettings.rawComplexLogFileName;
        String bandPwrFeaturesLogFileName = time + "_" + GlobalSettings.bandPwrFeaturesLogFileName;
        String bandPwrFeaturesAvgLogFileName = time + "_" + GlobalSettings.bandPwrFeaturesAvgLogFileName;
        String svmTrainingDataLogFileName = time + "_" + GlobalSettings.svmTrainingDataLogFileName;

        //new File(Environment.getExternalStorageDirectory() + "/DF/abc/");

        if (Util.isExternalStorageWritable()) {
            appDir = Environment.getExternalStorageDirectory().getAbsoluteFile(); //TODO rename XX app rootDir XX environment dir path
            appDir = new File(appDir + File.separator + com.fatigue.driver.app.GlobalSettings.getAppRootFolderName()); // + File.separator +folderDate

            usersDir = new File(appDir + File.separator + "Users");
            //Util.deleteDir(usersDir); //temporary purge every time app is started
            currentUserDir = new File(usersDir + File.separator + tempUserName); // + File.separator +folderDate

            currentUserTrainingDir = new File(currentUserDir + File.separator + "Training");
            currentUserTestingDir = new File(currentUserDir + File.separator + "Testing");
            currentUserEvaluationDir = new File(currentUserDir + File.separator + "Evaluation");
            currentUserRawDir = new File(currentUserDir + File.separator + GlobalSettings.rawFolderName);
            currentUserRawDirTimestamped = new File(currentUserRawDir + File.separator + time);

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

            if (!appDir.exists() || appDir.exists()) { //TODO temporary deletion

                //Util.deleteDir(usersDir); //temporary

                usersDir.mkdirs();
                logAppEvent("usersDir Created");
            }

            if (!currentUserDir.exists()) {
                currentUserDir.mkdir();
                logAppEvent("currentUserDir Created");
                currentUserTrainingDir.mkdir();
                logAppEvent("currentUserTrainingDir Created");
                currentUserTestingDir.mkdir();
                logAppEvent("currentUserTestingDir Created");
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

        } catch (FileNotFoundException ex) {
            //jsnieves:TODO:Handle this
            System.out.println("rawBufferedOutputStream FAILED");
        }
    }

    public void logAppEvent ( final String msg){
        System.out.println(msg);
        //logOut.append("\r\n" + "["+Util.currentMilliTimestampLogAsString() + "] " + msg);
        focusOnView();

        //TODO have this save to a log file
    }

    private final void focusOnView () {
        sv_Log.post(new Runnable() {
            @Override
            public void run() {
                sv_Log.smoothScrollTo(0, logOut.getBottom());
            }
        });
    }

    public void fireTrialCollectorInstance ( boolean eyesOpen){

        //if(!isTrialCurrentlyInProgress){}//TODO keep
        count=0;
        logAppEvent("ACTUAL trial instance fired off");
        isTrialCollectionSimulated = false;
        trialBuilderNumOfSinglePacketInstancesConsumedCounter = 0;
        isTrialEyesOpenAlert = eyesOpen;
        isProcessedRawDataToBeTransformedAsTrainingData=true;

        //numOfSimulatedTrialsObservedCounter=0;
        //numOfActualTrialsCollectedGivenCurrentClassifierCounter =0; //ensure we are gathering data from here on out, not averaging e.g. the last 3 seconds of baseline with 1 second where the user was following instructions
    }


    public void fireTrialCollectorInitializer () {

        numOfActualTrialsCollectedGivenCurrentClassifierCounter = 0; //ensure we are gathering data from here on out and not just averaging (i.e. -- e.g. the last 3 seconds of baseline with 1 second where the user was following instructions)
        trialBuilderNumOfSinglePacketInstancesConsumedCounter = 0;
        numOfActualAlertTrialsCollected=0;
        numOfActualFatigueTrialsCollected=0;

        //isTrialEyesOpenAlert= eyesOpen;
        //isTrialCollectionSimulated=true;
        //numOfSimulatedTrialsObservedCounter = 0;
    }

    public void fireContinuousEvalTesting (boolean eyesOpen) {
        //To measure app detection response time

        isProcessedRawDataToBeTransformedAsFeaturesForEval=true;
        isProcessedRawDataToBeTransformedAsFeaturesForContinuousEval=true;

    }

    public void stopEvalTesting (boolean eyesOpen) {

        isProcessedRawDataToBeTransformedAsFeaturesForEval=false;

        isTrialCollectionSimulated=true; //? TODO check

    }

    public void fireEvalTestingInstance (boolean eyesOpen) {

        isEvalEyesOpenAlert=eyesOpen;

        count=0;
        isProcessedRawDataToBeTransformedAsFeaturesForEval=true;
        isProcessedRawDataToBeTransformedAsFeaturesForContinuousEval=true; //TODO remove

    }

    public void fireEvalInitializer () {

//TODO fix below

        evalFeatures = new double[magnitudesArray.length+1];

        svmEvaluator = new SVMEvaluator();

        try {

            //FileReader fIn = new FileReader(GlobalSettings.svmModelFileName);
            //BufferedReader bufferedReader = new BufferedReader(fIn);


            BufferedReader bufferedReaderInputModel = new BufferedReader(new FileReader(currentUserEvaluationDir.getPath() +File.separator+ GlobalSettings.svmModelFileName));
            //        svm_model loadedEvalModel = svm.svm_load_model(currentUserEvaluationDir.getPath() + GlobalSettings.svmModelFileName);
            loadedEvalModel = svm.svm_load_model(bufferedReaderInputModel);


        } catch (IOException ex){
            ex.printStackTrace();
            showToast("FILE NOT FOUND", Toast.LENGTH_SHORT);
        }

/*
        numOfActualTrialsCollectedGivenCurrentClassifierCounter = 0; //ensure we are gathering data from here on out and not just averaging (i.e. -- e.g. the last 3 seconds of baseline with 1 second where the user was following instructions)
        trialBuilderNumOfSinglePacketInstancesConsumedCounter = 0;
        numOfActualAlertTrialsCollected=0;
        numOfActualFatigueTrialsCollected=0;
*/
        //isTrialEyesOpenAlert= eyesOpen;
        //isTrialCollectionSimulated=true;
        //numOfSimulatedTrialsObservedCounter = 0;
    }

    public void setIsTrialCollectionSimulated (boolean b){
        isTrialCollectionSimulated = b;
    }

    public void initDrawWaveView () {

        waveView = new DrawWaveView(context);
        wave_layout.addView(waveView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        waveView.setValue(2048, 2048, -2048);
    }

    public void updateWaveView ( int data){
        if (waveView != null) {
            waveView.updateData(data);
        }
    }

    public void showToast ( final String msg, final int timeStyle){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, msg, timeStyle).show();
            }
        });
    }
}