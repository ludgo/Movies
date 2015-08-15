package com.ludgo.android.movies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] imageUrls = new String[]{
                "http://image.tmdb.org/t/p/w342/7SGGUiTE6oc2fh9MjIk5M00dsQd.jpg",
                "http://image.tmdb.org/t/p/w342/5JU9ytZJyR3zmClGmVm9q4Geqbd.jpg",
                "http://image.tmdb.org/t/p/w342/kqjL17yufvn9OVLyXYpvtyrFfak.jpg",
                "http://image.tmdb.org/t/p/w342/yUlpRbbrac0GTNHZ1l20IHEcWAN.jpg",
                "http://image.tmdb.org/t/p/w342/aBBQSC8ZECGn6Wh92gKDOakSC8p.jpg",
                "http://image.tmdb.org/t/p/w342/uXZYawqUsChGSj54wcuBtEdUJbh.jpg",
                "http://image.tmdb.org/t/p/w342/aAmfIX3TT40zUHGcCKrlOZRKC7u.jpg"};

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
            gridView.setNumColumns(3);
        } else if (orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(2);
        }
        gridView.setAdapter(new ImageAdapter(getActivity(), imageUrls));

        return rootView;
    }
}
