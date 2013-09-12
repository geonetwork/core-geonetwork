package org.fao.geonet.domain;

/**
 * The type of metadata.
 * <p/>
 * User: Jesse
 * Date: 9/12/13
 * Time: 9:21 AM
 */
public enum MetadataType {
    METADATA('n'), TEMPLATE('y'), SUB_TEMPLATE('s');

    public final char code;

    private MetadataType(char code) {
        this.code = code;
    }

    public static MetadataType lookup(char code) {
        for (MetadataType type : values()) {
            if (type.code == code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Not a known MetadataType code: " + code);
    }
}
