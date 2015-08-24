package com.ludgo.android.movies;

import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ludgo.android.movies.data.MoviesContract;


/**
 * Display grid of movie posters
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = GridFragment.class.getSimpleName();

    // Set id for each loader
    private static final int GRID_LOADER = 0;

    PopularAdapter mPopularAdapter;

    // Indices tied to GRID_LOADER CursorLoader projection to map column index in Cursor
    // Carefully consider any changes!
    static final int COL_MOVIE_ID = 1;
    static final int COL_POSTER_PATH = 2;

    public GridFragment() {
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);
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

        // Set adapter with empty cursor
        // Note: CursorLoader will automatically initiate Cursor and register ContentObserver
        // on it, that is why no flags needed
        mPopularAdapter = new PopularAdapter(getActivity(), null, 0);
        gridView.setAdapter(mPopularAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int movie_id = cursor.getInt(COL_MOVIE_ID);
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra("movie_id", movie_id);
                    startActivity(intent);
                }
            }
        });

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
        fetchData();
        updateGrid();
    }

    // If settings have been changed since the activity was created, we need to fetch again
    void fetchData() {
        // Save movies details in database
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity());
        fetchMoviesTask.execute(MainActivity.getSortRule());
    }

    // If settings have been changed since the activity was created, we need restart of grid
    void updateGrid() {
        // Display movie posters correspondingly
        getLoaderManager().restartLoader(GRID_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

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
                // if projection changes, indices must change!
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
        mPopularAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mPopularAdapter.swapCursor(null);
    }
}