package org.fao.geonet.repository;

import java.util.Optional;

public interface AnonymousAccessLinkRepositoryCustom {

	Optional<Integer> getAuthorities(String hash);

}
