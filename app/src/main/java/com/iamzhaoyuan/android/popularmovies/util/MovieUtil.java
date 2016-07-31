package com.iamzhaoyuan.android.popularmovies.util;

import android.net.Uri;

/**
 * Created by yuan on 31/7/16.
 */
public class MovieUtil {
    private static MovieUtil instance = null;

    private MovieUtil() {

    }

    public static synchronized MovieUtil getInstance() {
        if (instance == null) instance = new MovieUtil();
        return instance;
    }

    public String getPosterUrl(String imageThumbnail) {
        final String THEMOVIEDB_BASE_URL = "http://image.tmdb.org/t/p";
        String imageSize = "w185";

        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                .appendPath(imageSize)
                .appendEncodedPath(imageThumbnail)
                .build();

        return builtUri.toString();
    }
}
