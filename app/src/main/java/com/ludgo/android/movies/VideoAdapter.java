package com.ludgo.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Populate ListView with trailer rows
 */
public class VideoAdapter extends ArrayAdapter<String> {
    private LayoutInflater inflater;
    private ArrayList<String> videoNames;

    public VideoAdapter(Context context, ArrayList<String> videoNames) {
        super(context, R.layout.trailer_item, videoNames);

        this.videoNames = videoNames;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.trailer_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.trailer);
        textView.setText(videoNames.get(position));

        return convertView;
    }
}