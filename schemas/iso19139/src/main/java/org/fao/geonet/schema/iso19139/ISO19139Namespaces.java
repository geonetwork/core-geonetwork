package org.fao.geonet.schema.iso19139;

import org.jdom.Namespace;

/**
 * Namespaces for iso19139 metadata standard.
 *
 * Created by francois on 3/26/14.
 */
public class ISO19139Namespaces {
    public static final Namespace GCO =
            Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    public static final Namespace SRV =
            Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
    public static final Namespace GMD =
            Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
}
