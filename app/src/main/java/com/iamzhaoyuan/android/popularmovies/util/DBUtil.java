package com.iamzhaoyuan.android.popularmovies.util;

import android.content.Context;
import android.database.Cursor;

import com.iamzhaoyuan.android.popularmovies.data.MovieContract;
import com.iamzhaoyuan.android.popularmovies.fragment.PosterFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yzhao on 6/12/2016.
 */

public class DBUtil {
    private static DBUtil instance = null;

    private static final String[] FAVOURITE_PROJECTION = new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID};

    private DBUtil() {}

    public synchronized static DBUtil getInstance() {
        if (instance == null) instance = new DBUtil();
        return instance;
    }

    public boolean isFavourite(Context context, String movieId) {
        String mSelectionClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] mSelectionArgs = { movieId };
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI, FAVOURITE_PROJECTION, null, null, null);
            try {
                cursor = context.getContentResolver().query(
                        MovieContract.MovieEntry.CONTENT_URI, FAVOURITE_PROJECTION, mSelectionClause, mSelectionArgs, null);
                return cursor.moveToFirst();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
