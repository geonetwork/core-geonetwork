package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.Constants;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;

/**
 * Encapsulates the information when a formatter is executed.
 * @author Jesse on 3/5/2015.
 */
public class StoreInfoAndDataLoadResult extends StoreInfoAndData {

    private final Key key;
    private final Callable<StoreInfoAndDataLoadResult> toCache;

    public StoreInfoAndDataLoadResult(String data, long changeDate, boolean published, @Nullable Key key,
                                      @Nullable Callable<StoreInfoAndDataLoadResult> toCache) {
        this(data == null ? null : data.getBytes(Constants.CHARSET), changeDate, published, key, toCache);
    }
    public StoreInfoAndDataLoadResult(byte[] data, long changeDate, boolean published, @Nullable Key key,
                                      @Nullable Callable<StoreInfoAndDataLoadResult> toCache) {
        super(data, changeDate, published);
        this.toCache = toCache;
        this.key = key;
    }

    public @Nullable Callable<StoreInfoAndDataLoadResult> getToCache() {
        return toCache;
    }

    public @Nullable Key getKey() {
        return key;
    }
}
