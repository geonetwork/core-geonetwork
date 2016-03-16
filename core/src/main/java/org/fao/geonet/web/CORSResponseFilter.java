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

        System.out.println("############# CORS FILTER #############");

        try {
            String clientOriginUrl = httpRequest.getHeader("origin");
            System.out.println("#### origin: " + clientOriginUrl);
            System.out.println("#### host: " + httpRequest.getHeader("host"));
            System.out.println("#### path: " + httpRequest.getServletPath());

            if(clientOriginUrl != null) {
                String clientOriginHost = new java.net.URI(clientOriginUrl).getHost();
                if(allowedRemoteHosts.indexOf(clientOriginHost) != -1) {
                    System.out.println("#### found: " + clientOriginHost);
                    httpResponse.setHeader("Access-Control-Allow-Origin", clientOriginUrl);
                    httpResponse.setHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
                    httpResponse.setHeader("'Access-Control-Allow-Credentials", "'Access-Control-Allow-Credentials: true'");
                    httpResponse.setHeader("Cache-Control", "no-cache");
                    httpResponse.setHeader("Pragma'", "no-cache");
                    httpResponse.setHeader("Vary", "Origin");

                    System.out.println("#### response: Access-Control-Allow-Origin " + httpResponse.getHeader("Access-Control-Allow-Origin"));
                    System.out.println("#### response: Access-Control-Allow-Headers " + httpResponse.getHeader("Access-Control-Allow-Headers"));

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