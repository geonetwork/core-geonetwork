package org.fao.geonet.domain;

/**
 * Contains constants describing the named queries related to OperationAllowed entity
 * 
 * @author Jesse
 */
public interface OperationAllowedNamedQueries {
    /**
     * The name of the groupId parameter
     */
    String PARAM_GROUP_ID = "groupId";
    /**
     * The name of the metadataId parameter
     */
    String PARAM_METADATA_ID = "metadataId";
    /**
     * A property path to the identifying the groupId property of OperationAllowed.  This is useful for sorting
     * and other queries. 
     */
    String PATH_GROUP_ID = OperationAllowed_.id.getName() + "." + OperationAllowedId_.groupId.getName();
    /**
     * A property path to the identifying the metadataId property of OperationAllowed.  This is useful for sorting
     * and other queries. 
     */
    String PATH_METADATA_ID = OperationAllowed_.id.getName() + "." + OperationAllowedId_.metadataId.getName();
    /**
     * A property path to the identifying the operationId property of OperationAllowed.  This is useful for sorting
     * and other queries. 
     */
    String PATH_OPERATION_ID = OperationAllowed_.id.getName() + "." + OperationAllowedId_.operationId.getName();

    /**
     * Constants defining a Stored Query and its parameters.
     *
     * @author Jesse
     */
    interface DeleteAllByMetadataIdExceptGroupId {
        /**
         * The name of the stored query.
         */
        String NAME = "deleteAllByMetadataIdExceptGroupId";
        /**
         * The groupId named parameter 
         */
        String PARAM_GROUP_ID = OperationAllowedNamedQueries.PARAM_GROUP_ID;
        /**
         * The metadataId named parameter 
         */
        String PARAM_METADATA_ID = OperationAllowedNamedQueries.PARAM_METADATA_ID;
        /**
         * The definition of the query.
         */
        String QUERY = "DELETE FROM OperationAllowed oa WHERE oa.id.groupId != :" + PARAM_GROUP_ID + " AND oa.id.metadataId = :"
                + PARAM_METADATA_ID;
    }

    /**
     * Constants defining a Stored Query and its parameters.
     *
     * @author Jesse
     */
    interface DeleteByMetadataId {
        /**
         * The name of the stored query.
         */
        String NAME = "DeleteByMetadataId";
        /**
         * The metadataId named parameter 
         */
        String PARAM_METADATA_ID = OperationAllowedNamedQueries.PARAM_METADATA_ID;
        /**
         * The definition of the query.
         */
        String QUERY = "DELETE FROM OperationAllowed oa WHERE oa.id.metadataId = :" + PARAM_METADATA_ID;
    }
}
