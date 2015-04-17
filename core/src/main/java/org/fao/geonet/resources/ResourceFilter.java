package org.fao.geonet.resources;

import com.google.common.collect.Maps;
import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.fao.geonet.resources.Resources.loadResource;

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
    private FilterConfig config;
    private ServletContext servletContext;
    private volatile Pair<byte[], Long> defaultImage;
    private Map<String, Pair<byte[], Long>> faviconMap = Maps.newConcurrentMap();

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.servletContext = config.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        new Instance(this.config, request, response, chain).execute();
    }

    public class Instance {
        private final ServletRequest request;
        private final ServletResponse response;

        private final Path resourcesDir;
        private final Path appPath;
        private final ConfigurableApplicationContext applicationContext;
        private final String nodeId;
        private Pair<byte[], Long> favicon;

        public Instance(FilterConfig config, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
            this.applicationContext = JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(servletContext);
            this.appPath = this.applicationContext.getBean(GeonetworkDataDirectory.class).getWebappDir();
            this.resourcesDir = Resources.locateResourcesDir(servletContext, applicationContext);
            if (defaultImage == null) {
                defaultImage = loadResource(resourcesDir, servletContext, appPath, "images/logos/GN3.png", new byte[0], -1);
            }
            this.nodeId = applicationContext.getBean(NodeInfo.class).getId();
            if (!faviconMap.containsKey(nodeId)) {
                final byte[] defaultImageBytes = defaultImage.one();
                faviconMap.put(nodeId, loadResource(resourcesDir, servletContext, appPath, "images/logos/GN3.ico", defaultImageBytes, -1));
            }

            this.favicon = faviconMap.get(nodeId);

            this.request = request;
            this.response = response;
        }

        private boolean isGet(ServletRequest request) {
            return ((HttpServletRequest) request).getMethod().equalsIgnoreCase("GET");
        }

        public void execute() throws IOException {
            if(isGet(request)) {
                String servletPath = ((HttpServletRequest) request).getServletPath();

                Log.info(Geonet.RESOURCES, "Handling resource request: " + servletPath);

                String filename = servletPath.substring(CONTEXT_PATH_PREFIX).replaceAll("/+", "/");
                int extIdx = filename.lastIndexOf('.');
                String ext;
                if (extIdx > 0) {
                    ext = filename.substring(extIdx + 1);
                } else {
                    ext = "png";
                }
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                FileTime lastModified = Resources.getLastModified(resourcesDir, servletContext, appPath, filename);
                if (lastModified != null &&
                    new ServletWebRequest((HttpServletRequest) request, httpServletResponse).checkNotModified(lastModified.toMillis())) {
                    return;
                }

                // TODO : other type of resources html
                httpServletResponse.setContentType("image/" + ext);
                httpServletResponse.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
                if (filename.equals("images/logos/GN3.ico")) {
                    favicon = loadResource(resourcesDir, servletContext, appPath, "images/logos/GN3.ico", favicon.one(), favicon.two());
                    faviconMap.put(nodeId, favicon);

                    httpServletResponse.setContentLength(favicon.one().length);
                    httpServletResponse.addHeader("Cache-Control", "max-age=" + FIVE_DAYS + ", public");
                    response.getOutputStream().write(favicon.one());
                } else {
                    Pair<byte[], Long> loadResource = loadResource(resourcesDir, servletContext, appPath, filename, defaultImage
                            .one(), -1);
                    if (loadResource.two() == -1) {

                        synchronized (this) {
                            defaultImage = loadResource(resourcesDir,
                                    config.getServletContext(), appPath, "images/logos/GN3.ico",
                                    defaultImage.one(), defaultImage.two());
                        }

                        // Return HTTP 404 ? TODO
                        Log.warning(Geonet.RESOURCES, "Resource not found, default resource returned: " + servletPath);
                        httpServletResponse.setContentType("image/png");
                        httpServletResponse.setHeader("Cache-Control", "no-cache");
                    }
                    httpServletResponse.setContentLength(loadResource.one().length);
                    response.getOutputStream().write(loadResource.one());
                }
            }

        }
    }
    public synchronized void destroy() {
        servletContext = null;
        defaultImage = null;
        faviconMap = null;
    }
}
