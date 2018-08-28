package org.fao.geonet.web;

import org.jasig.cas.client.util.CommonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CasAuthenticationRedirectEntryPoint extends org.springframework.security.cas.web.CasAuthenticationEntryPoint {
    @Override
    protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
        String service = CommonUtils.safeGetParameter(request, "service");
        if (service != null) {
            return service;
        }
        return request.getRequestURL().toString();
    }

}
