package me.bsu.moovgroovfinal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.activeandroid.query.Select;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
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
    public List<Boolean> trackEnableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        projectID = getIntent().getLongExtra(INTENT_PROJECT_ID, 0);
        Log.d(TAG, String.format("Got project ID %d", projectID));

        Project p = new Select().from(Project.class).where("Id = ?", projectID).executeSingle();
        setTitle(p.name);

        setupFAB();
        setupRecyclerView();
        populateTracksIfNecessary();
        populateRecyclerView();

        initializeMediaThreads();
    }

    private void initializeMediaThreads(){
        //initialize the list of media play
        trackEnableList = new ArrayList<Boolean>();
        trackList = Track.getTracks(projectID);
        mediaThreadList = new ArrayList<Thread>();
        for (int i = 0; i < trackList.size(); i++) {
            trackEnableList.add(i, false);
            int type = trackList.get(i).type;
            if (type == Track.TYPE_VOCAL) {
                String dir = trackList.get(i).filename;
                MediaRunnable mdr = new MediaRunnable(getApplicationContext(), dir);
                mediaThreadList.add(i, new Thread(mdr));
            } else if (type == Track.TYPE_BEAT_LOOP) {
                List<Timestamp> timestampList = trackList.get(i).getTimestamps();
                Log.d(TAG, "Got " + timestampList.size() + " items");
                ArrayList<Integer> timeList = new ArrayList<Integer>();
                for (int j = 0; j < timestampList.size(); j++) {
                    timeList.add(j, (int) timestampList.get(j).time);
                }
                BeatLoopRunnable blr = new BeatLoopRunnable(getApplicationContext(), timeList);
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
            }
        });
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
                View v = mRecyclerView.getLayoutManager().getChildAt(position);
                LinearLayout lnl = (LinearLayout)v.findViewById(R.id.track_item_layout);
                ImageView imv = (ImageView) v.findViewById(R.id.list_item_play_pause);
                if (!trackEnableList.get(position)) {
                    trackEnableList.set(position, true);
                    lnl.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    imv.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                } else {
                    trackEnableList.set(position, false);
                    lnl.setBackgroundColor(getResources().getColor(R.color.half_black));
                    imv.setBackgroundColor(getResources().getColor(R.color.half_black));
                }
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
        if (Track.getTracks(projectID).size() < 35) {
            Project p = new Select().from(Project.class).where("Id = ?", projectID).executeSingle();
            Log.d(TAG, "Got Project Name: " + p.name);

            String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(TAG, "dir: " + newFileName);
            newFileName += "/testaudio.mp3";

            String name = p.name + " Track 1";
            String filename = "bogus file name";
            Track t = new Track(name, filename, Track.TYPE_VOCAL, p);
            t.save();

            String name2 = p.name + " Track 2";
            String filename2 = "haha";
            Track t2 = new Track(name2, newFileName, Track.TYPE_VOCAL, p);
            t2.save();

            String namebeat1 = p.name + " Beat Track 1";
            String filename3 = "empty";
            Track tb1 = new Track(namebeat1, filename3, Track.TYPE_BEAT_LOOP, p);
            tb1.save();
            Timestamp tm1 = new Timestamp(tb1, 1000);
            Timestamp tm2 = new Timestamp(tb1, 2000);
            Timestamp tm3 = new Timestamp(tb1, 3000);
            Timestamp tm4 = new Timestamp(tb1, 4000);
            tm1.save();
            tm2.save();
            tm3.save();
            tm4.save();
            Log.d(TAG, "Track has " + tb1.getTimestamps().size() + " timestamps");

            String namebeat2 = p.name + " Beat Track 2";
            String filename4 = "empty 2";
            Track tb2 = new Track(namebeat2, filename4, Track.TYPE_BEAT_LOOP, p);
            tb2.save();
            Timestamp tm5 = new Timestamp(tb2, 500);
            Timestamp tm6 = new Timestamp(tb2, 1500);
            Timestamp tm7 = new Timestamp(tb2, 2500);
            Timestamp tm8 = new Timestamp(tb2, 3000);
            tm5.save();
            tm6.save();
            tm7.save();
            tm8.save();


        } else {
            Log.d(TAG, Track.getTracks(projectID).size() + " items for project");
        }
    }




}
