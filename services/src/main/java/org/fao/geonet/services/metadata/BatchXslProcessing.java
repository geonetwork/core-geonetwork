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

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.MetadataIndexerProcessor;
import org.fao.geonet.kernel.SelectionManager;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Process a metadata with an XSL transformation declared for the metadata
 * schema. Parameters sent to the service are forwaded to XSL process.
 * 
 * In each xml/schemas/schemaId directory, a process could be added in a
 * directory called process. Then the process could be called using the
 * following URL :
 * http://localhost:8080/geonetwork/srv/eng/md.processing.batch
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
	@Autowired
	private DataManager dataMan;
    @Autowired
    private XslProcessing xslProcessing;
    @Autowired
    private ServiceManager serviceManager;

	// --------------------------------------------------------------------------
	// ---
	// --- Service
	// ---
	// --------------------------------------------------------------------------

	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/{lang}/md.processing.batch", produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody
	XslProcessingReport serviceSpecificExec(@RequestParam String process,
                                            @PathVariable String lang,
                                            HttpSession session,
                                            HttpServletRequest request) throws Exception {

		XslProcessingReport xslProcessingReport = new XslProcessingReport(process);

		final String siteURL = request.getRequestURL().toString() + "?" + request.getQueryString();
		Log.info("org.fao.geonet.services.metadata", "Get selected metadata");
        ServiceContext serviceContext = ServiceContext.get();
        if (serviceContext != null) {
            SelectionManager selectionManager =
                    SelectionManager.getManager(serviceContext.getUserSession());

            final HashSet<String> metadata;
            synchronized (selectionManager.getSelection("metadata")) {
                final Set<String> selection = selectionManager.getSelection(SelectionManager.SELECTION_METADATA);
                xslProcessingReport.setTotalRecords(selection.size());
                metadata = Sets.newHashSet(selection);
            }

            ServiceContext context = serviceManager.createServiceContext("md.processing.batch", lang, request);
            BatchXslMetadataReindexer m = new BatchXslMetadataReindexer(context,
                    dataMan, metadata.iterator(), process, xslProcessing, session, siteURL,
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
		XslProcessing xslProcessing;
		HttpSession session;
		XslProcessingReport xslProcessingReport;
		HttpServletRequest request;
        ServiceContext context;

		public BatchXslMetadataReindexer(ServiceContext context,
                                         DataManager dm,
                                         Iterator<String> iter,
                                         String process,
                                         XslProcessing xslProcessing,
                                         HttpSession session,
                                         String siteURL,
                                         XslProcessingReport xslProcessingReport,
                                         HttpServletRequest request) {
			super(dm);
			this.iter = iter;
			this.process = process;
			this.session = session;
			this.siteURL = siteURL;
			this.request = request;
			this.xslProcessing = xslProcessing;
			this.xslProcessingReport = xslProcessingReport;
            this.context = context;
		}

		@Override
		public void process() throws Exception {

			while (iter.hasNext()) {
				String uuid = iter.next();
				String id = getDataManager().getMetadataId(uuid);
				Log.info("org.fao.geonet.services.metadata",
						"Processing metadata with id:" + id);

                xslProcessing.process(context, id, process, true, xslProcessingReport, siteURL, request.getParameterMap());

				this.session.setAttribute("BATCH_PROCESSING_REPORT", xslProcessingReport);
			}
		}
	}
}