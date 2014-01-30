package jeeves.server.overrides;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;

import java.util.Properties;

class RefValueLoader implements ValueLoader {
    private String beanName;

    public RefValueLoader(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public Object load(ConfigurableBeanFactory beanFactory, Properties properties) {
        return new RuntimeBeanReference(beanName);
    }
}