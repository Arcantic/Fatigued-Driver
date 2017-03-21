package com.fatigue.driver.app;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 *
 * Created by jsnieves
 *
 */

public class SVMEvaluator {

    double total=0;
    double correct=0;
    boolean isAccuracyToBeCalculated=true;

    public double evaluate(double[] features, svm_model model)
    {
        svm_node[] nodes = new svm_node[features.length-1];
        for (int i = 1; i < features.length; i++)
        {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features[i];

            nodes[i-1] = node;
        }

        int totalClasses = 2;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model,labels);

        double[] prob_estimates = new double[totalClasses];
        double v = svm.svm_predict_probability(model, nodes, prob_estimates);

        for (int i = 0; i < totalClasses; i++){
            System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
        }

        if(isAccuracyToBeCalculated){
            if (features[0]==v){
                correct++;
            }
        }
        total++;
            System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")(Accuracy: " + correct/total +")");

        return v;
    }

}