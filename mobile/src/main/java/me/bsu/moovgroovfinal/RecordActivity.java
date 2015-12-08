package me.bsu.moovgroovfinal;

import android.app.Activity;
        import android.support.annotation.NonNull;
        import android.text.InputType;
        import android.widget.LinearLayout;
        import android.os.Bundle;
        import android.os.Environment;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.content.Context;
        import android.util.Log;
        import android.media.MediaRecorder;
        import android.media.MediaPlayer;

        import com.afollestad.materialdialogs.DialogAction;
        import com.afollestad.materialdialogs.MaterialDialog;

        import java.io.File;
        import java.io.IOException;

// http://developer.android.com/guide/topics/media/audio-capture.html
public class RecordActivity extends Activity
{
    private static final String LOG_TAG = "RecordActivity";
    private static String mFileName = null;

    private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private PlayButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;

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

        // Pop up Dialog to get save filename
        new MaterialDialog.Builder(RecordActivity.this)
                .title("Play Sound Recording")
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
                        String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                        newFileName += "/" + fileName + ".mp4";

                        try {
                            mPlayer.setDataSource(newFileName);
                            mPlayer.prepare();
                            mPlayer.start();
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "prepare() failed");
                        }
                    }
                }).show();




    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;

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
                        String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
                        newFileName += "/" + fileName + ".mp4";
                        File file = new File(mFileName);
                        File file2 = new File(newFileName);
                        boolean success = file.renameTo(file2);
                    }
                }).show();


    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public RecordActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.mp4";
        Log.d("RecordActivity", "Filepath: "+mFileName);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_record);

        LinearLayout ll = new LinearLayout(this);
        mRecordButton = new RecordButton(this);
        ll.addView(mRecordButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        mPlayButton = new PlayButton(this);
        ll.addView(mPlayButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0));
        setContentView(ll);
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

