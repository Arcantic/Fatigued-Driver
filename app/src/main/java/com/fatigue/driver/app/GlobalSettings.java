package com.fatigue.driver.app;

public class GlobalSettings {

    public static final String APP_SETTINGS_FILENAME = "config.ini";
    public static final int EYES_CLOSED = 1;
    public static final int EYES_OPEN = 0;
    public static int COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS = 5;
    public static int COLLECTION_INTERVAL_DURATION_FATIGUE_IN_SECONDS = 2;
    public static int samplingSizeInterval = 512;
    public static int NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_ALERT = 5;
    public static int NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_FATIGUE = 2;
    public static int numOfPacketsToConsumePerTrial_Alert = 5;
    public static int numOfPacketsToConsumePerTrial_Fatigue = 2;
    public static int alertTrialCollectionIntervalDuration = 5;
    public static int fatigueTrialCollectionIntervalDuration = 2;
    public static int alertDelayTimeBetweenTrialCollections = 1;
    public static int fatigueDelayTimeBetweenTrialCollections = 1;
    public static int timeToDelayBetweenTrialCollections_Alert = 1;
    public static int timeToDelayBetweenTrialCollections_Fatigue = 1;
    public static int calibrationNumOfTrialsToPerformTotal = 200; //TODO temp value
    public static int calibrationNumOfTrialsToPerformAlert = 100; //TODO temp value
    public static int calibrationNumOfTrialsToPerformFatigue = 100; //TODO temp value
    public static int calibrationNumOfTrialsToPerformAlertOrFatigue = 100; //TODO temp value
    public static int numOfTrialsToPerformForTraining_CombinedTotal = 200;
    public static int numOfTrialsToPerformForTraining_Alert = 100;
    public static int numOfTrialsToPerformForTraining_Fatigue = 100;
    public static int numOfTrialsToPerformForTraining_Agnostic = 100;
    public static String svmTrainingDataLogFileName = "svm_train.txt"; //TEMP remove extension
    public static String svmModelFileName = "SVM_MODEL.txt"; //TEMP remove extension
    public static String featuresMinMaxLogFileName = "14.13_ER_4_4_MINMAX_OF_FEATURES.txt"; //TEMP remove extension
    public static String userName = "UNKNOWN";
    public static String appRootFolderName = "DriverFatigue";
    public static String appLogFileName = "DriverFatigueApp.log";
    public static String rawFolderName = "RawData";
    public static String rawLogFileName = "Raw.txt";
    public static String rawNormLogFileName = "RawNorm.txt";
    public static String magLogFileName = "Magnitude.txt";
    public static String magAvgLogFileName = "MagnitudesAveraged.txt";
    public static String bandPwrFeaturesLogFileName = "BandPowerValues.txt";
    public static String bandPwrFeaturesAvgLogFileName = "BandPowerMeanValues.txt";
    public static String fftComplexLogFileName = "FFT.txt";
    public static String rawComplexLogFileName = "RawComplex.txt";
    public static boolean isDebug = true;
    public static boolean isDebugVerbose = true;
    public static boolean isRecordingRawData = true;
    public static boolean isRecordingRawNormData = true;
    public static boolean isCalcFFT = true;
    public static boolean isRecordingFFT = true;
    public static boolean isRecordingMagnitudeArray = true;
    public static boolean isRecordingMagnitudeAveragedArray = true;
    public static boolean isCalcMagnitude = true;
    public static boolean isRecordingRawComplexArray = true;
    public static boolean isCalcTrialFeatures = true;
    public static boolean isLogAllData = true;
    public static boolean isLogData = true;
    public static boolean isProcessRawData = true;
    public static boolean isLogAndRecordTrainingData = true;
    public static boolean isRecordNoData = false;
    public static boolean isEvaluatingRealtimeData = false;
    public static int numOfFeatures = 7; //TODO smarter implementation of this

    public GlobalSettings() {}

    public static String getAppRootFolderName() {
        return appRootFolderName;
    }

    public static void setAppRootFolderName(String s) {
        appRootFolderName = s;
    }

    public static double getPollingIntervalInSeconds() {
        return (double) (samplingSizeInterval / 512);
    }

    public static void setAlertConfiguration() {}

    public static void setTrialCount(int count){
        calibrationNumOfTrialsToPerformTotal = count;
        calibrationNumOfTrialsToPerformAlert = calibrationNumOfTrialsToPerformFatigue
                = calibrationNumOfTrialsToPerformAlertOrFatigue = count/2;
    }
    public static void setTransitionDuration(int dur){
        timeToDelayBetweenTrialCollections_Alert = timeToDelayBetweenTrialCollections_Fatigue = alertDelayTimeBetweenTrialCollections = fatigueDelayTimeBetweenTrialCollections = dur;
    }
    public static void setAlertDuration(int dur){
        alertTrialCollectionIntervalDuration = COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS = NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_ALERT = dur;
    }
    public static void setFatigueDuration(int dur){
        fatigueTrialCollectionIntervalDuration = COLLECTION_INTERVAL_DURATION_FATIGUE_IN_SECONDS = NUMBER_OF_RAW_PACKETS_TO_CONSUME_FOR_EACH_TRIAL_FATIGUE = dur;
    }


}
