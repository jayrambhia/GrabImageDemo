package com.fenchtose.grabimagedemo.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fenchtose.grabimagedemo.R;
import com.fenchtose.grabimagedemo.controllers.providers.ImageProvider;
import com.fenchtose.grabimagedemo.controllers.utils.Constants;
import com.fenchtose.grabimagedemo.models.wrappers.ImageModelWrapper;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private static final String TAG = "ImageAdapter";
    private List<ImageModelWrapper> items;
    private Format dateFormatter;
    private ImageProvider imageProvider;

    public ImageAdapter(Context context) {
        dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT_STRING);
        items = new ArrayList<>();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Log.i(TAG, "width: " + width + " height: " + height);
        imageProvider = new ImageProvider.Builder(context).withSize((int)(0.5*width), (int)(0.5*height)).build();
    }

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.ViewHolder holder, int position) {
        ImageModelWrapper mItem = items.get(position);
        holder.mAddressView.setText(mItem.getAddressDisplayText());
        holder.mLocationView.setText(mItem.getLocationDisplayText());
        if (mItem.getUpdatedAt() != null) {
            holder.mTimeView.setText("Updated At: " + dateFormatter.format(mItem.getUpdatedAt()));
        } else {
            holder.mTimeView.setText("Updated At: --");
        }

        imageProvider.loadTo(mItem, holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public void onViewRecycled(ImageAdapter.ViewHolder holder) {
        Log.i(TAG, "onViewRecycled: " + holder.getAdapterPosition());
        holder.mImageView.setImageBitmap(null);
    }

    public void setData(List<ImageModelWrapper> data) {
        Log.i(TAG, "data size: " + data.size());
        items.clear();
        items.addAll(data);
    }

    public void release() {
        if (imageProvider != null) {
            imageProvider.release();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView;
        public TextView mLocationView;
        public TextView mAddressView;
        public TextView mTimeView;

        public ViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.item_imageview);
            mLocationView = (TextView)itemView.findViewById(R.id.location_textview);
            mAddressView = (TextView)itemView.findViewById(R.id.address_textview);
            mTimeView = (TextView)itemView.findViewById(R.id.time_textview);
        }
    }
}
