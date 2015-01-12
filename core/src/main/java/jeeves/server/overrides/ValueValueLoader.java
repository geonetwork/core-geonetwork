package jeeves.server.overrides;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.Properties;

class ValueValueLoader implements ValueLoader {

    private String value;

    public ValueValueLoader(String value) {
        this.value = value;
    }

    @Override
    public Object load(ConfigurableBeanFactory beanFactory, Properties properties) {
        return value;
    }
}