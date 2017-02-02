package com.fatigue.driver.app; /*
 |  CREATED on 10/27/16.
*/

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.media.MediaPlayer;
import android.widget.Button;

public class TestTwo extends Activity {

    private Button btnStartStop =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.test_two);

        btnStartStop = (Button) findViewById(R.id.btn_alarm_start_stop);
        final MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_alert);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                    btnStartStop.setText("Start");
                }
                else {
                    mediaPlayer.start();
                    btnStartStop.setText("Stop");
                }
            }
        });
    }
}
