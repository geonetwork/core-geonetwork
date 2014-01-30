package jeeves.server.overrides;

import org.apache.commons.beanutils.PropertyUtils;
import org.fao.geonet.utils.Log;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.*;

class AddPropertyUpdater extends PropertyUpdater {

    @SuppressWarnings("unchecked")
    @Override
    protected void doUpdate(ConfigurableListableBeanFactory beanFactory, BeanDefinition bean, Object value) {
        Log.debug(Log.JEEVES, "Adding new value "+value+" to property: "+propertyName+" on "+beanName);
        PropertyValue propertyValue = bean.getPropertyValues().getPropertyValue(propertyName);
        if (propertyValue == null) {
            final String beanClassName = bean.getBeanClassName();
            try {
                final Class<?> aClass = Class.forName(beanClassName);
                final PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(aClass);
                for (PropertyDescriptor descriptor : propertyDescriptors) {
                    if (propertyName.equals(descriptor.getName())) {
                        final Class<?> collectionType = descriptor.getWriteMethod().getParameterTypes()[0];
                        if (List.class.isAssignableFrom(collectionType)) {
                            propertyValue = new PropertyValue(propertyName, new ManagedList<Object>());
                        } else if (Set.class.isAssignableFrom(collectionType)) {
                            propertyValue = new PropertyValue(propertyName, new ManagedSet<Object>());
                        } else if (Map.class.isAssignableFrom(collectionType)) {
                            propertyValue = new PropertyValue(propertyName, new ManagedMap<Object, Object>());
                        } else if (Properties.class.isAssignableFrom(collectionType)) {
                            propertyValue = new PropertyValue(propertyName, new ManagedProperties());
                        } else if (Array.class.isAssignableFrom(collectionType)) {
                            throw new IllegalArgumentException("Array collections not currently supported");
                        } else if (Collection.class.isAssignableFrom(collectionType)) {
                            propertyValue = new PropertyValue(propertyName, new ManagedList<Object>());
                        } else {
                            throw new IllegalArgumentException(collectionType+" is not a supported type for adding new values");
                        }
                        break;
                    }
                }
                if (propertyValue == null) {
                    throw new IllegalArgumentException("Unable to find the collection type for property: "+propertyName+" on bean "+beanName);
                }
                bean.getPropertyValues().addPropertyValue(propertyValue);
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }

        Object originalValue = propertyValue.getValue();
        if (originalValue instanceof Collection) {
            Collection<Object> coll = (Collection<Object>) originalValue;
            coll.add(value);
        } else {
            throw new IllegalArgumentException(originalValue+" is not a collection as expected");
        }
    }
    
}