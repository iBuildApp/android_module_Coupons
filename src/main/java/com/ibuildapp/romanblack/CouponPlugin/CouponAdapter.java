/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.CouponPlugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.appbuilder.sdk.android.Utils;
import org.apache.http.util.ByteArrayBuffer;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Adapter for coupons list.
 */
public class CouponAdapter extends BaseAdapter {

    private ArrayList<CouponItem> items;
    private LayoutInflater layoutInflater;
    private int imageWidth = 75;
    private int imageHeight = 75;
    private String cachePath = "";
    private HashMap<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();
    private ImageDownloadTask downloadTask = null;

    /**
     * Constructs new EventsAdapter instance
     * @param context - Activity that using this adapter
     * @param list - event items list
     * @param bgColor 
     */
    CouponAdapter(Context context, ArrayList<CouponItem> list, int bgColor) {
        items = list;
        layoutInflater = LayoutInflater.from(context);

        downloadTask = new ImageDownloadTask();
        downloadTask.execute(items);
    }

    /**
     * Set disk cache path to store downloaded rss images
     * @param cachePath - disk cache path
     */
    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cachePath + "/cache.data"));
            oos.writeObject(items);
            oos.flush();
            oos.close();
        } catch (Exception e) {
        }
    }

    /**
     * Set rss image size to show in list
     * @param width
     * @param height 
     */
    public void setImageSize(int width, int height) {
        imageWidth = width;
        imageHeight = height;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public CouponItem getItem(int index) {
        return items.get(index);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {//ErrorLogging 

            View row;

            if (null == convertView) {
            } else {
                row = convertView;
            }

            if (items.get(position).hasImage()) {
                row = layoutInflater.inflate(R.layout.romanblack_coupon_feed_item_image, null);
                ImageView imageView = (ImageView) row.findViewById(R.id.romanblack_coupon_image);
                imageView.setImageResource(R.drawable.romanblack_coupon_no_image);
            } else {
                row = layoutInflater.inflate(R.layout.romanblack_coupon_feed_item, null);
            }

            // change arrow color depending of background color
            ImageView arrow = (ImageView) row.findViewById(R.id.romanblack_coupon_feed_item_arrow);
            if (Utils.isChemeDark(Statics.color1)) {
                arrow.setImageResource(R.drawable.romanblack_coupon_arrow_light);
            } else {
                arrow.setImageResource(R.drawable.romanblack_coupon_arrow);
            }

            TextView title = (TextView) row.findViewById(R.id.romanblack_coupon_title);
            title.setTextColor(Statics.color3);

            TextView description = (TextView) row.findViewById(R.id.romanblack_coupon_description);
            description.setTextColor(Statics.color4);

            title.setText(items.get(position).getTitle());
            description.setText(items.get(position).getAnounce(75));

            if (items.get(position).hasImage()) {
                ImageView imageView = (ImageView) row.findViewById(R.id.romanblack_coupon_image);
                if (imageView != null) {
                    if (items.get(position).getImagePath().length() > 0) {
                        Bitmap bitmap = null;
                        Integer key = new Integer(position);
                        if (bitmaps.containsValue(key)) {
                            bitmap = bitmaps.get(key);
                        } else {
                            try {
                                bitmap = decodeImageFile(items.get(position).getImagePath());
                                bitmaps.put(key, bitmap);
                            } catch (Exception e) {
                                Log.w("NEWS ADAPTER", e);
                            }
                        }

                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                }
            }
            return row;

        } catch (Exception e) {
            return null;
        }
    }

    private void viewUpdated() {
        this.notifyDataSetChanged();
    }

    private void downloadRegistration(int position, String value) {
        this.items.get(position).setImagePath(value);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cachePath + "/cache.data"));
            oos.writeObject(items);
            oos.flush();
            oos.close();
            Log.d("IMAGES PLUGIN CACHE DATA", "SUCCESS");
        } catch (Exception e) {
            Log.w("IMAGES PLUGIN CACHE DATA", e);
        }
    }

    /**
     * Refresh rss list if image was downloaded
     */
    private void downloadComplete() {
        this.notifyDataSetChanged();
    }

    /**
     * Decode rss item image thad storing in given file
     * @param imagePath - image file path
     * @return decoded image Bitmap
     */
    private Bitmap decodeImageFile(String imagePath) {
        try {
            File file = new File(imagePath);
            //Decode image size
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            //Find the correct scale value. It should be the power of 2.
            int width = opts.outWidth, height = opts.outHeight;
            int scale = 1;
            while (true) {
                if (width / 2 < imageWidth || height / 2 < imageHeight) {
                    break;
                }
                width /= 2;
                height /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;

            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, opts);

            int x = 0, y = 0, l = 0;
            if (width > height) {
                x = (int) (width - height) / 2;
                y = 0;
                l = height;
            } else {
                x = 0;
                y = (int) (height - width) / 2;
                l = width;
            }

            float matrixScale = (float) (imageWidth - 4) / (float) l;
            Matrix matrix = new Matrix();
            matrix.postScale(matrixScale, matrixScale);

            return Bitmap.createBitmap(bitmap, x, y, l, l, matrix, true);
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * This class creates a background thread to download rss item images.
     */
    private class ImageDownloadTask extends AsyncTask<ArrayList<CouponItem>, String, Void> {

        @Override
        protected Void doInBackground(ArrayList<CouponItem>... items) {
            try {//ErrorLogging

                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;

                for (int i = 0; i < items[0].size(); i++) {
                    if (isCancelled()) {
                        return null;
                    }

                    if (items[0].get(i).getImageUrl().length() == 0) {
                        continue;
                    }

                    if (items[0].get(i).getImagePath().length() > 0) {
                        File file = new File(items[0].get(i).getImagePath());
                        if (file.exists()) {
                            continue;
                        }
                    }

                    SystemClock.sleep(10);

                    try {
                        URL imageUrl = new URL(items[0].get(i).getImageUrl());
                        BufferedInputStream bis = new BufferedInputStream(imageUrl.openConnection().getInputStream());
                        ByteArrayBuffer baf = new ByteArrayBuffer(32);
                        int current = 0;
                        while ((current = bis.read()) != -1) {
                            baf.append((byte) current);
                        }
                        String filename = cachePath + "/" + System.currentTimeMillis();
                        FileOutputStream fos = new FileOutputStream(new File(filename));
                        fos.write(baf.toByteArray());
                        fos.close();

                        downloadRegistration(i, filename);
                    } catch (Exception e) {
                        Log.e("IMAGE ADAPTER", "An error has occurred downloading the image: " + items[0].get(i).getImageUrl() + " " + e);
                    }

                    publishProgress();
                }

                return null;

            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... param) {
            viewUpdated();
        }

        @Override
        protected void onPostExecute(Void unused) {
            downloadComplete();
        }
    }
}