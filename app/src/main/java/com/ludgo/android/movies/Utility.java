package com.ludgo.android.movies;

public class Utility {
    public static String createUrlFromEnding(String ending) {
        final String URL_BASE = "http://image.tmdb.org/t/p/w185/";
        String urlString = URL_BASE + ending;
        return urlString;
    }

    /**
     *
     * @param releaseDate is in format '2015-01-01'
     * @return is in format '2015'
     */
    public static String createYearFromReleaseDate(String releaseDate){
        if (releaseDate.length() >= 4){
            String year = releaseDate.substring(0,4);
            return year;
        }
        return releaseDate;
    }
}
