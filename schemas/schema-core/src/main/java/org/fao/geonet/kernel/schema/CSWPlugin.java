package org.fao.geonet.kernel.schema;

import org.jdom.Element;
import org.jdom.Namespace;

import java.util.Map;

public interface CSWPlugin {
    /**
     * Return the list of typenames and corresponding
     * namespace for the plugin.
     *
     * @return
     */
    Map<String, Namespace> getCswTypeNames();
}
