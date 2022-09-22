package org.fao.geonet.api.records.model.related;

public enum RelatedItemOrigin {

    /**
     * The metadata is available in the current portal.
     */
    portal,
    /**
     * The metadata is not available in the current portal, but is available in the local catalog.
     */
    catalog,
    /**
     * The metadata is available in a remote resource, used for operatesOn resources.
     */
    remote
}
