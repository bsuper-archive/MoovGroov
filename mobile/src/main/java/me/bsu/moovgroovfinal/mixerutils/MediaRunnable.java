package me.bsu.moovgroovfinal.mixerutils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

/**
 * Created by kirito on 12/7/15.
 */
public class MediaRunnable implements Runnable{

    MediaPlayer mPlayer;
    Context mContext;
    String mDirectory;

    public MediaRunnable(Context c, String dir){
        mPlayer = new MediaPlayer();
        mContext = c;
        mDirectory = dir;
    }

    @Override
    public void run() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.reset();
            } else {
                mPlayer.reset();
                try {
                    mPlayer = MediaPlayer.create(mContext, Uri.parse(mDirectory));
                    mPlayer.prepare();
                } catch (IOException | IllegalArgumentException | SecurityException e) {
                    Log.e("MAIN", e.getMessage());
                }
                mPlayer.start();
            }
        } else {
            mPlayer = MediaPlayer.create(mContext, Uri.parse(mDirectory));
        }
    }
}
