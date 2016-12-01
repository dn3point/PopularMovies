package com.iamzhaoyuan.android.popularmovies.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieReview implements Parcelable {
    private String mAuthor;
    private String mComment;

    public MovieReview(String author, String comment) {
        mAuthor = author;
        mComment = comment;
    }

    private MovieReview(Parcel in) {
        mAuthor = in.readString();
        mComment = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthor);
        dest.writeString(mComment);
    }

    public static final Parcelable.Creator<MovieReview> CREATOR = new Parcelable.Creator<MovieReview>(){
        @Override
        public MovieReview createFromParcel(Parcel parcel) {
            return new MovieReview(parcel);
        }

        @Override
        public MovieReview[] newArray(int i) {
            return new MovieReview[i];
        }
    };

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        mComment = comment;
    }
}
