package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates if/non-empty/etc... nodes.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryNonEmpty extends TNodeFactoryByAttName {

    public static final String NON_EMPTY = "non-empty";

    public TNodeFactoryNonEmpty() {
        super(NON_EMPTY);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, NON_EMPTY);
        final FilteredAttributes filteredAttributes = new FilteredAttributes(attributes, NON_EMPTY);
        return new TNodeNonEmpty(qName, filteredAttributes, value);
    }
}
