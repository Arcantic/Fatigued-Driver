package com.fatigue.driver.app;

import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
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

/**
 * Created by Eric on 11/14/2016.
 */

public class TrainingFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training, container, false);


        //ID the "Start" button and add listener-
        button_start = (Button)view.findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!running_test) {
                    beginTest();
                }else {
                    endTest(true);
                    cancelGatherData();
                    Toast.makeText(getActivity().getApplicationContext(), "Training Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        });;


        //Display texts...
        training_status = (TextView)view.findViewById(R.id.text_training_status);
        training_status_countdown = (TextView)view.findViewById(R.id.text_training_status_countdown);
        count_left = (TextView)view.findViewById(R.id.text_count_left);




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





        //"toggles" and add listener
        switch_eyes = (Switch)view.findViewById(R.id.switch_eyes);
        switch_alternate = (Switch)view.findViewById(R.id.switch_alternate);
        switch_eyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user_eyes_closed_current = isChecked;
                if(user_eyes_closed_current){
                    duration_active = duration_closed;
                    switch_eyes.setText("Eyes are closed");
                }else{
                    duration_active = duration_open;
                    switch_eyes.setText("Eyes are open");
                }
            }
        });
        switch_alternate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    switch_eyes.setEnabled(false);
                    switch_eyes.setChecked(false);
                    duration_active = duration_open;
                    switch_eyes.setText("Eyes are open");
                    alternate_eyes = true;
                }else{
                    switch_eyes.setEnabled(true);
                    alternate_eyes = false;
                }
            }
        });
        user_eyes_closed_current = switch_eyes.isChecked();
        alternate_eyes = switch_alternate.isChecked();


        //initMindwaveHelper();


        // Inflate the layout for this fragment
        return view;
    }


    private static final String TAG = TestThree_MindwaveHelper.class.getSimpleName();
    public MindwaveHelperFragment mMindwaveHelperFrag;
    public void initMindwaveHelper(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        mMindwaveHelperFrag = new MindwaveHelperFragment();

        ft.add(mMindwaveHelperFrag,TAG);
        ft.commit();
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
    Switch switch_eyes, switch_alternate;
    TextView training_status;
    TextView count_left;
    TextView training_status_countdown;

    int duration_open, duration_closed, duration_active, duration_transition = 0;
    int count = 0;
    boolean user_eyes_closed_current, alternate_eyes;
    public static Boolean running_test = false;

    int duration_default_open = 5;
    int duration_default_closed = 2;





    public static CountDownTimer timer;
    int timer_length_default = 5;
    //Start the test
    public void beginTest(){
        //Test begins in 5 seconds...
        running_test = true;
        disableSettings();
        button_start.setText("Cancel");
        count_left.setText(count+"");

        training_status.setText(getCurrentCommandString() + " in...");
        training_status_countdown.setText("5");

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
                training_status_countdown.setText(Math.ceil(millisUntilFinished / 100)/10 + "");
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
                    user_eyes_closed_current = getNextCommand();
                    playCompletionSound();
                }

                //If you are not in a transition period...
                // If the # of trials remaining > 0...
                //...then start another timer and update the text fields
                if(!in_transition_period && count > 0){
                    startTimer(getDuration());

                    //Begin to gather data. Later, replace with resumeGatherData() and stopGatherTrialData()
                    if(user_eyes_closed_current) {
                        training_status.setText("Eyes Closed...");
                        startGatherTrialData(GlobalSettings.EYES_CLOSED);
                    }else{
                        training_status.setText("Eyes Open...");
                        startGatherTrialData(GlobalSettings.EYES_OPEN);
                    }

                }else{
                    if(count > 0){
                        //Enter transition/grace period
                        stopGatherTrialData();
                        training_status.setText(getCurrentCommandString() + " in...");
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
        if(alternate_eyes){
            if(user_eyes_closed_current)
                return duration_closed;
            else
                return duration_open;
        }else{
            return duration_active;
        }
    }

    public boolean getNextCommand(){
        //Needs to run code to determine what the next command should be....
        //right now, will return the base value, eyes_closed_current
        if(alternate_eyes)
            return !user_eyes_closed_current;
        else
            return user_eyes_closed_current;
    }
    public String getNextCommandString(){
        if(getNextCommand())
            return "Eyes Closed";
        else
            return "Eyes Open";
    }
    public String getCurrentCommandString(){
        if(user_eyes_closed_current)
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

        training_status.setText("Training Not Running");
        training_status_countdown.setText("");
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
        Toast.makeText(getActivity().getApplicationContext(), "Training Complete!", Toast.LENGTH_LONG).show();

        Fragment fragment = new ResultsFragment();
        ((ResultsFragment)fragment).setType(ResultsFragment.TYPE_TRAINING);

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
        switch_eyes.setEnabled(false);
        switch_alternate.setEnabled(false);
        edit_duration_transition.setEnabled(false);
        edit_duration_open.setEnabled(false);
        edit_duration_closed.setEnabled(false);
        edit_count.setEnabled(false);
    }

    public void enableSettings(){
        if(!switch_alternate.isChecked())
            switch_eyes.setEnabled(true);
        switch_alternate.setEnabled(true);
        edit_duration_transition.setEnabled(true);
        edit_duration_open.setEnabled(true);
        edit_duration_closed.setEnabled(true);
        edit_count.setEnabled(true);

        user_eyes_closed_current = switch_eyes.isChecked();
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
        //Run the training function and notify the user with a Toast
        runSvmTrain();
    }

    public boolean running_svm_train;
    public void runSvmTrain(){
        running_svm_train = true;
        Toast.makeText(getActivity().getApplicationContext(), "SVM is Training...", Toast.LENGTH_LONG).show();
        //HERE//
        //RUN TRAIN FUNCTIONS IN BACGROUND. WHEN IT IS FINISHED, OPEN THE RESULTS SCREEN
        //HERE//
        running_svm_train = false;
        openResultsPage();
    }

    public void cancelGatherData(){
        gathering_data = false;
        //Tell the adapter to stop gathering data AND delete the data it gathered (file and memory)
        //Call appropriate function
    }

}