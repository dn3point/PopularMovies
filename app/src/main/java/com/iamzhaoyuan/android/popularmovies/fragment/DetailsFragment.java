package com.iamzhaoyuan.android.popularmovies.fragment;


import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.BuildConfig;
import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.adapter.MovieReviewAdapter;
import com.iamzhaoyuan.android.popularmovies.adapter.TrailerAdapter;
import com.iamzhaoyuan.android.popularmovies.data.MovieContract;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.entity.MovieReview;
import com.iamzhaoyuan.android.popularmovies.util.DBUtil;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    @BindView(R.id.movie_title) TextView mTitleTextView;
    @BindView(R.id.movie_release_date) TextView mReleaseDateTextView;
    @BindView(R.id.movie_rating) TextView mRatingTextView;
    @BindView(R.id.movie_overview) TextView mOverviewTextView;
    @BindView(R.id.trailers) RecyclerView mTrailerRecyclerView;
    @BindView(R.id.reviews) RecyclerView mReviewRecyclerView;
    @BindView(R.id.movie_trailer_title) TextView mTrailerTitle;
    @BindView(R.id.movie_review_title) TextView mReviewTitle;

    private Movie mMovie;
    private TrailerAdapter mTrailerAdapter;
    private MovieReviewAdapter mMovieReviewAdapter;

    public DetailsFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovie != null) {
            updateInfo(mMovie.getId());
        } else {
            Log.d(LOG_TAG, "Movie is null?");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =
                inflater.inflate(R.layout.fragment_details, container, false);
        // Get Movie Obj from intent
        if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            mMovie = getActivity().getIntent().getExtras().getParcelable(getString(R.string.intent_movie_obj_tag));
        } else if (getArguments() != null) {
            mMovie = getArguments().getParcelable(getString(R.string.intent_movie_obj_tag));
        } else {
            Log.d(LOG_TAG, "Intent from MainActivity is null?");
        }
        if (mMovie != null) {
            Log.i(LOG_TAG, mMovie.getId());
            ButterKnife.bind(this, rootView);
            // Set contents
            if (rootView.findViewById(R.id.movie_backdrop) != null) {
                ImageView backdrop = (ImageView) rootView.findViewById(R.id.movie_backdrop);
                Picasso.with(getContext()).load(MovieUtil.getInstance().getBackdropUrl(mMovie.getBackdrop())).into(backdrop);
            }

            if (rootView.findViewById(R.id.fav_btn) != null) {
                FloatingActionButton favBtn = (FloatingActionButton) rootView.findViewById(R.id.fav_btn);
                favBtn.setVisibility(View.VISIBLE);
                mMovie.setFavourite(DBUtil.getInstance().isFavourite(getActivity(), mMovie.getId()));
                if (mMovie.isFavourite()) {
                    favBtn.setImageDrawable(getActivity().getDrawable(R.drawable.fav_white));
                } else {
                    favBtn.setImageDrawable(getActivity().getDrawable(R.drawable.ol_white));
                }
                favBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mMovie.isFavourite()) {
                            ((FloatingActionButton) v).setImageDrawable(getActivity().getDrawable(R.drawable.ol_white));
                            Snackbar.make(v, "Removed from favourite", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            mMovie.setFavourite(false);
                            DBUtil.getInstance().deleteFavMovie(getContext(), mMovie.getId());
                        } else {
                            ((FloatingActionButton) v).setImageDrawable(getActivity().getDrawable(R.drawable.fav_white));
                            Snackbar.make(v, "Added to favourite", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            mMovie.setFavourite(true);
                            DBUtil.getInstance().insertFavMovie(getContext(), mMovie.getId());
                        }
                    }
                });
            }

            mTitleTextView.setText(mMovie.getTitle());
            mReleaseDateTextView.setText(getActivity().getString(R.string.movie_released_date_prefix) + mMovie.getReleaseDate());
            mRatingTextView.setText(getActivity().getString(R.string.movie_rating_prefix) + mMovie.getRating());
            mOverviewTextView.setBackground(getActivity().getDrawable(R.drawable.overview_border));
            mOverviewTextView.setText(mMovie.getOverview());

            mTrailerTitle.setText(getString(R.string.movie_trailer_title));
            mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<String>());
            LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            mTrailerRecyclerView.setLayoutManager(trailerLayoutManager);
            mTrailerRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mTrailerRecyclerView.setAdapter(mTrailerAdapter);

            mReviewTitle.setText(getString(R.string.movie_review_title));
            mMovieReviewAdapter = new MovieReviewAdapter(getActivity(), new ArrayList<MovieReview>());
            LinearLayoutManager  reviewLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            mReviewRecyclerView.setLayoutManager(reviewLayoutManager);
            mReviewRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mReviewRecyclerView.setAdapter(mMovieReviewAdapter);

        } else {
            Log.d(LOG_TAG, "Movie obj passed from MainActivity is null?");
        }
        return rootView;
    }

    private void updateInfo(String movieId) {
        new FetchMovieTrailerTask().execute(movieId);
        new FetchMovieReviewTask().execute(movieId);
    }

    public class FetchMovieTrailerTask extends AsyncTask<String, Void, List<String>> {
        private final String LOG_TAG = FetchMovieTrailerTask.class.getSimpleName();

        @Override
        protected List<String> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieId = params[0];
            String trailerJsonStr = null;
            try {
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";
                final String VIDEO_PATH = "videos";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(movieId)
                        .appendPath(VIDEO_PATH)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                trailerJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailerFromJson(trailerJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private List<String> getTrailerFromJson(String trailerJsonStr) throws JSONException{
            final String NODE_RESULTS = "results";
            final String NODE_SITE = "site";
            final String Node_KEY = "key";
            final String YOUTUBE = "YouTube";

            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray videoArray = trailerJson.getJSONArray(NODE_RESULTS);

            List<String> resultList = new ArrayList<>();

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject videoObj = videoArray.getJSONObject(i);
                if (YOUTUBE.equals(videoObj.getString(NODE_SITE))) {
                    resultList.add(videoObj.getString(Node_KEY));
                }
            }

            return resultList;
        }

        @Override
        protected void onPostExecute(List<String> trailerKeyList) {
            if (trailerKeyList != null) {
                mTrailerAdapter.clearTrailers();;
                mTrailerAdapter.addTrailers(trailerKeyList);
            }
        }
    }

    public class FetchMovieReviewTask extends AsyncTask<String, Void, List<MovieReview>> {
        private final String LOG_TAG = FetchMovieReviewTask.class.getSimpleName();

        @Override
        protected List<MovieReview> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieId = params[0];
            String reviewJsonStr = null;
            try {
                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";
                final String REVIEW_PATH = "reviews";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(movieId)
                        .appendPath(REVIEW_PATH)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                reviewJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewFromJson(reviewJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private List<MovieReview> getReviewFromJson(String trailerJsonStr) throws JSONException {
            final String NODE_RESULTS = "results";
            final String NODE_AUTHOR = "author";
            final String Node_CONTENT = "content";

            JSONObject reviewJson = new JSONObject(trailerJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(NODE_RESULTS);

            List<MovieReview> resultList = new ArrayList<>();

            for (int i = 0; i < reviewArray.length(); i++) {
                JSONObject reviewObj = reviewArray.getJSONObject(i);
                String author = reviewObj.getString(NODE_AUTHOR);
                String comment = reviewObj.getString(Node_CONTENT);
                resultList.add(new MovieReview(author, comment));
            }

            return resultList;
        }

        @Override
        protected void onPostExecute(List<MovieReview> movieReviews) {
            if (movieReviews != null) {
                mMovieReviewAdapter.clearMovieReviews();
                mMovieReviewAdapter.addMovieReviews(movieReviews);
            };
        }
    }
}
