package org.fao.geonet.services.metadata.format.groovy.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Jesse on 11/20/2014.
 */
public class LinkBlock {
    public static String CSS_CLASS_PREFIX = "summary-links-";

    public final Multimap<LinkType, Link> links = LinkedHashMultimap.create();
    private String html;

    /**
     * The translation key for the name of this section.
     */
    public final String name;
    public LinkBlock(String name) {
        this.name = name;
    }

    public String getName() throws JDOMException, IOException {
        return name;
    }

    public void put(LinkType type, Link link) {
        this.links.put(type, link);
    }

    public Collection<LinkBlockEntry> getLinks() {
        return Collections2.transform(links.asMap().entrySet(), new Function<Map.Entry<LinkType, Collection<Link>>, LinkBlockEntry>() {
            @Nullable
            @Override
            public LinkBlockEntry apply(Map.Entry<LinkType, Collection<Link>> input) {
                return new LinkBlockEntry(input.getKey(), input.getValue());
            }
        });
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public static final class LinkBlockEntry {
        private final LinkType type;
        private final Collection<Link> links;

        public LinkBlockEntry(LinkType type, Collection<Link> links) {
            this.type = type;
            this.links = links;
        }

        public LinkType getType() {
            return this.type;
        }

        public Collection<Link> getLinks() {
            return this.links;
        }
    }
}
