package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.FileResult;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.fao.geonet.services.metadata.format.groovy.Handlers;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Summary {
    private final Handlers handlers;
    private final Environment env;
    private final Functions functions;
    public String logo;
    public String smallThumbnail;
    public String largeThumbnail;
    public String title = "";
    public String abstr = "";
    public Multimap<String, Link> links = LinkedHashMultimap.create();
    public Multimap<String, Link> hierarchy = LinkedHashMultimap.create();
    public String navBar = "";
    public String content = "";

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
        params.put("links", linksHtml("links", links));
        params.put("hierarchy", linksHtml("hierarchy", hierarchy));
        params.put("navBar", navBar);
        params.put("content", content);

        return handlers.fileResult("html/view-header.html", params);
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

    private String linksHtml(String type,  Multimap<String, Link> links) throws JDOMException, IOException {
        if (links.isEmpty()) {
            return "";
        }
        StringBuilder xml = new StringBuilder("    <h3>\n"
                     + "        <button type=\"button\" class=\"btn btn-default toggler\">\n"
                     + "            <i class=\"fa fa-arrow-circle-down\"></i>\n"
                     + "        </button>\n"
                     + "        " + functions.translate(type) + "\n"
                     + "    </h3>\n"
                     + "\n"
                     + "    <div class=\"row target\" style=\"border-top: 1px solid #D9AF71; border-bottom: 1px solid #D9AF71;\">\n");


        for (Map.Entry<String, Collection<Link>> entry : links.asMap().entrySet()) {
            xml.append("        <div class=\"col-xs-12\" style=\"background-color: #F7EEE1;\">");
            xml.append(functions.translate(entry.getKey()));
            xml.append("</div>\n");

            int xs = 6, md = 4, lg = 2;
            switch (entry.getValue().size()) {
                case 1:
                    xs = md = lg = 12;
                    break;
                case 2:
                    xs = md = lg = 6;
                    break;
                case 3:
                    md = lg = 4;
                    break;
                case 4:
                    lg = 3;
                    break;
                default:
                    break;
            }
            for (Link link : entry.getValue()) {
                xml.append("        <div class=\"col-xs-").append(xs).append(" col-md-").append(md).append(" col-lg-").append(lg).append("\">");
                xml.append("            <a href=\"").append(link.getHref()).append("\">").append(link.text).append("</a>");
                xml.append("        </div>\n");
            }
        }

        xml.append("        </div>\n");
        return xml.toString();
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