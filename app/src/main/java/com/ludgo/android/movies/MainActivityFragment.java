package com.ludgo.android.movies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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


/**
 * Display grid of movie posters
 */
public class MainActivityFragment extends Fragment {

    ImageAdapter mImageAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateGrid();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Catch own layout
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        // Find grid view
        GridView gridView = (GridView) rootView.findViewById(R.id.gridview);
        // Choose grid style
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 2 columns when portrait orientation
            gridView.setNumColumns(2);
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 3 columns when landscape orientation
            gridView.setNumColumns(3);
        }

        // Populate grid view with image posters via adapter
        mImageAdapter = new ImageAdapter(getActivity(), new ArrayList<String>());
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String posterUrl = mImageAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, posterUrl);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    private void updateGrid() {
        // Find rule how to order movies
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortRule = preferences.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_entryValues_default));
        // Display movie posters correspondingly
        FetchJsonTask fetchJsonTask = new FetchJsonTask();
        fetchJsonTask.execute(sortRule);
    }

    /**
     * Get String data from JSON HTTP request
     */
    public class FetchJsonTask extends AsyncTask <String, Void, String[]> {

        private final String LOG_TAG = FetchJsonTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */
        private String[] getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            final String MOVIE_RESULTS = "results";
            final String MOVIE_POSTER_PATH = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray resultsArray = movieJson.getJSONArray(MOVIE_RESULTS);

            // Array with poster url endings
            String[] moviePosterPaths = new String[resultsArray.length()];
            for (int i = 0; i < moviePosterPaths.length; i++) {
                JSONObject movie = resultsArray.getJSONObject(i);
                moviePosterPaths[i] = movie.getString(MOVIE_POSTER_PATH);
            }

            return moviePosterPaths;
        }

        private String createUrlFromEnding(String ending) {
            final String URL_BASE = "http://image.tmdb.org/t/p/w185/";
            String urlString = URL_BASE + ending;
            return urlString;
        }

        @Override
        protected String[] doInBackground(String... params) {

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
                String[] movieEndings = getMovieDataFromJson(movieApiString);
                String[] movieUrls = new String[movieEndings.length];
                for (int i = 0; i < movieEndings.length; i++) {
                    movieUrls[i] = createUrlFromEnding(movieEndings[i]);
                }
                return movieUrls;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing JSON
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                // Change dummy urls for fetched ones
                mImageAdapter.clear();
                for(String movieUrl : result) {
                    mImageAdapter.add(movieUrl);
                }
            }
        }
    }
}
