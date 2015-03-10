//=============================================================================
//===   Copyright (C) 2011 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.subtemplate;
import com.google.common.collect.Sets;

import com.google.common.collect.Lists;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.Util;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.Iterator;
import org.jdom.Namespace;

import java.nio.file.Path;
import java.util.Set;
import java.util.List;

/**
 * Retrieve sub template from metadata table
 * @author francois
 *
 */
public class Get implements Service {

    private static final char SEPARATOR = '~';

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * Execute the service and return the sub template. 
     * 
     * <p>
     * Sub template are all public - no privileges check. Parameter "uuid" is mandatory.
     * </p>
     * 
     * <p>
     * One or more "process" parameters could be added in order to alter the template extracted.
     * This parameter is composed of one XPath expression pointing to a single {@link org.jdom.Element} or {@link org.jdom.Attribute}
     * and a text value separated by "{@value #SEPARATOR}". Warning, when pointing to an element, the content
     * of the element is removed before the value added (See {@link org.jdom.Element#setText(String)}).
     * </p>
     * 
     * <p>
     * For example, to return a contact template with a custom role use 
     * "&process=gmd:role/gmd:CI_RoleCode/@codeListValue~updatedRole".
     * </p>
     * 
     */
    public Element exec(Element params, ServiceContext context)
            throws Exception {
        String uuid = Util.getParam(params, Params.UUID);
        
        // Retrieve template
        final MetadataRepository metadataRepository = context.getBean(MetadataRepository.class);
        final Metadata metadata = metadataRepository.findOneByUuid(uuid);

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE) {
            throw new IllegalArgumentException("Metadata uuid="+uuid+" is not a subtemplate");
        }

        Element tpl = metadata.getXmlData(false);
        
        
        // Processing parameters process=xpath~value.
        // xpath must point to an Element or an Attribute.
        List<?> replaceList = params.getChildren(Params.PROCESS);
        for (Object replace : replaceList) {
            if (replace instanceof Element) {
                String parameters = ((Element) replace).getText();
                int endIndex = parameters.indexOf(SEPARATOR);

                if (endIndex == -1) {
                    continue;
                }
                String xpath = parameters.substring(0, endIndex);
                String value = parameters.substring(endIndex + 1);

                Set<Namespace> allNamespaces = Sets.newHashSet();
                final Iterator descendants = tpl.getDescendants();
                while (descendants.hasNext()) {
                    Object next = descendants.next();
                    if (next instanceof Element) {
                        Element element = (Element) next;
                        allNamespaces.add(element.getNamespace());
                        for (Object o : tpl.getAdditionalNamespaces()) {
                            if (o instanceof Namespace) {
                                Namespace namespace = (Namespace) o;
                                allNamespaces.add(namespace);
                            }
                        }
                    }
                }

                Object o = Xml.selectSingle(tpl, xpath, Lists.newArrayList(allNamespaces));
                if (o instanceof Element) {
                    ((Element)o).setText(value);        // Remove all content before adding the value.
                } else if (o instanceof Attribute) {
                    ((Attribute)o).setValue(value);
                }
            }
        }
        
        return tpl;
    }
}
