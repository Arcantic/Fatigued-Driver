package com.fatigue.driver.app;

public class GlobalSettings {
    public static final String APP_SETTINGS_FILENAME = "config.ini";
    public static final int COLLECTION_INTERVAL_DURATION_ALERT_IN_SECONDS = 5;
    public static String appRootFolderName = "DriverFatigue";
    public static String rawFolderName = "RawData";
    public static String rawLogFileName = "Raw.log";
    public static String rawNormLogFileName = "RawNorm.log";
    public static String magLogFileName = "Magnitude.log";
    public static String magAvgLogFileName = "MagnitudesAveraged.log";
    public static String fftComplexLogFileName = "FFT.log";
    public static String rawComplexLogFileName = "RawComplex.log";
    public static boolean isDebug = true;
    public static boolean isDebugVerbose = true;
    public static int samplingSizeInterval = 512;
    public static int collectionIntervalDurationFatigue = 2;
    public static int calibrationTotalNumOfTrialsToPerformAlert = 100;
    public static int calibrationTotalNumOfTrialsToPerformFatigue = 100;


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