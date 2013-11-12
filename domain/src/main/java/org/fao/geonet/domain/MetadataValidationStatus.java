package org.fao.geonet.domain;

/**
 * The enumeration of validation status'
 *
 * @author Jesse
 */
public enum MetadataValidationStatus {
    INVALID, VALID, NEVER_CALCULATED;

    public String getCode() {
        return String.valueOf(ordinal());
    }
}
