package me.bsu.moovgroovfinal.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.bsu.moovgroovfinal.R;
import me.bsu.moovgroovfinal.other.CursorRecyclerAdapter;
import me.bsu.moovgroovfinal.other.Utils;

public class ProjectsListCursorAdapter extends CursorRecyclerAdapter<ProjectsListCursorAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name, lastModified;
        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.list_item_name);
            lastModified = (TextView) v.findViewById(R.id.list_item_last_modified);
        }
    }

    public Cursor cursor;
    public Context context;

    public ProjectsListCursorAdapter(Cursor cursor, Context context) {
        super(cursor);
        this.cursor = cursor;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        holder.name.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        holder.lastModified.setText(Utils.convertUnixTimestampToLocalTimestampString(cursor.getLong(cursor.getColumnIndexOrThrow("last_modified"))));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_project, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


}
