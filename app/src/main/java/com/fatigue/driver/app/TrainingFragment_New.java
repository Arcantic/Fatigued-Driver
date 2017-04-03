package com.fatigue.driver.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

/**
 * Created by Eric on 3/27/2017.
 */

public class TrainingFragment_New extends Fragment {
    private static final String TAG = TrainingActivity_New.class.getSimpleName();
    Button btn_startPauseResume;

    TextView tv_timer;
    TextView tv_instruction;
    LinkDetectedHandler_New linkDH;

    public static boolean isTrialInProgress=false;
    boolean isStartOfTransitionPeriod = false;

    Handler hand = new Handler();
    TrainingFragment_New.TimerRunnable tRunnable;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training_new, container, false);

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

        initView(view);

        return view;
    }

    private void initView(View view) {
        hand = new Handler();

        btn_startPauseResume = (Button) view.findViewById(R.id.button_start);
        tv_timer = (TextView) view.findViewById(R.id.text_training_status_countdown);
        tv_instruction = (TextView) view.findViewById(R.id.text_training_status);

        initOtherViews(view);

        btn_startPauseResume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(linkDH != null) {
                    if (!isTrialInProgress && linkDH.isConnected) {
                        tv_timer.setText("0.0");
                        roundUpCount();

                        //Reset the logging locations at trial start.
                        linkDH.initLogCreate();

                        linkDH.fireTrialCollectorInitializer();
                        tRunnable = new TimerRunnable();
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

    public void cancelTest(){
        hand.removeCallbacks(tRunnable);
        hand = new Handler();
        resetTrainingCollector();

        isTrialInProgress = false;
        isStartOfTransitionPeriod = false;

        btn_startPauseResume.setText("Start");
        tv_instruction.setText("Training Not Running");
        tv_timer.setText("");
        count_left.setText("");
        enableSettings();
        Toast.makeText(getActivity().getApplicationContext(), "Training Canceled", Toast.LENGTH_SHORT).show();
    }

    public void finishTraining(){
        hand.removeCallbacks(tRunnable);
        hand = new Handler();

        isTrialInProgress = false;
        isStartOfTransitionPeriod = false;

        btn_startPauseResume.setText("Start");
        tv_instruction.setText("Training Not Running");
        tv_timer.setText("");
        count_left.setText("");
        enableSettings();
        Toast.makeText(getActivity().getApplicationContext(), "Training Complete!", Toast.LENGTH_LONG).show();

        launchResultsScreen();
    }

    public void launchResultsScreen(){

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

    EditText edit_duration_transition, edit_duration_open, edit_duration_closed;
    EditText edit_count;
    TextView count_left;

    public void initOtherViews(View view){
        count_left = (TextView)view.findViewById(R.id.text_count_left);

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

    public void resetTrainingCollector(){
        System.out.println("Stopping Trial Collection");
        linkDH.stopTrialCollector();
        linkDH.resetTrialCollector();
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

        double timeRemaining;
        DecimalFormat formatter = new DecimalFormat("#0.0");
        int counter = 0;

        public TimerRunnable() {

        }

        public void run() {
            timeRemaining = Double.parseDouble(tv_timer.getText().toString());
            timeRemaining -= 0.1;

            if (timeRemaining >= 0.0) {
                tv_timer.setText((String.valueOf(formatter.format(timeRemaining))));
            } else {
                isStartOfTransitionPeriod = !isStartOfTransitionPeriod; //flip

                if (isStartOfTransitionPeriod) {
                    if (counter < GlobalSettings.calibrationNumOfTrialsToPerformAlert) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.alertDelayTimeBetweenTrialCollections))));
                        tv_instruction.setText("Open eyes in...");

                        playCompletionSound();

                    } else if (counter == GlobalSettings.calibrationNumOfTrialsToPerformAlert) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.fatigueDelayTimeBetweenTrialCollections))));
                        tv_instruction.setText("Close eyes in...");

                        playCompletionSound();

                    } else if (counter < GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.fatigueDelayTimeBetweenTrialCollections))));
                        tv_instruction.setText("Close eyes in...");

                        playCompletionSound();

                    } else {
                        tv_instruction.setText("Finished");
                    }

                    count_left.setText(String.valueOf(GlobalSettings.calibrationNumOfTrialsToPerformTotal - counter));
                } else {

                    counter++;

                    if (counter <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {
                        tv_timer.setText((String.valueOf(formatter.format((double) GlobalSettings.alertTrialCollectionIntervalDuration))));
                        linkDH.fireTrialCollectorInstance(true);//eyesOpen collect
                        tv_instruction.setText("Open eyes");
                    } else if (counter <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                        tv_timer.setText((String.valueOf(formatter.format((double) GlobalSettings.fatigueTrialCollectionIntervalDuration))));
                        linkDH.fireTrialCollectorInstance(false);//eyesClosed
                        tv_instruction.setText("Close eyes");

                    } else {
                        tv_instruction.setText("Finished");
                    }
                }
            }

            if (counter <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2) {
                hand.postDelayed(this, 100);
            } else {
                finishTraining();
            }
        }
    }
}
