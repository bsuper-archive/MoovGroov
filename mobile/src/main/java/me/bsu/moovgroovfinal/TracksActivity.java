package me.bsu.moovgroovfinal;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.activeandroid.query.Select;

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

    public long projectID;

    public RecyclerView mRecyclerView;
    public TracksListCursorAdapter mAdapter;

    public List<Track> trackList;
    public List<Thread> mediaThreadList;
    public HashMap<Track, Thread> trackThreadMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectID = getIntent().getLongExtra(INTENT_PROJECT_ID, 0);
        Log.d(TAG, String.format("Got project ID %d", projectID));
        
        setupRecyclerView();
        populateTracksIfNecessary();
        populateRecyclerView();

        initializeMediaThreads();
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
