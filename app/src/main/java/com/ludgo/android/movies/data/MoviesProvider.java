package com.ludgo.android.movies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MoviesProvider extends ContentProvider {

    // Initiate an URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    private static final int MOVIES = 100;
    private static final int MOVIES_ONE = 101;
    private static final int TRAILERS = 200;
    private static final int TRAILERS_ONE = 201;
    private static final int REVIEWS = 300;
    private static final int REVIEWS_ONE = 301;

    // Must find combined tables
    private static final SQLiteQueryBuilder movieTrailersQueryBuilder;
    private static final SQLiteQueryBuilder movieReviewsQueryBuilder;

    static{
        movieTrailersQueryBuilder = new SQLiteQueryBuilder();
        // This is a inner join which looks like:
        // movies INNER JOIN trailers ON movies.movies_id = trailers.movie_id_trailers_key
        movieTrailersQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME +
                        // All trailers for one particular movie
                        " INNER JOIN " + MoviesContract.TrailersEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.TrailersEntry.TABLE_NAME +
                        "." + MoviesContract.TrailersEntry.COLUMN_MOVIE_ID_TRAILERS_KEY);

        movieReviewsQueryBuilder = new SQLiteQueryBuilder();
        // This is a inner join which looks like:
        // movies INNER JOIN reviews ON movies.movies_id = reviews.movie_id_reviews_key
        movieReviewsQueryBuilder.setTables(
                MoviesContract.MoviesEntry.TABLE_NAME +
                        // All reviews for one particular movie
                        " INNER JOIN " + MoviesContract.TrailersEntry.TABLE_NAME +
                        " ON " + MoviesContract.MoviesEntry.TABLE_NAME +
                        "." + MoviesContract.MoviesEntry.COLUMN_MOVIE_ID +
                        " = " + MoviesContract.ReviewsEntry.TABLE_NAME +
                        "." + MoviesContract.ReviewsEntry.COLUMN_MOVIE_ID_REVIEWS_KEY);
    }

    /*
        This UriMatcher will match each URI to the integer constants defined above.
     */
    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        // For each type of URI to add, create a corresponding code.
        matcher.addURI(authority, MoviesContract.PATH_MOVIES, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIES + "/#", MOVIES_ONE);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS, TRAILERS);
        matcher.addURI(authority, MoviesContract.PATH_TRAILERS + "/#", TRAILERS_ONE);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS, REVIEWS);
        matcher.addURI(authority, MoviesContract.PATH_REVIEWS + "/#", REVIEWS_ONE);
        return matcher;
    }

    /*
        Create a new WeatherDbHelper for later use
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // 'DIR'
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            // 'ITEM'
            case MOVIES_ONE:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            // 'DIR'
            case TRAILERS:
                return MoviesContract.TrailersEntry.CONTENT_TYPE;
            // 'DIR' (possibly zero or one row)
            case TRAILERS_ONE:
                return MoviesContract.TrailersEntry.CONTENT_TYPE;
            // 'DIR'
            case REVIEWS:
                return MoviesContract.ReviewsEntry.CONTENT_TYPE;
            // 'DIR' (possibly zero or one row)
            case REVIEWS_ONE:
                return MoviesContract.ReviewsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movies"
            case MOVIES: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "movies/#"
            case MOVIES_ONE: {
                String movieIdStr = MoviesContract.getMovieIdStrFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MoviesEntry.TABLE_NAME,
                        projection,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movieIdStr},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
//            // "trailers"
//            case TRAILERS: {
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        MoviesContract.TrailersEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
//                break;
//            }
            // "trailers/#"
            case TRAILERS_ONE: {
                String movieIdStr = MoviesContract.getMovieIdStrFromUri(uri);
                retCursor = movieTrailersQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movieIdStr},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
//            // "reviews"
//            case REVIEWS: {
//                retCursor = mOpenHelper.getReadableDatabase().query(
//                        MoviesContract.ReviewsEntry.TABLE_NAME,
//                        projection,
//                        selection,
//                        selectionArgs,
//                        null,
//                        null,
//                        sortOrder
//                );
//                break;
//            }
            // "reviews/#"
            case REVIEWS_ONE: {
                String movieIdStr = MoviesContract.getMovieIdStrFromUri(uri);
                retCursor = movieReviewsQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{movieIdStr},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MoviesEntry.buildMoviesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS: {
                long _id = db.insert(MoviesContract.TrailersEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.TrailersEntry.buildTrailersUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.ReviewsEntry.buildReviewsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Notifying the root Uri will notify all descendants
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIES:
                // Will cause also cascading delete from trailers and reviews tables
                rowsDeleted = db.delete(
                        MoviesContract.MoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MoviesEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRAILERS:
                rowsUpdated = db.update(MoviesContract.TrailersEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEWS:
                rowsUpdated = db.update(MoviesContract.ReviewsEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                int returnCountMovies = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MoviesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCountMovies++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCountMovies;
            case TRAILERS:
                db.beginTransaction();
                int returnCountTrailers = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TrailersEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCountTrailers++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCountTrailers;
            case REVIEWS:
                db.beginTransaction();
                int returnCountReviews = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCountReviews++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCountReviews;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // This is a method specifically to assist the testing framework in running smoothly
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}