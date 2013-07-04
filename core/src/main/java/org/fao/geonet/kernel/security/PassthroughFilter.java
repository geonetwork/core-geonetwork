package org.fao.geonet.kernel.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.filter.GenericFilterBean;

/**
 * A placeholder for a preAuthentication filter that actually does something.  The idea is that a particular configuration could
 * add a configuration file that overrides the "preAuthenticationFilter" bean and provides an implementation that actually does something :)
 * and this class is simply a placeholder for the configuration option.
 * 
 * See {@link org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter}
 * 
 * @author jeichar
 */
public class PassthroughFilter extends GenericFilterBean {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
	}

}
