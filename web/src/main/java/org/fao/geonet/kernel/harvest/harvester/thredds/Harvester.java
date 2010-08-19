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

package org.fao.geonet.kernel.harvest.harvester.thredds;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.BadServerCertificateEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.FragmentParams;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.HarvestSummary;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvMetadata;
import thredds.catalog.InvService;
import thredds.catalog.ServiceType;
import thredds.catalog.ThreddsMetadata;
import thredds.catalog.dl.DIFWriter;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.NetcdfDatasetInfo;
import ucar.nc2.ncml.NcMLWriter;
import ucar.unidata.util.StringUtil;

import javax.net.ssl.SSLHandshakeException;
import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//=============================================================================
/** 
 * A ThreddsHarvester is able to generate metadata for datasets and services
 * from a Thredds catalogue. Metadata for datasets are generated
 * using dataset information contained in the thredds catalogue document or
 * or from opening the dataset and retrieving variables, coordinate systems 
 * and/or global attributes.
 * 
 * Metadata produced are :
 * <ul>
 * 	<li>ISO19119 for service metadata (all services in the catalog)</li>
 * 	<li>ISO19139 for dataset metadata (for datasets in catalog)</li>
 * </ul>
 * 
 * <pre>  
 * <nodes>
 *  <node type="thredds" id="114">
 *    <site>
 *      <name>TEST</name>
 *      <uuid>c1da2928-c866-49fd-adde-466fe36d3508</uuid>
 *      <account>
 *        <use>true</use>
 *        <username />
 *        <password />
 *      </account>
 *      <url>http://localhost:5556/thredds/catalog.xml</url>
 *      <icon>default.gif</icon>
 *    </site>
 *    <options>
 *      <every>90</every>
 *      <oneRunOnly>false</oneRunOnly>
 *      <status>active</status>
 *      <lang>eng</lang>
 *      <createThumbnails>false</createThumbnails>
 *      <createServiceMd>false</createServiceMd>
 *      <createCollectionDatasetMd>true</createCollectionDatasetMd>
 *      <createAtomicDatasetMd>false</createAtomicDatasetMd>
 *      <ignoreHarvestOnCollections>true</ignoreHarvestOnCollections>
 *      <collectionGeneration>default</collectionGeneration>
 *      <outputSchemaOnCollections>iso19139</outputSchemaOnCollections>
 *      <collectionFragmentStylesheet>collection_fragments.xsl</collectionFragmentStylesheet>
 *      <collectionMetadataTemplate>10247</collectionMetadataTemplate>
 *      <createCollectionSubtemplates>false</createCollectionSubtemplates>
 *      <ignoreHarvestOnAtomics>true</ignoreHarvestOnAtomics>
 *      <atomicGeneration>default</atomicGeneration>
 *      <outputSchemaOnAtomics>iso19139.mcp</outputSchemaOnAtomics>
 *      <atomicFragmentStylesheet>atomic_fragments.xsl</atomicFragmentStylesheet>
 *      <atomicMetadataTemplate>10247</atomicMetadataTemplate>
 *      <createAtomicSubtemplates>false</createAtomicSubtemplates>
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
 * @author Simon Pigot
 *   
 */
class Harvester
{
	
	
	//---------------------------------------------------------------------------
	/** 
     * Constructor
     *  
     * @param log		
     * @param context		Jeeves context
     * @param dbms 			Database
     * @param params	Information about harvesting configuration for the node
     * 
     * @return null
     */
	public Harvester(Logger log, ServiceContext context, Dbms dbms, ThreddsParams params) {
		this.log    = log;
		this.context= context;
		this.dbms   = dbms;
		this.params = params;

		result = new ThreddsResult ();
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext (Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager ();
		difToIsoStyleSheet = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/DIF-to-ISO19139.xsl"; 
		difToIsoMcpStyleSheet = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/DIF-to-ISO19139-MCP.xsl"; 
		cdmCoordsToIsoKeywordsStyleSheet = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/CDMCoords-to-ISO19139Keywords.xsl";
		cdmCoordsToIsoMcpDataParametersStyleSheet = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/CDMCoords-to-ISO19139MCPDataParameters.xsl";

		SettingInfo si = new SettingInfo(context);
		String siteUrl = si.getSiteUrl() + context.getBaseUrl();
		metadataGetService = siteUrl + "/srv/en/xml.metadata.get";

		fragmentStylesheetDirectory = context.getAppPath() + Geonet.Path.TDS_STYLESHEETS;

		//--- Create fragment harvester for atomic datasets if required
		if (params.createAtomicDatasetMd && params.atomicMetadataGeneration.equals(ThreddsParams.FRAGMENTS)) {
			atomicFragmentHarvester = new FragmentHarvester(log, context, dbms, getAtomicFragmentParams());
		}
		
		//--- Create fragment harvester for collection datasets if required
		if (params.createCollectionDatasetMd && params.collectionMetadataGeneration.equals(ThreddsParams.FRAGMENTS)) {
			collectionFragmentHarvester = new FragmentHarvester(log, context, dbms, getCollectionFragmentParams());
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------
	/** 
    * Start the harvesting of a thredds catalog 
    */
	public ThreddsResult harvest() throws Exception {
		
		Element xml;
		log.info("Retrieving remote metadata information for : " + params.name);
        
		//--- Clean all before harvest : Remove/Add mechanism
		//--- If harvest failed (ie. if node unreachable), metadata will be 
		//--- removed, and the node will not be referenced in the catalogue 
		//--- until next harvesting.
        UUIDMapper localUuids = new UUIDMapper(dbms, params.uuid);

    //--- Try to load thredds catalog document
		String url = params.url;
		try {
			xml = Xml.loadFile (new URL(url));
		} catch (SSLHandshakeException e) {
			throw new BadServerCertificateEx(
				"Most likely cause: The thredds catalog "+url+" does not have a "+
				"valid certificate. If you feel this is because the server may be "+
				"using a test certificate rather than a certificate from a well "+
				"known certification authority, then you can add this certificate "+
				"to the GeoNetwork keystore using bin/installCert");
		} catch (JDOMException e) {
			throw new BadXmlResponseEx("Invalid THREDDS catalog XML"); 
		}
		
		//--- remove old metadata
		for (String uuid : localUuids.getUUIDs()) {
			String id = localUuids.getID (uuid);
			log.debug ("  - Removing old metadata before update with id: " + id);

			//--- Remove metadata
			dataMan.deleteMetadata (dbms, id);

			String isTemplate = localUuids.getTemplate(uuid);
			if (isTemplate.equals("s")) {
				//--- Uncache xlinks if a subtemplate
				Processor.uncacheXLinkUri(metadataGetService+"?uuid=" + uuid);
				result.subtemplatesRemoved++;
			} else {
				result.locallyRemoved++;
			}
		}
		
		if ((result.locallyRemoved + result.subtemplatesRemoved) > 0) dbms.commit();
		
    //--- traverse catalog to create services and dataset metadata as required
    addMetadata(xml);
        
    dbms.commit();

    result.total = result.serviceRecords + result.collectionDatasetRecords + result.atomicDatasetRecords;
		return result;
	}

	//---------------------------------------------------------------------------
	/** 
    * Add metadata to GN for the services and datasets in a thredds 
		* catalog
    *  
		*	1.Open Catalog Document
	 	* 2.Crawl the catalog processing datasets as ISO19139 records 
		* and recording services (attach dataset ids to the services that deliver
		* them)
	 	* 3.Process services found as ISO19119 records
	 	* 4.Create a service record for the thredds catalog service provided and 
		* list service records as something that the thredds catalog provides
	 	* 5.Save all
    *	
    * @param cata      Catalog document
    *                   
    */
	 private void addMetadata (Element cata) throws Exception {

		if (cata == null)
			return;

		//--- loading categories and groups
		localCateg 	= new CategoryMapper (dbms);
		localGroups = new GroupMapper (dbms);

		//--- md5 the full catalog URL

        //--- load catalog
		InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalogImpl catalog = factory.readXML(params.url);
		StringBuilder buff = new StringBuilder();
		if (!catalog.check(buff, true)) {
			throw new BadXmlResponseEx("Invalid catalog "+ params.url+"\n"+buff.toString());
		}

		//--- display catalog read in log file
		log.info("Catalog read from "+params.url+" is \n"+factory.writeXML(catalog));
		URL url = new URL(params.url);
		String hostPart = url.getProtocol()+"://"+url.getHost()+":"+url.getPort();
		String serviceStyleSheet = context.getAppPath() + Geonet.Path.IMPORT_STYLESHEETS + "/ThreddsCatalog-to-ISO19119_ISO19139.xsl"; 

		//--- get datasets and services by crawling the catalog 
		//--- create ISO19139 records if user asked for this
		/* But crawler doesn't seem to work on all catalogues - so we use our own
		CatalogCrawler.Listener listener = new CatalogCrawler.Listener() {
			public void getDataset(InvDataset ds) {
				processOneDataset(ds);
			}
			public boolean getCatalogRef(InvCatalogRef dd) { return true; }
		};

		ByteArrayOutputStream bis = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( bis);
		CatalogCrawler crawler = new CatalogCrawler( CatalogCrawler.USE_ALL, true, listener);
		crawler.crawl(catalog, null, ps);
		log.info("Crawler output: "+bis.toString());
		*/

		//--- Crawl all datasets in the thredds catalogue
		log.info("Crawling the datasets in the catalog....");
		List<InvDataset> dsets = catalog.getDatasets();
		for (InvDataset ds : dsets) {
			crawlDatasets(ds);
		}

		//--- show how many datasets have been processed
		int totalDs =	result.collectionDatasetRecords + result.atomicDatasetRecords;
		log.info("Processed "+totalDs+" datasets.");

		if (params.createServiceMd) {
			//--- now process services found by crawling the catalog
			log.info("Processing "+services.size()+" services...");
			processServices(cata, hostPart, serviceStyleSheet);
	
			//--- finally create a service record for the thredds catalog itself and
			//--- add uuids of services that it provides to operatesOn element  
			//--- (not sure that this is what we should do here really - the catalog
			//--- is a dataset and a service??
			log.info("Creating service metadata for thredds catalog...");
			Map<String, String> param = new HashMap<String, String>();
			param.put("lang",			params.lang);
			param.put("topic",		params.topic);
			param.put("uuid",			params.uuid);
			param.put("url",			params.url);
			param.put("name",			catalog.getName());
			param.put("type",			"Thredds Data Service Catalog "+ catalog.getVersion());
			param.put("version",	catalog.getVersion());
			param.put("desc",			Xml.getString(cata));
			param.put("props",		catalog.getProperties().toString());
			param.put("serverops",		"");
	
			log.debug ("  - XSLT transformation using "+serviceStyleSheet);
			Element md = Xml.transform (cata, serviceStyleSheet, param);
	
		  //--- TODO: Add links to services provided by the thredds catalog - but 
			//--- where do we do this in ISO19119?
			saveMetadata(md, params.uuid);
			result.serviceRecords ++;
		}
	}

	//---------------------------------------------------------------------------
	/** 
	  * Crawl all datasets in the catalog recursively
		*
    * @param theDs     the dataset being processed 
		*/
	public void crawlDatasets(InvDataset theDs) {
		log.info("Crawling through "+theDs.getName());

		if (theDs.hasNestedDatasets()) {
			List<InvDataset> dsets = theDs.getDatasets();
			for (InvDataset ds : dsets) {
				crawlDatasets(ds);
			}
		} 
		processOneDataset(theDs); // process the dataset 
	}

	//---------------------------------------------------------------------------
	/** 
	  * Save the metadata to GeoNetwork's database 
		*
    * @param md			the metadata being saved
    * @param uuid		the uuid of the metadata being saved
		*/
	private void saveMetadata(Element md, String uuid) throws Exception {

		//--- strip the catalog namespace as it is not required
		md.removeNamespaceDeclaration(invCatalogNS);

		String schema = dataMan.autodetectSchema(md); // should be iso19139
		if (schema == null) {
			log.warning("Skipping metadata with unknown schema.");
			result.unknownSchema ++;
		}

		log.info("  - Adding metadata with " + uuid + " schema is set to " + schema + "\n XML is "+ Xml.getString(md));
		DateFormat df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
		Date date = new Date();
		
		String id = dataMan.insertMetadataExt(dbms, schema, md, context.getSerialFactory(), params.uuid, df.format(date), df.format(date), uuid, 1, null);
		
		int iId = Integer.parseInt(id);
		addPrivileges(id);
		addCategories(id);
		
		dataMan.setTemplateExt(dbms, iId, "n", null);
		dataMan.setHarvestedExt(dbms, iId, params.uuid, params.url);

		dataMan.indexMetadataGroup(dbms, id);
		
		dbms.commit();
	}

	//---------------------------------------------------------------------------
	/** 
	  * Process one dataset generating metadata as per harvesting node settings
	    * also add UUID of this dataset to list of uuids 
		* served by any services in the thredds catalogs. 
		*
    * @param ds     the dataset to be processed 
		*/
	private void processOneDataset(InvDataset ds) {
		log.info("Processing dataset: "+ds.getName());
		
		if (harvestMetadataUsingFragments(ds))	{
			createMetadataUsingFragments(ds);
		} else if (harvestDefaultMetadata(ds)) {
			createDefaultMetadata(ds); 
		} else {
		    log.info("Harvest not required - skipping dataset: "+ds.getName());
		}
	}

	//---------------------------------------------------------------------------
	/** 
     * Create metadata using fragments
     *
     * <ul>
     * <li>collect useful metadata for the dataset<li>
     * <li>use supplied stylesheet to convert collected metadata into fragments</li>
     * <li>harvest metadata from fragments as requested</li> 
     * </ul>
     * 
     * Metadata collected is as follows:
     *
     * <pre>
     * {@code
     * <root>
     *    <catalogUri>http://someserver.com/thredds/catalog.xml</catalog>
     *    <uuid>uuid-generated-for-dataset</uuid>
     *    <catalog xmlns="http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0" xmlns:xlink="http://www.w3.org/1999/xlink" version="1.0.1">
	 *		 ... subset of catalog containing dataset as the top dataset ...
	 *    </catalog>
	 *    <netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2"  location="example1.nc">
	 *       ... ncml generated for netcdf dataset ...
	 *       ... atomic datasets only ...
	 *    </netcdf>
	 * </root>
	 * }
	 * </pre>
     */
	private void createMetadataUsingFragments(InvDataset ds) {
		try {
			log.info("Retrieving thredds/netcdf metadata...");
			
			//--- Create root element to collect dataset metadata to be passed to xsl transformation
			Element dsMetadata = new Element("root");
		
			//--- Add catalog uri (url) to allow relative urls to be resolved
			dsMetadata.addContent(new Element("catalogUri").setText(ds.getParentCatalog().getUriString()));

			//--- Add uuid for dataset
			dsMetadata.addContent(new Element("uuid").setText(getUuid(ds)));
			
			//--- Add dataset subset catalog information to metadata
			dsMetadata.addContent(getDatasetSubset(ds));
			
			//--- For atomic dataset's add ncml for dataset to metadata
			if (!ds.hasNestedDatasets()) {
				NetcdfDataset ncD = NetcdfDataset.openDataset("thredds:"+ds.getCatalogUrl());
				NcMLWriter ncmlWriter = new NcMLWriter();
				Element ncml = Xml.loadString(ncmlWriter.writeXML(ncD),false);
				dsMetadata.addContent(ncml);
			}

			log.debug("Thredds metadata and ncml is:"+Xml.getString(dsMetadata));

			//TODO: validate stylesheet entered when creating parameters (not here)
			//--- Create fragments using provided stylesheet
			String stylesheet = ds.hasNestedDatasets() ? params.collectionFragmentStylesheet : params.atomicFragmentStylesheet;
			Element fragments = Xml.transform(dsMetadata, fragmentStylesheetDirectory + "/" + stylesheet);
			log.debug("Fragments generated for dataset:"+Xml.getString(fragments));
			
			//--- Create metadata/subtemplates from fragments
			FragmentHarvester fragmentHarvester = ds.hasNestedDatasets() ? collectionFragmentHarvester : atomicFragmentHarvester;
			HarvestSummary fragmentResult = fragmentHarvester.harvest(fragments);
			
			//--- Include fragment results in thredds results
			result.fragmentsReturned += fragmentResult.fragmentsReturned;
			result.fragmentsUnknownSchema += fragmentResult.fragmentsUnknownSchema;
			result.subtemplatesAdded += fragmentResult.fragmentsAdded;
			result.fragmentsMatched += fragmentResult.fragmentsMatched;

			if (ds.hasNestedDatasets()) {
				result.collectionDatasetRecords += fragmentResult.recordsBuilt;
			} else {
				result.atomicDatasetRecords += fragmentResult.recordsBuilt;
			}
			
			//--- Record uuid of dataset against services that deliver it for  
			//--- inclusion in operatesOn element in 19119 service record
			List<InvAccess> accesses = ds.getAccess();
			for (InvAccess access : accesses) {
				processService(access.getService(), getUuid(ds), ds);
			}
		} catch (Exception e) {
			log.error("Thrown Exception "+e+" during dataset processing");
			e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------
	/** 
	  * Return a catalog having the specified dataset as the top dataset
		* resolving inherited metadata and required services
		*
    * @param ds     the dataset to be processed 
		*/
	private Element getDatasetSubset(InvDataset ds) throws Exception {
		String datasetSubsetUrl;
		
		if (ds instanceof InvCatalogRef) {
			//return referenced catalog
			datasetSubsetUrl = ((InvCatalogRef)ds).getURI().toString(); 
		} else {
			//subset current catalog
			String catalogUrl = ds.getCatalogUrl().split("#")[0];
			datasetSubsetUrl = catalogUrl + "?dataset=" + ds.getID();
		}
		
		return Xml.loadFile(new URL(datasetSubsetUrl));
	}
	
	//---------------------------------------------------------------------------
	/** 
	  * Get uuid for dataset
		*
    * @param ds     the dataset to be processed 
		*/
	private String getUuid(InvDataset ds) {
		String uuid = ds.getUniqueID();
		
		if (uuid == null) {
			uuid = Util.scramble(ds.getCatalogUrl()); // md5 full dataset url
		} else {
			uuid = StringUtil.allow(uuid, "_-.",'-');
		}
		
		return uuid;
	}

	//---------------------------------------------------------------------------
	/** 
	  * Process one dataset by extracting its metadata, writing to DIF
		* and using xslt to transform to the required ISO format. 
		*
    * @param ds     the dataset to be processed 
		*/
	private void createDefaultMetadata(InvDataset ds) {
	try {

			boolean addCoordSys = false; // add coordinate systems if not DIF relaxed

			//--- TODO: Thredds has a metadata converter interface and some other
			//--- methods of handling metadata (including XML of different 
			//--- namespaces) in the catalog - this is a place holder for getting
			//--- this info in future
			List <InvMetadata> mds = ds.getMetadata();
			log.info("Dataset has "+mds.size()+" metadata elements");
			for (InvMetadata md : mds) {
				log.info("Found metadata "+md.toString());
			}

			//--- check and see whether this dataset is DIF writeable
			DIFWriter difWriter = new DIFWriter();
			StringBuffer sBuff = new StringBuffer();
			Element dif;

			if (difWriter.isDatasetUseable(ds, sBuff)) {
				log.info("Yay! Dataset has DIF compatible metadata "+sBuff.toString());

				dif = difWriter.writeOneEntry(ds, sBuff);

			} else {
				log.info("Dataset does not have DIF compatible metadata so we will write a relaxed DIF entry\n"+sBuff.toString());

				dif = difWriter.writeOneRelaxedEntry(ds, sBuff);
				addCoordSys = true;
			}

			//--- get the UUID assigned to the DIF record
			String uuid = dif.getChild("Entry_ID", difNS).getText();

			boolean isCollection = ds.hasNestedDatasets();
			log.info("Dataset is a collection dataset? "+isCollection);

			//--- now convert DIF entry into an ISO entry using the appropriate
			//--- difToIso converter (only support ISO and MCP profile)
			Element md;
			if (isCollection) {
				if (params.outputSchemaOnCollections.equals("iso19139")) {
					log.info("Transforming collection dataset to iso19139");
					md = Xml.transform(dif, difToIsoStyleSheet);
				} else if (params.outputSchemaOnCollections.equals("iso19139.mcp")) {
					log.info("Transforming collection dataset to iso19139.mcp");
					md = Xml.transform(dif, difToIsoMcpStyleSheet);
				} else {
					throw new BadParameterEx("outputSchemaOnCollections", params.outputSchemaOnCollections);
				}
			} else {
				if (params.outputSchemaOnAtomics.equals("iso19139")) {
					log.info("Transforming atomic dataset to iso19139");
					md = Xml.transform(dif, difToIsoStyleSheet);
				} else if (params.outputSchemaOnAtomics.equals("iso19139.mcp")) {
					log.info("Transforming atomic dataset to iso19139.mcp");
					md = Xml.transform(dif, difToIsoMcpStyleSheet);
				} else {
					throw new BadParameterEx("outputSchemaOnAtomics", params.outputSchemaOnAtomics);
				}
			}

			//--- if we don't have full set of DIF metadata then 
			//--- if atomic dataset then check dataset for global attributes 
			//--- and/or dump coordinate systems else
			//--- if collection then check for ThreddsMetadata.Variables and
			//--- create a netcdfInfo for addition to the ISO record
			if (addCoordSys) { 
				boolean globalAttributes = false;
				if (!isCollection) { // open up atomic dataset for info
					log.info("Opening dataset to get global attributes");
					//--- if not a dataset collection then 
					//--- open and check global attributes for metadata conventions
					try {
						NetcdfDataset ncD = NetcdfDataset.openDataset("thredds:"+ds.getCatalogUrl());
						Attribute mdCon = ncD.findGlobalAttributeIgnoreCase("metadata_conventions");
						if (mdCon != null) {
							List<Attribute> ga = ncD.getGlobalAttributes(); 
							for (Attribute att : ga ) {
								log.debug("Attribute found "+att.toString());
								//--- TODO: Attach the attributes to the metadata node
								//--- for conversion into the ISO record by an xslt
							}
						} else {
							log.debug("No global attribute with metadata conventions found");	
						}
						ncD.close();
					} catch (Exception e) {
						log.info("Exception raised in netcdfDataset ops: "+e);
						e.printStackTrace();
					}
				}

				//--- if no metadata conventions then find the coordinate systems 
				//--- and add these to the appropriate place in whatever ISO or ISO 
				//--- profile we are using - MCP: mcp:dataParameters & gmd:keywords, 
				//--- ISO: gmd:keywords
				boolean foundNetcdfInfo = false;
				if (!globalAttributes && !isCollection) {
					log.info("No global attributes describing metadata so opening dataset to get coordinate systems");
					try {
						NetcdfDatasetInfo ncDI = new NetcdfDatasetInfo("thredds:"+ds.getCatalogUrl());
						log.info("Coordinate systems builder is "+ncDI.getConventionUsed());
						if (!ncDI.getConventionUsed().equals("None")) {
							Document doc = ncDI.makeDocument();
							Element coords = doc.detachRootElement();
							log.info("Coordinate systems of dataset are: \n"+Xml.getString(coords));
							addKeywordsAndDataParams(coords, md);
							foundNetcdfInfo = true;
						} else {
							log.debug("Coordinate system convention is not recognized");	
						}
						ncDI.close();
					} catch (Exception e) {
						log.info("Exception raised in netcdfDatasetInfo ops: "+e);
						e.printStackTrace();
					}
				}

				//--- finally - check and see whether we can extract variables from the
				//--- ThreddsMetadata - we no longer care whether this is a collection
				//--- or atomic
				if (!globalAttributes && !foundNetcdfInfo) { 
					//--- get ThreddsMetadata.Variables and create a netcdfDatasetInfo 
					//--- document if possible
					List<ThreddsMetadata.Variables> vsL = ds.getVariables();
					if (vsL != null && vsL.size() > 0) {
						for (ThreddsMetadata.Variables vs : vsL) {
							String vHref = vs.getVocabHref();
							URI    vUri  = vs.getVocabUri();
							String vocab = vs.getVocabulary();
							Element coords = new Element("netcdfDatasetInfo");
							for (ThreddsMetadata.Variable v : vs.getVariableList()) {
								Element varX = new Element("variable");	
								varX.setAttribute("name",		v.getName());
								varX.setAttribute("decl",		v.getDescription());
								varX.setAttribute("units",	v.getUnits());
								// - these three attributes are new but then there is no
								// - xsd for this so we can add as we want!
								varX.setAttribute("vocab",	    vocab);
								varX.setAttribute("vocaburi", 	vUri.toString());
								varX.setAttribute("vocabhref",	vHref);
								coords.addContent(varX);
							}
							log.info("Coordinate systems from ThreddsMetadata are: \n"+Xml.getString(coords));
							addKeywordsAndDataParams(coords, md);
						}
					}
				}
			}

			//--- write metadata if user options require it
			if (params.createAtomicDatasetMd && !isCollection) {
				if (ds.isHarvest() || params.ignoreHarvestOnAtomics) {
					saveMetadata(md, uuid);
					result.atomicDatasetRecords ++;
				}
			}
			if (params.createCollectionDatasetMd && isCollection) {
				if (ds.isHarvest() || params.ignoreHarvestOnCollections) {
					saveMetadata(md, uuid);
					result.collectionDatasetRecords ++;
				}
			}
	
			//--- record uuid of dataset against services that deliver it for  
			//--- inclusion in operatesOn element in 19119 service record
			List<InvAccess> accesses = ds.getAccess();
			for (InvAccess access : accesses) {
				processService(access.getService(), uuid, ds);
			}

		} catch (Exception e) {
				log.error("Thrown Exception "+e+" during dataset processing");
				e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------
	/** 
	  * Process a netcdfinfo document - adding variables as keywords and 
		* mcp:DataParameters if the output schema requires.
		*
    * @param coords     the netcdfinfo document with coord systems embedded
    * @param md			    ISO metadata record to add keywords and data params to
		*/
	private void addKeywordsAndDataParams(Element coords, Element md) throws Exception {
		Element keywords = Xml.transform(coords, cdmCoordsToIsoKeywordsStyleSheet);
		addKeywords(md, keywords);
		if (params.outputSchemaOnAtomics.equals("iso19139.mcp") ||
				params.outputSchemaOnCollections.equals("iso19139.mcp")) {
			Element dataParameters = Xml.transform(coords, cdmCoordsToIsoMcpDataParametersStyleSheet);
			log.info("mcp:DataParameters are: \n"+Xml.getString(dataParameters));
			addDataParameters(md, dataParameters);
		}
	}

	//---------------------------------------------------------------------------
	/** 
	  * Process a service reference in a dataset - record details of the 
		* service and add the details of a dataset to the list of datasets it
		* serves - Note: compound services are expanded.
		*
    * @param serv     the service to be processed 
    * @param uuid     uuid of the dataset that is delivered by this service
    * @param ds		    dataset that is being delivered by this service
		*/
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
			ThreddsService ts = services.get(s.getName());
			if (ts == null) {
				ts = new ThreddsService();
				ts.service = s;
				ts.version = getVersion(serv, ds);
				ts.ops = getServerOperations(serv, ds); 
				services.put(s.getName(),ts);
			}
			ts.datasets.put(uuid,ds.getName());
		}

	}

	//---------------------------------------------------------------------------
	/** 
	  * Find the version of the service that delivers a particular dataset
		* Handles OPeNDAP and HTTP only at present
		*
    * @param serv     					the service that delivers the dataset
    * @param ds									the dataset being delivered by the service
		*/
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
		}
		return result;
	}
 
	//---------------------------------------------------------------------------
	/** 
	  * Get the server operations 
		* Applicable to OPeNDAP only at present
		*
    * @param serv     					the service that delivers the dataset
    * @param ds									the dataset being delivered by the service
		*/
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

	//---------------------------------------------------------------------------
	/** 
	  * Get a String result from an HTTP URL
		*
    * @param href     						the URL to get the info from
		*/
	private String getResultFromHttpUrl(String href) {
		String result = null;
		try {
			//--- get the version from the OPeNDAP server
			URL url = new URL(href);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			Object o = conn.getContent();
			log.debug("Opened "+href+" and got class "+o.getClass().getName());
			StringBuffer version = new StringBuffer();
			String inputLine;
			DataInputStream dis = new DataInputStream(conn.getInputStream());
			while ((inputLine = dis.readLine()) != null) {
					version.append(inputLine);
					version.append('\n');
			}
			result = version.toString();  
			log.debug("Read from URL:\n"+result);
			dis.close();
		} catch (Exception e) {
		log.debug("Caught exception "+e+" whilst attempting to query URL "+href);
			e.printStackTrace();
		} finally {
			return result;
		}
	}
 

	//---------------------------------------------------------------------------
	/** 
	  * Process all services that serve datasets in the thredds catalog
		*
    * @param cata     					the XML of the catalog
    * @param hostPart						host part of the catalog URL
		* @param serviceStyleSheet	name of the stylesheet to produce 19119
		*/
	private void processServices(Element cata, String hostPart, String serviceStyleSheet) throws Exception {

		for (String servName : services.keySet()) {
		
			ThreddsService ts = services.get(servName);
			InvService serv = ts.service;

			log.debug("Processing Thredds service: "+serv.toString());

			String sUrl;
			if (!serv.isRelativeBase()) {
				sUrl = serv.getBase();
			} else {
				sUrl = hostPart+serv.getBase();
			}
			String sUuid = Util.scramble(sUrl);
			ts.uuid = sUuid;

			//--- TODO: if service is WCS or WMS then pass the full service url to 
			//--- OGCWxS service metadata creator
			
			//---	pass info to stylesheet which will create a 19119 record
			
			log.debug("  - XSLT transformation using "+serviceStyleSheet);

			Map<String, String> param = new HashMap<String, String>();
			param.put("lang",		params.lang);
			param.put("topic",	params.topic);
			param.put("uuid",		sUuid);
			param.put("url",		sUrl);
			param.put("name",		serv.getName());
			param.put("type",		serv.getServiceType().toString().toUpperCase());
			param.put("version", ts.version);
			param.put("desc",		serv.toString());
			param.put("props",	serv.getProperties().toString());
			param.put("serverops", ts.ops);

			Element md = Xml.transform (cata, serviceStyleSheet, param);

			String schema = dataMan.autodetectSchema (md); 
			if (schema == null) {
				log.warning("Skipping metadata with unknown schema.");
				result.unknownSchema ++;
			} else {

	    	//--- Update ISO19119 for data/service links (ie. operatesOn element)
				md = addOperatesOnUuid (md, ts.datasets);

	    	//--- Now add to geonetwork 
				saveMetadata(md, sUuid);
				result.serviceRecords ++;
			}
		}       
	}

	//---------------------------------------------------------------------------
	/** 
     * Add an Element to a child list at index after specified element
     *  
     * @param md      		iso19139 metadata
     * @param theNewElem	the new element to be added
     * @param name				the name of the element to search for
     * @param ns					the namespace of the element to search for
     *                   
     */
	boolean addAfter(Element md, Element theNewElem, String name, Namespace ns) throws Exception {
		Element chSet = md.getChild(name, ns);
		if (chSet != null) {
			int pos = md.indexOf(chSet);
			md.addContent(pos+1, theNewElem);
			return true;
		}
		return false;
	 }

	//---------------------------------------------------------------------------
	/** 
     * Add keywords generated from CDM coordinate systems to identificationInfo
       
       <gmd:descriptiveKeywords>
	   		<gmd:MD_Keywords>
		 			<gmd:keyword>
		 				<gco:CharacterString>
		 				</gco:CharacterString>
		 			</gmd:keyword>
		 			...
		 			...
		 			...
		 			<gmd:type>
		 				<gmd:MD_KeywordType codelist...>
		 			</gmd:type>
		 			<gmd:thesaurusName>
		 				<gmd:CI_Citation>
		 					....
		 				</gmd:CI_Citation>
		 			</gmd:thesaurusName>
	   		</gmd:MD_Keywords>
       </gmd:descriptiveKeywords>
     	
     * @param md        iso19139 metadata
     * @param keywords	gmd:keywords block to be added to metadata
     *                   
     */
	 private Element addKeywords (Element md, Element keywords) throws Exception {
		Element root	= (Element)md.getChild("identificationInfo", gmd).getChildren().get(0);
		boolean ok = addAfter(root, keywords, "descriptiveKeywords", gmd);
		if (!ok) {
			throw new BadXmlResponseEx("The metadata did not have a descriptiveKeywords Element");
		}
		return md;
	}
	
	//---------------------------------------------------------------------------
	/** 
     * Add mcp:dataParameters created from CDM coordinate systems to 
		 * identificationInfo (mcp only)
       
       <mcp:dataParameters>
	   		<mcp:DP_DataParameters>
		 			...
		 			...
		 			...
	   		</mcp:DP_DataParameters>
       </mcp:dataParameters>
     	
     * @param md        			iso19139 MCP metadata
     * @param dataParameters	mcp:dataParameters block to be added to metadata
     *                   
     */
	 private Element addDataParameters (Element md, Element dataParameters) throws Exception {
		Element root	= (Element)md.getChild("identificationInfo", gmd).getChildren().get(0);
		root.addContent(dataParameters); // this is dependent on the mcp schema
		return md;
	}
	

	//---------------------------------------------------------------------------
	/** 
     * Add OperatesOn elements on an ISO19119 metadata
     *  
     *  <srv:operatesOn>
	   *		<gmd:MD_DataIdentification uuidref=""/>
	   *	</srv:operatesOn>
     *	
     * @param md        iso19119 metadata
     * @param datasets  HashMap of datasets with uuids to be added
     *                   
     */
	 private Element addOperatesOnUuid (Element md, Map<String,String> datasets) {
		Element root 	= md.getChild("identificationInfo", gmd).getChild("SV_ServiceIdentification", srv);

        if (root != null) {
			log.debug("  - add operatesOn with uuid and other attributes");
			
			for (String dsUuid : datasets.keySet()) {
				Element op = new Element ("operatesOn", srv);
				op.setAttribute("uuidref", dsUuid);
				op.setAttribute("href", context.getBaseUrl() + "/srv/en/metadata.show?uuid=" + dsUuid, xlink);
				op.setAttribute("title", datasets.get(dsUuid), xlink);
				root.addContent(op);
			}
		}
		
		return md;
	}
	
	//---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
	/** 
     * Add categories according to harvesting configuration
     *   
     * @param id		GeoNetwork internal identifier
     * 
     */
	private void addCategories (String id) throws Exception {
		for(String catId : params.getCategories ()) {
			String name = localCateg.getName (catId);

			if (name == null) {
				log.debug ("    - Skipping removed category with id:"+ catId);
			} else {
				dataMan.setCategory (dbms, id, catId);
			}
		}
	}

	//---------------------------------------------------------------------------
	/** 
     * Add privileges according to harvesting configuration
     *   
     * @param id		GeoNetwork internal identifier
     * 
     */
	private void addPrivileges (String id) throws Exception {
		for (Privileges priv : params.getPrivileges ()) {
			String name = localGroups.getName( priv.getGroupId ());

			if (name == null) {
				log.debug ("    - Skipping removed group with id:"+ priv.getGroupId ());
			} else {
				for (int opId: priv.getOperations ()) {
					name = dataMan.getAccessManager().getPrivilegeName(opId);

					//--- allow only: view, dynamic, featured
					if (opId == 0 || opId == 5 || opId == 6) {
						dataMan.setOperation(dbms, id, priv.getGroupId(), opId +"");
					} else {
						log.debug("       --> "+ name +" (skipped)");
					}
				}
			}
		}
	}

    //---------------------------------------------------------------------------
    /** 
     * Determine whether dataset metadata should be harvested using fragments  
     *
     * @param ds     the dataset to be checked
     */
    private boolean harvestMetadataUsingFragments(InvDataset ds) {
        if (isCollection(ds)) {
            return params.createCollectionDatasetMd && (params.ignoreHarvestOnCollections || ds.isHarvest()) && 
                params.collectionMetadataGeneration.equals(ThreddsParams.FRAGMENTS);
        } else {
            return params.createAtomicDatasetMd && (params.ignoreHarvestOnAtomics || ds.isHarvest()) &&
                params.atomicMetadataGeneration.equals(ThreddsParams.FRAGMENTS);
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * Determine whether dataset metadata should be harvested using fragments  
     *
     * @param ds     the dataset to be checked
     */
    private boolean harvestDefaultMetadata(InvDataset ds) {
        if (isCollection(ds)) {
            return params.createCollectionDatasetMd && (params.ignoreHarvestOnCollections || ds.isHarvest()) && 
                params.collectionMetadataGeneration.equals(ThreddsParams.DEFAULT);
        } else {
            return params.createAtomicDatasetMd && (params.ignoreHarvestOnAtomics || ds.isHarvest()) &&
                params.atomicMetadataGeneration.equals(ThreddsParams.DEFAULT);
        }
    }

	//---------------------------------------------------------------------------
	/** 
	 * Determine whether dataset is a collection i.e. has nested datasets
     *
     * @param ds     the dataset to be checked
     */
	private boolean isCollection(InvDataset ds) {
		return ds.hasNestedDatasets();
	}

	//---------------------------------------------------------------------------
	/** 
     * Get fragment harvesting parameters for collection datasets
     *   
     * @return		fragment harvesting parameters for collection datasets
     * 
     */
	private FragmentParams getCollectionFragmentParams() {
		FragmentParams collectionParams = new FragmentHarvester.FragmentParams();
		collectionParams.categories = params.getCategories();
		collectionParams.createSubtemplates = params.createCollectionSubtemplates;
		collectionParams.isoCategory = params.datasetCategory;
		collectionParams.privileges = params.getPrivileges();
		collectionParams.templateId = params.collectionMetadataTemplate;
		collectionParams.url = params.url;
		collectionParams.uuid = params.uuid;
		return collectionParams;
	}

	//---------------------------------------------------------------------------
	/** 
     * Get fragment harvesting parameters for atomic datasets
     *   
     * @return		fragment harvesting parameters for atomic datasets
     * 
     */
	private FragmentParams getAtomicFragmentParams() {
		FragmentParams atomicParams = new FragmentHarvester.FragmentParams();
		atomicParams.categories = params.getCategories();
		atomicParams.createSubtemplates = params.createAtomicSubtemplates;
		atomicParams.isoCategory = params.datasetCategory;
		atomicParams.privileges = params.getPrivileges();
		atomicParams.templateId = params.atomicMetadataTemplate;
		atomicParams.url = params.url;
		atomicParams.uuid = params.uuid;
		return atomicParams;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private Dbms           dbms;
	private ThreddsParams  params;
	private DataManager    dataMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
    private ThreddsResult  result;
    private String				 difToIsoStyleSheet;
	private String				 difToIsoMcpStyleSheet; 
	private String				 cdmCoordsToIsoKeywordsStyleSheet; 
	private String				 cdmCoordsToIsoMcpDataParametersStyleSheet;
	private String				 fragmentStylesheetDirectory;
	private String	 			 metadataGetService;
	private Map<String,ThreddsService> services = new HashMap<String, ThreddsService>();

    private FragmentHarvester atomicFragmentHarvester;
	private FragmentHarvester collectionFragmentHarvester;

	private class ThreddsService {
		public String uuid;
		public Map<String,String> datasets = new HashMap<String, String>();
		public InvService service;
		public String version;
		public String ops;
	}

	static private final Namespace difNS = Namespace.getNamespace("http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/");
	static private final Namespace invCatalogNS = Namespace.getNamespace("http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0");
	static private final Namespace gmd 	= Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    static private final Namespace srv 	= Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
	static private final Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
		
}

//=============================================================================

