package com.fenchtose.grabimagedemo.controllers.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class Constants {
    public static final String DATE_FORMAT_STRING = "dd-MMM-yy HH:mm";
    public static final String DIRECTORY_NAME = "GrabImage";
    public static final String ROOT_DIRECTORY = Environment.getExternalStorageDirectory()
            + File.separator + DIRECTORY_NAME;
    public static final String IMAGE_DIRECTORY_NAME = "Images";
    public static final String IMAGE_DIRECTORY = ROOT_DIRECTORY + File.separator
            + IMAGE_DIRECTORY_NAME;
    public static final String NO_MEDIA_FILE = IMAGE_DIRECTORY + File.separator + ".nomedia";

    public static final int REQUEST_IMAGE_CAPTURE = 4;
    public static final int REQUEST_UPLOAD_IMAGE = 5;

    public static final String IMAGE_FILE_PATH = "image_file_path";

    public static final String IMAGE_ID = "image_id";
    public static final int RESULT_NEW_IMAGE_ENTRY = 21;
}
