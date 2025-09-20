package org.fao.geonet.kernel.security;

import org.fao.geonet.domain.AnonymousAccessLink;
import org.springframework.security.core.GrantedAuthority;

public final class ViewMdGrantedAuthority implements GrantedAuthority {
	private static final long serialVersionUID = -5004823258126237689L;

	private AnonymousAccessLink anonymousAccessLink;

	public AnonymousAccessLink getAnonymousAccessLink() {
		return anonymousAccessLink;
	}

	public ViewMdGrantedAuthority setAnonymousAccessLink(AnonymousAccessLink anonymousAccessLink) {
		this.anonymousAccessLink = anonymousAccessLink;
		return this;
	}

	@Override
	public String getAuthority() {
		return anonymousAccessLink.getMetadataUuid();
	}
}
