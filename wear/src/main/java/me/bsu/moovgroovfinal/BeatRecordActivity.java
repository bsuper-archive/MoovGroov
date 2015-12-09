package me.bsu.moovgroovfinal;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class BeatRecordActivity extends Activity {
    private GoogleApiClient mGoogleApiClient;
    private static final String FINISH_WATCH_BEATS_ACTIVITY = "/finish_watch_beats_activity";
    private static final String RECORD_BEATS_ACTIVITY = "/record_beats_activity";

    private stateEnum state = stateEnum.START_SEQ;
    private stateEnum nextState;
    private long startTime;

    private enum stateEnum { START_SEQ, REC_BEAT, END_BEAT};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beat_record);
        Log.d("WEAR BEATS RECORD", "OnCreate");

        // create Google Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("WEAR","SERVICE CONNECTED TO GOOGLE API");
                        /* Successfully connected */
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("WEAR","SERVICE SUSPENDED FROM GOOGLE API");
                        /* Connection was interrupted */
                    }
                })

                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("WEAR BEATS", "Failed to connect: "+result);
                    }
                })
                .addApi(Wearable.API).build();

        mGoogleApiClient.connect();

        // Entire watch face is Image Button
        // Tap to start
        // Tap beats
        // Swipe Left to finish
        final ImageButton newButton = (ImageButton) findViewById(R.id.tempButton);
        newButton.setImageResource(R.drawable.seqready);

        if (newButton != null) {
            // Interpret click differently depending on state
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (state) {
                        case START_SEQ: // Countdown 3-2-1-Go
                            Log.d("WATCH BEAT", "STARTSEQ");
                            startCountdown();
                            nextState = stateEnum.REC_BEAT;
                            break;
                        case REC_BEAT: // Every tap is registered as a beat
                            Log.d("WATCH BEAT", "REQBEAT");
                            sendBeat2Mobile();
                            break;
                    }
                    state = nextState;

                }
            });
            newButton.setOnTouchListener(new OnSwipeTouchListener(BeatRecordActivity.this) {

                public boolean onSwipeTop() {
                    Toast.makeText(BeatRecordActivity.this, "top", Toast.LENGTH_SHORT).show();
                    return true;
                }

                public boolean onSwipeRight() {
                    Toast.makeText(BeatRecordActivity.this, "right", Toast.LENGTH_SHORT).show();
                    return true;
                }

                public boolean onSwipeLeft() { // Stop beat recording
                    Toast.makeText(BeatRecordActivity.this, "left", Toast.LENGTH_SHORT).show();
                    if (state == stateEnum.REC_BEAT) {
                        sendMessage(FINISH_WATCH_BEATS_ACTIVITY, "");
                        state = stateEnum.START_SEQ;
                        newButton.setImageResource(R.drawable.seqready);
                    }
                    return true;
                }

                public boolean onSwipeBottom() {
                    Toast.makeText(BeatRecordActivity.this, "bottom", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }


    }

    private void startCountdown() {
        new CountDownTimer(4000, 500) {
            ImageButton newButton = (ImageButton) findViewById(R.id.tempButton);

            public void onTick(long millisUntilFinished) {

                if (millisUntilFinished < 4000)
                    newButton.setImageResource(R.drawable.seqcd3);
                if (millisUntilFinished < 3000)
                    newButton.setImageResource(R.drawable.seqcd2);
                if (millisUntilFinished < 2000)
                    newButton.setImageResource(R.drawable.seqcd1);
                if (millisUntilFinished < 1000)
                    newButton.setImageResource(R.drawable.seqcd0);
            }

            public void onFinish() {
                startTime = System.currentTimeMillis();
                startBeatRecording();
                this.cancel();
            }
        }.start();
    }

    private void startBeatRecording() {
        new CountDownTimer(4000, 500) {
            ImageButton newButton = (ImageButton) findViewById(R.id.tempButton);

            public void onTick(long millisUntilFinished) {
                if (state != stateEnum.REC_BEAT) {
                    this.cancel();
                } else {
                    if (millisUntilFinished < 4000)
                        newButton.setImageResource(R.drawable.seqrec1);
                    if (millisUntilFinished < 3000)
                        newButton.setImageResource(R.drawable.seqrec2);
                    if (millisUntilFinished < 2000)
                        newButton.setImageResource(R.drawable.seqrec3);
                    if (millisUntilFinished < 1000)
                        newButton.setImageResource(R.drawable.seqrec4);
                }
            }

            public void onFinish() {
                if (state != stateEnum.REC_BEAT) {
                    this.cancel();
                } else {
                    this.start();
                }
            }
        }.start();
    }

    private void sendBeat2Mobile() {
        long timeDelta = System.currentTimeMillis() - startTime;
        sendMessage(RECORD_BEATS_ACTIVITY, String.valueOf(timeDelta));
    }

    // Send message through api to Mobile
    private void sendMessage( final String path, final String text ) {
        Log.d("MOBILE SEND MESSAGE", "Start Beat 1");
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    Log.d("MOBILE SEND MESSAGE", "Start Beat with NODES");
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }
}
