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

import android.graphics.Color;
import android.sax.*;
import android.util.Log;
import android.util.Xml;
import org.xml.sax.Attributes;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * This class describes xml representation of COUPON module F.E.
 * <item>
 * <title><![CDATA[face]]></title>
 * <description><![CDATA[faaaaaace]]></description>
 * <url><![CDATA[http://ibuildapp.com/assets/data/00089/89851/341358/coupons/2674504-1360576015.html]]></url>
 * </item>
 */
public class EntityParser {

    private ArrayList<CouponItem> items = new ArrayList<CouponItem>();
    private String xml = "";
    private String type = "";
    private String func = "";
    private String feed = "";
    private String title = "";      // <title>
    private CouponItem item = null;
    private int color1 = Color.parseColor("#4d4948");// background
    private int color2 = Color.parseColor("#fff58d");// category header
    private int color3 = Color.parseColor("#fff7a2");// text header
    private int color4 = Color.parseColor("#ffffff");// text
    private int color5 = Color.parseColor("#bbbbbb");// date

    /**
     * Constructs new EntityParser instance
     * @param xml - module xml data to parse
     */
    EntityParser(String xml) {
        this.xml = xml.trim();
    }

    /**
     * @return "news" | "rss" | "events" 
     */
    public String getFuncName() {
        return func;
    }

    /**
     * @return coupons feed type
     */
    public String getFeedType() {
        return type;
    }

    /**
     * @return parsed feed url if it was configured
     */
    public String getFeedUrl() {
        return feed;
    }

    /**
     * @return parsed module title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return parsed items if it was configured
     */
    public ArrayList<CouponItem> getItems() {
        return items;
    }

    /**
     * Parse module data that was set in constructor.
     */
    public void parse() {
        RootElement root = new RootElement("data");

        Element colorSchemeElement = root.getChild("colorskin");

        Element color1Element = colorSchemeElement.getChild("color1");
        color1Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                try {
                    color1 = Color.parseColor(arg0.trim());
                } catch (Exception e) {
                }
            }
        });

        Element color2Element = colorSchemeElement.getChild("color2");
        color2Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                try {
                    color2 = Color.parseColor(arg0.trim());
                } catch (Exception e) {
                }
            }
        });

        Element color3Element = colorSchemeElement.getChild("color3");
        color3Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                try {
                    color3 = Color.parseColor(arg0.trim());
                } catch (Exception e) {
                }
            }
        });

        Element color4Element = colorSchemeElement.getChild("color4");
        color4Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                try {
                    color4 = Color.parseColor(arg0.trim());
                } catch (Exception e) {
                }
            }
        });

        Element color5Element = colorSchemeElement.getChild("color5");
        color5Element.setEndTextElementListener(new EndTextElementListener() {
            public void end(String arg0) {
                try {
                    color5 = Color.parseColor(arg0.trim());
                } catch (Exception e) {
                }
            }
        });

        android.sax.Element title = root.getChild("title");

        android.sax.Element rss = root.getChild("rss");
        android.sax.Element coupon = root.getChild("item");

        root.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
            }
        });

        title.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                EntityParser.this.title = body;
            }
        });

        coupon.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                item = new CouponItem();
            }
        });

        coupon.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                items.add(item);
                item = null;
            }
        });

        coupon.getChild("title").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setTitle(body);
                }
            }
        });

        coupon.getChild("url").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setLink(body);
                }
            }
        });

        coupon.getChild("description").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setDescription(body);
                }
            }
        });

        coupon.getChild("expiration").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                if (item != null) {
                    item.setDescription(body);
                }
            }
        });

        rss.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                type = "rss";
                func = "coupons";
            }
        });

        rss.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
            }
        });

        rss.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String body) {
                feed = body;
            }
        });

        try {
            Xml.parse(new ByteArrayInputStream(xml.getBytes()), Xml.Encoding.UTF_8, root.getContentHandler());
        } catch (Exception e) {
            Log.d("", "");
        }
    }

    /**
     * @return parsed color 1 of color scheme
     */
    public int getColor1() {
        return color1;
    }

    /**
     * @return parsed color 2 of color scheme
     */
    public int getColor2() {
        return color2;
    }

    /**
     * @return parsed color 3 of color scheme
     */
    public int getColor3() {
        return color3;
    }

    /**
     * @return parsed color 4 of color scheme
     */
    public int getColor4() {
        return color4;
    }

    /**
     * @return parsed color 5 of color scheme
     */
    public int getColor5() {
        return color5;
    }
}
