package org.fao.geonet.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSResponseFilter
        implements Filter {

    private FilterConfig config;
    private ServletContext servletContext;
    private List<String> allowedRemoteHosts;

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.servletContext = config.getServletContext();

        String hostExceptions = config.getInitParameter("hostExceptions");
        if(hostExceptions == null) {
            throw new ServletException("The CORSResponseFilter must have a hostExceptions parameter");
        }
        allowedRemoteHosts = Arrays.asList(hostExceptions.split(","));
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String clientOriginUrl = httpRequest.getHeader("origin");
            if(clientOriginUrl != null) {
                String clientOriginHost = new java.net.URI(clientOriginUrl).getHost();
                if(allowedRemoteHosts.indexOf(clientOriginHost) != -1) {
                    httpResponse.setHeader("Access-Control-Allow-Origin", clientOriginUrl);
                }
            }
        }
        catch (Exception e) {
            throw new ServletException("An error occurs while getting the host of the origin header of the incoming request");
        }

        chain.doFilter(request, httpResponse);
    }

    public synchronized void destroy() {}

}