package me.bsu.moovgroovfinal.mixerutils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

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
                mPlayer = MediaPlayer.create(mContext, Uri.parse(mDirectory));
                mPlayer.setLooping(true);
                mPlayer.start();
            }
        } else {
            mPlayer = MediaPlayer.create(mContext, Uri.parse(mDirectory));
        }
    }
}
