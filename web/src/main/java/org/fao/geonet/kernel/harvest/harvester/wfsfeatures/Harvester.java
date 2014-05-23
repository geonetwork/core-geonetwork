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

package org.fao.geonet.kernel.harvest.harvester.wfsfeatures;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.utils.XmlElementReader;
import jeeves.utils.XmlRequest;
import jeeves.xlink.Processor;
import org.apache.commons.httpclient.HttpException;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.FragmentParams;
import org.fao.geonet.kernel.harvest.harvester.fragment.FragmentHarvester.HarvestSummary;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================
/** 
 * A WfsFeaturesHarvester is able to harvest metadata fragments from the 
 * GetFeature response of an OGC WFS. 
 * The fragments that are obtained from the WFS are saved into the GeoNetwork
 * database as subtemplates. The editor will offer these during the edit 
 * session for use when creating metadata records.
 * 
 * <pre>  
 * <nodes>
 *  <node type="wfsfeatures" id="300">
 *    <site>
 *      <name>TEST</name>
 *      <uuid>c1da2928-c866-49fd-adde-466fe36d3508</uuid>
 *      <account>
 *        <use>true</use>
 *        <username />
 *        <password />
 *      </account>
 *      <url>http://localhost:8080/deegree/wfs</url>
 *      <query><wfs getfeature query ..../></query>
 *      <outputSchema>iso19139</outputSchema>
 *      <stylesheet>transform_response.xsl</stylesheet>
 *      <streamFeatures>false</streamFeatures>
 *      <createSubtemplates>true</createSubtemplates>
 *      <templateId>3</templateId>
 *      <icon>wfs.gif</icon>
 *    </site>
 *    <options>
 *      <every>90</every>
 *      <oneRunOnly>false</oneRunOnly>
 *      <status>active</status>
 *      <lang>eng</lang>
 *      <recordsCategory></recordsCategory>
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
 * @author sppigot
 *   
 */
class Harvester
{
	
	
	//---------------------------------------------------------------------------
	/** 
     * Constructor
     *  
     * @param log		
     * @param context									Jeeves context
     * @param dbms 										Database
     * @param params	harvesting configuration for the node
     * 
     * @return null
     */
	public Harvester(Logger log, ServiceContext context, Dbms dbms, WfsFeaturesParams params) {
		this.log    = log;
		this.context= context;
		this.dbms   = dbms;
		this.params = params;

		result = new WfsFeaturesResult ();
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext (Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager ();
		schemaMan = gc.getSchemamanager ();
		SettingInfo si = new SettingInfo(context);
		String siteUrl = si.getSiteUrl() + context.getBaseUrl();
		metadataGetService = "local://xml.metadata.get";
		ssParams.put("siteUrl", siteUrl);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------
	/** 
    * Start the harvesting of fragments from the WFS node.
		*
		*
	 
	<?xml version="1.0" encoding="utf-8"?>
	<wfs:GetFeature xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" xmlns:wfs="http://www.opengis.net/wfs" outputFormat="text/xml; subtype=gml/3.1.1">
		<wfs:Query xmlns:app="http://www.deegree.org/app" typeName="app:list_parcel_property">
			<wfs:PropertyName>app:cadastral_id</wfs:PropertyName>
			<wfs:PropertyName>app:property_id</wfs:PropertyName>
		  <wfs:PropertyName>app:owner_name</wfs:PropertyName>
			<wfs:PropertyName>app:addressline1</wfs:PropertyName>
			<wfs:PropertyName>app:addressline2</wfs:PropertyName>
			<wfs:PropertyName>app:addressline3</wfs:PropertyName>
			<wfs:PropertyName>app:addressline4</wfs:PropertyName>
			<wfs:PropertyName>app:database_access_date</wfs:PropertyName>
			<wfs:PropertyName>app:GEOM</wfs:PropertyName>
			<ogc:Filter>
				<ogc:DWithin xmlns:gml='http://www.opengis.net/gml' >
					<ogc:PropertyName xmlns:app="http://www.deegree.org/app">app:GEOM</ogc:PropertyName>
					<gml:Polygon>
						<gml:outerBoundaryIs>
							<gml:LinearRing>
								<gml:coordinates cs="," decimal="." ts=" ">506964.28,5413897.046 507051.215,5413999.211 507039.46,5414009.211 507012.503,5414032.146 506902.284,5413943.883 506964.28,5413897.046</gml:coordinates>
							</gml:LinearRing>
						</gml:outerBoundaryIs>
					</gml:Polygon>
					<ogc:Distance>500.0</ogc:Distance>
				</ogc:DWithin>
			</ogc:Filter>
		</wfs:Query>
	</wfs:GetFeature>

		*
		*
    */
	public WfsFeaturesResult harvest() throws Exception {

		log.info("Retrieving metadata fragments for : " + params.name);
        
		//--- collect all existing metadata uuids before we update
		localUuids = new UUIDMapper(dbms, params.uuid);

		//--- parse the xml query from the string - TODO: default should be 
		//--- get everything
		Element wfsQuery = null;
		
		log.info("Parsing query :\n" + params.query);
		try {
			wfsQuery = Xml.loadString(params.query, false); 
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new BadParameterEx("GetFeature Query failed to parse\n", params.query);
		}

		//--- harvest metadata and subtemplates from fragments using generic fragment harvester
		FragmentHarvester fragmentHarvester = new FragmentHarvester(log, context, dbms, getFragmentHarvesterParams());

		if (params.streamFeatures) {
			harvestFeatures(wfsQuery, fragmentHarvester);
		} else {
			harvestResponse(wfsQuery, fragmentHarvester);
		}
    
		return result;
	}

	/** 
     * Harvest fragments from the response document
     * 
     */
	
	private void harvestResponse(Element xmlQuery,
            FragmentHarvester fragmentHarvester) throws IOException,
            JDOMException, MalformedURLException, BadXmlResponseEx, Exception {

		//--- post the query to the remote site
	    Element xml = Xml.loadFile(new URL(params.url), xmlQuery);
	    
	    if (xml == null) {
	    	throw new BadXmlResponseEx("No response or problem getting response from "+params.url+":\n"+Xml.getString(xmlQuery));
	    }

	    //--- apply stylesheet from output schema - stylesheet can be optional
			//--- in case the server can do XSL transformations for us (eg. deegree 
			//--- 2.2)
			stylesheetDirectory = schemaMan.getSchemaDir(params.outputSchema) + Geonet.Path.WFS_STYLESHEETS;
	    if (!params.stylesheet.trim().equals("")) {
	    	xml = Xml.transform(xml, stylesheetDirectory + "/" + params.stylesheet, ssParams);
	    }
	   
		 	log.info("Applying "+stylesheetDirectory + "/" + params.stylesheet);
	    harvest(xml, fragmentHarvester);
    }

	/** 
     * Harvest fragments by applying a stylesheet to each feature as it is received
     * (reduces memory usage for large documents)
     */
	
	private void harvestFeatures(Element xmlQuery, FragmentHarvester fragmentHarvester)
            throws UnsupportedEncodingException, IOException, HttpException,
            XMLStreamException, FactoryConfigurationError, Exception {
		
		XmlRequest req = new XmlRequest(new URL(params.url));
		req.setRequest(xmlQuery);
		Lib.net.setupProxy(context, req);
		
		File tempFile = File.createTempFile("temp-", ".xml");

		try {
			// Read response into temporary file 
			req.executeLarge(tempFile);

	    	List<Namespace> namespaces = new ArrayList<Namespace>();
	    	namespaces.add(Namespace.getNamespace("gml", "http://www.opengis.net/gml"));
	    	
	        XmlElementReader reader = new XmlElementReader(new FileInputStream(tempFile), "gml:featureMembers/*", namespaces);
					if (!reader.hasNext()) {
	    			namespaces.add(Namespace.getNamespace("wfs", "http://www.opengis.net/wfs"));
						reader = new XmlElementReader(new FileInputStream(tempFile), "wfs:FeatureCollection/gml:featureMember", namespaces);
					}
	        
	        while (reader.hasNext()) {
						stylesheetDirectory = schemaMan.getSchemaDir(params.outputSchema) + Geonet.Path.WFS_STYLESHEETS;
	    			Element records = Xml.transform(reader.next(), stylesheetDirectory + "/" + params.stylesheet, ssParams);
	
	    			harvest(records, fragmentHarvester);
	        }
		} finally {
			tempFile.delete();
		}
				
    }

	/** 
     * Harvest fragments from the element passed
     */
	
	private void harvest(Element xml, FragmentHarvester fragmentHarvester)
            throws Exception {
		
	    HarvestSummary fragmentResult = fragmentHarvester.harvest(xml, params.url);

			deleteOrphanedMetadata(fragmentResult.updatedMetadata);
	    	
	    result.fragmentsReturned += fragmentResult.fragmentsReturned;
	    result.fragmentsUnknownSchema += fragmentResult.fragmentsUnknownSchema;
	    result.subtemplatesAdded += fragmentResult.fragmentsAdded;
	    result.fragmentsMatched += fragmentResult.fragmentsMatched;
	    result.recordsBuilt += fragmentResult.recordsBuilt;
	    result.recordsUpdated += fragmentResult.recordsUpdated;
	    result.subtemplatesUpdated += fragmentResult.fragmentsUpdated;

	    result.total = result.subtemplatesAdded + result.recordsBuilt;
    }

	/** 
     * Remove old metadata and subtemplates and uncache any subtemplates
		 * that are left over after the update.
     */
	
	public void deleteOrphanedMetadata(Set<String> updatedMetadata) throws Exception {
        if(log.isDebugEnabled()) log.debug("  - Removing orphaned metadata records and fragments after update");
		
		for (String uuid : localUuids.getUUIDs()) {
			String isTemplate = localUuids.getTemplate(uuid);
			if (isTemplate.equals("s")) {
					Processor.uncacheXLinkUri(metadataGetService+"?uuid=" + uuid);
			}

			if (!updatedMetadata.contains(uuid)) {	
				String id = localUuids.getID(uuid);
				dataMan.deleteMetadata(context, dbms, id);
			
				if (isTemplate.equals("s")) {
					result.subtemplatesRemoved ++;
				} else {
					result.recordsRemoved ++;
				}
			}
		}
		
		if (result.subtemplatesRemoved + result.recordsRemoved > 0)  {
			dbms.commit();
		}
    }

	/** 
     * Get generic fragment harvesting parameters from metadata fragment harvesting parameters
     *   
     */
	
	private FragmentParams getFragmentHarvesterParams() {
	    FragmentParams fragmentParams = new FragmentHarvester.FragmentParams();
		fragmentParams.categories = params.getCategories();
		fragmentParams.createSubtemplates = params.createSubtemplates;
		fragmentParams.outputSchema = params.outputSchema;
		fragmentParams.isoCategory = params.recordsCategory;
		fragmentParams.privileges = params.getPrivileges();
		fragmentParams.templateId = params.templateId;
		fragmentParams.url = params.url;
		fragmentParams.uuid = params.uuid;
		fragmentParams.owner = params.ownerId;
		return fragmentParams;
    }

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private Dbms           dbms;
	private WfsFeaturesParams   params;
	private DataManager    dataMan;
	private SchemaManager  schemaMan;
	private WfsFeaturesResult   result;
	private UUIDMapper     localUuids;
	private String	 		metadataGetService;
	private String	 		 stylesheetDirectory;
	private Map<String,String> ssParams = new HashMap<String,String>();
}

//=============================================================================

