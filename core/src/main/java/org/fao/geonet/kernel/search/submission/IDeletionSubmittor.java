package org.fao.geonet.kernel.search.submission;

import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;

public interface IDeletionSubmittor {

    void submitToIndex(String uuid, EsSearchManager searchManager) throws IOException;
}
