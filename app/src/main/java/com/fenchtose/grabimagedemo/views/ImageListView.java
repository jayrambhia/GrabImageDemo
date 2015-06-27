package com.fenchtose.grabimagedemo.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;

import java.util.List;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public interface ImageListView {

    Context getContext();
    void setData(List<ImageModelWrapper> items);
    void createActivityForResult(Intent intent, int requestCode);
    void showSnackBar(String message);
}
