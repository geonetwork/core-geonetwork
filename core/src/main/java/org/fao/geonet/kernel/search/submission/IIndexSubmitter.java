package org.fao.geonet.kernel.search.submission;

import org.fao.geonet.kernel.search.EsSearchManager;

import java.io.IOException;

public interface IIndexSubmitter {

    void submitToIndex(String id, String jsonDocument, EsSearchManager searchManager) throws IOException;
}
