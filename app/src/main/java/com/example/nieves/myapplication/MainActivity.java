package com.example.nieves.myapplication;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//jsnieves:BEGIN
@TargetApi(21)  // (18) to support:  mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                // (21) to support: if (mBluetoothAdapter.isMultipleAdvertisementSupported())
//jsnieves:END

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //jsnieves:BEGIN:From TGStream
    private static final String TAG = MainActivity.class.getSimpleName();
    //jsnieves:END:From TGStream

    //jsnieves:Begin:From BluetoothAdvertisements
    private BluetoothAdapter mBluetoothAdapter;
    //jsnieves:End:From BluetoothAdvertisements
    //jsnieves:from TGStream
    private Button btn_adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //jsnieves: ALL above is stock
        //See: http://developer.android.com/shareables/training/NavigationDrawer.zip for further customization?
        //end:jsnieves

        //jsnieves:BEGIN:From BluetoothAdvertisements
        setTitle(R.string.activity_main_title);



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
                    //jsnieves:TODO: create OnResult method
                }
            } else {

                // Bluetooth is not supported.
                showErrorText(R.string.bt_not_supported);
            }
        }

        //jsnieves:END:From BluetoothAdvertisements

        initView();



    }
    //end

    private void initView() {
        //jsnieves:entire method from TGStream

        btn_adapter = (Button) findViewById(R.id.btn_adapter);

        //jsnieves: btn_adapter is 2nd button on main_view.xml and of our primary and current concern
        //launches BluetoothAdapterDemoActivity
        btn_adapter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //jsnieves:changed below
                // Intent intent = new Intent(DemoActivity.this,BluetoothAdapterDemoActivity.class);
                Intent intent = new Intent(MainActivity.this, BluetoothAdapterDemoActivity.class);

                Log.d(TAG,"Start the BluetoothAdapterDemoActivity");
                startActivity(intent);
            }
        });




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
        // Handle navigation view item clicks here.

        //jsnieves: TODO: (below) correct R.id.names to match a generic form, or rename to match the finalized menu
        //end jsnieves
        int id = item.getItemId();

        if (id == R.id.nav_1) {
            // Handle the camera action
        } else if (id == R.id.nav_2) {

        } else if (id == R.id.nav_3) {

        } else if (id == R.id.nav_4) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void showErrorText(int messageId) {
    //jsnieves:BEGIN: Entire method from BluetoothAdvertisements

        TextView view = (TextView) findViewById(R.id.error_textview);
        view.setText(getString(messageId));
    }
    //jsnieves:END: Entire method from BluetoothAdvertisements


    private void setupFragments() {
//        //jsnieves:BEGIN: Entire method from BluetoothAdvertisements
//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//
//        ScannerFragment scannerFragment = new ScannerFragment();
//        // Fragments can't access system services directly, so pass it the BluetoothAdapter
//        scannerFragment.setBluetoothAdapter(mBluetoothAdapter);
//        transaction.replace(R.id.scanner_fragment_container, scannerFragment);
//
//        AdvertiserFragment advertiserFragment = new AdvertiserFragment();
//        transaction.replace(R.id.advertiser_fragment_container, advertiserFragment);
//
//        transaction.commit();
//        //jsnieves:END: Entire method from BluetoothAdvertisements
    }

}

