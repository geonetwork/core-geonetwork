package jeeves.config.springutil;

import java.io.IOException;

import jeeves.server.overrides.ConfigurationOverrides;

import org.jdom.JDOMException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.support.XmlWebApplicationContext;

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
                    configurationOverrides.applyNonImportSpringOverides(JeevesApplicationContext.this, getServletContext(), appPath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
