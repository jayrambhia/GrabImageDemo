package com.fenchtose.grabimagedemo.views.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fenchtose.grabimagedemo.R;
import com.fenchtose.grabimagedemo.controllers.Events.LocationRequestEvent;
import com.fenchtose.grabimagedemo.controllers.services.LocationProviderService;
import com.fenchtose.grabimagedemo.controllers.utils.Constants;
import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;
import com.fenchtose.grabimagedemo.presenters.ImageListPresenter;
import com.fenchtose.grabimagedemo.presenters.ImageListPresenterImpl;
import com.fenchtose.grabimagedemo.views.ImageListView;
import com.fenchtose.grabimagedemo.views.adapters.ImageAdapter;
import com.fenchtose.grabimagedemo.views.widgets.RecyclerViewScrollListener;

import java.util.List;

import de.greenrobot.event.EventBus;


public class ImageListActivity extends AppCompatActivity implements ImageListView,
        AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "ImageListActivity";

    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private FloatingActionButton mFAB;
    private RecyclerView mRecyclerView;

    private ImageAdapter mAdapter;

    private ImageListPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new ImageListPresenterImpl(this);

        setContentView(R.layout.activity_image_list);

        mCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.rootview);
        AppBarLayout appBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(this);

        mToolbar = (Toolbar)findViewById(R.id.toolbar);

        mFAB = (FloatingActionButton)findViewById(R.id.add_fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.onCameraImageRequested();
            }
        });

        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new ImageAdapter(getApplicationContext());
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new ScrollListener());

        mPresenter.attachView(savedInstanceState);

        initializeToolbar();

        startService(new Intent(this, LocationProviderService.class));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        mPresenter.saveInstance(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPresenter.restoreInstance(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mAdapter != null) {
            mAdapter.release();
        }

        mPresenter.detachView();
        EventBus.getDefault().post(new LocationRequestEvent(LocationRequestEvent.REQUEST_STOP_SELF));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_new) {
            mPresenter.onCameraImageRequested();
        }

        return true;
    }

    private void initializeToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.app_name);
            Log.i(TAG, "set title");
            setSupportActionBar(mToolbar);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void setData(List<ImageModelWrapper> items) {
        mAdapter.setData(items);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void createActivityForResult(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG, "onActivityResult");

        if (requestCode == Constants.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mPresenter != null) {
                mPresenter.onCameraImageCaptured();
            }
        }

        if (requestCode == Constants.REQUEST_UPLOAD_IMAGE) {
            if (mPresenter != null) {
                mPresenter.onImageUploaded(resultCode, data);
            }
        }
    }

    @Override
    public void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mAdapter != null && mAdapter.getItemCount() == 0) {
            appBarLayout.setTranslationY(0);
        }
    }

    private class ScrollListener extends RecyclerViewScrollListener {

        @Override
        public void onScrollUp() {

        }

        @Override
        public void onScrollDown() {

        }

        @Override
        public void onLoadMore() {
            mPresenter.feedOldEntries();
        }
    }
}
