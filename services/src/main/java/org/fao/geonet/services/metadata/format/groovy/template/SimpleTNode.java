package org.fao.geonet.services.metadata.format.groovy.template;

import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * @author Jesse on 11/29/2014.
 */
public class SimpleTNode extends TNode {
    public SimpleTNode(String qName, Attributes attributes) throws IOException {
        super(qName, attributes);
    }

    @Override
    protected boolean canRender(TRenderContext context) {
        return true;
    }
}
