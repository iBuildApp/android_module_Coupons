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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.StartUpActivity;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;

import java.io.*;
import java.util.ArrayList;

/**
 * Main module class. Module entry point.
 * Represents Coupons widget.
 */
@StartUpActivity(moduleName = "Coupons")
public class CouponPlugin extends AppBuilderModuleMain {

    private String feedUrl = "";
    private String cachePath = "";
    private ArrayList<CouponItem> items = new ArrayList<CouponItem>();
    private ProgressDialog progressDialog = null;
    private Widget widget;
    private boolean isOnline = false;
    private boolean useCache = false;
    private boolean isRss = false;
    private PullToRefreshListView listView = null;
    private RelativeLayout mainLayout = null;
    Intent currentIntent;
    private String title = "";
    private String cacheMD5 = "";
    private String widgetMD5 = "";
    final private int SHOW_COUPONS = 1;
    final private int INITIALIZATION_FAILED = 3;
    final private int NEED_INTERNET_CONNECTION = 4;
    final private int LOADING_ABORTED = 5;
    final private int CLEAR_ITEM_VIEW = 6;
    final private int COLORS_RECIEVED = 7;
    private ConnectivityManager cm = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case INITIALIZATION_FAILED: {
                    Toast.makeText(CouponPlugin.this, getString(R.string.plugin_initializatin_failed), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 5000);
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(CouponPlugin.this, getString(R.string.alert_no_internet), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 5000);
                }
                break;
                case SHOW_COUPONS: {
                    CouponPlugin.this.showCoupons();
                }
                break;
                case LOADING_ABORTED: {
                    CouponPlugin.this.closeActivity();
                }
                break;
                case CLEAR_ITEM_VIEW: {
                    clearItemView();
                }
                break;
                case COLORS_RECIEVED: {
                    colorsRecieved();
                }
                break;
            }

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    };
    private String xmlData;

    @Override
    public void create() {
        try {//Errorlogging

            setContentView(R.layout.romanblack_coupon_feed_main);
            setTitle("Feed");

            currentIntent = getIntent();
            Bundle store = currentIntent.getExtras();
            widget = (Widget) store.getSerializable("Widget");
            if (widget == null) {
                handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 100);
                return;
            }

            try {
                if (widget.getPluginXmlData().length() == 0) {
                    if (widget.getPathToXmlFile().length() == 0) {
                        handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
                        return;
                    }
                }
            } catch (Exception e) {
                handler.sendEmptyMessageDelayed(INITIALIZATION_FAILED, 3000);
                return;
            }


            if (widget.getPluginXmlData().length() > 0) {
                xmlData = widget.getPluginXmlData();
            } else {
                xmlData = readXmlFromFile(widget.getPathToXmlFile());
            }

            widgetMD5 = Utils.md5(xmlData);

            if (widget.getTitle() != null && widget.getTitle().length() != 0) {
                setTopBarTitle(widget.getTitle());
            } else {
                setTopBarTitle(getResources().getString(R.string.romanblack_coupon_coupons));
            }


            // topbar initialization
            setTopBarLeftButtonText(getResources().getString(R.string.common_home_upper), true, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            cachePath = widget.getCachePath() + "/feed-" + widget.getOrder();
            File cache = new File(this.cachePath);
            if (!cache.exists()) {
                cache.mkdirs();
            }

            File cacheData = new File(cachePath + "/cache.data");
            if (cacheData.exists() && cacheData.length() > 0) {
                cacheMD5 = readFileToString(cachePath + "/cache.md5").replace("\n", "");
                if (cacheMD5.equals(widgetMD5)) {
                    useCache = true;
                } else {
                    File[] files = cache.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        files[i].delete();
                    }

                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(cachePath + "/cache.md5")));
                        bw.write(widgetMD5);
                        bw.close();
                        Log.d("IMAGES PLUGIN CACHE MD5", "SUCCESS");
                    } catch (Exception e) {
                        Log.w("IMAGES PLUGIN CACHE MD5", e);
                    }
                }
            }

            cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnectedOrConnecting()) {
                isOnline = true;
            }

            if (!isOnline && !useCache) {
                handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                return;
            }

            mainLayout = (RelativeLayout) findViewById(R.id.romanblack_coupon_feed_main);

            progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper), true);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    handler.sendEmptyMessage(LOADING_ABORTED);
                }
            });

            new Thread() {
                @Override
                public void run() {
                    try {//ErrorLogging
                        EntityParser parser;
                        parser = new EntityParser(xmlData);

                        parser.parse();

                        Statics.color1 = parser.getColor1();
                        Statics.color2 = parser.getColor2();
                        Statics.color3 = parser.getColor3();
                        Statics.color4 = parser.getColor4();
                        Statics.color5 = parser.getColor5();

                        handler.sendEmptyMessage(COLORS_RECIEVED);

                        title = (widget.getTitle().length() > 0) ? widget.getTitle() : parser.getFuncName();
                        items = parser.getItems();

                        if ("rss".equals(parser.getFeedType())) {

                            isRss = true;

                            if (isOnline) {
                                feedUrl = parser.getFeedUrl();
                                CouponParser reader = new CouponParser(Utils.removeSpec(parser.getFeedUrl()));
                                items = reader.parseFeed();

                                // cache info update
                                if (items.size() > 0) {
                                    File cache = new File(cachePath);
                                    File[] files = cache.listFiles();
                                    for (int i = 0; i < files.length; i++) {
                                        if (!files[i].getName().equals("cache.md5")) {
                                            files[i].delete();
                                        }
                                    }

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
                            } else {
                                try {
                                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cachePath + "/cache.data"));
                                    items = (ArrayList<CouponItem>) ois.readObject();
                                    ois.close();
                                } catch (Exception e) {
                                }
                            }
                        }

                        for (int i = 0; i < items.size(); i++) {
                            if ("rss".equals(parser.getFeedType())) {
                                items.get(i).setType(CouponItem.Types.RSS);
                            }

                            items.get(i).setTextColor(widget.getTextColor());
                            items.get(i).setDateFormat(widget.getDateFormat());
                        }

                        handler.sendEmptyMessage(SHOW_COUPONS);

                    } catch (Exception e) {
                    }
                }
            }.start();

        } catch (Exception e) {
        }
    }


    /* PRIVATE METHODS */

    /**
     * This method using when module data is too big to put in Intent
     *
     * @param fileName - xml module data file name
     * @return xml module data
     */
    protected String readXmlFromFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return stringBuilder.toString();
    }

    /**
     * Shows coupons list.
     */
    private void showCoupons() {
        try {//ErrorLogging

            setTitle(title);
            if (items.isEmpty()) {
                return;
            }

            mainLayout.setBackgroundColor(Statics.color1);

            listView = (PullToRefreshListView) findViewById(R.id.romanblack_coupon_feedList);
            listView.setBackgroundColor(Statics.color1);
            listView.setCacheColorHint(Statics.color1);
            listView.setSelector(R.drawable.romanblack_coupon_listview_selector);
            try {
                if (widget.getBackgroundColor() != Color.TRANSPARENT) {
                    listView.setBackgroundColor(widget.getBackgroundColor());
                }
            } catch (IllegalArgumentException e) {
            }

            CouponAdapter adapter = new CouponAdapter(this, items, widget.getBackgroundColor());
            adapter.setCachePath(cachePath);
            listView.setAdapter(adapter);

            if (isRss) {
                listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
                    public void onRefresh() {
                        loadRSSOnScroll();
                        listView.onRefreshComplete();
                    }
                });
            } else {
                listView.refreshOff();
            }

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                    if (isRss) {
                        showDetails(position - 1, view);
                    } else {
                        showDetails(position, view);
                    }
                }
            });

            if (progressDialog != null) {
                progressDialog.dismiss();
            }

        } catch (Exception e) {
        }
    }

    /**
     * Called when module colors was parsed.
     */
    private void colorsRecieved() {
        try {
            mainLayout.setBackgroundColor(Statics.color1);
        } catch (NullPointerException nPEx) {
        }
    }

    /**
     * Async loading and parsing RSS feed.
     */
    private void loadRSS() {
        try {//ErrorLogging

            progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper), true);

            new Thread() {
                @Override
                public void run() {
                    CouponParser reader = new CouponParser(feedUrl);
                    items = reader.parseFeed();

                    if (items.size() > 0) {
                        File cache = new File(cachePath);
                        File[] files = cache.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            if (!files[i].getName().equals("cache.md5")) {
                                files[i].delete();
                            }
                        }

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

                    for (int i = 0; i < items.size(); i++) {
                        items.get(i).setType(CouponItem.Types.RSS);

                        items.get(i).setTextColor(widget.getTextColor());
                        items.get(i).setDateFormat(widget.getDateFormat());
                    }

                    handler.sendEmptyMessage(SHOW_COUPONS);
                }
            }.start();

        } catch (Exception ex) { // Error Logging
        }
    }

    /**
     * Async loading and parsing RSS feed when PullToRefresh header released.
     */
    private void loadRSSOnScroll() {
        if (isRss) {
            if (cm != null) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting()) {
                    isOnline = true;
                    loadRSS();
                } else {
                    isOnline = false;
                }
            }
        }
    }

    /**
     * Go to item details page.
     *
     * @param position list item position
     * @param view
     */
    private void showDetails(int position, View view) {
        try {//ErrorLogging
            handler.sendEmptyMessageDelayed(CLEAR_ITEM_VIEW, 2000);

            Intent details = new Intent(this, CouponDetails.class);
            Bundle store = new Bundle();
            store.putSerializable("Widget", widget);
            store.putSerializable("item", items.get(position));
            details.putExtras(store);
            startActivity(details);

        } catch (Exception e) {
        }
    }

    /**
     * Restore views color.
     */
    private void clearItemView() {
        int bgColor = widget.getBackgroundColor();

        for (int i = 1; i < listView.getChildCount(); i++) {
            View itemView = listView.getChildAt(i);
            itemView.setBackgroundColor(bgColor);
        }
    }

    private String readFileToString(String pathToFile) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(pathToFile)));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
        } catch (Exception e) {
        }
        return sb.toString();
    }

    private void closeActivity() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        finish();
    }
}