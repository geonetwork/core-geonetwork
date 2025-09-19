package org.fao.geonet.repository;

import java.util.Optional;

public class AnonymousAccessLinkRepositoryCustomImpl implements AnonymousAccessLinkRepositoryCustom {

	@Override
	public Optional<Integer> getAuthorities(String hash) {
		return Optional.empty();
	}
}
