package com.fatigue.driver.app;

import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat minuteTimestampFormat = new SimpleDateFormat("HH;mm");
    private static SimpleDateFormat secondTimestampFormat = new SimpleDateFormat("HH;mm;ss");
    private static SimpleDateFormat milliTimestampFormat = new SimpleDateFormat("HH;mm;ss;SSS");
    private static SimpleDateFormat milliTimestampLogFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    public static String currentDateAsString() {
        return dateFormat.format(new Date());
    }

    public static String currentMinuteTimestampAsString() {
        return minuteTimestampFormat.format(new Date());
    }

    public static String currentSecondTimestampAsString() {
        return secondTimestampFormat.format(new Date());
    }

    public static String currentMilliTimestampAsString() {
        return milliTimestampFormat.format(new Date());
    }

    public static String currentMilliTimestampLogAsString() {
        return milliTimestampLogFormat.format(new Date());
    }

    public static double calcSD(double[] array) {
        double[] temp = new double[array.length];
        double mean = calcMean(array);

        for (int i = 0; i < array.length; i++) {
            temp[i] = Math.pow((array[i] - mean), 2.0);
        }

        double sd = Math.sqrt(calcMean(temp));

        return sd;
    }

    public static double[] calcDoubleArrayMean(double[][] array) {
        double[] mean = new double[array[0].length];
        double[] values = new double[array.length];

        for (int i = 0; i < array[0].length; i++) { //e.g. 0-511

            for (int j = 0; j < array.length; j++) { //e.g. 0-4
                // total += array[j][i];
                values[j] = array[j][i];
            }
            mean[i] = calcMean(values);
        }

        return mean;
    }

    public static double calcMean(double[] array) {
        double total = 0.0;
        for (double num : array) {
            total += num;
        }

        return total / array.length;
    }

    public static double calcSum(double[] array) {
        double sum = 0.0;
        for (double num : array) {
            sum += num;
        }

        return sum;
    }

    public static double[] normalizeRawArray(final double[] rawArray) {
        double[] normArray = new double[rawArray.length];
        double sd = Util.calcSD(rawArray);
        double mean = Util.calcMean(rawArray);

        for (int i = 0; i < rawArray.length; i++) {
            normArray[i] = (rawArray[i] - mean) / sd;
        }

        return normArray;
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        return "mounted".equals(state);
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return "mounted".equals(state) || "mounted_ro".equals(state);
    }
}