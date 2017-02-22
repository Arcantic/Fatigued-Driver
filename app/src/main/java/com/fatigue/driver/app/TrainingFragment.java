package com.fatigue.driver.app;

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

/**
 * Created by Eric on 11/14/2016.
 */

public class TrainingFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training, container, false);


        //ID the "Start" button and add listener
        button_start = (Button)view.findViewById(R.id.button_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!running_test) {
                    beginTest();
                }else {
                    endTest(true);
                    cancelGatherData();
                    Toast.makeText(getActivity().getApplicationContext(), "Test Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        });;


        //ID the display texts...
        training_status = (TextView)view.findViewById(R.id.text_training_status);
        training_status_countdown = (TextView)view.findViewById(R.id.text_training_status_countdown);
        count_left = (TextView)view.findViewById(R.id.text_count_left);




        //ID the "duration" and add listener
        edit_duration = (EditText)view.findViewById(R.id.edit_duration);
        getDuration();
        edit_duration.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String text = edit_duration.getText().toString();
                if(!text.matches(""))
                    duration = Integer.parseInt(text);
                else duration = 0;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });




        //ID the "count" and add listener
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





        //ID the "eye toggle" and add listener
        switch_eyes = (Switch)view.findViewById(R.id.switch_eyes);
        user_eyes_closed_current = user_eyes_closed_next = switch_eyes.isChecked();
        switch_eyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                user_eyes_closed_current = user_eyes_closed_next = isChecked;
                if(user_eyes_closed_current){
                    edit_duration.setText(duration_default_closed+"");
                    duration = duration_default_closed;
                    switch_eyes.setText("Eyes are closed");
                }else{
                    edit_duration.setText(duration_default_open+"");
                    duration = duration_default_open;
                    switch_eyes.setText("Eyes are open");
                }
            }
        });


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





    public void getDuration(){
        String text = edit_duration.getText().toString();
        if(!text.matches(""))
            duration = Integer.parseInt(text);
        else duration = 0;
    }

    public void getCount(){
        String text = edit_count.getText().toString();
        if(!text.matches(""))
            count = Integer.parseInt(text);
        else count = 0;
    }

    Button button_start;
    EditText edit_duration;
    EditText edit_count;
    Switch switch_eyes;
    TextView training_status;
    TextView count_left;
    TextView training_status_countdown;

    int duration = 0;
    int count = 0;
    boolean user_eyes_closed_current, user_eyes_closed_next;
    public static Boolean running_test = false;

    int duration_default_open = 5;
    int duration_default_closed = 2;



    public static CountDownTimer timer;
    int timer_length_default = 5;
    int timer_length_grace_period = 5;
    //Start the test
    public void beginTest(){
        //Test begins in 5 seconds...
        running_test = true;
        disableSettings();
        button_start.setText("Cancel");
        count_left.setText(count+"");

        training_status.setText(getNextCommandString() + " in...");
        training_status_countdown.setText("5");

        //Begin updating the countdown textfield
        startTimer(timer_length_default);

        //Prevent screen sleep
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //Timer for updating the textfield for countdown
    public boolean in_transition_period = true;
    public void startTimer(int length){
        timer = new CountDownTimer(length*1000, 100) {
            //Every *100* millis, onTick is called.
            public void onTick(long millisUntilFinished) {
                training_status_countdown.setText(Math.ceil(millisUntilFinished / 100)/10 + "");
            }

            public void onFinish() {
                //in_transition_period is a variable for determining if the test is running-
                //or if the countdown is only for the "test begins in..." phase
                if(in_transition_period)
                    in_transition_period = false;
                else {
                    count--;
                    count_left.setText(count + "");
                    user_eyes_closed_current = user_eyes_closed_next;
                    user_eyes_closed_next = getNextCommand();
                    in_transition_period = true;
                }

                //If you are not in a grace period...
                // If the # of trials remaining > 0...
                //...then start another timer and update the text fields
                if(!in_transition_period && count > 0){
                    startTimer(duration);

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
                        training_status.setText(getNextCommandString() + " in...");
                        startTimer(timer_length_grace_period);
                    }else{
                        //If the # of trials remaining = 0, then end the test
                        completeTest();
                    }
                }
            }
        }.start();
    }

    public boolean getNextCommand(){
        //Needs to run code to determine what the next command should be....
        //right now, will return the base value, eyes_closed_current
        return user_eyes_closed_current;
    }
    public String getNextCommandString(){
        if(user_eyes_closed_next)
            return "Eyes Closed";
        else if(!user_eyes_closed_next)
            return "Eyes Open";
        return  "NULL";
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

        training_status.setText("No Test Running");
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
        cancelGatherData();
    }



    public void openResultsPage(){
        Toast.makeText(getActivity().getApplicationContext(), "Test Complete!", Toast.LENGTH_LONG).show();

        Fragment fragment = new ResultsFragment();

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        MainActivity.list_title_stack.add("Calibration");
        transaction.commit();
        getActivity().setTitle("Calibration");
        //((MainActivity)getActivity()).drawDrawerBackButton();
    }




    public void disableSettings(){
        switch_eyes.setEnabled(false);
        edit_duration.setEnabled(false);
        edit_count.setEnabled(false);
    }

    public void enableSettings(){
        switch_eyes.setEnabled(true);
        edit_duration.setEnabled(true);
        edit_count.setEnabled(true);
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