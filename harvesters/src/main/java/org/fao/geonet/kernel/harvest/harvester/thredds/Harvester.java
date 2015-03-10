//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	Copyright (C) 2008-2011 CSIRO Marine and Atmospheric Research, 
//=== Australia
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
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.BadServerCertificateEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UriMapper;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.FragmentParams;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.HarvestSummary;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Document;
import org.jdom.Element;
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
import ucar.nc2.units.DateType;
import ucar.unidata.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.SSLHandshakeException;

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
 * 	<li>ISO19139 (or profile) metadata for datasets in catalog</li>
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
 * Choice of {
 *      <outputSchemaOnCollectionsDIF>iso19139</outputSchemaOnCollectionsDIF>
 * } OR {
 *      <outputSchemaOnCollectionsFragments>iso19139</outputSchemaOnCollectionsFragments>
 *      <collectionFragmentStylesheet>collection_fragments.xsl</collectionFragmentStylesheet>
 *      <collectionMetadataTemplate>My template</collectionMetadataTemplate>
 *      <createCollectionSubtemplates>false</createCollectionSubtemplates>
 * }
 *      <ignoreHarvestOnAtomics>true</ignoreHarvestOnAtomics>
 * Choice of {
 *      <outputSchemaOnAtomicsDIF>iso19139.mcp</outputSchemaOnAtomicsDIF>
 * } OR {
 *      <outputSchemaOnAtomicsFragments>iso19139</outputSchemaOnAtomicsFragments>
 *      <atomicFragmentStylesheet>atomic_fragments.xsl</atomicFragmentStylesheet>
 *      <atomicMetadataTemplate>My template</atomicMetadataTemplate>
 *      <createAtomicSubtemplates>false</createAtomicSubtemplates>
 * }
 *      <modifiedOnly>true</modifiedOnly>
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
class Harvester extends BaseAligner implements IHarvester<HarvestResult>
{
	
	
	//---------------------------------------------------------------------------
	/** 
	 * Constructor
	 *  
	 *
     * @param cancelMonitor
     * @param log
     * @param context        Jeeves context
     * @param params    Information about harvesting configuration for the node
     *
     * @return null
     **/
	
	public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, ThreddsParams params) {
        super(cancelMonitor);
		this.log    = log;
		this.context= context;
		this.params = params;

		result = new HarvestResult ();
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext (Geonet.CONTEXT_NAME);
		dataMan = gc.getBean(DataManager.class);
		schemaMan = gc.getBean(SchemaManager.class);

		SettingInfo si = context.getBean(SettingInfo.class);
		String siteUrl = si.getSiteUrl() + context.getBaseUrl();
		metadataGetService = siteUrl + "/srv/en/xml.metadata.get";

		
		//--- Create fragment harvester for atomic datasets if required
		if (params.createAtomicDatasetMd && params.atomicMetadataGeneration.equals(ThreddsParams.FRAGMENTS)) {
			atomicFragmentHarvester = new FragmentHarvester(cancelMonitor, log, context, getAtomicFragmentParams());
		}
		
		//--- Create fragment harvester for collection datasets if required
		if (params.createCollectionDatasetMd && params.collectionMetadataGeneration.equals(ThreddsParams.FRAGMENTS)) {
			collectionFragmentHarvester = new FragmentHarvester(cancelMonitor, log, context, getCollectionFragmentParams());
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
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

                    if (log.isDebugEnabled()) log.debug("  - Removing deleted metadata with id: " + record.id);
                    dataMan.deleteMetadata(context, record.id);

                    if (record.isTemplate.equals("s")) {
                        //--- Uncache xlinks if a subtemplate
                        Processor.uncacheXLinkUri(metadataGetService + "?uuid=" + record.uuid);
                        result.subtemplatesRemoved++;
                    } else {
                        result.locallyRemoved++;
                    }
                }
            }
        }

        dataMan.flush();

        result.totalMetadata = result.serviceRecords + result.collectionDatasetRecords + result.atomicDatasetRecords;
        return result;
    }

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	//---------------------------------------------------------------------------
	/** 
     * Add metadata to GN for the services and datasets in a thredds 
	 * catalog
     *  
	 * 1. Open Catalog Document
	 * 2. Crawl the catalog processing datasets as ISO19139 records 
	 * and recording services (attach dataset ids to the services that deliver
	 * them)
	 * 3. Process services found as ISO19119 records
	 * 4. Create a service record for the thredds catalog service provided and 
	 * list service records as something that the thredds catalog provides
	 * 5. Save all
     *	
     * @param cata      Catalog document
     *                   
     **/
	
	 private void harvestCatalog (Element cata) throws Exception {

		if (cata == null)
			return;

		//--- loading categories and groups
		localCateg 	= new CategoryMapper (context);
		localGroups = new GroupMapper (context);

		//--- Setup proxy authentication  
		Lib.net.setupProxy(context);
		
		//--- load catalog
		InvCatalogFactory factory = new InvCatalogFactory("default", true);
		catalog = (InvCatalogImpl) factory.readXML(params.url);
		StringBuilder buff = new StringBuilder();
		if (!catalog.check(buff, true)) {
			throw new BadXmlResponseEx("Invalid catalog "+ params.url+"\n"+buff.toString());
		}

		//--- display catalog read in log file
		log.info("Catalog read from "+params.url+" is \n"+factory.writeXML(catalog));
		Path serviceStyleSheet = context.getAppPath().
                resolve(Geonet.Path.IMPORT_STYLESHEETS).
                resolve("ThreddsCatalog-to-ISO19119_ISO19139.xsl");

		//--- Get base host url
	    URL url = new URL(params.url);
		hostUrl = url.getProtocol()+"://"+url.getHost();
		if (url.getPort() != -1) hostUrl += ":"+url.getPort();
		
		//--- Crawl all datasets in the thredds catalogue
		log.info("Crawling the datasets in the catalog....");
		List<InvDataset> dsets = catalog.getDatasets();
		for (InvDataset ds : dsets) {
            if (cancelMonitor.get()) {
                return ;
            }

            crawlDatasets(ds);
		}

		//--- show how many datasets have been processed
		int totalDs =	result.collectionDatasetRecords + result.atomicDatasetRecords;
		log.info("Processed "+totalDs+" datasets.");

		if (params.createServiceMd) {
			//--- process services found by crawling the catalog
			log.info("Processing "+services.size()+" services...");
			processServices(cata, serviceStyleSheet);
	
			//--- finally create a service record for the thredds catalog itself and
			//--- add uuids of services that it provides to operatesOn element  
			//--- (not sure that this is what we should do here really - the catalog
			//--- is a dataset and a service??
			log.info("Creating service metadata for thredds catalog...");
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("lang",			params.lang);
			param.put("topic",		params.topic);
			param.put("uuid", params.getUuid());
			param.put("url",			params.url);
			param.put("name",			catalog.getName());
			param.put("type",			"Thredds Data Service Catalog "+catalog.getVersion());
			param.put("version",	catalog.getVersion());
			param.put("desc",			Xml.getString(cata));
			param.put("props",		catalog.getProperties().toString());
			param.put("serverops",		"");

            if(log.isDebugEnabled()) log.debug ("  - XSLT transformation using "+serviceStyleSheet);
			Element md = Xml.transform (cata, serviceStyleSheet, param);
	
			//--- TODO: Add links to services provided by the thredds catalog - but 
			//--- where do we do this in ISO19119?
			saveMetadata(md, Sha1Encoder.encodeString (params.url), params.url);
			
			harvestUris.add(params.url);
			
			result.serviceRecords ++;
		}
	}

	//---------------------------------------------------------------------------
	/** 
	 * Crawl all datasets in the catalog recursively
	 *
     * @param	catalogDs		the dataset being processed 
	 * @throws	Exception 
	 **/
	 
	private void crawlDatasets(InvDataset catalogDs) throws Exception {
		log.info("Crawling through "+catalogDs.getName());
		
		// HACK!! Get real dataset hidden by netcdf library when catalog ref name
		// equals top dataset name in referenced catalog
		InvDataset realDs = catalogDs;
		if (catalogDs instanceof InvCatalogRef) {
			InvDataset proxyDataset = ((InvCatalogRef)catalogDs).getProxyDataset();
			realDs = proxyDataset.getName().equals(catalogDs.getName())?proxyDataset:catalogDs;
		}

		if (realDs.hasNestedDatasets()) {
			List<InvDataset> dsets = realDs.getDatasets();
			for (InvDataset ds : dsets) {
				crawlDatasets(ds);
			}
		} 
		
		if (harvestMetadata(realDs)) {
		    log.info("Harvesting dataset: " + realDs.getName());
			harvest(realDs); 
		} else {
		    log.info("Skipping dataset: " + realDs.getName());
		}
		
		// Release resources allocated when crawling catalog references
		if (catalogDs instanceof InvCatalogRef) {
			((InvCatalogRef)catalogDs).release();
		}
	}

	//---------------------------------------------------------------------------
	/** 
	 * Save the metadata to GeoNetwork's database 
	 *
     * @param md		the metadata being saved
     * @param uuid		the uuid of the metadata being saved
     * @param uri		the uri from which the metadata has been harvested
	 **/
	
	private void saveMetadata(Element md, String uuid, String uri) throws Exception {

		//--- strip the catalog namespace as it is not required
		md.removeNamespaceDeclaration(invCatalogNS);

		String schema = dataMan.autodetectSchema(md, null); // should be iso19139
		if (schema == null) {
			log.warning("Skipping metadata with unknown schema.");
			result.unknownSchema ++;
		}

		log.info("  - Adding metadata with " + uuid + " schema is set to " + schema + "\n XML is "+ Xml.getString(md));

		deleteExistingMetadata(uri);

		//
        // insert metadata
        //
        Metadata metadata = new Metadata().setUuid(uuid);
        metadata.getDataInfo().
                setSchemaId(schema).
                setRoot(md.getQualifiedName()).
                setType(MetadataType.METADATA);
        metadata.getSourceInfo().
                setSourceId(params.getUuid()).
                setOwner(Integer.parseInt(params.getOwnerId()));
        metadata.getHarvestInfo().
                setHarvested(true).
                setUuid(params.getUuid()).
                setUri(uri);

        addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);
        metadata = dataMan.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        dataMan.indexMetadata(id, true);

        dataMan.flush();
    }

	//---------------------------------------------------------------------------
	/** 
	 * Process one dataset generating metadata as per harvesting node settings
	 *
     * @param 	ds	the dataset to be processed 
	 * @throws	Exception 
	 **/
	
	private void harvest(InvDataset ds) throws Exception {
		//--- harvest metadata only if the dataset has changed
		if (!params.modifiedOnly || datasetChanged(ds)) {
			if (harvestMetadataUsingFragments(ds))	{
				createMetadataUsingFragments(ds);
			} else {
				createDIFMetadata(ds); 
			}
		}
		
		//--- Add dataset uri to list of harvested uri's
		harvestUris.add(getUri(ds));

		//--- Record uuid of dataset against services that deliver it for  
		//--- inclusion in operatesOn element in 19119 service record
		List<InvAccess> accesses = ds.getAccess();
		for (InvAccess access : accesses) {
			processService(access.getService(), getUuid(ds), ds);
		}
	}

	//---------------------------------------------------------------------------
	/** 
	 * Get dataset uri 
	 *
     * @param 	ds	the dataset to be processed
     *  
	 **/
	
	private String getUri(InvDataset ds) {
		if (ds.getID() == null) {
			return ds.getParentCatalog().getUriString() + "#" + ds.getName();
		} else {
		    return getSubsetUrl(ds);
		}
    }

	//---------------------------------------------------------------------------
	/** 
	 * Has the dataset has been modified since its metadata was last
	 * harvested  
	 *
     * @param 	ds	the dataset to be processed
     *  
	 **/
	
	private boolean datasetChanged(InvDataset ds) {
		List<RecordInfo> localRecords = localUris.getRecords(getUri(ds));
		
		if (localRecords == null) return true;
		
	    Date lastModifiedDate  = null;
		
		List<DateType> dates = ds.getDates();
		
		for (DateType date: dates) {
			if (date.getType().equalsIgnoreCase("modified")) {
				lastModifiedDate = date.getDate();
			}
		}
		
		if (lastModifiedDate == null) return true;

		String datasetModifiedDate = new ISODate(lastModifiedDate.getTime(), false).toString();
		
		for (RecordInfo localRecord: localRecords) {
			if (localRecord.isOlderThan(datasetModifiedDate)) return true;
		}
		
		return false;
    }

	//---------------------------------------------------------------------------
	/** 
	 * Delete all metadata previously harvested for a particular uri 
	 *
     * @param 	uri		uri for which previously harvested metadata should be deleted
     *  
	 **/
	
	private void deleteExistingMetadata(String uri) throws Exception {
		List<RecordInfo> localRecords = localUris.getRecords(uri);
		
		if (localRecords == null) return;

		for (RecordInfo record: localRecords) {
			dataMan.deleteMetadata (context, record.id);

			if (record.isTemplate.equals("s")) {
				//--- Uncache xlinks if a subtemplate
				Processor.uncacheXLinkUri(metadataGetService+"?uuid=" + record.uuid);
			}
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
     **/
	
	private void createMetadataUsingFragments(InvDataset ds) {
		try {
			log.info("Retrieving thredds/netcdf metadata...");

			//--- Create root element to collect dataset metadata to be passed to xsl transformation
			Element dsMetadata = new Element("root");
		
			//--- Add catalog uri (url) to allow relative urls to be resolved
			dsMetadata.addContent(new Element("catalogUri").setText(ds.getParentCatalog().getUriString()));

			//--- Add suggested uuid for dataset
			dsMetadata.addContent(new Element("uuid").setText(getUuid(ds)));
			
			//--- Add fullName of dataset
			dsMetadata.addContent(new Element("fullName").setText(ds.getFullName()));

			//--- Add dataset subset catalog information to metadata
			dsMetadata.addContent(getDatasetSubset(ds));
			
			//--- For atomic dataset's add ncml for dataset to metadata
			if (!ds.hasNestedDatasets()) {
				NetcdfDataset ncD = NetcdfDataset.openDataset("thredds:"+ds.getCatalogUrl());
				NcMLWriter ncmlWriter = new NcMLWriter();
				Element ncml = Xml.loadString(ncmlWriter.writeXML(ncD),false);
				dsMetadata.addContent(ncml);
			}

            if(log.isDebugEnabled()) log.debug("Thredds metadata and ncml is:"+Xml.getString(dsMetadata));

			//--- Create fragments using provided stylesheet

			String schema = ds.hasNestedDatasets() ? params.outputSchemaOnCollectionsFragments : params.outputSchemaOnAtomicsFragments;
			fragmentStylesheetDirectory = schemaMan.getSchemaDir(schema).resolve(Geonet.Path.TDS_STYLESHEETS);
			String stylesheet = ds.hasNestedDatasets() ? params.collectionFragmentStylesheet : params.atomicFragmentStylesheet;

			Element fragments = Xml.transform(dsMetadata, fragmentStylesheetDirectory.resolve(stylesheet));
            if(log.isDebugEnabled()) log.debug("Fragments generated for dataset:"+Xml.getString(fragments));
			
			//--- remove any previously harvested metadata/sub-templates
			deleteExistingMetadata(getUri(ds));
			
			//--- Create metadata/subtemplates from fragments
			FragmentHarvester fragmentHarvester = ds.hasNestedDatasets() ? collectionFragmentHarvester : atomicFragmentHarvester;
			HarvestSummary fragmentResult = fragmentHarvester.harvest(fragments, getUri(ds));
			
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
		String datasetSubsetUrl = getSubsetUrl(ds);
		
		return Xml.loadFile(new URL(datasetSubsetUrl));
	}

	//---------------------------------------------------------------------------
	/** 
	 * Return url to a catalog having the specified dataset as the top dataset
	 *
     * @param ds     the dataset to be processed 
	 **/
	
	private String getSubsetUrl(InvDataset ds) {
	    try {
	        return ds.getParentCatalog().getUriString() + "?dataset=" + URLEncoder.encode(ds.getID(), Constants.ENCODING);
        } catch (UnsupportedEncodingException e) {
			log.error("Thrown Exception "+e+" during dataset processing");
	        e.printStackTrace();
        }
        return null;
    }
	
	//---------------------------------------------------------------------------
	/** 
	 * Get uuid for dataset
	 *
     * @param ds     the dataset to be processed 
	 **/
	
	private String getUuid(InvDataset ds) {
		String uuid = ds.getUniqueID();
		
		if (uuid == null) {
			uuid = Sha1Encoder.encodeString (ds.getCatalogUrl()); // md5 full dataset url
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
	
	private void createDIFMetadata(InvDataset ds) {
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
			Element dif = null;

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
			//--- difToIso converter (only schemas with a DIF converter are 
			//--- supplied to the user for choice)
			Element md = null;
			if (isCollection) {
				Path difToIsoStyleSheet = schemaMan.getSchemaDir(params.outputSchemaOnCollectionsDIF).
                        resolve(Geonet.Path.DIF_STYLESHEETS).
                        resolve("DIFToISO.xsl");
				log.info("Transforming collection dataset to "+params.outputSchemaOnCollectionsDIF);
				md = Xml.transform(dif, difToIsoStyleSheet);
			} else {
				Path difToIsoStyleSheet = schemaMan.getSchemaDir(params.outputSchemaOnAtomicsDIF).
                        resolve(Geonet.Path.DIF_STYLESHEETS).
                        resolve("DIFToISO.xsl");
				log.info("Transforming atomic dataset to "+params.outputSchemaOnAtomicsDIF);
				md = Xml.transform(dif, difToIsoStyleSheet);
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
                                if(log.isDebugEnabled()) log.debug("Attribute found "+att.toString());
								//--- TODO: Attach the attributes to the metadata node
								//--- for conversion into the ISO record by an xslt
							}
						} else {
                            if(log.isDebugEnabled()) log.debug("No global attribute with metadata conventions found");
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
							setCoordsStyleSheet(isCollection);
							addKeywordsAndDataParams(coords, md);
							foundNetcdfInfo = true;
						} else {
                            if(log.isDebugEnabled()) log.debug("Coordinate system convention is not recognized");
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
							setCoordsStyleSheet(isCollection);
							addKeywordsAndDataParams(coords, md);
						}
					}
				}
			}

			//--- write metadata
			saveMetadata(md, uuid, getUri(ds));

			//--- update totals
			if (isCollection) {
				result.collectionDatasetRecords ++;
			} else {
				result.atomicDatasetRecords ++;
			}
		} catch (Exception e) {
				log.error("Thrown Exception "+e+" during dataset processing");
				e.printStackTrace();
		}
	}

	//---------------------------------------------------------------------------
	/** 
	 * Create the coordinate stylesheet names that will be used to add 
	 * gmd:keywords and mcp:DataParameters if the output schema requires.
	 *
	 * @param	isCollection true if we are working with a collection dataset
	 */
	private void setCoordsStyleSheet(boolean isCollection) {

		Path schemaDir;
		if (!isCollection) {
			schemaDir = schemaMan.getSchemaDir(params.outputSchemaOnAtomicsDIF);
		} else {
			schemaDir = schemaMan.getSchemaDir(params.outputSchemaOnCollectionsDIF);
		}

		cdmCoordsToIsoKeywordsStyleSheet = schemaDir.resolve(Geonet.Path.DIF_STYLESHEETS).
                resolve("CDMCoords-to-ISO19139Keywords.xsl");

		// -- FIXME: This is still schema dependent and needs to be improved
		// -- What we wait upon is finalization of the new coverage data parameters
		// -- metadata elements (inside MD_ContentInformation) in ISO19115/19139
		if (schemaDir.toString().contains("iso19139.mcp")) {
			cdmCoordsToIsoMcpDataParametersStyleSheet = schemaDir.resolve(Geonet.Path.DIF_STYLESHEETS).
                    resolve("/CDMCoords-to-ISO19139MCPDataParameters.xsl");
		} else {
			cdmCoordsToIsoMcpDataParametersStyleSheet = null;
		}
	}

	//---------------------------------------------------------------------------
	/** 
	 * Process a netcdfinfo document - adding variables as keywords and 
	 * mcp:DataParameters if the output schema requires.
	 *
	 * @param	coords	the netcdfinfo document with coord systems embedded
	 * @param	md		ISO metadata record to add keywords and data params to
	 **/
	
	private void addKeywordsAndDataParams(Element coords, Element md) throws Exception {
		Element keywords = Xml.transform(coords, cdmCoordsToIsoKeywordsStyleSheet);
		addKeywords(md, keywords);
		if (cdmCoordsToIsoMcpDataParametersStyleSheet != null) {
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
				sUrl = hostUrl+s.getBase();
			}

			ThreddsService ts = services.get(sUrl);
			if (ts == null) {
				ts = new ThreddsService();
				ts.service = s;
				ts.version = getVersion(serv, ds);
				ts.ops = getServerOperations(serv, ds);
				
				services.put(sUrl,ts);
			}
			ts.datasets.put(uuid,ds.getName());
		}

	}

	//---------------------------------------------------------------------------
	/** 
	 * Find the version of the service that delivers a particular dataset
	 * Handles OPeNDAP and HTTP only at present
	 *
	 * @param	serv	the service that delivers the dataset
	 * @param	ds		the dataset being delivered by the service
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
		}
		return result;
	}
 
	//---------------------------------------------------------------------------
	/** 
	 * Get the server operations 
	 * Applicable to OPeNDAP only at present
	 *
	 * @param	serv	the service that delivers the dataset
	 * @param	ds		the dataset being delivered by the service
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

	//---------------------------------------------------------------------------
	/** 
	 * Get a String result from an HTTP URL
	 *
	 * @param 	href		the URL to get the info from
	 **/
	
	private String getResultFromHttpUrl(String href) {
		String result = null;
		try {
			//--- get the version from the OPeNDAP server
			URL url = new URL(href);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			Object o = conn.getContent();
            if(log.isDebugEnabled()) log.debug("Opened "+href+" and got class "+o.getClass().getName());
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
    					version.append(inputLine+"\n");	
    			}
    			result = version.toString();
                if(log.isDebugEnabled()) log.debug("Read from URL:\n"+result);
			} finally {
			    IOUtils.closeQuietly(is);
			    IOUtils.closeQuietly(isr);
			    IOUtils.closeQuietly(dis);
			}
		} catch (Exception e) {
            if(log.isDebugEnabled()) log.debug("Caught exception "+e+" whilst attempting to query URL "+href);
			e.printStackTrace();
		}
		return result;
	}
 
	//---------------------------------------------------------------------------
	/** 
	 * Process all services that serve datasets in the thredds catalog
	 *
	 * @param	cata				the XML of the catalog
	 * @param	serviceStyleSheet	name of the stylesheet to produce 19119
	 **/
	
	private void processServices(Element cata, Path serviceStyleSheet) throws Exception {

		for (String sUrl : services.keySet()) {
		
			ThreddsService ts = services.get(sUrl);
			InvService serv = ts.service;

            if(log.isDebugEnabled()) log.debug("Processing Thredds service: "+serv.toString());

			String sUuid = Sha1Encoder.encodeString (sUrl);

			//--- TODO: if service is WCS or WMS then pass the full service url to 
			//--- OGCWxS service metadata creator
			
			//---	pass info to stylesheet which will create a 19119 record

            if(log.isDebugEnabled()) log.debug("  - XSLT transformation using "+serviceStyleSheet);

			Map<String, Object> param = new HashMap<String, Object>();
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

			String schema = dataMan.autodetectSchema (md, null); 
			if (schema == null) {
				log.warning("Skipping metadata with unknown schema.");
				result.unknownSchema ++;
			} else {

	    	//--- Update ISO19119 for data/service links (ie. operatesOn element)
				md = addOperatesOnUuid (md, ts.datasets);

	    	//--- Now add to geonetwork 
				saveMetadata(md, sUuid, sUrl);

				harvestUris.add(sUrl);
				
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
     **/
	
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
     **/
	
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
     **/
	 
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
     **/
	 
	 private Element addOperatesOnUuid (Element md, Map<String,String> datasets) {
		Element root 	= md.getChild("identificationInfo", gmd).getChild("SV_ServiceIdentification", srv);
//		Element co 		= root.getChild("containsOperations", srv);

		if (root != null) {
            if(log.isDebugEnabled()) log.debug("  - add operatesOn with uuid and other attributes");
			
			for (Map.Entry<String, String> entry : datasets.entrySet()) {
			    String dsUuid = entry.getKey();
			    
				Element op = new Element ("operatesOn", srv);
				op.setAttribute("uuidref", dsUuid);
				op.setAttribute("href", context.getBaseUrl() + "/srv/en/metadata.show?uuid=" + dsUuid, xlink);
				op.setAttribute("title", entry.getValue(), xlink);
				root.addContent(op);
			}
		}
		
		return md;
	}
	
	//---------------------------------------------------------------------------
    /** 
     * Determine whether dataset metadata should be harvested  
     *
     * @param ds     the dataset to be checked
     **/
	
    private boolean harvestMetadata(InvDataset ds) {
        if (isCollection(ds)) {
            return params.createCollectionDatasetMd && (params.ignoreHarvestOnCollections || ds.isHarvest());
        } else {
            return params.createAtomicDatasetMd && (params.ignoreHarvestOnAtomics || ds.isHarvest());
        }
    }

    //---------------------------------------------------------------------------
    /** 
     * Determine whether dataset metadata should be harvested using fragments  
     *
     * @param ds     the dataset to be checked
     **/
    
    private boolean harvestMetadataUsingFragments(InvDataset ds) {
        if (isCollection(ds)) {
            return params.collectionMetadataGeneration.equals(ThreddsParams.FRAGMENTS);
        } else {
            return params.atomicMetadataGeneration.equals(ThreddsParams.FRAGMENTS);
        }
    }

	//---------------------------------------------------------------------------
	/** 
	 * Determine whether dataset is a collection i.e. has nested datasets
     *
     * @param ds     the dataset to be checked
     **/
    
	private boolean isCollection(InvDataset ds) {
		return ds.hasNestedDatasets();
	}

	//---------------------------------------------------------------------------
	/** 
     * Get fragment harvesting parameters for collection datasets
     *   
     * @return		fragment harvesting parameters for collection datasets
     * 
     **/
	
	private FragmentParams getCollectionFragmentParams() {
		FragmentParams collectionParams = new FragmentHarvester.FragmentParams();
		collectionParams.categories = params.getCategories();
		collectionParams.createSubtemplates = params.createCollectionSubtemplates;
		collectionParams.isoCategory = params.datasetCategory;
		collectionParams.privileges = params.getPrivileges();
		collectionParams.templateId = params.collectionMetadataTemplate;
		collectionParams.uuid = params.getUuid();
		collectionParams.outputSchema = params.outputSchemaOnCollectionsFragments;
		return collectionParams;
	}

	//---------------------------------------------------------------------------
	/** 
     * Get fragment harvesting parameters for atomic datasets
     *   
     * @return		fragment harvesting parameters for atomic datasets
     * 
     **/
	
	private FragmentParams getAtomicFragmentParams() {
		FragmentParams atomicParams = new FragmentHarvester.FragmentParams();
		atomicParams.categories = params.getCategories();
		atomicParams.createSubtemplates = params.createAtomicSubtemplates;
		atomicParams.isoCategory = params.datasetCategory;
		atomicParams.privileges = params.getPrivileges();
		atomicParams.templateId = params.atomicMetadataTemplate;
		atomicParams.uuid = params.getUuid();
		atomicParams.outputSchema = params.outputSchemaOnAtomicsFragments;
		atomicParams.owner = params.getOwnerId();
		return atomicParams;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private ThreddsParams  params;
	private DataManager    dataMan;
	private SchemaManager  schemaMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private UriMapper      localUris;
	private HarvestResult  result;
	private String         hostUrl;
	private HashSet<String> harvestUris = new HashSet<String>();
    private Path cdmCoordsToIsoKeywordsStyleSheet;
    private Path cdmCoordsToIsoMcpDataParametersStyleSheet;
    private Path fragmentStylesheetDirectory;
    private String metadataGetService;
    private Map<String,ThreddsService> services = new HashMap<String, Harvester.ThreddsService>();
	private InvCatalogImpl catalog;
	
	private FragmentHarvester atomicFragmentHarvester;
	private FragmentHarvester collectionFragmentHarvester;

	private static class ThreddsService {
		public Map<String,String> datasets = new HashMap<String, String>();
		public InvService service;
		public String version;
		public String ops;
	};

	static private final Namespace difNS = Namespace.getNamespace("http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/");
	static private final Namespace invCatalogNS = Namespace.getNamespace("http://www.unidata.ucar.edu/namespaces/thredds/InvCatalog/v1.0");
	static private final Namespace gmd 	= Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
	static private final Namespace srv 	= Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
	static private final Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
	
	private List<HarvestError> errors = new LinkedList<HarvestError>();
	@Override
	public List<HarvestError> getErrors() {
		return errors;
	}
		
}
