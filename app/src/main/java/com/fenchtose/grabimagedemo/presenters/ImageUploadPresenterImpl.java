package com.fenchtose.grabimagedemo.presenters;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.fenchtose.grabimagedemo.controllers.Events.LocationRequestEvent;
import com.fenchtose.grabimagedemo.controllers.Events.LocationResponseEvent;
import com.fenchtose.grabimagedemo.controllers.providers.ImageModelProvider;
import com.fenchtose.grabimagedemo.controllers.providers.ImageProvider;
import com.fenchtose.grabimagedemo.controllers.utils.Constants;
import com.fenchtose.grabimagedemo.models.databases.ImageModel;
import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;
import com.fenchtose.grabimagedemo.views.ImageUploadView;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public class ImageUploadPresenterImpl implements ImageUploadPresenter {

    private static final String TAG = "ImageUploaderPresenter";
    private ImageUploadView mImageUploadView;
    private ImageProvider mImageProvider;

    private ImageModelWrapper imageModelWrapper;
    private String mFilePath;

    private Handler mHandler;
    private EventBus mEventBus;

    private boolean useNewLocation = true;
    private Location currentLocation;
    private Address currentAddress;
    private long ALLOWED_TIME_DIFF = 2 * 60 * 1000; // 2 minutes
    private float MIN_ACCURACY = 50;

    private static final String SAVED_MODEL = "saved_model";
    private static final String SAVED_LOCATION = "saved_location";

    public ImageUploadPresenterImpl(ImageUploadView view) {
        mImageUploadView = view;
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void attachView() {

        mHandler = new Handler();

        DisplayMetrics metrics = mImageUploadView.getContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        mImageProvider = new ImageProvider.Builder(mImageUploadView.getContext())
                .withSize((int)(0.6*width), (int)(0.6*height))
                .build();

        mImageUploadView.showProgressLayout(true);
        mImageUploadView.showUpdateButton(false);
        mImageUploadView.showLocationDetails(false);
    }

    @Override
    public void resume(Bundle extras) {

        if (imageModelWrapper != null) {
            if (currentLocation == null) {
                getLocation();
            }
            return;
        }

        if (extras == null) {
            mImageUploadView.showSnackBar("Something went wrong!");
            return;
        }

        mFilePath = extras.getString(Constants.IMAGE_FILE_PATH, null);
        if (mFilePath == null || mFilePath.isEmpty()) {
            mImageUploadView.showSnackBar("Something went wrong!");
            return;
        }

        ImageModel model = new ImageModel();
        model.id = new Date().getTime();
        model.mmpath_local = mFilePath;
        model.created_ts = new Date();

        initializeModelWrapper(model);
        getLocation();
    }

    @Override
    public void saveInstance(Bundle outState) {
        if (imageModelWrapper != null) {
            outState.putParcelable(SAVED_MODEL, imageModelWrapper.getModel());
        }

        if (currentLocation != null) {
            outState.putParcelable(SAVED_LOCATION, currentLocation);
        }
    }

    @Override
    public void restoreInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ImageModel model = savedInstanceState.getParcelable(SAVED_MODEL);
            if (model != null) {
                initializeModelWrapper(model);
            }

            Location savedLocation = savedInstanceState.getParcelable(SAVED_LOCATION);
            if (savedLocation != null) {
                parseLocation(savedLocation);
            }
        }
    }

    @Override
    public void registerForEvents() {
        mEventBus.register(this);
    }

    @Override
    public void unregisterForEvents() {
        mEventBus.unregister(this);
    }

    @Override
    public void detachView() {
        mImageProvider.release();
        mImageProvider = null;
    }

    @Override
    public void cancel() {
        mImageUploadView.showCancelConfirmationDialog();
    }

    @Override
    public void discard() {
        mImageUploadView.sendResult(null, Activity.RESULT_CANCELED);
    }

    @Override
    public void update() {
        mImageUploadView.lockOrientation();
        mImageUploadView.showProcessingDialog();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                int randNum = random.nextInt(4);
                Log.i(TAG, "random: " + randNum);

                if (randNum != 0) {
                    onUpdateCompleted();
                } else {
                    onUpdateFailed();
                }

                mImageUploadView.unlockOrientation();
            }
        }, 5000);
    }

    private void initializeModelWrapper(ImageModel model) {
        imageModelWrapper = new ImageModelWrapper(model);
        mImageProvider.loadTo(imageModelWrapper, mImageUploadView.getImageContainer());
    }

    private void onUpdateFailed() {
        mImageUploadView.dismissProcessingDialog();
        mImageUploadView.showSnackBar("Unable to update. Please try again");
    }

    private void onUpdateCompleted() {
        mImageUploadView.dismissProcessingDialog();

        imageModelWrapper.setUpdatedAt(new Date());

        ImageModelProvider provider = new ImageModelProvider(mImageUploadView.getContext());
        provider.create(imageModelWrapper.getModel());

        Bundle extras = new Bundle();
        extras.putLong(Constants.IMAGE_ID, imageModelWrapper.getId());
        mImageUploadView.sendResult(extras, Constants.RESULT_NEW_IMAGE_ENTRY);
    }

    private void getLocation() {
        mEventBus.post(new LocationRequestEvent(LocationRequestEvent.REQUEST_LOCATION));
    }

    private void updateViewWithLocation() {
        mImageUploadView.setAddressText(imageModelWrapper.getAddressDisplayText());
        mImageUploadView.setLocationText(imageModelWrapper.getLocationDisplayText());
        mImageUploadView.showLocationDetails(true);
        mImageUploadView.showProgressLayout(false);
        mImageUploadView.showUpdateButton(true);
    }

    private void parseLocation(Location mLocation) {
        if (!useNewLocation) {
            return;
        }

        long diff = new Date().getTime() - mLocation.getTime();
        Log.i(TAG, "diff: " + diff);
        if (diff > ALLOWED_TIME_DIFF) {
            Log.e(TAG, "stale location. discard.");
            return;
        }

        if (mLocation.getAccuracy() > MIN_ACCURACY) {
            Log.e(TAG, "location is not accurate enough. discard.");
            return;
        }

        Log.i(TAG, "set location: " + mLocation);

        currentAddress = parseAddress(mLocation);
        if (currentAddress != null) {
            imageModelWrapper.setAddress(currentAddress);
        }

        imageModelWrapper.setLocation(mLocation);
        currentLocation = mLocation;
        useNewLocation = false;
        updateViewWithLocation();
    }

    private Address parseAddress(Location location) {
        Geocoder geocoder = new Geocoder(mImageUploadView.getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void onEventMainThread(LocationResponseEvent event) {
        Log.i(TAG, "location response: " + event.getType() + " " + event.getLocation().toString());
        Location mLocation = event.getLocation();
        if (mLocation != null) {
            parseLocation(mLocation);
        }

    }
}
