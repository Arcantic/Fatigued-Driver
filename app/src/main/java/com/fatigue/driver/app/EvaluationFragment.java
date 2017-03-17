package com.fatigue.driver.app;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * Created by Eric on 11/14/2016.
 */

public class EvaluationFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_evaluation, container, false);


        //ID the "Start" button and add listener-
        button_start = (Button)view.findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!running_test) {
                    beginTest();
                }else {
                    endTest(true);
                    cancelGatherData();
                    Toast.makeText(getActivity().getApplicationContext(), "Evaluation Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        });;


        //Display texts...
        evaluation_status = (TextView)view.findViewById(R.id.text_evaluation_status);
        evaluation_status_countdown = (TextView)view.findViewById(R.id.text_evaluation_status_countdown);
        count_left = (TextView)view.findViewById(R.id.text_count_left);
        evaluation_prev_command = (TextView)view.findViewById(R.id.text_evaluation_prev_command);
        evaluation_prev_classification = (TextView)view.findViewById(R.id.text_evaluation_prev_classification);




        //"durations" and add listener
        edit_duration_transition = (EditText)view.findViewById(R.id.edit_duration_transition);
        edit_duration_open = (EditText)view.findViewById(R.id.edit_duration_open);
        edit_duration_closed = (EditText)view.findViewById(R.id.edit_duration_closed);
        edit_duration_transition.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String text = edit_duration_transition.getText().toString();
                if(!text.matches(""))
                    duration_transition = Integer.parseInt(text);
                else duration_transition = 0;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        edit_duration_open.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String text = edit_duration_open.getText().toString();
                if(!text.matches("")) {
                    duration_open = Integer.parseInt(text);
                    duration_active = duration_open;
                }
                else duration_open = 0;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        edit_duration_closed.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String text = edit_duration_closed.getText().toString();
                if(!text.matches(""))
                    duration_closed = Integer.parseInt(text);
                else duration_closed = 0;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        duration_transition = Integer.parseInt(edit_duration_transition.getText().toString());
        duration_open = Integer.parseInt(edit_duration_open.getText().toString());
        duration_closed = Integer.parseInt(edit_duration_closed.getText().toString());




        //"count" and add listener
        edit_count = (EditText)view.findViewById(R.id.edit_count);
        getCount();
        edit_count.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String text = edit_count.getText().toString();
                if(!text.matches(""))
                    count = Integer.parseInt(text);
                else count = 0;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });



        //initMindwaveHelper();

        initEval();

        // Inflate the layout for this fragment
        return view;
    }



    public void getCount(){
        String text = edit_count.getText().toString();
        if(!text.matches(""))
            count = Integer.parseInt(text);
        else count = 0;
    }

    Button button_start;
    EditText edit_duration_transition, edit_duration_open, edit_duration_closed;
    EditText edit_count;
    TextView count_left;
    TextView evaluation_status;
    TextView evaluation_status_countdown;
    TextView evaluation_prev_command, evaluation_prev_classification;

    int duration_open, duration_closed, duration_active, duration_transition = 0;
    int count = 0;
    boolean user_eyes_closed_current;
    public static Boolean running_test = false;

    int duration_default_open = 5;
    int duration_default_closed = 2;





    public static CountDownTimer timer;
    int timer_length_default = 5;
    //Start the test
    public void beginTest(){
        //Test begins in 5 seconds...
        clearClassList();

        running_test = true;
        disableSettings();
        button_start.setText("Cancel");
        count_left.setText(count+"");

        generateNextCommand();
        evaluation_status.setText(getCurrentCommandString() + " in...");
        evaluation_status_countdown.setText("5");

        //Begin updating the countdown textfield
        startTimer(duration_transition);

        //Prevent screen sleep
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //Timer for updating the textfield for countdown
    public boolean in_transition_period = true;
    public void startTimer(int length){
        timer = new CountDownTimer(length*1000, 50) {
            //Every *100* millis, onTick is called.
            public void onTick(long millisUntilFinished) {
                evaluation_status_countdown.setText(Math.ceil(millisUntilFinished / 100)/10 + "");

                //In addition to gathering data, we need to evaluate every second...
            }

            public void onFinish() {
                //in_transition_period is a variable for determining if the test is running-
                //or if the countdown is only for the "test begins in..." phase
                if(in_transition_period) {
                    in_transition_period = false;
                }else {
                    count--;
                    count_left.setText(count + "");
                    in_transition_period = true;

                    //evaluation_prev_command.setText("Last Command: " + getCurrentCommandString());
                    //evaluation_prev_classification.setText("Last Classification: " + "N/A");

                    generateNextCommand();
                    user_eyes_closed_current = getCurrentCommand();
                    playCompletionSound();

                }

                //If you are not in a transition period...
                // If the # of trials remaining > 0...
                //...then start another timer and update the text fields
                if(!in_transition_period && count > 0){
                    startTimer(getDuration());
                    startEvaluationTimer();

                    //Begin to gather data. Later, replace with resumeGatherData() and stopGatherTrialData()
                    if(user_eyes_closed_current) {
                        evaluation_status.setText("Eyes Closed...");
                        startGatherTrialData(GlobalSettings.EYES_CLOSED);
                    }else{
                        evaluation_status.setText("Eyes Open...");
                        startGatherTrialData(GlobalSettings.EYES_OPEN);
                    }

                }else{
                    if(count > 0){
                        //Enter transition/grace period
                        stopGatherTrialData();
                        evaluation_status.setText(getCurrentCommandString() + " in...");
                        startTimer(duration_transition);
                    }else{
                        //If the # of trials remaining = 0, then end the test
                        completeTest();
                    }
                }
            }
        }.start();
    }

    public int getDuration(){
        if(getCurrentCommand() == BOOLEAN_EYES_CLOSED){
            return duration_closed;
        }else{
            return duration_open;
        }
    }

    public boolean BOOLEAN_EYES_OPEN = false;
    public boolean BOOLEAN_EYES_CLOSED = true;
    public int INT_EYES_OPEN = 0;
    public int INT_EYES_CLOSED = 1;

    public void generateNextCommand(){
        int r = new Random().nextInt(2);
        if(r==0)
            user_eyes_closed_current =  BOOLEAN_EYES_CLOSED;
        else
            user_eyes_closed_current =  BOOLEAN_EYES_OPEN;
    }
    public boolean getCurrentCommand(){
        return user_eyes_closed_current;
    }
    public String getCurrentCommandString(){
        if(getCurrentCommand() == BOOLEAN_EYES_CLOSED)
            return "Eyes Closed";
        else
            return "Eyes Open";
    }

    public void completeTest(){
        //Stop gathering data
        stopGatherTrialData();
        endTest(false);
        finishGatherData();
    }

    public void endTest(boolean invalidate){
        //If Invalidaded, delete test
        //Should never be false...
        //Do something...

        running_test = false;
        in_transition_period = true;
        button_start.setText("Start");

        if(timer != null)
            timer.cancel();

        evaluation_status.setText("Evaluation Not Running");
        evaluation_status_countdown.setText("");
        evaluation_prev_command.setText("");
        evaluation_prev_classification.setText("");
        getCount();
        count_left.setText("");

        enableSettings();

        //Allow screen sleep
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //Call this when forcing the test to end from another class
    public void forceEndTest(){
        running_test = false;
        in_transition_period = true;
        if(timer != null)
            timer.cancel();
        else System.out.println("TIMER REFUSED TO STOP");
        cancelGatherData();
    }



    public void openResultsPage(){
        Toast.makeText(getActivity().getApplicationContext(), "Evaluation Complete!", Toast.LENGTH_LONG).show();

        Fragment fragment = new ResultsFragment();
        ((ResultsFragment)fragment).setType(ResultsFragment.TYPE_EVALUATION);
        ((ResultsFragment)fragment).setClassificationList(is_classification_correct_list);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        //MainActivity.list_title_stack.add("Calibration");
        transaction.commit();
        getActivity().setTitle("Calibration");
        //((MainActivity)getActivity()).drawDrawerBackButton();
    }




    public void disableSettings(){
        edit_duration_transition.setEnabled(false);
        edit_duration_open.setEnabled(false);
        edit_duration_closed.setEnabled(false);
        edit_count.setEnabled(false);
    }

    public void enableSettings(){
        edit_duration_transition.setEnabled(true);
        edit_duration_open.setEnabled(true);
        edit_duration_closed.setEnabled(true);
        edit_count.setEnabled(true);
    }



    public void playCompletionSound(){
        try {
            //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            //Ringtone r = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            //r.play();
            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public svm_model training_model;
    public ArrayList<Boolean> is_classification_correct_list = new ArrayList<Boolean>();

    //Initiate everything for evaluation...
    public void initEval(){
        loadTrainingModel();
        clearClassList();
    }

    public void clearClassList(){
        is_classification_correct_list = new ArrayList();
    }

    //Load the training model to memory
    public void loadTrainingModel(){
        //TODO NEED TO CORRECT FILE NAME/LOCATION
        try {
            FileReader fIn = new FileReader(GlobalSettings.svmTraingDataLogFileName);
            BufferedReader bufferedReader = new BufferedReader(fIn);
            svm_model model = svm.svm_load_model(bufferedReader);
            training_model = model;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity().getApplicationContext(), "Failed to load model file. Have you set up training data?", Toast.LENGTH_LONG).show();
        }
    }

    //This timer runs the evaluation every second.
    public void startEvaluationTimer(){
        timer = new CountDownTimer(1000, 50) {
            //Every *100* millis, onTick is called.
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                //Run evaluation on finish.
                //Need to load our features first
                double[] data_features = loadFeatures();

                double eval_results = 0;
                //TODO eval_results = evaluate(data_features, training_model);

                String res_actual, res_class;
                res_actual = getCurrentCommandString();

                if(eval_results == INT_EYES_OPEN)
                    res_class = "Eyes Open";
                else if (eval_results == INT_EYES_CLOSED)
                    res_class = "Eyes Closed";
                else res_class = "UNKNOWN";

                System.out.println("Actual Results: " + res_actual + ", Eval Classification: " + res_class);

                //Update the screen with the results...
                evaluation_prev_command.setText("Last Command: " + res_actual);
                evaluation_prev_classification.setText("Last Classification: " + res_class);

                //Record the results, so that classification accuracy can be calculated in the end.
                if(eval_results == INT_EYES_CLOSED && user_eyes_closed_current) {
                    //If the results say eyes are closed, and they are, then it is correct
                    is_classification_correct_list.add(true);
                }else if(eval_results == INT_EYES_OPEN && !user_eyes_closed_current) {
                    //If the results say eyes are open, and they are, then it is correct
                    is_classification_correct_list.add(true);
                }else is_classification_correct_list.add(false);
            }
        }.start();
    }

    //Load our data's features (after preprocessing!)
    //Equivalent to the raw data after it has been normalized, etc.
    public double[] loadFeatures(){
        double[] data_features = {0,0,0,0,0};
        //TODO Load feature data into data_features...
        return data_features;
    }


    //Need to provide evaluation with the features and the model (pre-loaded)
    //This code was copy-pasted from online... aka incorrect
    //TODO write this code correctly
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
        System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");

        return v;
    }




    public boolean gathering_data;
    //The following functions connect to the Adapter classes
    //to gather data from headset
    public void startGatherTrialData(int eye_status){
        gathering_data = true;
        //Tell adapter to begin recording data
        //Call appropriate function
        //Need to provide 1/0 eyes open/closed
        //mMindwaveHelperFrag.startRecordingTrialData(eye_status);
    }

    public void stopGatherTrialData(){
        gathering_data = false;
        //Tell adapter to stop gathering data
        //Call appropriate function
        //mMindwaveHelperFrag.stopRecordingTrialData();
    }

    public void finishGatherData(){
        //Placeholder for now...
        //Run the evaluation function and notify the user with a Toast
        runSvmEval();
    }

    public boolean running_svm_eval;
    public void runSvmEval(){
        running_svm_eval = true;
        //HERE//
        //RUN EVAL FUNCTIONS IN BACKGROUND. WHEN IT IS FINISHED, OPEN THE RESULTS SCREEN
        //HERE//
        running_svm_eval = false;
        openResultsPage();
    }

    public void cancelGatherData(){
        gathering_data = false;
        //Tell the adapter to stop gathering data AND delete the data it gathered (file and memory)
        //Call appropriate function
    }

}