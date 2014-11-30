package org.fao.geonet.services.metadata.format.groovy.template;

import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates if/non-empty/etc... nodes.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryRepeat extends TNodeFactoryByAttName {
    private static final Pattern MAP_PATTERN = Pattern.compile("\\s*\\(\\s*(\\S+)\\s*,\\s*(\\S+)\\s*\\)");

    public static final String REPEAT = "repeat";

    protected TNodeFactoryRepeat() {
        super(REPEAT);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        String value = getValue(attributes, REPEAT);
        final String[] parts = value.split("\\s+in\\s+");
        final FilteredAttributes filteredAttributes = new FilteredAttributes(attributes, REPEAT);

        String key = parts[1];
        Objects.requireNonNull(key);
        String contextName = parts[0];
        Objects.requireNonNull(contextName);

        Matcher mapMatcher = MAP_PATTERN.matcher(contextName);
        if (mapMatcher.matches()) {
            return new TNodeRepeatMap(qName, attributes, key, mapMatcher.group(1), mapMatcher.group(2));
        } else {
            return new TNodeRepeatIter(qName, filteredAttributes, key, contextName);
        }
    }
}
