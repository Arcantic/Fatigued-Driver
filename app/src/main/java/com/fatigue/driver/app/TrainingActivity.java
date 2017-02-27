package com.fatigue.driver.app;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by Eric on 2/26/2017.
 */

public class TrainingActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_generic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Record Training Data");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadFragment();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (isTestRunning()) {
            if (TrainingFragment.running_test)
                testIsRunningAlert(true, null, "Test Running", "This action will cancel the test. Continue?");
            else if (ResultsFragment.isOpen())
                testIsRunningAlert(true, null, "Delete Results", "This action will delete the test results. Continue?");
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }else {
            super.onBackPressed();
        }
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

    public boolean isTestRunning(){
        if(TrainingFragment.running_test)
            return true;
        else if(ResultsFragment.isOpen())
            return true;
        return false;
    }

    public void cancelTest(){
        if(TrainingFragment.running_test){
            ((TrainingFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame)).forceEndTest();
            //Toast and allow screen sleep
            Toast.makeText(getApplicationContext(), "Test Canceled", Toast.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    if (ResultsFragment.isOpen())
                        testIsRunningAlert(true, null, "Delete Results", "This action will delete the test results. Continue?");
                }else{
                    if (TrainingFragment.running_test)
                        testIsRunningAlert(true, null, "Test Running", "This action will cancel the test. Continue?");
                    else NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Load the user select fragment...
    public void loadFragment(){
        //Replace the content with the UserSelect Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment = new TrainingFragment();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.commit();
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}