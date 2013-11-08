package jeeves.config.springutil;

import jeeves.server.overrides.ConfigurationOverrides;
import org.jdom.JDOMException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.IOException;

public class JeevesApplicationContext extends XmlWebApplicationContext {
    
    private String appPath;
    private final ConfigurationOverrides _configurationOverrides;
    
    public JeevesApplicationContext() {
        this(ConfigurationOverrides.DEFAULT);
    }

    public JeevesApplicationContext(final ConfigurationOverrides configurationOverrides) {
        this._configurationOverrides = configurationOverrides;
        addApplicationListener(new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                try {
                    if (event instanceof ContextRefreshedEvent) {
                        configurationOverrides.applyNonImportSpringOverides(JeevesApplicationContext.this, getServletContext(), appPath);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    RuntimeException e2 = new RuntimeException();
                    e2.initCause(e);
                    throw e2;
                }
            }
        });
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

	@Override
	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
        reader.setValidating(false);
        super.loadBeanDefinitions(reader);
        try {
            this._configurationOverrides.importSpringConfigurations(reader, (ConfigurableBeanFactory) reader.getBeanFactory(),
                    getServletContext(), appPath);
        } catch (JDOMException e) {
            throw new IOException(e);
        }
    }
}
