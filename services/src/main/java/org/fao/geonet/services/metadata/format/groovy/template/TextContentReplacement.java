package org.fao.geonet.services.metadata.format.groovy.template;

import java.util.List;

/**
 * A Text Content where the value is looked up in the Model using the key with a fallback to the key.
 *
 * @author Jesse on 11/29/2014.
 */
public class TextContentReplacement implements TextContent {
    private final String key;
    private final List<? extends TextContentFilter> filters;

    public TextContentReplacement(String key, List<? extends TextContentFilter> filters) {
        this.key = key;
        this.filters = filters;
    }

    @Override
    public String text(TRenderContext content) {
        Object value = content.getModelValue(key);
        if (value == null) {
            return key;
        }

        for (TextContentFilter filter : filters) {
            value = filter.process(content, value.toString());
        }

        return value.toString();
    }


    @Override
    public String toString() {
        return "{{" + key + "}}";
    }
}
