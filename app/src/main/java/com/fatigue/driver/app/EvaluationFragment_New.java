package com.fatigue.driver.app;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

/**
 * Created by Eric on 11/14/2016.
 */

public class EvaluationFragment_New extends Fragment {
    private static final String TAG = TrainingActivity_New.class.getSimpleName();
    Button btn_startPauseResume;

    TextView tv_timer;
    TextView tv_instruction;
    LinkDetectedHandler_New linkDH;

    public static boolean isTrialInProgress=false;
    boolean isStartOfTransitionPeriod = false;

    Handler hand = new Handler();
    EvaluationFragment_New.TimerRunnable tRunnable;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_evaluation_new, container, false);


        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        final MindwaveHelperFragment_New mMindwaveHelperFrag = new MindwaveHelperFragment_New();
        ft.add(mMindwaveHelperFrag, TAG);
        ft.commit();

        mMindwaveHelperFrag.setRecordingRawData(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    this.sleep((long) 1000);
                    linkDH = mMindwaveHelperFrag.getLinkDetectedHandler();
                    linkDH.showToast("TEST intercommunication", Toast.LENGTH_LONG);

                    //linkDH.setUsername(""); //TODO
                    //linkDH.initLogCreate();
                    //linkDH.initDrawWaveView();

                    linkDH.updateTrialCount();
                } catch (Exception e) {

                }
            }
        }.start();

        Toast.makeText(getActivity().getApplicationContext(), "Using " + GlobalSettings.svmModelFileName, Toast.LENGTH_SHORT).show();
        initView(view);

        //initMindwaveHelper();

        //initEval();

        // Inflate the layout for this fragment
        return view;
    }

    private void initView(View view) {
        hand = new Handler();

        btn_startPauseResume = (Button) view.findViewById(R.id.button_start);
        tv_timer = (TextView) view.findViewById(R.id.text_evaluation_status_countdown);
        tv_instruction = (TextView) view.findViewById(R.id.text_evaluation_status);

        initOtherViews(view);

        btn_startPauseResume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (linkDH != null) {
                    if (!isTrialInProgress && linkDH.isConnected) {
                        tv_timer.setText("0.0");
                        roundUpCount();

                        linkDH.fireEvalInitializer();
                        tRunnable = new EvaluationFragment_New.TimerRunnable();
                        isTrialInProgress = true;

                        hand.post(tRunnable);
                        btn_startPauseResume.setText("Stop");

                        disableSettings();
                    } else if (!isTrialInProgress && !linkDH.isConnected) {
                        Toast.makeText(getActivity().getApplicationContext(), "Not Connected", Toast.LENGTH_LONG).show();
                    } else {
                        cancelTest();
                    }
                }
            }
        });
    }

    //Round up the count to an even number
    public void roundUpCount(){
        String text = edit_count.getText().toString();
        int c = Integer.parseInt(text);
        if(c%2!=0) {
            c += 1;
            edit_count.setText(String.valueOf(c));
        }
        GlobalSettings.setTrialCount(c);
    }


    public void cancelTest(){
        hand.removeCallbacks(tRunnable);
        hand = new Handler();
        linkDH.stopEvalTesting();

        isTrialInProgress = false;
        isStartOfTransitionPeriod = false;
        command_count_fatigued = 0;
        command_count_alert = 0;

        btn_startPauseResume.setText("Start");
        tv_instruction.setText("Evaluation Not Running");
        tv_timer.setText("");
        count_left.setText("");
        evaluation_prev_command.setText("");
        evaluation_prev_classification.setText("");
        enableSettings();
        Toast.makeText(getActivity().getApplicationContext(), "Evaluation Canceled", Toast.LENGTH_SHORT).show();
    }


    public void finishEvaluation(){
        hand.removeCallbacks(tRunnable);
        hand = new Handler();
        linkDH.stopEvalTesting();

        isTrialInProgress = false;
        isStartOfTransitionPeriod = false;

        btn_startPauseResume.setText("Start");
        tv_instruction.setText("Evaluation Not Running");
        tv_timer.setText("");
        count_left.setText("");
        evaluation_prev_command.setText("");
        evaluation_prev_classification.setText("");
        enableSettings();
        Toast.makeText(getActivity().getApplicationContext(), "Evaluation Complete!", Toast.LENGTH_LONG).show();

        launchResultsScreen();
        command_count_fatigued = 0;
        command_count_alert = 0;
    }



    public void launchResultsScreen(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Evaluation Complete");
        builder.setMessage(command_count_alert*GlobalSettings.alertTrialCollectionIntervalDuration + " alert trials were performed."
                + System.lineSeparator() + command_count_fatigued*GlobalSettings.fatigueTrialCollectionIntervalDuration + " fatigue trials were performed."
                + System.lineSeparator() + "Overall accuracy: " + Math.round(SVMEvaluator.accuracy  * 100.0) / 100.0 + "%");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }


    EditText edit_duration_transition, edit_duration_open, edit_duration_closed;
    EditText edit_count;
    TextView count_left;
    TextView evaluation_prev_command, evaluation_prev_classification;

    public void initOtherViews(View view){
        //Display texts...
        count_left = (TextView)view.findViewById(R.id.text_count_left);
        evaluation_prev_command = (TextView)view.findViewById(R.id.text_evaluation_prev_command);
        evaluation_prev_classification = (TextView)view.findViewById(R.id.text_evaluation_prev_classification);



        //"durations" and add listener
        edit_duration_transition = (EditText)view.findViewById(R.id.edit_duration_transition);
        edit_duration_open = (EditText)view.findViewById(R.id.edit_duration_open);
        edit_duration_closed = (EditText)view.findViewById(R.id.edit_duration_closed);

        edit_duration_transition.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(!edit_duration_transition.getText().toString().equals("")) {
                    String text = edit_duration_transition.getText().toString();
                    int c = Integer.parseInt(text);
                    GlobalSettings.setTransitionDuration(c);
                    linkDH.updateTrialCount();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        edit_duration_open.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(!edit_duration_open.getText().toString().equals("")) {
                    String text = edit_duration_open.getText().toString();
                    int c = Integer.parseInt(text);
                    GlobalSettings.setAlertDuration(c);
                    linkDH.updateTrialCount();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        edit_duration_closed.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(!edit_duration_closed.getText().toString().equals("")) {
                    String text = edit_duration_closed.getText().toString();
                    int c = Integer.parseInt(text);
                    GlobalSettings.setFatigueDuration(c);
                    linkDH.updateTrialCount();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });


        //"count" and add listener
        edit_count = (EditText)view.findViewById(R.id.edit_count);
        edit_count.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if(!edit_count.getText().toString().equals("")) {
                    String text = edit_count.getText().toString();
                    int c = Integer.parseInt(text);
                    GlobalSettings.setTrialCount(c);
                    linkDH.updateTrialCount();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });




        //Init those changes...
        String text = edit_duration_transition.getText().toString();
        int c = Integer.parseInt(text);
        GlobalSettings.setTransitionDuration(c);

        text = edit_duration_open.getText().toString();
        c = Integer.parseInt(text);
        GlobalSettings.setAlertDuration(c);

        text = edit_duration_closed.getText().toString();
        c = Integer.parseInt(text);
        GlobalSettings.setFatigueDuration(c);

        text = edit_count.getText().toString();
        c = Integer.parseInt(text);
        GlobalSettings.setTrialCount(c);

        tv_timer.setText("");
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
            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class TimerRunnable implements Runnable {

        double timeRemaining, timeInitial;
        DecimalFormat formatter = new DecimalFormat("#0.0");
        int counter = 0;

        public TimerRunnable() {

        }

        public void run() {
            timeRemaining = Double.parseDouble(tv_timer.getText().toString());
            timeRemaining -= 0.1;

            if (timeRemaining > 0.0) {
                tv_timer.setText((String.valueOf(formatter.format(timeRemaining))));
                if(counter > 0 && timeRemaining < timeInitial-1.0) {
                    evaluation_prev_command.setText("Last Command: " + getLastCommandActive());
                    if (SVMEvaluator.lastCommandCorrect)
                        evaluation_prev_classification.setText("Last Classification: " + getLastCommandActive());
                    else
                        evaluation_prev_classification.setText("Last Classification: " + getLastCommandActiveOpposite());
                }
            } else {
                isStartOfTransitionPeriod = !isStartOfTransitionPeriod; //flip

                if (isStartOfTransitionPeriod) {
                    //Stop evaluation during transition period
                    linkDH.stopEvalTesting();

                    if (counter+1 <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                        //Generate the next command (random)
                        generateNextCommand();
                    }

                    if (counter < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.alertDelayTimeBetweenTrialCollections))));
                        tv_instruction.setText(getNextCommandPrep());
                        playCompletionSound();
                    } else {
                        tv_instruction.setText("Finished");
                    }
                    count_left.setText(String.valueOf(GlobalSettings.calibrationNumOfTrialsToPerformTotal - counter));
                } else {
                    counter++;

                    prev_command = next_command;
                    if (counter <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                        tv_timer.setText((String.valueOf(formatter.format((double) getNextCommandDuration()))));
                        timeInitial = Double.parseDouble(tv_timer.getText().toString());
                        linkDH.fireEvalTestingInstance(getNextCommandBoolean());
                        tv_instruction.setText(getNextCommandActive());
                    } else {
                        tv_instruction.setText("Finished");
                    }
                }
            }

            if (counter <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                hand.postDelayed(this, 100);
            } else {
                finishEvaluation();
            }
        }
    }




    public boolean next_command;
    public boolean BOOLEAN_EYES_CLOSED = false, BOOLEAN_EYES_OPEN = true;
    public int command_count_alert = 0, command_count_fatigued = 0;
    public void generateNextCommand(){
        int r = new Random().nextInt(2);
        if(r==0 && command_count_fatigued < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {
            next_command = BOOLEAN_EYES_CLOSED;
            command_count_fatigued++;
        } else if(command_count_alert < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {
            next_command = BOOLEAN_EYES_OPEN;
            command_count_alert++;
        } else {
            next_command = BOOLEAN_EYES_CLOSED;
            command_count_fatigued++;
        }
    }
    public boolean getNextCommandBoolean(){
        return next_command;
    }
    public String getNextCommandPrep(){
        if(next_command == BOOLEAN_EYES_OPEN)
            return "Open eyes in...";
        else return "Close eyes in...";
    }
    public String getNextCommandActive(){
        if(next_command == BOOLEAN_EYES_OPEN)
            return "Open eyes";
        else return "Close eyes";
    }
    public int getNextCommandDuration(){
        if(next_command == BOOLEAN_EYES_OPEN)
            return GlobalSettings.alertTrialCollectionIntervalDuration;
        else return GlobalSettings.fatigueTrialCollectionIntervalDuration;
    }


    public boolean prev_command;
    public String getLastCommandActive(){
        if(prev_command == BOOLEAN_EYES_OPEN)
            return "Open eyes";
        else return "Close eyes";
    }
    public String getLastCommandActiveOpposite(){
        if(prev_command == BOOLEAN_EYES_OPEN)
            return "Close eyes";
        else return "Open eyes";
    }




    public void openResultsPage(){
        Toast.makeText(getActivity().getApplicationContext(), "Evaluation Complete!", Toast.LENGTH_LONG).show();

        Fragment fragment = new ResultsFragment();
        ((ResultsFragment)fragment).setType(ResultsFragment.TYPE_EVALUATION);
        //((ResultsFragment)fragment).setClassificationList(is_classification_correct_list);

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


}