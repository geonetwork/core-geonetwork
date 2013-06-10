package org.fao.geonet.repository;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationAllowedRepository extends JpaRepository<OperationAllowed, OperationAllowedId>, OperationAllowedRepositoryCustom {
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
}
