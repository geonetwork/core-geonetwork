package org.fao.geonet.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * This filter systematically removes any ETag header present in a response sent back to the client.
 * This is useful when the webapp is run by Tomcat, which automatically adds this header
 * in some circumstances.
 */
public class SextantETagFilter implements Filter {
    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        chain.doFilter(request, new HttpServletResponseWrapper(httpResponse) {
            @Override
            public void setHeader(String name, String value) {
                if (!"etag".equalsIgnoreCase(name)) {
                    super.setHeader(name, value);
                }
            }
        });
    }

    @Override
    public void destroy() {
    }
}
