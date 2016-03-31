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

import com.appbuilder.sdk.android.AppBuilderModule;
import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.Widget;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URL;

/**
 * This Activity represents rss item or event details
 */
public class CouponDetails extends AppBuilderModule {

    private enum states {

        EMPTY, LOAD_START, LOAD_PROGRESS, LOAD_COMPLETE
    };
    private WebView webView = null;
    private ProgressDialog progressDialog = null;
    final private int SHOW_PROGRESS = 0;
    final private int HIDE_PROGRESS = 1;
    final private int NEED_INTERNET_CONNECTION = 2;
    final private int LOADING_ABORTED = 5;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case SHOW_PROGRESS: {
                    showProgress();
                }
                break;
                case HIDE_PROGRESS: {
                    hideProgress();
                }
                break;
                case NEED_INTERNET_CONNECTION: {
                    Toast.makeText(CouponDetails.this, R.string.alert_no_internet, Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            closeActivity();
                        }
                    }, 5000);
                }
                break;
                case LOADING_ABORTED: {
                    closeActivity();
                }
                break;
            }
        }
    };
    private String currentUrl = "";
    private states state1 = states.EMPTY;
    private boolean isOnline = false;

    @Override
    public void create() {
        try {//ErrorLogging    


            setContentView(R.layout.romanblack_coupon_feed_details);
            setTitle(R.string.romanblack_coupon_coupons);

            Intent currentIntent = getIntent();
            Bundle store = currentIntent.getExtras();
            CouponItem item = (CouponItem) store.getSerializable("item");
            if (item == null){
                finish();
            }

            Widget widget = (Widget) store.getSerializable("Widget");
            if (widget != null) {
                if (widget.getTitle().length() > 0) {
                    setTitle(widget.getTitle());
                }
            }

            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null && ni.isConnectedOrConnecting()) {
                isOnline = true;
            }

            webView = (WebView) findViewById(R.id.romanblack_coupon_feedDetails);
            webView.getSettings().setJavaScriptEnabled(true);
//            webView.getSettings().setPluginsEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setUseWideViewPort(false);
            webView.getSettings().setLoadWithOverviewMode(false);
            webView.getSettings().setDefaultTextEncodingName("utf-8");
            webView.clearHistory();

            webView.setWebViewClient(
                    new WebViewClient() {
                        @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    if (state1 == states.EMPTY) {
                        currentUrl = url;
                        setSession(currentUrl);
                        state1 = states.LOAD_START;
                        handler.sendEmptyMessage(SHOW_PROGRESS);
                    }
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    state1 = states.LOAD_COMPLETE;
                    handler.sendEmptyMessage(HIDE_PROGRESS);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    currentUrl = url;
                    setSession(currentUrl);
                    if (!isOnline) {
                        handler.sendEmptyMessage(HIDE_PROGRESS);
                        handler.sendEmptyMessage(NEED_INTERNET_CONNECTION);
                    } else {
                        if (!url.contains("ibuildapp") && !url.contains("solovathost")) {
                                    view.getSettings().setLoadWithOverviewMode(true);
                                    view.getSettings().setUseWideViewPort(true);
                        }
                        view.setBackgroundColor(Color.WHITE);
                    }
                    return false;
                }
            });

            currentUrl = (String) getSession();
            if (currentUrl == null) {
                currentUrl = "";
            }

            if (item.getType() == CouponItem.Types.ITEM && currentUrl.length() == 0) {
                currentUrl = item.getLink();
            }

            if (currentUrl.length() > 0 && !currentUrl.equals("about:blank")) {
                URL url = new URL(currentUrl);

                if (url.getFile().endsWith(".pdf")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(currentUrl));
                    startActivity(intent);
                    finish();
                    return;
                }

                webView.loadUrl(currentUrl);

                handler.sendEmptyMessageDelayed(HIDE_PROGRESS, 10000);
            } else {
                StringBuilder html = new StringBuilder();
                html.append("<html>");
                html.append("<body>");
                html.append(item.getDescription());
                if (item.getLink().length() > 0) {
                    html.append("<br><br><a href=\"");
                    html.append(item.getLink());
                    html.append("\"><strong>" + getString(R.string.romanblack_coupon_read_more) + " ...</strong></a>");
                }
                html.append("</body>");
                html.append("</html>");

                webView.loadDataWithBaseURL(null, html.toString(), "text/html", "utf-8", null);
            }


        } catch (Exception e) {
        }
    }

    @Override
    public void pause() {
        webView.stopLoading();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void destroy() {
        webView.stopLoading();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /* PRIVATE METHODS */
    private void showProgress() {
        if (state1 == states.LOAD_START) {
            state1 = states.LOAD_PROGRESS;
        }

        progressDialog = ProgressDialog.show(this, null, getString(R.string.common_loading_upper), true);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                handler.sendEmptyMessage(LOADING_ABORTED);
            }
        });
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        state1 = states.EMPTY;
    }

    public void closeActivity() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        finish();
    }
}
