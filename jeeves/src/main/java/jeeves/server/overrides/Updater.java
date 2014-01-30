package jeeves.server.overrides;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

interface Updater {
    public void update(ConfigurableListableBeanFactory beanFactory, Properties properties);
    public boolean runOnFinish();
}