package org.fao.geonet.services.metadata.format.groovy.util;

import org.fao.geonet.services.metadata.format.groovy.Environment;
import org.fao.geonet.services.metadata.format.groovy.Functions;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * A Link Block that is free form and has no markup defined but can be anything that the formatter needs.
 *
 * @author Jesse on 11/21/2014.
 */
public class RawHtmlLinkBlock extends LinkBlock {
    private final String html;

    public RawHtmlLinkBlock(String nameKey, String html) {
        super(nameKey);
        this.html = html;
    }

    @Override
    public boolean isEmpty() {
        return html.isEmpty();
    }

    @Override
    public void linksHtml(StringBuilder xml, Functions functions, Environment env) throws JDOMException, IOException {
        xml.append(html);
    }

}
