package com.fatigue.driver.app;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import libsvm.svm_model;

//jsnieves:BEGIN
@TargetApi(21)  // (18) to support:  mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
// (21) to support: if (mBluetoothAdapter.isMultipleAdvertisementSupported())
//jsnieves:END

public class Test_MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = Test_MainActivity.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    //jsnieves:from TGStream
    private Button btn_adapter = null;

    //jsnieves:BEGIN:init test buttons
    private Button btn_test1 = null;
    private Button btn_test2 = null;
    private Button btn_test3 = null;
    private Button btn_test4 = null;
    private Button btn_test5 = null;
    //jsnieves:END:init test buttons


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setTitle(R.string.title_main);

        if (savedInstanceState == null) {

            mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                    .getAdapter();

            // Is Bluetooth supported on this device?
            if (mBluetoothAdapter != null) {

                // Is Bluetooth turned on?
                if (mBluetoothAdapter.isEnabled()) {

                    // Are Bluetooth Advertisements supported on this device?
                    if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                        // Everything is supported and enabled, load the fragments.
                        //jsnieves:TODO:check functionality or necessity
                        //setupFragments(); //was enabled by default
                        //end jsnieves

                    } else {

                        // Bluetooth Advertisements are not supported.
                        showErrorText(R.string.bt_ads_not_supported);
                    }
                } else {

                    // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                    //jsnieves:TODO:create OnResult method
                }
            } else {

                // Bluetooth is not supported.
                showErrorText(R.string.bt_not_supported);
            }
        }

        initView();
    }
    //end

    private void initView() {

        btn_adapter = (Button) findViewById(R.id.btn_adapter);

        btn_test1 = (Button) findViewById(R.id.btn_test1);
        btn_test2 = (Button) findViewById(R.id.btn_test2);
        btn_test3 = (Button) findViewById(R.id.btn_mindwave_init);
        btn_test4 = (Button) findViewById(R.id.btn_test4);
        btn_test5 = (Button) findViewById(R.id.btn_test5);

        //launches BluetoothAdapterDemoActivity
        btn_adapter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //jsnieves:COMMENT:changed below
                // Intent intent = new Intent(DemoActivity.this,BluetoothAdapterDemoActivity.class);
                Intent intent = new Intent(Test_MainActivity.this, BluetoothAdapterDemoActivity.class);

                Log.d(TAG, "Starting the BluetoothAdapterDemoActivity");
                startActivity(intent);
            }
        });

        //jsnieves;BEGIN: onClickListeners for test buttons
        btn_test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Test_MainActivity.this, LoginActivity.class);

                Log.d(TAG, "Starting MainActivity Activity");
                startActivity(intent);
            }
        });

        btn_test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Test_MainActivity.this, TestTwo.class);

                Log.d(TAG, "Starting TestTwo Activity");
                startActivity(intent);
            }
        });

        btn_test3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Test_MainActivity.this, TestThree_MindwaveHelper.class);

                Log.d(TAG, "Starting TestThree Activity");
                startActivity(intent);
            }
        });

        btn_test4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

            }
        });

        btn_test5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final SVMTrainingTest svm_test = new SVMTrainingTest();
                svm_test.setData();

                svm_model model = svm_test.svmTrain();
                //SAVE "model" data to text file/
                System.out.println("TEST RESULT: "+model.nr_class);
            }
        });

        //jsnieves:END:added onClickListeners for test buttons
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //Handle navigation view item clicks here.

        int id = item.getItemId();

        if (id == R.id.nav_1) {}
        else if (id == R.id.nav_2) {}
        else if (id == R.id.nav_3) {}
        else if (id == R.id.nav_4) {}
        else if (id == R.id.nav_share) {}
        else if (id == R.id.nav_send) {}

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showErrorText(int messageId) {
        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText(getString(messageId));
    }

    private void setupFragments() {

//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        ScannerFragment scannerFragment = new ScannerFragment();
//        // Fragments can't access system services directly, so pass it the BluetoothAdapter
//        scannerFragment.setBluetoothAdapter(mBluetoothAdapter);
//        transaction.replace(R.id.scanner_fragment_container, scannerFragment);//
//        AdvertiserFragment advertiserFragment = new AdvertiserFragment();
//        transaction.replace(R.id.advertiser_fragment_container, advertiserFragment);//
//        transaction.commit();
    }
}