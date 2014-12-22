package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.escape.Escaper;
import com.google.common.xml.XmlEscapers;
import org.springframework.stereotype.Component;

/**
 * Escape the string for xml text content.
 *
 * @author Jesse on 12/19/2014.
 */
@Component("escapeXmlContent")
public class FilterEscapeXmlContent implements TextContentFilter {
    private static final Escaper escaper = XmlEscapers.xmlContentEscaper();

    @Override
    public String process(TRenderContext context, String rawValue) {
        return escaper.escape(rawValue);
    }
}
