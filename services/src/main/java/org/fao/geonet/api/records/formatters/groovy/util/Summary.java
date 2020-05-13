/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.groovy.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.groovy.Environment;
import org.fao.geonet.api.records.formatters.groovy.Functions;
import org.fao.geonet.api.records.formatters.groovy.Handlers;
import org.fao.geonet.api.records.formatters.groovy.template.FileResult;
import org.fao.geonet.domain.MetadataResourceVisibility;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the summary of a metadata element.  It is often the top section of a metadata view and
 * summarizes the critical part of the metadata to most users.
 * <p/>
 * The purpose of this class is to provide consistent way to display a summary of a metadata for all
 * schemas.  The formatter/view only needs to populate the fields of this class (or subclass) and
 * the class can take care of presentation.
 * <p/>
 * This implementation includes logo, thumbnail, title, abstract, navigation bar and the content
 * (view).  Any of the fields can be left with their default values and they will not be displayed
 * in the Summary.
 * <p/>
 * The data is rendered with the view-header.html template.
 */
public class Summary {
    protected final Handlers handlers;
    protected final Environment env;
    protected final Functions functions;
    public List<LinkBlock> links = Lists.newArrayList();
    public List<LinkBlock> associated = Lists.newArrayList();
    private String logo;
    private List<String> thumbnails = Lists.newArrayList();
    private String title = "";
    private String abstr = "";
    private List<NavBarItem> navBar = Lists.newArrayList();
    private List<NavBarItem> navBarOverflow = Lists.newArrayList();
    private String content = "";
    private boolean addCompleteNavItem = true;
    private boolean addOverviewNavItem = true;
    private String keywords = "";
    private String extent = "";
    private String formats = "";

    public Summary(Handlers handlers, Environment env, Functions functions) throws Exception {
        this.handlers = handlers;
        this.env = env;
        this.functions = functions;
        Collection<String> logo = env.getIndexInfo().get("_logo");
        if (logo != null && !logo.isEmpty()) {
            this.logo = env.getLocalizedUrl() + "../.." + logo.iterator().next();
        }
    }

    public FileResult getResult() throws Exception {
        HashMap<String, Object> params = Maps.newHashMap();

        params.put("logo", logo);
        params.put("title", title != null ? title : "");
        params.put("pageTitle", title != null ? title.replace('"', '\'') : "");
        params.put("abstract", abstr);
        params.put("thumbnail", thumbnailUrl());
        params.put("links", links);
        params.put("associated", associated);
        params.put("addOverviewNavItem", addOverviewNavItem);
        params.put("navBar", navBar);
        params.put("navBarOverflow", navBarOverflow);
        params.put("showNavOverflow", !navBarOverflow.isEmpty());
        params.put("addCompleteNavItem", addCompleteNavItem);
        params.put("content", content);
        params.put("extents", extent != null ? extent : "");
        params.put("formats", formats != null ? formats : "");
        params.put("keywords", keywords != null ? keywords : "");
        params.put("isHTML", env.getFormatType() == FormatType.html);
        params.put("isPDF", env.getFormatType() == FormatType.pdf || env.getFormatType() == FormatType.testpdf);

        return handlers.fileResult("html/view-header.html", params);
    }

    String thumbnailUrl() {
        String thumbnail = null;
        for (String t : thumbnails) {
            boolean isUrl = thumbnailIsUrl(t);
            boolean isSmall = isSmallThumbnail(t);
            // Preferred is large thumbnail that is a resource url:
            if (!isUrl && resourceUrlExists(t) && !isSmall) {
                thumbnail = resourceThumbnailUrl(t);
                break;
            }
            // Next preference is a full sized thumbnail
            if (isUrl) {
                thumbnail = t;
            }
            // Last choice is the small thumbnail
            if (thumbnail == null && resourceUrlExists(t)) {
                thumbnail = resourceThumbnailUrl(t);
            }
        }

        if (thumbnail == null) {
            thumbnail = "";
        }

        return thumbnail;
    }

    private String resourceThumbnailUrl(String t) {
        return env.getLocalizedUrl() + "resources.get?fname=" + t + "&access=public&id=" + env.getMetadataId();
    }

    private boolean isSmallThumbnail(String img) {
        return img.matches(".*_s\\.[^.]+");
    }

    private boolean resourceUrlExists(String imgFile) {
        final Store store = env.getBean("resourceStore", Store.class);
        try {
            return store.getResourceDescription(env.getContext(), env.getMetadataUUID(), MetadataResourceVisibility.PUBLIC,
                                                imgFile, true) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean thumbnailIsUrl(String img) {
        return img.startsWith("http://") || img.startsWith("https://");
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setThumbnails(List<String> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public void addThumbnail(String thumbnail) {
        this.thumbnails.add(thumbnail);
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setAbstr(String abstr) {
        this.abstr = abstr;
    }

    public void addNavBarItem(NavBarItem item) {
        this.navBar.add(item);
    }

    public void setNavBar(List<NavBarItem> navBar) {
        this.navBar = navBar;
    }

    public void addNavBarOverflow(NavBarItem item) {
        this.navBarOverflow.add(item);
    }

    public void setNavBarOverflow(List<NavBarItem> navBarOverflow) {
        this.navBarOverflow = navBarOverflow;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isAddCompleteNavItem() {
        return addCompleteNavItem;
    }

    public void setAddCompleteNavItem(boolean addCompleteNavItem) {
        this.addCompleteNavItem = addCompleteNavItem;
    }

    public boolean isAddOverviewNavItem() {
        return addOverviewNavItem;
    }

    public void setAddOverviewNavItem(boolean addOverviewNavItem) {
        this.addOverviewNavItem = addOverviewNavItem;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public void setFormats(String formats) {
        this.formats = formats;
    }
}
