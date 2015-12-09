package me.bsu.moovgroovfinal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import me.bsu.moovgroovfinal.models.Project;
import me.bsu.moovgroovfinal.models.Timestamp;
import me.bsu.moovgroovfinal.models.Track;
import me.bsu.moovgroovfinal.services.MobileListenerService;
import me.bsu.moovgroovfinal.sound.Sound;
import me.bsu.moovgroovfinal.sound.SoundPlayer;
import me.bsu.moovgroovfinal.sound.SoundStore;

public class BeatsActivity extends AppCompatActivity {

    private static final String TAG = "BeatsActivity";

    BroadcastReceiver wearTapReceiver;
    List beatArray = new ArrayList();
    long projectID;

    SoundPlayer mSoundPlayer;
    Sound[] soundArray;

    RelativeLayout seqLayout;
    TextView seqText;

    CountDownTimer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beats);

        setupSoundPlayer();

        setupFlashTimer();

        setTitle("Create New Beat Loop");
        projectID = getIntent().getLongExtra("projectID", 0);
        wearTapReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra("BEAT") && intent.hasExtra("FINISH")) {
                    String beatTimeStr = intent.getStringExtra("BEAT");
                    String finish = intent.getStringExtra("FINISH");
                    Log.d("BEAT", "Broadcast receive " + beatTimeStr + " finish: " + finish);
                    if (finish.equals("1")) {
                        // Save File
                        saveBeat();

                    } else {

                        // Save beat into array
                        long beatTime = Long.valueOf(beatTimeStr).longValue();
                        Log.d(TAG, "beat received: "+beatTime);
                        beatArray.add(beatTime);
                        mSoundPlayer.playSound(soundArray[1]);
                        seqLayout.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                        seqText.setBackgroundColor(getResources().getColor(R.color.background));
                        mTimer.start();
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver((wearTapReceiver),
                new IntentFilter(MobileListenerService.BEATS_ACTIVITY));
    }

    private void setupSoundPlayer() {
        mSoundPlayer = new SoundPlayer(getApplicationContext());
        soundArray = SoundStore.getSounds(getApplicationContext());
    }

    private void setupFlashTimer() {
        seqLayout = (RelativeLayout) findViewById(R.id.sequencer_layout);
        seqText = (TextView) findViewById(R.id.sequencer_text);
        mTimer = new CountDownTimer(300, 100) {

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                seqLayout.setBackgroundColor(getResources().getColor(R.color.background));
                seqText.setBackgroundColor(getResources().getColor(R.color.white));
            }
        };
    }

    private void saveBeat() {
        // Pop up Dialog to get save filename
        new MaterialDialog.Builder(BeatsActivity.this)
                .title("Save Beats Recording")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Beats Filename", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        // Do something
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String fileName = dialog.getInputEditText().getText().toString();
                        Log.d("RECORD", "EDITEXT: %s".format(dialog.getInputEditText().getText().toString()));
                        Track t1 = new Track(fileName, fileName, Track.TYPE_BEAT_LOOP, Project.getProject(projectID));
                        t1.save();
                        for (int i = 0; i < beatArray.size(); i++) {
                            Timestamp ts1 = new Timestamp(t1, (long) beatArray.get(i));
                            ts1.save();
                            Log.d(TAG, "Saved beat time: "+ts1.time);
                        }

                        // END ACTIVITY AND GO TO PARENT
                        finish();
                    }
                }).show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.d("SENSOR", "onResume");
        LocalBroadcastManager.getInstance(this).registerReceiver((wearTapReceiver),
                new IntentFilter(MobileListenerService.BEATS_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wearTapReceiver);
    }





}
