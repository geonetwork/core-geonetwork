package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a block of links, ie hierarchy, related, linked pages, metadata, etc...
 *
 * @author Jesse on 11/20/2014.
 */
public class StaticLinkBlock extends LinkBlock {
    public Multimap<LinkType, Link> links = LinkedHashMultimap.create();

    public StaticLinkBlock(String nameKey) {
        super(nameKey);
    }

    @Override
    public boolean isEmpty() {
        return links.isEmpty();
    }

    @Override
    public void linksHtml(StringBuilder xml, Functions functions, Environment env) throws Exception {
        for (Map.Entry<LinkType, Collection<Link>> entry : links.asMap().entrySet()) {
            LinkType linkType = entry.getKey();
            xml.append("        <div class=\"col-xs-12\" style=\"background-color: #F7EEE1;\">");
            if (linkType.icon != null) {
                xml.append("            <img src=\"").append(linkType.icon).append("\" />");
            }
            xml.append(linkType.getName(functions));
            xml.append("</div>\n");

            for (Link link : entry.getValue()) {
                xml.append("        <div class=\"col-xs-6").append(" col-md-4").append("\">");
                xml.append("            <a href=\"").append(link.getHref()).append("\">").append(link.getText()).append("</a>");
                xml.append("        </div>\n");
            }
        }
    }

}
