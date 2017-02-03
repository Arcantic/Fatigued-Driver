package com.fatigue.driver.app;

public class GlobalSettings {
    public static final String appRootFolder = "Fatigue";
    public static int samplingSizeInt = 512;
    public static int debugLevel = 5;

    public GlobalSettings() {
    }

    public static String getAppRootFolderName() {
        return "Fatigue";
    }

    public static double getPollingIntervalInSeconds() {
        return (double) (samplingSizeInt / 512);
    }
}