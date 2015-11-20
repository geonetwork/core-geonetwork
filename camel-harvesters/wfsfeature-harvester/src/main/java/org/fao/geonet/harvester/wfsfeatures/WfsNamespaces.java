package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.builder.xml.Namespaces;

/**
 * Created by fgravin on 10/29/15.
 */
public final class WfsNamespaces {

    public static final Namespaces wfsNamespaces;
    static {
        wfsNamespaces = new Namespaces("wfs", "http://www.opengis.net/wfs");
        wfsNamespaces.add("wfs2", "http://www.opengis.net/wfs/2.0");
        wfsNamespaces.add("gml32", "http://www.opengis.net/gml/3.2");
        wfsNamespaces.add("gml", "http://www.opengis.net/gml");
    }
}
