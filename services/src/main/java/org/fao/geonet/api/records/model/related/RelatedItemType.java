package org.fao.geonet.api.records.model.related;


import java.util.Arrays;

public enum RelatedItemType {

    children,
    parent,
    brothersAndSisters,
    siblings,
    associated,
    services,
    datasets,
    fcats,
    hasfeaturecats,
    sources,
    hassources,
    related,
    onlines,
    thumbnails;

    public static RelatedItemType find(String lookupName) {
        for (RelatedItemType value : values()) {
            if (value.name().equals(lookupName)) {
                return value;
            }
        }
        throw new IllegalArgumentException(String.format(
            "Invalid related type value '%s'. Allowed values are: %s",
            lookupName, values()
        ));
    }

    public String value() {
        return name();
    }
}
