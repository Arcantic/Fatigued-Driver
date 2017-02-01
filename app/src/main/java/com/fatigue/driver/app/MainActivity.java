package com.fatigue.driver.app; /*
 |  CREATED on 10/27/16.
*/

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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

        //Initialize the navigation drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setStatusBarBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawer.setElevation(16);
            toolbar.setElevation(4);
        }

        //Actionbar creation
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);

        //Initialize the navigation drawer
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        //Replace the content with the Main Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment fragment = new MainFragment();
        fragmentTransaction.add(R.id.content_frame, fragment);
        fragmentTransaction.commit();


        //Check for Bluetooth Connection
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
        this.registerReceiver(mReceiver, filter3);
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


    /** Swaps fragments in the main content view*/
    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = MainFragment.class;
        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                fragmentClass = MainFragment.class;
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
            case R.id.nav_training:
                fragmentClass = TrainingFragment.class;
                break;
            default:
                fragmentClass = MainFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        drawer.closeDrawers();
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
