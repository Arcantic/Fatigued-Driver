package com.fatigue.driver.app;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Util extends Activity {

    private static SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat monthDateHyphenFormat = new SimpleDateFormat("MM-dd"); //TODO rename others as such
    private static SimpleDateFormat monthDateUnderscoreFormat = new SimpleDateFormat("MM_dd"); //TODO rename others as such
    private static SimpleDateFormat minuteTimestampFormat = new SimpleDateFormat("HH.mm");
    private static SimpleDateFormat secondTimestampFormat = new SimpleDateFormat("HH.mm.ss");
    private static SimpleDateFormat secondTimestampFormatDirectory = new SimpleDateFormat("HH_mm_ss");
    private static SimpleDateFormat milliTimestampFormat = new SimpleDateFormat("HH.mm.ss.SSS");
    private static SimpleDateFormat milliTimestampLogFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    private static ArrayList<Double> featuresMinMaxValuesDoubleArrayList = new ArrayList<>(); //delete


    public static String currentDateAsString() {
        return yearDateFormat.format(new Date());
    }

    public static String currentMinuteTimestampAsString() {
        return minuteTimestampFormat.format(new Date());
    }

    public static String currentMonthDateUnderscoreFormat() {
        return monthDateUnderscoreFormat.format(new Date());
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
        return "["+ yearDateFormat.format(date)+"]("+ milliTimestampLogFormat.format(date)+")";
    }

    public static double calcSD(double[] array) {
        double[] temp = new double[array.length];
        double mean = calcMean(array);

        for (int i = 0; i < array.length; i++) {
            temp[i] = Math.pow((array[i] - mean), 2.0);
        }

        double sd = Math.sqrt(calcMean(temp));

        //System.out.println("mean: "+ mean + "       sd: " + sd); //TODO Accuracy Tweak //mean, sd, and Principal component analysis

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

        int len=rawArray.length;
        double[] normArray = new double[len];

        double sd = Util.calcSD(rawArray);
        double mean = Util.calcMean(rawArray);

        for (int i = 0; i < len; i++) {
            normArray[i] = (rawArray[i] - mean) / sd;
        }

        return normArray;
    }

    public static double[][] normalizeFeaturesWithBound(@NonNull double[][] feat2DArray, File minMaxFeaturesTrainingDataLogFile){

        //COMMENT: passed code Audit inspection -- "Blame me if something goes Kaboom here" -Jason 2017

        //TODO SAVE min and max values!!!!! for real-time normalization evaluation

        //TODO convert to thread
        //TODO consider data processing once ALL trial data has  been collected

        int len = feat2DArray.length;

        //converted to more dynamic method, plus efficient
        double[] deltaArray=new double[len];
        double[] thetaArray=new double[len];
        double[] alphaArray=new double[len];
        double[] betaLowArray=new double[len];
        double[] betaMidArray=new double[len];
        double[] betaHighArray=new double[len];
        double[] gammaLowArray=new double[len];

/*
        double[] deltaArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] thetaArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] alphaArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaLowArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaMidArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaHighArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] gammaLowArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
*/
/*
        double[] deltaNormArray;
        double[] thetaNormArray;
        double[] alphaNormArray;
        double[] betaLowNormArray;
        double[] betaMidNormArray;
        double[] betaHighNormArray;
        double[] gammaLowNormArray;
*/

        double[] f;
        for (int i = 0; i < len; i++) {
            f = feat2DArray[i];
            deltaArray[i] = f[0];
            thetaArray[i] = f[1];
            alphaArray[i] = f[2];
            betaLowArray[i] = f[3];
            betaMidArray[i] = f[4];
            betaHighArray[i] = f[5];
            gammaLowArray[i] = f[6];
        }

        double[][] normalizedFeaturesWithZeroOneBound = new double[len][feat2DArray[0].length];
        double[] deltaNormArray = normalizeArrayWithBound(deltaArray,0,1);
        double[] thetaNormArray = normalizeArrayWithBound(thetaArray,0,1);
        double[] alphaNormArray = normalizeArrayWithBound(alphaArray,0,1);
        double[] betaLowNormArray = normalizeArrayWithBound(betaLowArray,0,1);
        double[] betaMidNormArray = normalizeArrayWithBound(betaMidArray,0,1);
        double[] betaHighNormArray = normalizeArrayWithBound(betaHighArray,0,1);
        double[] gammaLowNormArray = normalizeArrayWithBound(gammaLowArray,0,1); //TODO 2nd and 3rd parameters unnecessary

        //
        logfeaturesMinMaxValues(minMaxFeaturesTrainingDataLogFile); //TODO call logging here
        //

        for(int j = 0; j< GlobalSettings.calibrationNumOfTrialsToPerformTotal; j++) { //TODO changed here for normalization
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

    private static double[] normalizeArrayWithBound(double[] dArray, double lowBound, double highBound){

        double[] normDoubleArray = new double[dArray.length]; //TODO create Global Trials # variable independent of Alert/Fatigue

        double[] minMax = findMinAndMax(dArray);
        double min = minMax[0];
        double max = minMax[1];

        double maxMinusMin = max-min;

        for(int i = 0; i< dArray.length; i++){

            normDoubleArray[i]= ((dArray[i]-min)/maxMinusMin);
        }

        //Log min and max

        featuresMinMaxValuesDoubleArrayList.add(min);
        featuresMinMaxValuesDoubleArrayList.add(max);

        return normDoubleArray;
    }




    public static double[][] normalize2DArray (@NonNull double[][] feat2DArray){

        //COMMENT: passed code Audit inspection -- "Blame me if something goes Kaboom here" -Jason 2017

        //TODO SAVE min and max values!!!!! for real-time normalization evaluation

        //TODO convert to thread
        //TODO consider data processing once ALL trial data has  been collected

        int len = feat2DArray.length;
        int height = feat2DArray[0].length;

        double[][]transposed2DArray= new double[len][height];
        for(int i=0;i<height ;i++){
            for(int j=0;j<len ;j++){
                transposed2DArray[i][j]=feat2DArray[i][j];
            }
        }

        double[]transposedArray= new double[len];
        for(int i=0;i<height ;i++){
            for(int j=0;j<len ;j++){
                transposedArray[j]=feat2DArray[j][i];


                ///THEN WORK HERE

            }
        }


        //converted to more dynamic method, plus efficient
        double[] deltaArray=new double[len];
        double[] thetaArray=new double[len];
        double[] alphaArray=new double[len];
        double[] betaLowArray=new double[len];
        double[] betaMidArray=new double[len];
        double[] betaHighArray=new double[len];
        double[] gammaLowArray=new double[len];

/*
        double[] deltaArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] thetaArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] alphaArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaLowArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaMidArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] betaHighArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
        double[] gammaLowArray=new double[GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue];
*/
/*
        double[] deltaNormArray;
        double[] thetaNormArray;
        double[] alphaNormArray;
        double[] betaLowNormArray;
        double[] betaMidNormArray;
        double[] betaHighNormArray;
        double[] gammaLowNormArray;
*/

        double[] f;
        for (int i = 0; i < len; i++) {
            f = feat2DArray[i];
            deltaArray[i] = f[0];
            thetaArray[i] = f[1];
            alphaArray[i] = f[2];
            betaLowArray[i] = f[3];
            betaMidArray[i] = f[4];
            betaHighArray[i] = f[5];
            gammaLowArray[i] = f[6];
        }

        double[][] normalizedFeaturesWithZeroOneBound = new double[len][feat2DArray[0].length];
        double[] deltaNormArray = normalizeArrayWithBound(deltaArray,0,1);
        double[] thetaNormArray = normalizeArrayWithBound(thetaArray,0,1);
        double[] alphaNormArray = normalizeArrayWithBound(alphaArray,0,1);
        double[] betaLowNormArray = normalizeArrayWithBound(betaLowArray,0,1);
        double[] betaMidNormArray = normalizeArrayWithBound(betaMidArray,0,1);
        double[] betaHighNormArray = normalizeArrayWithBound(betaHighArray,0,1);
        double[] gammaLowNormArray = normalizeArrayWithBound(gammaLowArray,0,1); //TODO 2nd and 3rd parameters unnecessary

        //
        //logfeaturesMinMaxValues(); //TODO call logging here
        //

        for(int j = 0; j< GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue; j++) {
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


    //EVAL!!
    public static double[] normalizeArrayWithKnownMinMax(double[] dArray, double[] minBound, double[] maxBound){

        int len=dArray.length;
        double[] normDoubleArray = new double[len];
        for(int i = 0; i < len; i++){normDoubleArray[i]= ((dArray[i]-minBound[i])/maxBound[i]);}

        return normDoubleArray;
    }

    public static double[] groupMagnitudesByBandwidth(final double[] magAvgArray){

        //0-3 delta; 4-7 theta, 8-12 alpha, 13-16 beta.low;17-21 beta.mid; 22-30beta.high; 31-40 gamma.low

        double avgDelta = Util.calcMean(Arrays.copyOfRange(magAvgArray, 1, 3)); //set to drop 0 Hz
        double avgTheta = Util.calcMean(Arrays.copyOfRange(magAvgArray, 4, 7));
        double avgAlpha = Util.calcMean(Arrays.copyOfRange(magAvgArray, 8, 12));
        double avgBetaLow = Util.calcMean(Arrays.copyOfRange(magAvgArray, 13, 16));
        double avgBetaMid = Util.calcMean(Arrays.copyOfRange(magAvgArray, 17, 21));
        double avgBetaHigh = Util.calcMean(Arrays.copyOfRange(magAvgArray, 22, 30));
        double avgGammaLow = Util.calcMean(Arrays.copyOfRange(magAvgArray, 31, 40));

        //System.out.println("#groupMagnitudesByBandwidth#");

        //TODO: normalize after all trials are collected

        double[] preNormalizedFeatureSet = {avgDelta, avgTheta, avgAlpha, avgBetaLow, avgBetaMid, avgBetaHigh, avgGammaLow};

        return preNormalizedFeatureSet;
        //return new double[] {avgDelta, avgTheta, avgAlpha, avgBetaLow, avgBetaMid, avgBetaHigh, avgGammaLow};
    }

    public synchronized static double[] findMinAndMax (final double[] dArray){

        if(dArray.length==1) return new double[]{dArray[0], dArray[0]};

        double min;
        double max;
        min = dArray[0];
        max = dArray[0];

        for(int i=0;i<dArray.length;i++){
            if(dArray[i]<min){
                min = dArray[i];
            }
            else if(dArray[i]>max){
                max=dArray[i];
            }
        }

        // jsnieves:TEST OUTPUT //
        // System.out.println("dArray enumerated HERE");
        // for(double d : dArray) System.out.println(d);
        // System.out.println("minMax");
        // System.out.println(min + "       " + max);

        return new double[]{min,max};
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();

        return "mounted".equals(state);
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return "mounted".equals(state) || "mounted_ro".equals(state);
    }

    public static boolean logfeaturesMinMaxValues(File logfeaturesMinMaxValuesFile) {

        if (isExternalStorageWritable() && isExternalStorageReadable()){

            File featuresMinMaxLogFile =logfeaturesMinMaxValuesFile;

            try {
                StringBuilder stringBuilder= new StringBuilder();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(featuresMinMaxLogFile), 102400);

                stringBuilder.append(featuresMinMaxValuesDoubleArrayList.get(0)); //first line
                for (int i=1; i < featuresMinMaxValuesDoubleArrayList.size(); i++) {
                    stringBuilder.append("\r\n" + featuresMinMaxValuesDoubleArrayList.get(i));
                }

                bos.write(stringBuilder.toString().getBytes());
                bos.flush();

                stringBuilder.setLength(0);

            } catch (Exception ex) {
                //jsnieves:TODO:Handle this
                System.out.println("MinMax features storage FAILED");
                //FileNotFoundException
                ex.printStackTrace();
            }
            System.out.println("MinMax features storage SUCCESS?");
            return true;
        }
        return false;
    }



    public static double[][] normalizeEvalArrayWithMinMaxValues(double[] evalData, double[]minData, double[]maxData, double[]workSpace, double[][]returnData2D) {

        int evalLen = evalData.length;
        int returnLen = returnData2D.length;

        for (int i = 0; i < returnLen; i++) {
            for (int j = 0; j + 2 < evalLen; j++) {
                returnData2D[i][j] = maxData[j];
                returnData2D[i][j + 1] = evalData[j];
                returnData2D[i][j + 2] = minData[j];

            }
        }

        return returnData2D;
    }

    public static double[] normalizeEvalMinMaxCombined2DArray(double[][] dataMinEvalMax) {

        return null;

    }

    public static double[] getfeaturesMinMaxValues() {

        double[] featuresMinMaxValues = new double[GlobalSettings.numOfFeatures*2];

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,"file.txt");
        File featuresMinMaxLogFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile().toString() + File.separator + GlobalSettings.getAppRootFolderName() + "Users" + File.separator + GlobalSettings.userName + File.separator + "Evaluation" + File.separator, GlobalSettings.featuresMinMaxLogFileName);

        //Read text from file
        StringBuilder text = new StringBuilder();

        int count=0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(featuresMinMaxLogFile));
            String line;

            while ((line = br.readLine()) != null) {

                featuresMinMaxValues[count]=Double.valueOf(line);
                //text.append(line);
                //text.append('\n');
                count++;
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return featuresMinMaxValues;
    }

    // Recursively deletes all Child folders and their contents, then the Directory itself
    public static boolean deleteDir(File dir) {

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

    public static Complex[] makeComplexArray(final double[] rawNormArray){
        Complex[] complexArray=new Complex[rawNormArray.length];

        double imaginary = 0.0;

        for(int i=0 ; i< rawNormArray.length; i++){
            complexArray[i]=new Complex(rawNormArray[i], imaginary);
        }

        return complexArray;
    }

    public static double[] read_doublesFromFile()
    {

        return null;
    }




    public static void playSound(final Context context, final SoundType type)
    {

        new Thread(new Runnable()
        {

            @Override
            public void run()
            {
                MediaPlayer mediaPlayer = new MediaPlayer();
                int resId = -1;
                switch (type)
                {
                    case RECORDING_FINISHED:
                        resId=R.raw.recording_finished;
                        break;
                    case TRAINING_COMPLETE:
                        //resId=R.raw.;
                        break;
                    case BLANK:
                        //resId=R.raw.;
                        break;
                    default:
                        resId=-1;
                        break;
                }

                if (resId != -1)
                {
                    mediaPlayer = MediaPlayer.create(context, resId);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setLooping(false);
                    mediaPlayer.start();

                    while (mediaPlayer.isPlaying() == true)
                    {
                    }
                }
            }
        }).start();

    }
}
