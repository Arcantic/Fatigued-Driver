package com.fatigue.driver.app;

import android.graphics.YuvImage;

import java.util.logging.XMLFormatter;

/**
 * Created by Caleb Moretz on 11/15/2016.
 */

public class Magnitude {

    public static double[] mag(Complex[] complex_array) {
        /**
         * Compute the magnitude of this complex number.
         *
         * double re is the real part
         *double I'm is the imaginary part
         *
         */


        double mag[] = new double[complex_array.length];

        int i = 0;
        for(i=0;i<complex_array.length;i++){
            mag[i] = Math.sqrt(Math.pow(complex_array[i].re(), 2) + Math.pow(complex_array[i].im(), 2));
        }

        return mag;
    }
}