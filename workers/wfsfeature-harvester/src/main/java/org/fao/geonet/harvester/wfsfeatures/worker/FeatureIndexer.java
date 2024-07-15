/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.harvester.wfsfeatures.worker;

import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import jeeves.server.context.ServiceContext;
import org.apache.camel.Exchange;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.attachments.FilesystemStore;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.lib.Lib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;


public class FeatureIndexer {
    private static Logger LOGGER = LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);


    @Value("${es.index.features}")
    private String index = "features";

    @Value("${es.index.features.ogr.exec}")
    private String ogrExec = "docker";

    @Value("${es.index.features.ogr.destination.datasource}")
    private String ogrDestinationDataSource = "http://localhost:9200";

    @Value("${es.index.features.ogr.args}")
    private String ogrArgs = "run --rm -v .:/data ghcr.io/osgeo/gdal:ubuntu-full-latest ogr2ogr -progress -f \"Elasticsearch\" -lco INDEX_NAME=${indexName} ${elasticUrl} ${metadataDir}${datasource} ${layer}";

    @Autowired
    private EsRestClient client;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    private final ApplicationContext appContext = ApplicationContextHolder.get();

    private Store store;

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    @PostConstruct
    public void init() {
        if (appContext != null) {
            this.store = appContext.getBean("resourceStore", Store.class);
        }
    }

    @Autowired
    IMetadataUtils metadataUtils;

    public void setIndex(String index) {
        this.index = index;
    }

    public void setOgrExec(String ogrExec) {
        this.ogrExec = ogrExec;
    }

    public void setOgrDestinationDataSource(String ogrDestinationDataSource) {
        this.ogrDestinationDataSource = ogrDestinationDataSource;
    }

    public void setOgrArgs(String ogrArgs) {
        this.ogrArgs = ogrArgs;
    }

    /**
     * Create index name
     */
    public String buildIndexName(String url, String typeName) {
        String indexName = url + "-" + typeName;
        indexName = Normalizer.normalize(indexName.toLowerCase(), Normalizer.Form.NFD);
        return index + "-" + indexName.replaceAll("[^\\x00-\\x7F]", "")
                .replaceAll("[^a-zA-Z0-9-_]", "");
    }

    public String getCommandArgs(Exchange exchange) throws InvalidArgumentException {
        WFSHarvesterParameter configuration = (WFSHarvesterParameter) exchange.getProperty("configuration");
        if (configuration == null) {
            throw new InvalidArgumentException("Missing WFS harvester configuration.");
        }

        LOGGER.info("Build command args for uuid '{}', url '{}'," +
                "feature type '{}'. treefields are {}, tokenizedFields are {} Exchange id is '{}'.", new Object[]{
                configuration.getMetadataUuid(),
                configuration.getUrl(),
                configuration.getTypeName(),
                configuration.getTreeFields(),
                configuration.getTokenizedFields(),
                exchange.getExchangeId()});


        String url = configuration.getUrl();
        String metadataDir = "";

        String typeName = configuration.getTypeName();
        if (url.contains("/attachments/")) {

            final String uuid = configuration.getMetadataUuid();
            String metadataId = null;
            try {
                metadataId = metadataUtils.getMetadataId(uuid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Path metadataDirPath = Lib.resource.getMetadataDir(dataDirectory, metadataId);
            final Path resourceFile = metadataDirPath
                    .resolve("public")
                    .resolve(
                    url.replaceFirst(".*/attachments/(.*)", "$1"));

            if (Files.exists(resourceFile)) {
                metadataDir = metadataDirPath.toAbsolutePath().toString();
                url = "/data/" + resourceFile.getFileName().toString();
                // TODO: only works for simple datasource
                typeName = FilenameUtils.removeExtension(resourceFile.getFileName().toString());
            }
        } else {
            url = "WFS:" + url;
        }
        Map<String, String> parameters = new HashMap<>();
        parameters.put("metadataDir", metadataDir);
        parameters.put("indexName", buildIndexName(url, typeName));
        parameters.put("datasource", url);
        parameters.put("layer", typeName);
        parameters.put("elasticUrl", ogrDestinationDataSource);
        return StringSubstitutor.replace(ogrArgs, parameters);
    }

    public String getWorkingDir(Exchange exchange) {
        WFSHarvesterParameter configuration = (WFSHarvesterParameter) exchange.getProperty("configuration");
        String url = configuration.getUrl();
        if (url.contains("/attachments/")) {
            final String uuid = configuration.getMetadataUuid();
            String metadataId = null;
            try {
                metadataId = metadataUtils.getMetadataId(uuid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Path metadataDirPath = Lib.resource.getMetadataDir(dataDirectory, metadataId);
            return metadataDirPath.resolve("public").toAbsolutePath().toString();

        }
        return "/";
    }


    /**
     * Delete all documents matching a WFS server and a specific typename.
     *
     * @param exchange
     */
    public void deleteFeatures(Exchange exchange) throws InvalidArgumentException {
        WFSHarvesterParameter configuration = (WFSHarvesterParameter) exchange.getProperty("configuration");
        if (configuration == null) {
            throw new InvalidArgumentException("Missing WFS harvester configuration.");
        }

        final String url = configuration.getUrl();
        final String typeName = configuration.getTypeName();

        deleteFeatures(url, typeName, client);
    }

    public void deleteFeatures(String url, String typeName, EsRestClient client) {
        String indexName = buildIndexName(url, typeName);
        LOGGER.info("Deleting features previously index from service '{}' and feature type '{}' in index '{}'",
                new Object[]{url, typeName, indexName});
        try {
            long begin = System.currentTimeMillis();
            DeleteIndexRequest deleteIndexRequest = DeleteIndexRequest.of(
                    b -> b.index(indexName)
            );

            DeleteIndexResponse deleteIndexResponse = client.getClient().indices().delete(deleteIndexRequest);
            if (deleteIndexResponse.acknowledged()) {
                LOGGER.debug("Index '{}' removed.", indexName);
            }
            client.deleteByQuery(index, String.format("+id:\"%s\"", indexName));
            LOGGER.info("  Report and index with features deleted in {} ms.", System.currentTimeMillis() - begin);

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error connecting to ES at '{}'. Error is {}.", index, e.getMessage());
        }
    }
}
