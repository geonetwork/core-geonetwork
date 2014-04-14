package org.fao.geonet.domain;

import org.jdom.Element;

/**
 * indicates the method is to be used to embed the current object into xml.
 *
 * Created by Jesse on 12/6/13.
 */
public interface XmlEmbeddable {

    /**
     * Add this object to Xml.
     */
    public void addToXml(Element element);
}
