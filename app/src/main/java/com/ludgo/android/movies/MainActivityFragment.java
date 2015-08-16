package com.ludgo.android.movies;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Display grid of movie posters
 */
public class MainActivityFragment extends Fragment {

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
            FetchJsonTask fetchJsonTask = new FetchJsonTask();
            fetchJsonTask.execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // dummy data
        String[] imageUrls = new String[]{
                "http://image.tmdb.org/t/p/w342/7SGGUiTE6oc2fh9MjIk5M00dsQd.jpg",
                "http://image.tmdb.org/t/p/w342/5JU9ytZJyR3zmClGmVm9q4Geqbd.jpg",
                "http://image.tmdb.org/t/p/w342/kqjL17yufvn9OVLyXYpvtyrFfak.jpg",
                "http://image.tmdb.org/t/p/w342/yUlpRbbrac0GTNHZ1l20IHEcWAN.jpg",
                "http://image.tmdb.org/t/p/w342/aBBQSC8ZECGn6Wh92gKDOakSC8p.jpg",
                "http://image.tmdb.org/t/p/w342/uXZYawqUsChGSj54wcuBtEdUJbh.jpg",
                "http://image.tmdb.org/t/p/w342/aAmfIX3TT40zUHGcCKrlOZRKC7u.jpg"};

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
        gridView.setAdapter(new ImageAdapter(getActivity(), imageUrls));

        // Use class for fetching JSON data
        FetchJsonTask fetchJsonTask = new FetchJsonTask();
        fetchJsonTask.execute();

        return rootView;
    }

    /**
     * Get String data from JSON HTTP request
     */
    public class FetchJsonTask extends AsyncTask <Void, Void, Void> {
        private final String LOG_TAG = FetchJsonTask.class.getSimpleName();

        @Override
        protected Void doInBackground(Void... params) {

            // Network code for fetching JSON String
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieApiString = null;

            try {
                // Construct the URL for the themoviedb.org API query
                // documentation at http://docs.themoviedb.apiary.io
                // # is private key
                URL url = new URL("http://api.themoviedb.org/3/discover/movie" +
                        "?sort_by=popularity.desc&api_key=#");

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
                    // Adding a newline (without affect on parsing) for debugging purposes.
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
            } finally{
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

            Log.d(LOG_TAG, "FetchJsonTask executed");

            return null;
        }
    }
}
