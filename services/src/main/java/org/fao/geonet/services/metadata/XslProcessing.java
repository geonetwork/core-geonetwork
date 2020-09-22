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

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.processing.XslProcessUtils;
import org.fao.geonet.api.processing.report.XsltMetadataProcessingReport;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.services.ReadWriteController;

/**
 * Process a metadata with an XSL transformation declared for the metadata schema. All parameters
 * sent to the service are forwarded to XSL process. <br/> Parameters are: <ul> <li>process: the
 * process identifier (aka. file name without extension)</li> <li>save: (optional) 1 to save the
 * results (default), 0 to only process and return the processed record</li> </ul> <br/>
 *
 * In each xml/schemas/schemaId directory, a process could be added in a directory called process.
 * Then the process could be called using the following URL : http://localhost:8080/geonetwork/srv/en/metadata.processing?process
 * =keywords-comma-exploder&url=http://xyz <br/> <br/>
 *
 * In that example the process has to be named keywords-comma-exploder.xsl.
 *
 * To retrieve parameters in XSL process use the following:
 * <pre>
 * {@code
 *     <xsl:param name="url">http://localhost:8080/</xsl:param>
 * }
 * </pre>
 *
 *
 * TODO : it could be nice to add an option to return a diff so we could preview the change before
 * applying them.
 *
 * @author fxprunayre
 */
@Controller("metadata.processing")
@ReadWriteController
@Deprecated
public class XslProcessing {

    @RequestMapping(value = {"/{portal}/{lang}/md.processing", "/{portal}/{lang}/xml.metadata.processing", "/{portal}/{lang}/metadata.processing.new"}, produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public XsltMetadataProcessingReport xmlProcessing(@RequestParam(value = Params.PROCESS) String process,
                                                      @PathVariable String lang,
                                                      @RequestParam(value = Params.SAVE, defaultValue = "1") String saveParam,
                                                      @RequestParam(defaultValue = "") String id,
                                                      @RequestParam(defaultValue = "") String uuid,
                                                      HttpServletRequest request)
        throws Exception {

        ConfigurableApplicationContext appContext = ApplicationContextHolder.get();

        DataManager dataMan = appContext.getBean(DataManager.class);
        SettingManager settingsMan = appContext.getBean(SettingManager.class);
        ServiceManager serviceManager = appContext.getBean(ServiceManager.class);

        ServiceContext context = serviceManager.createServiceContext("md.processing", lang, request);
        XsltMetadataProcessingReport report = new XsltMetadataProcessingReport(process);

        if (id.isEmpty()) {
            id = dataMan.getMetadataId(uuid);
        }
        final boolean save = "1".equals(saveParam);
        Element processedMetadata;
        try {
            final String siteURL = settingsMan.getSiteURL(context);
            processedMetadata = XslProcessUtils.process(context, id, process, save, true, true, report, siteURL, request.getParameterMap());
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed", "Not found:"
                    + report.getNumberOfRecordNotFound() +
                    ", Not owner:" + report.getNumberOfRecordsNotEditable() +
                    ", No process found:" + report.getNoProcessFoundCount() + ".");
            }
        } catch (Exception e) {
            throw e;
        }
        // -- return the processed metadata id

        // and the processed metadata if not saved.
        return report;
    }
}
