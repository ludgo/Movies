package com.ludgo.android.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String[] movieDetail = intent.getStringArrayExtra(Intent.EXTRA_TEXT);

            // set title
            TextView title = (TextView) rootView.findViewById(R.id.title);
            title.setText(movieDetail[0]);
            if (movieDetail[0].length() < 25) {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
            } else {
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }

            // set overview
            ((TextView) rootView.findViewById(R.id.overview)).setText(movieDetail[1]);

            // set voteAverage
            ((TextView) rootView.findViewById(R.id.voteAverage)).setText(movieDetail[2] + "/10");

            // set year
            ((TextView) rootView.findViewById(R.id.year)).setText(
                    Utility.createYearFromReleaseDate(movieDetail[3])
            );

            // set poster image
            ImageView imageView = (ImageView) rootView.findViewById(R.id.poster);
            String posterUrl = Utility.createUrlFromEnding(movieDetail[4]);
            Picasso.with(getActivity())
                    .load(posterUrl)
                    .into(imageView);
        }

        return rootView;
    }
}
