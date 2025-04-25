package org.fao.geonet.kernel.security.openidconnect;

import org.fao.geonet.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class GeonetworkOidcPreAuthActionsLoginFilter  implements Filter {

    @Autowired
    private  ClientRegistrationRepository clientRegistrationRepository;



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        String requestUri = servletRequest.getRequestURI();
        String contextPath = servletRequest.getContextPath();
        String clientRegistrationId = GeonetworkClientRegistrationProvider.CLIENTREGISTRATION_NAME;
        // Grab the first (or default) registrationId from repository
        String registrationId = clientRegistrationRepository.findByRegistrationId(clientRegistrationId).getRegistrationId();
        String loginPath = contextPath + OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI + "/" + registrationId;

        // Avoid infinite loop and skip API or OIDC system endpoints or bearer token access
        boolean isLoginRequest = requestUri.equals(loginPath);
        boolean isBearerTokenAccess = servletRequest.getHeader("Authorization") != null &&
            servletRequest.getHeader("Authorization").startsWith("Bearer ");
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);

        boolean isPublicEndpoint =
            requestUri.endsWith("/.well-known/jwks.json");

        if (!isAuthenticated && !isLoginRequest && !isPublicEndpoint && !isBearerTokenAccess) {
            String returningUrl = requestUri +
                (servletRequest.getQueryString() == null ? "" : "?" + servletRequest.getQueryString());

            String redirectUrl = loginPath + "?redirectUrl=" + URLEncoder.encode(returningUrl, Constants.ENCODING);
            servletResponse.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }



}
