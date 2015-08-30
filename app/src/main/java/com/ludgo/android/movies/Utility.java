package com.ludgo.android.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {

    // # is private key for developer
    public static final String API_KEY = "#";

    /**
     * @param ending is in format '/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg'
     * @param viewWidth is the width of ImageView in pixels
     */
    public static String createPosterUrl(String ending, int viewWidth) {
        final String URL_BASE = "http://image.tmdb.org/t/p/";
        String posterWidth;
        if (viewWidth <= 92 ){
            posterWidth = "w92";
        } else if (viewWidth <= 154 ){
            posterWidth = "w154";
        } else if (viewWidth <= 240) {
            // Rather prefer enlarging the image (to such extent that the quality is acceptable)
            // than choosing  the bigger one in order to assure performance of older phones
            // with low pixel densities
            posterWidth = "w185";
        } else {
            posterWidth = "w342";
        }
        return URL_BASE + posterWidth + ending;
    }

    /**
     * @param releaseDate is in format '2015-01-01'
     * @return is in format '2015'
     */
    public static String createYearFromReleaseDate(String releaseDate){
        if (releaseDate.length() >= 4){
            return releaseDate.substring(0,4);
        }
        return releaseDate;
    }

    public static String createYoutubeUrlFromKey (String key) {
        return "http://www.youtube.com/watch?v=" + key;
    }

    // Find rule how to order movies in grid
    public static String getSortRule(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_entryValues_default));
    }

    // Find rule what to display in grid
    public static String getShowRule(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_show_key),
                context.getString(R.string.pref_show_entryValues_default));
    }
}