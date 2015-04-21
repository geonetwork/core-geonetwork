package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.SystemInfo;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates fmt-if nodes.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryIf extends TNodeFactoryByAttName {

    public static final String IF = "if";
    public static final String ONLY_CHILDREN = "only-children";

    public TNodeFactoryIf(SystemInfo info, TextContentParser contentParser) {
        super(IF, info);
        this.textContentParser = contentParser;
    }

    public TNodeFactoryIf() {
        super(IF, null);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, IF);
        final boolean onlyChildren = getBooleanAttribute(attributes, ONLY_CHILDREN, false);

        final AttributesFiltered attributesFiltered = new AttributesFiltered(attributes, IF, ONLY_CHILDREN);
        return new TNodeIf(SystemInfo.getInfo(this.testingInfo), textContentParser, qName, attributesFiltered, value, onlyChildren);
    }
}
