package com.ludgo.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * All movie nitty-gritty
 */
public class DetailActivityFragment extends Fragment {

    private VideoAdapter mVideoAdapter;
    private String MOVIE_ID;

    // These are the names of goal values
    public static final String VIDEO_NAME = "name";
    public static final String VIDEO_KEY = "key";

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // trailers list view is also root in this case
        ListView trailersListView = (ListView) rootView;
        // all movie details will be prepended as header to trailers
        LinearLayout header = (LinearLayout) inflater.inflate(R.layout.fragment_detail_header, null);
        trailersListView.addHeaderView(header);
        // this adapter is to be populated with trailers
        mVideoAdapter = new VideoAdapter(getActivity(), new ArrayList<String>());
        trailersListView.setAdapter(mVideoAdapter);

        // set received header data
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(MainActivityFragment.MOVIE_ID)) {
                // save movie id
                MOVIE_ID = getActivity().getIntent().getStringExtra(MainActivityFragment.MOVIE_ID);
            }

            if (intent.hasExtra(MainActivityFragment.MOVIE_TITLE)) {
                // set title
                TextView titleView = (TextView) rootView.findViewById(R.id.title);
                String title = intent.getStringExtra(MainActivityFragment.MOVIE_TITLE);
                titleView.setText(title);
                if (title.length() < 25) {
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 45);
                } else {
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                }
            }

            if (intent.hasExtra(MainActivityFragment.MOVIE_OVERVIEW)) {
                // set overview
                TextView overviewView = (TextView) rootView.findViewById(R.id.overview);
                overviewView.setText(intent.getStringExtra(MainActivityFragment.MOVIE_OVERVIEW));
            }

            if (intent.hasExtra(MainActivityFragment.MOVIE_VOTE_AVERAGE)) {
                // set voteAverage
                TextView overviewView = (TextView) rootView.findViewById(R.id.voteAverage);
                overviewView.setText(intent.getStringExtra(MainActivityFragment.MOVIE_VOTE_AVERAGE) + "/10");
            }

            if (intent.hasExtra(MainActivityFragment.MOVIE_RELEASE_DATE)) {
                // set year
                TextView overviewView = (TextView) rootView.findViewById(R.id.year);
                overviewView.setText(Utility.createYearFromReleaseDate(
                                intent.getStringExtra(MainActivityFragment.MOVIE_RELEASE_DATE)
                        )
                );
            }

            if (intent.hasExtra(MainActivityFragment.MOVIE_POSTER_PATH)) {
                // set poster image
                ImageView imageView = (ImageView) rootView.findViewById(R.id.poster);
                String posterUrl = Utility.createUrlFromEnding(
                        intent.getStringExtra(MainActivityFragment.MOVIE_POSTER_PATH)
                );
                Picasso.with(getActivity())
                        .load(posterUrl)
                        .into(imageView);
            }
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTrailers();
    }

    private void updateTrailers() {
        // Display movie posters correspondingly
        FetchVideosTask fetchVideosTask = new FetchVideosTask();
        fetchVideosTask.execute();
    }

    /**
     * Get trailers data from JSON HTTP request
     */
    public class FetchVideosTask extends AsyncTask<Void, Void, Hashtable[]> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Take the String representing the complete videos in JSON Format and
         * pull out the data needed to construct the wireframes.
         */
        private Hashtable[] getVideoDataFromJson(String videosJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_RESULTS = "results";

            try {
                JSONObject videosJson = new JSONObject(videosJsonStr);
                JSONArray resultsArray = videosJson.getJSONArray(MOVIE_RESULTS);

                // All Youtube trailer names and keys will be stored here
                Hashtable[] allFetchedVideos = new Hashtable[resultsArray.length()];

                for(int i = 0; i < resultsArray.length(); i++) {
                    // These are the values that will be collected for each video.
                    String videoName;
                    String videoKey;

                    // Get the JSON object representing one particular video
                    JSONObject aVideo = resultsArray.getJSONObject(i);

                    videoName = aVideo.getString(VIDEO_NAME);
                    videoKey = aVideo.getString(VIDEO_KEY);

                    Hashtable videoDetails = new Hashtable(7);

                    videoDetails.put(VIDEO_NAME, videoName);
                    videoDetails.put(VIDEO_KEY, videoKey);

                    allFetchedVideos[i] = videoDetails;
                }

                return allFetchedVideos;

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected Hashtable[] doInBackground(Void... params) {

            // Network code for fetching JSON String
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String videoApiString = null;

            String apiKey = Utility.API_KEY;

            try {
                // Construct the URL for the themoviedb.org API query
                // documentation at http://docs.themoviedb.apiary.io
                final String API_BASE_URL =
                        "http://api.themoviedb.org/3/movie/" + MOVIE_ID + "/videos?";
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
                videoApiString = buffer.toString();
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
                return getVideoDataFromJson(videoApiString);
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
                mVideoAdapter.clear();
                for(int i = 0; i < result.length; i++) {
                    // populate adapter to fill trailers list
                    String trailerName = (String) result[i].get(VIDEO_NAME);
                    mVideoAdapter.add(trailerName);
                }
            }
        }
    }
}
