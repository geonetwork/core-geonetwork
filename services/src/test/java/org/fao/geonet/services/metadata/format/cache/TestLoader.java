package org.fao.geonet.services.metadata.format.cache;

import java.util.concurrent.Callable;

/**
* @author Jesse on 3/6/2015.
*/
public class TestLoader implements Callable<StoreInfoAndDataLoadResult> {
    private final String resultToStore;
    private final long changeDate;
    private final boolean published;

    public TestLoader(String resultToStore, long changeDate, boolean published) {
        this.resultToStore = resultToStore;
        this.changeDate = changeDate;
        this.published = published;
    }

    @Override
    public StoreInfoAndDataLoadResult call() throws Exception {
        return new StoreInfoAndDataLoadResult(resultToStore, changeDate, published, null, null);
    }
}
