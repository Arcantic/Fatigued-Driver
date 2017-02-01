package com.fatigue.driver.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

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




        //ID the "duration" edittext and add listener
        edit_duration = (EditText)view.findViewById(R.id.edit_duration);

        String text = edit_duration.getText().toString();
        if(!text.matches(""))
            duration = Integer.parseInt(text);
        else duration = 0;

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





        //ID the "count" edittext and add listener
        edit_count = (EditText)view.findViewById(R.id.edit_count);

        text = edit_count.getText().toString();
        if(!text.matches(""))
            count = Integer.parseInt(text);
        else count = 0;

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





        //ID the "eye toggle" edittext and add listener
        switch_eyes = (Switch)view.findViewById(R.id.switch_eyes);
        eyes_closed = switch_eyes.isChecked();
        switch_eyes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eyes_closed = isChecked;
            }
        });




        // Inflate the layout for this fragment
        return view;
    }

    Button button_start;
    EditText edit_duration;
    EditText edit_count;
    Switch switch_eyes;

    int duration = 0;
    int count = 0;
    boolean eyes_closed;
    Boolean running_test = false;


    //Button listener
    @Override
    public void onClick(View v) {
        if(!running_test) {
            beginTest();
            button_start.setText("Cancel");
        }else {
            endTest(true);
            button_start.setText("Start");
        }
    }


    public void beginTest(){
        //Start the test
        //Do something...
        running_test = true;
    }

    public void endTest(boolean invalidate){
        //If Invalidaded, delete test
        //Should never be false...
        //Do something...
        running_test = false;
    }

}