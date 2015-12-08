package me.bsu.moovgroovfinal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchListenerService extends WearableListenerService {
    private static final String START_WATCH_BEATS_ACTIVITY = "/watch_beats_activity";

    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("WATCH BEAT", "Received START for beat");

        if (messageEvent.getPath().equalsIgnoreCase(START_WATCH_BEATS_ACTIVITY)){
            Toast.makeText(this,"START BEATS",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this , BeatRecordActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }

    }
}
