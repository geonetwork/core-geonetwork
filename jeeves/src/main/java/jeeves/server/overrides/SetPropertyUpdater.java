package jeeves.server.overrides;

import java.beans.PropertyDescriptor;

import jeeves.utils.Log;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

class SetPropertyUpdater extends PropertyUpdater {

    @Override
    protected Object doUpdate(ApplicationContext applicationContext, Object bean, PropertyDescriptor descriptor, Object value) {
        Log.debug(Log.JEEVES, "Setting "+propertyName+" on "+beanName+" with new value: "+value);
        ReflectionUtils.invokeMethod(descriptor.getWriteMethod(), bean, value);
        return null;
    }
    
}