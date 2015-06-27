package com.fenchtose.grabimagedemo.presenters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import com.fenchtose.grabimagedemo.controllers.providers.ImageProvider;
import com.fenchtose.grabimagedemo.controllers.utils.Constants;
import com.fenchtose.grabimagedemo.models.providers.ImageFeeder;
import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;
import com.fenchtose.grabimagedemo.views.ImageListView;
import com.fenchtose.grabimagedemo.views.activities.ImageUploadActivity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class ImageListPresenterImpl implements ImageListPresenter {

    private ImageListView mImageListView;
    private ImageFeeder mImageFeeder;

    private File mImageFile;

    private Handler mHandler;

    private static final String SAVED_FILE_PATH = "saved_file_path";

    public ImageListPresenterImpl(ImageListView view) {
        mImageListView = view;
    }

    @Override
    public void attachView(Bundle savedInstanceState) {

        mHandler = new Handler();
        setupDirectory();

        mImageFeeder = new ImageFeeder(mImageListView.getContext());
        getData(true);
    }

    @Override
    public void restoreInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            String filePath = savedInstanceState.getString(SAVED_FILE_PATH, null);
            if (filePath != null) {
                mImageFile = new File(filePath);
            }
        }
    }

    @Override
    public void saveInstance(Bundle unsavedInstanceState) {
        if (mImageFile != null) {
            unsavedInstanceState.putString(SAVED_FILE_PATH, mImageFile.toString());
        }
    }

    @Override
    public void detachView() {

    }

    @Override
    public void onCameraImageRequested() {
        /*Intent uploadIntent = new Intent(mImageListView.getContext(), ImageUploadActivity.class);
        mImageListView.createActivityForResult(uploadIntent, Constants.REQUEST_UPLOAD_IMAGE);*/

        try {
            mImageFile = ImageProvider.createImageFile();
            Bundle extras = new Bundle();
            extras.putParcelable(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImageFile));
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtras(extras);
            mImageListView.createActivityForResult(cameraIntent, Constants.REQUEST_IMAGE_CAPTURE);

        } catch (IOException e) {
            e.printStackTrace();
            mImageListView.showSnackBar("Unable to perform your request. Please try again");
        }

    }

    @Override
    public void onCameraImageCaptured() {
        if (mImageFile == null) {
            mImageListView.showSnackBar("Unable to save the image. Please try again");
            return;
        }

        Bundle extras = new Bundle();
        extras.putString(Constants.IMAGE_FILE_PATH, mImageFile.toString());

        Intent uploadIntent = new Intent(mImageListView.getContext(), ImageUploadActivity.class);
        uploadIntent.putExtras(extras);
        mImageListView.createActivityForResult(uploadIntent, Constants.REQUEST_UPLOAD_IMAGE);
    }

    @Override
    public void onImageUploaded(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            mImageFile.delete();
            mImageFile = null;
        } else if (resultCode == Constants.RESULT_NEW_IMAGE_ENTRY) {
            mImageFile = null;
            loadMoreData();
        }
    }

    private void loadMoreData() {
        getData(false);
    }

    @Override
    public void feedOldEntries() {
        getData(true);
    }

    private void getData(final boolean loadOldEntries) {
        Observable<List<ImageModelWrapper>> observable = mImageFeeder.feed(loadOldEntries);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ImageModelWrapper>>() {
                    @Override
                    public void call(List<ImageModelWrapper> imageModelWrappers) {
                        mImageListView.setData(imageModelWrappers);
                    }
                });
    }

    private void setupDirectory() {
        File rootDirectory = new File(Constants.ROOT_DIRECTORY);
        boolean success = false;

        success = rootDirectory.isDirectory() || rootDirectory.mkdir();

        if (success) {
            File imageDirectory = new File(Constants.IMAGE_DIRECTORY);
            success = imageDirectory.isDirectory() || imageDirectory.mkdir();
        }

        if (success) {
            File noMediaFile = new File(Constants.NO_MEDIA_FILE);
            if (!noMediaFile.exists()) {
                try {
                    noMediaFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
