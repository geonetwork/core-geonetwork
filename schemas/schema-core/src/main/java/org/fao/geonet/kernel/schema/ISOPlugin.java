package org.fao.geonet.kernel.schema;

import org.jdom.Element;

/**
 * Created by francois on 31/01/15.
 */
public interface ISOPlugin {
    /**
     * Return the name (with namespace prefix)
     * for the basic default type.
     *
     * @return
     */
    String getBasicTypeCharacterStringName();

    /**
     * Return an element to be use as default
     * when creating new elements.
     *
     * @return
     */
    Element createBasicTypeCharacterString();
}
