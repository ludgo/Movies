package com.ludgo.android.movies;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.ludgo.android.movies.data.MoviesContract;
import com.ludgo.android.movies.service.MoviesService;


/**
 * Display grid of movie posters
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = GridFragment.class.getSimpleName();

    // Set id for each loader
    private static final int GRID_LOADER = 10;

    // Indices tied to GRID_LOADER CursorLoader projection to map column index in Cursor
    // Carefully consider any changes!
    static final int COL_MOVIE_ID = 1;
    static final int COL_POSTER_PATH = 2;

    GridAdapter mGridAdapter;

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

        // Get width of the actual screen
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        // Final value of this constant will be number of columns in grid
        int constant = 1;
        // maximal column width to which fetched image with maximal width can be stably
        // stretched by Picasso
        while (width / constant > 254) {
            constant += 1;
        }
        gridView.setNumColumns(constant);
        int itemWidth = width / constant;
        gridView.setColumnWidth(itemWidth);

        // Set adapter with empty cursor
        // Note: CursorLoader will automatically initiate Cursor and register ContentObserver
        // on it, that is why no flags needed
        mGridAdapter = new GridAdapter(getActivity(), null, 0, itemWidth);
        gridView.setAdapter(mGridAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    int movie_id = cursor.getInt(COL_MOVIE_ID);
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(MoviesContract.MoviesEntry.buildMoviesUriWithId(movie_id));
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
        fetchMovies();
        updateGrid();
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
        mGridAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mGridAdapter.swapCursor(null);
    }
}