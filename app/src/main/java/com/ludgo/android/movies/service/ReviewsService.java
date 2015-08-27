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

/**
 * Get reviews data from JSON HTTP request
 */
public class ReviewsService extends IntentService {

    private final String LOG_TAG = ReviewsService.class.getSimpleName();

    private int movie_id;

    public ReviewsService() {
        super("ReviewsService");
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
        String reviewsApiString;

        String apiKey = Utility.API_KEY;

        try {
            // Construct the URL for the themoviedb.org API query
            // documentation at http://docs.themoviedb.apiary.io
            final String API_BASE_URL =
                    "http://api.themoviedb.org/3/movie/" + movie_id + "/reviews?";
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
            reviewsApiString = buffer.toString();
            getReviewsDataFromJson(reviewsApiString);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the review data, there's no point in attempting
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
     * Take the String representing the complete reviews in JSON Format and
     * pull out the data needed to construct the wireframes.
     */
    private void getReviewsDataFromJson(String reviewsJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String NAME_RESULTS = "results";

        // These are the names of goal values
        final String NAME_ID = "id";
        final String NAME_AUTHOR = "author";
        final String NAME_CONTENT = "content";

        try {
            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray resultsArray = reviewsJson.getJSONArray(NAME_RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {

                // Get the JSON object representing one particular review
                JSONObject aReview = resultsArray.getJSONObject(i);

                // These are the values that will be collected for each review.
                String reviewId;
                String author;
                String content;

                reviewId = aReview.getString(NAME_ID);
                author = aReview.getString(NAME_AUTHOR);
                content = aReview.getString(NAME_CONTENT);

                if (reviewId != null &&
                        author != null &&
                        content != null) {

                    // First, check if the review with this id exists in the db
                    Cursor checkCursor = this.getContentResolver().query(
                            MoviesContract.ReviewsEntry.buildReviewsUriWithId(movie_id),
                            null,
                            MoviesContract.ReviewsEntry.COLUMN_REVIEW_ID + " = ?",
                            new String[]{reviewId},
                            null);
                    if (checkCursor.moveToFirst()) {
                        checkCursor.close();
                        break;
                    }
                    checkCursor.close();

                    // Insert the review information into the database
                    ContentValues reviewValues = new ContentValues();

                    reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_REVIEW_ID, reviewId);
                    reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_AUTHOR, author);
                    reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_CONTENT, content);
                    reviewValues.put(MoviesContract.ReviewsEntry.COLUMN_MOVIE_ID_REVIEWS_KEY, movie_id);

                    this.getContentResolver().insert(
                            MoviesContract.ReviewsEntry.CONTENT_URI, reviewValues);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}