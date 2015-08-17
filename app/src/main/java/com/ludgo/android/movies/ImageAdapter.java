package com.ludgo.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Populate GridView with ImageView poster images
 */
public class ImageAdapter extends ArrayAdapter<String> {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<String> imageUrls;

    public ImageAdapter(Context context, ArrayList<String> imageUrls) {
        super(context, R.layout.grid_item, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = (ImageView) convertView;

        // Helper Picasso library
        Picasso.with(context)
                .load(imageUrls.get(position))
                .resize(250,0)
                .into(imageView);

        return convertView;
    }
}