package com.ludgo.android.movies;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

    static final String MOVIE_URI = "URI";

    private static int movie_id;

    // Set id for each loader
    private static final int TRAILERS_LOADER = 20;
    private static final int REVIEWS_LOADER = 21;

    // Inflater that will inflate text views to append
    private static LayoutInflater mInflater;

    private static TextView titleTextView;
    private static TextView yearTextView;
    private static TextView favoriteTextView;
    // here is reference to views where text views will be appended
    private static LinearLayout trailersLinearLayout;
    private static LinearLayout reviewsLinearLayout;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        // Attach an intent to this ShareActionProvider
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        Uri movieUri = (arguments != null) ?
                // Case of two pane mode
                (Uri) arguments.getParcelable(DetailFragment.MOVIE_URI) :
                // Case of one pane mode
                getActivity().getIntent().getData();
        String movieIdStr = MoviesContract.getMovieIdStrFromUri(movieUri);
        // Get key variable which is to determine the whole layout of the fragment
        movie_id = Integer.parseInt(movieIdStr);

        mInflater = inflater;

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.title);
        FrameLayout posterContainer = (FrameLayout) rootView.findViewById(R.id.container_poster);
        yearTextView = (TextView) rootView.findViewById(R.id.year);
        TextView voteAverageTextView = (TextView) rootView.findViewById(R.id.voteAverage);
        favoriteTextView = (TextView) rootView.findViewById(R.id.favorite);
        TextView overviewTextView = (TextView) rootView.findViewById(R.id.overview);
        trailersLinearLayout = (LinearLayout) rootView.findViewById(R.id.trailers);
        reviewsLinearLayout = (LinearLayout) rootView.findViewById(R.id.reviews);

        // Pull details to display from database
        Cursor cursor = getActivity().getContentResolver().query(
                MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id),
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
            final String TITLE = cursor.getString(1);
            titleTextView.setText(TITLE);
            if (TITLE.length() > 24) {
                // Shrink default text size
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }

            // set overview
            final String OVERVIEW = cursor.getString(2);
            overviewTextView.setText(OVERVIEW);

            // set poster
            final String POSTER_PATH = cursor.getString(3);
            // 168dp is the width of posterContainer without padding
            int widthInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 168,
                    getResources().getDisplayMetrics());
            String posterUrl = Utility.createPosterUrl(POSTER_PATH, widthInPx);
            // dynamically append ImageView
            ImageView posterImageView = (ImageView) inflater.inflate(R.layout.grid_item, null);
            posterContainer.addView(posterImageView);
            Picasso.with(getActivity())
                    .load(posterUrl)
                    .resize(widthInPx, 0)
                    .into(posterImageView);

            // set year
            final String RELEASE_DATE = cursor.getString(4);
            String year = Utility.createYearFromReleaseDate(RELEASE_DATE);
            yearTextView.setText(year);

            // set vote average
            final double VOTE_AVERAGE = cursor.getDouble(5);
            voteAverageTextView.setText(Double.toString(VOTE_AVERAGE) + "/10");

            // allow user to mark/unmark movie as favorite
            int favorite = cursor.getInt(6);
            if (favorite == 1) {
                favoriteTextView.setSelected(true);
            }
            favoriteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onToggleFavorite();
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

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {
            case TRAILERS_LOADER:
                return new CursorLoader(getActivity(),
                        MoviesContract.TrailersEntry
                                .buildTrailersUriWithId(movie_id),
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
                                .buildReviewsUriWithId(movie_id),
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
                        final String TRAILER_KEY = cursor.getString(2);
                        TextView trailerTextView = (TextView) mInflater
                                .inflate(R.layout.trailer_item, null);
                        trailerTextView.setText(trailerName);
                        trailerTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String trailerUrl = Utility.createYoutubeUrlFromKey(TRAILER_KEY);
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                                startActivity(intent);
                            }
                        });
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

    private static Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        String title = (String) titleTextView.getText();
        String year = (String) yearTextView.getText();
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "Hi! Check out the movie " + title + " (" + year +
                        "). My opinion is " + "/10. #Movies app");
        return shareIntent;
    }

    void fetchTrailers() {
        // Save trailers for this movie in database
        Intent intent = new Intent(getActivity(), TrailersService.class)
                .setData(MoviesContract.MoviesEntry
                        .buildMoviesUriWithId(movie_id));
        getActivity().startService(intent);
    }

    void fetchReviews() {
        // Save reviews for this movie in database after some time
        Intent fetchIntent = new Intent(getActivity(), ReviewsService.FetchReceiver.class)
                .setData(MoviesContract.MoviesEntry
                        .buildMoviesUriWithId(movie_id));
        // Pending intent instead of regular to achieve better performance
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, fetchIntent,
                PendingIntent.FLAG_ONE_SHOT);
        //Set the AlarmManager to wake up the system.
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, pendingIntent);
    }

    private void onToggleFavorite() {
        if (favoriteTextView.isSelected()) {
            favoriteTextView.setSelected(false);
            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITE, 0);
            getActivity().getContentResolver().update(
                    MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id),
                    movieValues,
                    null,
                    null);
        } else {
            favoriteTextView.setSelected(true);
            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITE, 1);
            getActivity().getContentResolver().update(
                    MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id),
                    movieValues,
                    null,
                    null);
        }
    }
}