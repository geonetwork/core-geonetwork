package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.escape.Escaper;
import com.google.common.xml.XmlEscapers;
import org.springframework.stereotype.Component;

/**
 * Escape the string for xml attributes.
 *
 * @author Jesse on 12/19/2014.
 */
@Component("escapeXmlAttrs")
public class FilterEscapeXmlAttrs implements TextContentFilter {
    private static final Escaper escaper = XmlEscapers.xmlAttributeEscaper();

    @Override
    public String process(TRenderContext context, String rawValue) {
        return escaper.escape(rawValue);
    }
}
