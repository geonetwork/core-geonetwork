package org.fao.geonet.services.metadata.format.groovy.template;

/**
 * @author Jesse on 11/29/2014.
 */
public class TextContentStatic implements TextContent {
    private final String text;

    public TextContentStatic(String text) {
        this.text = text;
    }

    @Override
    public String text(TRenderContext content) {
        return this.text;
    }

    @Override
    public String toString() {
        return text;
    }
}
