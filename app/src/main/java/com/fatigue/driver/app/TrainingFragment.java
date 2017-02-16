package com.fatigue.driver.app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
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

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.Calendar;

/**
 * Created by Eric on 11/14/2016.
 */

public class TrainingFragment extends Fragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_training, container, false);


        //ID the "Start" button and add listener
        button_start = (Button)view.findViewById(R.id.button_start);
        button_start.setOnClickListener(this);

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
        eyes_closed = switch_eyes.isChecked();
        switch_eyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eyes_closed = isChecked;
                if(eyes_closed){
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




        // Inflate the layout for this fragment
        return view;
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
    boolean eyes_closed;
    public static Boolean running_test = false;

    int duration_default_open = 5;
    int duration_default_closed = 2;


    //Button listener
    @Override
    public void onClick(View v) {
        if(!running_test) {
            beginTest();
        }else {
            endTest(true);
            Toast.makeText(getActivity().getApplicationContext(), "Test Canceled", Toast.LENGTH_SHORT).show();
        }
    }


    public static CountDownTimer timer;
    int timer_length_default = 5;
    //Start the test
    public void beginTest(){
        //Test begins in 5 seconds...
        running_test = true;
        disableSettings();
        button_start.setText("Cancel");
        count_left.setText(count+"");

        training_status.setText("Training begins in");
        training_status_countdown.setText("5");

        //Begin updating the countdown textfield
        startTimer(timer_length_default);

        //Prevent screen sleep
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    //Timer for updating the textfield for countdown
    public boolean running_test_final;
    public void startTimer(int length){
        timer = new CountDownTimer(length*1000, 100) {
            //Every *100* millis, onTick is called.
            public void onTick(long millisUntilFinished) {
                training_status_countdown.setText(Math.ceil(millisUntilFinished / 100)/10 + "");
            }

            public void onFinish() {
                //running_test_final is a variable for determining if the test is running-
                //or if the countdown is only for the "test begins in..." phase
                if(!running_test_final)
                    running_test_final = true;
                else {
                    count--;
                    count_left.setText(count + "");
                }

                //If the # of trials remaining > 0, then start another timer and update the text fields
                if(count > 0){
                    if(eyes_closed) {
                        training_status.setText("Eyes Closed...");
                    }else{
                        training_status.setText("Eyes Open...");
                    }

                    startTimer(duration);
                }else{
                    //If the # of trials remaining = 0, then end the test
                    completeTest();
                }
            }
        }.start();
    }

    public void completeTest(){
        Toast.makeText(getActivity().getApplicationContext(), "Test Complete!", Toast.LENGTH_LONG).show();
        endTest(false);
    }

    public void endTest(boolean invalidate){
        //If Invalidaded, delete test
        //Should never be false...
        //Do something...

        running_test = false;
        running_test_final = false;
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
    public static void forceEndTest(){
        running_test = false;
        if(timer != null)
            timer.cancel();
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

}