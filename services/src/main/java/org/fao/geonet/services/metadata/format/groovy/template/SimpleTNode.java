package org.fao.geonet.services.metadata.format.groovy.template;

import com.google.common.base.Optional;
import org.fao.geonet.SystemInfo;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * @author Jesse on 11/29/2014.
 */
public class SimpleTNode extends TNode {
    public SimpleTNode(SystemInfo info, TextContentParser parser, String qName, Attributes attributes) throws IOException {
        super(info, parser, qName, attributes);
    }

    @Override
    protected Optional<String> canRender(TRenderContext context) {
        return Optional.absent();
    }
}
