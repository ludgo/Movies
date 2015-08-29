package com.ludgo.android.movies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private final String GRID_FRAGMENT_TAG = "GF_TAG";

    // Rules how to order grid
    private static String showRule;
    private static String sortRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load necessary rules
        showRule = Utility.getShowRule(this);
        sortRule = Utility.getSortRule(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new GridFragment(), GRID_FRAGMENT_TAG)
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

    public static String getShowRule() {
        return showRule;
    }

    public static String getSortRule() {
        return sortRule;
    }
}