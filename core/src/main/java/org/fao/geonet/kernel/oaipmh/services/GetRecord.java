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

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.oaipmh.OaiPmhService;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.oaipmh.exceptions.CannotDisseminateFormatException;
import org.fao.oaipmh.exceptions.IdDoesNotExistException;
import org.fao.oaipmh.requests.AbstractRequest;
import org.fao.oaipmh.requests.GetRecordRequest;
import org.fao.oaipmh.responses.AbstractResponse;
import org.fao.oaipmh.responses.GetRecordResponse;
import org.fao.oaipmh.responses.Header;
import org.fao.oaipmh.responses.Record;
import org.fao.oaipmh.util.ISODate;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;

//=============================================================================

public class GetRecord implements OaiPmhService
{
	public String getVerb() { return GetRecordRequest.VERB; }

	//---------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//---------------------------------------------------------------------------

	public AbstractResponse execute(AbstractRequest request, ServiceContext context) throws Exception
	{
		GetRecordRequest  req = (GetRecordRequest) request;
		GetRecordResponse res = new GetRecordResponse();

		String uuid   = req.getIdentifier();
		String prefix = req.getMetadataPrefix();

		res.setRecord(buildRecord(context, uuid, prefix));

		return res;
	}

	//---------------------------------------------------------------------------

	private Record buildRecord(ServiceContext context, String uuid, String prefix) throws Exception {
		return buildRecordStat(context,"uuid",uuid,prefix);
	}

	//---------------------------------------------------------------------------

	// function builds a OAI records from a metadata record, according to the arguments select and selectVal
	public static Record buildRecordStat(ServiceContext context, String select, Object selectVal, String prefix) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SchemaManager   sm = gc.getBean(SchemaManager.class);
		DataManager     dm = gc.getBean(DataManager.class);

		String query = "SELECT uuid,id, schemaId, changeDate, data FROM Metadata WHERE "+select+"=?";
		@SuppressWarnings("unchecked")
        List<Element> list = dbms.select(query, selectVal).getChildren();

		if (list.size() == 0)
			throw new IdDoesNotExistException(selectVal.toString());

		Element rec = (Element) list.get(0);

		String id         = rec.getChildText("id");
		String uuid				= rec.getChildText("uuid");
		String schema     = rec.getChildText("schemaid");
		String changeDate = rec.getChildText("changedate");
		String data       = rec.getChildText("data");

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
			String schemaDir = sm.getSchemaDir(schema);
			if (Lib.existsConverter(schemaDir, prefix)) {
				Element env = Lib.prepareTransformEnv(uuid, changeDate, context.getBaseUrl(), dm.getSiteURL(context), gc.getSiteName());
				md = Lib.transform(schemaDir, env, md, prefix+".xsl");
			} else {
				throw new CannotDisseminateFormatException("Unknown prefix : "+ prefix);
			}
		}

		//--- build header and set some infos

		Header h = new Header();

		h.setIdentifier(uuid);
		h.setDateStamp(new ISODate(changeDate));

		//--- find and add categories (here called sets)

		query = "SELECT name FROM Categories, MetadataCateg WHERE id=categoryId AND metadataId=?";

		@SuppressWarnings("unchecked")
        List<Element> list2 = dbms.select(query, Integer.valueOf(id)).getChildren();

		for (Object o : list2)
		{
			rec = (Element) o;

			h.addSet(rec.getChildText("name"));
		}

		//--- build and return record

		Record r = new Record();

		r.setHeader(h);
		r.setMetadata(md);

		return r;
	}
}

//=============================================================================

