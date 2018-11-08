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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.standards.StandardsUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo_;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.MetadataRepository;
import org.jdom.Element;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.nio.file.Path;
import java.util.List;

import static org.fao.geonet.repository.specification.MetadataSpecs.hasType;

@Deprecated
public class GetTypes implements Service {

    private static String[] elementNames = {"label", "description"};

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
        throws Exception {

        final Sort sort = new Sort(Sort.Direction.ASC, Metadata_.dataInfo.getName() + "." + MetadataDataInfo_.root.getName());

        final List<Metadata> metadatas = context.getBean(MetadataRepository.class).findAll((Specification<Metadata>)hasType(MetadataType.SUB_TEMPLATE), sort);
        Element subTemplateTypes = new Element("getTypes");

        for (Metadata metadata : metadatas) {
            subTemplateTypes.addContent(new Element("type").setText(metadata.getDataInfo().getRoot()));
            subTemplateTypes.addContent(new Element("schemaId").setText(metadata.getDataInfo().getSchemaId()));
        }

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager scm = gc.getBean(SchemaManager.class);

        for (Object e : subTemplateTypes.getChildren()) {
            if (e instanceof Element) {
                Element record = ((Element) e);
                try {
                    String schema = record.getChildText("schemaid");
                    String name = StandardsUtils.findNamespace(record.getChildText("type"), scm, (schema == null ? "iso19139" : schema));
                    Element info = StandardsUtils.getHelp(scm, "labels.xml", schema, name, "", "", "", null, context);
                    if (info != null) {
                        for (String childName : elementNames) {
                            Element child = info.getChild(childName);
                            if (child != null) {
                                record.addContent(child.detach());
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Can't retrieve information for the type
                }
            }
        }

        return subTemplateTypes;
    }
}
