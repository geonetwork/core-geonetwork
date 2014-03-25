//=============================================================================
//===	Copyright (C) 2010 GeoNetwork
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

package org.fao.geonet.services.schema;

import org.fao.geonet.exceptions.OperationAbortedEx;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.XmlRequest;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

//=============================================================================

public class SchemaUtils {

	public Element addSchema(ServiceContext context, String schema, String fname, URL url, String uuid, SchemaManager scm) throws Exception {
		return processSchema(context, schema, fname, url, uuid, scm, true);
	}

	// --------------------------------------------------------------------------

	public Element updateSchema(ServiceContext context, String schema, String fname, URL url, String uuid, SchemaManager scm) throws Exception {
		return processSchema(context, schema, fname, url, uuid, scm, false);
	}

	// --------------------------------------------------------------------------

	private Element processSchema(ServiceContext context, String schema, String fname, URL url, String uuid, SchemaManager scm, boolean add) throws Exception {

		File zipArchive = null;
		boolean deleteTempZip = false;

		// -- get the URL of schema zip archive from a metadata record if uuid set
		if (!("".equals(uuid))) { 
			GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
			DataManager dm = gc.getBean(DataManager.class);

			String id = dm.getMetadataId(uuid.toLowerCase());
			if (id == null) {
     		    throw new OperationAbortedEx("Metadata record with uuid "+uuid+" doesn't exist");
			}

			// -- check download permissions (should be ok since admin but...)
			try {
				Lib.resource.checkPrivilege(context, id, ReservedOperation.download);
			}
            catch (Exception e) {
     		    throw new OperationAbortedEx("Download access not available on metadata record with uuid "+uuid);
			}

			// -- get metadata
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element elMd = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

			if (elMd == null) {
     		    throw new OperationAbortedEx("Metadata record "+uuid+" doesn't exist");
			}

			// -- transform record into brief version
			Element elBrief = dm.extractSummary(elMd);

			// -- find link using XPath and create URL for further processing
			XPath xp = XPath.newInstance ("link[contains(@protocol,'metadata-schema')]");
			List<?> elems = xp.selectNodes(elBrief);
			try {
				url = getMetadataSchemaUrl(elems);
			}
            catch (MalformedURLException mu) {
				throw new OperationAbortedEx("Metadata schema URL link for metadata record "+uuid+" is malformed : "+mu.getMessage());
			}

			if (url == null) {
				throw new OperationAbortedEx("Unable to find metadata schema URL link for metadata record "+uuid);
			}
		}

		// -- get the schema zip archive from the net
		if (url != null) { 
			XmlRequest strReq = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(url);
			zipArchive = File.createTempFile("schema",".zip");
			deleteTempZip = true;

			// FIXME: add proxy credentials etc to strReq
			strReq.executeLarge(zipArchive);


		} else {
			zipArchive = new File(fname);
		}

		Element response = doSchema(context, scm, schema, zipArchive, add);
		if (deleteTempZip) IO.delete(zipArchive, false, Geonet.SCHEMA_MANAGER);
		return response;
	}

	// --------------------------------------------------------------------------

	private Element doSchema(ServiceContext context, SchemaManager scm, String schema, File zipArchive, boolean add) throws Exception {

		Element response = new Element("response");

		long fsize = 0;
		if (zipArchive.exists()) {
			fsize = zipArchive.length();
		} else {
     	throw new OperationAbortedEx("Zip Archive doesn't exist");
		}

		// -- check that the archive actually has something in it
		if (fsize == 0) {
     	throw new OperationAbortedEx("Schema archive has zero size");
		}

		InputStream inputStream = new BufferedInputStream(new FileInputStream(zipArchive));

		// -- supply the stream containing the schema zip archive to the schema 
		// -- manager
		try {
			if (add) {
				scm.addPluginSchema(context.getApplicationContext(), schema, inputStream);
			} else {
				scm.updatePluginSchema(context.getApplicationContext(), schema, inputStream);
			}
     	response.setAttribute("status", "ok");
     	response.setAttribute("message", "Schema "+schema+" has been added/updated");
		} catch (Exception e) {
			e.printStackTrace();
     	throw new OperationAbortedEx("Schema add/update failed: "+e.getMessage());
		}

		return response;
	}

	// --------------------------------------------------------------------------

	private URL getMetadataSchemaUrl(List<?> elems) throws MalformedURLException { 
	    for (Object ob : elems) {
			if (ob instanceof Element) {
				Element elem = (Element)ob;
				String href = elem.getAttributeValue("href");
				return new URL(href);
			}
		}

		return null;
	}

}

// =============================================================================

