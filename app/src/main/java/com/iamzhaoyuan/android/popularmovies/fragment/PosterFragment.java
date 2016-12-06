package com.iamzhaoyuan.android.popularmovies.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iamzhaoyuan.android.popularmovies.BuildConfig;
import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.adapter.MovieAdapter;
import com.iamzhaoyuan.android.popularmovies.data.MovieContract.MovieEntry;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.listener.OnLoadMoreListener;
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
    private int page = 1;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    private View mView;

    private static final String[] FAVOURITE_PROJECTION = new String[]{MovieEntry.COLUMN_MOVIE_ID};

    private static final int INDEX_MOVIE_ID = 0;

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

        return fragment;
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
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_poster, container, false);
            ButterKnife.bind(this, mView);

            mImageAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
            RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mImageAdapter);
            if (!isFavouriteTab()) {
                mImageAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        Log.i(LOG_TAG, "Load more");
                        mImageAdapter.add(null);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(LOG_TAG, "Load more in thread");
                                updatePosters();
                                mImageAdapter.setLoading(false);
                            }
                        }, 5000);
                    }
                });

                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                        MovieAdapter movieAdapter = (MovieAdapter) recyclerView.getAdapter();
                        movieAdapter.setTotalItemCount(gridLayoutManager.getItemCount());
                        movieAdapter.setLastVisibleItem(gridLayoutManager.findLastVisibleItemPosition());

                        if (!movieAdapter.isLoading() &&
                                movieAdapter.getTotalItemCount() <= (movieAdapter.getLastVisibleItem() + movieAdapter.getVisibleThreshold())) {
                            if (movieAdapter.getOnLoadMoreListener() != null) {
                                movieAdapter.getOnLoadMoreListener().onLoadMore();
                            }
                            movieAdapter.setLoading(true);
                        }
                    }
                });
            } else {
                mImageAdapter.setFavouriteTab(true);
            }
            mRecyclerView.setLayoutManager(layoutManager);
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch (mImageAdapter.getItemViewType(position)) {
                        case MovieAdapter.VIEW_TYPE_LOADING:
                            return 2;
                        case MovieAdapter.VIEW_TYPE_POSTER:
                            return 1;
                        default:
                            return -1;
                    }
                }
            });
        }
        return mView;
    }

    private boolean isFavouriteTab() {
        return getString(R.string.pref_sort_by_favourite).equals(getArguments().getString(SORT_BY_KEY));
    }

    private void updatePosters() {
        if (isFavouriteTab()) {
            Cursor cursor = null;
            try {
                cursor = getContext().getContentResolver().query(
                        MovieEntry.CONTENT_URI, FAVOURITE_PROJECTION, null, null, null);
                List<String> favMovieIds = new ArrayList<>();
                while (cursor.moveToNext()) {
                    favMovieIds.add(cursor.getString(INDEX_MOVIE_ID));
                }
                if (!favMovieIds.isEmpty()) {
                    new FetchFavouriteMovieTask().execute(favMovieIds);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        } else {
            new FetchMovieTask().execute(page);
            page++;
        }
    }

    public class FetchMovieTask extends AsyncTask<Integer, Void, List<Movie>> {
        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(Integer... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            // String sortBy = getSortBy();
            String sortBy = getArguments().getString(getString(R.string.pref_sort_by_key));
            int page = params[0];
            try {
                final String MOVIE_BASE_URL =
                        "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";
                final String PG_PARAM = "page";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendPath(sortBy)
                        .appendQueryParameter(PG_PARAM, page + "")
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

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                if (mImageAdapter.isLastItemNull()) {
                    mImageAdapter.remove();
                }
                mImageAdapter.addMovies(movies);
            }
        }

        private List<Movie> getMovieDataFromJson(String moviesJsonStr) throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String NODE_RESULTS = "results";
            final String NODE_POSTER_PATH = "poster_path";
            final String NODE_OVERVIEW = "overview";
            final String NODE_RELEASE_DATE = "release_date";
            final String NODE_ORIGINAL_TITLE = "original_title";
            final String NODE_VOTE_AVERAGE = "vote_average";
            final String NODE_ID = "id";
            final String NODE_BACKDROP = "backdrop_path";

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
                boolean isFavourite = isFavourite(id);
                String backdrop = movieObj.getString(NODE_BACKDROP);

                Movie movie = new Movie(
                        title,
                        posterPath,
                        overview,
                        rating,
                        releaseDate,
                        id,
                        isFavourite,
                        backdrop);

                resultList.add(movie);
            }

            return resultList;
        }

        private boolean isFavourite(String movieId) {
            String mSelectionClause = MovieEntry.COLUMN_MOVIE_ID + " = ?";
            String[] mSelectionArgs = {movieId};
            Cursor cursor = null;
            try {
                cursor = getContext().getContentResolver().query(
                        MovieEntry.CONTENT_URI, FAVOURITE_PROJECTION, mSelectionClause, mSelectionArgs, null);
                return cursor.moveToFirst();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    public class FetchFavouriteMovieTask extends AsyncTask<List<String>, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(List<String>... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            List<String> moviesJsonStrList = new ArrayList<>();

            List<String> ids = params[0];

            try {
                final String MOVIE_BASE_URL =
                        "https://api.themoviedb.org/3/movie/";
                final String APIKEY_PARAM = "api_key";

                for (String id : ids) {

                    Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                            .appendPath(id)
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
                    moviesJsonStrList.add(buffer.toString());
                }
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
                return getMovieDataListFromJson(moviesJsonStrList);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (!movies.isEmpty()) {
                mImageAdapter.clearMovies();
                mImageAdapter.addMovies(movies);
            }
        }

        private List<Movie> getMovieDataListFromJson(List<String> moviesJsonStrList) throws JSONException {
            List<Movie> movieList = new ArrayList<>();
            for (String movieJsonStr : moviesJsonStrList) {
                movieList.add(getMovieDataFromJson(movieJsonStr));
            }
            return movieList;
        }

        private Movie getMovieDataFromJson(String moviesJsonStr) throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String NODE_POSTER_PATH = "poster_path";
            final String NODE_OVERVIEW = "overview";
            final String NODE_RELEASE_DATE = "release_date";
            final String NODE_ORIGINAL_TITLE = "original_title";
            final String NODE_VOTE_AVERAGE = "vote_average";
            final String NODE_ID = "id";
            final String NODE_BACKDROP = "backdrop_path";

            JSONObject movieObj = new JSONObject(moviesJsonStr);
            String posterPath = movieObj.getString(NODE_POSTER_PATH);
            String overview = movieObj.getString(NODE_OVERVIEW);
            String releaseDate = movieObj.getString(NODE_RELEASE_DATE);
            String title = movieObj.getString(NODE_ORIGINAL_TITLE);
            double rating = movieObj.getDouble(NODE_VOTE_AVERAGE);
            String id = movieObj.getString(NODE_ID);
            boolean isFavourite = true;
            String backdrop = movieObj.getString(NODE_BACKDROP);

            return new Movie(title, posterPath, overview, rating, releaseDate, id, isFavourite, backdrop);
        }
    }
}
