package me.bsu.moovgroovfinal.mixerutils;

import android.content.Context;

import java.util.ArrayList;

import me.bsu.moovgroovfinal.other.Utils;
import me.bsu.moovgroovfinal.sound.Sound;
import me.bsu.moovgroovfinal.sound.SoundPlayer;
import me.bsu.moovgroovfinal.sound.SoundStore;

/**
 * Created by kirito on 12/7/15.
 */
public class BeatLoopRunnable implements Runnable{
    Context mContext;
    ArrayList<Integer> mLoop;
    boolean isPlaying;

    public BeatLoopRunnable(Context c, ArrayList<Integer> lp){
        mContext = c;
        mLoop = lp;
        isPlaying = false;
    }
    public static void playBeats(Context context, ArrayList<Integer> list) {

        SoundPlayer mSoundPlayer= new SoundPlayer(context);
        Sound[] soundArray = SoundStore.getSounds(context);

        ArrayList<Integer> deltas = new ArrayList<>();
        Integer prev = list.get(0);
        for (int i = 1; i<list.size(); i++){
            deltas.add(list.get(i)-prev);
            prev = list.get(i);
        }

        Sound sound = soundArray[1];
        mSoundPlayer.playSound(sound);

        for (int j = 0; j<deltas.size(); j++){
            try {
                Thread.sleep(deltas.get(j));                 //1000 milliseconds is one second.
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            mSoundPlayer.playSound(sound);
        }

    }

    @Override
    public void run(){
        if (!isPlaying) {
            Utils.playBeats(mContext, mLoop);
        }
    }
}
