package com.ludgo.android.movies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.ludgo.android.movies.Utility;
import com.ludgo.android.movies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Get movies data from JSON HTTP request
 */
public class MoviesService extends IntentService {

    private final String LOG_TAG = MoviesService.class.getSimpleName();

    public MoviesService() {
        super("Movies");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Network code for fetching JSON String
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesApiString;

        String sortRule = Utility.getSortRule(this);
        String apiKey = Utility.API_KEY;

        try {
            // Construct the URL for the themoviedb.org API query
            // documentation at http://docs.themoviedb.apiary.io
            final String API_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sortRule)
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
                return;
            }
            moviesApiString = buffer.toString();
            getMoviesDataFromJson(moviesApiString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
            return;
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
        final String MOVIE_RESULTS = "results";

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = moviesJson.getJSONArray(MOVIE_RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {
                // These are the values that will be collected for each movie.
                int movie_id;
                String title;
                String overview;
                String poster_path;
                String release_date;
                double vote_average;
                double popularity;

                // Get the JSON object representing one particular movie
                JSONObject aMovie = resultsArray.getJSONObject(i);

                // These are the names of goal values
                final String NAME_ID = "id";
                final String NAME_TITLE = "title";
                final String NAME_OVERVIEW = "overview";
                final String NAME_POSTER_PATH = "poster_path";
                final String NAME_RELEASE_DATE = "release_date";
                final String NAME_VOTE_AVERAGE = "vote_average";
                final String NAME_POPULARITY = "popularity";

                movie_id = aMovie.getInt(NAME_ID);
                title = aMovie.getString(NAME_TITLE);
                overview = aMovie.getString(NAME_OVERVIEW);
                poster_path = aMovie.getString(NAME_POSTER_PATH);
                release_date = aMovie.getString(NAME_RELEASE_DATE);
                vote_average = aMovie.getDouble(NAME_VOTE_AVERAGE);
                popularity = aMovie.getDouble(NAME_POPULARITY);

                // Insert the movie information into the database
                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movie_id);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_TITLE, title);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POSTER_PATH, poster_path);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE, release_date);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE, vote_average);
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POPULARITY, popularity);

                Uri returnedUri = this.getContentResolver().insert(
                        MoviesContract.MoviesEntry.CONTENT_URI, movieValues);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}