package org.fao.geonet.services.metadata.format.groovy.template;

import org.fao.geonet.domain.Pair;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates if/non-empty/etc... nodes.
 *
 * @author Jesse on 11/29/2014.
 */
public class IfNodeFactory extends TNodeFactory {

    public static final String NON_EMPTY = "non-empty";
    public static final String[] ACCEPTED = {NON_EMPTY};

    @Override
    public boolean applicable(String localName, String qName, Attributes attributes) {
        return hasAttribute(attributes, ACCEPTED);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final Pair<String, String> attributeAndValue = getAttributeAndValue(attributes, ACCEPTED);
        final FilteredAttributes filteredAttributes = new FilteredAttributes(attributes, ACCEPTED);
        switch (attributeAndValue.one()) {
            case NON_EMPTY:
                return new NonEmptyNode(qName, filteredAttributes, attributeAndValue.two());
            default:
                throw new IllegalStateException(
                        "applicable must be called before this method to ensure that the element is supported: "
                        + qName + ":" + localName + " " + attributes);
        }
    }
}
