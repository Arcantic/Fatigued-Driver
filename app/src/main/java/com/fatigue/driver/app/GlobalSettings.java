package com.fatigue.driver.app;

public class GlobalSettings {

    public static final String APP_SETTINGS_FILENAME = "config.ini";
    public static final int COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS = 3; //TODO temp value, set to 5
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
    public static String svmTraingDataLogFileName = "svm_train.txt"; //TEMP remove extension

    public static boolean isDebug = true;
    public static boolean isDebugVerbose = true;

    public static int samplingSizeInterval = 512;
    public static int collectionIntervalDurationFatigue = 2;
    public static int collectionIntervalDurationAlert = 5;
    public static int calibrationTotalNumOfTrialsToPerformAlert = 100;
    public static int calibrationTotalNumOfTrialsToPerformFatigue = 100;
    public static int calibrationTotalNumOfTrialsToPerform = 5; //TODO temp value
    public static int numOfFeatures = 7; //TODO smarter implementation of this

    public static int EYES_CLOSED = 1;
    public static int EYES_OPEN = 0;

    public GlobalSettings() {
    }

    public static String getAppRootFolderName() {
        return appRootFolderName;
    }

    public static void setAppRootFolderName(String s){
        appRootFolderName = s;
    }

    public static double getPollingIntervalInSeconds() {
        return (double) (samplingSizeInterval / 512);
    }
}