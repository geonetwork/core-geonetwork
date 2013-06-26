package org.fao.geonet.repository;

import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserGroupRepository extends JpaRepository<UserGroup, UserGroupId>, JpaSpecificationExecutor<UserGroup> {

}
