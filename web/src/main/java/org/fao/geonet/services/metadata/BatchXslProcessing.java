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

package org.fao.geonet.services.metadata;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SelectionManager;
import org.jdom.Element;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//=============================================================================

/**
 * Process a metadata with an XSL transformation declared for the metadata
 * schema. Parameters sent to the service are forwaded to XSL process.
 * 
 * In each xml/schemas/schemaId directory, a process could be added in a
 * directory called process. Then the process could be called using the 
 * following URL :
 * http://localhost:8080/geonetwork/srv/en/metadata.batch.processing?process=keywords-comma-exploder&url=http://xyz
 * 
 * In that example the process has to be named keywords-comma-exploder.xsl.
 * 
 * To retrieve parameters in XSL process use the following:
 * <code>
 *     <xsl:param name="url">http://localhost:8080/</xsl:param>
 * </code>
 * 
 * @author fxprunayre
 */

public class BatchXslProcessing implements Service {
	private String _appPath;

	public void init(String appPath, ServiceConfig params) throws Exception {
		_appPath = appPath;
		
		// TODO : here we could register process on startup
		// in order to not to check process each time.
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String process = Util.getParam(params, Params.PROCESS);

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession session = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		Set<Integer> metadata = new HashSet<Integer>();
		Set<Integer> notFound = new HashSet<Integer>();
		Set<Integer> notOwner = new HashSet<Integer>();
		Set<Integer> notProcessFound = new HashSet<Integer>();

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(session);
		
		synchronized(sm.getSelection("metadata")) {
			BatchXslMetadataReindexer m = new BatchXslMetadataReindexer(dataMan, dbms, sm.getSelection("metadata").iterator(), process, _appPath, params, context, metadata, notFound, notOwner, notProcessFound);
			m.process();
		}


		// -- for the moment just return the sizes - we could return the ids
		// -- at a later stage for some sort of result display
		return new Element(Jeeves.Elem.RESPONSE).addContent(
				new Element("done").setText(metadata.size() + "")).addContent(
				new Element("notProcessFound").setText(notProcessFound.size()
						+ "")).addContent(
				new Element("notOwner").setText(notOwner.size() + ""))
				.addContent(
						new Element("notFound").setText(notFound.size() + ""));
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	class BatchXslMetadataReindexer extends MetadataIndexerProcessor {
		Dbms dbms;
		Iterator<String> iter;
		String process;
		String appPath;
		Element params;
		ServiceContext context;
        Set<Integer> metadata, notFound, notOwner, notProcessFound;

        public BatchXslMetadataReindexer(DataManager dm, Dbms dbms, Iterator<String> iter, String process, String appPath, Element params, ServiceContext context, Set<Integer> metadata, Set<Integer> notFound, Set<Integer> notOwner, Set<Integer> notProcessFound) {
            super(dm);
            this.dbms = dbms;
            this.iter = iter;
            this.process = process;
            this.appPath = appPath;
            this.params = params;
            this.context = context;
            this.metadata = metadata;
            this.notFound = notFound;
            this.notOwner = notOwner;
            this.notProcessFound = notProcessFound;
        }

        @Override
        public void process() throws Exception {
            GeonetContext gc = (GeonetContext) context
                               .getHandlerContext(Geonet.CONTEXT_NAME);
            DataManager dataMan = gc.getDataManager();

            while (iter.hasNext()) {
                String uuid = (String) iter.next();
                String id = dm.getMetadataId(dbms, uuid);
                context.info("Processing metadata with id:" + id);

                XslProcessing.process(id, process, true, appPath, params, context, metadata, notFound, notOwner, notProcessFound, true, dataMan.getSiteURL());
            }
        }
	}
}

// =============================================================================

