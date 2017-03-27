package com.fatigue.driver.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class TrainingActivity_New extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_test_generic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Record Training Data");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new TrainingFragment_New();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }


    public boolean isTrialInProgress(){
        if(TrainingFragment_New.isTrialInProgress)
            return true;
        else if(ResultsFragment.isOpen())
            return true;
        return false;
    }

    public void cancelTest(){
        if(TrainingFragment_New.isTrialInProgress){
            ((TrainingFragment_New)getSupportFragmentManager().findFragmentById(R.id.content_frame)).cancelTest();
            //Toast and allow screen sleep
            Toast.makeText(getApplicationContext(), "Training Canceled", Toast.LENGTH_LONG).show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else if(ResultsFragment.isOpen()){
            ResultsFragment.prepClose();
            //Toast and allow screen sleep
            Toast.makeText(getApplicationContext(), "Results Deleted", Toast.LENGTH_LONG).show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (isTrialInProgress()) {
            if (TrainingFragment_New.isTrialInProgress)
                testIsRunningAlert(true, null, "Training Running", "This action will cancel the training. Continue?");
            else if (ResultsFragment.isOpen())
                testIsRunningAlert(true, null, "Delete Results", "This action will delete the training results. Continue?");
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    if (ResultsFragment.isOpen())
                        testIsRunningAlert(true, null, "Delete Results", "This action will delete the training results. Continue?");
                }else{
                    if (TrainingFragment_New.isTrialInProgress)
                        testIsRunningAlert(true, null, "Training Running", "This action will cancel the training. Continue?");
                    else NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    //Warn user that test will be canceled.
    public void testIsRunningAlert(final boolean backPressed, final MenuItem menuItem, String mTitle, String mMessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                cancelTest();
                //Force back-press
                if(backPressed)
                    onBackPressed();
                else{
                    FragmentManager fm = getSupportFragmentManager();
                    fm.popBackStack();
                    onOptionsItemSelected(menuItem);
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
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                }
                return true;
            }
        });
        dialog.show();
    }
}