package com.iamzhaoyuan.android.popularmovies.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    @BindView(R.id.movie_title) TextView mTitleTextView;
    @BindView(R.id.movie_poster) ImageView mPosterImageView;
    @BindView(R.id.movie_release_date) TextView mReleaseDateTextView;
    @BindView(R.id.movie_rating) TextView mRatingTextView;
    @BindView(R.id.movie_overview) TextView mOverviewTextView;

    public DetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =
                inflater.inflate(R.layout.fragment_details, container, false);
        // Get Movie Obj from intent
        Intent intent = getActivity().getIntent();
        Movie movie = null;
        if (intent != null) {
            movie = intent.getExtras().
                    getParcelable(getString(R.string.intent_movie_obj_tag));
        } else {
            Log.d(LOG_TAG, "Intent from MainActivity is null?");
        }

        if (movie != null) {
            ButterKnife.bind(this, rootView);
            // Set contents
            final String RATING_SUFFIX = "/10";
            MovieUtil movieUtil = MovieUtil.getInstance();
            String posterUrl = movieUtil.getPosterUrl(movie.getImageThumbnail());

            mTitleTextView.setText(movie.getTitle());
            mReleaseDateTextView.setText(movie.getReleaseDate().substring(0, 4));
            mRatingTextView.setText(movie.getRating() + RATING_SUFFIX);
            mOverviewTextView.setText(movie.getOverview());
            Picasso.with(getContext()).load(posterUrl).into(mPosterImageView);

        } else {
            Log.d(LOG_TAG, "Movie obj passed from MainActivity is null?");
        }
        return rootView;
    }

}
