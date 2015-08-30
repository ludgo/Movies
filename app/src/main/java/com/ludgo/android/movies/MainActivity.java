package com.ludgo.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity implements GridFragment.Callback {

    private static boolean isSingleFragment;

    private final String GRID_FRAGMENT_TAG = "GF_TAG";
    private final String DETAIL_FRAGMENT_TAG = "DF_TAG";

    // Rules how to order grid
    private static String showRule;
    private static String sortRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load necessary rules
        showRule = Utility.getShowRule(this);
        sortRule = Utility.getSortRule(this);

        super.onCreate(savedInstanceState);
        // dynamic approach when setting both fragments
        setContentView(R.layout.activity_main);
        isSingleFragment = findViewById(R.id.holder_detail) == null;
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.holder_grid, new GridFragment(), GRID_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentShowRule = Utility.getShowRule(this);
        String currentSortRule = Utility.getSortRule(this);

        if (showRule.equals(currentShowRule) && sortRule.equals(currentSortRule)) {
            // nothing changed
            return;
        }

        showRule = currentShowRule;
        sortRule = currentSortRule;

        // update the fragment layout using the fragment manager
        GridFragment gridFragment = (GridFragment) getSupportFragmentManager()
                .findFragmentByTag(GRID_FRAGMENT_TAG);
        if (null != gridFragment) {
            if (showRule.equals(
                    this.getString(R.string.pref_show_entryValues_all)
            )) {
                gridFragment.fetchMovies();
            }
            gridFragment.updateGrid();
        }
    }

    // Implemented as Callback in GridFragment
    @Override
    public void onItemSelected(Uri contentUri) {
        if (isSingleFragment) {
            // Case of one pane mode launches another activity

            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        } else {
            // Case of two pane mode replaces fragment

            // Create arguments such that DetailFragment will know
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.MOVIE_URI, contentUri);

            // Set arguments on new created DetailFragment
            DetailFragment df = new DetailFragment();
            df.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.holder_detail, df, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
    }

    public static String getShowRule() { return showRule; }

    public static String getSortRule() { return sortRule; }

    public static boolean isSingleFragment() { return isSingleFragment; }
}