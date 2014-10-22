package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;

/**
 * Encapsulates the data of an element and the data transforming it produced.  This data is used to control the order
 * of the transformed data being added to the document.
 *
 * At the moment SortData only has el, but in the future this maybe changed to have more values.  This class
 * provides the forward compatibility.
 *
 * @author Jesse on 10/20/2014.
 */
public class SortData {
    private GPathResult el;

    public GPathResult getEl() {
        return el;
    }
}
