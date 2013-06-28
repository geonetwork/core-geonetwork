package org.fao.geonet.repository;

import org.fao.geonet.domain.User;

public interface UserRepositoryCustom {
    User findOne(String userId);
}
