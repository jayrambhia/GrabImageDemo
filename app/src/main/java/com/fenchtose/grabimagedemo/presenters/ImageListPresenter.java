package com.fenchtose.grabimagedemo.presenters;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public interface ImageListPresenter {

    void attachView(Bundle savedInstanceState);
    void restoreInstance(Bundle savedInstanceState);
    void saveInstance(Bundle unsavedInstanceState);
    void detachView();

    void feedOldEntries();

    void onCameraImageRequested();
    void onCameraImageCaptured();
    void onImageUploaded(int resultCode, Intent data);

}
