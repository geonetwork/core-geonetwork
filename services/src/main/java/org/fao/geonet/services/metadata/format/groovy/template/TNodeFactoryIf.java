package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.SystemInfo;
import org.springframework.beans.factory.annotation.Autowired;
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

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SystemInfo info;

    public TNodeFactoryIf(SystemInfo info, TextContentParser contentParser) {
        super(IF);
        this.info = info;
        this.textContentParser = contentParser;
    }

    public TNodeFactoryIf() {
        super(IF);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, IF);
        final boolean onlyChildren = getBooleanAttribute(attributes, ONLY_CHILDREN, false);

        final AttributesFiltered attributesFiltered = new AttributesFiltered(attributes, IF, ONLY_CHILDREN);
        return new TNodeIf(info, textContentParser, qName, attributesFiltered, value, onlyChildren);
    }

}
