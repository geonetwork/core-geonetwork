package jeeves.server.overrides;

import java.beans.PropertyDescriptor;
import java.util.Properties;

import jeeves.utils.Xml;

import org.jdom.Element;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;

abstract class PropertyUpdater extends BeanUpdater {

    public static PropertyUpdater create(Element element) {
        PropertyUpdater updater;
        if("set".equalsIgnoreCase(element.getName())) {
            updater = new SetPropertyUpdater();
        } else if ("add".equalsIgnoreCase(element.getName())) {
            updater = new AddPropertyUpdater();
        } else {
            throw new IllegalArgumentException(element.getName()+" is not known type of updater");
        }
        updater.setBeanName(element);
        updater.setPropertyName(element.getAttributeValue("property"));
        ValueLoader valueLoader;
        if(element.getAttributeValue("ref") != null) {
            valueLoader = new RefValueLoader(element.getAttributeValue("ref"));
        }else if(element.getAttributeValue("value") != null) {
            valueLoader = new ValueValueLoader(element.getAttributeValue("value"));
        } else {
            throw new IllegalArgumentException(Xml.getString(element)+" does not have a value associated with it that is recognized. Excepted ref or value attribute");
        }
        updater.setSetValueLoader(valueLoader);
        return updater;
    }

    @Override
    public Object update(ApplicationContext applicationContext, Properties properties, Object bean) {
        Object value = valueLoader.load(applicationContext, properties);
        if (value instanceof String) {
            String string = (String) value;
            value = ConfigurationOverrides.DEFAULT.updatePropertiesInText(properties, string);
        }
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(bean.getClass(), propertyName);
        
        return doUpdate(applicationContext, bean, descriptor, value);
    }
    protected abstract Object doUpdate(ApplicationContext applicationContext, Object bean, PropertyDescriptor descriptor, Object value);
    
    protected ValueLoader valueLoader;
    protected String propertyName;

    private void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
    private void setSetValueLoader(ValueLoader valueLoader) {
        this.valueLoader = valueLoader;   
    }
}