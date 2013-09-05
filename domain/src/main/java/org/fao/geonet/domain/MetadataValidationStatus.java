package org.fao.geonet.domain;

/**
 * The enumeration of validation status'
 *
 * @author Jesse
 */
public enum MetadataValidationStatus {
    INVALID, VALID;

    public String getCode() {
        return String.valueOf(ordinal());
    }
}
