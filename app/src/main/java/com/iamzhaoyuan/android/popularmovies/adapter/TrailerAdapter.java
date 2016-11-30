package com.iamzhaoyuan.android.popularmovies.adapter;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mTrailerList;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trailer, parent, false);

        return new TrailerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String trailerKey = mTrailerList.get(position);
        String thumbnail = MovieUtil.getInstance().getTrailerThumbnail(trailerKey);
        Picasso.with(mContext).load(thumbnail).into(holder.thumbnail);

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO open trailer
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
        @BindView(R.id.thumbnail) ImageView thumbnail;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            thumbnail.getLayoutParams().width = size.x >> 1;
            thumbnail.getLayoutParams().height = size.x / 3;
        }

    }

    public TrailerAdapter(Context context, List<String> trailerList) {
        mContext = context;
        mTrailerList = trailerList;
    }


}
