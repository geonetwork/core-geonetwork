package org.fao.geonet.utils.nio;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * An Xml Entity Resolver that resolves xml files relative to a java.nio.file.Path object.  This means that
 * the xml can be loaded from any Java NIO filesystem not just the default filesystem.
 *
 * @author Jesse on 11/4/2014.
 */
public class NioPathAwareEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return NioPathHolder.resolveEntity(publicId, systemId);
    }
}
