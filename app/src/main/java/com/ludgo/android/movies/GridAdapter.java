package com.ludgo.android.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * {@link GridAdapter} exposes a grid of movie posters from
 * a {@link android.database.Cursor} to a {@link android.widget.GridView}.
 */
public class GridAdapter extends CursorAdapter {

    private int widthInPx;

    public GridAdapter(Context context, Cursor c, int flags, int itemWidth) {
        super(context, c, flags);
        widthInPx = itemWidth;
    }

    /*
        These views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
    }

    /*
        Fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String posterPath = cursor.getString(GridFragment.COL_POSTER_PATH);
        String posterUrl = Utility.createPosterUrl(posterPath, widthInPx);
        Picasso.with(context)
                .load(posterUrl)
                .resize(widthInPx, 0)
                .into((ImageView) view);
    }
}