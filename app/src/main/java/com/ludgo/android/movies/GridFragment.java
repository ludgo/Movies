package com.ludgo.android.movies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ludgo.android.movies.data.MoviesContract;
import com.ludgo.android.movies.service.MoviesService;


/**
 * Display grid of movie posters
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String POSITION_TAG = "POS_TAG";
    // Variable that stores information which grid item is activated until
    // either the page or a setting changes
    static int activatedPosition = GridView.INVALID_POSITION;

    static int page = 1;

    // Set id for each loader
    private static final int GRID_LOADER = 10;

    // Indices tied to GRID_LOADER CursorLoader projection to map column index in Cursor
    // Carefully consider any changes!
    static final int COL_MOVIE_ID = 1;
    static final int COL_POSTER_PATH = 2;

    private static GridView mGridView;
    private static TextView mEmptyView;
    private static ImageView mPreviousPageView;
    private static ImageView mNextPageView;
    private static GridAdapter mGridAdapter;

    /**
     * This mechanism allows activities to be notified of item selections at fragments.
     */
    public interface Callback {
        public void onItemSelected(Uri movieUri);
    }

    public GridFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        mGridView = (GridView) rootView.findViewById(R.id.gridView);

        mEmptyView = (TextView) rootView.findViewById(R.id.emptyView);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchMovies();
                updateGrid();
            }
        });

        mPreviousPageView = (ImageView) rootView.findViewById(R.id.previousPage);
        mPreviousPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activatedPosition = GridView.INVALID_POSITION;
                if (page > 1) {
                    page -= 1;
                }
                updateGrid();
            }
        });
        mNextPageView = (ImageView) rootView.findViewById(R.id.nextPage);
        mNextPageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activatedPosition = GridView.INVALID_POSITION;
                page += 1;
                // themoviedb.org API supports maximal value 1000 of the page parameter in Url
                if (page <= 1000) {
                    fetchMovies();
                }
                updateGrid();
            }
        });


        // Get width of the actual screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if (!MainActivity.isSingleFragment) {
            width = width / 2;
        }

        int itemWidth;
        // Choose grid style
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 2 columns when portrait orientation
            mGridView.setNumColumns(2);
            itemWidth = width / 2;
        } else {
            // 3 columns when landscape orientation
            mGridView.setNumColumns(3);
            itemWidth = width / 3;
        }
        mGridView.setColumnWidth(itemWidth);

        // Set adapter with empty cursor
        // Note: CursorLoader will automatically initiate Cursor and register ContentObserver
        // on it, that is why no flags needed
        mGridAdapter = new GridAdapter(getActivity(), null, 0, itemWidth);
        mGridView.setEmptyView(mEmptyView);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int movie_id = cursor.getInt(COL_MOVIE_ID);
                    ((Callback) getActivity()).onItemSelected(
                            MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id));
                }
                activatedPosition = position;
            }
        });

        if (!MainActivity.isSingleFragment &&
                savedInstanceState != null &&
                savedInstanceState.containsKey(POSITION_TAG)) {
            // Remind which grid item was chosen
            activatedPosition = savedInstanceState.getInt(POSITION_TAG);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRID_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchMovies();
        updateGrid();
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ( key.equals(getString(R.string.pref_movies_status_key)) ) {
            updateEmptyView();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!MainActivity.isSingleFragment &&
                activatedPosition != GridView.INVALID_POSITION) {
            // Save the position of activated grid item
            outState.putInt(POSITION_TAG, activatedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        String sortOption = null;
        // By which column to sort
        if (MainActivity.sortRule.equals(
                getActivity().getString(R.string.pref_sort_entryValues_popularity)
        )) {
            sortOption = MoviesContract.MoviesEntry.COLUMN_POPULARITY;
        } else if (MainActivity.sortRule.equals(
                getActivity().getString(R.string.pref_sort_entryValues_voteAverage)
        )) {
            sortOption = MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE;
        }
        // Order movies descending and limit the number to 12
        sortOption += " DESC LIMIT 12";
        if (page > 1) {
            // Skip definite number of movies, since it is not the first page
            sortOption = sortOption + " OFFSET " + (page - 1) * 12;
        }

        String showOption = null;
        String[] showOptionArgs = null;
        // If show only favorites
        if (MainActivity.showRule.equals(
                getActivity().getString(R.string.pref_show_entryValues_favorites)
        )) {
            showOption = MoviesContract.MoviesEntry.COLUMN_FAVORITE + " = 1";
        }
        // If show only one particular year movies
        final String YEAR = MainActivity.preferredYear;
        if (MainActivity.yearBoolean && !YEAR.equals("")) {
            if (showOption == null) {
                showOption = MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " LIKE ?";
            } else {
                showOption += " AND " + MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE + " LIKE ?";
            }
            showOptionArgs = new String[]{MainActivity.preferredYear + "-__-__"};
        }

        return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
                // if projection changes, the globally defined indices must change!
                new String[]{MoviesContract.MoviesEntry._ID,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
                        MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
                        MoviesContract.MoviesEntry.COLUMN_POPULARITY,
                        MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
                        MoviesContract.MoviesEntry.COLUMN_FAVORITE,
                        MoviesContract.MoviesEntry.COLUMN_RELEASE_DATE},
                showOption,
                showOptionArgs,
                sortOption);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        // Reset page navigator
        mPreviousPageView.setVisibility(View.GONE);
        mNextPageView.setVisibility(View.GONE);

        if (!Utility.isNetworkAvailable(getActivity())) {
            // Modification of the empty view in case of no connection
            mEmptyView.setText(R.string.view_empty_no_connection);
            mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                    this.getResources().getDrawable(R.drawable.ic_refresh_selector));
            // No action without connection since the whole app is based on fetching immediate data
            mGridAdapter.swapCursor(null);
            return;
        }

        mGridAdapter.swapCursor(cursor);

        if (!cursor.moveToFirst()) {
            updateEmptyView();
        } else {
            // Cursor contains data for at least one movie

            // Navigate through the pages
            if (page > 1) {
                mPreviousPageView.setVisibility(View.VISIBLE);
            }
            mNextPageView.setVisibility(View.VISIBLE);

            // Restore state after tablet screen rotation
            if (!MainActivity.isSingleFragment &&
                    activatedPosition != GridView.INVALID_POSITION) {
                // Restore previous state of scrollbar
                mGridView.requestFocusFromTouch();
                mGridView.setSelection(activatedPosition);
                // Activate the view
                mGridView.setItemChecked(activatedPosition, true);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mGridAdapter.swapCursor(null);
    }

    void fetchMovies() {
        // Launch service with aim to save movies data in database
        Intent intent = new Intent(getActivity(), MoviesService.class);
        intent.putExtra(MoviesService.PAGE_EXTRA, page + "");
        getActivity().startService(intent);
    }

    void updateGrid() {
        // Initiate CursorLoader with aim to display posters that correspond to the settings
        getLoaderManager().restartLoader(GRID_LOADER, null, this);
    }

    /*
        Find out why the cursor is empty and provide user with relevant information
        why no movie posters are displayed
     */
    private void updateEmptyView() {

        @MoviesService.MoviesStatus int moviesStatus = Utility.getMoviesStatus(getActivity());
        switch (moviesStatus) {
            case MoviesService.MOVIES_STATUS_OK:
                if (page == 1) {
                    // Modification of the empty view in case of no movies at all
                    mEmptyView.setText(R.string.view_empty_no_match);
                    mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else {
                    // Modification of the empty view in case of no more movies
                    mEmptyView.setText(R.string.view_empty_no_more);
                    mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    // Allow user to get back to filled cursor
                    mPreviousPageView.setVisibility(View.VISIBLE);
                }
                break;
            case MoviesService.MOVIES_STATUS_SERVER_DOWN:
                // Modification of the empty view in case of server side problem
                mEmptyView.setText(R.string.view_empty_server_down);
                mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        this.getResources().getDrawable(R.drawable.ic_refresh_selector));
                break;
            case MoviesService.MOVIES_STATUS_SERVER_INVALID:
                // Modification of the empty view in case of strange server response
                mEmptyView.setText(R.string.view_empty_server_error);
                mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        this.getResources().getDrawable(R.drawable.ic_refresh_selector));
                break;
            default:
                // Modification of the empty view in case of unknown error
                mEmptyView.setText(null);
                mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }
}