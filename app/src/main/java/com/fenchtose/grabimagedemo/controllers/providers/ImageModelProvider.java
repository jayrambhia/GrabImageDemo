package com.fenchtose.grabimagedemo.controllers.providers;

import android.content.Context;
import android.util.Log;

import com.fenchtose.grabimagedemo.controllers.databases.DatabaseHelper;
import com.fenchtose.grabimagedemo.controllers.databases.DatabaseManager;
import com.fenchtose.grabimagedemo.models.databases.ImageModel;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class ImageModelProvider {

    private static final String TAG = "ImageModelProvider";
    private DatabaseManager dbManager;
    private DatabaseHelper dbHelper;
    private Dao<ImageModel, Long> imageDao;

    public ImageModelProvider(Context context) {
        dbManager = new DatabaseManager();
        dbHelper = dbManager.getHelper(context);

        try {
            imageDao = dbHelper.getImageModelDao();
        } catch (SQLException e) {
            Log.e(TAG, "unable to get Dao", e);
            throw new RuntimeException(e);
        }
    }

    public void release() {
        dbManager.releaseHelper();
    }

    public int create(ImageModel model) {
        try {
            return imageDao.create(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int update(ImageModel model) {
        try {
            return imageDao.update(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int delete(ImageModel model) {
        try {
            return imageDao.delete(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public ImageModel getById(long id) {
        try {
            return imageDao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<ImageModel> getRecentEntries(long limit) {
        QueryBuilder<ImageModel, Long> qb = imageDao.queryBuilder();
        try {
            qb.orderBy(ImageModel.UPDATED_TIMESTAMP_COLUMN, false);
            qb.limit(limit);
            PreparedQuery<ImageModel> preparedQuery = qb.prepare();
            return imageDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<ImageModel> getMoreEntries(Date currentTS, long limit, boolean loadOlder) {
        QueryBuilder<ImageModel, Long> qb = imageDao.queryBuilder();
        try {

            qb.limit(limit);
            Where where = qb.where();

            if (loadOlder) {
                where.lt(ImageModel.UPDATED_TIMESTAMP_COLUMN, currentTS);
                qb.orderBy(ImageModel.UPDATED_TIMESTAMP_COLUMN, false);
            } else {
                where.gt(ImageModel.UPDATED_TIMESTAMP_COLUMN, currentTS);
                qb.orderBy(ImageModel.UPDATED_TIMESTAMP_COLUMN, true);
            }
            PreparedQuery<ImageModel> preparedQuery = qb.prepare();
            return imageDao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
