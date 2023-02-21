//=============================================================================
//===	Copyright (C) 2001-2017 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.thredds;

import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.BadServerCertificateEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UriMapper;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvService;
import thredds.catalog.ThreddsMetadata;
import thredds.catalog.ServiceType;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.util.StringUtil2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLHandshakeException;

//=============================================================================

/**
 * A ThreddsHarvester is able to generate metadata for datasets and services from a Thredds
 * catalogue. Thredds datasets can be laid out in one of two ways:
 *
 * 1. by variable: individual datasets contain a single variable. If one directory for each variable, then thredds
 * catalog metadata could be held about each variable at the directory level. Otherwise all datasets have to be
 * traversed and metadata collected.
 *
 * 2. by time/geospatial domain: could have thredds catalog metadata in the top directory or any sub directories
 * This harvester can be pointed at the directory
 * and it will query each dataset using WMS to retrieve the spatial and temporal extents plus variables
 * (each one is a wms layer). A single metadata record with these variables and the union of the extents will
 * be then created by running an XSLT on the information collected from the datasets in the directory.
 *
 * Metadata produced are : <ul> <li>ISO19119 for service metadata (all services in the catalog)</li>
 * <li>ISO19139 (or profile) metadata for the directory</li> </ul>
 *
 * <pre>
 * <nodes>
 *  <node type="thredds" id="114">
 *    <site>
 *      <name>TEST</name>
 *      <uuid>c1da2928-c866-49fd-adde-466fe36d3508</uuid>
 *      <url>http://opendap.bom.gov.au:8080/thredds/catalog/bmrc/access-r-fc/ops/surface/catalog.xml</url>
 *      <icon>thredds.gif</icon>
 *    </site>
 *    <options>
 *      <every>90</every>
 *      <oneRunOnly>false</oneRunOnly>
 *      <status>active</status>
 *      <lang>eng</lang>
 *      <createServiceMd>true</createServiceMd>
 *      <outputSchema>iso19139.mcp</outputSchema>
 *      <datasetCategory>datasets</datasetCategory>
 *      <serviceCategory>services</serviceCategory>
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
 * @author Simon Pigot
 */
class Harvester extends BaseAligner<ThreddsParams> implements IHarvester<HarvestResult> {


		// Namespaces needed here....

    static private final Namespace invCatalog = Namespace.getNamespace("http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0");
    static private final Namespace wms = Namespace.getNamespace("http://www.opengis.net/wms");
    static private final Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    static private final Namespace srv = Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
    static private final Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

		static private final Namespace xsi = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
		static private final Namespace gco = Namespace.getNamespace("gco","http://www.isotc211.org/2005/gco");
    static private final Namespace gmi = Namespace.getNamespace("gmi","http://www.isotc211.org/2005/gmi");
    static private final Namespace gmx = Namespace.getNamespace("gmx","http://www.isotc211.org/2005/gmx");
    static private final Namespace gsr = Namespace.getNamespace("gsr","http://www.isotc211.org/2005/gsr");
    static private final Namespace gss = Namespace.getNamespace("gss","http://www.isotc211.org/2005/gss");
    static private final Namespace gts = Namespace.getNamespace("gts","http://www.isotc211.org/2005/gts");
    static private final Namespace gml32 = Namespace.getNamespace("gml","http://www.opengis.net/gml/3.2");
    static private final Namespace xs = Namespace.getNamespace("xs","http://www.w3.org/2001/XMLSchema");

    private Logger log;
    private ServiceContext context;
    private SchemaManager schemaMan;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UriMapper localUris;
    private HarvestResult result;
    private String hostUrl;
    private HashSet<String> harvestUris = new HashSet<String>();
    private Map<String, ThreddsService> services = new HashMap<String, Harvester.ThreddsService>();
    private InvCatalogImpl catalog;
    private List<HarvestError> errors = new LinkedList<HarvestError>();

    private LatLonRect globalLatLonBox = null;
    private DateRange globalDateRange = null;
    private Element wmsResponse = null;
    private Element isoResponse = null;
    private Map<String,ThreddsMetadata.Variable> gridVariables = new HashMap<String,ThreddsMetadata.Variable>();
    private boolean metadataObtained = false;
    private boolean datasetMetadataObtained = false;
    private String metadataGetService;
		private List<Namespace> iso191152NamespaceList = new ArrayList<Namespace>();
		private List<InvDocumentation> docs = null;

    private IMetadataManager mdManager;
    private IMetadataSchemaUtils mdSchemaUtils;
    private IMetadataOperations mdOperations;
    private IMetadataIndexer mdIndexer;
    private DataManager dataMan;

    private GeonetworkDataDirectory dataDirectory;

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param context Jeeves context
     * @param params  Information about harvesting configuration for the node
     * @return null
     **/

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, ThreddsParams params) {
        super(cancelMonitor);
        this.log = log;
        this.context = context;
        this.params = params;

        result = new HarvestResult();

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        schemaMan = gc.getBean(SchemaManager.class);

        metadataGetService = "local://"+context.getNodeId()+"/api/records/";

				iso191152NamespaceList = buildISO191152NamespaceList();

        mdManager = gc.getBean(IMetadataManager.class);
        mdSchemaUtils = gc.getBean(IMetadataSchemaUtils.class);
        mdOperations = gc.getBean(IMetadataOperations.class);
        mdIndexer = gc.getBean(IMetadataIndexer.class);
        dataMan = gc.getBean(DataManager.class);
        dataDirectory = gc.getBean(GeonetworkDataDirectory.class);

    }

    //---------------------------------------------------------------------------

    /**
     * Start the harvesting of a thredds catalog
     **/

    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;

        Element xml = null;
        log.info("Retrieving remote metadata information for : " + params.getName());

        //--- Get uuid's and change dates of metadata records previously
        //--- harvested by this harvester grouping by harvest uri
        localUris = new UriMapper(context, params.getUuid());

        //--- Try to load thredds catalog document
        String url = params.url;
        try {
            XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest();
            req.setUrl(new URL(url));
            req.setMethod(XmlRequest.Method.GET);
            Lib.net.setupProxy(context, req);

            xml = req.execute();
        } catch (SSLHandshakeException e) {
            throw new BadServerCertificateEx(
                "Most likely cause: The thredds catalog " + url + " does not have a " +
                    "valid certificate. If you feel this is because the server may be " +
                    "using a test certificate rather than a certificate from a well " +
                    "known certification authority, then you can add this certificate " +
                    "to the GeoNetwork keystore using bin/installCert");
        }

        //--- Traverse catalog to create services and dataset metadata as required
        harvestCatalog(xml);

        //--- Remove previously harvested metadata for uris that no longer exist on the remote site
        for (String localUri : localUris.getUris()) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            if (!harvestUris.contains(localUri)) {
                for (RecordInfo record : localUris.getRecords(localUri)) {
                    if (cancelMonitor.get()) {
                        return this.result;
                    }

                    if (log.isDebugEnabled())
                        log.debug("  - Removing deleted metadata with id: " + record.id);
                    mdManager.deleteMetadata(context, record.id);

                    if (record.isTemplate.equals("s")) {
                        //--- Uncache xlinks if a subtemplate
                        Processor.uncacheXLinkUri(metadataGetService + record.uuid);
                        result.subtemplatesRemoved++;
                    } else {
                        result.locallyRemoved++;
                    }
                }
            }
        }

        mdManager.flush();

        result.totalMetadata = result.serviceRecords + result.collectionDatasetRecords;
        return result;
    }

    @Override
    public List<HarvestError> getErrors() {
        return errors;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------


    /**
     * Add metadata to GN for the services and datasets in a thredds catalog.
     *
     * 1. Open Catalog Document
     * 2. Crawl the catalog processing datasets as ISO19139 records and
     * request WMS GetCapabilities for each dataset
     * 3. Accumulate union of extents and other info
     * 4. Create a metadata record from accumulated info and service record for the thredds catalog service
     * 5. Save all
     *
     * @param cata Catalog document
     **/

    private void harvestCatalog(Element cata) throws Exception {

        if (cata == null)
            return;

        //--- loading categories and groups
        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);

        //--- Setup proxy authentication
        Lib.net.setupProxy(context);

        //--- load catalog
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        catalog = (InvCatalogImpl) factory.readXML(params.url);
        StringBuilder buff = new StringBuilder();
        if (!catalog.check(buff, true)) {
            throw new BadXmlResponseEx("Invalid catalog " + params.url + "\n" + buff.toString());
        }

        //--- display catalog read in log file
        log.info("Catalog read from " + params.url + " is \n" + factory.writeXML(catalog));

        Path schemaDir = schemaMan.getSchemaDir(params.outputSchema);

        //--- get service and dataset style sheets, try schema first
       	Path serviceStyleSheet = dataDirectory.getXsltConversion("schema:" + params.outputSchema + ":"
            + Geonet.Path.TDS_19119_19139_STYLESHEETS + "ThreddsCatalog-to-19119");
       	Path datasetStyleSheet = dataDirectory.getXsltConversion("schema:" + params.outputSchema + ":"
            + Geonet.Path.TDS_19119_19139_STYLESHEETS +"ThreddsCatalog-to-19139");

        Path dataParamsNCSSStylesheet = null;
        // -- This is schema dependent
        if (schemaDir.toString().contains("iso19139.mcp")) {
            dataParamsNCSSStylesheet = schemaDir.
							resolve(Geonet.Path.TDS_19119_19139_STYLESHEETS).
              resolve("NetcdfSubsetDataset-to-ISO19139MCPDataParameters.xsl");
        } else if (schemaDir.toString().contains("iso19115-2")) {
            dataParamsNCSSStylesheet = schemaDir.
							resolve(Geonet.Path.TDS_19119_19139_STYLESHEETS).
              resolve("NetcdfSubsetDataset-to-ISO191152contentInfo.xsl");
        }

        //--- Get base host url
        URL url = new URL(params.url);
        hostUrl = url.getProtocol() + "://" + url.getHost();
        if (url.getPort() != -1) hostUrl += ":" + url.getPort();

        //--- Crawl all datasets in the thredds catalogue
        log.info("Crawling the datasets in the catalog....");
        List<InvDataset> dsets = catalog.getDatasets();
        for (InvDataset ds : dsets) {
            if (cancelMonitor.get()) {
                return;
            }
            crawlDatasets(ds);
        }

        if (params.createServiceMd) {
            //--- process services found by crawling the catalog
            processServices(cata, serviceStyleSheet);
        }

        log.info("Adding dataset metadata...");
        createDatasetMetadata(cata, datasetStyleSheet, dataParamsNCSSStylesheet);

        //--- show how many datasets have been processed
        int totalDs = result.collectionDatasetRecords;
        log.info("Processed " + totalDs + " datasets.");

    }

    //---------------------------------------------------------------------------

    /**
     * Crawl all datasets in the catalog recursively
     *
     * @param  catalogDs        the dataset being processed
     * @throws Exception
     **/

    private void crawlDatasets(InvDataset catalogDs) throws Exception {
        log.info("Crawling through " + catalogDs.getName());

        // HACK!! Get real dataset hidden by netcdf library when catalog ref name
        // equals top dataset name in referenced catalog
        InvDataset realDs = catalogDs;
        if (catalogDs instanceof InvCatalogRef) {
            InvDataset proxyDataset = ((InvCatalogRef) catalogDs).getProxyDataset();
            realDs = proxyDataset.getName().equals(catalogDs.getName()) ? proxyDataset : catalogDs;
        }

        // if there are nested datasets then process those recursively - exclude latest as it is a convenience dir
        // for grouping the latest datasets and thus contains duplicates of other datasets we will traverse
        if (realDs.hasNestedDatasets() && !realDs.getName().contains("latest")) {
						// check for thredds metadata as it is most likely to occur here and will be relevant for all
            // datasets in the directory eg. variables, extents if inherited
						if (hasThreddsMetadata(realDs)) {
							extractThreddsMetadata(realDs);
						}
            List<InvDataset> dsets = realDs.getDatasets();
            for (InvDataset ds : dsets) {
                crawlDatasets(ds);
            }
        } else {
            log.info("Processing dataset: " + realDs.getName() + " with URL: " + getUri(realDs));
            examineThreddsDataset(realDs);
        }

        // Release resources allocated when crawling catalog references
        if (catalogDs instanceof InvCatalogRef) {
            ((InvCatalogRef) catalogDs).release();
        }
    }

    /**
     * Extract thredds metadata and use it to extend extents and record variables.
     *
     * @param ds        the dataset being processed
     **/

		private void extractThreddsMetadata(InvDataset ds) {
			log.info("Trying to find ThreddsMetadata for dataset "+ds.getName());

			ThreddsMetadata.GeospatialCoverage gsC = ds.getGeospatialCoverage();
			if (gsC != null) {
				log.info("Found ThreddsMetadata geospatialcoverage");
				addLatLonBox(gsC.getBoundingBox());
			}
			DateRange dr = ds.getTimeCoverage();
			if (dr != null) {
				log.info("Found ThreddsMetadata daterange");
				addTimeSpan(dr);
			}
			List<ThreddsMetadata.Variables> variablesList = ds.getVariables();
			if (variablesList != null && variablesList.size() > 0) {
				log.info("Found ThreddsMetadata variables");
				for (ThreddsMetadata.Variables variables : variablesList) {
					List<ThreddsMetadata.Variable> variableList = variables.getVariableList();
       		for (ThreddsMetadata.Variable variable : variableList) {
						gridVariables.put(variable.getName(), variable);
					}
				}
			}

      if (gsC != null && dr != null && gridVariables.size() > 0) metadataObtained = true;

			// record any documentation so that it can be passed to metadata records
	    docs = ds.getDocumentation();
			if (docs != null && docs.size() > 0) {
				log.info("Found ThreddsMetadata documentation");
			}
		}

    /**
     * Check to see whether the dataset has thredds metadata.
     *
     * @param ds        the dataset being processed
     **/

		private	boolean hasThreddsMetadata(InvDataset ds) {
			ThreddsMetadata.GeospatialCoverage gsC = ds.getGeospatialCoverage();
			DateRange dr = ds.getTimeCoverage();
			List<ThreddsMetadata.Variables> vars = ds.getVariables();
      List<InvDocumentation> docs = ds.getDocumentation();
      log.debug("ThreddsMetadata: "+gsC+" : "+dr+" : "+vars.size()+" : "+docs);
			return (gsC != null || dr != null || vars.size() > 0 || docs != null);
		}

    //---------------------------------------------------------------------------

    /**
     * Save the metadata to GeoNetwork's database
     *
     * @param md   the metadata being saved
     * @param uuid the uuid of the metadata being saved
     * @param uri  the uri from which the metadata has been harvested
     * @param isService  is this a service metadata record?
     **/

    private void saveMetadata(Element md, String uuid, String uri, boolean isService) throws Exception {

        //--- strip the catalog namespace as it is not required
        md.removeNamespaceDeclaration(invCatalog);

        String schema = mdSchemaUtils.autodetectSchema(md, null); // should be iso19139
        if (schema == null) {
            log.warning("Skipping metadata with unknown schema.");
            result.unknownSchema++;
        }

        log.info("  - Adding metadata with " + uuid + " schema is set to " + schema + "\n XML is " + Xml.getString(md));

        deleteExistingMetadata(uri);

        //
        // insert metadata
        //
        Metadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.METADATA);
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(getOwner()).
            setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid()).
            setUri(uri);

				if (!isService) {
        	if (params.datasetCategory != null && !params.datasetCategory.equals("")) {
           	MetadataCategory metadataCategory = context.getBean(MetadataCategoryRepository.class).findById(Integer.parseInt(params.datasetCategory)).get();

           	if (metadataCategory == null) {
             	throw new IllegalArgumentException("No category found with name: " + params.datasetCategory);
           	}
           	metadata.getMetadataCategories().add(metadataCategory);
        	}
				}
				else {
        	addCategories(metadata, params.getCategories(), localCateg, context, null, false);
				}

        metadata = (Metadata) mdManager.insertMetadata(context, metadata, md, IndexingMode.none, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        mdIndexer.indexMetadata(id, true, IndexingMode.full);

        mdManager.flush();
    }

    /**
     * Examine one dataset getting metadata including variables, extents and
     * service urls.
     * Note: collection datasets are not processes here.
     *
     * @param ds the dataset to be examined.
     * @throws Exception
     **/

    private void examineThreddsDataset(InvDataset ds) throws Exception {
        getMetadata(ds);

        //--- Add dataset uri to list of harvested uri's
        harvestUris.add(getUri(ds));

        //--- Record service URL for the dataset so that it can be added to the service
        //--- if required
        List<InvAccess> accesses = ds.getAccess();
        for (InvAccess access : accesses) {
            processService(access.getService(), getUuid(ds), ds);
        }
    }

    /**
     * Process a service reference for a dataset. Record details of the service      * and add a url to access the dataset using this service.
     *
     * @param serv the service to be processed
     * @param uuid uuid of the dataset that is delivered by this service
     * @param ds   dataset that is being delivered by this service
     **/

    private void processService(InvService serv, String uuid, InvDataset ds) {

        //--- get service, if compound service then get all nested services
        List<InvService> servs = new ArrayList<InvService>();
        if (serv.getServiceType() == ServiceType.COMPOUND) {
            servs.addAll(serv.getServices());
        } else {
            servs.add(serv);
        }

        //--- add dataset info to the appropriate ThreddsService
        for (InvService s : servs) {
            //Skip resolver services
            if (s.getServiceType().equals(ServiceType.RESOLVER)) continue;

            String sUrl = "";

            if (!s.isRelativeBase()) {
                sUrl = s.getBase();
            } else {
                sUrl = hostUrl + s.getBase();
            }

            log.info("Processing service: "+sUrl+" for "+ds.getName());
            ThreddsService ts = services.get(sUrl);
            if (ts == null) {
                ts = new ThreddsService();
                ts.service = s;
                ts.version = getVersion(serv, ds);
                ts.ops = getServerOperations(serv, ds);

                services.put(sUrl, ts);
            }
            InvAccess access = ds.getAccess(s.getServiceType());
        	  if (access != null) {
              String url = access.getStandardUrlName();
              ts.datasetUrls.add(url);
            }
        }

    }

    /**
     * Find the version of the service that delivers a particular dataset Handles OPeNDAP and HTTP
     * only at present
     *
     * @param    serv    the service that delivers the dataset
     * @param    ds        the dataset being delivered by the service
     **/

    private String getVersion(InvService serv, InvDataset ds) {
        String result = "unknown";
        if (serv.getServiceType() == ServiceType.OPENDAP) {
            InvAccess access = ds.getAccess(ServiceType.OPENDAP);
            if (access != null) {
                String href = access.getStandardUrlName() + ".ver";
                String readResult = getResultFromHttpUrl(href);
                if (readResult != null) result = readResult;
            }
        } else if (serv.getServiceType() == ServiceType.HTTPServer) {
            result = "HTTP/1.1";
        } else if (serv.getServiceType() == ServiceType.WMS) {
            result = "1.3.0"; // hard coded? We could get this elsewhere
        }
        return result;
    }

    /**
     * Get the server operations Applicable to OPeNDAP only at present
     *
     * @param    serv    the service that delivers the dataset
     * @param    ds        the dataset being delivered by the service
     **/

    private String getServerOperations(InvService serv, InvDataset ds) {
        String result = "none";
        if (serv.getServiceType() == ServiceType.OPENDAP) {
            InvAccess access = ds.getAccess(ServiceType.OPENDAP);
            if (access != null) {
                String href = access.getStandardUrlName() + ".help";
                String readResult = getResultFromHttpUrl(href);
                if (readResult != null) result = readResult;
            }
        }
        return result;
    }

    /**
     * Get a String result from an HTTP URL
     *
     * @param href the URL to get the info from
     **/

    private String getResultFromHttpUrl(String href) {
        String result = null;
        try {
            //--- get the version from the OPeNDAP server
            URL url = new URL(href);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            Object o = conn.getContent();
            if (log.isDebugEnabled())
                log.debug("Opened " + href + " and got class " + o.getClass().getName());
            StringBuffer version = new StringBuffer();
            String inputLine;
            BufferedReader dis = null;
            InputStreamReader isr = null;
            InputStream is = null;
            try {
                is = conn.getInputStream();
                isr = new InputStreamReader(is, Constants.ENCODING);
                dis = new BufferedReader(isr);
                while ((inputLine = dis.readLine()) != null) {
                    version.append(inputLine + "\n");
                }
                result = version.toString();
                if (log.isDebugEnabled()) log.debug("Read from URL:\n" + result);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(isr);
                IOUtils.closeQuietly(dis);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled())
                log.debug("Caught exception " + e + " whilst attempting to query URL " + href);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Get dataset uri
     *
     * @param ds the dataset to be processed
     **/

    private String getUri(InvDataset ds) {
        if (ds.getID() == null) {
            return ds.getParentCatalog().getUriString() + "#" + ds.getName();
        } else {
            return getSubsetUrl(ds);
        }
    }

    /**
     * Return url to a catalog having the specified dataset as the top dataset
     *
     * @param ds the dataset to be processed
     **/

    private String getSubsetUrl(InvDataset ds) {
        try {
            return ds.getParentCatalog().getUriString() + "?dataset=" + URLEncoder.encode(ds.getID(), Constants.ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.error("Thrown Exception " + e + " during dataset processing");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete all metadata previously harvested for a particular uri
     *
     * @param uri uri for which previously harvested metadata should be deleted
     **/

    private void deleteExistingMetadata(String uri) throws Exception {
        List<RecordInfo> localRecords = localUris.getRecords(uri);

        if (localRecords == null) return;

        for (RecordInfo record : localRecords) {
            mdManager.deleteMetadata(context, record.id);

            if (record.isTemplate.equals("s")) {
                //--- Uncache xlinks if a subtemplate
                Processor.uncacheXLinkUri(metadataGetService + record.uuid);
            }
        }
    }


    /**
     * Get uuid for dataset
     *
     * @param ds the dataset to be processed
     **/

    private String getUuid(InvDataset ds) {
        String uuid = ds.getUniqueID();

        if (uuid == null) {
            uuid = Sha1Encoder.encodeString(ds.getCatalogUrl()); // md5 full dataset url
        } else {
            uuid = StringUtil2.allow(uuid, "_-.", '-');
        }

        return uuid;
    }

    /**
     * Get XML Response.
     *
     * @param url the url to get the XML response from
     **/

		private Element getXMLResponse(String url) throws MalformedURLException, IOException {
      XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest();
      req.setUrl(new URL(url));
      req.setMethod(XmlRequest.Method.GET);
      Lib.net.setupProxy(context, req);

      return req.execute();
		}

    /**
     * Try the different THREDDS services until we find one that gives us the extents and
     * variables in the dataset supplied as a parameter.
     *
     * @param ds the dataset to be processed
     */

    private void getMetadata(InvDataset ds) {
        try {
          // try the various thredds services in order to get some metadata about the dataset
          if (!metadataObtained) {
						datasetMetadataObtained = false;
						if (hasWMSService(ds)) {
					  	extractMetadataFromWMS(ds);
						}

            if (!datasetMetadataObtained && hasNetcdfSubsetService(ds)) {
					  	extractMetadataFromNetcdfSubsetService(ds);
						}

            if (!datasetMetadataObtained && hasISOService(ds)) {
					  	extractMetadataFromISO(ds);
            }

						if (!datasetMetadataObtained) { // try for ddx - last ditch attempt here as usually one of the above will work
					  	extractMetadataFromOpendapDDX(ds);
            }
					}
        } catch (Exception e) {
          log.error("Thrown Exception " + e + " during dataset processing");
          e.printStackTrace();
        }
		}


		private boolean hasNetcdfSubsetService(InvDataset ds) {
			return (ds.getAccess(ServiceType.NetcdfSubset) != null);
		}

		private boolean hasWMSService(InvDataset ds) {
			return (ds.getAccess(ServiceType.WMS) != null);
		}

		private boolean hasISOService(InvDataset ds) {
			return (ds.getAccess(ServiceType.ISO) != null);
		}

// ISO Stuff

		private void extractMetadataFromISO(InvDataset ds) {
			try {
        String url = "";
        InvAccess access = null;
        // Get ISO URL and extract metadata from what we find.....
        access = ds.getAccess(ServiceType.ISO);
      	if (access != null) {
          url = access.getStandardUrlName();
          log.debug("ISO url is "+url);

					// record response in global var isoResponse as we may use it to add some other metadata eg. contacts
          isoResponse = getXMLResponse(url);

					log.debug("Name of root element in response is "+isoResponse.getName());
					if (isoResponse.getName().equals("MI_Metadata")) {
						Element latLonBox = Xml.selectElement(isoResponse,
																		"*//gmd:MD_DataIdentification//gmd:EX_GeographicBoundingBox", iso191152NamespaceList);
						if (latLonBox == null) {
						  log.error("Cannot find ISO19115-2 EX_GeographicBoundingBox element!");
						  return;
						}

            Element timeSpan = Xml.selectElement(isoResponse, "*//gmd:MD_DataIdentification//gml:TimePeriod",
																								iso191152NamespaceList);
						if (timeSpan == null) {
						  log.error("Cannot find ISO19115-2 TimePeriod element!");
						  return;
						}

            List<?> contentInfo = Xml.selectNodes(isoResponse,
								"gmd:contentInfo/gmi:MI_CoverageDescription/gmd:dimension/gmd:MD_Band", iso191152NamespaceList);
						if (contentInfo == null) {
							log.error("Cannot find ISO19115-2 contentInfo element!");
							return;
						}

          	if (log.isDebugEnabled())
          		log.debug("Bounding box is:\n"+Xml.getString(latLonBox)+"\n Time span is:\n"+Xml.getString(timeSpan)+
												"\nContent Info has "+contentInfo.size()+" bands");

						// extend global bbox and textent and add variables using what we found
            datasetMetadataObtained = (addISOLatLonBox(latLonBox) && addISOTimeSpan(timeSpan) &&
																	extractISOVariables(contentInfo));
					}
				}
      } catch (Exception e) {
        log.error("Thrown Exception " + e + " during dataset processing");
        e.printStackTrace();
      }
		}

  	/**
   	 * Extract ISO bounding box info.
   	 *
   	 * @param bbox EX_GeographicBoundingBox from THREDDS ISO metadata
   	 */

		private boolean addISOLatLonBox(Element bbox) {
			boolean result = false;

			try {
				Element westE = bbox.getChild("westBoundLongitude", gmd);
        Element eastE = bbox.getChild("eastBoundLongitude", gmd);
        Element southE = bbox.getChild("southBoundLatitude", gmd);
        Element northE = bbox.getChild("northBoundLatitude", gmd);
				if (westE != null && eastE != null && northE != null && southE != null) {
					double west = Double.parseDouble(westE.getChildText("Decimal", gco));
      		double east = Double.parseDouble(eastE.getChildText("Decimal", gco));
      		double south = Double.parseDouble(southE.getChildText("Decimal", gco));
      		double north = Double.parseDouble(northE.getChildText("Decimal", gco));
      		LatLonRect thisBox = new LatLonRect(new LatLonPointImpl(south,west), new LatLonPointImpl(north,east));
		  		addLatLonBox(thisBox);
					result = true;
				}
			} catch (NumberFormatException nfe) {
				// skip
			}
			return result;

		}

  	/**
   	 * Extract ISO temporal extent info.
   	 *
   	 * @param dimension gml:TimePeriod from THREDDS ISO metadata
   	 */

		private boolean addISOTimeSpan(Element dimension) {
			boolean result = false;

      String startTime = dimension.getChildText("beginPosition", gml32);
      String endTime   = dimension.getChildText("endPosition", gml32);
			if (startTime != null && endTime != null && startTime.length() > 0 && endTime.length() > 0) {
        ISODate st = new ISODate(startTime);
        ISODate et = new ISODate(endTime);
        DateRange thisDateRange = new DateRange(st.toDate(), et.toDate());
				addTimeSpan(thisDateRange);
        result = true;
      }

			return result;
		}

  	/**
   	 * Extract ISO variable info.
   	 *
   	 * @param bands List of gmd:MD_Band elements from contentInfo of ISO metadata
   	 */

		private boolean extractISOVariables(List<?> bands) {
			boolean result = false;

			try {
				for (Object o : bands) {
					if (o instanceof Element) {
						Element band = (Element)o;
						Element layer = Xml.selectElement(band, "*//gco:aName/gco:CharacterString", iso191152NamespaceList);
						if (layer != null) {
							String layerName = layer.getText();
        			if (layerName.length() > 0) {
								ThreddsMetadata.Variable var = new ThreddsMetadata.Variable();
								var.setName(layerName);
								Element desc = Xml.selectElement(band, "*//gmd:descriptor/gco:CharacterString", iso191152NamespaceList);
								if (desc != null) {
									String layerDesc = desc.getText();
          				if (layerDesc.length() > 0) {
										var.setDescription(layerDesc);
									}
								}
								gridVariables.put(layerName, var);
								result = true;
							}
						}
					}
				}
			} catch (JDOMException je) {
				je.printStackTrace();
			}
			return result;
		}

  	/**
   	 * Build list of ISO Namespaces for use in xpath calls.
   	 */

    private List<Namespace> buildISO191152NamespaceList() {
			List<Namespace> nsList = new ArrayList<Namespace>();

			nsList.add(xsi);
			nsList.add(gco);
      nsList.add(gmd);
      nsList.add(gmi);
      nsList.add(srv);
      nsList.add(gmx);
      nsList.add(gsr);
      nsList.add(gss);
      nsList.add(gts);
      nsList.add(gml32);
      nsList.add(xlink);
      nsList.add(xs);

			return nsList;
		}

// End ISO Stuff

// WMS Stuff

    /**
     * Extract extents and variables from WMS (OGC WMS GetCapabilities) call on dataset,
     *
     * @param ds the dataset to be queried via the WMS service
     */

		private void extractMetadataFromWMS(InvDataset ds) {
			try {
        String url = "";
        InvAccess access = null;
        // Get WMS URL and build getcapabilities statement for first layer we find.....
        access = ds.getAccess(ServiceType.WMS);
      	if (access != null) {
          url = access.getStandardUrlName();
          log.debug("WMS url is "+url);
          url += "?request=GetCapabilities&version=1.3.0&service=WMS";

					// record response in global var wmsResponse as we may use it to add some other metadata eg. contacts
          wmsResponse = getXMLResponse(url);

					if (wmsResponse.getName().equals("WMS_Capabilities")) {
						List<Element> layers = findLayers(wmsResponse);
						log.debug("Found "+layers.size()+" layers in WMS_Capabilities");
						if (layers.size() > 0) {
          		Element bbox = layers.get(0).getChild("BoundingBox", wms);
							if (bbox == null) {
						    log.error("Cannot find OGC WMS BoundingBox element!");
						    return;
							}
							Element dimension = layers.get(0).getChild("Dimension", wms);
							if (dimension == null || !(dimension.getAttributeValue("name").equals("time"))) {
						    log.error("Cannot find OGC WMS Dimension element!");
						    return;
							}

          		if (log.isDebugEnabled())
          			log.debug("Bounding box is:\n"+Xml.getString(bbox)+"\n Time span is:\n"+Xml.getString(dimension));

							// extend global bbox and textent and add variables using what we found
              datasetMetadataObtained = (addWMSLatLonBox(bbox) && addWMSTimeSpan(dimension) && extractWMSVariables(layers));
						}
					}
				}
      } catch (Exception e) {
        log.error("Thrown Exception " + e + " during dataset processing");
        e.printStackTrace();
      }
		}

   /**
   	 * Find queryable layers in a WMS getcapabilities statement.
   	 *
   	 * @param datasetXml JDOM xml of wms getcapabilities statement
   	 */

  	private List<Element> findLayers(Element datasetXml) {
			List<Element> layers = new ArrayList<Element>();

    	for (Iterator<?> iter = datasetXml.getDescendants(); iter.hasNext(); ) {
      	Object o = iter.next();
      	if (o instanceof Element) {
        	Element layer = (Element)o;
        	if (layer.getName().equals("Layer") && layer.getAttributeValue("queryable","0").equals("1") &&
              layer.getChild("BoundingBox", wms) != null && layer.getChild("Dimension", wms) != null) {
          	layers.add(layer);
        	}
      	}
    	}
    	return layers;
  	}

  	/**
   	 * Extract wms bounding box info.
   	 *
   	 * @param bbox BoundingBox element from WMS GetCapabilities
   	 */

		private boolean addWMSLatLonBox(Element bbox) {
			boolean result = false;

			try {
				double west = Double.parseDouble(bbox.getAttributeValue("minx"));
      	double east = Double.parseDouble(bbox.getAttributeValue("maxx"));
      	double south = Double.parseDouble(bbox.getAttributeValue("miny"));
      	double north = Double.parseDouble(bbox.getAttributeValue("maxy"));
      	LatLonRect thisBox = new LatLonRect(new LatLonPointImpl(south,west), new LatLonPointImpl(north,east));
		  	addLatLonBox(thisBox);
				result = true;
			} catch (NumberFormatException nfe) {
				// skip
			}
			return result;

		}

  	/**
   	 * Extract wms temporal Dimension info.
   	 *
   	 * @param dimension Dimension element from WMS GetCapabilities
   	 */

		private boolean addWMSTimeSpan(Element dimension) {
			boolean result = false;

			String[] times = dimension.getText().split(",");

      String startTime = times[0].trim();
      String endTime = times[times.length-1].trim();
			if (startTime != null && endTime != null && startTime.length() > 0 && endTime.length() > 0) {
        ISODate st = new ISODate(startTime);
        ISODate et = new ISODate(endTime);
        DateRange thisDateRange = new DateRange(st.toDate(), et.toDate());
				addTimeSpan(thisDateRange);
        result = true;
      }

			return result;
		}

  	/**
   	 * Extract wms layer names and use as variables.
   	 *
   	 * @param layers List of Layer elements from WMS GetCapabilities
   	 */

		private boolean extractWMSVariables(List<Element> layers) {
			boolean result = false;

			for (Element layer : layers) {
				String layerName = layer.getChildText("Name", wms);
				String layerDesc = layer.getChildText("Abstract", wms);
        if (layerName != null) {
					ThreddsMetadata.Variable var = new ThreddsMetadata.Variable();
					var.setName(layerName);
          if (layerDesc != null) {
						var.setDescription(layerDesc);
					}
					gridVariables.put(layerName, var);
					result = true;
				}
			}
			return result;
		}

// End WMS Stuff

// NCSS Routines

    /**
     * Extract extents and variables from NCSS (Netcdf subset service) call on dataset.
     *
     * @param ds the dataset to be queried via the NCSS service
     */

		private void extractMetadataFromNetcdfSubsetService(InvDataset ds) {
        try {
          // go to the subset service and get the
          // dataset.xml file as it contains bbox and textent
          InvAccess access = ds.getAccess(ServiceType.NetcdfSubset);
          String url = access.getStandardUrlName();
          log.info("NCSS url is "+url);
          url += "/dataset.xml";

          Element xml = getXMLResponse(url);
					/* Looking for:
                       ....
                         <LatLonBox>
                              <west>65.0000</west>
                              <east>-175.4299</east>
                              <south>-65.0000</south>
                              <north>16.9500</north>
                         </LatLonBox>
                         <TimeSpan>
                              <begin>2017-07-22T06:00:00Z</begin>
                              <end>2017-07-22T06:00:00Z</end>
                         </TimeSpan>
                       ....
             	If we don't find then we skip this dataset
          */
          Element latLonBox = xml.getChild("LatLonBox");
          if (latLonBox == null) {
						log.error("Cannot find LatLonBox element!, skipping dataset");
						return;
          }

          Element timeSpan = xml.getChild("TimeSpan");
          if (timeSpan == null) {
						log.error("Cannot find TimeSpan element!, skipping dataset");
						return;
          }

          if (log.isDebugEnabled())
          	log.debug("Bounding box is:\n"+Xml.getString(latLonBox)+"\n Time span is:\n"+Xml.getString(timeSpan));

					// extend global bbox and textent and add variables using what we found
          datasetMetadataObtained = (addNCSSLatLonBox(latLonBox) && addNCSSTimeSpan(timeSpan) && extractNCSSVariables(xml));

        } catch (Exception e) {
          log.error("Thrown Exception " + e + " during dataset processing");
          e.printStackTrace();
        }
		}

    /**
     * Extend global bounding box (globalLatLonBox) using latLonBox from NCSS xml.
     *
     * @param    latLonBox           bounding box from NCSS xml to add to globalLatLonBox
     **/

    private boolean addNCSSLatLonBox(Element latLonBox) {
			boolean result = false;

			try {
				double west = Double.parseDouble(latLonBox.getChildText("west"));
				double east = Double.parseDouble(latLonBox.getChildText("east"));
				double south = Double.parseDouble(latLonBox.getChildText("south"));
				double north = Double.parseDouble(latLonBox.getChildText("north"));
      	LatLonRect thisBox = new LatLonRect(new LatLonPointImpl(south,west), new LatLonPointImpl(north,east));
		  	addLatLonBox(thisBox);
				result = true;
			} catch (NumberFormatException nfe) {
				// skip
			}
			return result;
		}

    /**
     * Extend global date range (globalDateRange) using timeSpan from NCSS xml.
     *
     * @param    timeSpan           time span from NCSS xml to add to globalDateRange
     **/

    private boolean addNCSSTimeSpan(Element timeSpan) {
			boolean result = false;

			String bt = timeSpan.getChildText("begin");
			String et = timeSpan.getChildText("end");

			if (bt != null && et != null && bt.length() > 0 && et.length() > 0) {
      	ISODate beginDate = new ISODate(bt);
      	ISODate endDate = new ISODate(et);
      	DateRange thisDateRange = new DateRange(beginDate.toDate(), endDate.toDate());
				addTimeSpan(thisDateRange);
				result = true;
			}

			return result;
		}

    /**
     * Extract variables from NCSS (netcdf subset service) call on dataset.
     *
     * @param xml The XML returned from a NCSS call
     */

		private boolean extractNCSSVariables(Element xml) {
			boolean result = false;

			try {
				List<?> grids = Xml.selectNodes(xml, "gridSet/grid");
    		for (Object o : grids) {
      		if (o instanceof Element) {
						Element grid = (Element)o;
						String name = grid.getAttributeValue("name");
						if (name != null) {
							ThreddsMetadata.Variable var = new ThreddsMetadata.Variable();
							var.setName(name);
							List<?> attrs = grid.getChildren("attribute");
							for (Object oa : attrs) {
								if (oa instanceof Element) {
									Element attr = (Element)oa;
									String attrName = attr.getAttributeValue("name");
									if (attrName.equals("long_name")) {
										var.setDescription(attr.getAttributeValue("value"));
									} else if (attrName.equals("units")) {
										var.setUnits(attr.getAttributeValue("value"));
									}
								}
							}
            	gridVariables.put(name, var);
            	result = true;
						}
					}
				}
			} catch (JDOMException je) {
				je.printStackTrace();
			}

			return result;
		}
// End NCSS Stuff

// DDX Stuff

    /**
     * Extract metadata from DDX (Opendap ddx service) call on dataset. This is really a last ditch attempt to
     * get something about the variables in the dataset because the ddx doesn't contain any info about the
     * extents etc. So this gets called when we don't have NCSS, WMS, ISO etc which is only for very old THREDDS
     * services nowadays though even some of those will fail here as they don't even support ddx calls!
     *
     * @param ds the dataset to be queried via the OPENDAP ddx service
     */

		private void extractMetadataFromOpendapDDX(InvDataset ds) {
        try {
          // go to the subset service and get the
          // dataset.xml file as it contains bbox and textent
          InvAccess access = ds.getAccess(ServiceType.OPENDAP);
          String url = access.getStandardUrlName();
          log.info("Opendap url is "+url);
          url += ".ddx";

          Element xml = getXMLResponse(url);

          // add variables to gridVariables
					if (extractDDXVariables(xml)) datasetMetadataObtained = true;

        } catch (Exception e) {
          log.error("Thrown Exception " + e + " during dataset processing");
          e.printStackTrace();
        }
		}

    /**
     * Extract variables from DDX (Opendap ddx service) call on dataset.
     *
     * @param xml The XML returned from a DDX call
     */

		private boolean extractDDXVariables(Element xml) {
			boolean result = false;

    	for (Iterator<?> iter = xml.getDescendants(); iter.hasNext(); ) {
      	Object o = iter.next();
      	if (o instanceof Element) {
					Element array = (Element)o;
					String name = array.getAttributeValue("name");
        	if (name != null) {
						ThreddsMetadata.Variable var = new ThreddsMetadata.Variable();
						var.setName(name);
						List<?> attributes = array.getChildren("Attribute");
						for (Object oa : attributes) {
							if (oa instanceof Element) {
								Element attr = (Element)oa;
								String attrName = attr.getAttributeValue("name");
            		if (attrName.equals("long_name")) {
						  	  var.setDescription(attr.getChildText("value"));
								} else if (attrName.equals("units")) {
						  	  var.setUnits(attr.getChildText("value"));
								}
							}
						}
            gridVariables.put(name, var);
            result = true;
					}
				}
			}
			return result;
		}
// End DDX Stuff

    /**
     * Extend global bounding box (globalLatLonBox) using supplied LatLonRect.
     *
     * @param    thisBox           bounding box supplied as LatLonRect
     **/

		private void addLatLonBox(LatLonRect thisBox) {
      if (globalLatLonBox == null) {
				globalLatLonBox = thisBox;
			} else {
        globalLatLonBox.extend(thisBox);
			}
    }

    /**
     * Extend global date range (globalDateRange) using supplied DateRange.
     *
     * @param    thisDateRange      date range supplied as DateRange
     **/

		private void addTimeSpan(DateRange thisDateRange) {
      if (globalDateRange == null) {
      	globalDateRange = thisDateRange;
      } else {
        globalDateRange.extend(thisDateRange);
      }
    }

    /**
     * Process all services that serve datasets in the thredds catalog.
     *
     * @param    cata                the XML of the catalog
     * @param    styleSheet    name of the stylesheet to produce 19119
     **/

    private void processServices(Element cata, Path styleSheet) throws Exception {

        for (String sUrl : services.keySet()) {

            ThreddsService ts = services.get(sUrl);
            InvService serv = ts.service;
            String type = serv.getServiceType().toString();

            if (log.isDebugEnabled()) log.debug("Processing Thredds service: " + serv.toString());

            String sUuid = Sha1Encoder.encodeString(sUrl);
            String urls = StringUtils.join(ts.datasetUrls,"^^^");

            	//---	pass info to stylesheet which will create a 19119 record

            if (log.isDebugEnabled())
                log.debug("  - XSLT transformation using " + styleSheet);

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("lang", params.lang);
            param.put("topic", params.topic);
            param.put("uuid", sUuid);
            param.put("url", urls);
            param.put("name", "Thredds Service "+serv.getName()+ " at "+sUrl);
            param.put("type", serv.getServiceType().toString().toUpperCase());
            param.put("version", ts.version);
            param.put("desc", serv.toString());
            param.put("props", serv.getProperties().toString());
            param.put("serverops", ts.ops);
            param.put("bbox", globalLatLonBox.getLatMin()+"^^^"+globalLatLonBox.getLatMax()+"^^^"+globalLatLonBox.getLonMin()+"^^^"+globalLatLonBox.getLonMax());
            param.put("textent", globalDateRange.getStart().toDateTimeStringISO()+"^^^"+globalDateRange.getEnd().toDateTimeStringISO());

            Element md = Xml.transform(cata, styleSheet, param);

            String schema = mdSchemaUtils.autodetectSchema(md, null);
            if (schema == null) {
               	log.warning("Skipping metadata with unknown schema.");
               	result.unknownSchema++;
            } else {

               	//--- Now add to geonetwork
                boolean isService = true;
               	saveMetadata(md, sUuid, sUrl, isService);

               	harvestUris.add(sUrl);

               	result.serviceRecords++;
            }
        }
    }

    /**
     * Create a dataset for the thredds catalog URL, write variables and
     * selected services as online references in distributonInfo.
     *
     * @param    cata                    the XML of the catalog
     * @param    styleSheet              stylesheet to produce 19139
     * @param    dataParamsNCSSStylesheet    stylesheet to produce mcp:dataParameters from subset service xml
     **/

    private void createDatasetMetadata(Element cata, Path styleSheet,
																				Path dataParamsNCSSStylesheet) throws Exception {

        String sUuid = Sha1Encoder.encodeString(params.url);

        //---	pass info to stylesheet which will create a 19139 record

        if (log.isDebugEnabled())
               log.debug("  - XSLT transformation using " + styleSheet);

				String title = params.datasetTitle;
        if (title.equals("")) {
					title =  "Thredds Dataset at "+params.url;
				}
        String abst = params.datasetAbstract;
        if (abst.equals("")) {
					abst = "Thredds Dataset";
				}

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("lang", params.lang);
        param.put("topic", params.topic);
        param.put("uuid", sUuid);
        param.put("url", params.url);
        param.put("name", title);
        param.put("desc", abst);
        if (globalLatLonBox != null) {
        	param.put("bbox", globalLatLonBox.getLatMin()+"^^^"+globalLatLonBox.getLatMax()+"^^^"+globalLatLonBox.getLonMin()+"^^^"+globalLatLonBox.getLonMax());
				}
				if (globalDateRange != null) {
        	param.put("textent", globalDateRange.getStart().toDateTimeStringISO()+"^^^"+globalDateRange.getEnd().toDateTimeStringISO());
				}

        Element md = Xml.transform(wmsResponse, styleSheet, param);

        String schema = mdSchemaUtils.autodetectSchema(md, null);
        if (schema == null) {
          log.warning("Skipping metadata with unknown schema.");
          result.unknownSchema++;
        } else {
					if (schema.contains("iso19139.mcp") && gridVariables != null) {
						Element dps = null;
          	if (dataParamsNCSSStylesheet != null) {
								Element gridVariablesXml = turnThreddsMetadataVariablesIntoXml();
            		dps = Xml.transform(gridVariablesXml, dataParamsNCSSStylesheet);
						}
          	if (dps != null) addDataParameters(md, dps);
					}

 	        //--- Now add to geonetwork
        	boolean isService = false;
					log.debug("Will save metadata "+Xml.getString(md));
        	saveMetadata(md, sUuid, params.url, isService);

        	harvestUris.add(params.url);

        	result.collectionDatasetRecords++;
				}
    }

		private Element turnThreddsMetadataVariablesIntoXml() {
			Element result = new Element("gridDataset");
			Element gridSet = new Element("gridSet");
			for (ThreddsMetadata.Variable var : gridVariables.values()) {
				Element grid = new Element("grid");
				grid.setAttribute("name", var.getName());
				Element attr = new Element("attribute");
				attr.setAttribute("name", "long_name");
				attr.setAttribute("value", var.getDescription());
				grid.addContent(attr);
				attr = new Element("attribute");
				attr.setAttribute("name", "units");
				attr.setAttribute("value", var.getUnits());
				grid.addContent(attr);
				gridSet.addContent(grid);
			}
			result.addContent(gridSet);
			log.debug("Thredds variables have been turned into: "+Xml.getString(result));
			return result;
		}

    /**
     * Add mcp:dataParameters created from netcdf subset service to identificationInfo (mcp only)
     *
     * <mcp:dataParameters> <mcp:DP_DataParameters> ... ... ... </mcp:DP_DataParameters>
     * </mcp:dataParameters>
     *
     * @param md             iso19139 MCP metadata
     * @param dataParameters mcp:dataParameters block to be added to metadata
     **/

    private Element addDataParameters(Element md, Element dataParameters) throws Exception {
        Element root = (Element) md.getChild("identificationInfo", gmd).getChildren().get(0);
        root.addContent(dataParameters); // this is dependent on the mcp schema - last element
        return md;
    }

    private static class ThreddsService {
        public List<String> datasetUrls = new ArrayList();
        public InvService service;
        public String version;
        public String ops;
    }

}
