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

import static org.fao.geonet.repository.specification.MetadataSpecs.hasMetadataUuid;

import java.nio.file.Path;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Xml;
import org.fao.oaipmh.exceptions.CannotDisseminateFormatException;
import org.fao.oaipmh.exceptions.IdDoesNotExistException;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.GetRecordRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.GetRecordResponse;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.Record;
import org.jdom.Attribute;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

//=============================================================================

public class GetRecord implements OaiPmhService {
    // function builds a OAI records from a metadata record, according to the arguments select and selectVal
    public static Record buildRecordStat(ServiceContext context, Specification<Metadata> spec/*String select, Object selectVal*/,
                                         String prefix) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager sm = gc.getBean(SchemaManager.class);

        AbstractMetadata metadata = context.getBean(IMetadataUtils.class).findOne(spec);
        if (metadata == null)
            throw new IdDoesNotExistException(spec.toString());

        String uuid = metadata.getUuid();
        final MetadataDataInfo dataInfo = metadata.getDataInfo();
        String schema = dataInfo.getSchemaId();
        String changeDate = dataInfo.getChangeDate().getDateAndTime();
        String data = metadata.getData();

        Element md = Xml.loadString(data, false);

        //--- try to disseminate format

        if (prefix.equals(schema)) {
            Attribute schemaLocAtt = sm.getSchemaLocation(schema, context);
            if (schemaLocAtt != null) {
                if (md.getAttribute(schemaLocAtt.getName(), schemaLocAtt.getNamespace()) == null) {
                    md.setAttribute(schemaLocAtt);
                    // make sure namespace declaration for schemalocation is present -
                    // remove it first (does nothing if not there) then add it
                    md.removeNamespaceDeclaration(schemaLocAtt.getNamespace());
                    md.addNamespaceDeclaration(schemaLocAtt.getNamespace());
                }
            }
        } else {
            Path schemaDir = sm.getSchemaDir(schema);
            if (Lib.existsConverter(schemaDir, prefix)) {
                final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
                Element env = Lib.prepareTransformEnv(uuid, changeDate, context.getBaseUrl(), siteURL, gc.getBean(SettingManager.class)
                    .getSiteName());
                md = Lib.transform(schemaDir, env, md, prefix + ".xsl");
            } else {
                throw new CannotDisseminateFormatException("Unknown prefix : " + prefix);
            }
        }

        //--- build header and set some infos

        Header h = new Header();

        h.setIdentifier(uuid);
        h.setDateStamp(new ISODate(changeDate));

        for (MetadataCategory metadataCategory : metadata.getCategories()) {
            h.addSet(metadataCategory.getName());
        }

        //--- build and return record

        Record r = new Record();

        r.setHeader(h);
        r.setMetadata(md);

        return r;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //---------------------------------------------------------------------------

    public String getVerb() {
        return GetRecordRequest.VERB;
    }

    //---------------------------------------------------------------------------

    public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception {
        GetRecordRequest req = (GetRecordRequest) request;
        GetRecordResponse res = new GetRecordResponse();

        String uuid = req.getIdentifier();
        String prefix = req.getMetadataPrefix();

        res.setRecord(buildRecord(context, uuid, prefix));

        return res;
    }

    //---------------------------------------------------------------------------

    private Record buildRecord(ServiceContext context, String uuid, String prefix) throws Exception {
        return buildRecordStat(context, hasMetadataUuid(uuid), prefix);
    }
}

//=============================================================================

