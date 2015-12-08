package me.bsu.moovgroovfinal.models;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Tracks")
public class Track extends Model {

    public static final int TYPE_BEAT_LOOP = 0;
    public static final int TYPE_VOCAL = 1;


    public static final String TAG = "TRACKS_DB_MODEL";

    @Column(name = "name")
    public String name;

    @Column(name = "filename")
    public String filename;

    @Column(name = "project")
    public Project project;

    // new attribute that records the type: "beats" or "sound"
    @Column(name = "type")
    public int type;

    public Track() {
        super();
    }

    // modified! add "type" to signature
    public Track(String name, String filename, int type, Project project) {
        this.name = name;
        this.filename = filename;
        this.type = type;
        this.project = project;
    }

    public static Cursor getTracksCursor(long projectID) {
        String tableName = Cache.getTableInfo(Track.class).getTableName();
        // Query all items without any conditions
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id")
                .from(Track.class)
                .where("project = " + projectID)
                .orderBy("name ASC")
                .toSql();
        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    public static List<Track> getTracks(long projectID) {
        return new Select()
                .from(Track.class)
                .where("project = ?", projectID)
                .orderBy("name ASC")
                .execute();
    }

    public List<Timestamp> getTimestamps() {
        List<Timestamp> timestamps = getMany(Timestamp.class, "track");
        return timestamps;
    }
}
