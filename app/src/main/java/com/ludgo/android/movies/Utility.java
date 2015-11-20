package com.ludgo.android.movies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.TypedValue;

import com.ludgo.android.movies.service.MoviesService;

public class Utility {

    /**
     * @param ending    is in format '/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg'
     * @param viewWidth is the width of ImageView in pixels
     */
    public static String createPosterUrl(String ending, int viewWidth) {
        final String URL_BASE = "http://image.tmdb.org/t/p/";
        String posterWidth;
        if (viewWidth <= 92) {
            posterWidth = "w92";
        } else if (viewWidth <= 154) {
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
    public static String createYearFromReleaseDate(String releaseDate) {
        if (releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return releaseDate;
    }

    public static String createYoutubeUrlFromKey(String key) {
        return "http://www.youtube.com/watch?v=" + key;
    }

    /*
        Find rule how to order movies in grid
     */
    public static String getSortRule(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_entryValues_default));
    }

    /*
        Find rule what to display in grid
     */
    public static String getShowRule(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_show_key),
                context.getString(R.string.pref_show_entryValues_default));
    }

    /**
     * @return true if year preference should be used
     */
    public static boolean getYearBoolean(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.pref_enable_year_key), false);
    }

    /**
     * @return year from which movies to display in grid
     */
    public static String getPreferredYear(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(R.string.pref_year_key), "");
    }

    /**
     * @return the theme to style UI
     */
    public static int getPreferredTheme(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String themeString = preferences.getString(context.getString(R.string.pref_theme_key),
                context.getString(R.string.pref_theme_entryValues_default));
        if (themeString.equals(context.getString(R.string.pref_theme_entryValues_indigo))) {
            return R.style.Movies_ThemeIndigo;
        } else if (themeString.equals(context.getString(R.string.pref_theme_entryValues_deepOrange))) {
            return R.style.Movies_ThemeDeepOrange;
        } else if (themeString.equals(context.getString(R.string.pref_theme_entryValues_pink))) {
            return R.style.Movies_ThemePink;
        } else if (themeString.equals(context.getString(R.string.pref_theme_entryValues_blueGrey))) {
            return R.style.Movies_ThemeBlueGrey;
        } else {
            return R.style.Movies_ThemeTeal;
        }
    }

    /**
     * To be used only in activities with derived theme
     *
     * @return refresh image with respect to the current theme
     */
    public static Drawable getRefreshDrawable(Context context) {
        TypedArray attr = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.theme_refresh_selector});
        Drawable drawable = attr.getDrawable(0);
        attr.recycle();
        return drawable;
    }

    /**
     * @return true if the network is available
     */
    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager cm =
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * @param dipValue is number of density independent pixels
     * @return value is number of physical pixels
     */
    public static int dipToPx(final Context context, final int dipValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue,
                context.getResources().getDisplayMetrics());
    }

    /**
     * @return integer describing the movies server status response type or unknown by default
     */
    @SuppressWarnings("ResourceType")
    public static
    @MoviesService.MoviesStatus
    int getMoviesStatus(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(context.getString(R.string.pref_movies_status_key),
                MoviesService.MOVIES_STATUS_UNKNOWN);
    }
}