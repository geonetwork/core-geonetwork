package org.fao.geonet.kernel.search.submission;

import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;

public interface IDeletionSubmitter {

    void submitUUIDToIndex(String uuid, EsSearchManager searchManager) throws IOException;

    void submitQueryToIndex(String query, EsSearchManager searchManager) throws IOException;
}
