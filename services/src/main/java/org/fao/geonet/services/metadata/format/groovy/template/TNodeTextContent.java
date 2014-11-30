package org.fao.geonet.services.metadata.format.groovy.template;

import java.io.IOException;

/**
 * @author Jesse on 11/30/2014.
 */
public class TNodeTextContent extends TNode {
    private final TextBlock textContent;

    public TNodeTextContent(TextBlock textContent) throws IOException {
        super("", EMPTY_ATTRIBUTES);
        this.textContent = textContent;
    }

    @Override
    public void render(TRenderContext context) throws IOException {
        textContent.render(context);
    }

    @Override
    protected boolean canRender(TRenderContext context) {
        return true;
    }
}
