package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Data Access object for accessing {@link UserGroup} entities.
 *
 * @author Jesse
 */
public interface UserGroupRepository extends GeonetRepository<UserGroup, UserGroupId>, JpaSpecificationExecutor<UserGroup>,
        UserGroupRepositoryCustom {

}
