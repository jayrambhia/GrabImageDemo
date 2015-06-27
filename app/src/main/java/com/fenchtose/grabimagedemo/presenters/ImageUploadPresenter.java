package com.fenchtose.grabimagedemo.presenters;

import android.os.Bundle;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public interface ImageUploadPresenter {
    void attachView();
    void resume(Bundle extras);
    void saveInstance(Bundle outState);
    void restoreInstance(Bundle savedInstanceState);
    void registerForEvents();
    void unregisterForEvents();
    void detachView();

    void cancel();
    void discard();
    void update();
}
