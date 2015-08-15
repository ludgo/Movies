package com.ludgo.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by ludgo on 14/08/2015.
 */
public class ImageAdapter extends ArrayAdapter {
    private Context context;
    private String[] imageUrls;
    private LayoutInflater inflater;

    public ImageAdapter(Context context, String[] imageUrls) {
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

        Picasso.with(context)
                .load(imageUrls[position])
                .into(imageView);

        return convertView;
    }
}