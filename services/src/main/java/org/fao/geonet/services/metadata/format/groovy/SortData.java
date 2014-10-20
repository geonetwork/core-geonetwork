package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;

/**
 * Encapsulates the data of an element and the data transforming it produced.  This data is used to control the order
 * of the transformed data being added to the document.
 *
 * @author Jesse on 10/20/2014.
 */
public class SortData {
    public final GPathResult el;
    public final String data;

    public SortData(GPathResult el, String data) {
        this.el = el;
        this.data = data;
    }
}
