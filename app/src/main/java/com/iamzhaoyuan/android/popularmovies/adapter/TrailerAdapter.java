package com.iamzhaoyuan.android.popularmovies.adapter;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mTrailerList;

    public TrailerAdapter(Context context, List<String> trailerList) {
        mContext = context;
        mTrailerList = trailerList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer, parent, false);

        return new TrailerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final String trailerKey = mTrailerList.get(position);
        String thumbnail = MovieUtil.getInstance().getTrailerThumbnail(trailerKey);
        Picasso.with(mContext).load(thumbnail).into(holder.thumbnail);

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKey));
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerKey));
                try {
                    mContext.startActivity(appIntent);
                } catch (ActivityNotFoundException ex) {
                    mContext.startActivity(webIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTrailerList.size();
    }

    public void clearTrailers() {
        int size = mTrailerList.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mTrailerList.remove(0);
            }
            notifyItemRangeRemoved(0, size);
        }
    }

    public void addTrailers(List<String> trailers) {
        mTrailerList.addAll(trailers);
        notifyItemRangeInserted(0, trailers.size() - 1);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.thumbnail)
        ImageView thumbnail;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            thumbnail.getLayoutParams().width = itemView.getLayoutParams().width << 1 / 3;
            thumbnail.getLayoutParams().height = thumbnail.getLayoutParams().width << 1 / 3;
        }

    }
}
