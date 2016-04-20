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

package org.fao.geonet.kernel.oaipmh;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.ISearchManager;
import org.fao.geonet.utils.Xml;
import org.fao.oaipmh.exceptions.OaiPmhException;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

//=============================================================================

public class Lib
{
	public static final String SESSION_OBJECT = "oai-list-records-result";

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public static boolean existsConverter(Path schemaDir, String prefix) {
		 Path f = schemaDir.resolve("convert").resolve(prefix + ".xsl");
		 return Files.exists(f);
	}

	//--------------------------------------------------------------------------

	public static Element prepareTransformEnv(String uuid, String changeDate, String baseUrl, String siteUrl, String siteName) {

		//--- setup environment

		Element env = new Element("env");

		env.addContent(new Element("uuid")      .setText(uuid));
		env.addContent(new Element("changeDate").setText(changeDate));
		env.addContent(new Element("baseURL")   .setText(baseUrl));
		env.addContent(new Element("siteURL")   .setText(siteUrl));
		env.addContent(new Element("siteName")  .setText(siteName));

		return env;
	}

	//--------------------------------------------------------------------------

	public static Element transform(Path schemaDir, Element env, Element md, String targetFormat) throws Exception {

		//--- setup root element

		Element root = new Element("root");
		root.addContent(md);
		root.addContent(env);

		//--- do an XSL transformation

		Path styleSheet = schemaDir.resolve("convert").resolve(targetFormat);

		return Xml.transform(root, styleSheet);
	}

	//---------------------------------------------------------------------------

	public static List<Integer> search(ServiceContext context, Element params) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		ISearchManager sm = gc.getBean(ISearchManager.class);

        // TODO: SOLR-MIGRATION
        throw new UnsupportedOperationException("Solr search does not support OAI yet");
	}

	//---------------------------------------------------------------------------

	public static Element toJeevesException(OaiPmhException e)
	{
		String  msg = e.getMessage();
		String  cls = e.getClass().getSimpleName();
		String  id  = e.getCode();
		Element res = e.getResponse();

		Element error = new Element(Jeeves.Elem.ERROR)
								.addContent(new Element("message").setText(msg))
								.addContent(new Element("class")  .setText(cls));

		error.setAttribute("id", id);

		if (res != null)
		{
			Element elObj = new Element("object");
			elObj.addContent(res.detach());

			error.addContent(elObj);
		}

		return error;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private static ServiceConfig dummyConfig = new ServiceConfig();
}

//=============================================================================

