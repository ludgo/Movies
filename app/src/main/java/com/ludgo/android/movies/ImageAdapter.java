package com.ludgo.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Created by ludgo on 14/08/2015.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;
    private final String[] imageStrValues;

    public ImageAdapter(Context context, String[] imageStrValues) {
        this.context = context;
        this.imageStrValues = imageStrValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = inflater.inflate(R.layout.grid_item, null);

            ImageView imageView = (ImageView) gridView
                    .findViewById(R.id.grid_image);

            String imageStr = imageStrValues[position];

            if (imageStr.equals("untitled1")) {
                imageView.setImageResource(R.drawable.untitled1);
            } else if (imageStr.equals("untitled2")) {
                imageView.setImageResource(R.drawable.untitled2);
            } else if (imageStr.equals("untitled3")) {
                imageView.setImageResource(R.drawable.untitled3);
            } else if (imageStr.equals("untitled4")) {
                imageView.setImageResource(R.drawable.untitled4);
            }

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }

    @Override
    public int getCount() {
        return imageStrValues.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}