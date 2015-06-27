package com.fenchtose.grabimagedemo.controllers.providers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.widget.ImageView;

import com.fenchtose.grabimagedemo.controllers.utils.Constants;
import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Jay Rambhia on 26/06/15.
 */
public class ImageProvider {

    public static File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String filename = "IMG_" + timestamp + ".jpg";

        File imageFile = new File(Constants.IMAGE_DIRECTORY, filename);
        imageFile.createNewFile();
        return imageFile;
    }

    public static class Builder {
        private Context context;
        private int maxHeight = -1;
        private int maxWidth = -1;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder withSize(int width, int height) {
            maxHeight = height;
            maxWidth = width;
            return this;
        }

        public ImageProvider build() {
            return new ImageProvider(this);
        }
    }

    private Context context;
    private int maxWidth;
    private int maxHeight;
    private MemCache memCache;

    private ImageProvider(Builder builder) {
        context = builder.context;
        maxWidth = builder.maxWidth;
        maxHeight = builder.maxHeight;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        memCache = new MemCache(cacheSize);
    }

    public void loadTo(ImageModelWrapper modelWrapper, ImageView view) {
        String localPath = modelWrapper.getLocalPath();
        if (localPath == null || localPath.isEmpty() || view == null) {
            return;
        }

        if (loadFromMemCache(localPath, view)) {
            return;
        }

        loadFromDisk(localPath, view);

    }

    public void release() {
        memCache.release();
    }

    private boolean loadFromMemCache(String path, ImageView view) {
        Bitmap bitmap = memCache.getBitmap(path);
        if (bitmap == null) {
            return false;
        }

        view.setImageBitmap(bitmap);
        return true;
    }

    private boolean loadFromDisk(String path, final ImageView view) {

        if (!(new File(path).exists())) {
            return false;
        }

        // Load bitmap

        Observable<Bitmap> observable = readBitmap(path);
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        if (bitmap != null) {
                            view.setImageBitmap(bitmap);
                        }
                    }
                });

        return true;
    }

    private Observable<Bitmap> readBitmap(final String path) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(Subscriber<? super Bitmap> subscriber) {
                subscriber.onNext(readBitmapFromFile(path));
            }
        });
    }

    private Bitmap readBitmapFromFile(String path) {
        if (maxHeight == -1 || maxWidth == -1) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if (bitmap != null) {
                memCache.put(path, bitmap);
            }

            return bitmap;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int MAX_DIMENS = Math.max(maxHeight, maxWidth);

        BitmapFactory.Options loadingOptions = getLoadingOptions(options, MAX_DIMENS);

        Bitmap bitmap = BitmapFactory.decodeFile(path, loadingOptions);
        if (bitmap == null) {
            return null;
        }

        // check and resize to fit exactly
        int maxSize = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (maxSize <= MAX_DIMENS) {
            // no need to resize anymore.
            memCache.put(path, bitmap);
            return bitmap;
        }

        // Need to resize
        float samplingFactor = (float)MAX_DIMENS/maxSize;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap,
                (int)(samplingFactor*bitmap.getWidth()),
                (int)(samplingFactor*bitmap.getHeight()),
                true);

        memCache.put(path, resizedBitmap);
        return resizedBitmap;
    }

    private class MemCache extends LruCache<String, Bitmap> {

        /**
         * @param maxSize for caches that do not override {@link #sizeOf}, this is
         *                the maximum number of entries in the cache. For all other caches,
         *                this is the maximum sum of the sizes of the entries in this cache.
         */
        public MemCache(int maxSize) {
            super(maxSize);
        }

        public Bitmap getBitmap(String path) {
            return this.get(path);
        }

        public void putBitmap(String path, Bitmap bitmap) {
            this.put(path, bitmap);
        }

        public void release() {
            this.evictAll();
        }
    }

    public static BitmapFactory.Options getLoadingOptions(BitmapFactory.Options options, int MAX_DIMENS) {
        return getLoadingOptions(options, MAX_DIMENS, false);
    }

    public static BitmapFactory.Options getLoadingOptions(BitmapFactory.Options options,
                                                          int MAX_DIMENS, boolean forecSampling) {
        int width = options.outWidth;
        int height = options.outHeight;

        float insampleSize = 1.0f;
        if (width > height) {
            insampleSize = (float)width/(float)MAX_DIMENS;
        } else {
            insampleSize = (float)height/(float)MAX_DIMENS;
        }

        if (insampleSize < 1.0) {
            insampleSize = 1.0f;
        }

        BitmapFactory.Options loadOptions = new BitmapFactory.Options();
        if (insampleSize > 2 && forecSampling) {
            int power = (int)Math.ceil(insampleSize);
            power--;
            power |= power >> 1;
            power |= power >> 2;
            power |= power >> 4;
            power |= power >> 8;
            power |= power >> 16;
            power++;

            loadOptions.inSampleSize = power;
        } else {
            loadOptions.inSampleSize = (int)Math.ceil(insampleSize);
        }

        return loadOptions;
    }

}
