package com.ludgo.android.movies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ludgo.android.movies.data.MoviesContract;
import com.ludgo.android.movies.service.TrailersService;
import com.squareup.picasso.Picasso;


/**
 * All movie nitty-gritty
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailFragment.class.getSimpleName();

    // Key variable which is to determine the whole layout of the fragment
    private int movie_id;

    // Set id for each loader
    private static final int GRID_LOADER = 10;
    private static final int TRAILERS_LOADER = 11;
//    private static final int REVIEWS_LOADER = 12;

    // Indices tied to TRAILERS_LOADER CursorLoader projection to map column index in Cursor
    // Carefully consider any changes!
    public static final int COL_TRAILER_NAME = 1;
    public static final int COL_TRAILER_KEY = 2;
    public static final int COL_MOVIE_TITLE = 3;
    public static final int COL_MOVIE_OVERVIEW = 4;
    public static final int COL_MOVIE_POSTER_PATH = 5;
    public static final int COL_MOVIE_RELEASE_DATE = 6;
    public static final int COL_MOVIE_VOTE_AVERAGE = 7;
    public static final int COL_MOVIE_FAVORITE = 8;

    // Inflater that will inflate text views to append
    LayoutInflater mInflater;

    private TextView titleTextView;
    private ImageView posterImageView;
    private TextView yearTextView;
    private TextView voteAverageTextView;
    private Button favoriteButton;
    private TextView overviewTextView;
    // here is reference to views where text views will be appended
    private LinearLayout trailersLinearLayout;
    private LinearLayout reviewsLinearLayout;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            Uri movieIdUri = intent.getData();
            String movieIdStr = MoviesContract.getMovieIdStrFromUri(movieIdUri);
            movie_id = Integer.parseInt(movieIdStr);
        }

        mInflater = inflater;

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        titleTextView = (TextView) rootView.findViewById(R.id.title);
        posterImageView = (ImageView) rootView.findViewById(R.id.poster);
        yearTextView = (TextView) rootView.findViewById(R.id.year);
        voteAverageTextView = (TextView) rootView.findViewById(R.id.voteAverage);
        favoriteButton = (Button) rootView.findViewById(R.id.favorite);
        overviewTextView = (TextView) rootView.findViewById(R.id.overview);
        trailersLinearLayout = (LinearLayout) rootView.findViewById(R.id.trailers);
        reviewsLinearLayout = (LinearLayout) rootView.findViewById(R.id.reviews);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
//        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchTrailers();
//        fetchReviews();
    }

    void fetchTrailers() {
        // Save trailers for this movie in database
        Intent intent = new Intent(getActivity(), TrailersService.class)
                .setData(MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id));
        getActivity().startService(intent);
    }

//    void fetchReviews() {
//        // Save reviews for this movie in database
//        Intent intent = new Intent(getActivity(), ReviewsService.class);
//        intent.putExtra("movie_id", movie_id);
//        getActivity().startService(intent);
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(getActivity(),
                MoviesContract.TrailersEntry.buildTrailersUriWithId(movie_id),
                // if projection changes, indices must change!
                new String[]{
                        MoviesContract.TrailersEntry.TABLE_NAME + "." + MoviesContract.TrailersEntry._ID,
                        MoviesContract.TrailersEntry.COLUMN_NAME,
                        MoviesContract.TrailersEntry.COLUMN_KEY,
                        // Following works because the MoviesProvider returns movie data joined with
                        // trailers data, even though they're stored in two different tables.
                        MoviesContract.MoviesEntry.COLUMN_TITLE,
                        MoviesContract.MoviesEntry.COLUMN_OVERVIEW,
                        MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
                        MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE,
                        MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
                        MoviesContract.MoviesEntry.COLUMN_FAVORITE},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {

            // set title
            final String title = cursor.getString(COL_MOVIE_TITLE);
            titleTextView.setText(title);
            if (title.length() > 24) {
                // Shrink default text size
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }

            // set overview
            final String overview = cursor.getString(COL_MOVIE_OVERVIEW);
            overviewTextView.setText(overview);

            // set poster
            final String posterPath = cursor.getString(COL_MOVIE_POSTER_PATH);
            String posterUrl = Utility.createUrlFromEnding(posterPath);
            Picasso.with(getActivity())
                    .load(posterUrl)
                    .into(posterImageView);

            // set year
            final String releaseDate = cursor.getString(COL_MOVIE_RELEASE_DATE);
            String year = Utility.createYearFromReleaseDate(releaseDate);
            yearTextView.setText(year);

            // set vote average
            final double voteAverage = cursor.getDouble(COL_MOVIE_VOTE_AVERAGE);
            voteAverageTextView.setText(Double.toString(voteAverage));

            // allow user to mark movie as favorite
            int favorite = cursor.getInt(COL_MOVIE_FAVORITE);
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Change appropriate column in the database
                    favoriteButton.setText("IN");
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(MoviesContract.MoviesEntry.COLUMN_FAVORITE, 1);
                    int rowsUpdated = getActivity().getContentResolver().update(
                            MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id),
                            movieValues,
                            null,
                            null);
                }
            });

            // append all movie trailers to linear layout
            do {
                String trailerName = cursor.getString(COL_TRAILER_NAME);
                TextView trailerTextView = (TextView) mInflater.inflate(R.layout.trailer_item, null);
                trailerTextView.setText(trailerName);
                trailersLinearLayout.addView(trailerTextView);
            } while (cursor.moveToNext());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) { }


//    /**
//     * Get reviews data from JSON HTTP request
//     */
//    public class FetchReviewsTask extends AsyncTask<Void, Void, Hashtable[]> {
//
//        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();
//
//        /**
//         * Take the String representing the complete reviews in JSON Format and
//         * pull out the data needed to construct the wireframes.
//         */
//        private Hashtable[] getReviewDataFromJson(String reviewsJsonStr)
//                throws JSONException {
//
//            // These are the names of the JSON objects that need to be extracted.
//            final String MOVIE_RESULTS = "results";
//
//            try {
//                JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
//                JSONArray resultsArray = reviewsJson.getJSONArray(MOVIE_RESULTS);
//
//                // All themoviedb.org reviews and their authors will be stored here
//                Hashtable[] allFetchedReviews = new Hashtable[resultsArray.length()];
//
//                for (int i = 0; i < resultsArray.length(); i++) {
//                    // These are the values that will be collected for each video.
//                    String reviewContent;
//                    String reviewAuthor;
//
//                    // Get the JSON object representing one particular video
//                    JSONObject aReview = resultsArray.getJSONObject(i);
//
//                    reviewContent = aReview.getString(REVIEW_CONTENT);
//                    reviewAuthor = aReview.getString(REVIEW_AUTHOR);
//
//                    Hashtable reviewDetails = new Hashtable(2);
//
//                    reviewDetails.put(REVIEW_CONTENT, reviewContent);
//                    reviewDetails.put(REVIEW_AUTHOR, reviewAuthor);
//
//                    allFetchedReviews[i] = reviewDetails;
//                }
//
//                return allFetchedReviews;
//
//            } catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected Hashtable[] doInBackground(Void... params) {
//
//            // Network code for fetching JSON String
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            // Will contain the raw JSON response as a string.
//            String reviewApiString = null;
//
//            String apiKey = Utility.API_KEY;
//
//            try {
//                // Construct the URL for the themoviedb.org API query
//                // documentation at http://docs.themoviedb.apiary.io
//                final String API_BASE_URL =
//                        "http://api.themoviedb.org/3/movie/" + movie_id + "/reviews?";
//                final String KEY_PARAM = "api_key";
//
//                Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
//                        .appendQueryParameter(KEY_PARAM, apiKey)
//                        .build();
//
//                URL url = new URL(builtUri.toString());
//
//                // Create the request to themoviedb.org API, and open the connection
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                // Read the input stream into a String
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do without stream.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    // Adding a newline (without effect on parsing) for debugging purposes.
//                    buffer.append(line + "\n");
//                }
//
//                if (buffer.length() == 0) {
//                    // Stream was empty.  No point in parsing.
//                    return null;
//                }
//                reviewApiString = buffer.toString();
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Error ", e);
//                // If the code didn't successfully get the movie data, there's no point in attempting
//                // to parse it.
//                return null;
//            } finally {
//                if (urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if (reader != null) {
//                    try {
//                        reader.close();
//                    } catch (final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//
//            try {
//                return getReviewDataFromJson(reviewApiString);
//            } catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//
//            // This will only happen if there was an error getting or parsing JSON
//            return null;
//        }
//    }
}