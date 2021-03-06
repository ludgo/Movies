package com.ludgo.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;


public class MainActivity extends AppCompatActivity implements GridFragment.Callback {

    static boolean isSingleFragment;

    private final String GRID_FRAGMENT_TAG = "GF_TAG";
    private final String DETAIL_FRAGMENT_TAG = "DF_TAG";

    // Rules how to order grid
    static String showRule;
    static String sortRule;
    static boolean yearBoolean;
    static String preferredYear;
    private static int preferredTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load necessary rules
        showRule = Utility.getShowRule(this);
        sortRule = Utility.getSortRule(this);
        yearBoolean = Utility.getYearBoolean(this);
        preferredYear = Utility.getPreferredYear(this);
        preferredTheme = Utility.getPreferredTheme(this);

        super.onCreate(savedInstanceState);
        setTheme(preferredTheme);

        // dynamic approach when setting both fragments
        setContentView(R.layout.activity_main);
        // customise toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
        int currentTheme = Utility.getPreferredTheme(this);
        if (preferredTheme != currentTheme) {
            recreate();
        }
        super.onResume();
        String currentShowRule = Utility.getShowRule(this);
        String currentSortRule = Utility.getSortRule(this);
        boolean currentYearBoolean = Utility.getYearBoolean(this);
        String currentPreferredYear = Utility.getPreferredYear(this);

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

        // Reset current page constant
        GridFragment.page = 1;

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
}