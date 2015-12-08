package me.bsu.moovgroovfinal;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;

import me.bsu.moovgroovfinal.models.Project;
import me.bsu.moovgroovfinal.models.Track;

// http://developer.android.com/guide/topics/media/audio-capture.html
public class RecordActivity extends Activity {
    private static final String TAG = "RecordActivity";
    private static String tempFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.mp4";
    private String mFilePath = "";

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private Button   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    private long projectID;


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        if (!mFilePath.equals("")) {
            try {
                mPlayer.setDataSource(mFilePath);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.d(TAG, "Cannot get file");
            }
        } else {
            Log.d(TAG, "No file created yet");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(tempFilename);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        saveTempFile();
    }

    private void saveTempFile() {
        // Pop up Dialog to get save filename
        new MaterialDialog.Builder(RecordActivity.this)
                .title("Save Sound Recording")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("Sound Filename", "", new MaterialDialog.InputCallback() {
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
                        mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                        mFilePath += "/" + fileName + ".mp4";
                        File file = new File(tempFilename);
                        File file2 = new File(mFilePath);
                        boolean success = file.renameTo(file2);

                        Track t = new Track(fileName, mFilePath, Track.TYPE_VOCAL, Project.getProject(projectID));
                        t.save();
                    }
                }).show();
    }

    private void setupButtons() {
        mRecordButton = (Button) findViewById(R.id.record_button);
        mPlayButton = (Button) findViewById(R.id.play_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText("Stop recording");
                } else {
                    mRecordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButton.setText("Stop playing");
                } else {
                    mPlayButton.setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        });
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_record);

        projectID = getIntent().getLongExtra(TracksActivity.INTENT_PROJECT_ID, 0);

        setupButtons();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}

//    public RecordActivity() {
//        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFileName += "/audiorecordtest.mp4";
//        Log.d("RecordActivity", "Filepath: "+mFileName);
//    }

//    class RecordButton extends Button {
//        boolean mStartRecording = true;
//
//        OnClickListener clicker = new OnClickListener() {
//            public void onClick(View v) {
//                onRecord(mStartRecording);
//                if (mStartRecording) {
//                    setText("Stop recording");
//                } else {
//                    setText("Start recording");
//                }
//                mStartRecording = !mStartRecording;
//            }
//        };
//
//        public RecordButton(Context ctx) {
//            super(ctx);
//            setText("Start recording");
//            setOnClickListener(clicker);
//        }
//    }
//
//    class PlayButton extends Button {
//        boolean mStartPlaying = true;
//
//        OnClickListener clicker = new OnClickListener() {
//            public void onClick(View v) {
//                onPlay(mStartPlaying);
//                if (mStartPlaying) {
//                    setText("Stop playing");
//                } else {
//                    setText("Start playing");
//                }
//                mStartPlaying = !mStartPlaying;
//            }
//        };
//
//        public PlayButton(Context ctx) {
//            super(ctx);
//            setText("Start playing");
//            setOnClickListener(clicker);
//        }
//    }

