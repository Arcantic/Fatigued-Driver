package com.fatigue.driver.app;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static String currentDateAsString() {
        return dateFormat.format(new Date());
    }



    public static double calcSD (double[] array){

        double[] temp= new double[array.length];
        double mean = calcMean(array);

        for(int i=0; i<array.length; i++){

            temp[i]= Math.pow((array[i]- mean),2.0);
        }

        double sd = Math.sqrt(calcMean(temp));

        return sd;
    }


    public static double calcMean(double[] array){

        double total=0.0;
        for(double num : array){
            total+= num;
        }

        return total / array.length;
    }


    public double calcSum(double[] array){
        double sum=0.0;

        for(double num : array){
            sum+= num;
        }
        return sum;
    }



}