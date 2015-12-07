package me.bsu.moovgroovfinal.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.bsu.moovgroovfinal.R;
import me.bsu.moovgroovfinal.other.CursorRecyclerAdapter;

public class TracksListCursorAdapter extends CursorRecyclerAdapter<TracksListCursorAdapter.ViewHolder> {

    public static final String TAG = "TRACKSLISTCURSORADAPTER";

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public ViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.list_item_name);
        }
    }

    public Cursor cursor;
    public Context context;

    public TracksListCursorAdapter(Cursor cursor, Context context) {
        super(cursor);
        this.cursor = cursor;
        this.context = context;
        cursor.moveToFirst();
        for (String columnName : cursor.getColumnNames()) {
            Log.d(TAG, "Column Name: " + columnName);
            Log.d(TAG, "Index: " + cursor.getColumnIndexOrThrow(columnName));
        }
        Log.d(TAG, cursor.getCount() + " items");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        holder.name.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        Log.d(TAG, cursor.getString(cursor.getColumnIndexOrThrow("name")));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_tracks, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }
}
