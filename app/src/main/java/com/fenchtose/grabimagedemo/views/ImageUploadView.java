package com.fenchtose.grabimagedemo.views;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public interface ImageUploadView {

    ImageView getImageContainer();
    Context getContext();
    void showSnackBar(String message);

    void setLocationText(CharSequence text);
    void setAddressText(CharSequence text);
    void showProgressLayout(boolean show);
    void showLocationDetails(boolean show);
    void showUpdateButton(boolean show);

    void showProcessingDialog();
    void dismissProcessingDialog();

    void lockOrientation();
    void unlockOrientation();

    void showCancelConfirmationDialog();
    void sendResult(Bundle extras, int resultCode);
}
