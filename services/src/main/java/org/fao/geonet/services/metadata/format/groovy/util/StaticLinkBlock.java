package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.jdom.JDOMException;

import java.io.IOException;
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
    public void linksHtml(StringBuilder xml, Functions functions, Environment env) throws JDOMException, IOException {
        for (Map.Entry<LinkType, Collection<Link>> entry : links.asMap().entrySet()) {
            LinkType linkType = entry.getKey();
            xml.append("        <div class=\"col-xs-12\" style=\"background-color: #F7EEE1;\">");
            if (linkType.icon != null) {
                xml.append("            <img src=\"").append(linkType.icon).append("\" />");
            }
            xml.append(functions.translate(linkType.name));
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
    }

}
