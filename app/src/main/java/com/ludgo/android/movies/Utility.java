package com.ludgo.android.movies;

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
}