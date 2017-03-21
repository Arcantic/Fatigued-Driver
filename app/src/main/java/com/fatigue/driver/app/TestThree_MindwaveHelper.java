package com.fatigue.driver.app;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class TestThree_MindwaveHelper extends AppCompatActivity {
    private static final String TAG = TestThree_MindwaveHelper.class.getSimpleName();
    final int ALERT_PLUS_FATIGUED_TOTAL_NUM_OF_TRIALS_TO_RECORD = GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue * 2;
    Button btn_startPauseResume;
    Button btn_eval;

    TextView tv_log;
    TextView tv_timer;
    TextView tv_instruction;
    ScrollView sv_Log;
    LinkDetectedHandler linkDH;

    boolean isTrialInProgress=false;
    boolean isFirstStart = true;
    boolean isTrialCollectionPaused=false;
    boolean isContinue = true;
    boolean isLastCollectionFinished;

    boolean isStartOfTransitionPeriod =false;
    boolean isFirstEvalPress=true;

    boolean evalSwitch=true;

    Handler hand = new Handler();
    TimerRunnable tRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.test_three);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        final MindwaveHelperFragment mMindwaveHelperFrag = new MindwaveHelperFragment();

        ft.add(mMindwaveHelperFrag, TAG);
        ft.commit();

        mMindwaveHelperFrag.setRecordingRawData(false);

        new Thread() {
            @Override
            public void run() {
                try {
                    this.sleep((long) 1000);
                    linkDH = mMindwaveHelperFrag.getLinkDetectedHandler();
                    linkDH.showToast("TEST intercommunication", Toast.LENGTH_LONG);

                    //linkDH.setUsername(""); //TODO
                    //linkDH.initLogCreate();
                    //linkDH.initDrawWaveView();

                } catch (Exception e) {
                }
            }
        }.start();
        initView();
    }

    private void initView() {
        hand = new Handler();

        btn_startPauseResume = (Button) findViewById(R.id.btn_start);
        btn_eval =  (Button) findViewById(R.id.btn_eval);

        tv_timer = (TextView) findViewById(R.id.tv_timerCountdown);
        tv_log = (TextView) findViewById(R.id.tv_log);
        tv_instruction = (TextView) findViewById(R.id.tv_instruction);

        tv_timer.setText(String.valueOf(5.0));
        sv_Log = (ScrollView) findViewById(R.id.scrollViewLog);

        btn_startPauseResume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if(isFirstStart && linkDH.isConnected){

                    linkDH.fireTrialCollectorInitializer();
                    tv_instruction.setBackgroundColor(Color.YELLOW);
                    tv_instruction.setText("Wait...");
                    tRunnable = new TimerRunnable();
                    isTrialInProgress=true;
                    isTrialCollectionPaused=false;

                    hand.post(tRunnable);
                    isFirstStart=false;
                    btn_startPauseResume.setText("Pasue");
                }

                if(isTrialInProgress){
                    if (isTrialCollectionPaused){
                        hand.notify();
                        isTrialCollectionPaused=false;
                        btn_startPauseResume.setText("Resume");
                    } else{

                        try{hand.wait();
                        }catch(Exception e){
                        }
                        btn_startPauseResume.setText("Pause");
                    }
                }
            }
        });

        btn_eval.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (isFirstEvalPress) {
                    isFirstEvalPress=false;
                    linkDH.fireEvalInitializer(); //only on first click
                } else {
                    //TESTING //TODO fix here jsnieves

                    linkDH.fireEvalTestingInstance(evalSwitch);
                    evalSwitch = !evalSwitch;
                }
            }
        });
    }

    public void showToast(final String msg, final int timeStyle) {
        TestThree_MindwaveHelper.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, timeStyle).show();
            }
        });
    }

    public void showToast(final String msg) {
        TestThree_MindwaveHelper.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private final void focusOnView(){
        sv_Log.post(new Runnable() {
            @Override
            public void run() {
                sv_Log.smoothScrollTo(0, tv_log.getBottom());
            }
        });
    }

    public void playCompletionSound(){
        try {
            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class TimerRunnable implements Runnable {

        double timeRemaining;
        DecimalFormat formatter = new DecimalFormat("#0.0");
        int counter = 0;

        public TimerRunnable() {

        }

        public void run() {
            timeRemaining = Double.parseDouble(tv_timer.getText().toString());
            timeRemaining -= 0.1;

            if (timeRemaining >= 0.0) {
                tv_timer.setText((String.valueOf(formatter.format(timeRemaining))));

            } else {

                isStartOfTransitionPeriod = !isStartOfTransitionPeriod; //flip
                btn_startPauseResume.setEnabled(true); //enable pausing

                if (isStartOfTransitionPeriod) {

                    if (counter < GlobalSettings.calibrationNumOfTrialsToPerformAlert) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.alertDelayTimeBetweenTrialCollections))));
                        tv_instruction.setBackgroundColor(Color.YELLOW);
                        tv_instruction.setText("Wait...open eyes in...");

                        playCompletionSound();

                    } else if (counter == GlobalSettings.calibrationNumOfTrialsToPerformAlert) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.fatigueDelayTimeBetweenTrialCollections + 4.0))));
                        tv_instruction.setBackgroundColor(Color.YELLOW);
                        tv_instruction.setText("Wait...close eyes in...");

                        playCompletionSound();

                    } else if (counter < ALERT_PLUS_FATIGUED_TOTAL_NUM_OF_TRIALS_TO_RECORD) {
                        tv_timer.setText((String.valueOf(formatter.format(GlobalSettings.fatigueDelayTimeBetweenTrialCollections))));
                        tv_instruction.setBackgroundColor(Color.YELLOW);
                        tv_instruction.setText("Wait...close eyes in...");

                        playCompletionSound();

                    } else {
                        tv_instruction.setText("FINISHED");
                    }

                } else {

                    btn_startPauseResume.setEnabled(false);
                    counter++;

                    if (counter <= GlobalSettings.calibrationNumOfTrialsToPerformAlertOrFatigue) {
                        //
                        tv_timer.setText((String.valueOf(formatter.format((double) GlobalSettings.alertTrialCollectionIntervalDuration))));
                        linkDH.fireTrialCollectorInstance(true);//eyesOpen collect
                        tv_instruction.setBackgroundColor(Color.GREEN);
                        tv_instruction.setText("Open Eyes");

                    } else if (counter <= ALERT_PLUS_FATIGUED_TOTAL_NUM_OF_TRIALS_TO_RECORD) {
                        tv_timer.setText((String.valueOf(formatter.format((double) GlobalSettings.fatigueTrialCollectionIntervalDuration))));
                        linkDH.fireTrialCollectorInstance(false);//eyesClosed
                        tv_instruction.setBackgroundColor(Color.RED);
                        tv_instruction.setText("Close Eyes");

                    } else {
                        tv_instruction.setText("FINISHED");
                    }
                }
            }
            if (counter <= ALERT_PLUS_FATIGUED_TOTAL_NUM_OF_TRIALS_TO_RECORD) {
                hand.postDelayed(this, 100);
            } else {
                isTrialInProgress = false;
            }
        }
    }
}