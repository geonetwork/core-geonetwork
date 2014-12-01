package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;

/**
 * Text Block.
 * @author Jesse on 11/29/2014.
 */
public class TextBlock {
    private final List<TextContent> content = Lists.newArrayList();

    public TextBlock add(TextContent content) {
        this.content.add(content);
        return this;
    }

    public void render(TRenderContext context) throws IOException {
        for (TextContent textContent : this.content) {
            context.append(textContent.text(context));
        }
    }

    @Override
    public String toString() {
        return Joiner.on(' ').join(content);
    }
}
