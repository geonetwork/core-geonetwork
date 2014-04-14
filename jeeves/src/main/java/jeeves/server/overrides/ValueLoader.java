package jeeves.server.overrides;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

interface ValueLoader {
    Object load(ConfigurableBeanFactory beanFactory, Properties properties);
}