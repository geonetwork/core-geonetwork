package jeeves.server.overrides;

import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.util.Collection;

class AddPropertyUpdater extends PropertyUpdater {

    @SuppressWarnings("unchecked")
    @Override
    protected Object doUpdate(ApplicationContext applicationContext, Object bean, PropertyDescriptor descriptor, Object value) {
        Log.debug(Log.JEEVES, "Adding new value "+value+" to property: "+propertyName+" on "+beanName);
        Object originalValue = ReflectionUtils.invokeMethod(descriptor.getReadMethod(), bean);
        if (originalValue instanceof Collection) {
            Collection<Object> coll = (Collection<Object>) originalValue;
            coll.add(value);
        } else {
            throw new IllegalArgumentException(originalValue+" is not a collection as expected");
        }
        return null;
    }
    
}