package org.fao.geonet.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to avoid clickjaking attacks.
 *
 * See https://www.owasp.org/index.php/Clickjacking_Defense_Cheat_Sheet.
 *
 * @author Jose García
 */
public class XFrameOptionsFilter implements Filter {
    private String mode;

    public void init(FilterConfig filterConfig) throws ServletException {
        mode = filterConfig.getInitParameter("mode");

        // Mode: DENY, SAMEORIGIN. Any other value will default to SAMEORIGIN
        if (!mode.equals("DENY") && !mode.equals("SAMEORIGIN")) {
            mode = "DENY";
        }
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.addHeader("X-Frame-Options", this.mode);

        filterChain.doFilter(servletRequest, response);
    }


    public void destroy() {
    }
}
