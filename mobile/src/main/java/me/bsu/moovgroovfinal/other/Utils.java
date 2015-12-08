package me.bsu.moovgroovfinal.other;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import me.bsu.moovgroovfinal.sound.Sound;
import me.bsu.moovgroovfinal.sound.SoundPlayer;
import me.bsu.moovgroovfinal.sound.SoundStore;

public class Utils {
    public static String convertUnixTimestampToLocalTimestampString(long unixTS) {
        Date date = new Date(unixTS);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(getUserTimezone());
        return sdf.format(date);
    }

    public static TimeZone getUserTimezone() {
        return SimpleTimeZone.getDefault();
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

        Sound sound = soundArray[0];
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
}
