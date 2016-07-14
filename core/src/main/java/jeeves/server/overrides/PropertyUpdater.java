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

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Properties;

abstract class PropertyUpdater extends BeanUpdater {

    protected ValueLoader valueLoader;
    protected String propertyName;

    public static PropertyUpdater create(Element element) {
        PropertyUpdater updater;
        if ("set".equalsIgnoreCase(element.getName())) {
            updater = new SetPropertyUpdater();
        } else if ("add".equalsIgnoreCase(element.getName())) {
            updater = new AddPropertyUpdater();
        } else {
            throw new IllegalArgumentException(element.getName() + " is not known type of updater");
        }
        updater.setBeanName(element);
        updater.setPropertyName(element.getAttributeValue("property"));
        ValueLoader valueLoader;
        if (element.getAttributeValue("ref") != null) {
            valueLoader = new RefValueLoader(element.getAttributeValue("ref"));
        } else if (element.getAttributeValue("value") != null) {
            valueLoader = new ValueValueLoader(element.getAttributeValue("value"));
        } else {
            throw new IllegalArgumentException(Xml.getString(element) + " does not have a value associated with it that is recognized. Excepted ref or value attribute");
        }
        updater.setSetValueLoader(valueLoader);
        return updater;
    }

    @Override
    public void update(ConfigurableListableBeanFactory beanFactory, Properties properties, BeanDefinition bean) {
        Object value = valueLoader.load(beanFactory, properties);
        if (value instanceof String) {
            String string = (String) value;
            value = ConfigurationOverrides.DEFAULT.updatePropertiesInText(properties, string);
        }
        doUpdate(beanFactory, bean, value);
    }

    protected abstract void doUpdate(ConfigurableListableBeanFactory beanFactory, BeanDefinition bean, Object value);

    private void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    private void setSetValueLoader(ValueLoader valueLoader) {
        this.valueLoader = valueLoader;
    }
}
