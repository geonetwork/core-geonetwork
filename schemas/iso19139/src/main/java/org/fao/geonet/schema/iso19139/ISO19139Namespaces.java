package org.fao.geonet.schema.iso19139;

import org.jdom.Namespace;

/**
 * Namespaces for iso19139 metadata standard.
 * <p/>
 * Created by francois on 3/26/14.
 */
public class ISO19139Namespaces {
    public static final Namespace GCO =
            Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    public static final Namespace SRV =
            Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
    public static final Namespace GMD =
            Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    public static final Namespace GML =
            Namespace.getNamespace("gml", "http://www.opengis.net/gml");
    public static final Namespace XSI =
            Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final Namespace GTS =
            Namespace.getNamespace("gts", "http://www.isotc211.org/2005/gts");
    public static final Namespace GMX =
            Namespace.getNamespace("gmx", "http://www.isotc211.org/2005/gmx");
    public static final Namespace XLINK =
            Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
}
