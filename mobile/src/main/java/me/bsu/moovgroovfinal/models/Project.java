package me.bsu.moovgroovfinal.models;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "Projects")
public class Project extends Model {

    public static final String TAG = "PROJECT_DB_MODEL";

    @Column(name = "name")
    public String name;

    @Column(name = "last_modified")
    public long last_modified;

    public void updateTime() {
        this.last_modified = System.currentTimeMillis();
    }

    public Project() {
        super();
        this.last_modified = System.currentTimeMillis();
    }

    public Project(String name) {
        this.name = name;
        this.last_modified = System.currentTimeMillis();
    }

    public List<Track> tracks() {
        List<Track> projectTracks = getMany(Track.class, "project");
        Log.d(TAG, this.name + " has " + projectTracks.size() + " tracks");
        return projectTracks;
    }

    public static Cursor fetchCursor() {
        String tableName = Cache.getTableInfo(Project.class).getTableName();
        String resultRecords = new Select(tableName + ".*, " + tableName + ".Id as _id")
                .from(Project.class)
                .orderBy("last_modified DESC")
                .toSql();
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, null);
        return resultCursor;
    }

    public static List<Project> getAllProjects() {
        return new Select()
                .from(Project.class)
                .orderBy("last_modified DESC")
                .execute();
    }

    public static Project getProject(long projectID) {
        return new Select().from(Project.class).where("Id = ?", projectID).executeSingle();
    }


}
