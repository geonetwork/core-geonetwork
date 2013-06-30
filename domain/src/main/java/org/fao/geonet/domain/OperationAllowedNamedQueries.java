package org.fao.geonet.domain;

/**
 * Contains constants describing the named queries related to OperationAllowed entity
 * 
 * @author Jesse
 */
public interface OperationAllowedNamedQueries {
    String PARAM_GROUP_ID = "groupId";
    String PARAM_METADATA_ID = "metadataId";
    String PATH_GROUP_ID = OperationAllowed_.id.getName()+"."+OperationAllowedId_.groupId.getName();
    String PATH_METADATA_ID = OperationAllowed_.id.getName()+"."+OperationAllowedId_.metadataId.getName();
    String PATH_OPERATION_ID = OperationAllowed_.id.getName()+"."+OperationAllowedId_.operationId.getName();

    interface DeleteAllByMetadataIdExceptGroupId {
        String NAME = "deleteAllByMetadataIdExceptGroupId";
        String PARAM_GROUP_ID = OperationAllowedNamedQueries.PARAM_GROUP_ID;
        String PARAM_METADATA_ID = OperationAllowedNamedQueries.PARAM_METADATA_ID;
        String QUERY = "DELETE FROM OperationAllowed oa WHERE oa.id.groupId != :" + PARAM_GROUP_ID
                + " AND oa.id.metadataId = :" + PARAM_METADATA_ID;
    }
    
    interface DeleteByMetadataId {
        String NAME = "DeleteByMetadataId";
        String PARAM_METADATA_ID = OperationAllowedNamedQueries.PARAM_METADATA_ID;
        String QUERY = "DELETE FROM OperationAllowed oa WHERE oa.id.metadataId = :" + PARAM_METADATA_ID;
    }
}
