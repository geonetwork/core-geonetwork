//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

import org.fao.geonet.api.processing.report.IProcessingReport;
import org.fao.geonet.constants.Geonet;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

import jeeves.server.ServiceConfig;

@Controller("md.processing.batch.report")
/**
 * Return the current or last batch process report in the user session.

 The service response contains:
 * process: The name of the process
 * startdate: The date the report was initialized
 * reportdate: The date the report is generated
 * running: True if the process is currently running
 * totalRecords: The total number of records to process
 * processedRecords: The number of records processed when the report was generated
 * nullRecords: The number of records when a null metadata identifier is processed (may happen when a record is in the selection but was deleted after the selection)
 * done: The number of records successfully processed
 * notProcessFound: The number of records which does not provide process with that process id
 * notOwner: The number of records the user who starts the process is not allowed to edit
 * notFound: The number of records not found
 * metadataErrorReport: List of records with error and exception details


 Sample response:
 ```
 <response
 process="sextant-theme-add"
 startdate="2013-08-23T18:33:09"
 reportdate="2013-08-23T18:34:09"
 running="true"
 totalRecords="477"
 processedRecords="43"
 nullRecords="0">
 <done>28</done>
 <notProcessFound>0</notProcessFound>
 <notOwner>0</notOwner>
 <notFound>0</notFound>
 <metadataErrorReport>
 <metadata id="1186">
 <message>Failed to compile stylesheet. 1 error detected.</message>
 <stack>
 javax.xml.transform.TransformerConfigurationException: Failed to compile stylesheet. 1 error detected. at net.sf.saxon.PreparedStylesheet.prepare(PreparedStylesheet.java:176) at net.sf.saxon.TransformerFactoryImpl.newTemplates(TransformerFactoryImpl.java:139) at net.sf.saxon.TransformerFactoryImpl.newTransformer(TransformerFactoryImpl.java:91) at jeeves.utils.Xml.transform(Xml.java:538) at jeeves.utils.Xml.transform(Xml.java:382) at org.fao.geonet.kernel.DataManager.updateFixedInfo(DataManager.java:2728) at org.fao.geonet.kernel.DataManager.updateMetadata(DataManager.java:1733) at
 ...
 ```
 ]]>
 </documentation>
 <class name=".services.metadata.XslProcessingReportGet"/>
 <error sheet="../xslt/error/error-json.xsl"/>
 </service>

 FIXME : this is a default success response XSL
 * @author delawen
 *
 */
@Deprecated
public class XslProcessingReportGet {
    public void init(String appPath, ServiceConfig config) throws Exception {
    }

    @RequestMapping(value = "/{portal}/{lang}/md.processing.batch.report", produces = {
        MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public
    @ResponseBody
    IProcessingReport exec(HttpSession session) throws Exception {
        return (IProcessingReport) session
            .getAttribute(Geonet.Session.BATCH_PROCESSING_REPORT);
    }
}
