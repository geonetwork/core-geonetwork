package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.template.FileResult;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;

import java.util.HashMap;
import java.util.List;

/**
 * Represents the summary of a metadata element.  It is often the top section of a metadata view and summarizes the critical part
 * of the metadata to most users.
 *
 * The purpose of this class is to provide consistent way to display a summary of a metadata for all schemas.  The formatter/view only
 * needs to populate the fields of this class (or subclass) and the class can take care of presentation.
 *
 * This implementation includes logo, thumbnail, title, abstract, navigation bar and the content (view).  Any of the fields can be
 * left with their default values and they will not be displayed in the Summary.
 */
public class Summary {
    protected final Handlers handlers;
    protected final Environment env;
    protected final Functions functions;

    private String logo;
    private String smallThumbnail;
    private String largeThumbnail;
    private String title = "";
    private String abstr = "";
    private List<NavBarItem> navBar = Lists.newArrayList();
    private List<NavBarItem> navBarOverflow = Lists.newArrayList();
    private String content = "";

    public List<LinkBlock> links = Lists.newArrayList();

    public Summary(Handlers handlers, Environment env, Functions functions) {
        this.handlers = handlers;
        this.env = env;
        this.functions = functions;
    }

    public FileResult getResult() throws Exception {
        HashMap<String, Object> params = Maps.newHashMap();

        params.put("logo", logo);
        params.put("title", title != null ? title : "");
        params.put("pageTitle", title != null ? title.replace('"', '\'') : "");
        params.put("abstract", abstr);
        params.put("thumbnail", thumbnailUrl());
        params.put("links", links);
        params.put("navBar", navBar);
        params.put("navBarOverflow", navBarOverflow);
        params.put("content", content);
        params.put("isHTML", env.getFormatType() == FormatType.html);

        return handlers.fileResult("html/view-header.html", params);
    }

    String thumbnailUrl() {
        String img;
        if (this.smallThumbnail != null) {
            img = this.smallThumbnail;
        } else if (this.largeThumbnail != null) {
            img = this.largeThumbnail;
        } else {
            return "";
        }

        String thumbnailUrl;
        if (img.startsWith("http://") || img.startsWith("https://")) {
            thumbnailUrl = img;
        } else {
            thumbnailUrl = env.getLocalizedUrl() + "resources.get?fname=" + img + "&amp;access=public&amp;id=" + env.getMetadataId();

        }
        return thumbnailUrl;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setSmallThumbnail(String smallThumbnail) {
        this.smallThumbnail = smallThumbnail;
    }

    public void setLargeThumbnail(String largeThumbnail) {
        this.largeThumbnail = largeThumbnail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAbstr(String abstr) {
        this.abstr = abstr;
    }

    public void setNavBar(List<NavBarItem> navBar) {
        this.navBar = navBar;
    }

    public void setNavBarOverflow(List<NavBarItem> navBarOverflow) {
        this.navBarOverflow = navBarOverflow;
    }

    public void setContent(String content) {
        this.content = content;
    }
}