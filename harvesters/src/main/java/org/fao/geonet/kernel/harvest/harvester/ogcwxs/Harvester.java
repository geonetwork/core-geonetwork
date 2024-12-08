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

package org.fao.geonet.kernel.harvest.harvester.ogcwxs;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.api.records.attachments.FilesystemStore;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.kernel.schema.ISOPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.utils.AbstractHttpRequest.Method.GET;


/**
 * A OgcWxSHarvester is able to generate metadata for data and service from a GetCapabilities
 * documents. Metadata for layers are generated using layer information contained in the
 * GetCapabilities document or using a xml document pointed by the metadataUrl attribute of layer
 * element.
 *
 * OGC services supported are : <ul> <li>WMS</li> <li>WFS</li> <li>WCS</li> <li>WPS</li>
 * <li>SOS</li> </ul>
 *
 * Metadata produced are : <ul>
 *     <li>ISO19119 for service's metadata</li>
 *     <li>ISO19139 for data's metadata</li>
 *     <li>ISO19115-3 only if using template mode</li>
 *     </ul>
 *
 * Note : Layer stands for "Layer" for WMS, "FeatureType" for WFS and "Coverage" for WCS.
 *
 * <pre>
 * <nodes>
 *  <node type="ogcwxs" id="113">
 *    <site>
 *      <name>TEST</name>
 *      <uuid>c1da2928-c866-49fd-adde-466fe36d3508</uuid>
 *      <account>
 *        <use>true</use>
 *        <username />
 *        <password />
 *      </account>
 *      <url>http://localhost:8080/geoserver/wms</url>
 *      <ogctype>WMS111</ogctype>
 *      <icon>default.gif</icon>
 *    </site>
 *    <options>
 *      <every>90</every>
 *      <oneRunOnly>false</oneRunOnly>
 *      <status>active</status>
 *      <lang>eng</lang>
 *      <useLayer>true</useLayer>
 *      <useLayerMd>false</useLayerMd>
 *      <datasetCategory></datasetCategory>
 *    </options>
 *    <privileges>
 *      <group id="1">
 *        <operation name="view" />
 *      </group>
 *    </privileges>
 *    <categories>
 *      <category id="3" />
 *    </categories>
 *    <info>
 *      <lastRun>2007-12-05T16:17:20</lastRun>
 *      <running>false</running>
 *    </info>
 *  </node>
 * </nodes>
 * </pre>
 *
 * @author fxprunayre
 */
class Harvester extends BaseAligner<OgcWxSParams> implements IHarvester<HarvestResult> {


    private static final int WIDTH = 900;
    private static final String GETCAPABILITIES = "GetCapabilities";
    private static final String GETMAP = "GetMap";
    private static final String IMAGE_FORMAT = "image/png";
    private Logger log;
    private ServiceContext context;
    private DataManager dataMan;
    private IMetadataManager metadataManager;
    private SchemaManager schemaMan;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private HarvestResult result;

    /**
     * Store the GetCapabilities operation URL. This URL is scrambled and used to uniquelly
     * identified the service. The idea of generating a uuid based on the URL instead of a
     * randomuuid is to be able later to do an update of the service metadata (which could have been
     * updated in the catalogue) instead of a delete/insert operation.
     */
    private String capabilitiesUrl;
    private List<WxSLayerRegistry> layersRegistry = new ArrayList<WxSLayerRegistry>();

    /**
     * Constructor
     *
     * @param context Jeeves context
     * @param params  Information about harvesting configuration for the node
     * @return null
     */
    public Harvester(AtomicBoolean cancelMonitor, Logger log,
                     ServiceContext context,
                     OgcWxSParams params) {
        super(cancelMonitor);
        this.log = log;
        this.context = context;
        this.params = params;

        result = new HarvestResult();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        schemaMan = gc.getBean(SchemaManager.class);
        metadataManager = gc.getBean(IMetadataManager.class);
    }

    /**
     * Start the harvesting of a WMS, WFS or WCS node.
     */
    public HarvestResult harvest(Logger log) throws Exception {
        Element xml;

        this.log = log;

        log.info("Retrieving remote metadata information for : " + params.getName());

        // Clean all before harvest : Remove/Add mechanism
        // If harvest failed (ie. if node unreachable), metadata will be removed, and
        // the node will not be referenced in the catalogue until next harvesting.
        UUIDMapper localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());


        // Try to load capabilities document
        String serviceType = params.ogctype.replaceAll("[0-9]+.?", "");
        String version = params.ogctype.replaceAll("[A-Z]+", "");
        boolean isSos = "SOS".equals(serviceType);
        this.capabilitiesUrl = getBaseUrl(params.url) +
            "SERVICE=" +  serviceType +
            (isSos ? "" : ("&VERSION=" + version)) +
            "&REQUEST=" + GETCAPABILITIES
        ;

        if (log.isDebugEnabled()) {
            log.debug("GetCapabilities document: " + this.capabilitiesUrl);
        }

        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest();
        req.setUrl(new URL(this.capabilitiesUrl));
        req.setMethod(GET);
        Lib.net.setupProxy(context, req);

        if (params.isUseAccount()) {
            req.setCredentials(params.getUsername(), params.getPassword());
        }

        xml = req.execute();

        // Convert from GetCapabilities to ISO19119
        List<String> uuids = addMetadata(xml);
        metadataManager.flush();

        List<String> ids = Lists.transform(uuids, new Function<String, String>() {
            @Nullable
            @Override
            public String apply(String uuid) {
                try {
                    return dataMan.getMetadataId(uuid);
                } catch (Exception e) {
                    return null;
                }
            }
        });

        dataMan.batchIndexInThreadPool(context, ids);

        result.totalMetadata = result.addedMetadata + result.updatedMetadata;
        Store store = context.getBean("resourceStore", Store.class);

        //-----------------------------------------------------------------------
        //--- remove old metadata
        for (String uuid : localUuids.getUUIDs()) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            //If it was not on the uuids added:
            if (!uuids.contains(uuid)) {
                String id = localUuids.getID(uuid);

                if (log.isDebugEnabled())
                    log.debug("  - Removing old metadata before update with id: " + id);

                //--- remove the metadata directory including the public and private directories.
                store.delResources(context, uuid);

                // Remove metadata
                metadataManager.deleteMetadata(context, id);

                result.locallyRemoved++;
            }
        }


        if (result.locallyRemoved > 0) {
            metadataManager.flush();
        }

        return result;
    }

    /**
     * Add metadata to the node for a WxS service
     *
     * 1.Use GetCapabilities Document 2.Transform using XSLT to iso19119 3.Loop through layers
     * 4.Create md for layer 5.Add operatesOn elem with uuid 6.Save all
     *
     * @param capa GetCapabilities document
     */
    private List<String> addMetadata(Element capa) throws Exception {
        if (capa == null) {
            return Collections.<String>emptyList();
        }

        List<String> uuids = new LinkedList<String>();

        //--- Loading categories and groups
        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);

        // md5 the full capabilities URL
        String uuid = Sha1Encoder.encodeString(capabilitiesUrl); // is the service identifier

        // Metadata creation could be based on 2 scenarios:
        // 1. Use the GetCapabilities and process it using XSLT to create ISO records
        // 2. Use existing metadata templates and merge GetCapabilities information into those templates to build new metadata records.

        boolean usingTemplate = StringUtils.isNotBlank(params.serviceTemplateUuid);

        Element md = null;

        Map<String, Object> xsltParams = new HashMap<String, Object>();
        xsltParams.put("lang", params.lang);
        xsltParams.put("topic", params.topic);
        xsltParams.put("uuid", uuid);

        if (usingTemplate) {
            md = buildRecordFromTemplateOrExisting(uuid, xsltParams, capa, params.serviceTemplateUuid);
        } else {
            md = buildServiceRecordFromCapabilities(xsltParams, capa);
        }


        String schema = dataMan.autodetectSchema(md, null);
        if (schema == null) {
            log.warning("Skipping metadata with unknown schema.");
            result.unknownSchema++;
        }


        //--- Create metadata for layers only if user ask for
        if (params.useLayer || params.useLayerMd) {
            // Load CRS
            // TODO

            //--- Select layers, featureTypes and Coverages
            // (for layers having no child named layer = not take group of layer into account)
            // and add the metadata
            XPath xp = XPath.newInstance("//Layer[count(./*[name(.)='Layer'])=0] | " +
                "//wms:Layer[count(./*[name(.)='Layer'])=0] | " +
                "//wmts:Layer[count(./*[local-name(.)='Layer'])=0] | " +
                "//wfs:FeatureType | " +
                "//wcs:CoverageOfferingBrief | " +
                "//sos:ObservationOffering | " +
                "//wps:ProcessSummary");
            xp.addNamespace("wfs", "http://www.opengis.net/wfs");
            xp.addNamespace("wcs", "http://www.opengis.net/wcs");
            xp.addNamespace("wms", "http://www.opengis.net/wms");
            xp.addNamespace("wmts", "http://www.opengis.net/wmts/1.0");
            xp.addNamespace("wps", "http://www.opengis.net/wps/2.0");
            xp.addNamespace("sos", "http://www.opengis.net/sos/1.0");

            @SuppressWarnings("unchecked")
            // Add capabilities to a new document as it may
            // have been cloned in buildRecord (and cloning remove parent
            // and make xpath evaluation to return empty results)
            Document document = new Document();
            Element clone = (Element) capa.clone();
            document.addContent(clone);
            List<Element> layers = xp.selectNodes(document);
            if (layers.size() > 0) {
                log.info("  - Number of layers, featureTypes, Coverages or process found : " + layers.size());

                for (Element layer : layers) {
                    WxSLayerRegistry s = addLayerMetadata(layer, capa);
                    if (s != null) {
                        uuids.add(s.uuid);
                        layersRegistry.add(s);
                    }
                }

                // Update ISO19119 for data/service links creation (ie. operatesOn element)
                // The editor will support that but it will make quite heavy XML.
                md = addOperatesOnUuid(md, schema, layersRegistry);
            }
        }

        // Apply custom transformation if requested
        Path importXsl;
        String importXslFile = params.getImportXslt();
        if (importXslFile != null && !importXslFile.equals("none")) {
            importXsl = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class)
                .getXsltConversion(importXslFile);
            log.info("Applying custom import XSL " + importXsl.getFileName());
            md = Xml.transform(md, importXsl);
            schema = dataMan.autodetectSchema(md, null);
        }


        // Save iso19119 metadata in DB
        log.info("  - Adding metadata for services with " + uuid);

        //
        // insert metadata
        //
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.METADATA);
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(getOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid()).
            setUri(params.url);

        try {
            metadata.getSourceInfo().setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        } catch (NumberFormatException e) {
        }

        addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        if (!dataMan.existsMetadataUuid(uuid)) {
            result.addedMetadata++;
            metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, false, UpdateDatestamp.NO, false, false);
        } else {
            result.updatedMetadata++;
            String id = dataMan.getMetadataId(uuid);
            metadata.setId(Integer.valueOf(id));
            metadataManager.updateMetadata(context, id, md, false, false,
                context.getLanguage(), dataMan.extractDateModified(schema, md), false, IndexingMode.none);
        }

        String id = String.valueOf(metadata.getId());
        uuids.add(uuid);

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        return uuids;
    }

    private Element buildServiceRecordFromCapabilities(Map<String, Object> xsltParams, Element capa) throws Exception {
        Element md;

        //--- Loading stylesheet
        Path styleSheet = schemaMan.getSchemaDir(params.outputSchema).
            resolve(Geonet.Path.CONVERT_STYLESHEETS).
            resolve("OGCWxSGetCapabilitiesto19119").
            resolve("OGC" + params.ogctype.substring(0, 3) + "GetCapabilities-to-ISO19119_ISO19139.xsl");

        if (log.isDebugEnabled()) log.debug("  - XSLT transformation using " + styleSheet);

        try {
            md = Xml.transform(capa, styleSheet, xsltParams);
        } catch (Exception e) {
            String message = String.format(
                "Failed to convert GetCapabilities '%s' to metadata record. Error is: '%s'. Service response is: %s.",
                this.capabilitiesUrl, e.getMessage(), Xml.getString(capa));
            log.error(message);
            throw new IllegalStateException(message, e);
        }
        return md;
    }

    private Element buildRecordFromTemplateOrExisting(String uuid, Map<String, Object> xsltParams, Element capa, String templateUuid) throws Exception {
        // Check first that this record has not been generated in the past.
        // The UUID is a hash of the capabilities URL
        MetadataRepository metadataRepository = ApplicationContextHolder.get().getBean(MetadataRepository.class);
        Metadata existingRecord = metadataRepository.findOneByUuid(uuid);
        Element existingRecordXml;
        Element md;
        String schema = null;

        if (existingRecord != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("  - Building record from existing one %s.", uuid));
            }
            // Use record generated on previous run
            existingRecordXml = existingRecord.getXmlData(false);
            schema = existingRecord.getDataInfo().getSchemaId();
        } else {
            // Search the template in the catalogue
            // Return an exception if not found.
            String templateId = dataMan.getMetadataId(templateUuid);
            if (templateId == null) {
                String message = String.format(
                    "Template with UUID '%s' not found in the catalogue. Choose another template.",
                    templateUuid);
                log.error(message);
                throw new IllegalStateException(message);
            }

            // Use the template as a basis
            existingRecordXml = metadataManager.getMetadata(templateId);
            schema = dataMan.getMetadataSchema(templateId);
        }

        Element record = new Element("record");
        record.addContent(existingRecordXml);


        Element getCapabilities = new Element("getCapabilities");
        Element clone = (Element) capa.clone();
        getCapabilities.addContent(clone);



        Element root = new Element("root");
        root.addContent(record);
        root.addContent(getCapabilities);

        Path styleSheet = schemaMan.getSchemaDir(schema).
            resolve(Geonet.Path.CONVERT_STYLESHEETS).
            resolve("ogcwxs-info-injection.xsl");

        if (!Files.exists(styleSheet)) {
            String message = String.format(
                "Transformation does not exist for this template schema '%s'. Choose another template or create the XSLT in %s/convert/ogcwxs-info-injection.xsl.",
                schema, schema);
            log.error(message);
            throw new IllegalStateException(message);
        }

        try {
            md = Xml.transform(root, styleSheet, xsltParams);
        } catch (IllegalStateException e) {
            String message = String.format(
                "Failed to inject GetCapabilities '%s' to service template. Error is: '%s'. Service response is: %s.",
                this.capabilitiesUrl, e.getMessage(), Xml.getString(capa));
            log.error(message);
            throw new IllegalStateException(message, e);
        }
        return md;
    }

    /**
     * Add OperatesOn elements on an ISO19119 metadata
     *
     * <srv:operatesOn> <gmd:MD_DataIdentification uuidref=""/> </srv:operatesOn>
     *
     * @param md             iso19119 metadata
     * @param layersRegistry uuid to be added as an uuidref attribute
     */
    private Element addOperatesOnUuid(Element md, String schema, List<WxSLayerRegistry> layersRegistry) {

        Map<String, String> layers = new HashMap<>();
        for (WxSLayerRegistry wxSLayerRegistry : layersRegistry) {
            layers.put(wxSLayerRegistry.uuid, wxSLayerRegistry.name);
        }

        SchemaPlugin plugin = dataMan.getSchema(schema).getSchemaPlugin();
        boolean isISOPlugin = plugin instanceof ISOPlugin;

        if (isISOPlugin) {
            if (log.isDebugEnabled()) {
                log.debug("  - add SV_CoupledResource and OperatesOnUuid to service metadata record.");
            }
            ((ISOPlugin) plugin).addOperatesOn(md, layers, params.ogctype,
                context.getBean(SettingManager.class).getNodeURL());
        } else {
            if (log.isDebugEnabled()) {
                log.debug("  - Can't add SV_CoupledResource and OperatesOnUuid to service metadata record. This is not an ISOPlugin record.");
            }
        }
        return md;
    }

    /**
     * Add metadata for a Layer/FeatureType/Coverage element of a GetCapabilities document. This
     * function search for a metadataUrl element (with @type = TC211 and format = text/xml) and try
     * to load the XML document. If failed, then an XSLT is used for creating metadata from the
     * Layer/FeatureType/Coverage element. If loaded document contain an existing uuid, metadata
     * will not be loaded in the catalogue.
     *
     * @param layer Layer/FeatureType/Coverage element
     * @param capa  GetCapabilities document
     * @return uuid
     */
    private WxSLayerRegistry addLayerMetadata(Element layer, Element capa) throws JDOMException {

        WxSLayerRegistry reg = new WxSLayerRegistry();
        String schema;
        String mdXml;
        Element xml = null;

        boolean exist;
        boolean loaded = false;
        String ogcType = params.ogctype.replaceAll("[0-9]+.?", "");

        if (ogcType.equals("WMS")) {
            Element name;
            if (params.ogctype.substring(3, 8).equals("1.3.0")) {
                Namespace wms = Namespace.getNamespace("http://www.opengis.net/wms");
                name = layer.getChild("Name", wms);
            } else {
                name = layer.getChild("Name");
            }
            //--- For the moment, skip non-requestable category layers
            if (name == null || name.getValue().trim().equals("")) {
                log.info("  - skipping layer with no name element");
                return null;
            }
            reg.name = name.getValue();
        } else if (ogcType.equals("WFS")) {
            Namespace wfs = Namespace.getNamespace("http://www.opengis.net/wfs");
            reg.name = layer.getChild("Name", wfs).getValue();
        } else if (ogcType.equals("WCS")) {
            Namespace wcs = Namespace.getNamespace("http://www.opengis.net/wcs");
            reg.name = layer.getChild("name", wcs).getValue();
        } else if (ogcType.equals("WPS")) {
            Namespace ows = Namespace.getNamespace("http://www.opengis.net/ows/2.0");
            reg.name = layer.getChild("Identifier", ows).getValue();
        } else if (ogcType.equals("SOS")) {
            Namespace gml = Namespace.getNamespace("http://www.opengis.net/gml");
            reg.name = layer.getChild("name", gml).getValue();
        } else if (ogcType.equals("WMTS")) {
            Namespace ows = Namespace.getNamespace("http://www.opengis.net/ows/1.1");
            reg.name = layer.getChild("Identifier", ows).getValue();
        }

        //--- md5 the full capabilities URL + the layer, coverage or feature name
        reg.uuid = Sha1Encoder.encodeString(this.params.url + "#" + reg.name); // the dataset identifier

        log.info("  - Loading layer: " + reg.name + " with UUID " + reg.uuid + " (hash of capabilities URL + layer name).");

        if (params.useLayerMd && (
            ogcType.equals("WMS") ||
            ogcType.equals("WFS") ||
            ogcType.equals("WPS") ||
            ogcType.equals("WCS"))) {

            log.info("  - Searching for metadataUrl for layer " + reg.name);

            Namespace xlink = Namespace.getNamespace("http://www.w3.org/1999/xlink");

            // Get metadataUrl xlink:href
            // Check if add namespace prefix to Xpath queries.  If layer.getNamespace() is:
            //    * Namespace.NO_NAMESPACE, should not be added, otherwise exception is launched
            //    * Another namespace, should be added a namespace prefix to Xpath queries, otherwise doesn't find any result
            String dummyNsPrefix = "";
            boolean addNsPrefix = !layer.getNamespace().equals(Namespace.NO_NAMESPACE);
            if (addNsPrefix) dummyNsPrefix = "x:";

            Element onLineSrc = null;
            XPath mdUrlXpath = null;
            mdXml = null;
            if (params.ogctype.startsWith("WFS1")) {
                mdUrlXpath = XPath.newInstance(
                    "./" + dummyNsPrefix + "MetadataURL[" +
                        "@type='TC211' and (@format='XML' or @format='text/xml')" +
                        "]");
                if (addNsPrefix) {
                    mdUrlXpath.addNamespace("x", layer.getNamespace().getURI());
                }
                onLineSrc = (Element) mdUrlXpath.selectSingleNode(layer);
                if (onLineSrc != null) {
                    mdXml = onLineSrc.getText();
                }
            } else if (params.ogctype.startsWith("WPS")) {
//                <wps:ProcessSummary processVersion="" jobControlOptions="async-execute dismiss" outputTransmission="value reference">
//                  <ows:Title>Nadira service (B1)</ows:Title>
//                  <ows:Identifier>ndrserviceb1</ows:Identifier>
//                  <ows:Metadata xlin:role="Process description" xlin:href="http://35.195.144.11:80/WPS/WebProcessingService?service=WPS&amp;request=DescribeProcess&amp;version=2.0.0&amp;identifier=ndrserviceb1"/>
//                </wps:ProcessSummary>
                Namespace ows = Namespace.getNamespace("http://www.opengis.net/ows/2.0");
                Element metadata = layer.getChild("Metadata", ows);
                if (metadata != null) {
                    org.jdom.Attribute href = metadata.getAttribute("href", xlink);
                    if (href != null) {
                        mdXml = href.getValue();
                    }
                }
            } else {
                mdUrlXpath = XPath.newInstance(
                    "./" + dummyNsPrefix + "MetadataURL[" +
                        "(@type='TC211' or @type='ISO19115:2003') and " +
                        dummyNsPrefix + "Format='text/xml'" +
                        "]/" + dummyNsPrefix + "OnlineResource");
                if (addNsPrefix) {
                    mdUrlXpath.addNamespace("x", layer.getNamespace().getURI());
                }
                onLineSrc = (Element) mdUrlXpath.selectSingleNode(layer);
                if (onLineSrc != null) {
                    org.jdom.Attribute href = onLineSrc.getAttribute("href", xlink);
                    if (href != null) {
                        mdXml = href.getValue();
                    }
                }
            }

            if (mdXml != null) {    // No metadataUrl attribute for that layer
                try {
                    xml = Xml.loadFile(new URL(mdXml));
                    boolean isUsingTemplate = StringUtils.isNotEmpty(params.datasetTemplateUuid);

                    // If url is CSW GetRecordById remove envelope
                    if (xml.getName().equals("GetRecordByIdResponse")) {
                        xml = (Element) xml.getChildren().get(0);
                    } else if (xml.getName().equals("ProcessOfferings")) {
                        // Convert WPS process metadata to ISO record

                        Map<String, Object> xsltParams = new HashMap<String, Object>();
                        xsltParams.put("lang", params.lang);
                        xsltParams.put("topic", params.topic);
                        xsltParams.put("Name", reg.name);
                        xsltParams.put("serviceType", params.ogctype.substring(0, 3));
                        xsltParams.put("uuid", reg.uuid);

                        if (isUsingTemplate) {
                            xml = buildRecordFromTemplateOrExisting(reg.uuid, xsltParams, xml, params.datasetTemplateUuid);
                        } else {
                            log.warning(String.format(
                                "    Building record for WPS process is only supported when a template for the layer is defined. Choose a template. Process '%s' was not converted to metadata record.",
                                reg.name));
                        }
                    }

                    schema = dataMan.autodetectSchema(xml, null);

                    // Extract uuid from loaded xml document
                    // FIXME : uuid could be duplicate if metadata already exist in catalog
                    reg.uuid = dataMan.extractUUID(schema, xml);
                    exist = dataMan.existsMetadataUuid(reg.uuid);

                    // Overwrite always when using template
                    if (exist && !isUsingTemplate) {
                        log.warning(String.format(
                            "    Metadata uuid '%s' already exist in the catalogue. Metadata for layer '%s' will not be loaded.",
                            reg.uuid, reg.name));
                        result.layerUuidExist++;
                        // Return the layer info even if it exists in order
                        // to link to the service record.
                        return reg;
                    }

                    if (schema == null) {
                        log.warning(String.format(
                            "    Failed to detect schema from metadataUrl '%s' file.",
                            mdXml));
                        result.unknownSchema++;
                        loaded = false;
                    } else {
                        log.info(String.format(
                            "  - MetadataUrl document '%s' for layer '%s' accepted.",
                            mdXml, reg.name));

                        loaded = true;
                        result.layerUsingMdUrl++;
                    }
                    // TODO : catch other exception
                } catch (Exception e) {
                    log.warning(String.format(
                        "  - Failed to load layer using metadataUrl attribute '%s'. Error is: '%s'",
                        mdXml, e.getMessage()
                    ));
                    loaded = false;
                }
            } else {
                log.info(String.format("  - No metadataUrl attribute found for layer '%s'", reg.name));
                loaded = false;
            }
        }


        //--- using GetCapabilities document
        if (!loaded && params.useLayer) {
            try {
                //--- set XSL param to filter on layer and set uuid
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("uuid", reg.uuid);
                param.put("Name", reg.name);
                param.put("serviceType", params.ogctype.substring(0, 3));
                param.put("lang", params.lang);
                param.put("topic", params.topic);

                boolean isUsingTemplate = StringUtils.isNotEmpty(params.datasetTemplateUuid);
                if (isUsingTemplate) {
                    xml = buildRecordFromTemplateOrExisting(reg.uuid, param, capa, params.datasetTemplateUuid);
                    if (log.isDebugEnabled()) {
                        log.debug("  - Dataset record built using GetCapabilities and template.");
                    }
                } else {
                    //--- Loading stylesheet
                    Path styleSheet = schemaMan.getSchemaDir(params.outputSchema).
                        resolve(Geonet.Path.CONVERT_STYLESHEETS).
                        resolve("OGCWxSGetCapabilitiesto19119").
                        resolve("OGC" + params.ogctype.substring(0, 3) + "GetCapabilitiesLayer-to-19139.xsl");

                    xml = Xml.transform(capa, styleSheet, param);

                    // Apply custom transformation if requested
                    Path importXsl;
                    String importXslFile = params.getImportXslt();
                    if (importXslFile != null && !importXslFile.equals("none")) {
                        importXsl = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class)
                            .getXsltConversion(importXslFile);
                        log.info("Applying custom import XSL " + importXsl.getFileName());
                        xml = Xml.transform(xml, importXsl);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("  - Layer loaded using GetCapabilities document.");
                    }
                }

            } catch (Exception e) {
                log.warning("  - Failed to do XSLT transformation on Layer element : " + e.getMessage());
            }
        }


        // Insert in db
        try {

            //
            //  insert metadata
            //
            schema = dataMan.autodetectSchema(xml);
            AbstractMetadata metadata = new Metadata();
            metadata.setUuid(reg.uuid);
            metadata.getDataInfo().
                setSchemaId(schema).
                setRoot(xml.getQualifiedName()).
                setType(MetadataType.METADATA);
            metadata.getSourceInfo().
                setSourceId(params.getUuid()).
                setOwner(Integer.parseInt(
                        StringUtils.isNotEmpty(params.getOwnerIdUser()) && StringUtils.isNumeric(params.getOwnerIdUser()) ?
                            params.getOwnerIdUser() : params.getOwnerId()));
            metadata.getHarvestInfo().
                setHarvested(true).
                setUuid(params.getUuid()).
                setUri(params.url);
            if (params.datasetCategory != null && !params.datasetCategory.equals("")) {
                MetadataCategory metadataCategory = context.getBean(MetadataCategoryRepository.class).findById(Integer.parseInt(params.datasetCategory)).get();

                if (metadataCategory == null) {
                    throw new IllegalArgumentException("No category found with name: " + params.datasetCategory);
                }
                metadata.getCategories().add(metadataCategory);
            }
            if (!dataMan.existsMetadataUuid(reg.uuid)) {
                result.addedMetadata++;
                metadata = metadataManager.insertMetadata(context, metadata, xml, IndexingMode.none, false, UpdateDatestamp.NO, false, false);
            } else {
                result.updatedMetadata++;
                String id = dataMan.getMetadataId(reg.uuid);
                metadata.setId(Integer.valueOf(id));
                metadataManager.updateMetadata(context, id, xml, false, false,
                    context.getLanguage(), dataMan.extractDateModified(schema, xml), false, IndexingMode.none);
            }

            reg.id = String.valueOf(metadata.getId());

            if (log.isDebugEnabled()) log.debug("    - Layer loaded in DB.");

            if (log.isDebugEnabled()) log.debug("    - Set Privileges and category.");
            addPrivileges(reg.id, params.getPrivileges(), localGroups, context);

            if (log.isDebugEnabled()) log.debug("    - Set Harvested.");

            try {
                // Load bbox info for WMS thumbnails creation
                SchemaPlugin plugin = dataMan.getSchema(schema).getSchemaPlugin();
                boolean isISOPlugin = plugin instanceof ISOPlugin;

                if (params.ogctype.startsWith("WMS") && params.createThumbnails && isISOPlugin) {
                    List<ISOPlugin.Extent> extents = ((ISOPlugin) plugin).getExtents(xml);
                    if (extents.size() > 0) {
                        reg.minx = extents.get(0).xmin;
                        reg.maxx = extents.get(0).xmax;
                        reg.miny = extents.get(0).ymin;
                        reg.maxy = extents.get(0).ymax;
                    } else {
                        log.warning("  - Failed to extract layer bbox from metadata. It looks to be null.");
                    }

                    xml = loadThumbnail(reg, xml, schema);
                    if (xml != null) {
                        metadataManager.updateMetadata(context, reg.id, xml,
                            false, false,
                            context.getLanguage(),
                            dataMan.extractDateModified(schema, xml), false, IndexingMode.none);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("  - Can't get bounding boxes for that record. This is not an ISOPlugin record.");
                    }
                }
            } catch (Exception e) {
                log.warning("  - Failed to extract layer bbox from metadata : " + e.getMessage());
            }

            result.layer++;
            log.info("  - metadata loaded with uuid: " + reg.uuid + "/internal id: " + reg.id);

        } catch (Exception e) {
            log.warning(String.format(
                "  - Failed to load metadata document for layer '%s'. Error is: '%s',",
                reg.name, e.getMessage()));
            result.unretrievable++;
            return null;
        }

        return reg;
    }

    /**
     * @param layer layer for which the thumbnail needs to be generated
     * @param schema
     * @return The thumbnail URL or an empty string
     */
    private Element loadThumbnail(WxSLayerRegistry layer, Element xml, String schema) {
        if (log.isDebugEnabled())
            log.debug("  - Creating thumbnail for layer metadata: " + layer.name + " id: " + layer.id);

        try {
            Path filename = getMapThumbnail(layer);

            // Add downloaded file to metadata store
            Store store = context.getBean("filesystemStore", Store.class);
            try {
                store.delResource(context, layer.uuid, filename.getFileName().toString());
            } catch (Exception e) {}
            MetadataResource resource = store.putResource(context, layer.uuid,
                filename,
                MetadataResourceVisibility.PUBLIC);
            Path xslProcessing = schemaMan
                .getSchemaDir(schema).resolve("process")
                .resolve("thumbnail-add.xsl");
            if (Files.exists(xslProcessing)) {
                Map<String, Object> params = new HashMap<>();
                params.put("thumbnail_url", resource.getUrl());
                xml = Xml.transform(xml, xslProcessing, params);
                // Add overview URL in record
                result.thumbnails++;
                return xml;
            }
            return null;
        } catch (Exception e) {
            log.warning("  - Failed to set thumbnail for metadata: " + e.getMessage());
            log.error(e);
            result.thumbnailsFailed++;
        }
        return null;
    }

    /**
     * Load thumbnails making a GetMap operation. Width is 300px. Ratio is computed for height using
     * LatLongBoundingBoxElement.
     *
     * @param layer layer for which the thumbnail needs to be generated
     */
    private Path getMapThumbnail(WxSLayerRegistry layer) {
        String filename = layer.uuid + ".png";
        Path file = context.getUploadDir().resolve(filename);
        Double r = WIDTH /
            (layer.maxx - layer.minx) *
            (layer.maxy - layer.miny);


        // Usual GetMap url tested with mapserver and geoserver
        // http://localhost:8080/geoserver/wms?service=WMS&request=GetMap&VERSION=1.1.1&
        // 		LAYERS=gn:world&WIDTH=200&HEIGHT=200&FORMAT=image/png&BBOX=-180,-90,180,90&STYLES=
        String crsParamName;
        String bboxParamValue;
        if (params.ogctype.substring(3).equals("1.3.0")) {
            crsParamName = "CRS";
            bboxParamValue = layer.miny + "," +
                layer.minx + "," +
                layer.maxy + "," +
                layer.maxx;
        } else {
            crsParamName = "SRS";
            bboxParamValue = layer.minx + "," +
                layer.miny + "," +
                layer.maxx + "," +
                layer.maxy;
        }
        String url =
            getBaseUrl(params.url) +
                "&SERVICE=" + params.ogctype.substring(0, 3) +
                "&VERSION=" + params.ogctype.substring(3) +
                "&REQUEST=" + GETMAP +
                "&FORMAT=" + IMAGE_FORMAT +
                "&WIDTH=" + WIDTH +
                "&" + crsParamName + "=EPSG:4326" +
                "&HEIGHT=" + r.intValue() +
                "&LAYERS=" + layer.name +
                "&STYLES=" +
                "&BBOX=" + bboxParamValue;

        log.info("Retrieving thumbnail from URL: " + url);

        HttpGet req = new HttpGet(url);


        try {
            // Connect
            final GeonetHttpRequestFactory requestFactory = context.getBean(GeonetHttpRequestFactory.class);
            final String requestHost = req.getURI().getHost();
            final ClientHttpResponse httpResponse = requestFactory.execute(req, new Function<HttpClientBuilder, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable HttpClientBuilder input) {
                    // set proxy from settings manager
                    Lib.net.setupProxy(context, input, requestHost);
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });

            if (log.isDebugEnabled()) {
                log.debug("   Get " + httpResponse.getStatusCode());
            }

            if (httpResponse.getStatusCode() == HttpStatus.OK) {
                // Save image document to temp directory
                // TODO: Check OGC exception

                try (OutputStream fo = Files.newOutputStream(file);
                     InputStream in = httpResponse.getBody()) {
                    BinaryFile.copy(in, fo);
                }
            } else {
                log.info(" Http error connecting");
                return null;
            }
        } catch (IOException ioe) {
            log.info(" Unable to connect to '" + req.toString() + "'");
            log.info(ioe.getMessage());
            return null;
        } finally {
            // Release current connection to the connection pool once you are done
            req.releaseConnection();
        }

        return file;
    }

    /**
     * Add '?' or '&' if required to url so that parameters can just be appended to it
     *
     * @param url Url to which parameters are going to be appended
     */
    private String getBaseUrl(String url) {
        if (url.endsWith("?")) {
            return url;
        } else if (url.contains("?")) {
            return url + "&";
        } else {
            return url + "?";
        }
    }

    /* (non-Javadoc)
     * @see org.fao.geonet.kernel.harvest.harvester.IHarvester#getErrors()
     */
    @Override
    public List<HarvestError> getErrors() {
        return new ArrayList<>();
    }

    private static class WxSLayerRegistry {
        public String uuid;
        public String id;
        public String name;
        public Double minx = -180.0;
        public Double miny = -90.0;
        public Double maxx = 180.0;
        public Double maxy = 90.0;
    }

}
