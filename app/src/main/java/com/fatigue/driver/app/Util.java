package com.fatigue.driver.app;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Util extends Activity {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat minuteTimestampFormat = new SimpleDateFormat("HH.mm");
    private static SimpleDateFormat secondTimestampFormat = new SimpleDateFormat("HH.mm.ss");
    private static SimpleDateFormat secondTimestampFormatDirectory = new SimpleDateFormat("HH_mm_ss");
    private static SimpleDateFormat milliTimestampFormat = new SimpleDateFormat("HH.mm.ss.SSS");
    private static SimpleDateFormat milliTimestampLogFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    public static String currentDateAsString() {
        return dateFormat.format(new Date());
    }

    public static String currentMinuteTimestampAsString() {
        return minuteTimestampFormat.format(new Date());
    }

    public static String currentSecondTimestampDirAsString() {
        return secondTimestampFormatDirectory.format(new Date());
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

    public static String currentDateMilliTimestampLogAsFancyString() {
        Date date = new Date();
        return "[" + dateFormat.format(date) + "](" + milliTimestampLogFormat.format(date) + ")";
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

    public static double[] normalizeRawArray(final double[] rawArray) { //TODO: rename normalizeDoubleArray

        //TODO use thread

        double[] normArray = new double[rawArray.length];
        double sd = Util.calcSD(rawArray);
        double mean = Util.calcMean(rawArray);

        for (int i = 0; i < rawArray.length; i++) {
            normArray[i] = (rawArray[i] - mean) / sd;
        }

        return normArray;
    }

    public static double[][] normalizeFeaturesWithBound(double[][] feat2DArray) {

        //COMMENT: passed code Audit inspection --
        //"Blame me if something goes Kaboom"
        // -- Jason (2017)

        //TODO convert to thread
        //TODO consider data processing once ALL trial data has  been collected

        double[] deltaNormArray;
        double[] thetaNormArray;
        double[] alphaNormArray;
        double[] betaLowNormArray;
        double[] betaMidNormArray;
        double[] betaHighNormArray;
        double[] gammaLowNormArray;

        double[] deltaArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] thetaArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] alphaArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaLowArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaMidArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaHighArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] gammaLowArray = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];

        double[] f;

        for (int i = 0; i < feat2DArray.length; i++) {
            f = feat2DArray[i];
            deltaArray[i] = f[0];
            thetaArray[i] = f[1];
            alphaArray[i] = f[2];
            betaLowArray[i] = f[3];
            betaMidArray[i] = f[4];
            betaHighArray[i] = f[5];
            gammaLowArray[i] = f[6];
        }

        double[][] normalizedFeaturesWithZeroOneBound = new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue][GlobalSettings.numOfFeatures];

        deltaNormArray = normalizeArrayWithBound(deltaArray, 0, 1); //TODO remove unnecessary 2nd and 3rd parameters
        thetaNormArray = normalizeArrayWithBound(thetaArray, 0, 1);
        alphaNormArray = normalizeArrayWithBound(alphaArray, 0, 1);
        betaLowNormArray = normalizeArrayWithBound(betaLowArray, 0, 1);
        betaMidNormArray = normalizeArrayWithBound(betaMidArray, 0, 1);
        betaHighNormArray = normalizeArrayWithBound(betaHighArray, 0, 1);
        gammaLowNormArray = normalizeArrayWithBound(gammaLowArray, 0, 1);

        for (int j = 0; j < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue; j++) {
            normalizedFeaturesWithZeroOneBound[j][0] = deltaNormArray[j];
            normalizedFeaturesWithZeroOneBound[j][1] = thetaNormArray[j];
            normalizedFeaturesWithZeroOneBound[j][2] = alphaNormArray[j];
            normalizedFeaturesWithZeroOneBound[j][3] = betaLowNormArray[j];
            normalizedFeaturesWithZeroOneBound[j][4] = betaMidNormArray[j];
            normalizedFeaturesWithZeroOneBound[j][5] = betaHighNormArray[j];
            normalizedFeaturesWithZeroOneBound[j][6] = gammaLowNormArray[j];
        }

        return normalizedFeaturesWithZeroOneBound;
    }

    private static double[] normalizeArrayWithBound(double[] dArray, double lowBound, double highBound) {

        double[] normDoubleArray = new double[dArray.length];
        double[] minMax = findMinAndMax(dArray);
        double min = minMax[0];
        double max = minMax[1];

        double maxMinusMin = max - min;

        for (int i = 0; i < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue; i++) {

            normDoubleArray[i] = ((dArray[i] - min) / maxMinusMin);
        }

        return normDoubleArray;
    }

    public static Complex[] makeComplexArray(final double[] rawNormArray) {

        Complex[] complexArray = new Complex[rawNormArray.length];

        double imaginary = 0.0;

        for (int i = 0; i < rawNormArray.length; i++) {
            complexArray[i] = new Complex(rawNormArray[i], imaginary);
        }

        return complexArray;
    }

    public static double[] groupMagnitudesBandwidth(final double[] magAvgArray) {

        //TODO store individual Hz into array before avg is calculated and stored
        //0-3 delta; 4-7 theta, 8-12 alpha, 13-16 beta.low;17-21 beta.mid; 22-30beta.high; 31-40 gamma.low

        double avgDelta = Util.calcMean(Arrays.copyOfRange(magAvgArray, 1, 3)); //set to drop 0 Hz
        double avgTheta = Util.calcMean(Arrays.copyOfRange(magAvgArray, 4, 7));
        double avgAlpha = Util.calcMean(Arrays.copyOfRange(magAvgArray, 8, 12));
        double avgBetaLow = Util.calcMean(Arrays.copyOfRange(magAvgArray, 13, 16));
        double avgBetaMid = Util.calcMean(Arrays.copyOfRange(magAvgArray, 17, 21));
        double avgBetaHigh = Util.calcMean(Arrays.copyOfRange(magAvgArray, 22, 30));
        double avgGammaLow = Util.calcMean(Arrays.copyOfRange(magAvgArray, 31, 40));

        //System.out.println("#groupMagnitudesBandwidth#");

        //TODO: normalize after all trials are collected
        return new double[]{avgDelta, avgTheta, avgAlpha, avgBetaLow, avgBetaMid, avgBetaHigh, avgGammaLow};
    }

    public synchronized static double[] findMinAndMax(final double[] dArray) {

        if (dArray.length == 1) return new double[]{dArray[0], dArray[0]};

        double min;
        double max;
        min = dArray[0];
        max = dArray[0];

        for (int i = 0; i < dArray.length; i++) {
            if (dArray[i] < min) {
                min = dArray[i];
            } else if (dArray[i] > max) {
                max = dArray[i];
            }
        }

        // jsnieves:TEST OUTPUT //
        // System.out.println("dArray enumerated HERE");
        // for(double d : dArray) System.out.println(d);
        // System.out.println("minMax");
        // System.out.println(min + "       " + max);

        return new double[]{min, max};
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        return "mounted".equals(state);
    }

    public static boolean deleteDir(File dir) {
        // Recursively deletes all child folders, their contents, and the parent directory itself
        if (dir == null)
            return false;

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean isSuccessful = deleteDir(new File(dir, children[i]));
                if (!isSuccessful) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return "mounted".equals(state) || "mounted_ro".equals(state);
    }
}