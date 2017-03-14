package com.fatigue.driver.app;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

public class ResultsFragment extends Fragment{


    public static int TYPE_TRAINING = 0, TYPE_EVALUATION = 1;
    public int type;
    public void setType(int type){
        this.type = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_results_page, container, false);

        //The fragment is open.
        is_open = true;

        text_title = (TextView)view.findViewById(R.id.text_test_complete);
        if(type == TYPE_TRAINING)
            text_title.setText("Training Complete");
        if(type == TYPE_EVALUATION)
            text_title.setText("Evaluation Complete");

        //Add listener to each button
        button_save = (Button)view.findViewById(R.id.button_save_results);
        button_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Save Results
                if(type == TYPE_TRAINING)
                    actionAlert(true, "Save Results", "Would you like to save the training results?");
                if(type == TYPE_EVALUATION)
                    actionAlert(true, "Save Results", "Would you like to save the evaluation results?");
            }
        });

        button_delete = (Button)view.findViewById(R.id.button_delete_results);
        button_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Delete Results
                if(type == TYPE_TRAINING)
                    actionAlert(false,"Delete Results", "This action will delete the training results. Continue?");
                if(type == TYPE_EVALUATION)
                    actionAlert(false,"Delete Results", "This action will delete the evaluation results. Continue?");
            }
        });

        // Inflate the layout for this fragment
        return view;
    }


    public Button button_save, button_delete;
    public TextView text_title;

    public static boolean is_open;
    public static boolean isOpen(){
        return is_open;
    }


    //The following functions connect to the Adapter classes
    //to gather data from headset
    public void saveResults(){
        //Tell adapter to save gathered data
        //Call appropriate function
    }

    public void deleteResults(){
        //Tell adapter to delete gathering data
        //Call appropriate function
    }


    public void removeResultsPage(String mTitle){
        is_open = false;
        Toast.makeText(getActivity().getApplicationContext(), mTitle, Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }

    public static void prepClose(){
        is_open = false;
    }


    public void actionAlert(final boolean save_results, String mTitle, String mMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(save_results){
                    saveResults();
                    removeResultsPage("Results Saved");
                }else{
                    deleteResults();
                    removeResultsPage("Results Deleted");
                }

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }


}