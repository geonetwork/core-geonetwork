package org.fao.geonet.services.region;

import java.util.Collection;

public interface Request {

    Request setLabel(String labelParam);

    Request setCategoryId(String categoryIdParam);

    Request setMaxRecords(int maxRecordsParam);

    Collection<Region> execute();

}
