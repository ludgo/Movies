package com.ludgo.android.movies.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import java.util.Vector;

/**
 * Get trailers data from JSON HTTP request
 */
public class TrailersService extends IntentService {

    private final String LOG_TAG = TrailersService.class.getSimpleName();

    private int movie_id;

    public TrailersService() {
        super("TrailersService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            Uri movieIdUri = intent.getData();
            String movieIdStr = MoviesContract.getMovieIdStrFromUri(movieIdUri);
            movie_id = Integer.parseInt(movieIdStr);
        } else {
            // Nothing to do without movie id
            return;
        }

        // Network code for fetching JSON String
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String videoApiString;

        String apiKey = Utility.API_KEY;

        try {
            // Construct the URL for the themoviedb.org API query
            // documentation at http://docs.themoviedb.apiary.io
            final String API_BASE_URL =
                    "http://api.themoviedb.org/3/movie/" + movie_id + "/videos?";
            final String KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
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
            videoApiString = buffer.toString();
            getVideoDataFromJson(videoApiString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the video data, there's no point in attempting
            // to parse it.
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
     * Take the String representing the complete videos in JSON Format and
     * pull out the data needed to construct the wireframes.
     */
    private void getVideoDataFromJson(String videosJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String NAME_RESULTS = "results";

        // These are the names of goal values
        final String NAME_ID = "id";
        final String NAME_NAME = "name";
        final String NAME_KEY = "key";
        // Only for checking purposes
        final String NAME_SITE = "site";

        try {
            JSONObject videosJson = new JSONObject(videosJsonStr);
            JSONArray resultsArray = videosJson.getJSONArray(NAME_RESULTS);

            // Collect new information
            Vector<ContentValues> contentValuesVector = new Vector<ContentValues>(resultsArray.length());

            for (int i = 0; i < resultsArray.length(); i++) {

                // Get the JSON object representing one particular video
                JSONObject aVideo = resultsArray.getJSONObject(i);

                String site;
                site = aVideo.getString(NAME_SITE);

                if (site != null && site.equals("YouTube")) {

                    // These are the values that will be collected for each video.
                    String videoId;
                    String name;
                    String key;

                    videoId = aVideo.getString(NAME_ID);
                    name = aVideo.getString(NAME_NAME);
                    key = aVideo.getString(NAME_KEY);

                    if (videoId != null &&
                            name != null &&
                            key != null) {

                        // First, check if the trailer with this id exists in the db
                        Cursor checkCursor = this.getContentResolver().query(
                                MoviesContract.TrailersEntry.buildOneTrailerUri(movie_id, videoId),
                                null,
                                null,
                                null,
                                null);
                        if (!checkCursor.moveToFirst()) {
                            // A new row
                            ContentValues videoValues = new ContentValues();

                            videoValues.put(MoviesContract.TrailersEntry.COLUMN_TRAILER_ID, videoId);
                            videoValues.put(MoviesContract.TrailersEntry.COLUMN_NAME, name);
                            videoValues.put(MoviesContract.TrailersEntry.COLUMN_KEY, key);
                            videoValues.put(MoviesContract.TrailersEntry.COLUMN_MOVIE_ID_TRAILERS_KEY, movie_id);

                            contentValuesVector.add(videoValues);
                        }
                        checkCursor.close();
                    }
                }
            }

            // Insert new information into the database
            if ( contentValuesVector.size() > 0 ) {
                ContentValues[] rowsArray = new ContentValues[contentValuesVector.size()];
                contentValuesVector.toArray(rowsArray);
                this.getContentResolver()
                        .bulkInsert(MoviesContract.TrailersEntry.CONTENT_URI, rowsArray);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}