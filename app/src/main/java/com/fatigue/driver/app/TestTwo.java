package com.fatigue.driver.app; /*
 |  CREATED on 10/27/16.
*/

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.media.MediaPlayer;

public class TestTwo extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.test_two);

        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.Loud_Alarm_Clock_Buzzer);
        mediaPlayer.start();


    }
}
