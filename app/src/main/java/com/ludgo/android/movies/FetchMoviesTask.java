package com.ludgo.android.movies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;

/**
 * Get String data from JSON HTTP request
 */
public class FetchMoviesTask extends AsyncTask <String, Void, Hashtable[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private ImageAdapter mImageAdapter;

    public FetchMoviesTask(ImageAdapter imageAdapter) {
        mImageAdapter = imageAdapter;
    }

    /**
     * Take the String representing the complete movies in JSON Format and
     * pull out the data needed to construct the wireframes.
     */
    private Hashtable[] getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_RESULTS = "results";

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultsArray = movieJson.getJSONArray(MOVIE_RESULTS);

            // All necessary data for UX will be stored here
            Hashtable[] allFetchedMovies = new Hashtable[resultsArray.length()];

            for(int i = 0; i < resultsArray.length(); i++) {
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

                movie_id = aMovie.getInt(MainActivityFragment.MOVIE_ID);
                title = aMovie.getString(MainActivityFragment.MOVIE_TITLE);
                overview = aMovie.getString(MainActivityFragment.MOVIE_OVERVIEW);
                poster_path = aMovie.getString(MainActivityFragment.MOVIE_POSTER_PATH);
                release_date = aMovie.getString(MainActivityFragment.MOVIE_RELEASE_DATE);
                vote_average = aMovie.getDouble(MainActivityFragment.MOVIE_VOTE_AVERAGE);
                popularity = aMovie.getDouble(MainActivityFragment.MOVIE_POPULARITY);

                Hashtable movieDetails = new Hashtable(7);

                movieDetails.put(MainActivityFragment.MOVIE_ID, movie_id);
                movieDetails.put(MainActivityFragment.MOVIE_TITLE, title);
                movieDetails.put(MainActivityFragment.MOVIE_OVERVIEW, overview);
                movieDetails.put(MainActivityFragment.MOVIE_POSTER_PATH, poster_path);
                movieDetails.put(MainActivityFragment.MOVIE_RELEASE_DATE, release_date);
                movieDetails.put(MainActivityFragment.MOVIE_VOTE_AVERAGE, vote_average);
                movieDetails.put(MainActivityFragment.MOVIE_POPULARITY, popularity);

                allFetchedMovies[i] = movieDetails;
            }

            return allFetchedMovies;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Hashtable[] doInBackground(String... params) {

        if (params.length == 0) {
            // Case of no params, nothing to do.
            return null;
        }

        // Network code for fetching JSON String
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieApiString = null;

        // # is private key for developer
        String apiKey = "#";

        try {
            // Construct the URL for the themoviedb.org API query
            // documentation at http://docs.themoviedb.apiary.io
            final String API_BASE_URL =
                    "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, params[0])
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
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Adding a newline (without effect on parsing) for debugging purposes.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieApiString = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
            return null;
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

        try {
            return getMovieDataFromJson(movieApiString);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing JSON
        return null;
    }

    @Override
    protected void onPostExecute(Hashtable[] result) {
        if (result != null) {
            mImageAdapter.clear();
            MainActivityFragment.allMoviesData = new Hashtable[result.length];
            for(int i = 0; i < result.length; i++) {
                // populate adapter to fill grid
                String urlEnding = (String) result[i].get(MainActivityFragment.MOVIE_POSTER_PATH);
                String movieUrl = Utility.createUrlFromEnding(urlEnding);
                mImageAdapter.add(movieUrl);
                // populate hashtable to be able to fire intents
                MainActivityFragment.allMoviesData[i] = (Hashtable) result[i].clone();
            }
        }
    }
}