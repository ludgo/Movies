package com.ludgo.android.movies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ludgo.android.movies.data.MoviesContract;
import com.ludgo.android.movies.service.ReviewsService;
import com.ludgo.android.movies.service.TrailersService;
import com.squareup.picasso.Picasso;


/**
 * All movie nitty-gritty
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    // Set id for each loader
    private static final int TRAILERS_LOADER = 20;
    private static final int REVIEWS_LOADER = 21;

    // Inflater that will inflate text views to append
    LayoutInflater mInflater;

    Button favoriteButton;
    // here is reference to views where text views will be appended
    private LinearLayout trailersLinearLayout;
    private LinearLayout reviewsLinearLayout;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mInflater = inflater;

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.title);
        ImageView posterImageView = (ImageView) rootView.findViewById(R.id.poster);
        TextView yearTextView = (TextView) rootView.findViewById(R.id.year);
        TextView voteAverageTextView = (TextView) rootView.findViewById(R.id.voteAverage);
        favoriteButton = (Button) rootView.findViewById(R.id.favorite);
        TextView overviewTextView = (TextView) rootView.findViewById(R.id.overview);
        trailersLinearLayout = (LinearLayout) rootView.findViewById(R.id.trailers);
        reviewsLinearLayout = (LinearLayout) rootView.findViewById(R.id.reviews);

        // Pull details to display from database
        Cursor cursor = getActivity().getContentResolver().query(
                MoviesContract.MoviesEntry.buildMoviesUriWithId(DetailActivity.getMovieId()),
                // if projection changes, indices must change!
                new String[]{MoviesContract.MoviesEntry.TABLE_NAME + "." +
                        MoviesContract.MoviesEntry._ID,  // 0
                        MoviesContract.MoviesEntry.COLUMN_TITLE,  // 1
                        MoviesContract.MoviesEntry.COLUMN_OVERVIEW,  // 2
                        MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,  // 3
                        MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, // 4
                        MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,  // 5
                        MoviesContract.MoviesEntry.COLUMN_FAVORITE},  // 6
                null,
                null,
                null);

        // This movie should be in database, since its id got here from grid where the movie was
        if (cursor.moveToFirst()) {
            // set title
            final String title = cursor.getString(1);
            titleTextView.setText(title);
            if (title.length() > 24) {
                // Shrink default text size
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }

            // set overview
            final String overview = cursor.getString(2);
            overviewTextView.setText(overview);

            // set poster
            final String posterPath = cursor.getString(3);
            String posterUrl = Utility.createUrlFromEnding(posterPath);
            Picasso.with(getActivity())
                    .load(posterUrl)
                    .into(posterImageView);

            // set year
            final String releaseDate = cursor.getString(4);
            String year = Utility.createYearFromReleaseDate(releaseDate);
            yearTextView.setText(year);

            // set vote average
            final double voteAverage = cursor.getDouble(5);
            voteAverageTextView.setText(Double.toString(voteAverage));

            // allow user to mark movie as favorite
            int favorite = cursor.getInt(6);
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Change appropriate column in the database
                    favoriteButton.setText("IN");
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITE, 1);
                    getActivity().getContentResolver().update(
                            MoviesContract.MoviesEntry.buildMoviesUriWithId(DetailActivity.getMovieId()),
                            movieValues,
                            null,
                            null);
                }
            });
        }
        cursor.close();

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchTrailers();
        fetchReviews();
    }

    void fetchTrailers() {
        // Save trailers for this movie in database
        Intent intent = new Intent(getActivity(), TrailersService.class)
                .setData(MoviesContract.MoviesEntry
                        .buildMoviesUriWithId(DetailActivity.getMovieId()));
        getActivity().startService(intent);
    }

    void fetchReviews() {
        // Save reviews for this movie in database
        Intent intent = new Intent(getActivity(), ReviewsService.class)
                .setData(MoviesContract.MoviesEntry
                        .buildMoviesUriWithId(DetailActivity.getMovieId()));
        getActivity().startService(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {
            case TRAILERS_LOADER:
                return new CursorLoader(getActivity(),
                        MoviesContract.TrailersEntry
                                .buildTrailersUriWithId(DetailActivity.getMovieId()),
                        // if projection changes, indices must change!
                        new String[]{MoviesContract.TrailersEntry.TABLE_NAME + "." +
                                MoviesContract.TrailersEntry._ID, // 0
                                MoviesContract.TrailersEntry.COLUMN_NAME,  // 1
                                MoviesContract.TrailersEntry.COLUMN_KEY},  // 2
                        null,
                        null,
                        null);
            case REVIEWS_LOADER:
                return new CursorLoader(getActivity(),
                        MoviesContract.ReviewsEntry
                                .buildReviewsUriWithId(DetailActivity.getMovieId()),
                        // if projection changes, indices must change!
                        new String[]{MoviesContract.ReviewsEntry.TABLE_NAME + "." +
                                MoviesContract.ReviewsEntry._ID, // 0
                                MoviesContract.ReviewsEntry.COLUMN_AUTHOR,  // 1
                                MoviesContract.ReviewsEntry.COLUMN_CONTENT},  // 2
                        null,
                        null,
                        null);
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            switch (loader.getId()) {
                case TRAILERS_LOADER:
                    trailersLinearLayout.removeAllViews();
                    // append all movie trailers to linear layout
                    do {
                        String trailerName = cursor.getString(1);
                        String trailerKey = cursor.getString(2);
                        TextView trailerTextView = (TextView) mInflater
                                .inflate(R.layout.trailer_item, null);
                        trailerTextView.setText(trailerName);
                        trailersLinearLayout.addView(trailerTextView);
                    } while (cursor.moveToNext());
                    break;
                case REVIEWS_LOADER:
                    reviewsLinearLayout.removeAllViews();
                    // append all movie reviews to linear layout
                    do {
                        String reviewAuthor = cursor.getString(1);
                        String reviewContent = cursor.getString(2);
                        String wholeReview = reviewContent + " <b>(" + reviewAuthor + ")</b>";
                        TextView reviewTextView = (TextView) mInflater
                                .inflate(R.layout.review_item, null);
                        reviewTextView.setText(Html.fromHtml(wholeReview));
                        reviewsLinearLayout.addView(reviewTextView);
                    } while (cursor.moveToNext());
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
    }
}