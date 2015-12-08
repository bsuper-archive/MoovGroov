package me.bsu.moovgroovfinal.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MobileListenerService extends WearableListenerService {
    private static final String FINISH_WATCH_BEATS_ACTIVITY = "/finish_watch_beats_activity";
    private static final String RECORD_BEATS_ACTIVITY = "/record_beats_activity";
    public static final String BEATS_ACTIVITY = "/beats_activity";
    LocalBroadcastManager broadcaster;

    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("MOBILE BEAT", "Received from wear for beat");

        if (messageEvent.getPath().equalsIgnoreCase(FINISH_WATCH_BEATS_ACTIVITY)){
            //Toast.makeText(this, "MOBILE FINISH BEATS", Toast.LENGTH_SHORT).show();
            // TODO End Beats Activity and go back to Tracks
            Intent i = new Intent();
            i.setAction(BEATS_ACTIVITY);
            i.putExtra("FINISH", "1");
            i.putExtra("BEAT", "0");
            broadcaster.sendBroadcast(i);
            Log.d("BEAT", "FINISH BEATS");
        } else if (messageEvent.getPath().equalsIgnoreCase(RECORD_BEATS_ACTIVITY)){
            String value = new String(messageEvent.getData(), StandardCharsets.UTF_8);
            //Toast.makeText(this, "BEAT DETECTED: " + value, Toast.LENGTH_SHORT).show();
            // TODO Figure out what to do with this data
            Intent i = new Intent();
            i.setAction(BEATS_ACTIVITY);
            i.putExtra("FINISH", "0");
            i.putExtra("BEAT", value);
            broadcaster.sendBroadcast(i);
            Log.d("BEAT", "BEAT sent: " +value);


        } else {
            super.onMessageReceived(messageEvent);
        }

    }



}
