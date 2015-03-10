package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.List;

/**
 * Parses xml attributes and elements and create TNode if applicable.
 *
 * @author Jesse on 11/29/2014.
 */
public abstract class TNodeFactory {
    public static final String FMT_PREFIX = "fmt-";
    public static final List<String> ATTRIBUTE_NAME_PREFIXES = Lists.newArrayList(FMT_PREFIX, "x-" + FMT_PREFIX, "data-" + FMT_PREFIX);

    @Autowired
    protected TextContentParser textContentParser;
    /**
     * Test if this node factory can process the current node.
     */
    public abstract boolean applicable(String localName, String qName, Attributes attributes);

    /**
     * Create a node from the current XML element.
     */
    public abstract TNode create(String localName, String qName, Attributes attributes) throws IOException;

    protected String getValue(Attributes attributes, String name) {
        for (String attributeNamePrefix : ATTRIBUTE_NAME_PREFIXES) {
            final int index = attributes.getIndex(attributeNamePrefix + name);
            if (index > -1) {
                return attributes.getValue(index);
            }
        }

        return null;
    }

    protected boolean hasAttribute(Attributes attributes, String... attNames) {
        for (String att : attNames) {
            if (getValue(attributes, att) != null) {
                return true;
            }
        }
        return false;
    }

    protected boolean getBooleanAttribute(Attributes attributes, String attName, boolean defaultVal) {
        String replaceAtt = getValue(attributes, attName);
        boolean value = defaultVal;

        if (replaceAtt != null) {
            value = replaceAtt.isEmpty() || replaceAtt.equalsIgnoreCase("y") || replaceAtt.equalsIgnoreCase("true")
                      || replaceAtt.equalsIgnoreCase("yes");
        }

        return value;
    }
}
