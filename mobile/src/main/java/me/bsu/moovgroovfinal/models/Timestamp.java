package me.bsu.moovgroovfinal.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Timestamp")
public class Timestamp extends Model {

    @Column(name = "time")
    public long time;

    @Column(name = "track")
    public Track track;

    public Timestamp(Track track, long time) {
        this.track = track;
        this.time = time;
    }

}
