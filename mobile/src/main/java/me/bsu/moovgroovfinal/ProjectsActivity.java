package me.bsu.moovgroovfinal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import me.bsu.moovgroovfinal.adapters.ProjectsListCursorAdapter;
import me.bsu.moovgroovfinal.models.Project;
import me.bsu.moovgroovfinal.models.Timestamp;
import me.bsu.moovgroovfinal.models.Track;
import me.bsu.moovgroovfinal.other.RecyclerItemClickListener;

public class ProjectsActivity extends AppCompatActivity {

    public static final String TAG = "PROJECTS_ACTIVITY";

    public RecyclerView mRecyclerView;
    public ProjectsListCursorAdapter mProjectsAdapter;

    public LinearLayout mNothingToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNothingToShow = (LinearLayout) findViewById(R.id.proj_nothing_to_show);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                new MaterialDialog.Builder(ProjectsActivity.this)
                        .title("Create New Project")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("Project Name", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                // Do something
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Log.d(TAG, "EDITEXT: %s".format(dialog.getInputEditText().getText().toString()));
                                new Project(dialog.getInputEditText().getText().toString()).save();
                                populateRecyclerView();
                                startTracksActivity(0);
                            }
                        }).show();
            }
        });

        //deleteEverything();

        //populateDBifNecessary();
        setupRecyclerView();
        populateRecyclerView();

    }

    private void setupRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.listview_projects);
        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, String.format("%d clicked", position));
                final TextView txtDelete = (TextView) view.findViewById(R.id.project_delete);
                if (txtDelete.isEnabled()) {
                    txtDelete.callOnClick();
                    txtDelete.setEnabled(false);
                    txtDelete.setVisibility(View.INVISIBLE);
                    populateRecyclerView();
                } else {
                    startTracksActivity(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.d(TAG, String.format("%d long press", position));
                Project p = Project.getAllProjects().get(position);
                final long pid = p.getId();

                new AlertDialog.Builder(ProjectsActivity.this)
                        .setTitle("Delete Project")
                        .setMessage("Are you sure you want to delete this project?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                Log.d("DELETE PROJECT", "delete button pressed");
                                deleteProject(pid);
                                populateRecyclerView();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.icon_delete)
                        .show();
            }
        }));
    }

    private void populateRecyclerView() {
        mProjectsAdapter = new ProjectsListCursorAdapter(Project.fetchCursor(), this);
        mRecyclerView.swapAdapter(mProjectsAdapter, true);
        if (Project.getAllProjects().size() <= 0) {
            mNothingToShow.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            mNothingToShow.setVisibility(View.INVISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void populateDBifNecessary() {
        List<Project> projects = Project.getAllProjects();
        if (projects.size() == 0) {
            Project p1 = new Project("Angela's Jam");
            Project p2 = new Project("Nisha's Tunez");
            Project p3 = new Project("Brian's Beatzz");
            p1.save();
            p2.save();
            p3.save();
        }
    }

    private void startTracksActivity(int projectIndex) {
        Project p = Project.getAllProjects().get(projectIndex);
        Intent intent = new Intent(this, TracksActivity.class);
        intent.putExtra(TracksActivity.INTENT_PROJECT_ID, p.getId());
        startActivity(intent);
    }

    private void deleteProject(long projectId) {

        Project p = Project.getProject(projectId);
        Log.d(TAG, "Project name: "+p.name +", id: "+p.getId());

        List<Track> tracks =  p.tracks();

        for (int i = 0; i < tracks.size(); i++) {
            Log.d(TAG, "Examing track: "+tracks.get(i).name);

            if (tracks.get(i).type == Track.TYPE_BEAT_LOOP) {
                new Delete().from(Timestamp.class).where("track = ?", tracks.get(i).getId()).execute();
                Log.d(TAG, "timestamps deleted from " + tracks.get(i).name);
            }

        }

        new Delete().from(Track.class).where("project = ?", projectId).execute();

        new Delete().from(Project.class).where("Id = ?", projectId).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_projects, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            deleteEverything();
            Log.d(TAG, "Database Reset!");
            populateRecyclerView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteEverything() {
        new Delete().from(Timestamp.class).execute();
        new Delete().from(Track.class).execute();
        new Delete().from(Project.class).execute();
    }
}
