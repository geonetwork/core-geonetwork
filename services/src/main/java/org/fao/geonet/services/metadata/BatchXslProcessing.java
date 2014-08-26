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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import jeeves.server.ServiceConfig;
import jeeves.services.ReadWriteController;

import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Process a metadata with an XSL transformation declared for the metadata
 * schema. Parameters sent to the service are forwaded to XSL process.
 * 
 * In each xml/schemas/schemaId directory, a process could be added in a
 * directory called process. Then the process could be called using the
 * following URL :
 * http://localhost:8080/geonetwork/srv/en/metadata.batch.processing
 * ?process=keywords-comma-exploder&url=http://xyz
 * 
 * In that example the process has to be named keywords-comma-exploder.xsl.
 * 
 * To retrieve parameters in XSL process use the following: <code>
 *     <xsl:param name="url">http://localhost:8080/</xsl:param>
 * </code>
 * 
 * @author fxprunayre
 */
@Controller("md.processing.batch")
@ReadWriteController
public class BatchXslProcessing { // extends NotInReadOnlyModeService {
	private String _appPath;

	@Autowired
	private DataManager dataMan;
	@Autowired
	private SettingManager settingManager;
	@Autowired
	private SelectionManager selectionManager;

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
	@RequestMapping(value = "/{lang}/md.processing.batch", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	XslProcessingReport serviceSpecificExec(@RequestParam String process,
			HttpSession session, HttpServletRequest request) throws Exception {

		XslProcessingReport xslProcessingReport = new XslProcessingReport(
				process);

		final String siteURL = request.getRequestURL().toString() + "?"
				+ request.getQueryString();
		Log.info("org.fao.geonet.services.metadata", "Get selected metadata");

		synchronized (selectionManager.getSelection("metadata")) {
			xslProcessingReport.setTotalRecords(selectionManager.getSelection(
					"metadata").size());
			BatchXslMetadataReindexer m = new BatchXslMetadataReindexer(
					dataMan, selectionManager.getSelection("metadata")
							.iterator(), process, _appPath, session, siteURL,
					xslProcessingReport, request);
			m.process();
		}

		return xslProcessingReport;
	}

	// --------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// --------------------------------------------------------------------------

	static final class BatchXslMetadataReindexer extends
			MetadataIndexerProcessor {
		Iterator<String> iter;
		String process;
		String siteURL;
		String appPath;
		HttpSession session;
		XslProcessingReport xslProcessingReport;
		HttpServletRequest request;

		public BatchXslMetadataReindexer(DataManager dm, Iterator<String> iter,
				String process, String appPath, HttpSession session,
				String siteURL, XslProcessingReport xslProcessingReport,
				HttpServletRequest request) {
			super(dm);
			this.iter = iter;
			this.process = process;
			this.session = session;
			this.siteURL = siteURL;
			this.request = request;
			this.appPath = appPath;
			this.xslProcessingReport = xslProcessingReport;
		}

		@Override
		public void process() throws Exception {

			while (iter.hasNext()) {
				String uuid = iter.next();
				String id = getDataManager().getMetadataId(uuid);
				Log.info("org.fao.geonet.services.metadata",
						"Processing metadata with id:" + id);

				XslProcessing.get().process(id, process, true, appPath,
						xslProcessingReport, true, siteURL, request);

				this.session.setAttribute("BATCH_PROCESSING_REPORT",
						xslProcessingReport);

			}
		}
	}
}