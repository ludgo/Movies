package com.ludgo.android.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * {@link PopularAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.GridView}.
 */
public class PopularAdapter extends CursorAdapter {

    public PopularAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
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

        ImageView imageView = (ImageView) view;

        String posterPath = cursor.getString(GridFragment.COL_POSTER_PATH);
        String posterUrl = Utility.createUrlFromEnding(posterPath);

        Picasso.with(context)
                .load(posterUrl)
                .resize(250, 0)
                .into(imageView);
    }
}