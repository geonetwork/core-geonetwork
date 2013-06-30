package org.fao.geonet.repository;

import static org.fao.geonet.domain.OperationAllowedNamedQueries.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.OperationAllowedNamedQueries;
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
    @Nonnull
    List<OperationAllowed> findByGroupIdAndMetadataId(int groupId, int metadataId);

    @Nonnull
    List<OperationAllowed> findByMetadataId(int metadataId);

    @Nonnull
    List<OperationAllowed> findByGroupId(int groupId);

    @Nonnull
    List<OperationAllowed> findByOperationId(int operationId);

    @Nullable
    OperationAllowed findByGroupIdAndMetadataIdAndOperationId(int groupId, int metadataId, int operationId);

    @Modifying
    @Query(name=OperationAllowedNamedQueries.DeleteAllByMetadataIdExceptGroupId.NAME)
    void deleteAllByMetadataIdExceptGroupId(@Param(DeleteAllByMetadataIdExceptGroupId.PARAM_METADATA_ID) int metadataId,
            @Param(DeleteAllByMetadataIdExceptGroupId.PARAM_GROUP_ID) int groupId);

    @Modifying
    @Query(name = OperationAllowedNamedQueries.DeleteByMetadataId.NAME)
    void deleteAllByMetadataId(@Param(DeleteByMetadataId.PARAM_METADATA_ID) int metadataId);
}
