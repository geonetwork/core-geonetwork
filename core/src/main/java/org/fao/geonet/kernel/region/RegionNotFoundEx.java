package org.fao.geonet.kernel.region;

import jeeves.exceptions.NotFoundEx;

public class RegionNotFoundEx extends NotFoundEx {

    public RegionNotFoundEx(String regionId) {
        super("No region with "+regionId+" found", null);
        id="region-not-found";
        code = 404;
    }

    private static final long serialVersionUID = 1L;

}
