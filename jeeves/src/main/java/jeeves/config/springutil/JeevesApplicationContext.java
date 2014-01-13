package jeeves.config.springutil;

import jeeves.server.overrides.ConfigurationOverrides;
import jeeves.server.sources.http.ServletPathFinder;
import org.fao.geonet.Constants;
import org.jdom.JDOMException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;
import java.io.IOException;

public class JeevesApplicationContext extends XmlWebApplicationContext  {

    private final ConfigurationOverrides _configurationOverrides;

    public JeevesApplicationContext(final ConfigurationOverrides configurationOverrides,
                                    ApplicationContext parent, String... configLocations) {
        if (configLocations == null || configLocations.length == 0) {
            throw new IllegalArgumentException("No config locations were specified.  There must be at least one");
        }
        setParent(parent);

        setConfigLocations(configLocations);
        this._configurationOverrides = configurationOverrides;
    }


    @Override
    protected void onRefresh() {
        try {
            final ServletContext servletContext = getServletContext();

            String appPath = getAppPath();
            _configurationOverrides.applyNonImportSpringOverides(JeevesApplicationContext.this, servletContext, appPath);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            RuntimeException e2 = new RuntimeException();
            e2.initCause(e);
            throw e2;
        }
    }

    /**
     * Get the path to the webapplication directory.
     *
     * This method is protected so tests can provide custom implementations.
     */
    protected String getAppPath() {
        final ServletPathFinder pathFinder = new ServletPathFinder(getServletContext());
        return pathFinder.getAppPath();
    }

    @Override
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
        reader.setValidating(false);
        super.loadBeanDefinitions(reader);

        String appPath = getAppPath();
        try {
            this._configurationOverrides.importSpringConfigurations(reader, (ConfigurableBeanFactory) reader.getBeanFactory(),
                    getServletContext(), appPath);
        } catch (JDOMException e) {
            throw new IOException(e);
        }

    }

}
