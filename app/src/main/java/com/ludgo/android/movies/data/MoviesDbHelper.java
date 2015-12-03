package com.ludgo.android.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ludgo.android.movies.data.MoviesContract.MoviesEntry;
import com.ludgo.android.movies.data.MoviesContract.TrailersEntry;
import com.ludgo.android.movies.data.MoviesContract.ReviewsEntry;

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
    public void onOpen(SQLiteDatabase sqLiteDatabase){
        super.onOpen(sqLiteDatabase);

        // http://stackoverflow.com/questions/22791217/should-i-enable-foreign-key-constraint-in-onopen-or-onconfigure
        if (!sqLiteDatabase.isReadOnly()) {
            // Foreign keys necessary for cascading delete
            sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // Note: Database relies on not null values to provide the best user experience
    // and to avoid buggy environment
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // http://stackoverflow.com/questions/22791217/should-i-enable-foreign-key-constraint-in-onopen-or-onconfigure
        if (!sqLiteDatabase.isReadOnly()) {
            // Foreign keys necessary for cascading delete
            sqLiteDatabase.execSQL("PRAGMA foreign_keys=ON;");
        }

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +

                // _ID constraints are not required directly when constructing these tables,
                // but necessary in case a table will be used with CursorAdapter
                MoviesEntry._ID + " INTEGER PRIMARY KEY, " +

                // To assure the application has just one movie entry per particular
                // themoviedb.org API movie id, it's created a UNIQUE constraint.
                // To maintain favorites, we CANNOT replace movies by insert again and again.
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MoviesEntry.COLUMN_POPULARITY + " REAL NOT NULL, " +
                // 1 for favorite, 0 default
                MoviesEntry.COLUMN_FAVORITE + " INTEGER NOT NULL DEFAULT 0);";

        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + TrailersEntry.TABLE_NAME + " (" +

                // From oldest to newest
                TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                // To assure the application has just one trailer entry per particular
                // themoviedb.org API trailer id, it's created a UNIQUE constraint.
                // Presumably, its parameters won't, change, so that no point in replacing.
                TrailersEntry.COLUMN_TRAILER_ID + " TEXT UNIQUE NOT NULL, " +
                TrailersEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_MOVIE_ID_TRAILERS_KEY + " INTEGER NOT NULL, " +

                // Set up the movie id column as a foreign key to movies table,
                // with delete if the record in the parent table is deleted
                "FOREIGN KEY (" + TrailersEntry.COLUMN_MOVIE_ID_TRAILERS_KEY + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_MOVIE_ID + ") " +
                "ON DELETE CASCADE);";

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + ReviewsEntry.TABLE_NAME + " (" +

                // From oldest to newest
                ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                // To assure the application has just one review entry per particular
                // themoviedb.org API review id, it's created a UNIQUE constraint.
                // Presumably, its parameters won't, change, so that no point in replacing.
                ReviewsEntry.COLUMN_REVIEW_ID + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_MOVIE_ID_REVIEWS_KEY + " INTEGER NOT NULL, " +

                // Set up the movie id column as a foreign key to movies table,
                // with delete if the record in the parent table is deleted
                "FOREIGN KEY (" + ReviewsEntry.COLUMN_MOVIE_ID_REVIEWS_KEY + ") REFERENCES " +
                MoviesEntry.TABLE_NAME + " (" + MoviesEntry.COLUMN_MOVIE_ID + ") " +
                "ON DELETE CASCADE);";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILERS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEWS_TABLE);
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailersEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}