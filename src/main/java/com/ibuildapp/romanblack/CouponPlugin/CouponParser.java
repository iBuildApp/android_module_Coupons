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

import android.util.Log;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.util.Xml;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * This class used for RSS feed parsing
 */
public class CouponParser {

    private URL feedUrl = null;
    private String encoding = "";

    /**
     * Constructs new CouponParser
     */
    CouponParser() {
    }

    /**
     * Constructs new CouponParser with given RSS feed url
     * @param url RSS feed url
     */
    CouponParser(String url) {
        try {
            feedUrl = new URL(url);
        } catch (MalformedURLException e) {
        }
    }

    /**
     * Parse RSS feed with url that was set in constructor
     * @return parsed RSS feed items list
     */
    public ArrayList<CouponItem> parseFeed() {
        CouponHandler handler = new CouponHandler();
        try {
            String xml1 = "";
            String line1 = "";
            StringBuilder s1 = new StringBuilder();

            BufferedReader rd1 = new BufferedReader(new InputStreamReader(feedUrl.openStream()));
            try {
                while ((line1 = rd1.readLine()) != null) {
                    s1.append(line1);
                    s1.append("\n");
                }
                xml1 = s1.toString();
            } catch (Exception e) {
            }

            line1 = null;
            s1 = null;

            encoding = parseEncoding(xml1);

            xml1 = null;

            String xml = "";
            String line = "";
            StringBuilder s = new StringBuilder();

            BufferedReader rd = new BufferedReader(new InputStreamReader(feedUrl.openStream(), getEncoding()));
            try {
                while ((line = rd.readLine()) != null) {
                    s.append(line);
                    s.append("\n");
                }
                xml = s.toString();
            } catch (Exception e) {
            }

            line = null;
            s = null;

            encoding = parseEncoding(xml);
            Xml.parse(xml, handler);
        } catch (Exception e) {
            Log.d("", "", e);
        }
        return handler.getItems();
    }

    /**
     * Parses RSS feed encoding
     * @param xml RSS feed
     * @return encoding
     */
    private String parseEncoding(String xml) {
        String enc = "UTF-8";

        if (xml.contains("ISO-8859-1")) {
            enc = "ISO-8859-1";
        } else if (xml.contains("US-ASCII")) {
            enc = "US-ASCII";
        } else if (xml.contains("UTF-16")) {
            enc = "UTF-16";
        } else if (xml.contains("windows-1251")) {
            enc = "windows-1251";
        }

        return enc;
    }

    /**
     * @return the RSS feed encoding
     */
    public String getEncoding() {
        return encoding;
    }
}
