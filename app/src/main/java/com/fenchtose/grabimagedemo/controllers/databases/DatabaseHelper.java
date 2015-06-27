package com.fenchtose.grabimagedemo.controllers.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.fenchtose.grabimagedemo.models.databases.ImageModel;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "image_grab_v1.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";

    private Dao<ImageModel, Long> imageDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, ImageModel.class);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create table", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        // nothing to update yet
    }

    @Override
    public void close() {
        super.close();
        imageDao = null;
    }

    public Dao<ImageModel, Long> getImageModelDao() throws SQLException {
        if (imageDao == null) {
            imageDao = getDao(ImageModel.class);
        }

        return imageDao;
    }
}
