package me.bsu.moovgroovfinal.mixerutils;

import android.content.Context;
import android.os.CountDownTimer;

import java.util.ArrayList;

import me.bsu.moovgroovfinal.sound.Sound;
import me.bsu.moovgroovfinal.sound.SoundPlayer;
import me.bsu.moovgroovfinal.sound.SoundStore;

/**
 * Created by kirito on 12/7/15.
 * Sound playing
 */
public class BeatLoopRunnable implements Runnable{
    Context mContext;
    ArrayList<Integer> mLoop;
    boolean isPlaying;
    int totalTime;
    int currTime;
    int currBeat;
    CountDownTimer mTimer;

    SoundPlayer mSoundPlayer;
    Sound[] soundArray;

    public BeatLoopRunnable(Context c, ArrayList<Integer> lp){

        mContext = c;
        mLoop = lp;
        isPlaying = false;
        totalTime = lp.get(lp.size()-1);

        currTime = 0;
        currBeat = 0;

        mSoundPlayer = new SoundPlayer(c);
        soundArray = SoundStore.getSounds(c);

        createTimer();
    }

    private void createTimer() {
        mTimer = new CountDownTimer(totalTime, 1) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (currTime == mLoop.get(currBeat)) {
                    mSoundPlayer.playSound(soundArray[1]);
                    currBeat++;
                }
                currTime++;
            }

            @Override
            public void onFinish() {
                mSoundPlayer.playSound(soundArray[1]);
                currTime = 0;
                currBeat = 0;
                mTimer.start();
            }
        };
    }

    @Override
    public void run(){
        if (isPlaying) {
            isPlaying = false;
            mTimer.cancel();
        } else {
            isPlaying = true;
            mTimer.start();
        }
    }
}
