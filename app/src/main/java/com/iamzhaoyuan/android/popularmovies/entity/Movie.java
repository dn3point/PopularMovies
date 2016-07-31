package com.iamzhaoyuan.android.popularmovies.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yuan on 29/7/16.
 */
public class Movie implements Parcelable {
    private String mTitle;
    private String mImageThumbnail;
    private String mOverview;
    private double mRating;
    private String mReleaseDate;

    public Movie(String title,
                 String imageThumbnail,
                 String overview,
                 double rating,
                 String releaseDate) {
        mTitle = title;
        mImageThumbnail = imageThumbnail;
        mOverview = overview;
        mRating = rating;
        mReleaseDate = releaseDate;
    }

    private Movie(Parcel in) {
        mTitle = in.readString();
        mImageThumbnail = in.readString();
        mOverview = in.readString();
        mRating = in.readDouble();
        mReleaseDate = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mImageThumbnail);
        dest.writeString(mOverview);
        dest.writeDouble(mRating);
        dest.writeString(mReleaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR =
            new Parcelable.Creator<Movie>(){
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    /**
     * Getters and Setters
     */
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getImageThumbnail() {
        return mImageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        mImageThumbnail = imageThumbnail;
    }

    public String getOverview() {
        return mOverview;
    }

    public void setOverview(String overview) {
        mOverview = overview;
    }

    public double getRating() {
        return mRating;
    }

    public void setRating(double rating) {
        mRating = rating;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        mReleaseDate = releaseDate;
    }
}
