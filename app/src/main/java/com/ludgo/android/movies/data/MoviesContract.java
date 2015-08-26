package com.ludgo.android.movies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines tables and column names for the movies database.
 */
public final class MoviesContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public MoviesContract() {}

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.ludgo.android.movies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_MOVIES = "movies";
    public static final String PATH_TRAILERS = "trailers";
    public static final String PATH_REVIEWS = "reviews";

    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        // URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;
        // URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIES;

        public static final String TABLE_NAME = "movies";

        // Integer representing unique movie id at API
        public static final String COLUMN_MOVIE_ID = "movie_id";
        // String
        public static final String COLUMN_TITLE = "title";
        // String
        public static final String COLUMN_OVERVIEW = "overview";
        // String representing the end of image Url
        public static final String COLUMN_POSTER_PATH = "poster_path";
        // String in format 'YYYY-MM-DD'
        public static final String COLUMN_RELEASE_DATE = "release_date";
        // Double from 0 to 10 representing user evaluation, higher means better
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        // Double representing user interest, higher means more popular
        public static final String COLUMN_POPULARITY = "popularity";
        // Integer representing boolean
        public static final String COLUMN_FAVORITE = "favorite";


        public static Uri buildMoviesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMoviesUriWithId(int movieId) {
            String movieIdStr = Integer.toString(movieId);
            return CONTENT_URI.buildUpon().appendPath(movieIdStr).build();
        }
    }

    public static final class TrailersEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILERS).build();

        // URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;
        // URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILERS;

        public static final String TABLE_NAME = "trailers";

        // String representing unique trailer id at API
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        // String
        public static final String COLUMN_NAME = "name";
        // String representing key as the part of Url for video at Youtube
        public static final String COLUMN_KEY = "key";
        // Integer representing foreign key
        public static final String COLUMN_MOVIE_ID_TRAILERS_KEY = "movie_id_trailers_key";


        public static Uri buildTrailersUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailersUriWithId(int movieId) {
            String movieIdStr = Integer.toString(movieId);
            return CONTENT_URI.buildUpon().appendPath(movieIdStr).build();
        }
    }

    public static final class ReviewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        // URI containing a Cursor of zero or more items.
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;
        // URI containing a Cursor of a single item.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEWS;

        public static final String TABLE_NAME = "reviews";

        // String representing unique review id at API
        public static final String COLUMN_REVIEW_ID = "review_id";
        // String
        public static final String COLUMN_AUTHOR = "author";
        // String
        public static final String COLUMN_CONTENT = "content";
        // Integer representing foreign key
        public static final String COLUMN_MOVIE_ID_REVIEWS_KEY = "movie_id_reviews_key";


        public static Uri buildReviewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReviewsUriWithId(int movieId) {
            String movieIdStr = Integer.toString(movieId);
            return CONTENT_URI.buildUpon().appendPath(movieIdStr).build();
        }
    }

    /**
     * @return movie id as String got from the only one segment of Uri
     */
    public static String getMovieIdStrFromUri(Uri uri) {
        return uri.getPathSegments().get(1);
    }
}