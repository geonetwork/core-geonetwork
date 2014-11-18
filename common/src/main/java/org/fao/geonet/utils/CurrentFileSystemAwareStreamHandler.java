package org.fao.geonet.utils;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

/**
 * A Stream handler that will first check the current file system in IO to see if it is a url referring to that file system, otherwise
 * it will fail over to the default behaviour.
 *
 * @author Jesse on 11/18/2014.
 */
class CurrentFileSystemAwareStreamHandler implements URLStreamHandlerFactory {
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
//        if (IO.defaultFs != null) {
        return null;
    }
}
