package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.domain.UserGroupNamedQueries.DeleteAllByUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data Access object for accessing {@link UserGroup} entities.
 * 
 * @author Jesse
 */
public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId>, JpaSpecificationExecutor<UserGroup> {

    @Modifying
    @Transactional
    @Query(name = DeleteAllByUserId.QUERY)
    void deleteAllByUserId(@Param(DeleteAllByUserId.PARAM_USERID) int userId);
}
