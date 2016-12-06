package com.iamzhaoyuan.android.popularmovies.data;

import android.content.Context;
import com.iamzhaoyuan.android.popularmovies.data.MovieContract.MovieEntry;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class MovieDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movie.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_FAV_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY);";
        db.execSQL(SQL_CREATE_FAV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
