package me.bsu.moovgroovfinal.mixerutils;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

import java.util.List;

import me.bsu.moovgroovfinal.models.Timestamp;

/**
 * Created by kirito on 12/7/15.
 */
public class BeatLoopRunnable implements Runnable{
    MediaPlayer mPlayer;
    CountDownTimer mTimer;
    Context mContext;
    int mResourceID;
    List<Timestamp> mLoop;
    int mBpm;
    boolean isPlaying;


    public BeatLoopRunnable(Context c, int resid, List<Timestamp> lp, int bpm){
        mPlayer = new MediaPlayer();
        mTimer = new CountDownTimer(1000000, 60000/bpm) {
            int count = 0;
            public void onTick(long millisUntilFinished) {

//                if (mPlayer.isPlaying()) {
//                    mPlayer.stop();
//                }
//                if (mLoop[count] == 1) {
//                    mPlayer.start();
//                }
//
//                if (count < 15) {
//                    count++;
//                } else {
//                    count = 0;
//                }

            }
            public void onFinish() {
                count = 0;
                mTimer.start();
            }
        };
        mContext = c;
        mResourceID = resid;
        mLoop = lp;
        mBpm = bpm;
        isPlaying = false;
    }

    @Override
    public void run(){
        mPlayer = MediaPlayer.create(mContext, mResourceID);
        if (isPlaying) {
            isPlaying = false;
            mTimer.cancel();
        } else {
            isPlaying = true;
            mTimer.start();
        }
    }
}
