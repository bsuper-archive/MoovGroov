package me.bsu.moovgroovfinal;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.activeandroid.query.Select;

import java.io.IOException;

import me.bsu.moovgroovfinal.adapters.TracksListCursorAdapter;
import me.bsu.moovgroovfinal.models.Project;
import me.bsu.moovgroovfinal.models.Track;
import me.bsu.moovgroovfinal.other.RecyclerItemClickListener;

public class TracksActivity extends AppCompatActivity {

    public static final String TAG = "TRACKS_ACTIVITY";

    public static final String INTENT_PROJECT_ID = "TRACKS_ACTIVITY.PROJECT_ID";

    public long projectID;

    public RecyclerView mRecyclerView;
    public TracksListCursorAdapter mAdapter;

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
                // play sound here
                MediaRunnable test = new MediaRunnable(TracksActivity.this, R.raw.test);
                MediaRunnable test1 = new MediaRunnable(TracksActivity.this, R.raw.test1);
                MediaRunnable test2 = new MediaRunnable(TracksActivity.this, R.raw.test2);

                final Thread track0 = new Thread(test);
                final Thread track1 = new Thread(test1);
                final Thread track2 = new Thread(test2);

                if (position == 0) {
                    track0.run();
                } else if (position == 1) {
                    track1.run();
                } else {
                    track2.run();
                }
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

//            String name = p.name + " kick ass";
//            String filename = "bogus file name";
//            Track t = new Track(name, filename, p);
//            t.save();
//
//            String name2 = p.name + " momo";
//            String filename2 = "haha";
//            Track t2 = new Track(name2, filename2, p);
//            t2.save();
            String name = "Track 1";
            String filename = "bogus file name";
            Track t = new Track(name, filename, p);
            t.save();

            String name2 = "Track 2";
            String filename2 = "haha";
            Track t2 = new Track(name2, filename2, p);
            t2.save();

            String name3 = "Track 3";
            String filename3 = "hehe";
            Track t3 = new Track(name3, filename3, p);
            t3.save();
        } else {
            Log.d(TAG, Track.getTracks(projectID).size() + " items for project");
        }
    }

    private class MediaRunnable implements Runnable {
        MediaPlayer mPlayer;
        Context mContext;
        int mResourceID;

        public MediaRunnable(Context c, int resid){
            mPlayer = new MediaPlayer();
            mContext = c;
            mResourceID = resid;
        }

        @Override
        public void run(){

            if (mPlayer != null) {
                if (mPlayer.isPlaying()){
                    mPlayer.stop();
                    mPlayer.reset();
                } else {
                    mPlayer.reset();
                    try {
                        AssetFileDescriptor afd =
                                mContext.getResources().openRawResourceFd(mResourceID);
                        if (afd == null)
                            return;
                        mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                        mPlayer.prepare();
                    } catch (IOException | IllegalArgumentException | SecurityException e) {
                        Log.e("MAIN", e.getMessage());
                    }
                    mPlayer.start();
                }
            } else {
                mPlayer = MediaPlayer.create(mContext, mResourceID);
            }
        }
    }
}
