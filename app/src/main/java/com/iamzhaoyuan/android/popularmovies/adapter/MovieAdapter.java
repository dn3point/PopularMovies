package com.iamzhaoyuan.android.popularmovies.adapter;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.activity.DetailsActivity;
import com.iamzhaoyuan.android.popularmovies.data.MovieContract.MovieEntry;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.listener.OnLoadMoreListener;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public static final int VIEW_TYPE_POSTER = 0;
    public static final int VIEW_TYPE_LOADING = 1;

    private OnLoadMoreListener mOnLoadMoreListener;

    private Context mContext;
    private List<Movie> mMovieList;

    private boolean isLoading, isFavouriteTab;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public MovieAdapter(Activity context, List<Movie> movies) {
        mContext = context;
        mMovieList = movies;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof PosterViewHolder) {
            final PosterViewHolder posterHolder = (PosterViewHolder)holder;
            final Movie movie = mMovieList.get(position);
            posterHolder.title.setText(movie.getTitle());

            // loading movie cover using Picasso library
            Picasso.with(mContext)
                    .load(MovieUtil.getInstance().getPosterUrl(movie.getImageThumbnail()))
                    .into(posterHolder.poster);
            if (isFavouriteTab) {
                updateFavouriteImage(posterHolder.favourite, movie.isFavourite());
                posterHolder.favourite.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        movie.setFavourite(!movie.isFavourite());
                        updateFavourite(posterHolder.favourite, movie);
                        if (isFavouriteTab && !movie.isFavourite()) {
                            remove(position);
                        }
                    }
                });
                posterHolder.rate.setVisibility(View.INVISIBLE);
            } else {
                posterHolder.rate.setText(movie.getRating() + "");
                posterHolder.favourite.setVisibility(View.INVISIBLE);
            }

            posterHolder.poster.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, DetailsActivity.class);
                    intent.putExtra(
                            mContext.getString(R.string.intent_movie_obj_tag),
                            movie);
                    mContext.startActivity(intent);
                }
            });
        } else if (holder instanceof ProgressViewHolder) {
            ProgressViewHolder progressHolder = (ProgressViewHolder)holder;
            progressHolder.progressBar.setIndeterminate(true);
        } else {
            Log.d(LOG_TAG, "ViewHolder type issue: " + holder.getClass().getSimpleName());
        }

    }

    private void updateFavouriteImage(ImageView favourite, boolean isFavourite) {
        if (isFavourite) {
            favourite.setImageResource(R.drawable.favourite);
        } else {
            favourite.setImageResource(R.drawable.outline);
        }
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
        if (movie.isFavourite()) {
            ContentValues updateValues = new ContentValues();
            updateValues.put(MovieEntry.COLUMN_MOVIE_ID, movie.getId());
            mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, updateValues);
        } else {
            String mSelectionClause = MovieEntry.COLUMN_MOVIE_ID + " = ?";
            String[] mSelectionArgs = {movie.getId()};
            mContext.getContentResolver().delete(MovieEntry.CONTENT_URI, mSelectionClause, mSelectionArgs);
        }
    }

    @Override
    public int getItemCount() {
        return mMovieList == null ? 0 : mMovieList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMovieList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_POSTER;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POSTER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poster_card, parent, false);
            return new PosterViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poster_loading, parent, false);
            return new ProgressViewHolder(view);
        } else {
            Log.d(LOG_TAG, "View type is : " + viewType + "?");
        }
        return null;
    }

    public void clearMovies() {
        int size = mMovieList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mMovieList.remove(0);
            }
            notifyItemRangeRemoved(0, size);
        }
    }

    public void addMovies(List<Movie> movies) {
        int startPosition = mMovieList == null ? 0 : mMovieList.size();
        mMovieList.addAll(movies);
        notifyItemRangeInserted(startPosition, movies.size() - 1);
    }

    public void add(Movie movie) {
        mMovieList.add(movie);
        notifyItemInserted(mMovieList.size() - 1);
    }

    public void remove() {
        mMovieList.remove(mMovieList.size() - 1);
        notifyItemRemoved(mMovieList.size());
    }

    public void remove(int position) {
        mMovieList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }

    public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener;
    }

    public boolean isLastItemNull() {
        if (mMovieList != null && !mMovieList.isEmpty() && mMovieList.get(mMovieList.size() - 1) == null) {
            return true;
        }
        return false;
    }

    public boolean isUpdated(List<String> movieIds) {
        if (mMovieList != null && !mMovieList.isEmpty()) {
            if (mMovieList.size() == movieIds.size()) {
                Set<String> idSet = new HashSet<>(movieIds);
                for (Movie movie : mMovieList) {
                    idSet.remove(movie.getId());
                }
                if (idSet.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(int totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public int getLastVisibleItem() {
        return lastVisibleItem;
    }

    public void setLastVisibleItem(int lastVisibleItem) {
        this.lastVisibleItem = lastVisibleItem;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public int getVisibleThreshold() {
        return visibleThreshold;
    }

    public void setFavouriteTab(boolean favouriteTab) {
        isFavouriteTab = favouriteTab;
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return mOnLoadMoreListener;
    }

    class PosterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title) TextView title;
        @BindView(R.id.poster) ImageView poster;
        @BindView(R.id.favourite) ImageView favourite;
        @BindView(R.id.rate) TextView rate;

        PosterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            // TODO change height for tablet
            poster.getLayoutParams().height = (size.y << 1) / 5;
        }

    }

    class ProgressViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        ProgressViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
