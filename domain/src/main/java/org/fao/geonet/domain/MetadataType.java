package org.fao.geonet.domain;

import javax.annotation.Nonnull;

/**
 * The type of metadata.
 * <p/>
 * User: Jesse
 * Date: 9/12/13
 * Time: 9:21 AM
 */
public enum MetadataType {
    /**
     * Indicates the associated {@link Metadata} entity is a normal metadata.
     */
    METADATA('n'),
    /**
     * Indicates the associated {@link Metadata} entity is a template metadata.
     */
    TEMPLATE('y'),
    /**
     * Indicates the associated {@link Metadata} entity is a sub-template metadata.
     * <p></p>
     * A sub-template is a metadata fragment that can be inserted into another metadata.  It can also be
     * shared as an xlink in multiple metadata to reduce duplication.
     */
    SUB_TEMPLATE('s');

    /**
     * The code (for backwards compatibility) of the metadatatype.
     */
    public final char code;
    /**
     * Same as {@link #code} expect as a string instead of a char.
     */
    public final String codeString;

    private MetadataType(final char code) {
        this.code = code;
        this.codeString = String.valueOf(code);
    }
    @Nonnull
    public static MetadataType lookup(final char code) {
        for (MetadataType type : values()) {
            if (type.code == code) {
                return type;
            }
        }

        throw new IllegalArgumentException("Not a known MetadataType code: " + code);
    }

    /**
     * Look up the MetadataType from a string.
     * @param code the 1 character long string representing the type.
     *
     * @return the metadata type.
     */
    @Nonnull
    public static MetadataType lookup(@Nonnull final String code) {
        final String trimmedCode = code.trim();
        if (trimmedCode.length() != 1) {
            throw new IllegalArgumentException("Not a known MetadataType code: " + trimmedCode);
        }
        return lookup(trimmedCode.charAt(0));
    }
}
