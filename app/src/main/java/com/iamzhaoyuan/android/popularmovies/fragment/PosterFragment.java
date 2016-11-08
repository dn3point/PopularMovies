package com.iamzhaoyuan.android.popularmovies.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.iamzhaoyuan.android.popularmovies.BuildConfig;
import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.activity.DetailsActivity;
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


/**
 * A simple {@link Fragment} subclass.
 */
public class PosterFragment extends Fragment {
    private static final String LOG_TAG = PosterFragment.class.getSimpleName();

    private static final String SORT_BY_KEY = "sort_by";

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

    public static PosterFragment newIntance(String sortBy) {
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

        mImageAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.poster_gridview);
        gridView.setAdapter(mImageAdapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(
                        getString(R.string.intent_movie_obj_tag),
                        mImageAdapter.getItem(position));
                startActivity(intent);
            }
        });

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
                mImageAdapter.clear();
                mImageAdapter.addAll(movies);
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
                String posterPath;
                String overview;
                String releaseDate;
                String title;
                double rating;
                String id;

                JSONObject movieObj = movieArray.getJSONObject(i);
                posterPath = movieObj.getString(NODE_POSTER_PATH);
                overview = movieObj.getString(NODE_OVERVIEW);
                releaseDate = movieObj.getString(NODE_RELEASE_DATE);
                title = movieObj.getString(NODE_ORIGINAL_TITLE);
                rating = movieObj.getDouble(NODE_VOTE_AVERAGE);
                id = movieObj.getString(NODE_ID);

                Movie movie = new Movie(title, posterPath, overview, rating, releaseDate, id);

                resultList.add(movie);
            }

            return resultList;
        }
    }

}
