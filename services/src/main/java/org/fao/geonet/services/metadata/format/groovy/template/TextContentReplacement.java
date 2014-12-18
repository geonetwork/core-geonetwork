package org.fao.geonet.services.metadata.format.groovy.template;

/**
 * A Text Content where the value is looked up in the Model using the key with a fallback to the key.
 *
 * @author Jesse on 11/29/2014.
 */
public class TextContentReplacement implements TextContent {
    private final String key;

    public TextContentReplacement(String key) {
        this.key = key;
    }

    @Override
    public String text(TRenderContext content) {
        Object value = content.getModelValue(key);
        if (value == null) {
            return escapeSpecialChars(key);
        }

        if (value instanceof String) {
            return escapeSpecialChars((String) value);
        } else {
            return value.toString();
        }
    }

    public String escapeSpecialChars(String value) {
        return value.replace("&", "&amp;");
    }

    @Override
    public String toString() {
        return "{{" + key + "}}";
    }
}
