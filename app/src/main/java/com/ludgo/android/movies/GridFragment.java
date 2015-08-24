package com.ludgo.android.movies;

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
import android.widget.GridView;

import com.ludgo.android.movies.data.MoviesContract;


/**
 * Display grid of movie posters
 */
public class GridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Set id for each loader
    private static final int GRID_LOADER = 0;

    PopularAdapter mPopularAdapter;

    // Rules how to order grid
    String showRule;
    String sortRule;

    // These are the names of goal values
    public static final String MOVIE_ID = "id";
    public static final String MOVIE_TITLE = "title";
    public static final String MOVIE_OVERVIEW = "overview";
    public static final String MOVIE_POSTER_PATH = "poster_path";
    public static final String MOVIE_RELEASE_DATE = "release_date";
    public static final String MOVIE_VOTE_AVERAGE = "vote_average";
    public static final String MOVIE_POPULARITY = "popularity";

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

        // Load necessary rules
        showRule = Utility.getShowRule(getActivity());
        sortRule = Utility.getSortRule(getActivity());

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

        // set adapter with empty cursor
        // Note: CursorLoader will automatically initiate Cursor and register ContentObserver
        // on it, that is why no flags needed
        mPopularAdapter = new PopularAdapter(getActivity(), null, 0);
        gridView.setAdapter(mPopularAdapter);

//        if (showRule.equals(
//                getActivity().getString(R.string.pref_show_entryValues_all))) {
//            mPopularAdapter = new PopularAdapter(getActivity(), null, 0);
//            gridView.setAdapter(mPopularAdapter);
//        } else if (showRule.equals(
//                getActivity().getString(R.string.pref_show_entryValues_favorites))) {
//            Cursor c = getActivity().getContentResolver().query(
//                    MoviesContract.MoviesEntry.CONTENT_URI,
//                    new String[]{MoviesContract.MoviesEntry._ID,
//                            MoviesContract.MoviesEntry.COLUMN_POSTER_PATH,
//                            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID},
//                    MoviesContract.MoviesEntry.COLUMN_FAVORITE + " = 1",
//                    null,
//                    null);
//            gridView.setAdapter(new PopularAdapter(getActivity(), c, 0));
//        }

//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                String movie_id = Integer.toString( (Integer) allMoviesData[position].get(MOVIE_ID) );
//                String title = (String) allMoviesData[position].get(MOVIE_TITLE);
//                String overview = (String) allMoviesData[position].get(MOVIE_OVERVIEW);
//                String voteAverage = Double.toString( (Double) allMoviesData[position].get(MOVIE_VOTE_AVERAGE) );
//                String releaseDate = (String) allMoviesData[position].get(MOVIE_RELEASE_DATE);
//                String posterPath = (String) allMoviesData[position].get(MOVIE_POSTER_PATH);
//                String popularity = Double.toString( (Double) allMoviesData[position].get(MOVIE_POPULARITY) );
//
//                Intent intent = new Intent(getActivity(), DetailActivity.class)
//                        .putExtra(MOVIE_ID, movie_id)
//                        .putExtra(MOVIE_TITLE, title)
//                        .putExtra(MOVIE_OVERVIEW, overview)
//                        .putExtra(MOVIE_POSTER_PATH, posterPath)
//                        .putExtra(MOVIE_RELEASE_DATE, releaseDate)
//                        .putExtra(MOVIE_VOTE_AVERAGE, voteAverage)
//                        .putExtra(MOVIE_POPULARITY, popularity);
//                startActivity(intent);
//            }
//        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateGrid();
    }

    private void updateGrid() {
        // Display movie posters correspondingly
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(getActivity());
        fetchMoviesTask.execute(sortRule);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(GRID_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // by which column to sort
        String sortColumn = null;
        if (sortRule.equals(
                getActivity().getString(R.string.pref_sort_entryValues_popularity)
        )){
            sortColumn = MoviesContract.MoviesEntry.COLUMN_POPULARITY;
        } else if (sortRule.equals(
                getActivity().getString(R.string.pref_sort_entryValues_voteAverage)
        )){
            sortColumn = MoviesContract.MoviesEntry.COLUMN_VOTE_AVERAGE;
        }

        // if show only favorites
        String showOption = null;
        if (showRule.equals(
                getActivity().getString(R.string.pref_show_entryValues_favorites)
        )){
            showOption = MoviesContract.MoviesEntry.COLUMN_FAVORITE + " = 1";
        }

        return new CursorLoader(getActivity(),
                MoviesContract.MoviesEntry.CONTENT_URI,
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