package com.fatigue.driver.app; /*
 |  CREATED on 10/27/16.
*/

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    public CharSequence mTitle;
    public DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Actionbar creation
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTitle = getString(R.string.title_main);


        //Actionbar creation
        //Initialize the navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, (Toolbar)findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawer.setElevation(16);
            toolbar.setElevation(4);
        }



        //Initialize the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);


        //If a user has been selected, then load the MainFragment.
        //else, load the UserSelect fragment.
        loadMainFragment();


        //Check for Bluetooth Connection
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);
    }

    //Load the main fragment...
    public void loadMainFragment(){
        setNavigationTitle(User.user_name);

        //Replace the content with the Main Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(!connectionBluetooth() || !connectionHeadset()){
            //Load connection error screen
            Fragment fragment = new MainFragment2();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.commit();
        }else{
            //Load mains screen
            Fragment fragment = new MainFragment();
            fragmentTransaction.replace(R.id.content_frame, fragment);
            fragmentTransaction.commit();
        }
    }

    public void lockDrawer() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    public void unlockDrawer(){
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void setNavigationTitle(String title){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        TextView nav_user = (TextView)hView.findViewById(R.id.nav_title);
        nav_user.setText(title);
    }


    private BluetoothAdapter mBluetoothAdapter;
    //Add code to check for bluetooth connection
    public boolean connectionBluetooth(){
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        // Is Bluetooth supported on this device?
        if (mBluetoothAdapter != null) {
            // Is Bluetooth turned on?
            if (mBluetoothAdapter.isEnabled()) {
                // Are Bluetooth Advertisements supported on this device?
                return true;
            } else {
                // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                Toast.makeText(getApplicationContext(), "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
                //jsnieves:TODO:create OnResult method
            }
        } else {
            // Bluetooth is not supported.
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
        }

        return false;
    }



    //Add code to check for headset connection
    public boolean connectionHeadset(){
        return true;
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

                //Force menu selection
                if(!backPressed && menuItem != null)
                    selectDrawerItem(menuItem);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_calibration);

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
                    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                    navigationView.setCheckedItem(R.id.nav_calibration);
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
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        System.out.println("BACK PRESSED");

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        /*
        else if(isTestRunning()){
            if(TrainingFragment.running_test)
                testIsRunningAlert(true, null, "Test Running", "This action will cancel the test. Continue?");
            else if(ResultsFragment.isOpen())
                testIsRunningAlert(true, null, "Delete Results", "This action will delete the test results. Continue?");
        }
        */
        else if (fragmentManager.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fragmentManager.popBackStack();
            if(list_title_stack.size() > 2){
                setTitle(list_title_stack.get(list_title_stack.size() - 1));
            }else if(list_title_stack.size() > 1) {
                setTitle(list_title_stack.get(list_title_stack.size() - 1));
                removeDrawerBackButton();
            }else{
                setTitle(getTitle());
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.setCheckedItem(R.id.nav_home);
                removeDrawerBackButton();
            }
            list_title_stack.remove(list_title_stack.size()-1);
        } else {
            super.onBackPressed();
        }
    }

    public ActionBarDrawerToggle mDrawerToggle;
    private boolean mToolBarNavigationListenerIsRegistered = false;
    public void removeDrawerBackButton(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, (Toolbar)findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // Show hamburger
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        // Remove the/any drawer toggle listener
        mDrawerToggle.setToolbarNavigationClickListener(null);
        mToolBarNavigationListenerIsRegistered = false;
    }

    public void drawDrawerBackButton(){
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(!mToolBarNavigationListenerIsRegistered) {
            mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
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



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        //int id = item.getItemId();

        selectDrawerItem(item);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if(title.equals(getString(R.string.nav_home)))
            mTitle = getString(R.string.title_main);
        getSupportActionBar().setTitle(mTitle);
    }


    public static ArrayList<String> list_title_stack = new ArrayList<>();

    /** Swaps fragments in the main content view*/
    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = MainFragment.class;
        boolean back_stack = true;
        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragmentClass = MainFragment.class;
                back_stack = false;
                break;
            case R.id.nav_stats:
                fragmentClass = StatsFragment.class;
                break;
            case R.id.nav_settings:
                fragmentClass = SettingsFragment.class;
                break;
            case R.id.nav_debugging:
                fragmentClass = DebuggingFragment.class;
                break;
            case R.id.nav_calibration:
                fragmentClass = CalibrationFragment.class;
                break;
            case R.id.nav_logout:
                logoutUser();
                break;
            default:
                fragmentClass = MainFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //See if a test is running before switching fragments.
        if(!isTestRunning()) {
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            list_title_stack.clear();

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content_frame, fragment);
            if (back_stack) {
                transaction.addToBackStack(null);
                list_title_stack.add(menuItem.getTitle() + "");
            }
            transaction.commit();

            // Highlight the selected item has been done by NavigationView
            menuItem.setChecked(true);
            // Set action bar title
            setTitle(menuItem.getTitle());
        }else{
            //If a test is running, alert user first
            /*
            if(TrainingFragment.running_test)
                testIsRunningAlert(false, menuItem, "Test Running", "This action will cancel the test. Continue?");
            else if(ResultsFragment.isOpen())
                testIsRunningAlert(false, menuItem, "Delete Results", "This action will delete the test results. Continue?");
            */
        }
        // Close the navigation drawer
        drawer.closeDrawers();
    }


    public void logoutUser(){
        User.logout(this);
    }



    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                System.out.println("Device Found");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                System.out.println("Device Connected");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                System.out.println("Device Search Completed");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                System.out.println("Device Disconnecting");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                System.out.println("Device Disconnected");
            }
        }
    };
}
