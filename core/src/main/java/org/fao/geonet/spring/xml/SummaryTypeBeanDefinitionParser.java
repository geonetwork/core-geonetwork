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

import org.fao.geonet.kernel.search.facet.Format;
import org.fao.geonet.kernel.search.facet.SummaryType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

import static org.fao.geonet.spring.xml.BeanDefinitionParserUtils.addPropertyValueUsingValueOf;

public class SummaryTypeBeanDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        return parseSummaryTypesElement(element, parserContext);
    }

    private AbstractBeanDefinition parseSummaryTypesElement(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(SummaryType.class);
        factory.addConstructorArgValue(element.getAttribute("name"));
        addPropertyValueUsingValueOf(factory, element, "format", Format.class);

        List<Element> childElements = DomUtils.getChildElementsByTagName(element, "item");

        if (childElements != null && childElements.size() > 0) {
            parseChildItems(childElements, parserContext, factory);
        }

        return factory.getBeanDefinition();
    }

    private void parseChildItems(List<Element> childElements, ParserContext parserContext, BeanDefinitionBuilder factory) {
        ManagedList<BeanDefinition> children = new ManagedList<BeanDefinition>(childElements.size());

        ParserContext nestedContext = new ParserContext(
            parserContext.getReaderContext(),
            parserContext.getDelegate(),
            factory.getBeanDefinition()
        );

        ItemBeanDefinitionParser facetParser = new ItemBeanDefinitionParser();

        for (Element element : childElements) {
            children.add(facetParser.parse(element, nestedContext));
        }

        factory.addConstructorArgValue(children);
    }

}
