package me.bsu.moovgroovfinal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.activeandroid.query.Select;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.bsu.moovgroovfinal.adapters.TracksListCursorAdapter;
import me.bsu.moovgroovfinal.mixerutils.BeatLoopRunnable;
import me.bsu.moovgroovfinal.mixerutils.MediaRunnable;
import me.bsu.moovgroovfinal.models.Project;
import me.bsu.moovgroovfinal.models.Timestamp;
import me.bsu.moovgroovfinal.models.Track;
import me.bsu.moovgroovfinal.other.RecyclerItemClickListener;

public class TracksActivity extends AppCompatActivity {

    public static final String TAG = "TRACKS_ACTIVITY";

    public static final String INTENT_PROJECT_ID = "TRACKS_ACTIVITY.PROJECT_ID";
    private static final String START_WATCH_BEATS_ACTIVITY = "/watch_beats_activity";

    public long projectID;

    public RecyclerView mRecyclerView;
    public TracksListCursorAdapter mAdapter;

    public List<Track> trackList;
    public List<Thread> mediaThreadList;
    public HashMap<Track, Thread> trackThreadMap;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectID = getIntent().getLongExtra(INTENT_PROJECT_ID, 0);
        Log.d(TAG, String.format("Got project ID %d", projectID));

        setupFAB();
        setupRecyclerView();
        populateTracksIfNecessary();
        populateRecyclerView();

        initializeMediaThreads();
        connectGoogleApiClient();
    }

    private void connectGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("MOBILE", "SERVICE CONNECTED TO GOOGLE API");
                        /* Successfully connected */
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("MOBILE", "SERVICE SUSPEEDED FROM GOOGLE API");
                        /* Connection was interrupted */
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }


    private void initializeMediaThreads(){
        //initialize the list of media play
        trackList = Track.getTracks(projectID);
        mediaThreadList = new ArrayList<Thread>();
        for (int i = 0; i < trackList.size(); i++) {
            int type = trackList.get(i).type;
            if (type == Track.TYPE_VOCAL) {
                String dir = trackList.get(i).filename;
                MediaRunnable mdr = new MediaRunnable(getApplicationContext(), dir);
                mediaThreadList.add(i, new Thread(mdr));
            } else if (type == Track.TYPE_BEAT_LOOP) {
                List<Timestamp> timestampList = trackList.get(i).getTimestamps();
                BeatLoopRunnable blr = new BeatLoopRunnable(getApplicationContext(), 0, timestampList, 120);
                mediaThreadList.add(i, new Thread(blr));
            }
        }
    }

    private void setupFAB() {
        FloatingActionButton FABaddVocalMelody = (FloatingActionButton) findViewById(R.id.fab_add_vocal_melody_track);
        FloatingActionButton FABaddBeatLoop = (FloatingActionButton) findViewById(R.id.fab_add_beat_loop_track);
        FABaddVocalMelody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FAB Add Vocal Melody clicked");
                startAddVocalTrackActivity();
            }
        });
        FABaddBeatLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FAB Add Beat Loop clicked");

                // Start Beats Activity on Mobile
                Intent intent = new Intent(v.getContext(), BeatsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("projectID", projectID);
                startActivity(intent);

                // Send Message to Watch to start beats
                String msg = "START";
                // Send message to Phone.
                sendMessage( START_WATCH_BEATS_ACTIVITY, msg);
                Log.d("MOBILE BEAT", "Start Beat");

            }
        });
    }

    // Send message through api to Watch
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

    private void startAddVocalTrackActivity() {
        Intent i = new Intent(TracksActivity.this, RecordActivity.class);
        startActivity(i);
    }

    private void setupRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.listview_tracks);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, String.format("%d clicked", position));
                mediaThreadList.get(position).run();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.d(TAG, String.format("%d long press", position));
            }
        }));
    }

    private void populateRecyclerView() {
        mAdapter = new TracksListCursorAdapter(Track.getTracksCursor(projectID), this);
        mRecyclerView.swapAdapter(mAdapter, true);
    }

    private void populateTracksIfNecessary() {
        if (Track.getTracks(projectID).size() < 5) {
            Project p = new Select().from(Project.class).where("Id = ?", projectID).executeSingle();
            Log.d(TAG, "Got Project Name: " + p.name);

            String name = p.name + " kick ass";
            String filename = "bogus file name";
            Track t = new Track(name, filename, Track.TYPE_VOCAL, p);
            t.save();

            String name2 = p.name + " momo";
            String filename2 = "haha";
            Track t2 = new Track(name2, filename2, Track.TYPE_VOCAL, p);
            t2.save();
        } else {
            Log.d(TAG, Track.getTracks(projectID).size() + " items for project");
        }
    }




}
