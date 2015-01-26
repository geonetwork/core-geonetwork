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

import org.fao.geonet.kernel.search.facet.Dimension;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

public class FacetBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<Dimension> getBeanClass(Element element) {
        return Dimension.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        bean.addConstructorArgValue(element.getAttribute("name"));
        bean.addConstructorArgValue(element.getAttribute("indexKey"));
        bean.addConstructorArgValue(element.getAttribute("label"));

        String classifier = element.getAttribute("classifier");

        if (!classifier.isEmpty()) {
            bean.addPropertyReference("classifier", classifier);
        }

        String localized = element.getAttribute("localized");
        if (!localized.isEmpty()) {
            bean.addPropertyValue("localized", localized);
        }

    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

}
