package com.ludgo.android.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ludgo.android.movies.data.MoviesContract.MoviesEntry;

/**
 * Manages a local database for movies data.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                // Unique constant that will always be autoincremented
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesEntry.COLUMN_TITLE + " TEXT, " +
                MoviesEntry.COLUMN_OVERVIEW + " TEXT, " +
                MoviesEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MoviesEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                // 1 for favorite, 0 default
                MoviesEntry.COLUMN_FAVORITE + " INTEGER DEFAULT 0, " +

                // To assure the application have just one movie entry per particular themoviedb.org
                // API movie id, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + MoviesEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // This method DOES fire only if change the version number for database.
        // This method does NOT depend on the version number for application.
        // If updating the schema without wiping data, commenting out the next 2 lines
        // should be top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}