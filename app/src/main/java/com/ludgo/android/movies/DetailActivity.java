package com.ludgo.android.movies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.ludgo.android.movies.data.MoviesContract;


public class DetailActivity extends ActionBarActivity {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static int movie_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Get key variable which is to determine the whole layout of the fragment
        Intent intent = this.getIntent();
        if (intent != null) {
            Uri movieIdUri = intent.getData();
            String movieIdStr = MoviesContract.getMovieIdStrFromUri(movieIdUri);
            movie_id = Integer.parseInt(movieIdStr);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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

    public static int getMovieId() {
        return movie_id;
    }
}
