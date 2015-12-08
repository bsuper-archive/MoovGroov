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

    public static final String TAG = "TRACKS_DB_MODEL";

    @Column(name = "name")
    public String name;

    @Column(name = "filename")
    public String filename;

    @Column(name = "project")
    public Project project;
    
    public Track() {
        super();
    }

    public Track(String name, String filename, Project project) {
        this.name = name;
        this.filename = filename;
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
}
