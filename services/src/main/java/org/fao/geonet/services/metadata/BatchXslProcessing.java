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

import java.util.Iterator;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

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
public class BatchXslProcessing extends NotInReadOnlyModeService {
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

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
	public Element serviceSpecificExec(Element params, ServiceContext context)
			throws Exception {
		String process = Util.getParam(params, Params.PROCESS);

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dataMan = gc.getBean(DataManager.class);
		UserSession session = context.getUserSession();

        XslProcessingReport xslProcessingReport = new XslProcessingReport(process);
        

		context.info("Get selected metadata");
		SelectionManager sm = SelectionManager.getManager(session);
		
		synchronized(sm.getSelection("metadata")) {
			xslProcessingReport.setTotalRecords(sm.getSelection("metadata").size());
			BatchXslMetadataReindexer m = new BatchXslMetadataReindexer(dataMan, sm.getSelection("metadata").iterator(),
					process, _appPath, params, context, xslProcessingReport);
			m.process();
		}
		
		return xslProcessingReport.toXml();
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	static final class BatchXslMetadataReindexer extends MetadataIndexerProcessor {
		Iterator<String> iter;
		String process;
		String appPath;
		Element params;
		ServiceContext context;
        XslProcessingReport xslProcessingReport;

        public BatchXslMetadataReindexer(DataManager dm, Iterator<String> iter, String process,
        		String appPath, Element params, ServiceContext context, 
        		XslProcessingReport xslProcessingReport) {
            super(dm);
            this.iter = iter;
            this.process = process;
            this.appPath = appPath;
            this.params = params;
            this.context = context;
            this.xslProcessingReport = xslProcessingReport;
        }

        @Override
        public void process() throws Exception {

            while (iter.hasNext()) {
                String uuid = iter.next();
                String id = getDataManager().getMetadataId(uuid);
                context.info("Processing metadata with id:" + id);

                final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
                XslProcessing.process(id, process, true, appPath, params, context, xslProcessingReport, true, siteURL);
                
                UserSession  session = context.getUserSession();
                session.setProperty("BATCH_PROCESSING_REPORT", xslProcessingReport);
                
            }
        }
	}
}