package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.services.metadata.format.FormatType;

/**
 * A key for storing a value in the cache.
 *
 * @author Jesse on 3/5/2015.
 */
public class Key {
    public final int mdId;
    public final String lang;
    public final FormatType formatType;
    public final String formatterId;
    public final boolean hideWithheld;

    /**
     * Constructor.
     * @param mdId the id of the metadata
     * @param lang the current ui language
     * @param formatType the content type of the output
     * @param formatterId the formatter used to create the output
     * @param hideWithheld if true then elements in the metadata with the attribute gco:nilreason="withheld" are being hidden
     */
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
        result = 31 * result + formatType.ordinal();
        result = 31 * result + formatterId.hashCode();
        result = 31 * result + (hideWithheld ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Key{" +
               "mdId=" + mdId +
               ", lang='" + lang + '\'' +
               ", formatType=" + formatType +
               ", formatterId='" + formatterId + '\'' +
               ", hideWithheld=" + hideWithheld +
               '}';
    }
}
