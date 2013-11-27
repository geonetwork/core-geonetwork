package jeeves.config.springutil;

import com.google.common.base.Function;
import org.fao.geonet.domain.User;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Enumeration;

public class JeevesContextLoaderListener extends ContextLoaderListener {

    private static final String POST_PROCESSOR_INIT_PARAM = "applicationContextPostProcessors";

    @Override
    protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
        processBeanFactoryPostProcessorParam(applicationContext, servletContext.getInitParameter(POST_PROCESSOR_INIT_PARAM));
        String baseURL = servletContext.getContextPath();
        String webappName;
        if (baseURL.length() > 1) {
            webappName = baseURL.substring(1) + ".";
        } else {
            webappName = "";
        }
        String key = webappName + POST_PROCESSOR_INIT_PARAM;
        String param = System.getProperty(key);
        if (param != null) {
            processBeanFactoryPostProcessorParam(applicationContext, param);
        } else {
            key = "geonetwork." + POST_PROCESSOR_INIT_PARAM;
            param = System.getProperty(key);
            processBeanFactoryPostProcessorParam(applicationContext, param);
        }
    }

    private void processBeanFactoryPostProcessorParam(ConfigurableWebApplicationContext applicationContext, String param) {
        if (param != null) {
            for (String className : param.split(",")) {
                if (!className.trim().isEmpty()) {
                    try {
                        Class<?> class1 = Class.forName(className.trim());
                        BeanFactoryPostProcessor postProcessor = (BeanFactoryPostProcessor) class1.newInstance();
                        applicationContext.addBeanFactoryPostProcessor(postProcessor);
                    } catch (Throwable e) {
                        Log.error(Log.JEEVES, "Unable to create Bean Post processor: "+className);
                    }
                }
            }
        }
    }

    @Override
    public void contextInitialized(final ServletContextEvent event) {
        super.contextInitialized(event);
        final ServletContext servletContext = event.getServletContext();
        withEachApplicationContext(servletContext, new Function<ConfigurableApplicationContext, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ConfigurableApplicationContext input) {
                if (input != null) {
                    input.start();
                }
                return null;
            }
        });
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event) {
        final ServletContext servletContext = event.getServletContext();
        withEachApplicationContext(servletContext, new Function<ConfigurableApplicationContext, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ConfigurableApplicationContext input) {
                if (input != null) {
                    input.stop();
                }
                return null;
            }
        });
        super.contextDestroyed(event);
    }

    private void withEachApplicationContext (ServletContext servletContext, Function<ConfigurableApplicationContext, Void> action) {
        final Enumeration attributeNames = servletContext.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = (String) attributeNames.nextElement();
            if (name.startsWith(User.NODE_APPLICATION_CONTEXT_KEY)) {
                ConfigurableApplicationContext context = (ConfigurableApplicationContext) servletContext.getAttribute(name);
                action.apply(context);
            }
        }
    }
}
