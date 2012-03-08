package org.fao.geonet.logos;

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

/**
 * Servlet for serving up logos.  This solves a largely historical issue because
 * logos are hardcoded across the application to be in /images/logos.  However this
 * is often not desirable.  They would be better to be in the datadirectory and thus
 * possibly outside of geonetwork (allowing easier upgrading of geonetwork etc...)
 *
 * User: jeichar
 * Date: 1/17/12
 * Time: 4:03 PM
 */
public class LogoFilter implements Filter {
    private static final int CONTEXT_PATH_PREFIX = "/images/".length();
    private static final int FIVE_DAYS = 60*60*24*5;
    private String dataImagesDir;
    private byte[] defaultImage;
    private FilterConfig config;
    private byte[] favicon;
    private ServletContext servletContext;
    private String appPath;

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if(isGet(request)) {
            synchronized(this) {
                if(dataImagesDir == null) {
                    initFields();
                }
            }
            String servletPath = ((HttpServletRequest) request).getServletPath();

            Log.info(Geonet.LOGOS, "Handling logos request: "+servletPath);
            
            String filename = servletPath.substring(CONTEXT_PATH_PREFIX).replaceAll("/+", "/");
            int extIdx = filename.lastIndexOf('.');
            String ext;
            if(extIdx > 0) {
                ext = filename.substring(extIdx+1);
            } else {
                ext = "gif"; 
            }
            HttpServletResponse httpServletResponse = (HttpServletResponse)response;
            httpServletResponse.setContentType("image/"+ext);
            httpServletResponse.addHeader("Cache-Control", "max-age="+FIVE_DAYS+", public");
            if(filename.equals("logos/favicon.gif")) {
                httpServletResponse.setContentLength(favicon.length);
                
                response.getOutputStream().write(favicon);
            } else {
                byte[] loadImage = Logos.loadImage(dataImagesDir, servletContext, appPath, filename, defaultImage);
                if(loadImage == defaultImage) {
                    Log.warning(Geonet.LOGOS, "Icon not found, default image returned: "+servletPath);
                    httpServletResponse.setContentType("image/gif");
                    httpServletResponse.setHeader("Cache-Control", "no-cache");
                }
                httpServletResponse.setContentLength(loadImage.length);
                response.getOutputStream().write(loadImage);
            }

        }
    }

    private void initFields() throws IOException {
        servletContext = config.getServletContext();
        appPath = servletContext.getContextPath();
        dataImagesDir = Logos.locateDataImagesDir(config.getServletContext(), appPath);
        defaultImage = Logos.loadImage(dataImagesDir, config.getServletContext(), appPath, "logos/dummy.gif", new byte[0]);
        favicon = Logos.loadImage(dataImagesDir, config.getServletContext(), appPath, "logos/favicon.gif", defaultImage);
    }

    private boolean isGet(ServletRequest request) {
        return ((HttpServletRequest) request).getMethod().equalsIgnoreCase("GET");
    }

    public void destroy() {
        servletContext = null;
        appPath = null;
        dataImagesDir = null;
        defaultImage = null;
        favicon = null;
    }
}
