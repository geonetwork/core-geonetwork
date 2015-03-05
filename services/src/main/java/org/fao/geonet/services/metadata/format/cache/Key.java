package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.services.metadata.format.FormatType;

/**
 * A key for storing a value in the cache.
 *
 * @author Jesse on 3/5/2015.
 */
public class Key {
    final int mdId;
    final String lang;
    final FormatType formatType;
    final String formatterId;
    final boolean hideWithheld;
    private volatile int hash;

    public Key(int mdId, String lang, FormatType formatType, String formatterId, boolean hideWithheld) {
        this.mdId = mdId;
        this.lang = lang;
        this.formatType = formatType;
        this.formatterId = formatterId;
        this.hideWithheld = hideWithheld;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (hideWithheld != key.hideWithheld) return false;
        if (mdId != key.mdId) return false;
        if (formatType != key.formatType) return false;
        if (!formatterId.equals(key.formatterId)) return false;
        if (!lang.equals(key.lang)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mdId;
        result = 31 * result + lang.hashCode();
        result = 31 * result + formatType.hashCode();
        result = 31 * result + formatterId.hashCode();
        result = 31 * result + (hideWithheld ? 1 : 0);
        return result;
    }
}
