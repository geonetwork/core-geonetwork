package jeeves.server.overrides;

import org.fao.geonet.utils.Log;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

class SetPropertyUpdater extends PropertyUpdater {

    @Override
    protected void doUpdate(ConfigurableListableBeanFactory beanFactory, BeanDefinition bean, Object value) {
        Log.debug(Log.JEEVES, "Setting "+propertyName+" on "+beanName+" with new value: "+value);
        bean.getPropertyValues().removePropertyValue(propertyName);

        bean.getPropertyValues().add(propertyName, value);
    }
    
}