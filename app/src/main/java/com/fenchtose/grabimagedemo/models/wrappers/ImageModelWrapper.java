package com.fenchtose.grabimagedemo.models.wrappers;

import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;

import com.fenchtose.grabimagedemo.controllers.utils.Constants;
import com.fenchtose.grabimagedemo.models.databases.ImageModel;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class ImageModelWrapper {

    private long id;
    private String address;
    private String latitude;
    private String longitude;
    private Date createdAt;
    private Date updatedAt;
    private String localPath;
    private String remotePath;
    private int size;

    private ImageModel model;

    public ImageModelWrapper(@NonNull ImageModel model) {
        setModel(model);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public ImageModel getModel() {
        ImageModel model = new ImageModel();
        model.id = this.id;
        model.address = this.address;
        model.latitude = this.latitude;
        model.longitude = this.longitude;
        model.created_ts = createdAt;
        model.updated_ts = updatedAt;
        model.mmpath_local = localPath;
        model.mmpath_remote = remotePath;
        model.mmsize = size;
        return model;
    }

    public void setModel(@NonNull ImageModel model) {
        this.model = model;
        this.id = model.id;
        this.address = model.address;
        this.longitude = model.longitude;
        this.latitude = model.latitude;
        this.createdAt = model.created_ts;
        this.updatedAt = model.updated_ts;
        this.localPath = model.mmpath_local;
        this.remotePath = model.mmpath_remote;
        this.size = model.mmsize;
    }

    public CharSequence getLocationDisplayText() {
        return "Lat: " + latitude + " Long: " + longitude;
    }

    public CharSequence getAddressDisplayText() {
        return address != null ? address : "";
    }

    public void setAddress(@NonNull Address locationAddress) {
        String newLocation = "";
        for (int i=0; i<locationAddress.getMaxAddressLineIndex(); i++) {
            newLocation += locationAddress.getAddressLine(i) + ", ";
            if (i == 1) {
                break;
            }
        }

        newLocation += locationAddress.getLocality() +", ";
        newLocation += locationAddress.getPostalCode();

        this.address = newLocation;
    }

    public void setLocation(@NonNull Location location) {
        latitude = String.format("%.2f", location.getLatitude());
        longitude = String.format("%.2f", location.getLongitude());

    }
}
