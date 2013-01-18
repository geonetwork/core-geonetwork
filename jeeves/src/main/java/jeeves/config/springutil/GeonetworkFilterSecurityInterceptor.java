package jeeves.config.springutil;

import java.io.IOException;

import javax.servlet.ServletException;

import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

/**
 * This class is used in spring configuration to allow custom applications to add security mappings
 * to the spring configuration without having to edit the main mappings file.  The primary reason for
 * this need is to keep the application specific concerns separated.  This allows an application to 
 * easier merge changes from the core system.  
 * 
 * This class is needed because the {@link FilterSecurityInterceptor} puts an attribute on the
 * request so only 1 {@link FilterSecurityInterceptor} will be consulted per request.  This class
 * is essentially the same but sets a different attribute.
 * 
 * @author jeichar
 *
 */
public class GeonetworkFilterSecurityInterceptor extends FilterSecurityInterceptor {
    private static final String GN_SECURITY_ACCEPTED = "__geonetwork_spring_security_filterSecurityInterceptor_permission_granted";
	private final String appliedToken; 
	public GeonetworkFilterSecurityInterceptor(String appliedToken) {
		this.appliedToken = appliedToken;
	}
    public void invoke(FilterInvocation fi) throws IOException, ServletException {
        if ((fi.getRequest() != null) && (fi.getRequest().getAttribute(appliedToken) != null || fi.getRequest().getAttribute(GN_SECURITY_ACCEPTED) != null)) {
            // filter already applied to this request and user wants us to observe
            // once-per-request handling, so don't re-do security checking
            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
        } else {
            // first time this request being called, so perform security checking
            if (fi.getRequest() != null) {
                fi.getRequest().setAttribute(appliedToken, Boolean.TRUE);
            }

            InterceptorStatusToken token = super.beforeInvocation(fi);
            if (fi.getRequest() != null && token != null) {
            	fi.getRequest().setAttribute(GN_SECURITY_ACCEPTED, Boolean.TRUE);
            }

            fi.getChain().doFilter(fi.getRequest(), fi.getResponse());

            super.afterInvocation(token, null);
        }
    }
}
