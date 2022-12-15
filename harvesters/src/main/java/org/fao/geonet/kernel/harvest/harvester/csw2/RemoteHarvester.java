//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.csw2;


import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.client.RemoteHarvesterApiClient;
import org.fao.geonet.client.model.RemoteHarvesterConfiguration;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

class RemoteHarvester implements IHarvester<CswRemoteHarvestResult> {
    private final AtomicBoolean cancelMonitor;

    private Logger log;
    private CswParams2 params;
    private ServiceContext context;

    /**
     * Contains a list of accumulated errors during the executing of this harvest.
     */
    private List<HarvestError> errors = new LinkedList<HarvestError>();


    public RemoteHarvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, CswParams2 params) {
        this.cancelMonitor = cancelMonitor;
        this.log = log;
        this.context = context;
        this.params = params;
    }

    public CswRemoteHarvestResult harvest(Logger log) throws Exception {
        this.log = log;

        if (cancelMonitor.get()) {
            return new CswRemoteHarvestResult();
        }

        boolean error = false;
        CswRemoteHarvestResult result = new CswRemoteHarvestResult();

        SettingManager sm = context.getBean(SettingManager.class);
        String url = sm.getValue(RemoteHarvesterApiClient.SETTING_REMOTE_HARVESTER_API);
        if (StringUtils.isEmpty(url)) {
            throw new Exception("Remote harvester API endpoint is not configured. Configure it in the Settings page.");
        }

        RemoteHarvesterConfiguration remoteHarvesterConfiguration = new RemoteHarvesterConfiguration();
        remoteHarvesterConfiguration.setUrl(params.capabUrl);
        remoteHarvesterConfiguration.setLongTermTag(params.getName());
        remoteHarvesterConfiguration.setNumberOfRecordsPerRequest(params.numberOfRecordsPerRequest);
        remoteHarvesterConfiguration.setLookForNestedDiscoveryService(params.remoteHarvesterNestedServices);
        remoteHarvesterConfiguration.setErrorConfigDuplicatedUuids(params.errorConfigDuplicatedUuids);
        remoteHarvesterConfiguration.setErrorConfigFewerRecordsThanRequested(params.errorConfigFewerRecordsThanRequested);
        remoteHarvesterConfiguration.setErrorConfigNextRecordsBadValue(params.errorConfigNextRecordsBadValue);
        remoteHarvesterConfiguration.setErrorConfigNextRecordsNotZero(params.errorConfigNextRecordsNotZero);
        remoteHarvesterConfiguration.setErrorConfigTotalRecordsChanged(params.errorConfigTotalRecordsChanged);
        remoteHarvesterConfiguration.setErrorConfigMaxPercentTotalRecordsChangedAllowed(params.errorConfigMaxPercentTotalRecordsChangedAllowed);
        remoteHarvesterConfiguration.setGetRecordQueueHint(params.processQueueType);
        remoteHarvesterConfiguration.setSkipHarvesting(params.skipHarvesting);
        remoteHarvesterConfiguration.setStoreAtMostNHistoricalRuns(5);

        String filterConstraint;

        if (StringUtils.isNotEmpty(params.rawFilter)) {
            filterConstraint = params.rawFilter;
        } else {
            filterConstraint = getFilterConstraint(params.eltFilters, params.bboxFilter);
        }

        remoteHarvesterConfiguration.setFilter(filterConstraint);
        remoteHarvesterConfiguration.setExecuteLinkChecker(params.executeLinkChecker);

        RemoteHarvesterApiClient remoteHarvesterApiClient = new RemoteHarvesterApiClient(url);
        result.processId = remoteHarvesterApiClient.startHarvest(remoteHarvesterConfiguration, log);

        log.info("Remote CSW harvester, processId " + result.processId);

        //remoteHarvesterApiClient.retrieveProgress(result.processId);

        return result;
    }




    public List<HarvestError> getErrors() {
        return errors;
    }


    private String getFilterConstraint(List<Element> filters, Element bboxFilter) throws Exception {
        Path file = context.getAppPath().resolve("xml").resolve("csw").resolve("harvester-csw-filter.xsl");

        Element eltFilter = new Element("filters");
        for(Element e: filters) {
            Element e1 = (Element) e.clone();

            eltFilter.addContent(e1.detach());
        }

        Element cswFilter = Xml.transform(eltFilter, file);

        if (bboxFilter != null) {
            Map<String, Double> bboxCoordinates = new HashMap<>();
            bboxCoordinates.put("bbox-xmin", Double.parseDouble(bboxFilter.getChildText("bbox-xmin")));
            bboxCoordinates.put("bbox-ymin", Double.parseDouble(bboxFilter.getChildText("bbox-ymin")));
            bboxCoordinates.put("bbox-xmax", Double.parseDouble(bboxFilter.getChildText("bbox-xmax")));
            bboxCoordinates.put("bbox-ymax", Double.parseDouble(bboxFilter.getChildText("bbox-ymax")));

            if (cswFilter.getChildren().size() == 0) {
                cswFilter.addContent(buildBboxFilter(bboxCoordinates));
            } else {
                Element filterContent = ((Element) cswFilter.getChildren().get(0));
                filterContent = (Element) filterContent.detach();

                Element and = new Element("And", Csw.NAMESPACE_OGC);
                and.addContent(filterContent);
                and.addContent(buildBboxFilter(bboxCoordinates));
                cswFilter.setContent(and);
            }
        }

        if (cswFilter.getChildren().size() == 0) {
            return StringUtils.EMPTY;
        } else {
            return Xml.getString(cswFilter);
        }
    }

    private Content buildBboxFilter(Map<String, Double> bboxCoordinates) {
        Namespace gml = Namespace.getNamespace("http://www.opengis.net/gml");

        Element bbox = new Element("BBOX", Csw.NAMESPACE_OGC);
        Element bboxProperty = new Element("PropertyName", Csw.NAMESPACE_OGC);
        bboxProperty.setText("ows:BoundingBox");
        bbox.addContent(bboxProperty);
        Element envelope = new Element("Envelope", gml);
        Element lowerCorner = new Element("lowerCorner", gml);
        lowerCorner.setText(bboxCoordinates.get("bbox-xmin") + " " + bboxCoordinates.get("bbox-ymin"));
        Element upperCorner = new Element("upperCorner", gml);
        upperCorner.setText(bboxCoordinates.get("bbox-xmax") + " " + bboxCoordinates.get("bbox-ymax"));
        envelope.addContent(lowerCorner);
        envelope.addContent(upperCorner);
        bbox.addContent(envelope);
        return bbox;
    }
}
