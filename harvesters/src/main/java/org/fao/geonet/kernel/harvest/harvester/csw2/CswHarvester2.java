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

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.Logger;
import org.fao.geonet.client.RemoteHarvesterApiClient;
import org.fao.geonet.client.model.DocumentTypeStatus;
import org.fao.geonet.client.model.EndpointStatus;
import org.fao.geonet.client.model.HarvestStatus;
import org.fao.geonet.client.model.IngestStatus;
import org.fao.geonet.client.model.LinkCheckStatus;
import org.fao.geonet.client.model.OrchestratedHarvestProcessState;
import org.fao.geonet.client.model.OrchestratedHarvestProcessStatus;
import org.fao.geonet.client.model.StatusType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.jdom.Element;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Harvest metadata from other catalogues using the CSW protocol
 */
public class CswHarvester2 extends AbstractHarvester<HarvestResult, CswParams2> {

    @Override
    protected CswParams2 createParams() {
        return new CswParams2(dataMan);
    }

    /**
     * Stores in the harvester settings table some values not managed by {@link AbstractHarvester}
     * @param params the harvester parameters.
     * @param path
     * @param siteId
     * @param optionsId
     * @throws SQLException
     */
    protected void storeNodeExtra(CswParams2 params, String path, String siteId, String optionsId) throws SQLException {

        harvesterSettingsManager.add("id:" + siteId, "capabUrl", params.capabUrl);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "outputSchema", params.outputSchema);
        harvesterSettingsManager.add("id:" + optionsId, "remoteHarvesterNestedServices", params.remoteHarvesterNestedServices);
        harvesterSettingsManager.add("id:" + optionsId, "numberOfRecordsPerRequest", params.numberOfRecordsPerRequest);

        harvesterSettingsManager.add("id:" + optionsId, "errorConfigNextRecordsNotZero", params.errorConfigNextRecordsNotZero);
        harvesterSettingsManager.add("id:" + optionsId, "errorConfigNextRecordsBadValue", params.errorConfigNextRecordsBadValue);
        harvesterSettingsManager.add("id:" + optionsId, "errorConfigFewerRecordsThanRequested", params.errorConfigFewerRecordsThanRequested);
        harvesterSettingsManager.add("id:" + optionsId, "errorConfigTotalRecordsChanged", params.errorConfigTotalRecordsChanged);
        harvesterSettingsManager.add("id:" + optionsId, "errorConfigMaxPercentTotalRecordsChangedAllowed", params.errorConfigMaxPercentTotalRecordsChangedAllowed);
        harvesterSettingsManager.add("id:" + optionsId, "errorConfigDuplicatedUuids", params.errorConfigDuplicatedUuids);

        harvesterSettingsManager.add("id:" + optionsId, "processQueueType", params.processQueueType);

        harvesterSettingsManager.add("id:" + optionsId, "doNotSort", params.doNotSort);
        harvesterSettingsManager.add("id:" + optionsId, "executeLinkChecker", params.executeLinkChecker);
        harvesterSettingsManager.add("id:" + optionsId, "processID", "");
        harvesterSettingsManager.add("id:" + optionsId, "skipHarvesting",  params.skipHarvesting);

        //--- store dynamic filter nodes
        String filtersID = harvesterSettingsManager.add(path, "filters", "");

        if (params.eltFilters != null) {
            int i = 1;
            for (Element element : params.eltFilters) {
                String fID = harvesterSettingsManager.add("id:" + filtersID, "filter", "");

                for (Element value : (List<Element>) element.getChildren()) {
                    harvesterSettingsManager.add("id:" + fID, value.getName(), value.getText());
                }

                harvesterSettingsManager.add("id:" + fID, "position", i++);
            }
        }

        harvesterSettingsManager.add(path, "rawFilter", params.rawFilter);

        if (params.bboxFilter != null) {
            String bboxFilterID = harvesterSettingsManager.add(path, "bboxFilter", "");
            for (Element value : (List<Element>) params.bboxFilter.getChildren()) {
                harvesterSettingsManager.add("id:" + bboxFilterID, value.getName(), value.getText());
            }

        }
    }



    /**
     * @param log
     * @throws Exception
     */
    public void doHarvest(Logger log) throws Exception {
        RemoteHarvester h = new RemoteHarvester(cancelMonitor, log, context, params);
        String processId = "";

        try {
            result = h.harvest(log);

            processId = ((CswRemoteHarvestResult) result).processId;
        } catch (Exception ex) {
            log.error("Remote CSW harvester, doHarvest error: " + ex.getMessage());
            log.error(ex);
            running = false;

            throw ex;
        }

        final String harvesterProcessId = processId;

        if (StringUtils.isNotEmpty(harvesterProcessId)) {
            harvesterSettingsManager.setValue("harvesting/id:" + getID() + "/options/processID", harvesterProcessId);
            final CswHarvester2 thiz = this;
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    long startTime = System.currentTimeMillis();
                    String url = thiz.settingManager.getValue(RemoteHarvesterApiClient.SETTING_REMOTE_HARVESTER_API);

                    RemoteHarvesterApiClient remoteHarvesterApiClient = new RemoteHarvesterApiClient(url);

                    boolean check = true;

                    try {
                        Thread.sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }

                    while (check) {
                        try {
                            if (thiz.cancelMonitor.get()) {
                                remoteHarvesterApiClient.abortHarvest(harvesterProcessId, log);
                                thiz.harvesterSettingsManager.setValue("harvesting/id:" + thiz.getID() + "/options/processID", "");
                                thiz.harvesterSettingsManager.setValue("harvesting/id:" + thiz.getID() + "/options/skipHarvesting", false);
                                check = false;
                                log.warning("Harvester cancelled. Stopping to set it as INACTIVE.");
                                thiz.stop(Common.Status.INACTIVE);
                                thiz.running = false;
                            } else {
                                OrchestratedHarvestProcessStatus harvesterStatus = remoteHarvesterApiClient.retrieveProgress(harvesterProcessId, log, true);

                                OrchestratedHarvestProcessState state = harvesterStatus.getOrchestratedHarvestProcessState();

                                ((CswRemoteHarvestResult) result).runningHarvest = state.equals(OrchestratedHarvestProcessState.HAVESTING);
                                ((CswRemoteHarvestResult) result).runningLinkChecker = state.equals(OrchestratedHarvestProcessState.LINKCHECKING);
                                ((CswRemoteHarvestResult) result).runningIngest = state.equals(OrchestratedHarvestProcessState.INGESTING);
                                ((CswRemoteHarvestResult) result).harvesterStatus = harvesterStatus;

                                if (!state.equals(OrchestratedHarvestProcessState.COMPLETE) &&
                                    !state.equals((OrchestratedHarvestProcessState.ERROR)) &&
                                    !state.equals((OrchestratedHarvestProcessState.USERABORT))) {
                                    try {
                                        log.info(String.format("Monitor harvester process progress (%s), state (%s):  %s" , harvesterProcessId, state.toString(), harvesterStatus.toString()));
                                        Thread.sleep(10 * 1000);
                                    } catch (InterruptedException e) {
                                        log.error(e);
                                    }
                                } else {
                                    if (state.equals(OrchestratedHarvestProcessState.ERROR)) {
                                        OrchestratedHarvestProcessStatus harvesterStatusDetailed = remoteHarvesterApiClient.retrieveProgress(harvesterProcessId, log, false);

                                        log.error(String.format("Monitor harvester process progress (%s), error: %s" , harvesterProcessId, harvesterStatusDetailed.toString()));
                                    } else {
                                        log.info(String.format("Monitor harvester process progress (%s), state (%s):  %s" , harvesterProcessId, state.toString(), harvesterStatus.toString()));
                                    }


                                    if (!state.equals(OrchestratedHarvestProcessState.ERROR) &&
                                        !state.equals(OrchestratedHarvestProcessState.USERABORT)) {
                                        thiz.log.info("Indexing metadata for harvester uuid: " + thiz.getParams().getUuid());
                                        thiz.indexHarvestedMetadata(thiz.getParams().getUuid());
                                    }

                                    log.warning("Harvester finished with state" + state.toString() + ". Stopping to set it as INACTIVE.");

                                    thiz.stop(Common.Status.INACTIVE);
                                    thiz.running = false;
                                    thiz.harvesterSettingsManager.setValue("harvesting/id:" + thiz.getID() + "/options/processID", "");

                                    // If finished successfully update lastRunSuccess
                                    if (state.equals(OrchestratedHarvestProcessState.COMPLETE)) {
                                        thiz.harvesterSettingsManager.setValue("harvesting/id:" + thiz.getID() + "/info/lastRunSuccess",
                                            thiz.harvesterSettingsManager.getValue("harvesting/id:" + thiz.getID() + "/info/lastRun"));
                                    }

                                    long elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);

                                    thiz.harvesterSettingsManager.setValue("harvesting/id:" + thiz.getID() + "/info/elapsedTime",
                                        elapsedTime);

                                    // Reset to false the skipHarvesting parameter
                                    thiz.getParams().skipHarvesting = false;

                                    thiz.harvesterSettingsManager.setValue("harvesting/id:" + thiz.getID() + "/options/skipHarvesting", false);

                                    check = false;
                                }
                            }

                        } catch (Exception ex) {
                            thiz.log.error(ex.getMessage());
                            thiz.log.error(ex);
                        }
                    }

                    final Logger logger = thiz.log;
                    final String nodeName = thiz.getParams().getName() + " (" + thiz.getClass().getSimpleName() + ")";
                    final String lastRun = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);

                    long elapsedTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);

                    logger.info("Ended harvesting from node : " + nodeName);

                    thiz.logHarvest(log.getFileAppender(), logger, nodeName, lastRun, elapsedTime);

                }
            }.start();
        }

    }

    public Element getResult() {
        Element resultEl = new Element("result");
        if (result != null) {
            CswRemoteHarvestResult r = (CswRemoteHarvestResult) result;

            resultEl.addContent(new Element("processID").setText(r.processId));
            resultEl.addContent(new Element("runningHarvest").setText(r.runningHarvest + ""));
            resultEl.addContent(new Element("runningLinkChecker").setText(((CswRemoteHarvestResult) result).runningLinkChecker + ""));
            resultEl.addContent(new Element("runningIngest").setText(((CswRemoteHarvestResult) result).runningIngest + ""));

            OrchestratedHarvestProcessStatus orchestratedHarvestProcessStatus = ((CswRemoteHarvestResult) result).harvesterStatus;

            if (orchestratedHarvestProcessStatus != null) {

                if (orchestratedHarvestProcessStatus.getHarvestStatus() != null) {
                    int total = 0;

                    if (orchestratedHarvestProcessStatus.getHarvestStatus().endpoints != null) {
                        for(int i = 0; i <  orchestratedHarvestProcessStatus.getHarvestStatus().endpoints.size(); i++) {
                            total = total +  orchestratedHarvestProcessStatus.getHarvestStatus().endpoints.get(i).numberOfRecordsReceived;
                        }
                    }
                    add(resultEl, "total", total);
                }

                resultEl.addContent(getHarvestStatusAsElement(orchestratedHarvestProcessStatus.getHarvestStatus()));
                resultEl.addContent(getLinkCheckerStatusAsElement(orchestratedHarvestProcessStatus.getLinkCheckStatus()));
                resultEl.addContent(getIngestStatusAsElement(orchestratedHarvestProcessStatus.getIngestStatus()));
            }
        }

        return resultEl;
    }


    private Element getHarvestStatusAsElement(HarvestStatus harvestStatus) {
        Element element = new Element("harvestStatus");

        if (harvestStatus != null) {
            for (EndpointStatus endpoint : harvestStatus.endpoints) {
                Element elementEp = new Element("endPoint");

                elementEp.addContent(new Element("url").setText(endpoint.url));
                elementEp.addContent(new Element("expected").setText(String.valueOf(endpoint.expectedNumberOfRecords)));
                elementEp.addContent(new Element("received").setText(String.valueOf(endpoint.numberOfRecordsReceived)));

                element.addContent(elementEp);
            }

            if (harvestStatus.errorMessage != null) {
                Element elementErrors = new Element("errors");
                for (String error : harvestStatus.errorMessage) {
                    elementErrors.addContent(new Element("error").setText(error));
                }

                element.addContent(elementErrors);
            }

        }

        return element;
    }

    private Element getLinkCheckerStatusAsElement(LinkCheckStatus linkCheckStatus) {
        Element element = new Element("linkCheckerStatus");

        if (linkCheckStatus != null) {
            DocumentTypeStatus datasets = linkCheckStatus.getDatasetRecordStatus();

            if (datasets != null) {
                Element elementDatasets = new Element("datasets");
                elementDatasets.addContent(new Element("nTotalDocuments").setText(String.valueOf(datasets.getnTotalDocuments())));

                Element elementDatasetsStatuses = new Element("statuses");

                for (StatusType statusType : datasets.getStatusTypes()) {
                    Element elementStatus = new Element("status");
                    elementStatus.addContent(new Element("type").setText(statusType.getStatusType()));
                    elementStatus.addContent(new Element("nTotalDocuments").setText(String.valueOf(statusType.getnDocuments())));

                    elementDatasetsStatuses.addContent(elementStatus);
                }

                elementDatasets.addContent(elementDatasetsStatuses);
                element.addContent(elementDatasets);
            }


            DocumentTypeStatus services = linkCheckStatus.getServiceRecordStatus();

            if (services != null) {
                Element elementServices = new Element("services");
                elementServices.addContent(new Element("nTotalDocuments").setText(String.valueOf(services.getnTotalDocuments())));

                Element elementServicesStatuses = new Element("statuses");

                for (StatusType statusType : services.getStatusTypes()) {
                    Element elementStatus = new Element("status");
                    elementStatus.addContent(new Element("type").setText(statusType.getStatusType()));
                    elementStatus.addContent(new Element("nTotalDocuments").setText(String.valueOf(statusType.getnDocuments())));

                    elementServicesStatuses.addContent(elementStatus);
                }

                elementServices.addContent(elementServicesStatuses);
                element.addContent(elementServices);
            }

            if ( linkCheckStatus.errorMessage != null) {
                Element elementErrors = new Element("errors");
                for (String error : linkCheckStatus.errorMessage) {
                    elementErrors.addContent(new Element("error").setText(error));
                }
                element.addContent(elementErrors);
            }
        }

        return element;
    }

    private Element getIngestStatusAsElement(IngestStatus ingestStatus) {
        Element element = new Element("ingestStatus");

        if (ingestStatus != null) {
            element.addContent(new Element("total").setText(String.valueOf(ingestStatus.getTotalRecords())));
            element.addContent(new Element("ingested").setText(String.valueOf(ingestStatus.getNumberOfRecordsIngested())));
            element.addContent(new Element("indexed").setText(String.valueOf(ingestStatus.getNumberOfRecordsIndexed())));
        }

        return element;
    }

    private void indexHarvestedMetadata(String harvesterUuid) throws Exception {
        EsSearchManager esSearchManager = context.getBean(EsSearchManager.class);

        this.log.info("Deleting indexed records for harvester uuid: " +harvesterUuid);

        // Delete the harvested metadata from the index
        esSearchManager.delete(Geonet.IndexFieldNames.HARVESTUUID + ": \"" + harvesterUuid + "\"");

        // Index the harvested metadata
        List<Integer> metadataIds = metadataRepository.findIdsBy(MetadataSpecs.hasHarvesterUuid(harvesterUuid));
        this.log.info("Total records to index for harvester uuid: " + harvesterUuid + ": " + metadataIds.size());

        List<String> metadataIdsToIndex = new ArrayList<>();
        int i = 1;

        Iterator<Integer> it = metadataIds.listIterator();
        while (it.hasNext()) {
            metadataIdsToIndex.add(it.next() + "");

            i++;

            if ((i == 100) || (!it.hasNext())) {
                this.log.info("Indexing " + i + " records ");
                metadataIndexer.indexMetadata(metadataIdsToIndex);

                i = 1;
                metadataIdsToIndex.clear();
            }
        }
    }

    public String getID() {
        return super.getID();
    }
}
