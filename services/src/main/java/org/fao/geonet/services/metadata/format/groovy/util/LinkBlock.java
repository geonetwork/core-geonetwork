package org.fao.geonet.services.metadata.format.groovy.util;

import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * @author Jesse on 11/20/2014.
 */
public abstract class LinkBlock {
    public static String CSS_CLASS_PREFIX = "summary-links-";
    /**
     * The translation key for the name of this section.
     */
    public final String nameKey;
    public LinkBlock(String nameKey) {
        this.nameKey = nameKey;
    }

    public String getName() throws JDOMException, IOException {
        return nameKey;
    }
    public abstract boolean isEmpty();
    public abstract void linksHtml(StringBuilder xml, Functions functions, Environment env) throws JDOMException, IOException;
}
