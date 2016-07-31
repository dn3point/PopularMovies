package com.iamzhaoyuan.android.popularmovies.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by yuan on 29/7/16.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

    public MovieAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MovieUtil movieUtil = MovieUtil.getInstance();
        String imageThumbnail = getItem(position).getImageThumbnail();

        if (convertView == null) {
            convertView =
                    LayoutInflater.from(getContext()).inflate(
                            R.layout.grid_item_poster, parent, false);
        }

        ImageView posterView =
                (ImageView) convertView.findViewById(R.id.grid_item_poster_image);

        Picasso.with(getContext())
                .load(movieUtil.getPosterUrl(imageThumbnail)).into(posterView);

        return convertView;

    }
}
