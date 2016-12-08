package com.iamzhaoyuan.android.popularmovies.util;

import android.content.ContentValues;
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
    private static final int INDEX_MOVIE_ID = 0;

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
                    MovieContract.MovieEntry.CONTENT_URI, FAVOURITE_PROJECTION, mSelectionClause, mSelectionArgs, null);
            return cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public List<String> getFavouriteMovieIds(Context context) {
        Cursor cursor = null;
        List<String> favMovieIds = null;
        try {
            cursor = context.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI, FAVOURITE_PROJECTION, null, null, null);
            favMovieIds = new ArrayList<>();
            while (cursor.moveToNext()) {
                favMovieIds.add(cursor.getString(INDEX_MOVIE_ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return favMovieIds;
    }

    public void deleteFavMovie(Context context, String id) {
        String mSelectionClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] mSelectionArgs = {id};
        context.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, mSelectionClause, mSelectionArgs);
    }

    public void insertFavMovie(Context context, String id) {
        ContentValues updateValues = new ContentValues();
        updateValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, id);
        context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, updateValues);
    }

}
