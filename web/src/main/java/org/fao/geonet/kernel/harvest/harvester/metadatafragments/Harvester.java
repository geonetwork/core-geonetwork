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

package org.fao.geonet.kernel.harvest.harvester.metadatafragments;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.interfaces.Logger;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import jeeves.xlink.Processor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.Privileges;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


//=============================================================================
/** 
 * A MetadataFragmentsHarvester is able to harvest metadata fragments from a 
 * WFS. 
 * The fragments that are obtained from the WFS are saved into the GeoNetwork
 * database as subtemplates. The editor will offer these during the edit 
 * session for use when creating metadata records.
 * 
 * <pre>  
 * <nodes>
 *  <node type="metadatafragments" id="300">
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
 *      <stylesheet>transform_response.xsl</stylesheet>
 *      <templateId>3</templateId>
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
	public Harvester(Logger log, ServiceContext context, Dbms dbms, MetadataFragmentsParams params) {
		this.log    = log;
		this.context= context;
		this.dbms   = dbms;
		this.params = params;

		result = new MetadataFragmentsResult ();
		
		GeonetContext gc = (GeonetContext) context.getHandlerContext (Geonet.CONTEXT_NAME);
		dataMan = gc.getDataManager ();
		templateForLinks = null;
		SettingInfo si = new SettingInfo(context);
		String siteUrl = si.getSiteUrl() + context.getBaseUrl();
		metadataGetService = siteUrl + "/srv/en/xml.metadata.get";
		stylesheetDirectory = context.getAppPath() + Geonet.Path.WFS_STYLESHEETS;
		xlink   = Namespace.getNamespace ("xlink", "http://www.w3.org/1999/xlink");
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
	public MetadataFragmentsResult harvest() throws Exception {

		Element xml;

		log.info("Retrieving metadata fragments for : " + params.name);
        
		//--- clean all before harvest : Remove/Add mechanism
        UUIDMapper localUuids = new UUIDMapper(dbms, params.uuid);

		//--- parse the xml query from the string - TODO: default should be 
		//--- get everything
		Element xmlQuery;
		
		log.info("Parsing query :\n" + params.query);
		try {
			xmlQuery = Xml.loadString(params.query, false); 
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new BadParameterEx("GetFeature Query failed to parse\n", params.query);
		}

		//--- post the query to the remote site
		xml = Xml.loadFile(new URL(params.url), xmlQuery);
		if (xml == null) {
			throw new BadXmlResponseEx("No response or problem getting response from "+params.url+":\n"+Xml.getString(xmlQuery));
		}

		//--- apply stylesheet if specified
		if (!params.stylesheet.trim().equals("")) {
			xml = Xml.transform(xml, stylesheetDirectory + "/" + params.stylesheet);
		}
		
		//--- remove old metadata fragments and uncache them
		log.debug("  - Removing old metadata fragments before update with harvestUUid of " + params.uuid);
		for (String uuid : localUuids.getUUIDs()) {
			String id = localUuids.getID(uuid);
			String isTemplate = localUuids.getTemplate(uuid);
			if (isTemplate.equals("s")) {
				dataMan.deleteMetadata(dbms, id);
				Processor.uncacheXLinkUri(metadataGetService+"?uuid=" + uuid);
				result.fragmentsRemoved ++;
			} else {
				buildRecords = false;
			}
		}
		log.debug("  - Records will be built? "+buildRecords);
		
		if (result.fragmentsRemoved > 0) dbms.commit();

		//--- convert fragments from WFS to subtemplates in GeoNetwork and
		//--- place links in chosen template if required
		addSubtemplatesAndLinks(xml);
		dbms.commit();
            
		//--- reindex records if fragments reharvested
		log.debug("  - Records will be built? "+buildRecords);
		if (!buildRecords) {
			for (String uuid : localUuids.getUUIDs()) {
				String id = localUuids.getID(uuid);
				String isTemplate = localUuids.getTemplate(uuid);
				log.debug("  - isTemplate "+isTemplate+" - "+id);
				if (!isTemplate.equals("s")) {
					log.debug("  - indexing "+id);
					dataMan.indexMetadata(dbms, id);
				}
			}
		}

		//--- add up results read from WFS
		result.total = result.fragmentsAdded + result.recordsBuilt;
    
		return result;
	}
	
	

	//---------------------------------------------------------------------------
	/** 
    * Add metadata fragments read from the WFS.
		* Typical response expected:

	<?xml version="1.0" encoding="utf-8"?>
	<records>
		<record>
			<fragment title="John P BlockHead - CSIRO" id="contactinfo" uuid="contactinfo-18">
				<gmd:CI_ResponsibleParty>
					<gmd:individualName>
						<gco:CharacterString>John P BlockHead</gco:CharacterString>
					</gmd:individualName>
					<gmd:organisationName>
						<gco:CharacterString>CSIRO Division of Marine and Atmospheric Research (CMAR)</gco:CharacterString>
					</gmd:organisationName>
					<gmd:role>
						<gmd:CI_RoleCode codeList="http://www.isotc211.org/2005/resources/Codelist/gmxCodelists.xml#CI_RoleCode" codeListValue="custodian"/>
					</gmd:role>
				</gmd:CI_ResponsibleParty>
			</fragment>
			<fragment ...>
			 ...
			 </fragment>
		</record>
	</records>

		* We use a WFS such as deegree that can apply an xslt to its output and
		* produce the desired fragment layout above on the server before delivering 
		* it to us as the response
		*
    * @param xml      Fragments from the WFS that are to be kept as 
		*                 subtemplates
    *                   
    */
	private void addSubtemplatesAndLinks(Element xml) throws Exception {

		if (xml == null || !xml.getName().equals("records")) { 
			throw new BadXmlResponseEx("<records> not found in response: \n"+Xml.getString(xml));
		}

		//--- Loading categories and groups
		localCateg 	= new CategoryMapper (dbms);
		localGroups = new GroupMapper (dbms);

		List<Element> recs = xml.getChildren();
		if (recs == null || recs.size() == 0) {
			throw new BadXmlResponseEx("No children of <records> found in response: \n"+Xml.getString(xml));
		}

		//--- get the template to match the fragments into
		if (!params.templateId.equals("0")) {
			templateForLinks = dataMan.getMetadataNoInfo(context, params.templateId); 
			// build a list of all Namespaces in the templateForLinks document
			Namespace ns = templateForLinks.getNamespace();
			if (ns != null) {
				theNss.add(ns);
				theNss.addAll(templateForLinks.getAdditionalNamespaces());
			}
		}

		for (Element rec : recs) {
			addFragments(rec.getChildren());
		}
	}

	//---------------------------------------------------------------------------
	/** 
     * Add Fragments to GeoNetwork database as subtemplates
     *   
     * @param fragments		List of fragments to add to GeoNetwork database
     * 
     */
	private void addFragments(List<Element> fragments) throws Exception {

		Element templateCopy = null;
		if (templateForLinks != null) {
			templateCopy = (Element)templateForLinks.clone();
		}
		boolean matchFragment = true;

		for (Element fragment : fragments) {

			// get the id and title from the fragment to match/use in any template
			String title = fragment.getAttributeValue("title");
			String matchId = fragment.getAttributeValue("id");
			if (matchId == null || matchId.equals("")) {
				log.error("Fragment won't be matched because no id attribute "+Xml.getString(fragment));
				matchFragment = false;
			}
				
			// get the metadata fragment from the fragment container
			Element md = (Element) fragment.getChildren().get(0);
		
			String schema = dataMan.autodetectSchema (md); // ie. iso19139; 
			if (schema == null) {
				log.warning("Skipping metadata with unknown schema.");
				result.fragmentsUnknownSchema ++;
			} else {
	
				String uuid = fragment.getAttributeValue("uuid");
				if (uuid == null || uuid.equals("")) {
					uuid = UUID.randomUUID().toString(); 
					log.warning("  - Metadata fragment did not have uuid! Fragment XML is "+ Xml.getString(md));
				}
				log.info("  - Adding metadata fragment with " + uuid + " schema is set to " + schema + " XML is "+ Xml.getString(md));
				DateFormat df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
				Date date = new Date();
		
				String id = dataMan.insertMetadataExt(dbms, schema, md, context.getSerialFactory(), params.uuid, df.format(date), df.format(date), uuid, 1, null);
		
				int iId = Integer.parseInt(id);

				addPrivileges(id);
				addCategories(id);
			
				dataMan.setTemplateExt(dbms, iId, "s", null);
				dataMan.setHarvestedExt(dbms, iId, params.uuid, params.url);

				dataMan.indexMetadataGroup(dbms, id);
		
				dbms.commit();
				result.fragmentsAdded ++;

				if (templateForLinks != null && matchFragment) {
					insertLinkToFragmentIntoTemplate(templateCopy, matchId, uuid, title);
				}
			}
		}
		result.fragmentsReturned += fragments.size();

		if (buildRecords && templateForLinks != null && matchFragment) {
			// now add any record built from template with linked in fragments
			log.info("	- Attempting to insert metadata record with link");
			DateFormat df = new SimpleDateFormat ("yyyy-MM-dd'T'HH:mm:ss");
			Date date = new Date();
			String recUuid = UUID.randomUUID().toString();
			String templateSchema = dataMan.autodetectSchema(templateCopy);
			templateCopy = dataMan.setUUID(templateSchema, recUuid, templateCopy); 
		
			String id = dataMan.insertMetadataExt(dbms, templateSchema, templateCopy, context.getSerialFactory(), params.uuid, df.format(date), df.format(date), recUuid, 1, null);
		
			int iId = Integer.parseInt(id);

			log.info("	- Set privileges, category, template and harvested");
			addPrivileges(id);
			dataMan.setCategory (dbms, id, params.recordsCategory);
			
			dataMan.setTemplateExt(dbms, iId, "n", null);
			dataMan.setHarvestedExt(dbms, iId, params.uuid, params.url);

			dataMan.indexMetadataGroup(dbms, id);

			log.info("	- Commit "+id);
			dbms.commit();
			result.recordsBuilt++;
		}
	}
	
	//---------------------------------------------------------------------------
	/** 
     * Insert Link to Fragment - replace all instances of matchId to the uuid
		 * of the fragment
     *   
     * @param templateCopy		Copy of the template for fragment links
     * @param matchId		Id used in template to place fragment
     * @param uuid 			uuid of the fragment inserted into GeoNetwork db
     * 
     */
	private void insertLinkToFragmentIntoTemplate(Element templateCopy, String matchId, String uuid, String title) throws Exception {

		// find all elements that have an attribute id with the matchId
		log.info("Attempting to search metadata for "+matchId);
		List<Element> elems = (List<Element>) Xml.selectNodes(templateCopy,"*//*[@id='"+matchId+"']", theNss);

		// for each of these elements...
		for (Element elem : elems) {
			log.info("Element found "+Xml.getString(elem));
			
			// add uuidref attribute to link to fragment
			elem.setAttribute("uuidref", uuid);
			elem.setAttribute("href", metadataGetService+"?uuid="+uuid, xlink);
			elem.setAttribute("show", "replace", xlink);
			if (title != null) elem.setAttribute("title", title, xlink);
		}
		if (elems.size() > 0) result.fragmentsMatched++;

		log.info("Template with metadata links is\n"+Xml.getString(templateCopy));
	}

	//---------------------------------------------------------------------------
	/** 
     * Add categories according to harvesting configuration
     *   
     * @param id		GeoNetwork internal identifier
     * 
     */
	private void addCategories(String id) throws Exception {

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
	private void addPrivileges(String id) throws Exception {

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
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Logger         log;
	private ServiceContext context;
	private Dbms           dbms;
	private MetadataFragmentsParams   params;
	private DataManager    dataMan;
	private CategoryMapper localCateg;
	private GroupMapper    localGroups;
	private MetadataFragmentsResult   result;
    private Element				 templateForLinks;
	private Namespace			 xlink;
	private boolean 			 buildRecords = true;
	private String	 			 metadataGetService;
	private String	 			 stylesheetDirectory;
	private List<Namespace> theNss = new ArrayList<Namespace>();


}

//=============================================================================

