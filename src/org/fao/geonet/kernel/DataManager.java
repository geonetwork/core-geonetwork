//==============================================================================
//===
//=== DataManager
//===
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

package org.fao.geonet.kernel;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Xml;

import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.FileCopyMgr;
import org.fao.geonet.util.ISODate;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Text;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;

//=============================================================================

/** Handles all operations on metadata (select,insert,update,delete etc...)
  */

public class DataManager
{
	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** initializes the search manager and index not-indexed metadata
	  */

	public DataManager(SearchManager sm, AccessManager am, Dbms dbms, SettingManager ss, String baseURL, String htmlCacheDir) throws Exception
	{
		searchMan = sm;
		accessMan = am;
		settingMan= ss;

		this.baseURL = baseURL;
		this.htmlCacheDir = htmlCacheDir;

		init(dbms, false);
	}

	/**
	 * Init Data manager and refresh index if needed. 
	 * Could be called after GeoNetwork startup in order to rebuild the lucene 
	 * index
	 * 
	 * @param dbms
	 * @param force         Force reindexing all from scratch
	 *
	 **/
	public void init(Dbms dbms, Boolean force) throws Exception {

		// get all metadata from DB
		Element result = dbms.select("SELECT id, changeDate FROM Metadata ORDER BY id ASC");
		List list = result.getChildren();

		Log.debug(Geonet.DATA_MANAGER, "DB CONTENT:\n'"+ Xml.getString(result) +"'"); //DEBUG

		// get all metadata from index
		Hashtable docs = searchMan.getDocs();

        Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT:"); //DEBUG

		// index all metadata in DBMS if needed
		for(int i = 0; i < list.size(); i++)
		{
			// get metadata
			Element record = (Element) list.get(i);
			String  id     = record.getChildText("id");

			Log.debug(Geonet.DATA_MANAGER, "- record ("+ id +")"); //DEBUG

			Hashtable idxRec = (Hashtable)docs.get(id);

			// if metadata is not indexed index it
			if (idxRec == null)
				indexMetadata(dbms, id);

			// else, if indexed version is not the latest index it
			else
			{
				docs.remove(id);

				String lastChange    = record.getChildText("changedate");
				String idxLastChange = (String)idxRec.get("_changeDate");

	            Log.debug(Geonet.DATA_MANAGER, "- lastChange: " + lastChange); //DEBUG
                Log.debug(Geonet.DATA_MANAGER, "- idxLastChange: " + idxLastChange); //DEBUG

				if (force || !idxLastChange.equalsIgnoreCase(lastChange)) // date in index contains 't', date in DBMS contains 'T'
					indexMetadata(dbms, id);
			}
		}

		Log.debug(Geonet.DATA_MANAGER, "INDEX SURPLUS:"); //DEBUG

		// remove from index metadata not in DBMS
		for (Enumeration i = docs.keys(); i.hasMoreElements(); )
		{
			String id = (String)i.nextElement();
			searchMan.delete("_id", id);

            Log.debug(Geonet.DATA_MANAGER, "- record (" + id + ")"); //DEBUG
		}
	}

	//--------------------------------------------------------------------------
	
	public void indexMetadata(Dbms dbms, String id) throws Exception
	{

	    Log.debug(Geonet.DATA_MANAGER, "Indexing record (" + id + ")"); //DEBUG

	    indexMetadata(dbms, id, searchMan);
	}

	//--------------------------------------------------------------------------

	public static void indexMetadata(Dbms dbms, String id, SearchManager sm) throws Exception
	{
		try
		{
			indexMetadataI(dbms, id, sm);
		}
		catch (Exception e)
		{
			Log.error(Geonet.DATA_MANAGER, "The metadata document index with id="+id+" is corrupt/invalid - ignoring it. Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------

	private static void indexMetadataI(Dbms dbms, String id, SearchManager sm) throws Exception
	{
		Vector moreFields = new Vector();

		// get metadata table fields
		Element md   = XmlSerializer.select(dbms, "Metadata", id);
		String  root = md.getName();

		String query ="SELECT schemaId, createDate, changeDate, source, isTemplate, title, uuid, "+
									"isHarvested, owner, groupOwner, popularity, rating FROM Metadata WHERE id = " + id;

		Element rec = dbms.select(query).getChild("record");

		String  schema     = rec.getChildText("schemaid");
		String  createDate = rec.getChildText("createdate");
		String  changeDate = rec.getChildText("changedate");
		String  source     = rec.getChildText("source");
		String  isTemplate = rec.getChildText("istemplate");
		String  title      = rec.getChildText("title");
		String  uuid       = rec.getChildText("uuid");
		String  isHarvested= rec.getChildText("isharvested");
		String  owner      = rec.getChildText("owner");
		String  groupOwner = rec.getChildText("groupowner");
		String  popularity = rec.getChildText("popularity");
		String  rating     = rec.getChildText("rating");

        Log.debug(Geonet.DATA_MANAGER, "record schema (" + schema + ")"); //DEBUG
        Log.debug(Geonet.DATA_MANAGER, "record createDate (" + createDate + ")"); //DEBUG

		moreFields.add(makeField("_root",        root,        true, true, false));
		moreFields.add(makeField("_schema",      schema,      true, true, false));
		moreFields.add(makeField("_createDate",  createDate,  true, true, false));
		moreFields.add(makeField("_changeDate",  changeDate,  true, true, false));
		moreFields.add(makeField("_source",      source,      true, true, false));
		moreFields.add(makeField("_isTemplate",  isTemplate,  true, true, false));
		moreFields.add(makeField("_title",       title,       true, true, false));
		moreFields.add(makeField("_uuid",        uuid,        true, true, false));
		moreFields.add(makeField("_isHarvested", isHarvested, true, true, false));
		moreFields.add(makeField("_owner",       owner,       true, true, false));
		moreFields.add(makeField("_dummy",       "0",        false, true, false));
		moreFields.add(makeField("_popularity",  popularity,  true, true, false));
		moreFields.add(makeField("_rating",      rating,      true, true, false));

		if (groupOwner != null)
			moreFields.add(makeField("_groupOwner", groupOwner, true, true, false));

		// get privileges
		List operations = dbms.select("SELECT groupId, operationId FROM OperationAllowed "+
												"WHERE metadataId = " + id + " ORDER BY operationId ASC").getChildren();

		for (Iterator iter = operations.iterator(); iter.hasNext(); )
		{
			Element operation   = (Element)iter.next();
			String  groupId     = operation.getChildText("groupid");
			String  operationId = operation.getChildText("operationid");

			moreFields.add(makeField("_op" + operationId, groupId, true, true, false));
		}
		// get categories
		List categories = dbms.select("SELECT id, name FROM MetadataCateg, Categories "+
												"WHERE metadataId = " + id + " AND categoryId = id ORDER BY id").getChildren();

		for (Iterator iter = categories.iterator(); iter.hasNext(); )
		{
			Element category     = (Element)iter.next();
			String  categoryName = category.getChildText("name");

			moreFields.add(makeField("_cat", categoryName, true, true, false));
		}

		sm.index(schema, md, id, moreFields, isTemplate, title);
	}

	//--------------------------------------------------------------------------

	private static Element makeField(String name, String value, boolean store,
												boolean index, boolean token)
	{
		Element field = new Element("Field");

		field.setAttribute("name",   name);
		field.setAttribute("string", value);
		field.setAttribute("store",  store+"");
		field.setAttribute("index",  index+"");
		field.setAttribute("token",  token+"");

		return field;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Schema management API
	//---
	//--------------------------------------------------------------------------

	public void setHarvestManager(HarvestManager hm)
	{
		harvestMan = hm;
	}

	//--------------------------------------------------------------------------

	public void addSchema(String id, String xmlSchemaFile, String xmlSuggestFile, String xmlSubstitutesFile) throws Exception
	{
		editLib.addSchema(id, xmlSchemaFile, xmlSuggestFile, xmlSubstitutesFile);
	}

	//--------------------------------------------------------------------------

	public MetadataSchema getSchema(String name)
	{
		return editLib.getSchema(name);
	}

	//--------------------------------------------------------------------------

	public Set<String> getSchemas()
	{
		return editLib.getSchemas();
	}

	//--------------------------------------------------------------------------

	public boolean existsSchema(String name)
	{
		return editLib.existsSchema(name);
	}

	//--------------------------------------------------------------------------

	public String getCasedSchemaName(String name)
	{
		return editLib.getCasedSchemaName(name);
	}

	//--------------------------------------------------------------------------

	public String getSchemaDir(String name)
	{
		return editLib.getSchemaDir(name);
	}

	//--------------------------------------------------------------------------

	public void validate(String schema, Element md) throws Exception
	{
		Xml.validate(editLib.getSchemaDir(schema) + Geonet.File.SCHEMA, md);
	}

	//--------------------------------------------------------------------------

	public String getMetadataSchema(Dbms dbms, String id) throws Exception
	{
		List list = dbms.select("SELECT schemaId FROM Metadata WHERE id = " +id).getChildren();

		if (list.size() == 0)
			throw new IllegalArgumentException("Metadata not found for id : " +id);
		else
		{
			// get metadata
			Element record = (Element) list.get(0);
			return record.getChildText("schemaid");
		}
	}
	
	//--------------------------------------------------------------------------
	
	public Element schemaTron(String schemaPath, Element md, String id, String lang) throws Exception
	{
		String fileSchemaTronReport = doSchemaTronReport(schemaPath,md,id,lang);
		return doSchemaTronForEditor(schemaPath,md,lang);
	}

	//--------------------------------------------------------------------------
	
	public String doSchemaTronReport(String schemaPath, Element md, String id, String lang) throws Exception
	{

		String dirId = "SchematronReport"+id;
		String outDir = htmlCacheDir+File.separator+dirId;
		String inDir  = htmlCacheDir+File.separator+"schematronscripts";

		// copy the schematron templates for the output report
		FileCopyMgr.copyFiles(inDir,outDir);

		// set up the inputs to/output from the XSLT transformer and run it
		// xslt transformer
		String schemaTronReport = schemaPath+File.separator+Geonet.File.SCHEMATRON;
		Map<String,String> params = new HashMap<String,String>();
		params.put("lang",lang);

		// output schematron-errors.html
		String fileOut = outDir+File.separator+"schematron-errors.html";
		File fileResult = new File(fileOut);
		Result result = new StreamResult(fileResult.toURI().getPath());
		try {
			Xml.transform(md,schemaTronReport,result,params);
		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+schemaTronReport+" failed");
			e.printStackTrace();
		}

		// now place anchors in the metadata xml so that schematron-report can
		// show the problems with the XML
		String schemaTronAnchors = schemaPath+File.separator+Geonet.File.SCHEMATRON_VERBID;

		// output schematron-out.html
		String fileSchemaTronOut = outDir+File.separator+"schematron-out.html";
		File schemaTronOut = new File(fileSchemaTronOut);
		Result resultOut = new StreamResult(schemaTronOut.toURI().getPath());
		try {
			Xml.transform(md,schemaTronAnchors,resultOut,params);
		} catch (Exception e) {
			Log.warning(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+schemaTronAnchors+" failed");
			e.printStackTrace();
		}

		return fileSchemaTronOut;
	}

	//--------------------------------------------------------------------------

	public Element doSchemaTronForEditor(String schemaPath,Element md,String lang) throws Exception
	{

		// enumerate the metadata xml so that we can report any problems found 
		// by the schematron_xml script to the geonetwork editor
		editLib.enumerateTree(md);

		// get an xml version of the schematron errors and return for error display
		Element schemaTronXmlReport = getSchemaTronXmlReport(schemaPath, md, lang);

		// remove editing info added by enumerateTree
		editLib.removeEditingInfo(md);

		return schemaTronXmlReport;
	}

	//--------------------------------------------------------------------------
	
	private Element getSchemaTronXmlReport(String schemaPath, Element md, String lang) throws Exception {
		// NOTE: this method assumes that you've run enumerateTree on the 
		// metadata
		String schemaTronXmlXslt = schemaPath+File.separator+Geonet.File.SCHEMATRON_XML;
		Element schemaTronXmlOut = null;
		
		try {
			Map<String,String> params = new HashMap<String,String>();
			params.put("lang",lang);
			schemaTronXmlOut = Xml.transform(md, schemaTronXmlXslt, params);
		} catch (Exception e) {
			Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+schemaTronXmlXslt+" failed");
			e.printStackTrace();
		}

		return schemaTronXmlOut;
	}

	//--------------------------------------------------------------------------
	
	private synchronized boolean getXSDXmlReport(String schema, Element md) throws Exception {

		// NOTE: this method assumes that enumerateTree has NOT been run on the
		// metadata

		Element xsdErrors = Xml.validateInfo(getSchemaDir(schema) + Geonet.File.SCHEMA, md);
		if (xsdErrors != null) {
			MetadataSchema mds = getSchema(schema);
			List<Namespace> schemaNamespaces = mds.getSchemaNS();
		
			//-- now get each xpath and evaluate it
			//-- xsderrors/xsderror/{message,xpath} 
			List list = xsdErrors.getChildren();
			for (Object o : list) {
				Element elError = (Element) o;
				String xpath = elError.getChildText("xpath");
				String message = elError.getChildText("message");
				message = "\\n"+message;

				//-- get the element from the xpath and add the error message to it 
				Element elem = Xml.selectElement(md, xpath, schemaNamespaces);
				if (elem != null) {
					String existing = elem.getAttributeValue("xsderror",Edit.NAMESPACE);
					if (existing != null) message = existing + message;
					elem.setAttribute("xsderror",message,Edit.NAMESPACE);
				} else {
					Log.warning(Geonet.DATA_MANAGER,"WARNING: evaluating XPath "+xpath+" against metadata failed - XSD validation message: "+message+" will NOT be shown by the editor");
				}
			}

			return true;
		} else { 
			//-- no validation errors
			return false;
		}
	}

	//--------------------------------------------------------------------------

	public AccessManager getAccessManager()
	{
		return accessMan;
	}

	//--------------------------------------------------------------------------
	//---
	//--- General purpose API
	//---
	//--------------------------------------------------------------------------

	public String extractUUID(String schema, Element md) throws Exception
	{
		String styleSheet = editLib.getSchemaDir(schema) + Geonet.File.EXTRACT_UUID;
		String uuid       = Xml.transform(md, styleSheet).getText().trim();

		Log.debug(Geonet.DATA_MANAGER, "Extracted UUID '"+ uuid +"' for schema '"+ schema +"'");

		//--- needed to detach md from the document
		md.detach();

		return uuid;
	}

	//--------------------------------------------------------------------------

	public Element setUUID(String schema, String uuid, Element md) throws Exception
	{
		//--- setup environment

		Element env = new Element("env");
		env.addContent(new Element("uuid").setText(uuid));

		//--- setup root element

		Element root = new Element("root");
		root.addContent(md.detach());
		root.addContent(env.detach());

		//--- do an XSL  transformation

		String styleSheet = editLib.getSchemaDir(schema) + Geonet.File.SET_UUID;

		return Xml.transform(root, styleSheet);
	}

	@SuppressWarnings("unchecked")
	public List<Element> getMetadataByHarvestingSource(Dbms dbms, String harvestingSource) throws Exception {
		String query = "SELECT id FROM Metadata WHERE harvestUuid=?";
		return dbms.select(query, harvestingSource).getChildren();
	}


	//--------------------------------------------------------------------------

	public String getMetadataId(Dbms dbms, String uuid) throws Exception
	{
		String query = "SELECT id FROM Metadata WHERE uuid=?";

		List list = dbms.select(query, uuid).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		return record.getChildText("id");
	}

	//--------------------------------------------------------------------------

	public String getMetadataId(ServiceContext srvContext, String uuid) throws Exception {
		Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);
		String query = "SELECT id FROM Metadata WHERE uuid=?";
		List list = dbms.select(query, uuid).getChildren();
		if (list.size() == 0)
			return null;
		Element record = (Element) list.get(0);
		return record.getChildText("id");
	}
	//--------------------------------------------------------------------------

	public String getMetadataUuid(Dbms dbms, String id) throws Exception
	{
		String query = "SELECT uuid FROM Metadata WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		return record.getChildText("uuid");
	}

	//--------------------------------------------------------------------------

	public MdInfo getMetadataInfo(Dbms dbms, String id) throws Exception
	{
		String query = "SELECT id, uuid, schemaId, isTemplate, isHarvested, createDate, "+
							"       changeDate, source, title, root, owner, groupOwner "+
							"FROM   Metadata "+
							"WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		MdInfo info = new MdInfo();

		info.id          = id;
		info.uuid        = record.getChildText("uuid");
		info.schemaId    = record.getChildText("schemaid");
		info.isHarvested = "y".equals(record.getChildText("isharvested"));
		info.createDate  = record.getChildText("createdate");
		info.changeDate  = record.getChildText("changedate");
		info.source      = record.getChildText("source");
		info.title       = record.getChildText("title");
		info.root        = record.getChildText("root");
		info.owner       = record.getChildText("owner");
		info.groupOwner  = record.getChildText("groupowner");

		String temp = record.getChildText("istemplate");

		if ("y".equals(temp))
			info.template = MdInfo.Template.TEMPLATE;

		else if ("s".equals(temp))
			info.template = MdInfo.Template.SUBTEMPLATE;

		else
			info.template = MdInfo.Template.METADATA;

		return info;
	}

	//--------------------------------------------------------------------------

	public String getVersion(String id)
	{
		return editLib.getVersion(id);
	}

	//--------------------------------------------------------------------------

	public String getNewVersion(String id)
	{
		return editLib.getNewVersion(id);
	}

	//--------------------------------------------------------------------------

	public void setTemplate(Dbms dbms, int id, String isTemplate, String title) throws Exception
	{
		if (title == null) dbms.execute("UPDATE Metadata SET isTemplate=? WHERE id=?", isTemplate, id);
		else               dbms.execute("UPDATE Metadata SET isTemplate=?, title=? WHERE id=?", isTemplate, title, id);
		indexMetadata(dbms, Integer.toString(id));
	}

	//--------------------------------------------------------------------------

	public void setHarvested(Dbms dbms, int id, String harvestUuid) throws Exception
	{
		String value = (harvestUuid != null) ? "y" : "n";
		if (harvestUuid == null) {
			dbms.execute("UPDATE Metadata SET isHarvested=? WHERE id=?", value,id );
		} else {
			dbms.execute("UPDATE Metadata SET isHarvested=?, harvestUuid=? WHERE id=?", value, harvestUuid, id);
		}

		indexMetadata(dbms, Integer.toString(id));
	}

	//--------------------------------------------------------------------------

	public void setHarvested(Dbms dbms, int id, String harvestUuid, String harvestUri) throws Exception
	{
		String value = (harvestUuid != null) ? "y" : "n";
		String query = "UPDATE Metadata SET isHarvested=?, harvestUuid=?, harvestUri=? WHERE id=?";

		dbms.execute(query, value, harvestUuid, harvestUri, id);
		indexMetadata(dbms, Integer.toString(id));
	}

	//---------------------------------------------------------------------------

	public String getSiteURL()
	{
		String host    = settingMan.getValue("system/server/host");
		String port    = settingMan.getValue("system/server/port");
		String locServ = baseURL +"/"+ Jeeves.Prefix.SERVICE +"/en";

		return "http://" + host + (port == "80" ? "" : ":" + port) + locServ;
	}

	//--------------------------------------------------------------------------

	public String autodetectSchema(Element md)
	{
		Namespace nons= Namespace.NO_NAMESPACE;
		
		Namespace metadatadRootElemenNSUri = md.getNamespace();

		List<Namespace> metadataAdditionalNS = md.getAdditionalNamespaces();
		
		Log.debug(Geonet.DATA_MANAGER, "Autodetect schema for metadata with :\n * root element:'" + md.getQualifiedName()
				 + "'\n * with namespace:'" + md.getNamespace()
				 + "\n * with additional namespaces:" + metadataAdditionalNS.toString());
		
		if (md.getName().equals("Record") && md.getNamespace().equals(Csw.NAMESPACE_CSW)) {
			return "csw-record";
		} else if (md.getNamespace().equals(nons)) {
			if (md.getName().equals("Metadata")) {
				return "iso19115";
			}

			/* there are some other suggested container names,
			 * like <dc>, <dublinCore>, <resource>, <record> and <metadata>
			 * We may need to also check for those on import and export
			 */
			if (md.getName().equals("simpledc")) {
				return "dublin-core";
			}
			if (md.getName().equals("metadata")) {
				return "fgdc-std";
			}
		} else if (metadataAdditionalNS.contains(Csw.NAMESPACE_GMD)
				|| metadatadRootElemenNSUri.equals(Csw.NAMESPACE_GMD)) {
			// Here we have an iso19139 or an ISO profil
			
			// the root element will have different namespace (element name
			// and additionnal namespace).
			// this is important for profiles which usually need to override the top level
			// element for proper definition 
			// eg. mcp:MD_Metadata versus wmo:MD_Metadata
			//
			// But profil for france does not override top level element. But only sub
			// elements.
			// 
			// we suppose that the root element declare the prime namespace of the profil
			// declared as targetNamespace of schema.xsd.
			// eg. <gmd:MD_Metadata  xmlns:gmd="http://www.isotc211.org/2005/gmd
			//	 xmlns:fra="http://www.cnig.gouv.fr/2005/fra" ...
			
			// FIXME : Issue if :
			// eg. <gmd:MD_Metadata xmlns:gmd="http://www.isotc211.org/2005/gmd ..;
			//	 <fra:FRA_DataIdentification xmlns:fra="http://www.cnig.gouv.fr/2005/fra">
			// if profil specific namespace only declared on sub elements and not on root.

			
			for (String schema : getSchemas()) {
				MetadataSchema mds = getSchema(schema);
				String primeNs = mds.getPrimeNS();

				// Check if gmd is not the root element namespace
				// and root element as a namespace which is
				// defined in one schema, we have an ISO profil
				// and current schema is ok. 
				if (metadatadRootElemenNSUri.getURI().equals(primeNs) && 
						!metadatadRootElemenNSUri.equals(Csw.NAMESPACE_GMD)) {
					return schema;
				}
				
				// Check if a prime namespace exists in all
				// additional namespaces of the root element
				for (Namespace ns : metadataAdditionalNS) {
					if (ns.getURI().equals(primeNs) &&
							metadatadRootElemenNSUri.equals(Csw.NAMESPACE_GMD)) {
						return schema;
					}
				}
			}
			
			// Default schema name is 
			return "iso19139";
		}
		return null;
	}

	//--------------------------------------------------------------------------

	public void increasePopularity(Dbms dbms, String id) throws Exception
	{
		String query = "UPDATE Metadata SET popularity = popularity +1 WHERE "+
							"id = ? AND isHarvested='n'";

		dbms.execute(query, new Integer(id));
		indexMetadata(dbms, id);
	}

	//--------------------------------------------------------------------------
	/** Allow to rate a metadata
	  * @param ipAddress IP address of the submitting client
	  * @param rating range should be 1..5
	  */

	public int rateMetadata(Dbms dbms, int id, String ipAddress, int rating) throws Exception
	{
		//--- first, update rating on the database

		String query = "UPDATE MetadataRating SET rating=? WHERE metadataId=? AND ipAddress=?";

		int res = dbms.execute(query, rating, id, ipAddress);

		if (res == 0)
		{
			query = "INSERT INTO MetadataRating(metadataId, ipAddress, rating) VALUES(?,?,?)";
			dbms.execute(query, id, ipAddress, rating);
		}

		//--- then, calculate new rating

		query = "SELECT sum(rating) as total FROM MetadataRating WHERE metadataId=?";
		List list = dbms.select(query, id).getChildren();

		String sum = ((Element) list.get(0)).getChildText("total");

		query = "SELECT count(*) as numr FROM MetadataRating WHERE metadataId=?";
		list  = dbms.select(query, id).getChildren();

		String count = ((Element) list.get(0)).getChildText("numr");

		rating = (int)(Float.parseFloat(sum) / Float.parseFloat(count) + 0.5);

		Log.debug(Geonet.DATA_MANAGER, "Setting rating for id:"+ id +" --> rating is:"+rating);

		//--- finally, update metadata and reindex it

		query = "UPDATE Metadata SET rating=? WHERE id=?";
		dbms.execute(query, rating, id);
		indexMetadata(dbms, Integer.toString(id));

		return rating;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Insert API
	//---
	//--------------------------------------------------------------------------

	/** Create a new metadata duplicating an existing template
	  */

	public String createMetadata(Dbms dbms, String templateId, String groupOwner,
										  SerialFactory sf, String source, int owner) throws Exception
	{
		String query = "SELECT schemaId, data FROM Metadata WHERE id="+ templateId;

		List listTempl = dbms.select(query).getChildren();

		if (listTempl.size() == 0)
			throw new IllegalArgumentException("Template id not found : " + templateId);

		Element el = (Element) listTempl.get(0);

		String schema = el.getChildText("schemaid");
		String data   = el.getChildText("data");
		String uuid   = UUID.randomUUID().toString();

		//--- generate a new metadata id
		int serial = sf.getSerial(dbms, "Metadata");

		Element xml = updateFixedInfoNew(schema, Integer.toString(serial), Xml.loadString(data, false), uuid);

		//--- store metadata

		String id = XmlSerializer.insert(dbms, schema, xml, serial, source, uuid, owner, groupOwner);

		copyDefaultPrivForGroup(dbms, id, groupOwner);

		//--- store metadata categories copying them from the template

		List categList = dbms.select("SELECT categoryId FROM MetadataCateg WHERE metadataId = "+templateId).getChildren();

		for(int i=0; i<categList.size(); i++)
		{
			Element elRec = (Element) categList.get(i);

			String catId = elRec.getChildText("categoryid");

			setCategory(dbms, id, catId);
		}

		//--- index metadata and exit

		indexMetadata(dbms, id);

		return id;
	}

	//--------------------------------------------------------------------------
	/** Adds a metadata in xml form (the xml should be validated). This method is
	  * used to add a metadata got from a remote site via a mef and the data has 
		* NOT been included. Note that neither permissions nor lucene indexes are 
		* updated.
	  */

	public String insertMetadataExt(Dbms dbms, String schema, Element md, 
												SerialFactory sf, String source, String createDate, 
												String changeDate, String uuid, int owner, 
												String groupOwner ) throws Exception
	{
		//--- generate a new metadata id
		int id = sf.getSerial(dbms, "Metadata");

		return insertMetadataExt(dbms, schema, md, id, source, createDate, changeDate, uuid,
										 owner, groupOwner, "n");
	}

	//--------------------------------------------------------------------------
	/** Adds a metadata in xml form (the xml should be validated). This method is
	  * used to add a metadata got from a remote site via a mef and the data has 
		* been included. Note that neither permissions nor lucene indexes are 
		* updated.
	  */

	public String insertMetadataExt(Dbms dbms, String schema, Element md, 
							SerialFactory sf, String source, String createDate, 
							String changeDate, String uuid, int owner, String groupOwner, 
							String isTemplate ) throws Exception
	{
		//--- generate a new metadata id
		int id = sf.getSerial(dbms, "Metadata");

		if (isTemplate.equals("n"))
			md = updateFixedInfoExisting(schema, Integer.toString(id), md, uuid);

		return insertMetadataExt(dbms, schema, md, id, source, createDate, changeDate, uuid,
										 owner, groupOwner, isTemplate);
	}

	//--------------------------------------------------------------------------
	/** @param source the source of the metadata. If null, the local siteId will be used
	  */

	public String insertMetadataExt(Dbms dbms, String schema, Element md, int id,
											  String source, String createDate, String changeDate,
											  String uuid, int owner, String groupOwner, String isTemplate) throws Exception
	{
		if (source == null)
			source = getSiteID();

		//--- force namespace prefix for iso19139 metadata
		setNamespacePrefixUsingSchemas(md);

		//--- Note: we cannot index metadata here. Indexing is done in the harvesting part

		return XmlSerializer.insert(dbms, schema, md, id, source, uuid, createDate,
											 changeDate, isTemplate, null, owner, groupOwner);
	}

	//--------------------------------------------------------------------------
	/** Adds a metadata in xml form (the xml should be validated). The group id is
	  * used to setup permissions. Internal metadata fields are updated. Default
	  * operations are set.
	  */

	public String insertMetadata(Dbms dbms, String schema, String category, String groupId, Element xml, SerialFactory sf, String source, String uuid, int owner) throws Exception
	{
		return insertMetadata(dbms, schema, category, groupId, xml, sf, source, uuid, "n", null, owner);
	}

	//--------------------------------------------------------------------------

	public String insertMetadata(Dbms dbms, String schema, String category, String groupOwner, Element xml, SerialFactory sf, String source, String uuid, String isTemplate, String title, int owner) throws Exception
	{
		//--- generate a new metadata id
		int serial = sf.getSerial(dbms, "Metadata");

		//--- force namespace prefix for iso19139 metadata
		setNamespacePrefixUsingSchemas(xml);

		if (isTemplate.equals("n"))
			xml = updateFixedInfoExisting(schema, Integer.toString(serial), xml, uuid);

		//--- store metadata

		String id = XmlSerializer.insert(dbms, schema, xml, serial, source, uuid, isTemplate, title, owner, groupOwner);

		copyDefaultPrivForGroup(dbms, id, groupOwner);
		if (category != null)
			setCategory(dbms, id, category);
		indexMetadata(dbms, id);

		return id;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Get API
	//---
	//--------------------------------------------------------------------------

	/** Retrieves a metadata (in xml) given its id; adds editing information 
	 *  if requested and does NOT include validation errors
	 */

	public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing) throws Exception
	{
		return getMetadata(srvContext, id, forEditing, false);
	}

	/** Retrieves a metadata (in xml) given its id; adds editing information 
	 *  if requested and validation errors if requested
	 */
	public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean withEditorValidationErrors) throws Exception
	{

		Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);

		Element md = XmlSerializer.select(dbms, "Metadata", id);

		if (md == null)
			return null;

		String version = null;

		if (forEditing)
		{
			String schema = getMetadataSchema(dbms, id);

			boolean xsdErrors = false;
			if (withEditorValidationErrors) {
				//-- get an XSD validation report and add results to the metadata 
				//-- as geonet:xsderror attributes on the affected elements
				xsdErrors = getXSDXmlReport(schema,md);
			}

			//-- now expand the elements and add the geonet: elements
			editLib.expandElements(schema,md);
			version = editLib.getVersionForEditing(schema, id, md);

			if (withEditorValidationErrors && !xsdErrors) {
				//-- get a schematron error report if no xsd errors and add results
				//-- to the metadata as a geonet:schematronerrors element with 
				//-- links to the ref id of the affected element
				Element condChecks = getSchemaTronXmlReport(getSchemaDir(schema),md,srvContext.getLanguage());
				if (condChecks != null) md.addContent(condChecks);
			}
		}

		md.addNamespaceDeclaration(Edit.NAMESPACE);
		Element info = buildInfoElem(srvContext, id, version);
		md.addContent(info);

		md.detach();
		return md;
	}

	//--------------------------------------------------------------------------
	/** Retrieves a metadata element given it's ref
	 */

	public Element getElementByRef(Element md, String ref)
	{
		return editLib.findElement(md, ref);
	}

	//--------------------------------------------------------------------------
	/** Returns true if the metadata exists in the database
	  */

	public boolean existsMetadata(Dbms dbms, String id) throws Exception
	{
		//FIXME : should use lucene

		List list = dbms.select("SELECT id FROM Metadata WHERE id="+ id).getChildren();
		return list.size() != 0;
	}

	/** Returns true if the metadata uuid exists in the database
	  */

	public boolean existsMetadataUuid(Dbms dbms, String uuid) throws Exception
	{
		//FIXME : should use lucene

		List list = dbms.select("SELECT uuid FROM Metadata WHERE uuid='" + uuid + "'").getChildren();
		return list.size() != 0;
	}

	//--------------------------------------------------------------------------
	/** Returns all the keywords in the system
	  */

	public Element getKeywords() throws Exception
	{
		Vector keywords = searchMan.getTerms("keyword");

		Element el = new Element("keywords");

		for(int i=0; i<keywords.size(); i++)
			el.addContent(new Element("keyword").setText((String)keywords.get(i)));

		return el;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Embedded Metadata Update API for AJAX Editor support
	//---
	//--------------------------------------------------------------------------

	private Element getMetadataFromSession(UserSession session)
	{
		Log.debug(Geonet.DATA_MANAGER, "Retrieving metadata from session "+session.getUserId());
		Element md = (Element) session.getProperty(Geonet.Session.METADATA_EDITING);
		md.detach();
		return md;
	}

	private void setMetadataIntoSession(UserSession session, Element md)
	{
		Log.debug(Geonet.DATA_MANAGER, "Storing metadata in session "+session.getUserId());
		session.setProperty(Geonet.Session.METADATA_EDITING, md);
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : removes metadata from session
	  */
	public void removeMetadataEmbedded(UserSession session)
	{
		Log.debug(Geonet.DATA_MANAGER, "Removing metadata from session "+session.getUserId());
		session.removeProperty(Geonet.Session.METADATA_EDITING);
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : gets Metadata from database and places it in session
	  */
	public Element getMetadataEmbedded(ServiceContext srvContext, String id, boolean forEditing, boolean withValidationErrors) throws Exception
	{
		Element md = getMetadata(srvContext, id, forEditing, withValidationErrors);

		UserSession session = srvContext.getUserSession();
		setMetadataIntoSession(session,md);
		return md;
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : adds an element to a metadata ([add] link)
	  */
	public synchronized Element addElementEmbedded(Dbms dbms, UserSession session, String id, String ref, String name, String childName)  throws Exception
	{

		String  schema = getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session);

		//--- ref is parent element so find it
		Element el = (Element)editLib.findElement(md, ref);
		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- locate the geonet:element and geonet:info elements and clone for 
		//--- later re-use
		Element refEl = (Element)(el.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE)).clone();
		Element info = (Element)(md.getChild(Edit.RootChild.INFO,Edit.NAMESPACE)).clone();
		md.removeChild(Edit.RootChild.INFO,Edit.NAMESPACE);
		
		//--- normal element
		Element child = editLib.addElement(schema, el, name);
		MetadataSchema mds = editLib.getSchema(schema);
		if (childName != null && !childName.equals(""))
		{
			//--- or element
			String uChildName = editLib.getUnqualifiedName(childName);
      String prefix     = editLib.getPrefix(childName);
      String ns         = editLib.getNamespace(childName,md,mds);
      if (prefix.equals("")) {
         prefix = editLib.getPrefix(el.getName());
         ns = editLib.getNamespace(el.getName(),md,mds);
      }
      Element orChild = new Element(uChildName,prefix,ns);
      child.addContent(orChild);

      //--- add mandatory sub-tags
      editLib.fillElement(schema, child, orChild);
		}

		//--- now add the geonet:element back again to keep ref number
		el.addContent(refEl);

		//--- now enumerate the new child
		int iRef = editLib.findMaximumRef(md);
		editLib.expandElements(schema, child);
		editLib.enumerateTreeStartingAt(child, iRef+1, Integer.parseInt(ref));

		//--- add editing info to everything from the parent down
		editLib.expandTree(mds,el);

		//--- attach the info element to the child (and the metadata root)
		child.addContent(info);
		md.addContent((Element)info.clone());

		//--- store the metadata in the session again 
		setMetadataIntoSession(session,(Element)md.clone());

		// Return element added
		return child;

	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : removes an element from a metadata ([del] link)
	  */

	public synchronized Element deleteElementEmbedded(Dbms dbms, UserSession session, String id, String ref, String parentRef) throws Exception
	{

		String schema = getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session);

		//--- locate the geonet:info element and clone for later re-use
		Element info = (Element)(md.getChild(Edit.RootChild.INFO,Edit.NAMESPACE)).clone();
		md.removeChild(Edit.RootChild.INFO,Edit.NAMESPACE);

		//--- get element to remove
		Element el = editLib.findElement(md, ref);

		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);


		String uName = el.getName();
		Namespace ns = el.getNamespace();
		Element parent = el.getParentElement();
		Element result = null;
		if (parent != null) {
			int me = parent.indexOf(el);
		
			//--- check and see whether the element to be deleted is the last one 
			Filter elFilter = new ElementFilter(uName,ns);
			if (parent.getContent(elFilter).size() == 1) {

				//--- get geonet child element with attribute name = unqualified name 
				Filter chFilter = new ElementFilter(Edit.RootChild.CHILD, Edit.NAMESPACE);
				List children = parent.getContent(chFilter);
				for (int i=0;i<children.size();i++) {
					Element ch = (Element)children.get(i);
					String name = ch.getAttributeValue("name");
					if (name != null && name.equals(uName)) {
						result = (Element)ch.clone();
						// -- now delete the element as requested
						parent.removeContent(me);
					}
				}

				//--- existing geonet child element not present so create it
				if (result == null) {
					result = editLib.createElement(schema,el,parent);
					parent.setContent(me,result);
				}
				result.setAttribute(Edit.ChildElem.Attr.PARENT,parentRef);
				result.addContent(info);
			} 
			//--- if not the last one then just delete it
			else {
				parent.removeContent(me);
			}
		} else {
			throw new IllegalStateException("Element at ref = " + ref + " doesn't have a parent");
		}

		// if we don't need a child then create a geonet:null element
		if (result == null) {
			result = new Element(Edit.RootChild.NULL, Edit.NAMESPACE);
		}

		//--- reattach the info element to the metadata
		md.addContent((Element)info.clone());

		//--- store the metadata in the session again 
		setMetadataIntoSession(session,(Element)md.clone());

		return result;
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : swap element with sibling ([up] and [down] links)
	  */

	public synchronized void swapElementEmbedded(Dbms dbms, UserSession session, String id, String ref, boolean down) throws Exception
	{
		String schema = getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session);

		//--- get element to swap
		Element elSwap = editLib.findElement(md, ref);

		if (elSwap == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- swap the elements
		int iSwapIndex = -1;

		List list = ((Element) elSwap.getParent()).getChildren(elSwap.getName(), elSwap.getNamespace());

		for(int i=0; i<list.size(); i++)
			if (list.get(i) == elSwap)
			{
				iSwapIndex = i;
				break;
			}

		if (iSwapIndex == -1)
			throw new IllegalStateException("Index not found for element --> " + elSwap);

		if (down)	swapElements(elSwap, (Element) list.get(iSwapIndex +1));
			else		swapElements(elSwap, (Element) list.get(iSwapIndex -1));

		//--- store the metadata in the session again 
		setMetadataIntoSession(session,(Element)md.clone());

		return;
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : updates all leaves with new values
	  */

	public synchronized boolean updateMetadataEmbedded(UserSession session,
			Dbms dbms, String id, String currVersion, Hashtable changes,
			String lang) throws Exception {
		String schema = getMetadataSchema(dbms, id);

		// --- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion)) {
			Log.error(Geonet.DATA_MANAGER, "Version mismatch: had "
					+ currVersion + " but expected " + editLib.getVersion(id));
			return false;
		}

		// --- get metadata from session
		Element md = getMetadataFromSession(session);

		// Store XML fragments to be handled after other elements update
		HashMap<String, String> xmlInputs = new HashMap<String, String>();

		// --- update elements
		for (Enumeration e = changes.keys(); e.hasMoreElements();) {
			String ref = ((String) e.nextElement()).trim();
			String val = ((String) changes.get(ref)).trim();
			String attr = null;

			// Catch element starting with a X to replace XML fragments
			if (ref.startsWith("X")) {
				ref = ref.substring(1);
				xmlInputs.put(ref, val);
				continue;
			}

			if (ref.equals(""))
				continue;

			if (updatedLocalizedTextElement(md, ref, val)) {
				continue;
			}

			int at = ref.indexOf("_");
			if (at != -1) {
				attr = ref.substring(at + 1);
				ref = ref.substring(0, at);
			}

			Element el = editLib.findElement(md, ref);
			if (el == null)
				Log.error(Geonet.DATA_MANAGER, "Element not found at ref = " + ref);

			if (attr != null) {
				Integer indexColon = attr.indexOf("COLON");
				if (indexColon != -1) {
					String prefix = attr.substring(0, indexColon);
					String localname = attr.substring(indexColon + 5);
					String namespace = editLib.getNamespace(prefix + ":"
							+ localname, md, getSchema(schema));
					Namespace attrNS = Namespace
							.getNamespace(prefix, namespace);
					if (el.getAttribute(localname, attrNS) != null) {
						el.setAttribute(new Attribute(localname, val, attrNS));
					}
				} else {
					if (el.getAttribute(attr) != null)
						el.setAttribute(new Attribute(attr, val));
				}
			} else {
				List content = el.getContent();

				for (int i = 0; i < content.size(); i++) {
					if (content.get(i) instanceof Text) {
						el.removeContent((Text) content.get(i));
						i--;
					}
				}
				el.addContent(val);
			}
		}

		// Deals with XML fragments to insert or update
		if (!xmlInputs.isEmpty()) {

			// Loop over each XML fragments to insert or replace
			for (Iterator<String> it = xmlInputs.keySet().iterator(); it
					.hasNext();) {
				String ref = it.next();
				String value = xmlInputs.get(ref);

				String name = null;
				int addIndex = ref.indexOf("_");
				if (addIndex != -1) {
					name = ref.substring(addIndex + 1);
					ref = ref.substring(0, addIndex);
				}

				// Get element to fill
				Element el = editLib.findElement(md, ref);

				if (el == null)
					throw new IllegalStateException(
							"Element not found at ref = " + ref);

				if (value != null && !value.equals("")) {
					String[] fragments = value.split("&&&");
					for (String fragment : fragments) {
						if (name != null) {
							name = name.replace("COLON", ":");
							editLib.addFragment(schema, el, name, fragment);
						} else {
							// clean before update
							el.removeContent();
							// Add content
							el.addContent(Xml.loadString(fragment, false));
						}
					}
					Log.debug(Geonet.DATA_MANAGER, "replacing XML content");
				}
			}
		}

		// --- remove editing info
		editLib.removeEditingInfo(md);

		md.detach();
		return updateMetadata(session, dbms, id, md, false, currVersion, lang);

	}

	/**
	 * Add a localised character string to an element.
	 * 
	 * @param md metadata record
	 * @param ref current ref of element. All _lang_AB_123 element will be processed.
	 * @param val
	 * @return
	 */
    private boolean updatedLocalizedTextElement(Element md, String ref, String val)
    {
        if (ref.startsWith("lang"))
        {
            if (val.length() > 0)
            {
                String[] ids = ref.split("_");
                // --- search element in current metadata record
                Element parent = editLib.findElement(md, ids[2]);

                // --- add required attribute
                parent.setAttribute("type", 
                			"gmd:PT_FreeText_PropertyType", 
                			Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
                		);
                
                // --- add new translation
                Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
                Element langElem = new Element("LocalisedCharacterString", gmd);
                langElem.setAttribute("locale", "#" + ids[1]);
                langElem.setText(val);

                Element freeText = getOrAdd(parent, "PT_FreeText", gmd);

                Element textGroup = new Element("textGroup", gmd);
                freeText.addContent(textGroup);
                textGroup.addContent(langElem);
                Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
                refElem.setAttribute(Edit.Element.Attr.REF, "");
                textGroup.addContent(refElem);
                langElem.addContent((Element) refElem.clone());
            }
            return true;
        }
        return false;
    }
	
    /**
     * If no PT_FreeText element exist create a geonet:element with
     * an empty ref.
     * 
     * @param parent
     * @param name
     * @param ns
     * @return
     */
	private Element getOrAdd(Element parent, String name, Namespace ns)
	{
		Element child = parent.getChild(name, ns);
		if (child == null)
		{
			child = new Element(name, ns);
			Element refElem = new Element(Edit.RootChild.ELEMENT, Edit.NAMESPACE);
			refElem.setAttribute(Edit.Element.Attr.REF, "");
			child.addContent(refElem);
			parent.addContent(child);
		}
		return child;
	}
	//--------------------------------------------------------------------------
	/** For Ajax Editing : retrieves metadata from session and validates it
	  */

	public void validateMetadataEmbedded(UserSession session, Dbms dbms, String id, String lang) throws Exception
	{
		String schema = getMetadataSchema(dbms, id);

		//--- get metadata from session and clone it for validation
		Element realMd = getMetadataFromSession(session);
		Element md = (Element)realMd.clone();

		//--- remove editing info
		editLib.removeEditingInfo(md);
		editLib.contractElements(md);
		md = updateFixedInfo(schema, id, md, dbms);

		//--- do the validation on the metadata
		doValidate(session,schema,id,md, lang);

	}
	
	//--------------------------------------------------------------------------
	/** For snippet service: create a new element from schema
	  */

	public synchronized Element snippetElement(String schema, String grandParentName, String parentName, String childName) throws Exception
	{

		MetadataSchema mds = editLib.getSchema(schema);
		String uChildName = editLib.getUnqualifiedName(childName);
    String prefix     = editLib.getPrefix(childName);
    String ns         = editLib.getNamespace(childName,mds);
    Element child     = new Element(uChildName,prefix,ns);

    //--- add mandatory sub-tags
    editLib.fillElement(schema, parentName, child);

		//--- expand tree to add container children and return
    editLib.expandElements(schema, child);
    editLib.addEditingInfoToSnippet(schema, grandParentName, parentName, child);
		return child;
	}

	//--------------------------------------------------------------------------
	/** For Editing : adds an attribute from a metadata ([add] link) 
	  * FIXME: Modify and use within Ajax controls
	  */

	public synchronized boolean addAttribute(Dbms dbms, String id, String ref,
														  String name, String currVersion) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;

		String schema = getMetadataSchema(dbms, id);
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--- get element to add
		Element el = editLib.findElement(md, ref);

		if (el == null)
			Log.error(Geonet.DATA_MANAGER, "Element not found at ref = " + ref);
			//throw new IllegalStateException("Element not found at ref = " + ref);

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

		el.setAttribute(new Attribute(name, ""));

		editLib.contractElements(md);
		md = updateFixedInfo(schema, id, md, dbms);
		XmlSerializer.update(dbms, id, md);

		//--- update search criteria
		indexMetadata(dbms, id);

		return true;
	}

	//--------------------------------------------------------------------------
	/** For Editing : removes an attribute from a metadata ([del] link) 
	  * FIXME: Modify and use within Ajax controls
	  */

	public synchronized boolean deleteAttribute(Dbms dbms, String id, String ref,
															  String name, String currVersion) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;

		String schema = getMetadataSchema(dbms, id);
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--- get element to remove
		Element el = editLib.findElement(md, ref);

		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

		el.removeAttribute(name);

		editLib.contractElements(md);
		md = updateFixedInfo(schema, id, md, dbms);
		XmlSerializer.update(dbms, id, md);

		//--- update search criteria
		indexMetadata(dbms, id);

		return true;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Update API
	//---
	//--------------------------------------------------------------------------

	//--------------------------------------------------------------------------
	/** For update of owner info
	  */

	public synchronized void updateMetadataOwner(UserSession session, Dbms dbms, String id, String owner, String groupOwner) throws Exception
	{
		updateMetadataOwner(session,dbms,new Integer(id),owner,groupOwner);
	}

	public synchronized void updateMetadataOwner(UserSession session, Dbms dbms, int id, String owner, String groupOwner) throws Exception
	{
		dbms.execute("UPDATE Metadata SET owner=?, groupOwner=? WHERE id=?", owner, groupOwner, id);
	}

	//--------------------------------------------------------------------------
	/** For Editing : updates all leaves with new values
	  */

	public synchronized boolean updateMetadata(UserSession session, Dbms dbms, String id, String currVersion, Hashtable changes, boolean validate, String lang) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		//--- check if the metadata has been deleted
		if (md == null)
			return false;
		String schema = getMetadataSchema(dbms, id);
		editLib.expandElements(schema, md);
		editLib.enumerateTree(md);

		//--- check if the metadata has been modified from last time
		if (currVersion != null && !editLib.getVersion(id).equals(currVersion))
			return false;

		//--------------------------------------------------------------------
		//--- update elements

		for(Enumeration e=changes.keys(); e.hasMoreElements();)
		{
			String ref = ((String) e.nextElement()) .trim();
			String val = ((String) changes.get(ref)).trim();
			String attr= null;

			if(updatedLocalizedTextElement(md, ref, val)) {
			    continue;
			}
			
			int at = ref.indexOf("_");
			if (at != -1)
			{
				attr = ref.substring(at +1);
				ref  = ref.substring(0, at);
			}
			boolean xmlContent = false;
            if (ref.startsWith("X"))
            {
                ref = ref.substring(1);
                xmlContent = true;
            }
			Element el = editLib.findElement(md, ref);
			if (el == null)
				throw new IllegalStateException("Element not found at ref = " + ref);

			if (attr != null) {
// The following work-around decodes any attribute name that has a COLON in it
// The : is replaced by the word COLON in the xslt so that it can be processed
// by the XML Serializer when an update is submitted - a better solution is 
// to modify the argument handler in Jeeves to store arguments with their name
// as a value rather than as the element itself
				Integer indexColon = attr.indexOf("COLON");
        if (indexColon != -1) {
					String prefix = attr.substring(0,indexColon);
          String localname = attr.substring(indexColon + 5);
          String namespace = editLib.getNamespace(prefix+":"+localname,md,getSchema(schema));
					Namespace attrNS = Namespace.getNamespace(prefix,namespace);
          if (el.getAttribute(localname,attrNS) != null) {
            el.setAttribute(new Attribute(localname,val,attrNS));
          }
// End of work-around
        } else {
          if (el.getAttribute(attr) != null)
            el.setAttribute(new Attribute(attr, val));
        }
			} else if(xmlContent)
			{
                el.removeContent();
                //add the gml namespace if its missing
                if (val.contains("<gml:") && !val.contains("xmlns:gml=\"")) {
                    val = val.replaceFirst("<gml:([^ >]+)", "<gml:$1 xmlns:gml=\"http://www.opengis.net/gml\"");
                }
                el.addContent(Xml.loadString(val, false));
                Log.debug(Geonet.DATA_MANAGER, "replacing XML content");
            }
			else
			{
				List content = el.getContent();

				for(int i=0; i<content.size(); i++)
				{
					if (content.get(i) instanceof Text)
					{
						el.removeContent((Text) content.get(i));
						i--;
					}
				}
				el.addContent(val);
			}
		}
		//--- remove editing info added by previous call
		editLib.removeEditingInfo(md);

		return updateMetadata(session, dbms, id, md, validate, currVersion, lang);
	}

	//--------------------------------------------------------------------------

	public synchronized boolean updateMetadata(UserSession session, Dbms dbms, String id, Element md, boolean validate, String version, String lang) throws Exception
	{
		//--- check if the metadata has been modified from last time
		if (version != null && !editLib.getVersion(id).equals(version)) {
			Log.error(Geonet.DATA_MANAGER, "Version mismatch: had "+version+" but expected "+editLib.getVersion(id));
			return false;
		}

		editLib.contractElements(md);
		String schema = getMetadataSchema(dbms, id);
		md = updateFixedInfo(schema, id, md, dbms);

		//--- write metadata to dbms
		XmlSerializer.update(dbms, id, md);

		//--- update search criteria
		indexMetadata(dbms, id);

		//--- do the validation last - it throws exceptions
		if (validate) {
			doValidate(session,schema,id,md,lang);
		}
	
		return true;
	}

	//--------------------------------------------------------------------------
	//--- Used by the validate embedded service

	public void doValidate(UserSession session, String schema, String id, Element md, String lang) throws Exception
	{

		// XSD first... 
		Element xsdXPaths = Xml.validateInfo(editLib.getSchemaDir(schema) + Geonet.File.SCHEMA, md);
		if (xsdXPaths != null && xsdXPaths.getContent().size() > 0) {
			Element xsd = new Element("xsderrors");
			Element idElem = new Element("id");
			idElem.setText(id);
			xsd.addContent(idElem);
			throw new XSDValidationErrorEx("XSD validation errors detected", xsdXPaths);
		}

		// ...then schematrons
		Element schemaTronXml = schemaTron(getSchemaDir(schema),md,id,lang);
		if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
			Element schematron = new Element("schematronerrors");
			Element idElem = new Element("id");
			idElem.setText(id);
			schematron.addContent(idElem);
			throw new SchematronValidationErrorEx("Schematron errors detected - see schemaTron report for "+id+" in htmlCache for more details",schematron);
		}
	}

	//--------------------------------------------------------------------------
	//--- Used by the harvesting procedure

	public void updateMetadataExt(Dbms dbms, String id, Element md, String changeDate)
											throws Exception
	{
		XmlSerializer.update(dbms, id, md, changeDate);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Delete API
	//---
	//--------------------------------------------------------------------------

	/** Removes a metadata
	  */

	public synchronized void deleteMetadata(Dbms dbms, String id) throws Exception
	{
		//--- remove operations
		deleteMetadataOper(dbms, id, false);

		//--- remove categories
		deleteAllMetadataCateg(dbms, id);

		dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", new Integer(id));

		//--- remove metadata
		XmlSerializer.delete(dbms, "Metadata", id);

		//--- update search criteria
		searchMan.delete("_id", id+"");
	}

	//--------------------------------------------------------------------------
	/** Remove all operations stored for a metadata
	  */

	public void deleteMetadataOper(Dbms dbms, String id, boolean skipAllIntranet) throws Exception
	{
		String query = "DELETE FROM OperationAllowed WHERE metadataId="+id;

		if (skipAllIntranet)
			query += " AND groupId>1";

		dbms.execute(query);
	}

	//--------------------------------------------------------------------------
	/** Remove all categories stored for a metadata
	  */

	public void deleteAllMetadataCateg(Dbms dbms, String id) throws Exception
	{
		String query = "DELETE FROM MetadataCateg WHERE metadataId="+id;

		dbms.execute(query);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata thumbnail API
	//---
	//--------------------------------------------------------------------------

	public Element getThumbnails(Dbms dbms, String id) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		if (md == null)
			return null;

		md.detach();

		String schema = getMetadataSchema(dbms, id);

		//--- do an XSL  transformation

		String styleSheet = editLib.getSchemaDir(schema) + Geonet.File.EXTRACT_THUMBNAILS;

		Element result = Xml.transform(md, styleSheet);
		result.addContent(new Element("id").setText(id));

		return result;
	}

	//--------------------------------------------------------------------------

	public void setThumbnail(Dbms dbms, String id, boolean small, String file) throws Exception
	{
		int    pos = file.lastIndexOf(".");
		String ext = (pos == -1) ? "???" : file.substring(pos +1);

		Element env = new Element("env");
		env.addContent(new Element("file").setText(file));
		env.addContent(new Element("ext").setText(ext));

		manageThumbnail(dbms, id, small, env, Geonet.File.SET_THUMBNAIL);
	}

	//--------------------------------------------------------------------------

	public void unsetThumbnail(Dbms dbms, String id, boolean small) throws Exception
	{
		Element env = new Element("env");

		manageThumbnail(dbms, id, small, env, Geonet.File.UNSET_THUMBNAIL);
	}

	//--------------------------------------------------------------------------
	
	private void manageThumbnail(Dbms dbms, String id, boolean small, Element env,
										  String styleSheet) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		if (md == null)
			return;

		md.detach();

		String schema = getMetadataSchema(dbms, id);

		//-----------------------------------------------------------------------
		//--- remove thumbnail from metadata

		//--- setup environment

		String type = small ? "thumbnail" : "large_thumbnail";

		env.addContent(new Element("type").setText(type));

		transformMd(dbms,id,md,env,schema,styleSheet);
	}

	//--------------------------------------------------------------------------

	void transformMd(Dbms dbms, String id, Element md, Element env, String schema, String styleSheet) throws Exception {

		//--- setup root element

		Element root = new Element("root");
		root.addContent(md);
		root.addContent(env);

		//--- do an XSL  transformation

		styleSheet = getSchemaDir(schema) + styleSheet;

		md = Xml.transform(root, styleSheet);
		XmlSerializer.update(dbms, id, md);

		//--- update search criteria
		indexMetadata(dbms, id);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Privileges API
	//---
	//--------------------------------------------------------------------------

	/** Adds a permission to a group. Metadata is not reindexed
	  */

	public void setOperation(Dbms dbms, String mdId, String grpId, String opId) throws Exception
	{
		setOperation(dbms,new Integer(mdId),new Integer(grpId),new Integer(opId));
	}

	public void setOperation(Dbms dbms, int mdId, int grpId, int opId) throws Exception
	{
		String query = "SELECT metadataId FROM OperationAllowed " +
							"WHERE metadataId=? AND groupId=? AND operationId=?";

		Element elRes = dbms.select(query, mdId, grpId, opId);

		if (elRes.getChildren().size() == 0) {
			dbms.execute("INSERT INTO OperationAllowed(metadataId, groupId, operationId) VALUES(?,?,?)", mdId, grpId, opId);
		}
	}

	//--------------------------------------------------------------------------

	public void unsetOperation(Dbms dbms, String mdId, String grpId, String opId) throws Exception
	{
		unsetOperation(dbms,new Integer(mdId),new Integer(grpId),new Integer(opId));
	}

	public void unsetOperation(Dbms dbms, int mdId, int groupId, int operId) throws Exception
	{
		String query = "DELETE FROM OperationAllowed "+
							"WHERE metadataId=? AND groupId=? AND operationId=?";

		dbms.execute(query, mdId, groupId, operId);
	}

	//--------------------------------------------------------------------------

	public void copyDefaultPrivForGroup(Dbms dbms, String id, String groupId) throws Exception
	{
		//--- store access operations for group

		setOperation(dbms, id, groupId, AccessManager.OPER_VIEW);
		setOperation(dbms, id, groupId, AccessManager.OPER_NOTIFY);
		//
		// Restrictive: new and inserted records should not be editable, 
		// their resources can't be downloaded and any interactive maps can't be 
		// displayed by users in the same group 
		// setOperation(dbms, id, groupId, AccessManager.OPER_EDITING);
		// setOperation(dbms, id, groupId, AccessManager.OPER_DOWNLOAD);
		// setOperation(dbms, id, groupId, AccessManager.OPER_DYNAMIC);
		// Ultimately this should be configurable elsewhere
	}

	//--------------------------------------------------------------------------
	//---
	//--- Categories API
	//---
	//--------------------------------------------------------------------------

	/** Adds a category to a metadata. Metadata is not reindexed
	  */

	public void setCategory(Dbms dbms, String mdId, String categId) throws Exception
	{
		Object args[] = { new Integer(mdId), new Integer(categId) };

		if (!isCategorySet(dbms, mdId, categId))
			dbms.execute("INSERT INTO MetadataCateg(metadataId, categoryId) VALUES(?,?)", args);
	}

	//--------------------------------------------------------------------------

	public boolean isCategorySet(Dbms dbms, String mdId, String categId) throws Exception
	{
		String query = "SELECT metadataId FROM MetadataCateg " +"WHERE metadataId=? AND categoryId=?";

		Element elRes = dbms.select(query, new Integer(mdId), new Integer(categId));

		return (elRes.getChildren().size() != 0);
	}

	//--------------------------------------------------------------------------

	public void unsetCategory(Dbms dbms, String mdId, String categId) throws Exception
	{
		String query = "DELETE FROM MetadataCateg WHERE metadataId=? AND categoryId=?";

		dbms.execute(query, new Integer(mdId), new Integer(categId));
	}

	//--------------------------------------------------------------------------

	public Element getCategories(Dbms dbms, String mdId) throws Exception
	{
		String query = "SELECT id, name FROM Categories, MetadataCateg "+
							"WHERE id=categoryId AND metadataId=?";

		return dbms.select(query, new Integer(mdId));
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	/** Used for editing : swaps 2 elements
	  */

	private void swapElements(Element el1, Element el2) throws Exception
	{

		Element parent = el1.getParentElement();
		if (parent == null) {
			throw new IllegalArgumentException("No parent element for swapping");
		}

		int index1 = parent.indexOf(el1);
		if (index1 == -1) {
			throw new IllegalArgumentException("Element 1 not found for swapping");
		}
		int index2 = parent.indexOf(el2);
		if (index2 == -1) {
			throw new IllegalArgumentException("Element 2 not found for swapping");
		}

		Element el1Spare = (Element)el1.clone();
		
		parent.setContent(index1, (Element)el2.clone());
		parent.setContent(index2, el1Spare);
	}	

	//--------------------------------------------------------------------------

	private Element updateFixedInfo(String schema, String id, Element md, Dbms dbms) throws Exception
	{
		String query = "SELECT uuid, source, isTemplate FROM Metadata WHERE id = " + id;

		Element rec = dbms.select(query).getChild("record");
		String isTemplate = rec.getChildText("istemplate");

		// don't process templates
		if (isTemplate.equals("n"))
		{
			String uuid = rec.getChildText("uuid");
			return updateFixedInfoExisting(schema, id, md, uuid);
		}
		else return md;
	}

	//--------------------------------------------------------------------------

	public Element updateFixedInfoExisting(String schema, String id, Element md, String uuid) throws Exception
	{
		//--- setup environment - for new records

		Element env = new Element("env");

		env.addContent(new Element("id")        			.setText(id));
		env.addContent(new Element("uuid")      			.setText(uuid));
		env.addContent(new Element("updateDateStamp")	.setText("no"));
		return updateFixedInfo(schema, md, env);
	}


	//--------------------------------------------------------------------------

	public Element updateFixedInfoNew(String schema, String id, Element md, String uuid) throws Exception
	{
		//--- setup environment - for new records

		Element env = new Element("env");

		env.addContent(new Element("id")        			.setText(id));
		env.addContent(new Element("uuid")      			.setText(uuid));
		env.addContent(new Element("updateDateStamp")	.setText("yes"));
		return updateFixedInfo(schema, md, env);
	}

	//--------------------------------------------------------------------------

	private Element updateFixedInfo(String schema, Element md, Element env) throws Exception
	{

		//--- environment common to both existing and new records goes here
		
		env.addContent(new Element("changeDate").setText(new ISODate().toString()));
		env.addContent(new Element("siteURL")   .setText(getSiteURL()));

		//--- setup root element

		Element root = new Element("root");
		root.addContent(md);
		root.addContent(env);

		//--- do the XSL transformation using update-fixed-info.xsl

		String styleSheet = editLib.getSchemaDir(schema) + Geonet.File.UPDATE_FIXED_INFO;

		return Xml.transform(root, styleSheet);
	}

	//--------------------------------------------------------------------------

	private Element buildInfoElem(ServiceContext context, String id, String version) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query ="SELECT schemaId, createDate, changeDate, source, isTemplate, title, "+
									"uuid, isHarvested, harvestUuid, popularity, rating, owner FROM Metadata WHERE id = " + id;

		// add Metadata table infos: schemaId, createDate, changeDate, source,
		Element rec = dbms.select(query).getChild("record");

		String  schema     = rec.getChildText("schemaid");
		String  createDate = rec.getChildText("createdate");
		String  changeDate = rec.getChildText("changedate");
		String  source     = rec.getChildText("source");
		String  isTemplate = rec.getChildText("istemplate");
		String  title      = rec.getChildText("title");
		String  uuid       = rec.getChildText("uuid");
		String  isHarvested= rec.getChildText("isharvested");
		String  harvestUuid= rec.getChildText("harvestuuid");
		String  popularity = rec.getChildText("popularity");
		String  rating     = rec.getChildText("rating");
		String  owner      = rec.getChildText("owner");

		Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);

		addElement(info, Edit.Info.Elem.ID,          id);
		addElement(info, Edit.Info.Elem.SCHEMA,      schema);
		addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
		addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
		addElement(info, Edit.Info.Elem.IS_TEMPLATE, isTemplate);
		addElement(info, Edit.Info.Elem.TITLE,       title);
		addElement(info, Edit.Info.Elem.SOURCE,      source);
		addElement(info, Edit.Info.Elem.UUID,        uuid);
		addElement(info, Edit.Info.Elem.IS_HARVESTED,isHarvested);
		addElement(info, Edit.Info.Elem.POPULARITY,  popularity);
		addElement(info, Edit.Info.Elem.RATING,      rating);

		if (isHarvested.equals("y"))
			info.addContent(harvestMan.getHarvestInfo(harvestUuid, id, uuid));

		if (version != null)
			addElement(info, Edit.Info.Elem.VERSION, version);

		// add operations
		Element operations = accessMan.getAllOperations(context, id, context.getIpAddress());
		HashSet hsOper = accessMan.getOperations(context, id, context.getIpAddress(), operations);

		addElement(info, Edit.Info.Elem.VIEW,     			String.valueOf(hsOper.contains(AccessManager.OPER_VIEW)));
		addElement(info, Edit.Info.Elem.NOTIFY,   			String.valueOf(hsOper.contains(AccessManager.OPER_NOTIFY)));
		addElement(info, Edit.Info.Elem.DOWNLOAD, 			String.valueOf(hsOper.contains(AccessManager.OPER_DOWNLOAD)));
		addElement(info, Edit.Info.Elem.DYNAMIC,  			String.valueOf(hsOper.contains(AccessManager.OPER_DYNAMIC)));
		addElement(info, Edit.Info.Elem.FEATURED, 			String.valueOf(hsOper.contains(AccessManager.OPER_FEATURED)));


		if (!hsOper.contains(AccessManager.OPER_DOWNLOAD)) {
			boolean gDownload = Xml.selectNodes(operations, "guestoperations/record[operationid="+AccessManager.OPER_DOWNLOAD+" and groupid='-1']").size() == 1;
			addElement(info, Edit.Info.Elem.GUEST_DOWNLOAD, gDownload+"");
		}

		if (accessMan.canEdit(context, id))
			addElement(info, Edit.Info.Elem.EDIT, "true");

		if (accessMan.isOwner(context, id)) {
			addElement(info, Edit.Info.Elem.OWNER, "true");
		}

		// add owner name
		query = "SELECT username FROM Users WHERE id = " + owner;
		Element record = dbms.select(query).getChild("record");
		if (record != null) {
			String ownerName = record.getChildText("username");
			addElement(info, Edit.Info.Elem.OWNERNAME, ownerName);
		}

		// add categories
		List categories = dbms.select("SELECT id, name FROM MetadataCateg, Categories "+
												"WHERE metadataId = " + id + " AND categoryId = id ORDER BY id").getChildren();

		for (Iterator iter = categories.iterator(); iter.hasNext(); )
		{
			Element category = (Element)iter.next();
			addElement(info, Edit.Info.Elem.CATEGORY, category.getChildText("name"));
		}

		// add subtemplates
		List subList = getSubtemplates(dbms, schema);
		if (subList != null) {
			Element subs = new Element(Edit.Info.Elem.SUBTEMPLATES);
			subs.addContent(subList);
			info.addContent(subs);
		}
		return info;
	}

	//--------------------------------------------------------------------------
	/** Get all relevant subtemplates and return them as an xml fragment
	 */

	private List getSubtemplates(Dbms dbms, String schema) throws Exception
	{
		ArrayList alSubs = new ArrayList();

		String query ="SELECT title, id FROM Metadata WHERE schemaId = '" + schema + "' AND isTemplate = 's'";
		List subtemplates = dbms.select(query).getChildren();
		for (Iterator iter = subtemplates.iterator(); iter.hasNext(); ) {
			Element sub = (Element)iter.next();
			alSubs.add(sub.clone());	
		}

		return alSubs;

	}

	//--------------------------------------------------------------------------

	private static void addElement(Element root, String name, String value)
	{
		root.addContent(new Element(name).setText(value));
	}

	//--------------------------------------------------------------------------

	public String getSiteID()
	{
		return settingMan.getValue("system/site/siteId");
	}

	
	//---------------------------------------------------------------------------
	//---
	//--- Static methods - GAST is the only thing that should use these
	//---
	//---------------------------------------------------------------------------

	public static void setNamespacePrefix(Element md)
	{
		//--- if the metadata has no namespace or already has a namespace then
		//--- we must skip this phase

		Namespace ns = md.getNamespace();
    if (ns == Namespace.NO_NAMESPACE || (!md.getNamespacePrefix().equals("")))
      return;


		//--- set prefix for iso19139 metadata

		ns = Namespace.getNamespace("gmd", md.getNamespace().getURI());
		setNamespacePrefix(md, ns);
	}

	//---------------------------------------------------------------------------

	private static void setNamespacePrefix(Element md, Namespace ns)
	{
		if (md.getNamespaceURI().equals(ns.getURI()))
			md.setNamespace(ns);

		for (Object o : md.getChildren())
			setNamespacePrefix((Element) o, ns);
	}

	//--------------------------------------------------------------------------

	private void setNamespacePrefixUsingSchemas(Element md) throws Exception
	{
		//--- if the metadata has no namespace or already has a namespace prefix
		//--- then we must skip this phase

		Namespace ns = md.getNamespace();
    if (ns == Namespace.NO_NAMESPACE)  
      return;

		MetadataSchema mds = findSchema(md, ns);
		//--- get the namespaces and add prefixes to any that are
		//--- default ie. prefix is ''
		
		ArrayList nsList = new ArrayList();
		nsList.add(ns);
		nsList.addAll(md.getAdditionalNamespaces());
		for (int i = 0; i < nsList.size(); i++) {
			Namespace aNs = (Namespace)nsList.get(i);
			if (aNs.getPrefix().equals("")) { // found default namespace
				String prefix = mds.getPrefix(aNs.getURI());
				if (prefix == null) {
					throw new IllegalArgumentException("No prefix - cannot find a namespace to set for element "+md.getQualifiedName()+" - namespace URI "+ns.getURI());
				}
				ns = Namespace.getNamespace(prefix, aNs.getURI());
				setNamespacePrefix(md, ns);
				if (!md.getNamespace().equals(ns)) {
					md.removeNamespaceDeclaration(aNs);
					md.addNamespaceDeclaration(ns);
				}
			}
		}
		return;
	}

	//--------------------------------------------------------------------------
	
	private MetadataSchema findSchema(Element md, Namespace ns) throws Exception
	{
		String nsUri = ns.getURI();
		for (String schema : getSchemas()) {
			MetadataSchema mds = getSchema(schema);
			String nsSchema = mds.getPrimeNS();
			if (nsSchema != null && nsUri.equals(nsSchema)) {
				Log.debug(Geonet.DATA_MANAGER, "Found schema "+schema+" with NSURI "+nsSchema);
				return mds;
			}
		}

		throw new IllegalArgumentException("Cannot find a namespace to set for element "+md.getQualifiedName()+" with namespace URI "+nsUri);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String baseURL;

	private EditLib editLib = new EditLib(this);

	private AccessManager  accessMan;
	private SearchManager  searchMan;
	private SettingManager settingMan;
	private HarvestManager harvestMan;
	private String htmlCacheDir;
}

//=============================================================================

