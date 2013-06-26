package org.fao.geonet.domain;

/**
 * Constains constants describing the named queries related to OperationAllowed entity
 * 
 * @author Jesse
 */
public final class OperationAllowedNamedQueries {
    private OperationAllowedNamedQueries() {
        // Not to be instantiated
    }

    private static final String PARAM_GROUP_ID = "groupId";
    private static final String PARAM_METADATA_ID = "metadataId";
    public static final String PATH_GROUP_ID = OperationAllowed_.id.getName()+"."+OperationAllowedId_.groupId.getName();
    public static final String PATH_METADATA_ID = OperationAllowed_.id.getName()+"."+OperationAllowedId_.metadataId.getName();
    public static final String PATH_OPERATION_ID = OperationAllowed_.id.getName()+"."+OperationAllowedId_.operationId.getName();

    public static final class DeleteAllByMetadataIdExceptGroupId {
        public static final String NAME = "deleteAllByMetadataIdExceptGroupId";
        public static final String PARAM_GROUP_ID = OperationAllowedNamedQueries.PARAM_GROUP_ID;
        public static final String PARAM_METADATA_ID = OperationAllowedNamedQueries.PARAM_METADATA_ID;
        public static final String QUERY = "DELETE FROM OperationAllowed oa WHERE oa.id.groupId != :" + PARAM_GROUP_ID
                + " AND oa.id.metadataId = :" + PARAM_METADATA_ID;
    }
    
    public static final class DeleteByMetadataId {
        public static final String NAME = "DeleteByMetadataId";
        public static final String PARAM_METADATA_ID = OperationAllowedNamedQueries.PARAM_METADATA_ID;
        public static final String QUERY = "DELETE FROM OperationAllowed oa WHERE oa.id.metadataId = :" + PARAM_METADATA_ID;
    }
}
