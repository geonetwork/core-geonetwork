package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.fao.geonet.services.metadata.format.groovy.template.FileResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the summary of a metadata element.  It is often the top section of a metadata view and summarizes the critical part
 * of the metadata to most users.
 * <p/>
 * The purpose of this class is to provide consistent way to display a summary of a metadata for all schemas.  The formatter/view only
 * needs to populate the fields of this class (or subclass) and the class can take care of presentation.
 * <p/>
 * This implementation includes logo, thumbnail, title, abstract, navigation bar and the content (view).  Any of the fields can be
 * left with their default values and they will not be displayed in the Summary.
 * <p/>
 * The data is rendered with the view-header.html template.
 */
public class Summary {
    protected final Handlers handlers;
    protected final Environment env;
    protected final Functions functions;

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

    public List<LinkBlock> links = Lists.newArrayList();

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
        params.put("addOverviewNavItem", addOverviewNavItem);
        params.put("navBar", navBar);
        params.put("navBarOverflow", navBarOverflow);
        params.put("showNavOverflow", !navBarOverflow.isEmpty() || addCompleteNavItem);
        params.put("addCompleteNavItem", addCompleteNavItem);
        params.put("content", content);
        params.put("extents", extent != null ? extent : "");
        params.put("formats", formats != null ? formats : "");
        params.put("keywords", keywords != null ? keywords : "");
        params.put("isHTML", env.getFormatType() == FormatType.html);
        params.put("isPDF", env.getFormatType() == FormatType.pdf);

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
        final Path mdDataDir = Lib.resource.getDir(env.getBean(GeonetworkDataDirectory.class), Params.Access.PUBLIC, env.getMetadataId());
        return Files.exists(mdDataDir.resolve(imgFile));
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