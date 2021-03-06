package com.iamzhaoyuan.android.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.entity.MovieReview;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.MyViewHolder> {
    private static final String LOG_TAG = MovieReviewAdapter.class.getSimpleName();

    private Context mContext;
    private List<MovieReview> mReviewList;

    public MovieReviewAdapter(Context context, List<MovieReview> reviewList) {
        mContext = context;
        mReviewList = reviewList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.review, parent, false);

        return new MovieReviewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MovieReview movieReview = mReviewList.get(position);
        holder.authorText.setText(movieReview.getAuthor());
        holder.commentText.setText(movieReview.getComment());
        if (position == mReviewList.size() - 1) {
            holder.itemView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return mReviewList.size();
    }

    public void clearMovieReviews() {
        int size = mReviewList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mReviewList.remove(0);
            }
            notifyItemRangeRemoved(0, size);
        }
    }

    public void addMovieReviews(List<MovieReview> movieReviews) {
        mReviewList.addAll(movieReviews);
        notifyItemRangeInserted(0, movieReviews.size() - 1);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.movie_review_author)
        TextView authorText;
        @BindView(R.id.movie_review)
        ExpandableTextView commentText;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
