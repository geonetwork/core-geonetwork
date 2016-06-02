//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.spring.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

public class BeanDefinitionParserUtils {

    public static void addPropertyValueUsingFind(BeanDefinitionBuilder bean,
                                                 Element element, String propertyName, Class<?> enumClass) {
        addPropertyValueUsingMethod(bean, element, propertyName, enumClass, "find");
    }

    public static void addPropertyValueUsingValueOf(BeanDefinitionBuilder bean,
                                                    Element element, String propertyName, Class<?> enumClass) {
        addPropertyValueUsingMethod(bean, element, propertyName, enumClass, "valueOf");
    }

    public static void addOptionalPropertyValue(Element element,
                                                BeanDefinitionBuilder bean, String propertyName) {
        String value = element.getAttribute(propertyName);

        if (!value.isEmpty()) {
            bean.addPropertyValue(propertyName, value);
        }
    }

    private static void addPropertyValueUsingMethod(BeanDefinitionBuilder bean,
                                                    Element element, String propertyName, Class<?> enumClass,
                                                    String methodName) {
        String enumValue = element.getAttribute(propertyName);

        if (!enumValue.isEmpty()) {
            BeanDefinitionBuilder enumBean = BeanDefinitionBuilder.rootBeanDefinition(enumClass, methodName);
            enumBean.addConstructorArgValue(enumValue);
            bean.addPropertyValue(propertyName, enumBean.getBeanDefinition());
        }
    }

}
