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

import java.util.List;

import org.fao.geonet.kernel.search.facet.SummaryTypes;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class SummaryTypesBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
            ParserContext parserContext) {
        return parseSummaryTypesElement(element, parserContext);
    }

    @Override
    protected String resolveId(Element element,
            AbstractBeanDefinition definition, ParserContext parserContext)
            throws BeanDefinitionStoreException {
        return "summaryTypes";
    }

    private AbstractBeanDefinition parseSummaryTypesElement(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SummaryTypes.class);
        List<Element> childElements = DomUtils.getChildElementsByTagName(element, "summaryType");

        if (childElements != null && childElements.size() > 0) {
            parseChildSummaryTypes(childElements, parserContext, factory);
        }

        return factory.getBeanDefinition();
    }

    private static void parseChildSummaryTypes(List<Element> childElements, ParserContext parserContext, BeanDefinitionBuilder factory) {
        ManagedList<BeanDefinition> children = new ManagedList<BeanDefinition>(childElements.size());

        ParserContext nestedContext = new ParserContext(
            parserContext.getReaderContext(), 
            parserContext.getDelegate(),
            factory.getBeanDefinition()
        );

        SummaryTypeBeanDefinitionParser facetParser = new SummaryTypeBeanDefinitionParser();

        for (Element element : childElements) {
            children.add(facetParser.parse(element, nestedContext));
        }

        factory.addConstructorArgValue(children);
    }

}
