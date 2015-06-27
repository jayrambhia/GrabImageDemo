package com.fenchtose.grabimagedemo.models.providers;

import android.content.Context;

import com.fenchtose.grabimagedemo.controllers.providers.ImageModelProvider;
import com.fenchtose.grabimagedemo.models.databases.ImageModel;
import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class ImageFeeder {

    private ImageModelProvider mProvider;
    private List<ImageModelWrapper> mItems;

    private static final long ENTRY_LIMIT = 4;

    public ImageFeeder(Context context) {
        mProvider = new ImageModelProvider(context);
        mItems = new ArrayList<>();
    }

    public Observable<List<ImageModelWrapper>> feed(final boolean oldEntries) {
        return Observable.create(new Observable.OnSubscribe<List<ImageModelWrapper>>() {
            @Override
            public void call(Subscriber<? super List<ImageModelWrapper>> subscriber) {
                subscriber.onNext(feedList(oldEntries));
            }
        });
    }

    private List<ImageModelWrapper> feedList(boolean oldEntries) {
        if (mItems.isEmpty()) {
            List<ImageModel> models = mProvider.getRecentEntries(ENTRY_LIMIT);
            addModels(models, false);
        } else {
            if (oldEntries) {
                loadOld();
            } else {
                loadNew();
            }
        }

        return mItems;
    }

    private List<ImageModelWrapper> loadNew() {
        if (mItems.isEmpty()) {
            return feedList(false);
        }

        List<ImageModel> models = mProvider.getMoreEntries(
                mItems.get(0).getUpdatedAt(), ENTRY_LIMIT, false);
        addModels(models, false);
        return mItems;
    }

    private List<ImageModelWrapper> loadOld() {
        if (mItems.isEmpty()) {
            return feedList(true);
        }

        List<ImageModel> models = mProvider.getMoreEntries(
                mItems.get(mItems.size()-1).getUpdatedAt(), ENTRY_LIMIT, true);
        addModels(models, true);
        return mItems;
    }

    private synchronized boolean addModels(List<ImageModel> models, boolean olderEntries) {
        if (models == null) {
            return false;
        }
        if (olderEntries) {
            for (ImageModel model : models) {
                mItems.add(new ImageModelWrapper(model));
            }
        } else {
            List<ImageModelWrapper> wrappers = new ArrayList<>();
            for (ImageModel model : models) {
                wrappers.add(new ImageModelWrapper(model));
            }

            mItems.addAll(0, wrappers);
        }

        return true;
    }

    public void loadDummyData() {
        for (int i=0; i<5; i++) {
            ImageModel model = new ImageModel();
            model.id = new Date(new Date().getTime() + i * 120*1000).getTime();
            model.latitude = String.valueOf(50 + i);
            model.longitude = String.valueOf(72 + i);
            model.address = String.valueOf(91 + i) + ", YOLO";
            model.updated_ts = new Date(new Date().getTime() - i * 120*1000);

            mItems.add(new ImageModelWrapper(model));
        }
    }

}
