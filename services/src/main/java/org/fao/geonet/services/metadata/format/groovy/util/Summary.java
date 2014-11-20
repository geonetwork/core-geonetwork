package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.FileResult;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.jdom.JDOMException;

import java.io.IOException;
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

    public String logo;
    public String smallThumbnail;
    public String largeThumbnail;
    public String title = "";
    public String abstr = "";
    public String navBar = "";
    public String content = "";

    public List<LinkBlock> links = Lists.newArrayList();

    public Summary(Handlers handlers, Environment env, Functions functions) {
        this.handlers = handlers;
        this.env = env;
        this.functions = functions;
    }

    public FileResult getResult() throws IOException, JDOMException {
        HashMap<String, Object> params = Maps.newHashMap();

        params.put("logo", logoHtml());
        params.put("title", title != null ? title : "");
        params.put("abstract", abstrHtml());
        params.put("thumbnail", thumbnailHtml());
        addLinks(params);
        params.put("navBar", navBar);
        params.put("content", content);

        return handlers.fileResult("html/view-header.html", params);
    }

    /**
     * Adds Links section to the params.  This implementation adds an empty string because this summary does not have links.
     */
    protected void addLinks(HashMap<String, Object> params) throws JDOMException, IOException {
        StringBuilder linksHtml = new StringBuilder();
        for (LinkBlock link : links) {
            link.linksHtml(linksHtml, functions, env);
        }

        params.put("links", linksHtml);
    }

    private String abstrHtml() throws JDOMException, IOException {
        if (abstr == null || abstr.isEmpty()) {
            return "";
        }

        String translatedTitle = functions.translate("abstract");

        return "    <h3>\n"
                + "        <button type=\"button\" class=\"btn btn-default toggler\">\n"
                + "            <i class=\"fa fa-arrow-circle-down\"></i>\n"
                + "        </button>\n"
                + "        " + translatedTitle + "\n"
                + "    </h3>\n"
                + "\n"
                + "    <div class=\"target\">\n"
                + "        " + abstr + "\n"
                + "    </div>\n";
    }

    String thumbnailHtml() {
        String img;
        if (this.smallThumbnail != null) {
            img = this.smallThumbnail;
        } else if (this.largeThumbnail != null) {
            img = this.largeThumbnail;
        } else {
            return "";
        }

        String logoUrl;
        if (img.startsWith("http://") || img.startsWith("https://")) {
            logoUrl = img;
        } else {
            logoUrl = env.getLocalizedUrl() + "resources.get?fname=" + img + "&amp;access=public&amp;id=" + env.getMetadataId();

        }
        return "<img src=\"" + logoUrl + "\"></img>";
    }

    String logoHtml() {
        if (logo != null) {
            return "<img style=\"max-width: 128px\" src=\"" + logo + "\"></img>";
        } else {
            return "<i class=\"fa fa-arrow-circle-down\"></i>";
        }
    }
}