package com.ludgo.android.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Utility {

    // # is private key for developer
    public static final String API_KEY = "#";

    /**
     * @param ending is in format '/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg'
     * @return is in format 'http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg'
     */
    public static String createUrlFromEnding(String ending) {
        final String URL_BASE = "http://image.tmdb.org/t/p/w185/";
        return URL_BASE + ending;
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

    // @return is either 'popularity.desc' or 'vote_average.desc'
    public static String getOrderRule(Context context) {
        // Find rule how to order movies in grid
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_entryValues_default));
    }
}