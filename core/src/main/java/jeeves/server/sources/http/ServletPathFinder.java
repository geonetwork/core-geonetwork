package jeeves.server.sources.http;

import org.fao.geonet.utils.IO;

import java.nio.file.Path;
import javax.servlet.ServletContext;

/**
 * Look up all the paths that the ServletContext is needed to find.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 10:10 AM
 */
public class ServletPathFinder {
    private final String _baseUrl;
    private final Path _configPath;
    private final Path _appPath;

    /**
     * Constructor.
     *
     * @param servletContext a servletContext for the current webapp.
     */
    public ServletPathFinder(ServletContext servletContext) {
        Path appPath = IO.toPath(servletContext.getRealPath("/xsl")).getParent();

        String baseUrl = "";

        try {
            // 2.5 servlet spec or later (eg. tomcat 6 and later)
            baseUrl = servletContext.getContextPath();
        } catch (java.lang.NoSuchMethodError ex) {
            // 2.4 or earlier servlet spec (eg. tomcat 5.5)
            try {
                String resource = servletContext.getResource("/").getPath();
                baseUrl = resource.substring(resource.indexOf('/', 1), resource.length() - 1);
            } catch (java.net.MalformedURLException e) { // unlikely
                baseUrl = servletContext.getServletContextName();
            }
        }

        String configPathString = servletContext.getRealPath("/WEB-INF/config.xml");
        if (configPathString == null) {
            this._configPath = appPath.resolve("WEB-INF");
        } else {
            this._configPath = IO.toPath(configPathString).getParent();
        }

        this._baseUrl = baseUrl;
        this._appPath = appPath;
    }

    public String getBaseUrl() {
        return _baseUrl;
    }

    public Path getConfigPath() {
        return _configPath;
    }

    public Path getAppPath() {
        return _appPath;
    }
}
