package com.fatigue.driver.app;

public class GlobalSettings {
    public static final String appRootFolder = "DriverFatigue";
    public static final String appSettingsFilename = "Config.ini";

    public static int samplingSizeInt = 512;
    public static int debugLevel = 5;

    public static boolean verboseDebug = true;

    public GlobalSettings() {
    }

    public static String getAppRootFolderName() {
        return appRootFolder;
    }

    public static double getPollingIntervalInSeconds() {
        return (double) (samplingSizeInt / 512);
    }
}