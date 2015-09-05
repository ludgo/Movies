package com.ludgo.android.movies;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;

import com.ludgo.android.movies.data.MoviesContract;
import com.ludgo.android.movies.service.MoviesService;


/**
 * Display grid of movie posters
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String POSITION_TAG = "POS_TAG";
    static int activatedPosition = GridView.INVALID_POSITION;

    // Set id for each loader
    private static final int GRID_LOADER = 10;

    // Indices tied to GRID_LOADER CursorLoader projection to map column index in Cursor
    // Carefully consider any changes!
    static final int COL_MOVIE_ID = 1;
    static final int COL_POSTER_PATH = 2;

    private static GridView mGridView;
    static TextView mEmptyView;
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
        // Find grid view
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mEmptyView = (TextView) rootView.findViewById(R.id.emptyView);
        mEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchMovies();
                updateGrid();
            }
        });

        // Get width of the actual screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if (!MainActivity.isSingleFragment()) { width = width/2; };

        int itemWidth;
        // Choose grid style
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 2 columns when portrait orientation
            mGridView.setNumColumns(2);
            itemWidth = width/2;
        } else {
            // 3 columns when landscape orientation
            mGridView.setNumColumns(3);
            itemWidth = width/3;
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

        if (!MainActivity.isSingleFragment() &&
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
    public void onSaveInstanceState(Bundle outState) {
        if (!MainActivity.isSingleFragment() &&
                activatedPosition != GridView.INVALID_POSITION) {
            // Save the position of activated grid item
            outState.putInt(POSITION_TAG, activatedPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        // By which column to sort
        String sortColumn = null;
        if (MainActivity.getSortRule().equals(
                getActivity().getString(R.string.pref_sort_entryValues_popularity)
        )) {
            sortColumn = MoviesContract.MoviesEntry.COLUMN_POPULARITY;
        } else if (MainActivity.getSortRule().equals(
                getActivity().getString(R.string.pref_sort_entryValues_voteAverage)
        )) {
            sortColumn = MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE;
        }

        // If show only favorites
        String showOption = null;
        if (MainActivity.getShowRule().equals(
                getActivity().getString(R.string.pref_show_entryValues_favorites)
        )) {
            showOption = MoviesContract.MoviesEntry.COLUMN_FAVORITE + " = 1";
        }

        return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
                // if projection changes, the globally defined indices must change!
                new String[]{MoviesContract.MoviesEntry._ID,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
                        MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
                        sortColumn,
                        MoviesContract.MoviesEntry.COLUMN_FAVORITE},
                showOption,
                null,
                sortColumn + " DESC LIMIT 12");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!Utility.isNetworkAvailable(getActivity())) {
            // No action without connection since the whole app is based on fetching immediate data
            mGridAdapter.swapCursor(null);
        } else {
            mGridAdapter.swapCursor(cursor);

            if (!cursor.moveToFirst() &&
                    MainActivity.getShowRule().equals(
                            getActivity().getString(R.string.pref_show_entryValues_favorites))){
                // Case when user has no movies in their favorites collection
                mEmptyView.setText(R.string.view_empty_no_favorites);
                mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (!MainActivity.isSingleFragment() &&
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

    // If settings have been changed since the activity was created, we need to fetch again
    void fetchMovies() {
        // Save movies details in database
        Intent intent = new Intent(getActivity(), MoviesService.class);
        getActivity().startService(intent);
    }

    // If settings have been changed since the activity was created, we need to update the grid
    void updateGrid() {
        // Display movies posters correspondingly
        getLoaderManager().restartLoader(GRID_LOADER, null, this);
    }
}