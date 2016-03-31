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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.graphics.Color;

/**
 * Entity class that represents item in Coupons list
 */
public class CouponItem implements Serializable {

    private static final long serialVersionUID = 1L;

    static public enum Types {

        RSS, ITEM
    };
    private String title = "";
    private String description = "";
    private String anounce = "";
    private Date pubdate = null;
    private String imageUrl = "";
    private String imagePath = "";
    private String indextext = "";
    private int color = Color.DKGRAY;
    private int dateformat = 0;
    private String link = "";
    private CouponItem.Types type = CouponItem.Types.ITEM;

    CouponItem() {
    }

    /**
     * Sets the type of this item 
    * @param type item type
     */
    public void setType(CouponItem.Types type) {
        this.type = type;
    }

    /**
     * Returns this item type
     * @return item type
     */
    public CouponItem.Types getType() {
        return type;
    }

    /**
     * Sets the title of coupon item
     *
     * @param value title
     */
    public void setTitle(String value) {
        title = value;
    }

    /**
     * Returns a string title of coupon item
     *
     * @return title of item
     */
    public String getTitle() {
        return title.trim();
    }

    /**
     * Sets the description of coupon item
     * @param value description to set 
     */
    synchronized public void setDescription(String value) {
        try {
            value = value.trim();

            Document doc = Jsoup.parse(value);

            if (imageUrl == null) {
                imageUrl = "";
            }

            if (imageUrl.length() == 0) {
                Element img = doc.select("img").first();
                if (img != null) {
                    imageUrl = img.attr("src");
                }

                if (imageUrl == null) {
                    imageUrl = "";
                }

                String ext = imageUrl.substring(imageUrl.lastIndexOf(".") + 1);
                if (!ext.equalsIgnoreCase("png") && !ext.equalsIgnoreCase("gif") && !ext.equalsIgnoreCase("jpg") && !ext.equalsIgnoreCase("jpeg")) {
                    imageUrl = "";
                }
            }

            anounce = doc.text();
            try {
                description = value;
            } catch (Exception e) {
                description = e.getMessage();
            }
        } catch (NullPointerException nPEx) {
        }
    }

    /**
     * Returns a string description of coupon item.
     *
     * @return description of item
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the short description of item
     * @param length announce lenght
     * @return 
     */
    public String getAnounce(int length) {
        if (length > 30) {
            length -= 3;
        }
        return (anounce.length() > length) ? anounce.substring(0, length) + "..." : anounce;
    }

    /**
     * Sets pubdate of coupon item.
     * @param value date string
     * @param pattern date sreing pattern
     */
    synchronized public void setPubdate(String value, String pattern) {
        value = value.replaceAll("[\\n\\t]", "");
        value = value.trim();
        if (value.contains("+")) {
            value = value.substring(0, value.lastIndexOf("+") - 1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);

        try {
            if (getPubdate() == null) {
                pubdate = sdf.parse(value);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Sets pubdate of coupon item.
     * If you have date string patter use {@code setPubdate(String, String)} instead.
     * @param value date string
     * @return 
     */
    synchronized public String setPubdate(String value) {
        value = value.replaceAll("[\\n\\t]", "");
        value = value.trim();
        if (value.contains("+")) {
            value = value.substring(0, value.lastIndexOf("+") - 1);
        }

        String[] patterns = {
            "yyyy.MM.dd G 'at' HH:mm:ss z",
            "EEE, MMM d, ''yy",
            "yyyyy.MMMMM.dd GGG hh:mm aaa",
            "EEE, d MMM yyyy HH:mm:ss Z",
            "yyMMddHHmmssZ",
            "d MMM yyyy HH:mm:ss z",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssz",
            "yyyy-MM-dd'T'HH:mm:ss.SSSz",
            "EEE, d MMM yy HH:mm:ssz",
            "EEE, d MMM yy HH:mm:ss",
            "EEE, d MMM yy HH:mm z",
            "EEE, d MMM yy HH:mm Z",
            "EEE, d MMM yyyy HH:mm:ss z",
            "EEE, d MMM yyyy HH:mm:ss Z",
            "EEE, d MMM yyyy HH:mm:ss ZZZZ",
            "EEE, d MMM yyyy HH:mm z",
            "EEE, d MMM yyyy HH:mm Z",
            "d MMM yy HH:mm z",
            "d MMM yy HH:mm:ss z",
            "d MMM yyyy HH:mm z",
            "d MMM yyyy HH:mm:ss z"};

        for (int i = 0; i < patterns.length; i++) {
            SimpleDateFormat sdf = new SimpleDateFormat(patterns[i], Locale.ENGLISH);
            try {
                if (getPubdate() == null) {
                    pubdate = sdf.parse(value);
                    return patterns[i];
                }
            } catch (Exception e) {
            }
        }

        return "";
    }

    /**
     * Returns publication date of coupon item with given format.
     * @param format
     * @return 
     */
    public String getPubdate(String format) {
        if (pubdate == null) {
            return "";
        } else {
            if (format.length() == 0) {
                format = "MMM dd yyyy hh:mm a";
                if (dateformat == 1) {
                    format = "dd MMM yyyy HH:mm";
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            try {
                return sdf.format(pubdate);
            } catch (Exception e) {
                return "";
            }
        }
    }

    /**
     * @return true if this coupon item has image, false otherwise.
     */
    public boolean hasImage() {
        try {
            return (imageUrl.length() > 0 || imagePath.length() > 0) ? true : false;
        } catch (Throwable thr) {
            return false;
        }
    }

    /**
     * Sets the image url to this coupon item.
     * @param value image url to set.
     */
    synchronized public void setImageUrl(String value) {
        imageUrl = value;
    }

    /**
     * Returns the image url of this feed item.
     * @return image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the path of downloaded image
     * @param value path of downloaded image
     */
    synchronized public void setImagePath(String value) {
        imagePath = value;
    }

    /**
     * Returns the path of downloadad imade
     * @return image path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the index text to this coupon item.
     * @param value index text to set
     */
    synchronized public void setIndextext(String value) {
        indextext = value.trim();
    }

    /**
     * @return the index text
     */
    public String getIndextext() {
        return indextext;
    }

    /**
     * Sets coupon item text color.
     * @param color text color to set
     */
    synchronized public void setTextColor(int color) {
        if (color != Color.TRANSPARENT) {
            this.color = color;
        }
    }

    /**
     * Returns coupon item text color.
     * @return coupon item text color
     */
    public int getTextColor() {
        return color;
    }

    /**
     * Sets coupon item date format.
     * @param value date format to set 1 - 24 hours format, 0 - 12 hours format
     */
    synchronized public void setDateFormat(int value) {
        dateformat = (value != 0 || value != 1) ? 0 : 1;;
    }

    /**
     * Sets link.
     * @param value link to set 
     */
    synchronized public void setLink(String value) {
        link = value.trim();
    }

    /**
     * Returns link.
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * @return the pubdate
     */
    public Date getPubdate() {
        return pubdate;
    }
}
