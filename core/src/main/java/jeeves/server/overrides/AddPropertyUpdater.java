/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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
        Log.debug(Log.JEEVES, "Adding new value " + value + " to property: " + propertyName + " on " + beanName);
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
                            throw new IllegalArgumentException(collectionType + " is not a supported type for adding new values");
                        }
                        break;
                    }
                }
                if (propertyValue == null) {
                    throw new IllegalArgumentException("Unable to find the collection type for property: " + propertyName + " on bean " + beanName);
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
            throw new IllegalArgumentException(originalValue + " is not a collection as expected");
        }
    }

}
