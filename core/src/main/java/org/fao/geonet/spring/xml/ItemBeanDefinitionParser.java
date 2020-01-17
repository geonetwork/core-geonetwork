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

import static org.fao.geonet.spring.xml.BeanDefinitionParserUtils.addOptionalPropertyValue;
import static org.fao.geonet.spring.xml.BeanDefinitionParserUtils.addPropertyValueUsingFind;

import org.fao.geonet.kernel.search.facet.ItemConfig;
import org.fao.geonet.kernel.search.facet.SortBy;
import org.fao.geonet.kernel.search.facet.SortOrder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class ItemBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class<ItemConfig> getBeanClass(Element element) {
        return ItemConfig.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        bean.addConstructorArgReference(element.getAttribute("facet"));
        bean.addConstructorArgReference("translatorFactory");

        addPropertyValueUsingFind(bean, element, "sortBy", SortBy.class);
        addPropertyValueUsingFind(bean, element, "sortOrder", SortOrder.class);

        addOptionalPropertyValue(element, bean, "max");
        addOptionalPropertyValue(element, bean, "depth");
        addOptionalPropertyValue(element, bean, "pageSize");
        addOptionalPropertyValue(element, bean, "translator");
    }

}
