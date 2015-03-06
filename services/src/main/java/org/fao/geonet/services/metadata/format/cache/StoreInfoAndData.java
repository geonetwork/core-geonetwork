package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.Constants;

/**
 * Encapsulates the information when a formatter is executed.
 * @author Jesse on 3/5/2015.
 */
public class StoreInfoAndData extends StoreInfo {
    public final byte[] data;

    public StoreInfoAndData(String data, long changeDate, boolean published) {
        this(data == null ? null : data.getBytes(Constants.CHARSET), changeDate, published);
    }
    public StoreInfoAndData(byte[] data, long changeDate, boolean published) {
        super(changeDate, published);
        this.data = data;
    }

    public StoreInfoAndData(StoreInfo info, byte[] data) {
        super(info.getChangeDate(), info.isPublished());
        this.data = data;
    }

    public String getDataAsString() {
        return data == null ? null : new String(data, Constants.CHARSET);
    }
}
