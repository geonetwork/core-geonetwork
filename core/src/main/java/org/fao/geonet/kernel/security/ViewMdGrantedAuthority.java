package org.fao.geonet.kernel.security;

import org.springframework.security.core.GrantedAuthority;

public final class ViewMdGrantedAuthority implements GrantedAuthority {
	private static final long serialVersionUID = -5004823258126237689L;

	private String mdId;

	@Override
	public String getAuthority() {
		return mdId;
	}

	public ViewMdGrantedAuthority setMdId(String mdId) {
		this.mdId = mdId;
		return this;
	}
}
