package com.ludgo.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;


public class MainActivity extends ActionBarActivity implements GridFragment.Callback {

    private static boolean isSingleFragment;

    private final String GRID_FRAGMENT_TAG = "GF_TAG";
    private final String DETAIL_FRAGMENT_TAG = "DF_TAG";

    // Rules how to order grid
    private static String showRule;
    private static String sortRule;
    private static boolean yearBoolean;
    private static String preferredYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load necessary rules
        showRule = Utility.getShowRule(this);
        sortRule = Utility.getSortRule(this);
        yearBoolean = Utility.getYearBoolean(this);
        preferredYear = Utility.getPreferredYear(this);

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
        boolean currentYearBoolean = Utility.getYearBoolean(this);
        String currentPreferredYear = Utility.getPreferredYear(this);

        // Reset text of the empty view
        GridFragment.mEmptyView.setText(R.string.view_empty_no_connection);
        GridFragment.mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                this.getResources().getDrawable(R.drawable.refresh_selector));

        if (showRule.equals(currentShowRule) &&
                sortRule.equals(currentSortRule) &&
                yearBoolean == currentYearBoolean &&
                preferredYear.equals(currentPreferredYear)) {
            // nothing changed
            return;
        }

        showRule = currentShowRule;
        sortRule = currentSortRule;
        yearBoolean = currentYearBoolean;
        preferredYear = currentPreferredYear;

        // Reset activated item
        GridFragment.activatedPosition = GridView.INVALID_POSITION;

        // Update the fragment layout using the fragment manager
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

    public static String getShowRule() {
        return showRule;
    }

    public static String getSortRule() {
        return sortRule;
    }

    public static boolean getYearBoolean() {
        return yearBoolean;
    }

    public static String getPreferredYear() {
        return preferredYear;
    }

    public static boolean isSingleFragment() {
        return isSingleFragment;
    }
}