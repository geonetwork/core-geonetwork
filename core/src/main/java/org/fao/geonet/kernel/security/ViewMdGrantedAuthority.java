package org.fao.geonet.kernel.security;

import com.google.common.annotations.VisibleForTesting;
import org.springframework.security.core.GrantedAuthority;

public final class ViewMdGrantedAuthority implements GrantedAuthority {
	private static final long serialVersionUID = -5004823258126237689L;

	private String mdId;

	@Override
	public String getAuthority() {
		return mdId;
	}

	@VisibleForTesting
	public void setMdId(String mdId) {
		this.mdId = mdId;
	}
}
