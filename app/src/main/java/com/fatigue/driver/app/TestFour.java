package com.fatigue.driver.app; /*
 |  CREATED on 10/27/16.
*/

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class TestFour extends AppCompatActivity {

    private static final String TAG = TestFour.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setContentView(R.layout.test_four);

    }
}