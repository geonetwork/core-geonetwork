package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A common super class for objects that are configured from the groovy scripts and that are applied on a by element basis like
 * {@link org.fao.geonet.services.metadata.format.groovy.Sorter} and {@link org.fao.geonet.services.metadata.format.groovy.Handler}.
 *
 * @author Jesse on 10/22/2014.
 */
public abstract class Selectable {
    protected int priority = 0;
    protected String name;
    protected String mode = Mode.DEFAULT;

    public Selectable(int priority) {
        this.priority = priority;
    }

    /**
     * Return true if the sorter should sort the children of the element.
     */
    public abstract boolean select(TransformationContext context, GPathResult result);

    /**
     * A name for this handler.  This is strictly for debugging purposes.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The priority of the handler.  If two (or more) handlers can be applied to a metadata element, the handler with the
     * higher priority will be selected.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{" +
               (name != null ? "name='" + name + "\'," : "")  +
               " priority=" + priority +
               extraToString() +
               '}';
    }

    protected abstract String extraToString();

    public void configure(Map<String, Object> properties) {
        final PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(getClass());

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            final String propertyName = entry.getKey();
            if (propertyName.equalsIgnoreCase(Handlers.HANDLER_SELECT)) {
                // skip
                continue;
            }

            final Object value = entry.getValue();
            PropertyDescriptor propertyDescriptor = findPropertyDescriptor(propertyDescriptors, propertyName, value);
            final Method writeMethod = propertyDescriptor.getWriteMethod();

            try {
                writeMethod.invoke(this, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private PropertyDescriptor findPropertyDescriptor(PropertyDescriptor[] propertyDescriptors, String key, Object value) {
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            if (descriptor.getWriteMethod() != null && descriptor.getName().equalsIgnoreCase(key)) {
                return descriptor;
            }
        }
        throw new IllegalArgumentException("Handler's do not have a configurable property: " + key + " value = " + value);
    }

    public String getName() {
        return name;
    }
}
