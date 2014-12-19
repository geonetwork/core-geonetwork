package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.base.Optional;
import org.fao.geonet.SystemInfo;

import java.io.IOException;

/**
 * @author Jesse on 11/30/2014.
 */
public class TNodeTextContent extends TNode {
    private final TextBlock textContent;

    public TNodeTextContent(SystemInfo info, TextContentParser parser, TextBlock textContent) throws IOException {
        super(info, parser, "", EMPTY_ATTRIBUTES);
        this.textContent = textContent;
    }

    @Override
    public void render(TRenderContext context) throws IOException {
        textContent.render(context);
    }

    @Override
    protected Optional<String> canRender(TRenderContext context) {
        return Optional.absent();
    }
}
