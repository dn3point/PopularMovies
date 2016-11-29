package com.iamzhaoyuan.android.popularmovies.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.BuildConfig;
import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.adapter.TrailerAdapter;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.util.GridSpacingItemDecoration;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.iamzhaoyuan.android.popularmovies.util.NetworkUtil;
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
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    @BindView(R.id.movie_title) TextView mTitleTextView;
    @BindView(R.id.movie_release_date) TextView mReleaseDateTextView;
    @BindView(R.id.movie_rating) TextView mRatingTextView;
    @BindView(R.id.movie_overview) TextView mOverviewTextView;
    @BindView(R.id.trailers) RecyclerView mRecyclerView;

    private Movie mMovie;
    private TrailerAdapter mTrailerAdapter;

    public DetailsFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovie != null) {
            updateTrailer(mMovie.getId());
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
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mMovie = intent.getExtras().getParcelable(getString(R.string.intent_movie_obj_tag));
        } else {
            Log.d(LOG_TAG, "Intent from MainActivity is null?");
        }
        if (mMovie != null) {
            ButterKnife.bind(this, rootView);
            // Set contents\
            MovieUtil movieUtil = MovieUtil.getInstance();
            String posterUrl = movieUtil.getPosterUrl(mMovie.getImageThumbnail());

            mTitleTextView.setText(mMovie.getTitle());
            mReleaseDateTextView.setText(
                    getActivity().getString(R.string.movie_released_date_prefix) +
                            mMovie.getReleaseDate());
            mRatingTextView.setText(
                    getActivity().getString(R.string.movie_rating_prefix) + mMovie.getRating());
            mOverviewTextView.setText(mMovie.getOverview());

            mTrailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<String>());
            RecyclerView.LayoutManager layoutManager =
                    new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mTrailerAdapter);

        } else {
            Log.d(LOG_TAG, "Movie obj passed from MainActivity is null?");
        }
        return rootView;
    }

    private void updateTrailer(String movieId) {
        new FetchMovieTrailerTask().execute(movieId);
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
                final String MOVIE_BASE_URL =
                        "https://api.themoviedb.org/3/movie/";
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

            JSONObject forecastJson = new JSONObject(trailerJsonStr);
            JSONArray videoArray = forecastJson.getJSONArray(NODE_RESULTS);

            List<String> resultList = new ArrayList<>();

            for (int i = 0; i < videoArray.length(); i++) {
                JSONObject videoObj = videoArray.getJSONObject(i);
                if (YOUTUBE.equals(videoObj.getString(NODE_SITE))) {
                    resultList.add(
                            MovieUtil.getInstance().getTrailerUrl(videoObj.getString(Node_KEY)));
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
}
