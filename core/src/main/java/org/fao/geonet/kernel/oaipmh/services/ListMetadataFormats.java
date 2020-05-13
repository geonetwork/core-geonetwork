//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.oaipmh.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.utils.Xml;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.ListMetadataFormatsRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.ListMetadataFormatsResponse;
import org.fao.oaipmh.responses.MetadataFormat;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import jeeves.server.context.ServiceContext;
import jeeves.server.overrides.ConfigurationOverrides;

//=============================================================================

public class ListMetadataFormats implements OaiPmhService {
    private static final String DEFAULT_PREFIXES_FILE = "WEB-INF/config-oai-prefixes.xml";

    //---------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //---------------------------------------------------------------------------

    public String getVerb() {
        return ListMetadataFormatsRequest.VERB;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception {
        ListMetadataFormatsRequest req = (ListMetadataFormatsRequest) request;
        ListMetadataFormatsResponse res = new ListMetadataFormatsResponse();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager sm = gc.getBean(SchemaManager.class);

        String uuid = req.getIdentifier();
        if (uuid != null) {
            String schema = context.getBean(IMetadataUtils.class).findOneByUuid(uuid).getDataInfo().getSchemaId();
            res.addFormat(getSchemaInfo(context, sm, schema));
        } else {
            for (String schema : sm.getSchemas())
                res.addFormat(getSchemaInfo(context, sm, schema));
        }

        for (MetadataFormat mdf : getConvertFormats(context)) {
            res.addFormat(mdf);
        }

        return res;
    }

    //---------------------------------------------------------------------------

    private MetadataFormat getSchemaInfo(ServiceContext context, SchemaManager sm, String name) throws IOException, JDOMException {
        MetadataFormat mf = new MetadataFormat();
        mf.prefix = name;
        mf.schema = "";
        mf.namespace = Namespace.NO_NAMESPACE;

        Attribute schemaLoc = sm.getSchemaLocation(name, context);
        if (schemaLoc == null) {
            // no schema location eg. when schema is a DTD
        } else if (schemaLoc.getName().equals("noNamespaceSchemaLocation")) {
            mf.schema = schemaLoc.getValue();
        } else {
            String sLoc = schemaLoc.getValue();
            String[] toks = sLoc.split("\\s");
            if (toks.length > 1) {
                mf.namespace = Namespace.getNamespace(toks[0]);
                mf.schema = toks[1];
            }
        }
        return mf;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    private List<MetadataFormat> getConvertFormats(ServiceContext context) throws IOException, JDOMException {

        Element elem = Xml.loadFile(context.getAppPath().resolve(DEFAULT_PREFIXES_FILE));
        if (context.getServlet() != null && context.getServlet().getServletContext() != null) {
            ConfigurationOverrides.DEFAULT.updateWithOverrides(DEFAULT_PREFIXES_FILE, context.getServlet().getServletContext(), context.getAppPath(), elem);
        }

        @SuppressWarnings("unchecked")
        List<Element> defaultSchemas = elem.getChildren();

        List<MetadataFormat> defMdfs = new ArrayList<MetadataFormat>();
        for (Element schema : defaultSchemas) {
            defMdfs.add(new MetadataFormat(schema.getAttributeValue("prefix"), schema.getAttributeValue("schemaLocation"), schema.getAttributeValue("nsUrl")));
        }
        return defMdfs;
    }
}

//=============================================================================

