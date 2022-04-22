package org.fao.geonet.api.records.model.related;


public enum RelatedItemType {

    /**
     * Children are records having a parentUuid index field
     * pointing to the parent record.
     */
    children,
    /**
     * Parent are records returned by {@link org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin#getAssociatedParentUUIDs}.
     * At least parentIdentifier in ISO19139 or parentMetadata in ISO19115-3
     * but also associatedResources having a specific association type
     * (usually partOfSeamlessDatabase)
     */
    parent,
    /**
     * When 2 records share the same parents, they are siblings.
     */
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
