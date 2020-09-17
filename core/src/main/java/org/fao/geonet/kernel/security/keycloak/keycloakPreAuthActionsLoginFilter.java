package org.fao.geonet.kernel.security.keycloak;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.Log;
import org.keycloak.adapters.spi.UserSessionManagement;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

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

    private static String getSigninPath() {
        if (signinPath == null) {
            try {
                LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint = ApplicationContextHolder.get().getBean(LoginUrlAuthenticationEntryPoint.class);
                String signinPath = loginUrlAuthenticationEntryPoint.getLoginFormUrl().split("\\?")[0];
            } catch(BeansException e) {
                // If we cannot find the bean then we will just use a default.
            }
            // If signinPath is null then something may have gone wrong.
            // This should generally not happen - if it does then lets set to what it currently expected and then log a warning.
            if (StringUtils.isEmpty(signinPath)) {
                signinPath = "/signin";
                Log.warning(Log.JEEVES,
                        "Could not detect signin path from configuration. Using /signin");
            }
        }
        return signinPath;
    }

    @Autowired
    LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    public keycloakPreAuthActionsLoginFilter() {
        super();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        if (servletRequest.getPathInfo() != null &&
                !(servletRequest.getContextPath() + getSigninPath()).equals(servletRequest.getRequestURI())  &&
                !isAuthenticated() ) {

            String encodedRedirectURL = ((HttpServletResponse) response).encodeRedirectURL(
                    servletRequest.getContextPath() + getSigninPath() + "?redirectUrl=" + URLEncoder.encode(servletRequest.getRequestURL().toString(), Constants.ENCODING));

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
