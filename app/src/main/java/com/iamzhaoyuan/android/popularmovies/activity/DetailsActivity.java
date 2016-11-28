package com.iamzhaoyuan.android.popularmovies.activity;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;

import com.iamzhaoyuan.android.popularmovies.R;
import com.iamzhaoyuan.android.popularmovies.entity.Movie;
import com.iamzhaoyuan.android.popularmovies.fragment.DetailsFragment;
import com.iamzhaoyuan.android.popularmovies.util.MovieUtil;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.appbar) AppBarLayout mAppBarLayout;
    @BindView(R.id.backdrop) ImageView mImageView;
    @BindView(R.id.float_btn) FloatingActionButton mFloatingActionButton;

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        if (intent == null) {
            Log.d(LOG_TAG, "Intent from MainActivity is null?");
            return;
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams)mAppBarLayout.getLayoutParams();
        lp.height = size.y >> 1;

        mMovie = intent.getExtras().
                getParcelable(getString(R.string.intent_movie_obj_tag));

        Picasso.with(this)
                .load(MovieUtil.getInstance().getBackdropUrl(mMovie.getBackdrop()))
                .into(mImageView);

        mCollapsingToolbarLayout.setTitle(" ");

        if (mMovie.isFavourite()) {
            mFloatingActionButton.setImageDrawable(getDrawable(R.drawable.fav_white));
        } else {
            mFloatingActionButton.setImageDrawable(getDrawable(R.drawable.ol_white));
        }

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMovie.isFavourite()) {
                    ((FloatingActionButton) v).setImageDrawable(getDrawable(R.drawable.ol_white));
                    Snackbar.make(v, "Remove from favourite", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mMovie.setFavourite(false);
                } else {
                    ((FloatingActionButton) v).setImageDrawable(getDrawable(R.drawable.fav_white));
                    Snackbar.make(v, "Add to favourite", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mMovie.setFavourite(true);
                }
                // TODO update database
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailsFragment())
                    .commit();
        }
    }

}
