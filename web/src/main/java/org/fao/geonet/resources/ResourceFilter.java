package org.fao.geonet.resources;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.Pair;

/**
 * Servlet for serving up resources located in GeoNetwork data directory.  
 * For example, this solves a largely historical issue because
 * logos are hardcoded across the application to be in /images/logos.  However this
 * is often not desirable.  They would be better to be in the data directory and thus
 * possibly outside of geonetwork (allowing easier upgrading of geonetwork etc...)
 *
 * User: jeichar
 * Date: 1/17/12
 * Time: 4:03 PM
 */
public class ResourceFilter implements Filter {
    private static final int CONTEXT_PATH_PREFIX = "/".length();
    private static final int FIVE_DAYS = 60*60*24*5;
    private static final int SIX_HOURS = 60*60*6;
    private String resourcesDir;
    private Pair<byte[], Long> defaultImage;
    private Pair<byte[], Long> favicon;
    private FilterConfig config;
    private ServletContext servletContext;
    private String appPath;

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(isGet(request)) {
            synchronized(this) {
                if(resourcesDir == null) {
                    initFields();
                }
            }
            String servletPath = ((HttpServletRequest) request).getServletPath();

            Log.info(Geonet.RESOURCES, "Handling resource request: " + servletPath);
            
            String filename = servletPath.substring(CONTEXT_PATH_PREFIX).replaceAll("/+", "/");
            int extIdx = filename.lastIndexOf('.');
            String ext;
            if(extIdx > 0) {
                ext = filename.substring(extIdx+1);
            } else {
                ext = "gif"; 
            }
            HttpServletResponse httpServletResponse = (HttpServletResponse)response;
            // TODO : other type of resources html
            httpServletResponse.setContentType("image/"+ext);
            httpServletResponse.addHeader("Cache-Control", "max-age="+SIX_HOURS+", public");
            if(filename.equals("images/logos/favicon.gif")) {
            	synchronized (this) {
            		favicon = Resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/favicon.gif", favicon.one(), favicon.two());
            	}
            	
                httpServletResponse.setContentLength(favicon.one().length);
                httpServletResponse.addHeader("Cache-Control", "max-age="+FIVE_DAYS+", public");
                response.getOutputStream().write(favicon.one());
            } else {
                Pair<byte[], Long> loadResource = Resources.loadResource(resourcesDir, servletContext, appPath, filename, defaultImage.one(), -1);
                if(loadResource.two() == -1) {

                	synchronized (this) {
                        defaultImage = Resources.loadResource(resourcesDir, config.getServletContext(), appPath, "images/logos/dummy.gif", defaultImage.one(), defaultImage.two());
    				}

                	// Return HTTP 404 ? TODO
                    Log.warning(Geonet.RESOURCES, "Resource not found, default resource returned: "+servletPath);
                    httpServletResponse.setContentType("image/gif");
                    httpServletResponse.setHeader("Cache-Control", "no-cache");
                }
                httpServletResponse.setContentLength(loadResource.one().length);
                response.getOutputStream().write(loadResource.one());
            }

        }
    }

	private void initFields() throws IOException {
        servletContext = config.getServletContext();
        appPath = new java.io.File(servletContext.getRealPath(".")).getParent();
        resourcesDir = Resources.locateResourcesDir(config.getServletContext());

        defaultImage = Resources.loadResource(resourcesDir, config.getServletContext(), appPath, "images/logos/dummy.gif", new byte[0], -1);
        favicon = Resources.loadResource(resourcesDir, config.getServletContext(), appPath, "images/logos/favicon.gif", defaultImage.one(), -1);
    }

    private boolean isGet(ServletRequest request) {
        return ((HttpServletRequest) request).getMethod().equalsIgnoreCase("GET");
    }

    public void destroy() {
        servletContext = null;
        appPath = null;
        resourcesDir = null;
        defaultImage = null;
        favicon = null;
    }
}
