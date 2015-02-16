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
import jeeves.server.context.ServiceContext;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataCategory;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.IHarvester;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataCategoryRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.thumbnail.Set;
import org.fao.geonet.util.Sha1Encoder;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.xpath.XPath;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;


//=============================================================================
/** 
 * A OgcWxSHarvester is able to generate metadata for data and service
 * from a GetCapabilities documents. Metadata for layers are generated
 * using layer information contained in the GetCapabilities document
 * or using a xml document pointed by the metadataUrl attribute of layer
 * element.
 * 
 * OGC services supported are : 
 * <ul>
 * 	<li>WMS</li>
 * 	<li>WFS</li>
 * 	<li>WCS</li>
 * 	<li>WPS</li>
 * 	<li>SOS</li>
 * </ul>
 * 
 * Metadata produced are :
 * <ul>
 * 	<li>ISO19119 for service's metadata</li>
 * 	<li>ISO19139 for data's metadata</li>
 * </ul>
 * 
 *  Note : Layer stands for "Layer" for WMS, "FeatureType" for WFS
 *  and "Coverage" for WCS.
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
 *   
 */
class Harvester extends BaseAligner implements IHarvester<HarvestResult>
{
	
	
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
     */
	public Harvester(AtomicBoolean cancelMonitor, Logger log,
                     ServiceContext context,
                     OgcWxSParams params) {
        super(cancelMonitor);
		this.log    = log;
		this.context= context;
		this.params = params;

		result = new HarvestResult();
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext (Geonet.CONTEXT_NAME);
		dataMan = gc.getBean(DataManager.class);
		schemaMan = gc.getBean(SchemaManager.class);
    }

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------
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
        // TODO : define a rule for UUID in order to be able to do an update operation ? 
        UUIDMapper localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.getUuid());


        // Try to load capabilities document
		this.capabilitiesUrl = getBaseUrl(params.url) +
        		"SERVICE=" + params.ogctype.substring(0,3) +
        		"&VERSION=" + params.ogctype.substring(3) +
        		"&REQUEST=" + GETCAPABILITIES
        		;

        if(log.isDebugEnabled()) {
            log.debug("GetCapabilities document: " + this.capabilitiesUrl);
        }
		
        XmlRequest req = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest();
        req.setUrl(new URL(this.capabilitiesUrl));
        req.setMethod(XmlRequest.Method.GET);
        Lib.net.setupProxy(context, req);

        if (params.isUseAccount()) {
            req.setCredentials(params.getUsername(), params.getPassword());
        }

        xml = req.execute();

		//-----------------------------------------------------------------------
		//--- remove old metadata
		for (String uuid : localUuids.getUUIDs()) {
            if (cancelMonitor.get()) {
                return this.result;
            }

            String id = localUuids.getID (uuid);

            if(log.isDebugEnabled()) log.debug ("  - Removing old metadata before update with id: " + id);

			// Remove thumbnails
			unsetThumbnail (id);
			
			// Remove metadata
			dataMan.deleteMetadata (context, id);
			
			result.locallyRemoved ++;
		}


        if (result.locallyRemoved > 0) {
            dataMan.flush();
        }
		
        // Convert from GetCapabilities to ISO19119
        addMetadata (xml);
        dataMan.flush();

        result.totalMetadata = result.addedMetadata + result.layer;
    
		return result;
	}
	
	

	/** 
     * Add metadata to the node for a WxS service
     *  
	 *  1.Use GetCapabilities Document
	 *  2.Transform using XSLT to iso19119
	 *  3.Loop through layers
	 *  4.Create md for layer
	 *  5.Add operatesOn elem with uuid
	 *  6.Save all
     *	
     * @param capa      GetCapabilities document
     *                   
     */
	 private void addMetadata (Element capa) throws Exception
	 {
		if (capa == null)
			return;

		//--- Loading categories and groups
		localCateg 	= new CategoryMapper (context);
		localGroups = new GroupMapper (context);

		// md5 the full capabilities URL
		String uuid = Sha1Encoder.encodeString (this.capabilitiesUrl); // is the service identifier
		
		//--- Loading stylesheet
		Path styleSheet = schemaMan.getSchemaDir(params.outputSchema).
                resolve(Geonet.Path.CONVERT_STYLESHEETS).
                resolve("OGCWxSGetCapabilitiesto19119").
                resolve("OGC" + params.ogctype.substring(0, 3) + "GetCapabilities-to-ISO19119_ISO19139.xsl");

         if(log.isDebugEnabled()) log.debug ("  - XSLT transformation using " + styleSheet);
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("lang", params.lang);
		param.put("topic", params.topic);
		param.put("uuid", uuid);
		
		Element md = Xml.transform (capa, styleSheet, param);
		
		String schema = dataMan.autodetectSchema (md, null); // ie. iso19139; 

		if (schema == null) {
			log.warning("Skipping metadata with unknown schema.");
			result.unknownSchema ++;
		}


		//--- Create metadata for layers only if user ask for
		if (params.useLayer || params.useLayerMd) {			
			// Load CRS
			// TODO
			
			//--- Select layers, featureTypes and Coverages (for layers having no child named layer = not take group of layer into account) 
			// and add the metadata
			XPath xp = XPath.newInstance ("//Layer[count(./*[name(.)='Layer'])=0] | " + 
											"//wms:Layer[count(./*[name(.)='Layer'])=0] | " +
											"//wfs:FeatureType | " +
											"//wcs:CoverageOfferingBrief | " +
											"//sos:ObservationOffering");
			xp.addNamespace("wfs", "http://www.opengis.net/wfs");
			xp.addNamespace("wcs", "http://www.opengis.net/wcs");
			xp.addNamespace("wms", "http://www.opengis.net/wms");
			xp.addNamespace("sos", "http://www.opengis.net/sos/1.0");
										
			@SuppressWarnings("unchecked")
            List<Element> layers = xp.selectNodes(capa);
			if (layers.size()>0) {
				log.info("  - Number of layers, featureTypes or Coverages found : " + layers.size());
			
				for (Element layer : layers) {
					WxSLayerRegistry s = addLayerMetadata (layer, capa);
					if (s != null)
						layersRegistry.add(s);
				}       
				
				// Update ISO19119 for data/service links creation (ie. operatesOn element)
				// The editor will support that but it will make quite heavy XML.
				md = addOperatesOnUuid (md, layersRegistry);
			}
		}	

        // Save iso19119 metadata in DB
		log.info("  - Adding metadata for services with " + uuid);

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
                 setUri(params.url);

         addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);

         metadata = dataMan.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

         String id = String.valueOf(metadata.getId());

         addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

         dataMan.flush();

         dataMan.indexMetadata(id, true);
         result.addedMetadata++;
		
		// Add Thumbnails only after metadata insertion to avoid concurrent transaction
		// and loaded thumbnails could eventually failed anyway.
		if (params.ogctype.startsWith("WMS") && params.createThumbnails) {
        	for (WxSLayerRegistry layer : layersRegistry) {
                loadThumbnail (layer);
            }
        }
	}
	
	
	
	/** 
     * Add OperatesOn elements on an ISO19119 metadata
     *  
     *  <srv:operatesOn>
	 *		<gmd:MD_DataIdentification uuidref=""/>
	 *	</srv:operatesOn>
     *	
     * @param md                    iso19119 metadata
     * @param layersRegistry		uuid to be added as an uuidref attribute
     *                   
     */
	 private Element addOperatesOnUuid (Element md, List<WxSLayerRegistry> layersRegistry) {
				
		Namespace gmd 	= Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
		Namespace gco 	= Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
		Namespace srv 	= Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
        Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

        Element root 	= md.getChild("identificationInfo", gmd)
							.getChild("SV_ServiceIdentification", srv);


        /*
           * TODO
           *
              For each queryable layer queryable = "1" et /ROOT/Capability/Request/*[name()!='getCapabilities']
                  or queryable = "0" et /ROOT/Capability/Request/*[name()!='getCapabilities' and name!='GetFeatureInfo']
              should do
                  srv:coupledResource/srv:SV_CoupledResource/srv:OperationName = /ROOT/Capability/Request/child::name()
                  srv:coupledResource/srv:SV_CoupledResource/srv:identifier = UUID of the data metadata
                  srv:coupledResource/srv:SV_CoupledResource/gco:ScopedName = Layer/Name
              But is this really useful in ISO19119 ?
           */
		
		if (root != null) {
            if(log.isDebugEnabled()) log.debug("  - add SV_CoupledResource and OperatesOnUuid");
			
			Element couplingType = root.getChild("couplingType", srv);
			int coupledResourceIdx = root.indexOf(couplingType);
			
			for (WxSLayerRegistry layer : layersRegistry)
			{
				// Create coupled resources elements to register all layername
				// in service metadata. This information could be used to add
				// interactive map button when viewing service metadata.
				Element coupledResource = new Element ("coupledResource", srv);
				Element scr = new Element ("SV_CoupledResource", srv);
				
				
				// Create operation according to service type
				Element operation = new Element ("operationName", srv);
				Element operationValue = new Element ("CharacterString", gco);
				
				if (params.ogctype.startsWith("WMS"))
					operationValue.setText("GetMap"); 
				else if (params.ogctype.startsWith("WFS"))
					operationValue.setText("GetFeature");
				else if (params.ogctype.startsWith("WCS"))
					operationValue.setText("GetCoverage");
				else if (params.ogctype.startsWith("SOS"))
					operationValue.setText("GetObservation");
				operation.addContent(operationValue);
				
				// Create identifier (which is the metadata identifier)
				Element id = new Element ("identifier", srv);
				Element idValue = new Element ("CharacterString", gco);
				idValue.setText(layer.uuid);
				id.addContent(idValue);
				
				// Create scoped name element as defined in CSW 2.0.2 ISO profil
				// specification to link service metadata to a layer in a service.
				Element scopedName = new Element ("ScopedName", gco);
				scopedName.setText(layer.name);
				
				scr.addContent(operation);
				scr.addContent(id);
				scr.addContent(scopedName);
				coupledResource.addContent(scr);
				
				// Add coupled resource before coupling type element
				root.addContent(coupledResourceIdx, coupledResource);
				
				
		
				// Add operatesOn element at the end of identification section.
				Element op = new Element ("operatesOn", srv);
				op.setAttribute("uuidref", layer.uuid);

                String hRefLink =  context.getBean(SettingManager.class).getSiteURL(context) + "/xml.metadata.get?uuid=" + layer.uuid;
                op.setAttribute("href", hRefLink, xlink);

				
				root.addContent(op);
				
			}
		}

		
		return md;
	}

	
	/** 
     * Add metadata for a Layer/FeatureType/Coverage element of a GetCapabilities document.
     * This function search for a metadataUrl element (with @type = TC211 and format = text/xml) 
     * and try to load the XML document. 
     * If failed, then an XSLT is used for creating metadata from the 
     * Layer/FeatureType/Coverage element.
     * If loaded document contain an existing uuid, metadata will not be loaded in the catalogue.
     *  
     * @param layer     Layer/FeatureType/Coverage element
     * @param capa		GetCapabilities document
     *  
     * @return          uuid 
     *                   
     */
	private WxSLayerRegistry addLayerMetadata (Element layer, Element capa) throws JDOMException
	{
		
		WxSLayerRegistry reg= new WxSLayerRegistry ();
		String schema;
		String mdXml;
		//--- Loading stylesheet
		Path styleSheet 	= schemaMan.getSchemaDir(params.outputSchema).
                resolve(Geonet.Path.CONVERT_STYLESHEETS).
                resolve("OGCWxSGetCapabilitiesto19119").
                resolve("OGC" + params.ogctype.substring(0, 3) + "GetCapabilitiesLayer-to-19139.xsl");
		Element xml 		= null;

        boolean exist;
        boolean loaded = false;

        if (params.ogctype.substring(0,3).equals("WMS")) {
			Element name;
			if (params.ogctype.substring(3,8).equals("1.3.0")) {
				Namespace wms = Namespace.getNamespace("http://www.opengis.net/wms");
				name = layer.getChild ("Name", wms);
			} else {
				name = layer.getChild ("Name");
			}
			//--- For the moment, skip non-requestable category layers
			if (name == null || name.getValue().trim().equals("")) {
				log.info("  - skipping layer with no name element");
				return null;
			}
			reg.name = name.getValue();
		} else if (params.ogctype.substring(0,3).equals("WFS")) {
			Namespace wfs = Namespace.getNamespace("http://www.opengis.net/wfs");
			reg.name 	= layer.getChild ("Name", wfs).getValue ();
		} else if (params.ogctype.substring(0,3).equals("WCS")) {
			Namespace wcs = Namespace.getNamespace("http://www.opengis.net/wcs");
			reg.name 	= layer.getChild ("name", wcs).getValue ();
		} else if (params.ogctype.substring(0,3).equals("SOS")) {
			Namespace gml = Namespace.getNamespace("http://www.opengis.net/gml");
			reg.name 	= layer.getChild ("name", gml).getValue ();
		}
		
		log.info ("  - Loading layer: " + reg.name);
		
		//--- md5 the full capabilities URL + the layer, coverage or feature name
		reg.uuid = Sha1Encoder.encodeString(this.capabilitiesUrl+"#"+reg.name); // the dataset identifier
	
		//--- Trying loading metadataUrl element
		if (params.useLayerMd && !params.ogctype.substring(0,3).equals("WMS")) {
			log.info("  - MetadataUrl harvester only supported for WMS layers.");
		}
		
		if (params.useLayerMd && params.ogctype.substring(0,3).equals("WMS")) {
			
			Namespace xlink 	= Namespace.getNamespace ("http://www.w3.org/1999/xlink");
			
			// Get metadataUrl xlink:href
			// TODO : add support for WCS & WFS metadataUrl element.


            // Check if add namespace prefix to Xpath queries.  If layer.getNamespace() is:
            //    * Namespace.NO_NAMESPACE, should not be added, otherwise exception is launched
            //    * Another namespace, should be added a namespace prefix to Xpath queries, otherwise doesn't find any result
            String dummyNsPrefix = "";
            boolean addNsPrefix = !layer.getNamespace().equals(Namespace.NO_NAMESPACE);
            if (addNsPrefix) dummyNsPrefix = "x:";

            XPath mdUrl 		= XPath.newInstance ("./" + dummyNsPrefix + "MetadataURL[@type='TC211' and " + dummyNsPrefix + "Format='text/xml']/" + dummyNsPrefix + "OnlineResource");
            if (addNsPrefix) mdUrl.addNamespace("x", layer.getNamespace().getURI());
            Element onLineSrc 	= (Element) mdUrl.selectSingleNode (layer);

            // Check if metadataUrl in WMS 1.3.0 format
            if (onLineSrc == null) {
                mdUrl 		= XPath.newInstance ("./" + dummyNsPrefix + "MetadataURL[@type='ISO19115:2003' and " + dummyNsPrefix + "Format='text/xml']/" + dummyNsPrefix + "OnlineResource");
                if (addNsPrefix) mdUrl.addNamespace("x", layer.getNamespace().getURI());
                onLineSrc 	= (Element) mdUrl.selectSingleNode (layer);
            }

			if (onLineSrc != null) {
				org.jdom.Attribute href = onLineSrc.getAttribute ("href", xlink);

				if (href != null) {	// No metadataUrl attribute for that layer
					mdXml = href.getValue ();
					try {
						xml = Xml.loadFile (new URL(mdXml));

                        // If url is CSW GetRecordById remove envelope
                        if (xml.getName().equals("GetRecordByIdResponse")) {
                            xml = (Element) xml.getChildren().get(0);
                        }

						schema = dataMan.autodetectSchema (xml, null); // ie. iso19115 or 139 or DC
						// Extract uuid from loaded xml document
						// FIXME : uuid could be duplicate if metadata already exist in catalog
						reg.uuid = dataMan.extractUUID(schema, xml);
						exist = dataMan.existsMetadataUuid(reg.uuid);
						
						if (exist) {
							log.warning("    Metadata uuid already exist in the catalogue. Metadata will not be loaded.");
							result.layerUuidExist ++;
							// Return the layer info even if it exists in order
							// to link to the service record.
							return reg;
						}
						
						if (schema == null) {
							log.warning("    Failed to detect schema from metadataUrl file. Use GetCapabilities document instead for that layer.");
							result.unknownSchema ++;
							loaded = false;
						} else { 
							log.info("  - Load layer metadataUrl document ok: " + mdXml);
							
							loaded = true;
							result.layerUsingMdUrl ++;
						}
					// TODO : catch other exception
					}catch (Exception e) {
						log.warning("  - Failed to load layer using metadataUrl attribute : " + e.getMessage());
						loaded = false;
					}
				} else {
					log.info("  - No metadataUrl attribute with format text/xml found for that layer");
					loaded = false;
				}
			} else {
				log.info("  - No OnlineResource found for that layer");
				loaded = false;
			}
		}
		
		
		//--- using GetCapabilities document
		if (!loaded && params.useLayer){
			try {
				//--- set XSL param to filter on layer and set uuid
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("uuid", reg.uuid);
				param.put("Name", reg.name);
				param.put("lang", params.lang);
				param.put("topic", params.topic);
				
				xml = Xml.transform (capa, styleSheet, param);
                if(log.isDebugEnabled()) log.debug("  - Layer loaded using GetCapabilities document.");
				
			} catch (Exception e) {
				log.warning("  - Failed to do XSLT transformation on Layer element : " + e.getMessage());
			}
		}
		
		
		// Insert in db
		try {

            //
            //  insert metadata
            //
			schema = dataMan.autodetectSchema (xml);
            Metadata metadata = new Metadata().setUuid(reg.uuid);
            metadata.getDataInfo().
                    setSchemaId(schema).
                    setRoot(xml.getQualifiedName()).
                    setType(MetadataType.METADATA);
            metadata.getSourceInfo().
                    setSourceId(params.getUuid()).
                    setOwner(Integer.parseInt(params.getOwnerId()));
            metadata.getHarvestInfo().
                    setHarvested(true).
                    setUuid(params.getUuid()).
                    setUri(params.url);
            if (params.datasetCategory!=null && !params.datasetCategory.equals("")) {
                MetadataCategory metadataCategory = context.getBean(MetadataCategoryRepository.class).findOneByName(params.datasetCategory);

                if (metadataCategory == null) {
                    throw new IllegalArgumentException("No category found with name: " + params.datasetCategory);
                }
                metadata.getCategories().add(metadataCategory);
            }
            metadata = dataMan.insertMetadata(context, metadata, xml, true, false, false, UpdateDatestamp.NO, false, false);

            reg.id = String.valueOf(metadata.getId());

            if(log.isDebugEnabled()) log.debug("    - Layer loaded in DB.");

            if(log.isDebugEnabled()) log.debug("    - Set Privileges and category.");
            addPrivileges(reg.id, params.getPrivileges(), localGroups, dataMan, context, log);

            if(log.isDebugEnabled()) log.debug("    - Set Harvested.");

            dataMan.flush();

            dataMan.indexMetadata(reg.id, true);
			
			try {
    			// Load bbox info for later use (eg. WMS thumbnails creation)
    			Namespace gmd 	= Namespace.getNamespace("http://www.isotc211.org/2005/gmd");
    			Namespace gco 	= Namespace.getNamespace("http://www.isotc211.org/2005/gco");
    			
    			ElementFilter bboxFinder = new ElementFilter("EX_GeographicBoundingBox", gmd);
                @SuppressWarnings("unchecked")
                Iterator<Element> bboxes = xml.getDescendants(bboxFinder);
    			
    			while (bboxes.hasNext()) {
    				Element box = bboxes.next();
    				// FIXME : Could be null. Default bbox if from root layer
    				reg.minx = Double.valueOf(box.getChild("westBoundLongitude", gmd).getChild("Decimal", gco).getText());
    				reg.miny = Double.valueOf(box.getChild("southBoundLatitude", gmd).getChild("Decimal", gco).getText());
    				reg.maxx = Double.valueOf(box.getChild("eastBoundLongitude", gmd).getChild("Decimal", gco).getText());
    				reg.maxy = Double.valueOf(box.getChild("northBoundLatitude", gmd).getChild("Decimal", gco).getText());
    				
    			}
			}  catch (Exception e) {
	            log.warning("  - Failed to extract layer bbox from metadata : " + e.getMessage());
	        }

			result.layer ++;
			log.info("  - metadata loaded with uuid: " + reg.uuid + "/internal id: " + reg.id);
				
		} catch (Exception e) {
			log.warning("  - Failed to load layer metadata : " + e.getMessage());
			result.unretrievable ++;
			return null;
		}
		
		return reg;
	}
	

	/** 
     * Call GeoNetwork service to load thumbnails and create small and 
     * big ones. 
     *  
     *  
     * @param layer   layer for which the thumbnail needs to be generated
     *                   
     */
	private void loadThumbnail (WxSLayerRegistry layer){
        if(log.isDebugEnabled())
            log.debug("  - Creating thumbnail for layer metadata: " + layer.name + " id: " + layer.id);
		Set s = new org.fao.geonet.services.thumbnail.Set ();
		
		try {
			String filename = getMapThumbnail(layer);
			
			if (filename != null) {
                if(log.isDebugEnabled()) log.debug("  - File: " + filename);
				
				Element par = new Element ("request");
				par.addContent(new Element ("id").setText(layer.id));
				par.addContent(new Element ("version").setText("10"));
				par.addContent(new Element ("type").setText("large"));
				
				Element fname = new Element ("fname").setText(filename);
				fname.setAttribute("content-type", "image/png");
				fname.setAttribute("type", "file");
				fname.setAttribute("size", "");
				
				par.addContent(fname);
				par.addContent(new Element ("add").setText("Add"));
				par.addContent(new Element ("createSmall").setText("on"));
				par.addContent(new Element ("smallScalingFactor").setText("180"));
				par.addContent(new Element ("smallScalingDir").setText("width"));
				
				// Call the services 
				s.execOnHarvest(par, context, dataMan);

                dataMan.flush();

                result.thumbnails ++;
			} else {
				result.thumbnailsFailed ++;
            }
		} catch (Exception e) {
			log.warning("  - Failed to set thumbnail for metadata: " + e.getMessage());
			e.printStackTrace();
			result.thumbnailsFailed ++;
		}
		
	}
	
	/** 
     * Remove thumbnails directory for all metadata
     * FIXME : Do this only for existing one !
     *  
     * @param id   layer for which the thumbnail needs to be generated
     *                   
     */
	private void unsetThumbnail (String id){
        if(log.isDebugEnabled()) log.debug("  - Removing thumbnail for layer metadata: " + id);

		try {
			Path file = Lib.resource.getDir(context, Params.Access.PUBLIC, id);
            IO.deleteFileOrDirectory(file);
		} catch (Exception e) {
			log.warning("  - Failed to remove thumbnail for metadata: " + id + ", error: " + e.getMessage());
		}
	}

	
	
	/** 
     * Load thumbnails making a GetMap operation.
     * Width is 300px. Ratio is computed for height using LatLongBoundingBoxElement.
     *  
     *  
     * @param layer   layer for which the thumbnail needs to be generated
     *                   
     */
	private String getMapThumbnail (WxSLayerRegistry layer) {
		String filename = layer.uuid + ".png";
		Path dir = context.getUploadDir();
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
        		"&SERVICE=" + params.ogctype.substring(0,3) +
        		"&VERSION=" + params.ogctype.substring(3) +
        		"&REQUEST=" + GETMAP + 
        		"&FORMAT=" + IMAGE_FORMAT + 
        		"&WIDTH=" + WIDTH +
        		"&" + crsParamName + "=EPSG:4326" +
        		"&HEIGHT=" + r.intValue() +
        		"&LAYERS=" + layer.name + 
        		"&STYLES=" +
        		"&BBOX=" + bboxParamValue;

        if(log.isDebugEnabled()) log.debug ("Retrieving remote document: " + url);

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

            if(log.isDebugEnabled()) {
                log.debug("   Get " + httpResponse.getStatusCode());
            }

			if (httpResponse.getStatusCode() == HttpStatus.OK) {
			    // Save image document to temp directory
				// TODO: Check OGC exception

                try (OutputStream fo = Files.newOutputStream(dir.resolve(filename));
                     InputStream in = httpResponse.getBody()) {
                    BinaryFile.copy(in, fo);
                }
            } else {
				log.info (" Http error connecting");
				return null;
			}
		} catch (IOException ioe){
			log.info (" Unable to connect to '" + req.toString() + "'");
			log.info (ioe.getMessage());
			return null;
		} finally {
		    // Release current connection to the connection pool once you are done
		    req.releaseConnection ();
		}
		
		return filename;
	}

	/** 
     * Add '?' or '&' if required to url so that parameters can just be 
     * appended to it 
     *   
     * @param url		Url to which parameters are going to be appended
     * 
     */
	private String getBaseUrl(String url) {
		if (url.endsWith("?")) {
			return url;
		} else if (url.contains("?")) {
			return url+"&";
		} else {
			return url+"?";
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private OgcWxSParams   params;
	private DataManager    dataMan;
	private SchemaManager  schemaMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
    private HarvestResult   result;

    /**
	 * Store the GetCapabilities operation URL. This URL is scrambled
	 * and used to uniquelly identified the service. The idea of generating
	 * a uuid based on the URL instead of a randomuuid is to be able later
	 * to do an update of the service metadata (which could have been updated
	 * in the catalogue) instead of a delete/insert operation.
	 */
	private String capabilitiesUrl;
    private static final int WIDTH = 900;
	private static final String GETCAPABILITIES = "GetCapabilities";
	private static final String GETMAP = "GetMap";
    private static final String IMAGE_FORMAT = "image/png";
    private List<WxSLayerRegistry> layersRegistry = new ArrayList<WxSLayerRegistry>();
	
	private static class WxSLayerRegistry {
		public String uuid;
		public String id;
		public String name;
		public Double minx = -180.0;
		public Double miny = -90.0;
		public Double maxx = 180.0;
		public Double maxy = 90.0;
	}

	/* (non-Javadoc)
	 * @see org.fao.geonet.kernel.harvest.harvester.IHarvester#getErrors()
	 */
	@Override
	public List<HarvestError> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

}
