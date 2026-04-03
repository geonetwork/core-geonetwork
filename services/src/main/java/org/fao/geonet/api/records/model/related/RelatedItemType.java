package org.fao.geonet.api.records.model.related;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
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
     * When 2 records share the same parents.
     */
    brothersAndSisters,
    /**
     * Aggregation info in ISO19139, Associated resources in ISO19115-3,
     * isPartOf in Dublin core.
     */
    siblings,
    /**
     * Associated is reverse direction of siblings.
     * Record having agg_associated index field set to the record of interest.
     * The relation does not contain details about association and initiative type.
     */
    associated,
    /**
     * Return all revisions for a record. Revision link is made using
     * associated records in ISO19115-3 with revisionOf codelist value
     * for association type.
     * <pre>
     *    <mri:associatedResource>
     *      <mri:MD_AssociatedResource>
     *         <mri:associationType>
     *            <mri:DS_AssociationTypeCode codeList="http://standards.iso.org/iso/19115/resources/Codelists/cat/codelists.xml#DS_AssociationTypeCode"
     *                                        codeListValue="revisionOf"/>
     *         </mri:associationType>
     *         <mri:metadataReference uuidref="481df889-4f3d-4290-bd89-b7f3ad11a2f1"
     *                                xlink:href="http://localhost:8080/geonetwork/srv/api/records/481df889-4f3d-4290-bd89-b7f3ad11a2f1"/>
     *      </mri:MD_AssociatedResource>
     *   </mri:associatedResource>
     * </pre>
     *
     * In the index document, the field agg_associated_revisionOf
     * is pointing to the previous version of a record.
     */
    versions,
    next,
    previous,
    /**
     * All services having recordOperateOn index field pointing to record of interest.
     */
    services,
    /**
     * When a service metadata record operatesOn a dataset.
     */
    datasets,
    /**
     * Feature catalogue citation reference.
     */
    fcats,
    hasfeaturecats,
    /**
     * Source reference.
     */
    sources,
    hassources,
    @Deprecated
    related,
    @Deprecated
    onlines,
    @Deprecated
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
