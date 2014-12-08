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

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SystemInfo info;

    public TNodeFactoryIf(SystemInfo info) {
        super(IF);
        this.info = info;
    }
    public TNodeFactoryIf() {
        super(IF);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, IF);
        final FilteredAttributes filteredAttributes = new FilteredAttributes(attributes, IF);
        return new TNodeIf(info, qName, filteredAttributes, value);
    }

}
