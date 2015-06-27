package com.fenchtose.grabimagedemo.models.databases;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by Jay Rambhia on 25/06/15.
 */

@DatabaseTable(tableName = "image_model")
public class ImageModel implements Parcelable {

    public static final String UPDATED_TIMESTAMP_COLUMN = "updated_ts";

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    public long id;

    @DatabaseField
    public String address;

    @DatabaseField
    public String latitude;

    @DatabaseField
    public String longitude;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date created_ts;

    @DatabaseField(dataType = DataType.DATE_LONG, columnName = UPDATED_TIMESTAMP_COLUMN)
    public Date updated_ts;

    @DatabaseField
    public String mmpath_local;

    @DatabaseField
    public String mmpath_remote;

    @DatabaseField
    public int mmsize;

    // Empty constructor for OrmLite.
    public ImageModel() {

    }

    private ImageModel(Parcel source) {
        id = source.readLong();
        address = source.readString();
        latitude = source.readString();
        longitude = source.readString();
        long created_ts_long = source.readLong();
        if (created_ts_long > 0) {
            created_ts = new Date(created_ts_long);
        }

        long updated_ts_long = source.readLong();
        if (updated_ts_long > 0) {
            updated_ts = new Date(updated_ts_long);
        }

        mmpath_local = source.readString();
        mmpath_remote = source.readString();
        mmsize = source.readInt();
    }

    public static final Creator<ImageModel> CREATOR = new Parcelable.Creator<ImageModel>() {

        @Override
        public ImageModel createFromParcel(Parcel source) {
            return new ImageModel(source);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);

        if (address == null) {
            address = "";
        }
        dest.writeString(address);

        if (latitude == null) {
            latitude = "";
        }
        dest.writeString(latitude);

        if (longitude == null) {
            longitude = "";
        }
        dest.writeString(longitude);

        if (created_ts != null) {
            dest.writeLong(created_ts.getTime());
        } else {
            dest.writeLong(-1);
        }

        if (updated_ts != null) {
            dest.writeLong(updated_ts.getTime());
        } else {
            dest.writeLong(-1);
        }

        if (mmpath_local == null) {
            mmpath_local = "";
        }
        dest.writeString(mmpath_local);

        if (mmpath_remote == null) {
            mmpath_remote = "";
        }
        dest.writeString(mmpath_remote);

        dest.writeInt(mmsize);

    }
}
