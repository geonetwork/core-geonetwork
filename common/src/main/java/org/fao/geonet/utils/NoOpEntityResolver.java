package org.fao.geonet.utils;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.StringReader;

public class NoOpEntityResolver implements EntityResolver {
    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
    }
}