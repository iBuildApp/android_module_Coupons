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

import com.appbuilder.sdk.android.Utils;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Sax handler that handle configuration xml tags and prepare module data structure
 */
public class CouponHandler extends DefaultHandler {

    private ArrayList<CouponItem> items = new ArrayList<CouponItem>();
    private CouponItem item = null;
    private StringBuilder builder;
    private String datePattern = "";
    private boolean isMedia = false;
    private boolean hasLink = false;
    private boolean wasGoogleDate = false;

    public ArrayList<CouponItem> getItems() {
        return items;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.item != null) {
            builder.append(ch, start, length);
        }
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
        this.items = new ArrayList<CouponItem>();
        this.builder = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (localName.equalsIgnoreCase("content")
                && uri.equalsIgnoreCase("http://search.yahoo.com/mrss/")) {
            if (item != null) {
                try {
                    if (attributes.getValue("medium").equalsIgnoreCase("image")) {
                        if (!item.hasImage()) {
                            item.setImageUrl(attributes.getValue("url"));
                        }
                    }
                } catch (NullPointerException nPEx) {
                }
            }
        } else if (localName.equalsIgnoreCase("when")
                && uri.equalsIgnoreCase("http://schemas.google.com/g/2005")) {
            if (item != null) {
                if (!attributes.getValue("startTime").equalsIgnoreCase("")) {
                    item.setPubdate(attributes.getValue("startTime"));
                    wasGoogleDate = true;
                }
            }
        } else if (localName.equalsIgnoreCase("enclosure")) {
        } else if (localName.equalsIgnoreCase("player")
                && uri.equalsIgnoreCase("http://search.yahoo.com/mrss/")) {
        } else if (localName.equalsIgnoreCase("ITEM") || localName.equalsIgnoreCase("ENTRY")) {
            this.item = new CouponItem();
        }
        isMedia = (uri.indexOf("mrss") == -1) ? false : true;

        if (isMedia && localName.equalsIgnoreCase("thumbnail")) {
            if (this.item != null) {
                String url = attributes.getValue("url");
                if (url.length() > 0) {
                    item.setImageUrl(url);
                }
            }
        }
        if (this.item != null) {
            if (localName.equalsIgnoreCase("LINK")) {
                String rel = attributes.getValue("rel");
                if (rel == null || rel.equalsIgnoreCase("ALTERNATE") || rel.length() == 0) {
                    hasLink = true;
                    String lnk = attributes.getValue("href");
                    if (lnk != null) {
                        if (lnk.length() != 0) {
                            this.item.setLink(lnk);
                        }
                    }
                }
                if (rel == null || rel.equalsIgnoreCase("IMAGE") || rel.length() == 0) {
                    this.item.setImageUrl(attributes.getValue("href"));
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {

        if (this.item != null) {
            if (uri.equalsIgnoreCase("http://www.w3.org/2005/Atom")
                    || uri.equalsIgnoreCase("http://purl.org/rss/1.0/")) {
                uri = "";
            }

            boolean uriLengthEmpty = uri.length() == 0;

            if (uriLengthEmpty && localName.equalsIgnoreCase("TITLE")) {
                this.item.setTitle(Utils.removeSpec(builder.toString()));
            } else if (uriLengthEmpty && localName.equalsIgnoreCase("description")) {
                this.item.setDescription(builder.toString());
            } else if (uriLengthEmpty && localName.equalsIgnoreCase("CONTENT")) {
                this.item.setDescription(builder.toString());
            } else if (uriLengthEmpty && localName.equalsIgnoreCase("SUMMARY")) {
                if (this.item.getDescription().length() == 0) {
                    this.item.setDescription(builder.toString());
                }
            } else if (uriLengthEmpty && localName.equalsIgnoreCase("PUBDATE")) {
                if (!wasGoogleDate) {
                    if (datePattern.length() == 0) {
                        datePattern = this.item.setPubdate(builder.toString());
                    } else {
                        this.item.setPubdate(builder.toString(), datePattern);
                    }
                }
            } else if (uriLengthEmpty && localName.equalsIgnoreCase("UPDATED")) {
                if (!wasGoogleDate) {
                    if (datePattern.length() == 0) {
                        datePattern = this.item.setPubdate(builder.toString());
                    } else {
                        this.item.setPubdate(builder.toString(), datePattern);
                    }
                }
            } else if (uriLengthEmpty && localName.equalsIgnoreCase("LINK")) {
                if (hasLink) {
                    if (builder.toString().trim().length() != 0) {
                        this.item.setLink(builder.toString().trim());
                    }
                }
            } else if (localName.equalsIgnoreCase("ITEM") || localName.equalsIgnoreCase("ENTRY")) {
                this.items.add(item);
                item = null;
            } else if (localName.equalsIgnoreCase("date")
                    && uri.equalsIgnoreCase("http://purl.org/dc/elements/1.1/")) {
                if ("".equals(this.item.getPubdate(""))) {
                    if (datePattern.length() == 0) {
                        datePattern = this.item.setPubdate(builder.toString());
                    } else {
                        this.item.setPubdate(builder.toString(), datePattern);
                    }
                }
            } else if (localName.equalsIgnoreCase("encoded")
                    && uri.equalsIgnoreCase("http://purl.org/rss/1.0/modules/content/")) {
                this.item.setDescription(builder.toString());
            }
            this.builder.setLength(0);
        }
    }
}
