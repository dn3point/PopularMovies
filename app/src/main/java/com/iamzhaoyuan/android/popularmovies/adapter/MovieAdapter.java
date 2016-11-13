package com.iamzhaoyuan.android.popularmovies.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.activity.DetailsActivity;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MyViewHolder> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR =
            new OvershootInterpolator(4);

    private Context mContext;
    private List<Movie> mMovieList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title) TextView title;
        @BindView(R.id.poster) ImageView poster;
        @BindView(R.id.favourite) ImageView favourite;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    public MovieAdapter(Activity context, List<Movie> movies) {
        mContext = context;
        mMovieList = movies;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poster_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Movie movie = mMovieList.get(position);
        holder.title.setText(movie.getTitle());

        // loading movie cover using Picasso library
        Picasso.with(mContext)
                .load(MovieUtil.getInstance().getPosterUrl(movie.getImageThumbnail()))
                .into(holder.poster);

        updateFavouriteImage(holder.favourite, movie.isFavourite());
        holder.favourite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                movie.setFavourite(!movie.isFavourite());
                updateFavourite(holder.favourite, movie);
            }
        });

        holder.poster.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, DetailsActivity.class);
                intent.putExtra(
                        mContext.getString(R.string.intent_movie_obj_tag),
                        movie);
                mContext.startActivity(intent);
            }
        });

    }

    private void updateFavouriteImage(ImageView favourite, boolean isFavourite) {
        if (isFavourite) {
            likeAnimations(favourite);
        } else {
            favourite.setImageResource(R.drawable.outline);
        }
    }

    private void likeAnimations(final ImageView favourite) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(favourite, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(favourite, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                favourite.setImageResource(R.drawable.favourite);
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }

    private void updateFavourite(ImageView favourite, Movie movie) {
        if (movie != null) {
            updateFavouriteImage(favourite, movie.isFavourite());
            updateFavouriteDB(movie);
        } else {
            Log.e(LOG_TAG, "Movie is null in updateFavourite() method?");
        }
    }

    private void updateFavouriteDB(Movie movie) {
        // TODO implement codes
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public void clearMovies() {
        int size = mMovieList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mMovieList.remove(0);
            }

            this.notifyItemRangeRemoved(0, size);
        }
    }

    public void addMovies(List<Movie> movies) {
        mMovieList.addAll(movies);
        this.notifyItemRangeInserted(0, movies.size() - 1);
    }
}
