package jeeves.server.sources.http;

import javax.servlet.ServletContext;
import java.io.File;

/**
 * Look up all the paths that the ServletContext is needed to find.
 *
 * User: Jesse
 * Date: 10/18/13
 * Time: 10:10 AM
 */
public class ServletPathFinder {
    private final String _baseUrl;
    private final String _configPath;
    private final String _appPath;

    /**
     * Constructor.
     *
     * @param servletContext a servletContext for the current webapp.
     */
    public ServletPathFinder(ServletContext servletContext) {
        String appPath = new File(servletContext.getRealPath("/xsl")).getParent() + File.separator;

        if (!appPath.endsWith(File.separator))
            appPath += File.separator;

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

        String configPath = servletContext.getRealPath("/WEB-INF/config.xml");
        if (configPath == null) {
            configPath = appPath + "WEB-INF" + File.separator;
        } else {
            configPath = new File(configPath).getParent() + File.separator;
        }

        this._baseUrl = baseUrl;
        this._configPath = configPath;
        this._appPath = appPath;
    }

    public String getBaseUrl() {
        return _baseUrl;
    }

    public String getConfigPath() {
        return _configPath;
    }

    public String getAppPath() {
        return _appPath;
    }
}
