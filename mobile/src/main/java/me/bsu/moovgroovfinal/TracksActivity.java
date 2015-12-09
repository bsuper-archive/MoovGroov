package me.bsu.moovgroovfinal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

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
    private static final String START_WATCH_BEATS_ACTIVITY = "/watch_beats_activity";

    public long projectID;

    public RecyclerView mRecyclerView;
    public TracksListCursorAdapter mAdapter;
    public LinearLayout mMenuBackground;
    public LinearLayout mNothingToShow;

    public List<Track> trackList;
    public List<Thread> mediaThreadList;
    public List<Boolean> trackEnableList;

    private GoogleApiClient mGoogleApiClient;

    //file request/reception code from google android website:
    //http://developer.android.com/guide/topics/providers/document-provider.html
    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNothingToShow = (LinearLayout) findViewById(R.id.track_nothing_to_show);

        projectID = getIntent().getLongExtra(INTENT_PROJECT_ID, 0);
        Log.d(TAG, String.format("Got project ID %sd", projectID));

        Project p = Project.getProject(projectID);
        setTitle(p.name);

        setupFAB();
        setupRecyclerView();
        //populateTracksIfNecessary();
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
        mediaThreadList = new ArrayList<Thread>();
        trackEnableList = new ArrayList<Boolean>();
        mediaThreadList.clear();
        trackEnableList.clear();

        trackList = Track.getTracks(projectID);

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

        mMenuBackground = (LinearLayout) findViewById(R.id.track_fab_background);
        final FloatingActionsMenu FABaddTrack = (FloatingActionsMenu) findViewById(R.id.fab_add_track);
        FloatingActionButton FABaddVocalMelody = (FloatingActionButton) findViewById(R.id.fab_add_vocal_melody_track);
        FloatingActionButton FABaddBeatLoop = (FloatingActionButton) findViewById(R.id.fab_add_beat_loop_track);
        FloatingActionButton FABaddFile = (FloatingActionButton) findViewById(R.id.fab_add_from_file_track);
        FABaddVocalMelody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FAB Add Vocal Melody clicked");
                FABaddTrack.collapse();
                startAddVocalTrackActivity();
            }
        });
        FABaddBeatLoop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FAB Add Beat Loop clicked");
                FABaddTrack.collapse();
                // Start Beats Activity on Mobile
                Intent intent = new Intent(v.getContext(), BeatsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("projectID", projectID);
                startActivity(intent);

                // Send Message to Watch to start beats
                String msg = "START";
                // Send message to Phone.
                sendMessage(START_WATCH_BEATS_ACTIVITY, msg);
                Log.d("MOBILE BEAT", "Start Beat");
            }
        });
        FABaddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FABaddTrack.collapse();

                /**
                 * Fires an intent to spin up the "file chooser" UI and select an image.
                 */

                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // Filter to show only images, using the image MIME data type.
                // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
                // To search for all documents available via installed storage providers,
                // it would be "*/*".
                intent.setType("audio/*");

                startActivityForResult(intent, READ_REQUEST_CODE);

            }
        });
        FABaddTrack.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded() {
                mMenuBackground.setVisibility(View.VISIBLE);
            }

            @Override
            public void onMenuCollapsed() {
                mMenuBackground.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Result Uri: " + uri.toString());
                String path = getPathFromURI(uri);
                Track t = new Track(getDisplayName(path), path, Track.TYPE_VOCAL, Project.getProject(projectID));
                t.save();
            }
        }
    }

    // uri converting code:
    //http://stackoverflow.com/questions/19985286/convert-content-uri-to-actual-path-in-android-4-4
    public String getPathFromURI(Uri uri) {

        String path;

        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        Uri contentUri = null;
        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String selection = "_id=?";
        final String[] selectionArgs = new String[] {
                split[1]
        };
        path = getDataColumn(getApplicationContext(), contentUri, selection, selectionArgs);
        Log.d(TAG, "resolved path: " + path);
        return path;
    };

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public String getDisplayName(String path) {
        String displayName = "";
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        displayName += retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        displayName += " - ";
        displayName += retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        Log.d(TAG, "Retrieved audio metadata: " + displayName);
        return displayName;
    }

    // Send message through api to Watch
    private void sendMessage(final String path, final String text ) {
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
        i.putExtra(INTENT_PROJECT_ID, projectID);
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

                final TextView txtDelete = (TextView) view.findViewById(R.id.project_delete);
                if (txtDelete.isEnabled()) {
                    txtDelete.callOnClick();
                    txtDelete.setEnabled(false);
                    txtDelete.setVisibility(View.INVISIBLE);
                    populateRecyclerView();
                } else {
                    LinearLayout lnl = (LinearLayout) view.findViewById(R.id.track_item_layout);
                    ImageView imv = (ImageView) view.findViewById(R.id.list_item_play_pause);
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


            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.d(TAG, String.format("%d long press", position));

                final Track t = Track.getTracks(projectID).get(position);

                new AlertDialog.Builder(TracksActivity.this)
                        .setTitle("Delete Track")
                        .setMessage("Are you sure you want to delete this track?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Log.d("DELETE Track", "delete button pressed");
                                deleteTrack(t);
                                populateRecyclerView();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        }));
    }

    private void deleteTrack(Track t) {

        Log.d(TAG, "Track name: " + t.name + ", id: " + t.getId());

        if (t.type == Track.TYPE_BEAT_LOOP) {
            new Delete().from(Timestamp.class).where("track = ?", t.getId()).execute();
            Log.d(TAG, "timestamps deleted from " + t.name);
        }
        new Delete().from(Track.class).where("Id = ?", t.getId()).execute();

    }


    private void populateRecyclerView() {
        mAdapter = new TracksListCursorAdapter(Track.getTracksCursor(projectID), this);
        mRecyclerView.swapAdapter(mAdapter, true);
        if (Track.getTracks(projectID).size() <= 0) {
            mNothingToShow.setVisibility(View.VISIBLE);
        } else {
            mNothingToShow.setVisibility(View.INVISIBLE);
        }
    }

    private void populateTracksIfNecessary() {
        if (Track.getTracks(projectID).size() < 35) {
            Project p = Project.getProject(projectID);
            Log.d(TAG, "Got Project Name: " + p.name);

            String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.d(TAG, "dir: " + newFileName);

            String newForTrack1 = newFileName + "/testaudio.mp3";
            String newForTrack2 = newFileName + "/testaudio2.mp3";

            String name = p.name + " Track 1";
            String filename = "bogus file name";
            Track t = new Track(name, newForTrack1, Track.TYPE_VOCAL, p);
            t.save();

            String name2 = p.name + " Track 2";
            String filename2 = "haha";
            Track t2 = new Track(name2, newForTrack2, Track.TYPE_VOCAL, p);
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

    @Override
    protected void onResume() {
        super.onResume();
        populateRecyclerView();
        initializeMediaThreads();
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (int i = 0; i < mediaThreadList.size(); i++) {
            if (trackEnableList.get(i)) {
                mediaThreadList.get(i).run();
            }
        }
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < mediaThreadList.size(); i++) {
            if (trackEnableList.get(i)) {
                mediaThreadList.get(i).run();
            }
        }
        super.onDestroy();
    }
}
