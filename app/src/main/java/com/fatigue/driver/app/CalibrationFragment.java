package com.fatigue.driver.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Eric on 11/14/2016.
 */

public class CalibrationFragment extends Fragment{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);

        //Add listener to each button
        button_gather_training_data = (Button)view.findViewById(R.id.button_gather_training_data);
        button_gather_training_data.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //openFragment(TrainingFragment.class);
                openActivity(TrainingActivity.class);
            }
        });

        button_evaluation = (Button)view.findViewById(R.id.button_evaluation);
        button_evaluation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //openFragment(TrainingFragment.class);
                openActivity(EvaluationActivity.class);
            }
        });

        button_select_model = (Button)view.findViewById(R.id.button_select_model);
        button_select_model.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //openFragment(TrainingFragment.class);
                openActivity(ModelSelectActivity.class);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    Button button_gather_training_data, button_evaluation, button_select_model;


    public void openFragment(Class fragmentClass){
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            transaction.addToBackStack(null);
            MainActivity.list_title_stack.add("Calibration");
            transaction.commit();
            getActivity().setTitle("Calibration");
            //((MainActivity)getActivity()).drawDrawerBackButton();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void openActivity(Class actClass){
        Intent intent = new Intent(getActivity(), actClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



}