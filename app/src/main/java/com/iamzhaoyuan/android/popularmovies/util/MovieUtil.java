package com.iamzhaoyuan.android.popularmovies.util;

import android.net.Uri;

public class MovieUtil {
    private static final String LOG_TAG = MovieUtil.class.getSimpleName();

    private static final String THEMOVIEDB_BASE_URL = "http://image.tmdb.org/t/p";
    private static final String THUMBNAIL_PREFIX = "http://i3.ytimg.com/vi/";
    private static final String THUMBNAIL_AFFIX = "/hqdefault.jpg";
    private static MovieUtil instance = null;

    private MovieUtil() {

    }

    public static synchronized MovieUtil getInstance() {
        if (instance == null) instance = new MovieUtil();
        return instance;
    }

    public String getPosterUrl(String poster) {
        return getImageUrl(poster, "w185");
    }

    public String getBackdropUrl(String backdrop) {
        return getImageUrl(backdrop, "w500");
    }

    private String getImageUrl(String image, String size) {
        Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                .appendPath(size)
                .appendEncodedPath(image)
                .build();

        return builtUri.toString();
    }

    public String getTrailerThumbnail(String key) {
        return new StringBuilder(THUMBNAIL_PREFIX).append(key).append(THUMBNAIL_AFFIX).toString();
    }

}
