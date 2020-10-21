package org.fao.geonet.kernel.security.keycloak;

import org.fao.geonet.Constants;
import org.keycloak.adapters.spi.UserSessionManagement;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.io.IOException;

public class keycloakPreAuthActionsLoginFilter extends KeycloakPreAuthActionsFilter {
    public static String signinPath = null;

    @Autowired
    LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    public keycloakPreAuthActionsLoginFilter(UserSessionManagement userSessionManagement) {
        super(userSessionManagement);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        if (servletRequest.getPathInfo() != null &&
                !(servletRequest.getContextPath() + KeycloakUtil.getSigninPath()).equals(servletRequest.getRequestURI())  &&
                !(servletRequest.getPathInfo()).equals("/k_logout")  &&
                !isAuthenticated() ) {

            String encodedRedirectURL = ((HttpServletResponse) response).encodeRedirectURL(
                    servletRequest.getContextPath() + KeycloakUtil.getSigninPath() + "?redirectUrl=" + URLEncoder.encode(servletRequest.getRequestURL().toString(), Constants.ENCODING));

            servletResponse.sendRedirect(encodedRedirectURL);

            // No further action required as we are redirecting to new page
            return;
        }

        super.doFilter(servletRequest, servletResponse, chain);
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
