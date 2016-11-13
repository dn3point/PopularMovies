package com.iamzhaoyuan.android.popularmovies.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamzhaoyuan.android.popularmovies.BuildConfig;
import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.adapter.MovieAdapter;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.util.NetworkUtil;

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
public class PosterFragment extends Fragment {
    private static final String LOG_TAG = PosterFragment.class.getSimpleName();
    private static final String SORT_BY_KEY = "sort_by";

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    private MovieAdapter mImageAdapter;
    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkUtil networkUtil = NetworkUtil.getInstance();
            if (networkUtil.isNetworkConnected(context)) {
                updatePosters();
            }
        }
    };

    public PosterFragment() {
    }

    public static PosterFragment newInstance(String sortBy) {
        PosterFragment fragment = new PosterFragment();

        Bundle args = new Bundle();
        args.putString(SORT_BY_KEY, sortBy);
        fragment.setArguments(args);

        return  fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (NetworkUtil.getInstance().isNetworkConnected(getActivity())) {
            updatePosters();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mNetworkReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mNetworkReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_poster, container, false);
        ButterKnife.bind(this, rootView);

        mImageAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mImageAdapter);

        return rootView;
    }

    private void updatePosters() {
        if (getString(R.string.pref_sort_by_favourite)
                .equals(getArguments().getString(SORT_BY_KEY))) {
            // TODO Fetch favourite movies from DB
        } else {
            FetchMovieTask task = new FetchMovieTask();
            task.execute();
        }
    }

    public class FetchMovieTask extends AsyncTask<Void, Void, List<Movie>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            // String sortBy = getSortBy();
            String sortBy = getArguments().getString(getString(R.string.pref_sort_by_key));
            try {
                final String MOVIE_BASE_URL =
                        "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(sortBy)
                        .appendQueryParameter(APIKEY_PARAM, BuildConfig.THEMOVIEDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to The Movie DB, and open the connection
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
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Get sort by value from pref
         * @return
         */
        private String getSortBy() {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortBy =
                    pref.getString(
                            getString(R.string.pref_sort_by_key),
                            getString(R.string.pref_sort_by_popular));
            return sortBy;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                mImageAdapter.clearMovies();
                mImageAdapter.addMovies(movies);
            }
        }

        private List<Movie> getMovieDataFromJson(String moviesJsonStr)
            throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String NODE_RESULTS = "results";
            final String NODE_POSTER_PATH = "poster_path";
            final String NODE_OVERVIEW = "overview";
            final String NODE_RELEASE_DATE = "release_date";
            final String NODE_ORIGINAL_TITLE = "original_title";
            final String NODE_VOTE_AVERAGE = "vote_average";
            final String NODE_ID = "id";

            JSONObject forecastJson = new JSONObject(moviesJsonStr);
            JSONArray movieArray = forecastJson.getJSONArray(NODE_RESULTS);

            List<Movie> resultList = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieObj = movieArray.getJSONObject(i);
                String posterPath = movieObj.getString(NODE_POSTER_PATH);
                String overview = movieObj.getString(NODE_OVERVIEW);
                String releaseDate = movieObj.getString(NODE_RELEASE_DATE);
                String title = movieObj.getString(NODE_ORIGINAL_TITLE);
                double rating = movieObj.getDouble(NODE_VOTE_AVERAGE);
                String id = movieObj.getString(NODE_ID);
                boolean isFavourite = false; // TODO should get from DB


                Movie movie = new Movie(
                        title,
                        posterPath,
                        overview,
                        rating,
                        releaseDate,
                        id,
                        isFavourite);

                resultList.add(movie);
            }

            return resultList;
        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
