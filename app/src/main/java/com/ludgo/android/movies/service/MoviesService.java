package com.ludgo.android.movies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.util.Log;

import com.ludgo.android.movies.R;
import com.ludgo.android.movies.Utility;
import com.ludgo.android.movies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Get movies data from JSON HTTP request
 */
public class MoviesService extends IntentService {

    private final String LOG_TAG = MoviesService.class.getSimpleName();

    public static final String PAGE_EXTRA = "page_extra";

    // Annotated interface to provide server status integer description
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOVIES_STATUS_LOADING,
            MOVIES_STATUS_OK,
            MOVIES_STATUS_SERVER_DOWN,
            MOVIES_STATUS_SERVER_INVALID,
            MOVIES_STATUS_UNKNOWN})
    public @interface MoviesStatus {}

    public static final int MOVIES_STATUS_LOADING = 0;
    public static final int MOVIES_STATUS_OK = 1;
    public static final int MOVIES_STATUS_SERVER_DOWN = 2;
    public static final int MOVIES_STATUS_SERVER_INVALID = 3;
    public static final int MOVIES_STATUS_UNKNOWN = 4;

    public MoviesService() {
        super("MoviesService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // At the beginning, 'reset' server status which notifies that fetching isn't completed
        setMoviesStatus(this, MOVIES_STATUS_LOADING);

        // Network code for fetching JSON String
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesApiString;

        String sortRule = Utility.getSortRule(this);
        String year = Utility.getPreferredYear(this);
        String pageStr = "1";
        if (intent.hasExtra(PAGE_EXTRA)){
            pageStr = intent.getStringExtra(PAGE_EXTRA);
        }
        String apiKey = Utility.API_KEY;

        try {
            // Construct the URL for the themoviedb.org API query
            // documentation at http://docs.themoviedb.apiary.io
            final String API_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String YEAR_PARAM = "primary_release_year";
            final String PAGE_PARAM = "page";
            final String KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortRule)
                    .appendQueryParameter(YEAR_PARAM, year)
                    .appendQueryParameter(PAGE_PARAM, pageStr)
                    .appendQueryParameter(KEY_PARAM, apiKey)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to themoviedb.org API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do without stream.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Adding a newline (without effect on parsing) for debugging purposes.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                setMoviesStatus(this, MOVIES_STATUS_SERVER_DOWN);
                return;
            }
            moviesApiString = buffer.toString();
            getMoviesDataFromJson(moviesApiString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
            setMoviesStatus(this, MOVIES_STATUS_SERVER_DOWN);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setMoviesStatus(this, MOVIES_STATUS_SERVER_INVALID);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    /**
     * Take the String representing the complete movies in JSON Format and
     * pull out the data needed to construct the wireframes.
     */
    private void getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String NAME_RESULTS = "results";

        // These are the names of goal values
        final String NAME_ID = "id";
        final String NAME_TITLE = "title";
        final String NAME_OVERVIEW = "overview";
        final String NAME_POSTER_PATH = "poster_path";
        final String NAME_RELEASE_DATE = "release_date";
        final String NAME_VOTE_AVERAGE = "vote_average";
        final String NAME_POPULARITY = "popularity";

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(NAME_RESULTS);

            // Collect new information
            Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(resultsArray.length());

            for (int i = 0; i < resultsArray.length(); i++) {

                // Get the JSON object representing one particular movie
                JSONObject aMovie = resultsArray.getJSONObject(i);

                // These are the values that will be collected for each movie.
                int movie_id;
                String title;
                String overview;
                String poster_path;
                String release_date;
                double vote_average;
                double popularity;

                movie_id = aMovie.getInt(NAME_ID);
                title = aMovie.getString(NAME_TITLE);
                overview = aMovie.getString(NAME_OVERVIEW);
                poster_path = aMovie.getString(NAME_POSTER_PATH);
                release_date = aMovie.getString(NAME_RELEASE_DATE);
                vote_average = aMovie.getDouble(NAME_VOTE_AVERAGE);
                popularity = aMovie.getDouble(NAME_POPULARITY);

                if (movie_id != 0 &&
                        title != null &&
                        overview != null &&
                        poster_path != null &&
                        release_date != null &&
                        vote_average != 0.0d &&
                        popularity != 0.0d) {

                    // First, check if the movie with this id exists in the db
                    Cursor checkCursor = this.getContentResolver().query(
                            MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id),
                            null,
                            null,
                            null,
                            null);
                    if (!checkCursor.moveToFirst()) {
                        // A new row
                        ContentValues movieValues = new ContentValues();

                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie_id);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, poster_path);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, release_date);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, vote_average);
                        movieValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, popularity);

                        contentValuesVector.add(movieValues);
                    }
                    checkCursor.close();
                }
            }

            // Insert new information into the database
            // Note: Theoretically, there are 20 movie entries per fetched page. Considering
            // not null requirements, however, it could be less. But it is still enough
            // to populate 12-item grid view
            if ( contentValuesVector.size() > 0 ) {
                ContentValues[] rowsArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(rowsArray);
                this.getContentResolver()
                        .bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, rowsArray);
            }
            setMoviesStatus(this, MOVIES_STATUS_OK);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            setMoviesStatus(this, MOVIES_STATUS_SERVER_INVALID);
        }
    }

    /**
     * Saves the movies server response status code into shared preference. Should not be called
     * from the UI thread because it uses commit and not apply.
     * @param moviesStatus The IntDef value to set
     */
    static private void setMoviesStatus(Context context, @MoviesStatus int moviesStatus){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(context.getString(R.string.pref_movies_status_key), moviesStatus);
        spe.commit();
    }
}