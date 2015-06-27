package com.fenchtose.grabimagedemo.views.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fenchtose.grabimagedemo.R;
import com.fenchtose.grabimagedemo.presenters.ImageUploadPresenter;
import com.fenchtose.grabimagedemo.presenters.ImageUploadPresenterImpl;
import com.fenchtose.grabimagedemo.views.ImageUploadView;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public class ImageUploadActivity extends AppCompatActivity implements ImageUploadView {

    private static final String TAG = "ImageUploadActivity";
    private Toolbar mToolbar;
    private ImageView imageContainer;
    private LinearLayout progressBarLayout;
    private LinearLayout bottomLayout;
    private TextView locationView;
    private TextView addressView;
    private Button updateButton;
    private Button cancelButton;

    private ImageUploadPresenter mPresenter;

    private int cancelButtonMargin;

    private ProgressDialog mProgressDialog;

    private int mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        mConfig = getResources().getConfiguration().orientation;
        if (mConfig == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_image_upload_portrait_layout);
        } else {
            setContentView(R.layout.activity_image_upload_landscape_layout);
        }

        mPresenter = new ImageUploadPresenterImpl(this);

        cancelButtonMargin = getResources().getDimensionPixelSize(R.dimen.cancel_button_left_margin);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        imageContainer = (ImageView)findViewById(R.id.image_container);
        bottomLayout = (LinearLayout)findViewById(R.id.bottom_layout);
        progressBarLayout = (LinearLayout)findViewById(R.id.progressbar_layout);
        locationView = (TextView)findViewById(R.id.location_textview);
        addressView = (TextView)findViewById(R.id.address_textview);
        updateButton = (Button)findViewById(R.id.button_update);
        cancelButton = (Button)findViewById(R.id.button_cancel);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.update();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.cancel();
            }
        });

        mPresenter.attachView();
        setupToolbar();
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.registerForEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume(getIntent().getExtras());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.saveInstance(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPresenter.restoreInstance(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.unregisterForEvents();
    }

    private void setupToolbar() {
        if (mToolbar != null) {
            mToolbar.setTitle("Upload Image");
            setSupportActionBar(mToolbar);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            mPresenter.cancel();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mPresenter.cancel();
    }

    @Override
    public ImageView getImageContainer() {
        return imageContainer;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showSnackBar(String message) {
        Snackbar.make(bottomLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setLocationText(CharSequence text) {
        locationView.setText(text);
    }

    @Override
    public void setAddressText(CharSequence text) {
        addressView.setText(text);
    }

    @Override
    public void showProgressLayout(boolean show) {
        if (show) {
            progressBarLayout.setVisibility(View.VISIBLE);
        } else {
            progressBarLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showLocationDetails(boolean show) {
        if (!show) {
            locationView.setVisibility(View.GONE);
            addressView.setVisibility(View.GONE);
        } else {
            locationView.setVisibility(View.VISIBLE);
            addressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showUpdateButton(boolean show) {
        if (show) {
            updateButton.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)cancelButton.getLayoutParams();
            if (mConfig == Configuration.ORIENTATION_PORTRAIT) {
                params.gravity = Gravity.CENTER_HORIZONTAL;
                params.leftMargin = 0;
            } else {
                params.gravity = Gravity.CENTER_VERTICAL;
                params.topMargin = 0;
            }
            cancelButton.setLayoutParams(params);
        } else {
            updateButton.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)cancelButton.getLayoutParams();
            params.gravity = Gravity.CENTER;
            if (mConfig == Configuration.ORIENTATION_PORTRAIT) {
                params.leftMargin = cancelButtonMargin;
            } else {
                params.topMargin = cancelButtonMargin;
            }
            cancelButton.setLayoutParams(params);
        }
    }

    @Override
    public void showCancelConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Discard")
                .setMessage("Are you sure you want to discard this image?")
                .setPositiveButton("NO", null)
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.discard();
                    }})
                .setCancelable(false);

        builder.show();

    }

    @Override
    public void showProcessingDialog() {
        mProgressDialog = ProgressDialog.show(this, "Uploading Image", "Please Wait...", true, false);
    }

    @Override
    public void dismissProcessingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void lockOrientation() {
        if (mConfig == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void sendResult(@Nullable Bundle extras, int resultCode) {
        Intent intent = new Intent();
        if (extras != null) {
            intent.putExtras(extras);
        }
        setResult(resultCode, intent);
        finish();
    }
}
