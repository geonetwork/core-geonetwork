package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.OperationAllowedNamedQueries;
import org.fao.geonet.domain.OperationAllowedNamedQueries.DeleteAllByMetadataIdExceptGroupId;
import org.fao.geonet.domain.OperationAllowedNamedQueries.DeleteByMetadataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Data Access object for finding and saving {@link OperationAllowed} entities.
 *
 * @author Jesse
 */
public interface OperationAllowedRepository extends JpaRepository<OperationAllowed, OperationAllowedId>, OperationAllowedRepositoryCustom,
        JpaSpecificationExecutor<OperationAllowed> {
    /**
     * Find all operations allowed entities with the given groupid and metadataid
     *
     * @param groupId the id of the group
     * @param metadataId the id of the metadata
     * 
     * @return all operation allowed entities with the given groupid and metadataid
     */
    @Nonnull
    List<OperationAllowed> findByGroupIdAndMetadataId(int groupId, int metadataId);

    /**
     * Find all operations allowed entities with the given metadataid.
     * @param metadataId the metadata id
     * @return all operation allowed entities with the given metadataid.
     */
    @Nonnull
    List<OperationAllowed> findByMetadataId(int metadataId);

    /**
     * Find all operations allowed entities with the given groupid.
     * @param groupId the group id
     * @return all operation allowed entities with the given groupid.
     */
    @Nonnull
    List<OperationAllowed> findByGroupId(int groupId);

    /**
     * Find all operations allowed entities with the given operationId.
     * @param operationId the operationId
     * @return all operations allowed entities with the given operationId.
     */
    @Nonnull
    List<OperationAllowed> findByOperationId(int operationId);

    /**
     * Find the one OperationAllowed entity by the operation, metadata and group ids (or null if not found).
     * 
     * @param groupId the groupid
     * @param metadataId the metadataid
     * @param operationId the operationid
     * @return the one OperationAllowed entity by the operation, metadata and group ids (or null if not found).
     */
    @Nullable
    OperationAllowed findByGroupIdAndMetadataIdAndOperationId(int groupId, int metadataId, int operationId);

    /**
     * Delete all OperationsAllowed entities with the give metadata and group ids.
     *
     * @param metadataId the metadata id 
     * @param groupId the group id
     */
    @Modifying
    @Query(name=OperationAllowedNamedQueries.DeleteAllByMetadataIdExceptGroupId.NAME)
    void deleteAllByMetadataIdExceptGroupId(@Param(DeleteAllByMetadataIdExceptGroupId.PARAM_METADATA_ID) int metadataId,
            @Param(DeleteAllByMetadataIdExceptGroupId.PARAM_GROUP_ID) int groupId);

    /**
     * Deleta all OperationsAllowed entities with the given metadata id.
     * @param metadataId the metadata id.
     */
    @Modifying
    @Query(name = OperationAllowedNamedQueries.DeleteByMetadataId.NAME)
    void deleteAllByMetadataId(@Param(DeleteByMetadataId.PARAM_METADATA_ID) int metadataId);
}
