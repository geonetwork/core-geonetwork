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
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;
import java.sql.SQLException;

import jeeves.constants.Jeeves;
import jeeves.exceptions.JeevesException;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;
import jeeves.utils.Xml;
import jeeves.utils.Xml.ErrorHandler;
import jeeves.xlink.Processor;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.csw.domain.CswCapabilitiesInfo;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.ISODate;
import org.jdom.Attribute;
import org.jdom.Document;
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

	public DataManager(ServiceContext context, SchemaManager scm, SearchManager sm, AccessManager am, Dbms dbms, SettingManager ss, String baseURL, String htmlCacheDir, String dataDir, String appPath) throws Exception
	{
		searchMan = sm;
		accessMan = am;
		settingMan= ss;
		schemaMan = scm;
		editLib = new EditLib(schemaMan);
    servContext=context;

		this.baseURL = baseURL;
        this.dataDir = dataDir;
		this.appPath = appPath;

		stylePath = context.getAppPath() + FS + Geonet.Path.STYLESHEETS + FS;


		XmlSerializer.setSettingManager(ss);

		init(context, dbms, false);
	}

    private synchronized void finishRebuilding() { rebuilding = false; }

	/**
	 * Init Data manager and refresh index if needed. 
	 * Can also be called after GeoNetwork startup in order to rebuild the lucene 
	 * index
	 * 
	 * @param context
	 * @param dbms
	 * @param force         Force reindexing all from scratch
	 *
	 **/
	public synchronized void init(ServiceContext context, Dbms dbms, Boolean force) throws Exception {

		if (rebuilding) throw new OperationNotAllowedEx("Index rebuilding already in progress");

		// get all metadata from DB
		Element result = dbms.select("SELECT id, changeDate FROM Metadata ORDER BY id ASC");
		
		Log.debug(Geonet.DATA_MANAGER, "DB CONTENT:\n'"+ Xml.getString(result) +"'"); 

		// get lastchangedate of all metadata in index
		HashMap<String,String> docs = searchMan.getDocsChangeDate();

		// set up results HashMap for post processing of records to be indexed
		ArrayList<Integer> toIndex = new ArrayList<Integer>();

		Log.debug(Geonet.DATA_MANAGER, "INDEX CONTENT:");

		// index all metadata in DBMS if needed
		for(int i = 0; i < result.getContentSize(); i++) {
			// get metadata
			Element record = (Element) result.getContent(i);
			String  id     = record.getChildText("id");
			int iId = Integer.parseInt(id);
	
			Log.debug(Geonet.DATA_MANAGER, "- record ("+ id +")");

			String idxLastChange = docs.get(id);

			// if metadata is not indexed index it
			if (idxLastChange == null) {
				Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
				toIndex.add(iId);
	
			// else, if indexed version is not the latest index it
			} else {
				docs.remove(id);
	
				String lastChange    = record.getChildText("changedate");
	
       	Log.debug(Geonet.DATA_MANAGER, "- lastChange: " + lastChange); 
       	Log.debug(Geonet.DATA_MANAGER, "- idxLastChange: " + idxLastChange); 
	
				// date in index contains 't', date in DBMS contains 'T'
				if (force || !idxLastChange.equalsIgnoreCase(lastChange)) {
					Log.debug(Geonet.DATA_MANAGER, "-  will be indexed");
					toIndex.add(iId);
				}
			}
		}

		// if anything to index then schedule it to be done after servlet is
		// up so that any links to local fragments are resolvable
		if ( toIndex.size() > 0 ) {
			Timer t = new Timer();
			t.schedule(new IndexMetadataTask(context, toIndex), 10);
		}

		if (docs.size() > 0) { // anything left?
			Log.debug(Geonet.DATA_MANAGER, "INDEX HAS RECORDS THAT ARE NOT IN DB:"); 
		}

		// remove from index metadata not in DBMS
		for ( String id : docs.keySet() )
		{
			searchMan.delete("_id", id);

      Log.debug(Geonet.DATA_MANAGER, "- removed record (" + id + ") from index");
		}
	}

    //--------------------------------------------------------------------------

	public synchronized void rebuildIndexXLinkedMetadata(ServiceContext context) throws Exception {
		
		if (rebuilding) throw new OperationNotAllowedEx("Index rebuilding already in progress");

		// get all metadata with XLinks
		ArrayList<Integer> toIndex = searchMan.getDocsWithXLinks();

		Log.debug(Geonet.DATA_MANAGER, "Will index "+toIndex.size()+" records with XLinks");
		if ( toIndex.size() > 0 ) {
			// clean XLink Cache so that cache and index remain in sync
			Processor.clearCache();	

			// execute indexing operation
			Timer t = new Timer();
			t.schedule(new IndexMetadataTask(context, toIndex), 10);
		}
	}

	//--------------------------------------------------------------------------

	class IndexMetadataTask extends TimerTask {
		ServiceContext context;
		ArrayList<Integer> toIndex;

        IndexMetadataTask(ServiceContext context, ArrayList<Integer> toIndex) {
			this.context = context;
			this.toIndex = toIndex;
		}

		public void run() {

			try {

				// poll context to see whether servlet is up yet
				while (!context.isServletInitialized()) {
					Log.debug(Geonet.DATA_MANAGER, "Waiting for servlet to finish initializing.."); 
					Thread.sleep(10000); // sleep 10 seconds
				}

				// servlet up so safe to index all metadata that needs indexing

				Dbms dbms = (Dbms) context.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
				startIndexGroup();
				try {
					for ( Integer id : toIndex ) {
						indexMetadataGroup(dbms, id.toString());
					}
				} finally {
					endIndexGroup();
				}

				//-- commit Dbms resource (which makes it available to pool again) 
				//-- to avoid exhausting Dbms pool
				context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				finishRebuilding();
			}
		}
	}

	//--------------------------------------------------------------------------

	public void indexMetadata(Dbms dbms, String id) throws Exception
	{

	    Log.debug(Geonet.DATA_MANAGER, "Indexing record (" + id + ")"); //DEBUG

	    indexMetadata(dbms, id, searchMan, schemaMan, false);
	}

	//--------------------------------------------------------------------------

	public void startIndexGroup() throws Exception {
		searchMan.startIndexGroup();
	}

	//--------------------------------------------------------------------------

	public void endIndexGroup() throws Exception {
		searchMan.endIndexGroup();
	}

	//--------------------------------------------------------------------------

	public void indexMetadataGroup(Dbms dbms, String id) throws Exception {
		Log.debug(Geonet.DATA_MANAGER, "Indexing record (" + id + ")"); //DEBUG
		indexMetadata(dbms, id, searchMan, schemaMan, true);
	}

	//--------------------------------------------------------------------------

	public static void indexMetadata(Dbms dbms, String id, SearchManager sm, SchemaManager scm) throws Exception
	{
		indexMetadata(dbms, id, sm, scm, false);
	}

	//--------------------------------------------------------------------------

	public static void indexMetadata(Dbms dbms, String id, SearchManager sm, SchemaManager scm, boolean indexGroup) throws Exception
	{
		try
		{
			indexMetadataI(dbms, id, sm, scm, indexGroup);
		}
		catch (Exception e)
		{
			Log.error(Geonet.DATA_MANAGER, "The metadata document index with id="+id+" is corrupt/invalid - ignoring it. Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------

	private static void indexMetadataI(Dbms dbms, String id, SearchManager sm, SchemaManager schemaMan, boolean indexGroup) throws Exception
	{
		Vector<Element> moreFields = new Vector<Element>();

		// get metadata, extracting and indexing any xlinks
		Element md   = XmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id);
		if (XmlSerializer.resolveXLinks()) {
			List<Attribute> xlinks = Processor.getXLinks(md);
			if (xlinks.size() > 0) {
				moreFields.add(makeField("_hasxlinks", "1", true, true, false));
				StringBuilder sb = new StringBuilder();
				for (Attribute xlink : xlinks) {
					sb.append(xlink.getValue()); sb.append(" ");
				}
				moreFields.add(makeField("_xlink", sb.toString(), true, true, false));
				Processor.detachXLink(md);
			} else {
				moreFields.add(makeField("_hasxlinks", "0", true, true, false));
			}
		} else {
			moreFields.add(makeField("_hasxlinks", "0", true, true, false));	
		}

		// get metadata table fields
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
		moreFields.add(makeField("_uuid",        uuid,        true, true, true));
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

        for (Object operation1 : operations) {
            Element operation = (Element) operation1;
            String groupId = operation.getChildText("groupid");
            String operationId = operation.getChildText("operationid");

            moreFields.add(makeField("_op" + operationId, groupId, true, true, false));
        }
		// get categories
		List categories = dbms.select("SELECT id, name FROM MetadataCateg, Categories "+
												"WHERE metadataId = " + id + " AND categoryId = id ORDER BY id").getChildren();

        for (Object category1 : categories) {
            Element category = (Element) category1;
            String categoryName = category.getChildText("name");

            moreFields.add(makeField("_cat", categoryName, true, true, false));
        }

        // getValidationInfo
        // -1 : not evaluated
        // 0 : invalid
        // 1 : valid
        List<Element> validationInfo = dbms.select("SELECT valType, status FROM Validation WHERE metadataId=?", new Integer(id)).getChildren();
        if (validationInfo.size() == 0) {
            moreFields.add(makeField("_valid", "-1", true, true, false));
        } else {
            String isValid = "1";
            for (Object elem : validationInfo) {
                Element vi = (Element) elem;
                String type = vi.getChildText("valtype");
                String status = vi.getChildText("status");
                if ("0".equals(status)) {
                    isValid = "0";
                }
                moreFields.add(makeField("_valid_" + type, status, true, true, false));
            }
            moreFields.add(makeField("_valid", isValid, true, true, false));
        }
		if (indexGroup) {
			sm.indexGroup(schemaMan.getSchemaDir(schema), md, id, moreFields, isTemplate, title);
		} else {
			sm.index(schemaMan.getSchemaDir(schema), md, id, moreFields, isTemplate, title);
		}
	}

	//--------------------------------------------------------------------------

	public void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception {
		searchMan.rescheduleOptimizer(beginAt, interval);
	}

	//--------------------------------------------------------------------------

	public void disableOptimizer() throws Exception {
		searchMan.disableOptimizer();
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

	public MetadataSchema getSchema(String name)
	{
		return schemaMan.getSchema(name);
	}

	//--------------------------------------------------------------------------

	public Set<String> getSchemas()
	{
		return schemaMan.getSchemas();
	}

	//--------------------------------------------------------------------------

	public boolean existsSchema(String name)
	{
		return schemaMan.existsSchema(name);
	}

	//--------------------------------------------------------------------------

	public String getSchemaDir(String name)
	{
		return schemaMan.getSchemaDir(name);
	}

	//--------------------------------------------------------------------------
	// Use this validate method for XML documents with dtd
	public void validate(String schema, Document doc) throws Exception
	{
		Xml.validate(doc);	
	}

	//--------------------------------------------------------------------------
	// Use this validate method for XML documents with xsd validation
	public void validate(String schema, Element md) throws Exception
	{
		String schemaLoc = md.getAttributeValue("schemaLocation", Geonet.XSI_NAMESPACE);
		Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of "+schemaLoc);
		if (schemaLoc == null) schemaLoc = "";

		if (schema == null) {
			// must use schemaLocation 
			Xml.validate(md);
		} else {
			// if schemaLocation use that
			if (!schemaLoc.equals("")) { 
				Xml.validate(md);
			// otherwise use supplied schema name 
			} else {
				Xml.validate(getSchemaDir(schema) + Geonet.File.SCHEMA, md);
			}
		}
	}

	//--------------------------------------------------------------------------

	public Element validateInfo(String schema, Element md, ErrorHandler eh) throws Exception
	{
		String schemaLoc = md.getAttributeValue("schemaLocation", Geonet.XSI_NAMESPACE);
		Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of "+schemaLoc);
		if (schemaLoc == null) schemaLoc = "";

		if (schema == null) {
			// must use schemaLocation 
			return Xml.validateInfo(md, eh);
		} else {
			// if schemaLocation use that
			if (!schemaLoc.equals("")) { 
				return Xml.validateInfo(md, eh);
			// otherwise use supplied schema name 
			} else {
				return Xml.validateInfo(getSchemaDir(schema) + Geonet.File.SCHEMA, md, eh);
			}
		}
	}
	/** 
	* Create XML schematron report. 
	*/ 
	public Element doSchemaTronForEditor(String schema,Element md,String lang) throws Exception { 
    	// enumerate the metadata xml so that we can report any problems found  
    	// by the schematron_xml script to the geonetwork editor 
    	editLib.enumerateTree(md); 
    	
    	// get an xml version of the schematron errors and return for error display 
    	Element schemaTronXmlReport = getSchemaTronXmlReport(schema, md, lang, null); 
    	
    	// remove editing info added by enumerateTree 
    	editLib.removeEditingInfo(md); 
    	
    	return schemaTronXmlReport; 
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

	public Element enumerateTree(Element md) throws Exception {
		editLib.enumerateTree(md);
		return md;
	}


	//--------------------------------------------------------------------------
	/**
	 * Create XML schematron report for each set of rules defined
	 * in schema directory.
	 * @param valTypeAndStatus 
	 */
	private Element getSchemaTronXmlReport(String schema, Element md, String lang, HashMap<String, Integer[]> valTypeAndStatus) throws Exception {
		// NOTE: this method assumes that you've run enumerateTree on the 
		// metadata

		MetadataSchema metadataSchema = getSchema(schema);
		String[] rules = metadataSchema.getSchematronRules();
		
		// Schematron report is composed of one or more report(s)
		// for each set of rules.
		Element schemaTronXmlOut = new Element("schematronerrors",
				Edit.NAMESPACE);

		for (String rule : rules) {
			// -- create a report for current rules.
			// Identified by a rule attribute set to shematron file name
			Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule);
			String ruleId = rule.substring(0, rule.indexOf(".xsl"));
			Element report = new Element("report", Edit.NAMESPACE);
			report.setAttribute("rule", ruleId,
					Edit.NAMESPACE);

			String schemaTronXmlXslt = metadataSchema.getSchemaDir() + File.separator
					+ rule;
			try {
				Map<String,String> params = new HashMap<String,String>();
				params.put("lang", lang);
				params.put("rule", rule);
				params.put("dataDir", this.dataDir);
				Element xmlReport = Xml.transform(md, schemaTronXmlXslt, params);
				if (xmlReport != null) {
					report.addContent(xmlReport);
				}
				// add results to persitent validation information
				int firedRules = 0;
				Iterator<Element> i = xmlReport.getDescendants(new ElementFilter ("fired-rule", Namespace.getNamespace("http://purl.oclc.org/dsdl/svrl")));
				while (i.hasNext()) {
                    i.next();
                    firedRules ++;
                }
				int invalidRules = 0;
                i = xmlReport.getDescendants(new ElementFilter ("failed-assert", Namespace.getNamespace("http://purl.oclc.org/dsdl/svrl")));
                while (i.hasNext()) {
                    i.next();
                    invalidRules ++;
                }
				Integer[] results = {invalidRules!=0?0:1, firedRules, invalidRules};
				if (valTypeAndStatus != null) {
				    valTypeAndStatus.put(ruleId, results);
				}
			} catch (Exception e) {
				Log.error(Geonet.DATA_MANAGER,"WARNING: schematron xslt "+schemaTronXmlXslt+" failed");
				e.printStackTrace();
			}

			// -- append report to main XML report.
			schemaTronXmlOut.addContent(report);
		}

		return schemaTronXmlOut;
	}

	//--------------------------------------------------------------------------
	/**
	 * Valid the metadata record against its schema.
	 * For each error found, an xsderror attribute is added to
	 * the corresponding element trying to find the element
	 * based on the xpath return by the ErrorHandler.
	 * 
	 */
	private synchronized Element getXSDXmlReport(String schema, Element md) throws Exception {

		// NOTE: this method assumes that enumerateTree has NOT been run on the
		// metadata
		ErrorHandler errorHandler = new ErrorHandler();
		errorHandler.setNs(Edit.NAMESPACE);
		Element xsdErrors;
		
		try {
		    xsdErrors = validateInfo(schema,
				md, errorHandler);
		}catch (Exception e) {
		    xsdErrors = JeevesException.toElement(e);
		    return xsdErrors;
        }
		
		if (xsdErrors != null) {
			MetadataSchema mds = getSchema(schema);
			List<Namespace> schemaNamespaces = mds.getSchemaNS();
		
			//-- now get each xpath and evaluate it
			//-- xsderrors/xsderror/{message,xpath} 
			List list = xsdErrors.getChildren();
			for (Object o : list) {
				Element elError = (Element) o;
				String xpath = elError.getChildText("xpath", Edit.NAMESPACE);
				String message = elError.getChildText("message", Edit.NAMESPACE);
				message = "\\n" + message;

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
		}
		return xsdErrors;
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
		String styleSheet = getSchemaDir(schema) + Geonet.File.EXTRACT_UUID;
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

		String styleSheet = getSchemaDir(schema) + Geonet.File.SET_UUID;

		return Xml.transform(root, styleSheet);
	}

	@SuppressWarnings("unchecked")
	public List<Element> getMetadataByHarvestingSource(Dbms dbms, String harvestingSource) throws Exception {
		String query = "SELECT id FROM Metadata WHERE harvestUuid=?";
		return dbms.select(query, harvestingSource).getChildren();
	}

	//--------------------------------------------------------------------------

	public Element extractSummary(Element md) throws Exception
	{
		String styleSheet = stylePath + Geonet.File.METADATA_BRIEF;
		Element summary       = Xml.transform(md, styleSheet);

		Log.debug(Geonet.DATA_MANAGER, "Extracted summary '\n"+Xml.getString(summary));

		//--- needed to detach md from the document
		md.detach();

		return summary;
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

	public String getMetadataTemplate(Dbms dbms, String id) throws Exception
	{
		String query = "SELECT istemplate FROM Metadata WHERE id=?";

		List list = dbms.select(query, new Integer(id)).getChildren();

		if (list.size() == 0)
			return null;

		Element record = (Element) list.get(0);

		return record.getChildText("istemplate");
	}

	//--------------------------------------------------------------------------

	public MdInfo getMetadataInfo(Dbms dbms, String id) throws Exception
	{
		String query = "SELECT id, uuid, schemaId, isTemplate, isHarvested, createDate, "+
							"       changeDate, source, title, root, owner, groupOwner, displayOrder "+
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
                info.displayOrder  = record.getChildText("displayOrder");

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
		setTemplateExt(dbms, id, isTemplate, title);
		indexMetadata(dbms, Integer.toString(id));
	}

	//--------------------------------------------------------------------------

	public void setTemplateExt(Dbms dbms, int id, String isTemplate, String title) throws Exception
	{
		if (title == null) dbms.execute("UPDATE Metadata SET isTemplate=? WHERE id=?", isTemplate, id);
		else               dbms.execute("UPDATE Metadata SET isTemplate=?, title=? WHERE id=?", isTemplate, title, id);
	}

	//--------------------------------------------------------------------------

	public void setHarvested(Dbms dbms, int id, String harvestUuid) throws Exception
	{
		setHarvestedExt(dbms, id, harvestUuid);
		indexMetadata(dbms, Integer.toString(id));
	}

	//--------------------------------------------------------------------------

	public void setHarvestedExt(Dbms dbms, int id, String harvestUuid) throws Exception
	{
		String value = (harvestUuid != null) ? "y" : "n";
		if (harvestUuid == null) {
			dbms.execute("UPDATE Metadata SET isHarvested=? WHERE id=?", value,id );
		} else {
			dbms.execute("UPDATE Metadata SET isHarvested=?, harvestUuid=? WHERE id=?", value, harvestUuid, id);
		}
	}

	//--------------------------------------------------------------------------

	public void setHarvestedExt(Dbms dbms, int id, String harvestUuid, String harvestUri) throws Exception
	{
		String value = (harvestUuid != null) ? "y" : "n";
		String query = "UPDATE Metadata SET isHarvested=?, harvestUuid=?, harvestUri=? WHERE id=?";

		dbms.execute(query, value, harvestUuid, harvestUri, id);
	}

	//---------------------------------------------------------------------------

	public String getSiteURL()
	{
		String host    = settingMan.getValue("system/server/host");
		String port    = settingMan.getValue("system/server/port");
		String locServ = baseURL +"/"+ Jeeves.Prefix.SERVICE +"/en";

		return "http://" + host + (port.equals("80") ? "" : ":" + port) + locServ;
	}

	//--------------------------------------------------------------------------

	public String autodetectSchema(Document doc)
	{
		return autodetectSchema(doc.getRootElement());
	}

	//--------------------------------------------------------------------------
		
	public String autodetectSchema(Element md)
	{
		
		Log.debug(Geonet.DATA_MANAGER, "Autodetect schema for metadata with :\n * root element:'" + md.getQualifiedName()
				 + "'\n * with namespace:'" + md.getNamespace()
				 + "\n * with additional namespaces:" + md.getAdditionalNamespaces().toString());
		String schema =  schemaMan.autodetectSchema(md);
		Log.debug(Geonet.DATA_MANAGER, "Schema detected was "+schema);
		return schema;
	}
		
  //--------------------------------------------------------------------------
  public void updateDisplayOrder(Dbms dbms, String id, String displayOrder) throws Exception {
    String query = "UPDATE Metadata SET displayOrder = ? WHERE id = ?";
    dbms.execute(query, new Integer(displayOrder), new  Integer(id));
  }

	//--------------------------------------------------------------------------

	public void increasePopularity(ServiceContext srvContext, String id) throws Exception
	{
		GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
		gc.getThreadPool().runTask(new IncreasePopularityTask(srvContext, id));
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
										  SerialFactory sf, String source, int owner,
										  String parentUuid) throws Exception
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

		Element xml = updateFixedInfoNew(schema, Integer.toString(serial), Xml.loadString(data, false), uuid, parentUuid);

		//--- store metadata

		String id = XmlSerializer.insert(dbms, schema, xml, serial, source, uuid, owner, groupOwner);

		copyDefaultPrivForGroup(dbms, id, groupOwner);

		//--- store metadata categories copying them from the template

		List categList = dbms.select("SELECT categoryId FROM MetadataCateg WHERE metadataId = "+templateId).getChildren();

        for (Object aCategList : categList) {
            Element elRec = (Element) aCategList;

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

		return insertMetadataExt(dbms, schema, md, id, source, createDate, 
											changeDate, uuid, owner, groupOwner, "", "n");
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

		return insertMetadataExt(dbms, schema, md, id, source, createDate, 
											changeDate, uuid, owner, groupOwner, "", isTemplate);
	}

	//--------------------------------------------------------------------------
	/** @param source the source of the metadata. If null, the local siteId will be used
	  */

	public String insertMetadataExt(Dbms dbms, String schema, Element md, int id,
											  String source, String createDate, String changeDate,
											  String uuid, int owner, String groupOwner, 
												String docType, String isTemplate) throws Exception
	{
		if (source == null)
			source = getSiteID();

		//--- force namespace prefix for iso19139 metadata
		setNamespacePrefixUsingSchemas(md);

		//--- Note: we cannot index metadata here. Indexing is done in the harvesting part

	    String record = XmlSerializer.insert(dbms, schema, md, id, source, uuid, createDate,
										 changeDate, isTemplate, null, owner, groupOwner, docType);

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, md, id + "");

        return record;
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

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, xml, id);

		return id;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Metadata Get API
	//---
	//--------------------------------------------------------------------------

	/** Retrieves a metadata (in xml) given its id with no geonet:info
	 */
	public Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception
	{
		Element md = getMetadata(srvContext, id, false, false);
		md.removeChild(Edit.RootChild.INFO, Edit.NAMESPACE);
		return md;
	}

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

		boolean doXLinks = XmlSerializer.resolveXLinks();
			
		Element md = XmlSerializer.selectNoXLinkResolver(dbms, "Metadata", id);
		if (md == null) return null;

		String version = null;

		if (forEditing) { // copy in xlink'd fragments but leave xlink atts to editor
			if (doXLinks) Processor.processXLink(md); 
			String schema = getMetadataSchema(dbms, id);
			
			if (withEditorValidationErrors) {
			    version = doValidate(srvContext.getUserSession(), dbms, schema, id, md, srvContext.getLanguage(), forEditing).two();
			} else {
                editLib.expandElements(schema, md);
                version = editLib.getVersionForEditing(schema, id, md);
            }
		} else {
			if (doXLinks) Processor.detachXLink(md);
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
	public boolean existsMetadata(Dbms dbms, int id) throws Exception
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

        for (Object keyword : keywords) {
            el.addContent(new Element("keyword").setText((String) keyword));
        }

		return el;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Embedded Metadata Update API for AJAX Editor support
	//---
	//--------------------------------------------------------------------------

	private Element getMetadataFromSession(UserSession session, String id)
	{
		Log.debug(Geonet.DATA_MANAGER, "Retrieving metadata from session "+session.getUserId());
		Element md = (Element) session.getProperty(Geonet.Session.METADATA_EDITING + id);
		md.detach();
		return md;
	}

	private void setMetadataIntoSession(UserSession session, Element md, String id)
	{
		Log.debug(Geonet.DATA_MANAGER, "Storing metadata in session "+session.getUserId());
		session.setProperty(Geonet.Session.METADATA_EDITING + id, md);
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : removes metadata from session
	  */
	public void removeMetadataEmbedded(UserSession session, String id)
	{
		Log.debug(Geonet.DATA_MANAGER, "Removing metadata from session "+session.getUserId());
		session.removeProperty(Geonet.Session.METADATA_EDITING + id);
		session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);
	}
	
	//--------------------------------------------------------------------------
	/** For Ajax Editing : gets Metadata from database and places it in session
	  */
	public Element getMetadataEmbedded(ServiceContext srvContext, String id, boolean forEditing, boolean withValidationErrors) throws Exception
	{
		Element md = getMetadata(srvContext, id, forEditing, withValidationErrors);

		UserSession session = srvContext.getUserSession();
		setMetadataIntoSession(session, md, id);
		return md;
	}

	//--------------------------------------------------------------------------
	/** For Ajax Editing : adds an element or an attribute to a metadata element ([add] link)
	  */
	public synchronized Element addElementEmbedded(Dbms dbms, UserSession session, String id, String ref, String name, String childName)  throws Exception
	{

		String  schema = getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- ref is parent element so find it
		Element el = editLib.findElement(md, ref);
		if (el == null)
			throw new IllegalStateException("Element not found at ref = " + ref);

		//--- locate the geonet:element and geonet:info elements and clone for 
		//--- later re-use
		Element refEl = (Element)(el.getChild(Edit.RootChild.ELEMENT, Edit.NAMESPACE)).clone();
		Element info = (Element)(md.getChild(Edit.RootChild.INFO,Edit.NAMESPACE)).clone();
		md.removeChild(Edit.RootChild.INFO,Edit.NAMESPACE);
		
		Element child = null;
		MetadataSchema mds = getSchema(schema);
		if (childName != null) {
			if (childName.equals("geonet:attribute")) {
				 String defaultValue = "";
				 List attributeDefs = el.getChildren(Edit.RootChild.ATTRIBUTE, Edit.NAMESPACE);
				 for (Object a : attributeDefs) {
					 Element attributeDef = (Element) a;
					 if (attributeDef != null && attributeDef.getAttributeValue(Edit.Attribute.Attr.NAME).equals(name)) {
						 Element defaultChild = attributeDef.getChild(Edit.Attribute.Child.DEFAULT, Edit.NAMESPACE);
						 if (defaultChild != null) {
							defaultValue = defaultChild.getAttributeValue(Edit.Attribute.Attr.VALUE); 
						 }
					 }
				 }
				 //--- Add new attribute with default value  
				 el.setAttribute(new Attribute(name, defaultValue));
				 // TODO : add attribute should be false and del true after adding an attribute  
				child = el;
			} else {
				//--- normal element
				child = editLib.addElement(schema, el, name);
				if (!childName.equals(""))
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
			}
		} else {
			child = editLib.addElement(schema, el, name);
		}
		//--- now enumerate the new child (if not a simple attribute)
		if (childName == null || !childName.equals("geonet:attribute")) {
			//--- now add the geonet:element back again to keep ref number
			el.addContent(refEl);
			
			int iRef = editLib.findMaximumRef(md);
			editLib.expandElements(schema, child);
			editLib.enumerateTreeStartingAt(child, iRef+1, Integer.parseInt(ref));
			
			//--- add editing info to everything from the parent down
			editLib.expandTree(mds,el);
			
		}
		//--- attach the info element to the child
		child.addContent(info);
		
		//--- attach the info element to the metadata root)
		md.addContent((Element)info.clone());

		//--- store the metadata in the session again 
		setMetadataIntoSession(session,(Element)md.clone(), id);

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
		Element md = getMetadataFromSession(session, id);

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
		
			//--- check and see whether the element to be deleted is the last one of its kind
			Filter elFilter = new ElementFilter(uName,ns);
			if (parent.getContent(elFilter).size() == 1) {

				//--- get geonet child element with attribute name = unqualified name 
				Filter chFilter = new ElementFilter(Edit.RootChild.CHILD, Edit.NAMESPACE);
				List children = parent.getContent(chFilter);
				
				for (int i = 0; i < children.size(); i++) {
					Element ch = (Element) children.get(i);
					String name = ch.getAttributeValue("name");
					if (name != null && name.equals(uName)) {
						result = (Element) ch.clone();
						break;
					}
				}

				// -- now delete the element as requested
				parent.removeContent(me);

				//--- existing geonet child element not present so create it and insert it
				//--- where the last element was deleted
				if (result == null) {
					result = editLib.createElement(schema,el,parent);
					parent.addContent(me,result);
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
			result.addContent(info);
		}

		//--- reattach the info element to the metadata
		md.addContent((Element)info.clone());

		//--- store the metadata in the session again 
		setMetadataIntoSession(session,(Element)md.clone(), id);

		return result;
	}

	/**
	 * Remove attribute in embedded mode
	 * 
	 * @param dbms
	 * @param session
	 * @param id
	 * @param ref	Attribute identifier (eg. _169_uom).
	 * @return
	 * @throws Exception
	 */
	public synchronized Element deleteAttributeEmbedded(Dbms dbms, UserSession session, String id, String ref) throws Exception {
		String[] token = ref.split("_");
		String elementId = token[1];
		String attributeName = token[2];
		Element result = new Element(Edit.RootChild.NULL, Edit.NAMESPACE);
		
		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

		//--- get element to remove
		Element el = editLib.findElement(md, elementId);

		if (el != null) {
			el.removeAttribute(attributeName);
		}
		
		//--- store the metadata in the session again 
		setMetadataIntoSession(session,(Element)md.clone(), id);

		return result;
	}
	
	//--------------------------------------------------------------------------
	/** For Ajax Editing : swap element with sibling ([up] and [down] links)
	  */

	public synchronized void swapElementEmbedded(Dbms dbms, UserSession session, String id, String ref, boolean down) throws Exception
	{
        getMetadataSchema(dbms, id);

		//--- get metadata from session
		Element md = getMetadataFromSession(session, id);

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
		setMetadataIntoSession(session,(Element)md.clone(), id);

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
		Element md = getMetadataFromSession(session, id);

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

			int at = ref.indexOf('_');
			if (at != -1) {
				attr = ref.substring(at + 1);
				ref = ref.substring(0, at);
			}

			Element el = editLib.findElement(md, ref);
			if (el == null) {
				Log.error(Geonet.DATA_MANAGER, "Element not found at ref = " + ref);
				continue;
			}

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
            for (String ref : xmlInputs.keySet()) {
                String value = xmlInputs.get(ref);

                String name = null;
                int addIndex = ref.indexOf('_');
                if (addIndex != -1) {
                    name = ref.substring(addIndex + 1);
                    ref = ref.substring(0, addIndex);
                }

                // Get element to fill
                Element el = editLib.findElement(md, ref);

                if (el == null) {
                    throw new IllegalStateException(
                            "Element not found at ref = " + ref);
                }

                if (value != null && !value.equals("")) {
                    String[] fragments = value.split("&&&");
                    for (String fragment : fragments) {
                        if (name != null) {
                            name = name.replace("COLON", ":");
                            editLib.addFragment(schema, el, name, fragment);
                        }
                        else {
                            // clean before update
                            el.removeContent();

                            fragment = addNamespaceToFragment(fragment);

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

	public Element validateMetadataEmbedded(UserSession session, Dbms dbms, String id, String lang) throws Exception
	{
		String schema = getMetadataSchema(dbms, id);
		
		//--- get metadata from session and clone it for validation
		Element realMd = getMetadataFromSession(session, id);
		Element md = (Element)realMd.clone();

		//--- remove editing info
		editLib.removeEditingInfo(md);
		editLib.contractElements(md);
		md = updateFixedInfo(schema, id, md, dbms);

		//--- do the validation on the metadata
		return doValidate(session, dbms, schema, id, md, lang, false).one();

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

        if (el != null) {
            el.setAttribute(new Attribute(name, ""));
        }

        editLib.contractElements(md);
		md = updateFixedInfo(schema, id, md, dbms);
		XmlSerializer.update(dbms, id, md);

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, md, id);

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

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, md, id);

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

	public synchronized void updateMetadataOwner(Dbms dbms, String id, String owner, String groupOwner) throws Exception
	{
		updateMetadataOwner(dbms,new Integer(id),owner,groupOwner);
	}

	public synchronized void updateMetadataOwner(Dbms dbms, int id, String owner, String groupOwner) throws Exception
	{
		dbms.execute("UPDATE Metadata SET owner=?, groupOwner=? WHERE id=?", new Integer(owner), new Integer(groupOwner), id);
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
			
			int at = ref.indexOf('_');
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
				Log.debug(Geonet.DATA_MANAGER, "replacing XML content");
				el.removeContent();
				
				val = addNamespaceToFragment(val);

				el.addContent(Xml.loadString(val, false));
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
	/**
	 * Update a metadata record.
	 * Clean current validation report in session. If user ask for validation
	 * the validation report will be (re-)created then.
	 */
	public synchronized boolean updateMetadata(UserSession session, Dbms dbms, String id, Element md, boolean validate, String version, String lang) throws Exception
	{
		session.removeProperty(Geonet.Session.VALIDATION_REPORT + id);

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

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, md, id);

		try {
    		//--- do the validation last - it throws exceptions
    		if (validate) {
    			doValidate(session, dbms, schema,id,md,lang, false);
    		}
		} finally {
	        //--- update search criteria
	        indexMetadata(dbms, id);
		}
	
		return true;
	}

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @param xml
     * @return
     */
    public boolean validate(Element xml) {
        String schema = autodetectSchema(xml);
        try {
            validate(schema, xml);
        }
        // XSD validation error(s)
        catch (Exception x) {
            Log.debug(Geonet.DATA_MANAGER, "invalid metadata: " + x.getMessage());
            return false;
        }
        return true;
    }

	/**
	 * Used by the validate embedded service. The validation report
	 * is stored in the session.
	 * 
	 * 
	 * @param session
	 * @param schema
	 * @param id
	 * @param md
	 * @param lang
	 * @param forEditing TODO
	 * @return
	 * @throws Exception
	 */
	public Pair <Element, String> doValidate(UserSession session, Dbms dbms, String schema, String id, Element md, String lang, boolean forEditing) throws Exception
	{
	    String version = null;
		Log.debug(Geonet.DATA_MANAGER, "Creating validation report for record #" + id + " [schema: " + schema + "].");
		
		Element sessionReport = (Element)session.getProperty(Geonet.Session.VALIDATION_REPORT + id);		
		if (sessionReport != null) {
			Log.debug(Geonet.DATA_MANAGER, "  Validation report available in session.");
			sessionReport.detach();
			return Pair.read(sessionReport, version);
		}
		
	    HashMap <String, Integer[]> valTypeAndStatus = new HashMap<String, Integer[]>();
		Element errorReport = new Element ("report", Edit.NAMESPACE);
		errorReport.setAttribute("id", id, Edit.NAMESPACE);
		
		
		//-- get an XSD validation report and add results to the metadata 
		//-- as geonet:xsderror attributes on the affected elements 
		Element xsdErrors = getXSDXmlReport(schema,md);
		if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
			errorReport.addContent(xsdErrors);
			Integer[] results = {0, 0, 0};
			valTypeAndStatus.put("xsd", results);
		     Log.debug(Geonet.DATA_MANAGER, "  - XSD error: " + Xml.getString(xsdErrors));
		} else {
		    Integer[] results = {1, 0, 0};
		    valTypeAndStatus.put("xsd", results);
		}
		
		
		// ...then schematrons
		Element schematronError;
		
		// If in edit mode
        if (forEditing) {
              Log.debug(Geonet.DATA_MANAGER, "  - Schematron in editing mode.");
              //-- now expand the elements and add the geonet: elements
              editLib.expandElements(schema, md);
              version = editLib.getVersionForEditing(schema, id, md);
                    
              //-- get a schematron error report if no xsd errors and add results
              //-- to the metadata as a geonet:schematronerrors element with 
              //-- links to the ref id of the affected element
              schematronError = getSchemaTronXmlReport(schema, md, lang, valTypeAndStatus);
              if (schematronError != null) {
                  md.addContent((Element)schematronError.clone());
                  Log.debug(Geonet.DATA_MANAGER, "  - Schematron error: " + Xml.getString(schematronError));
              }
		} else {
	        // enumerate the metadata xml so that we can report any problems found 
	        // by the schematron_xml script to the geonetwork editor
	        editLib.enumerateTree(md);

	        // get an xml version of the schematron errors and return for error display
	        schematronError = getSchemaTronXmlReport(schema, md, lang, valTypeAndStatus);

	        // remove editing info added by enumerateTree
	        editLib.removeEditingInfo(md);
		}
        
        if (schematronError != null && schematronError.getContent().size() > 0) {
            Element schematron = new Element("schematronerrors", Edit.NAMESPACE);
            Element idElem = new Element("id", Edit.NAMESPACE);
            idElem.setText(id);
            schematron.addContent(idElem);
            errorReport.addContent(schematronError);
            //throw new SchematronValidationErrorEx("Schematron errors detected - see schemaTron report for "+id+" in htmlCache for more details",schematron);
        }
        
        // Save report in session (invalidate by next update) and db
   		session.setProperty(Geonet.Session.VALIDATION_REPORT + id, errorReport);
		saveValidationStatus(dbms, id, valTypeAndStatus, new ISODate().toString());
   		
		return Pair.read(errorReport, version);
	}
	
	/**
	 * Save validation status information into the database for the current record.
	 * 
	 * @param id   the metadata record internal identifier
	 * @param valTypeAndStatus  the validation type could be xsd or schematron rules set identifier
	 * @param date the validation date time
	 */
	private void saveValidationStatus (Dbms dbms, String id, HashMap<String, Integer[]> valTypeAndStatus, String date) throws Exception {
	    clearValidationStatus(dbms, id);
	    Set<String> i = valTypeAndStatus.keySet();
	    for (String type : i) {
	        String query = "INSERT INTO Validation (metadataId, valType, status, tested, failed, valDate) VALUES (?,?,?,?,?,?)";
            Integer[] results = valTypeAndStatus.get(type);
            dbms.execute(query, new Integer(id), type, results[0], results[1], results[2], date);
        }
	}
	/**
	 * Remove validation status information for a metadata record
	 * @param dbms
	 * @param id   the metadata record internal identifier
	 */
	private void clearValidationStatus (Dbms dbms, String id) throws Exception {
	    dbms.execute("DELETE FROM Validation WHERE metadataId=?", new Integer(id));
	}
	/**
	 * Return the validation status information for the metadata record
	 * @param dbms
	 * @param id   the metadata record internal identifier
	 * @return
	 */
	private List<Element> getValidationStatus (Dbms dbms, String id) throws Exception {
	    return dbms.select("SELECT valType, status, tested, failed FROM Validation WHERE metadataId=?", new Integer(id)).getChildren();
    }
	
	
	//--------------------------------------------------------------------------
	//--- Used by the harvesting procedure

	public void updateMetadataExt(Dbms dbms, String id, Element md, String changeDate)
											throws Exception
	{
		XmlSerializer.update(dbms, id, md, changeDate);

        String isTemplate = getMetadataTemplate(dbms, id);
        // Notifies the metadata change to metatada notifier service
        if (isTemplate.equals("n")) {
            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, md, id);
        }
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
        String uuid = getMetadataUuid(dbms, id);
        String isTemplate = getMetadataTemplate(dbms, id);

		//--- remove operations
		deleteMetadataOper(dbms, id, false);

		//--- remove categories
		deleteAllMetadataCateg(dbms, id);

		dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", new Integer(id));
		dbms.execute("DELETE FROM Validation WHERE metadataId=?", new Integer(id));

		//--- remove metadata
		XmlSerializer.delete(dbms, "Metadata", id);

        // Notifies the metadata delete to metatada notifier service

        // Notifies the metadata change to metatada notifier service
        if (isTemplate.equals("n")) {
            notifyMetadataDelete(dbms, id, uuid);
        }

		//--- update search criteria
		searchMan.delete("_id", id+"");
	}

	public synchronized void deleteMetadataGroup(Dbms dbms, String id) throws Exception
	{
		//--- remove operations
		deleteMetadataOper(dbms, id, false);

		//--- remove categories
		deleteAllMetadataCateg(dbms, id);

		dbms.execute("DELETE FROM MetadataRating WHERE metadataId=?", new Integer(id));
        dbms.execute("DELETE FROM Validation WHERE metadataId=?", new Integer(id));

		//--- remove metadata
		XmlSerializer.delete(dbms, "Metadata", id);

		//--- update search criteria
		searchMan.deleteGroup("_id", id+"");
	}

	//--------------------------------------------------------------------------
	/** Remove all operations stored for a metadata
	  */

	public void deleteMetadataOper(Dbms dbms, String id, boolean skipAllIntranet) throws Exception
	{
		String query = "DELETE FROM OperationAllowed WHERE metadataId=?";

		if (skipAllIntranet)
			query += " AND groupId>1";

		dbms.execute(query, new Integer(id));
	}

	//--------------------------------------------------------------------------
	/** Remove all categories stored for a metadata
	  */

	public void deleteAllMetadataCateg(Dbms dbms, String id) throws Exception
	{
		String query = "DELETE FROM MetadataCateg WHERE metadataId=?";

		dbms.execute(query, new Integer(id));
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

		String styleSheet = getSchemaDir(schema) + Geonet.File.EXTRACT_THUMBNAILS;

		Element result = Xml.transform(md, styleSheet);
		result.addContent(new Element("id").setText(id));

		return result;
	}

	//--------------------------------------------------------------------------

	public void setThumbnail(Dbms dbms, String id, boolean small, String file) throws Exception
	{
		int    pos = file.lastIndexOf('.');
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

        // Notifies the metadata change to metatada notifier service
        notifyMetadataChange(dbms, md, id);


		//--- update search criteria
		indexMetadata(dbms, id);
	}

	//--------------------------------------------------------------------------

	public void setDataCommons(Dbms dbms, String id, String licenseurl, String imageurl, String jurisdiction, String licensename, String type) throws Exception
	{
		Element env = new Element("env");
		env.addContent(new Element("imageurl").setText(imageurl));
		env.addContent(new Element("licenseurl").setText(licenseurl));
		env.addContent(new Element("jurisdiction").setText(jurisdiction));
		env.addContent(new Element("licensename").setText(licensename));
		env.addContent(new Element("type").setText(type));

		manageCommons(dbms,id,env,Geonet.File.SET_DATACOMMONS);
	}

	//--------------------------------------------------------------------------

	public void setCreativeCommons(Dbms dbms, String id, String licenseurl, String imageurl, String jurisdiction, String licensename, String type) throws Exception
	{
		Element env = new Element("env");
		env.addContent(new Element("imageurl").setText(imageurl));
		env.addContent(new Element("licenseurl").setText(licenseurl));
		env.addContent(new Element("jurisdiction").setText(jurisdiction));
		env.addContent(new Element("licensename").setText(licensename));
		env.addContent(new Element("type").setText(type));

		manageCommons(dbms,id,env,Geonet.File.SET_CREATIVECOMMONS);
	}

	//--------------------------------------------------------------------------

	private void manageCommons(Dbms dbms, String id, Element env,
										  String styleSheet) throws Exception
	{
		Element md = XmlSerializer.select(dbms, "Metadata", id);

		if (md == null)
			return;

		md.detach();

		String schema = getMetadataSchema(dbms, id);

		transformMd(dbms,id,md,env,schema,styleSheet);
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
		env.addContent(new Element("datadir")         .setText(Lib.resource.getDir(dataDir, Params.Access.PRIVATE, id)));
		return updateFixedInfo(schema, md, env);
	}


	//--------------------------------------------------------------------------

	public Element updateFixedInfoNew(String schema, String id, Element md, String uuid, String parentUuid) throws Exception
	{
		//--- setup environment - for new records

		Element env = new Element("env");

		env.addContent(new Element("id")        			.setText(id));
		env.addContent(new Element("uuid")      			.setText(uuid));
		env.addContent(new Element("parentUuid")			.setText(parentUuid));
		env.addContent(new Element("updateDateStamp")	.setText("yes"));
		env.addContent(new Element("datadir")         .setText(Lib.resource.getDir(dataDir, Params.Access.PRIVATE, id)));
		return updateFixedInfo(schema, md, env);
	}

	//--------------------------------------------------------------------------

    /**
     * Retrieves the unnotified metadata to update/insert for a notifier service
     *
     * @param dbms
     * @param notifierId
     * @return
     * @throws Exception
     */
    public Map<String,Element> getUnnotifiedMetadata(Dbms dbms, String notifierId) throws Exception {
        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata start");
        Map<String,Element> unregisteredMetadata = new HashMap<String,Element>();

        String query = "select m.id, m.uuid, m.data, mn.notifierId, mn.action from metadata m left join metadatanotifications mn on m.id = mn.metadataId\n" +
                "where (mn.notified is null or mn.notified = 'n') and (mn.action <> 'd') and (mn.notifierId is null or mn.notifierId = " + notifierId + ")";
        List<Element> results = dbms.select(query).getChildren();
        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata after select: " + (results != null));

        if (results != null) {
          for(Element result : results) {
              String uuid = result.getChild("uuid").getText();
              System.out.println("getUnnotifiedMetadata: " + uuid);
              unregisteredMetadata.put(uuid, (Element)((Element)result.clone()).detach());

          }
        }

        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadata returning #" + unregisteredMetadata.size() + " results");
        return unregisteredMetadata;
    }

    //--------------------------------------------------------------------------

    /**
     * Retrieves the unnotified metadata to delete for a notifier service
     *
     * @param dbms
     * @param notifierId
     * @return
     * @throws Exception
     */
    public Map<String,Element> getUnnotifiedMetadataToDelete(Dbms dbms, String notifierId) throws Exception {
        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete start");
        Map<String,Element> unregisteredMetadata = new HashMap<String,Element>();
        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete after dbms");

        String query = "select metadataId as id, metadataUuid as uuid, notifierId, action from metadatanotifications " +
                "where (notified = 'n') and (action = 'd') and (notifierId = " + notifierId + ")";
        List<Element> results = dbms.select(query).getChildren();
        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete after select: " + (results != null));

        if (results != null) {
          for(Element result : results) {
              String uuid = result.getChild("uuid").getText();
              System.out.println("getUnnotifiedMetadataToDelete: " + uuid);
              unregisteredMetadata.put(uuid, (Element)((Element)result.clone()).detach());

          }
        }

        Log.debug(Geonet.DATA_MANAGER, "getUnnotifiedMetadataToDelete returning #" + unregisteredMetadata.size() + " results");
        return unregisteredMetadata;
    }


    //--------------------------------------------------------------------------

    /**
     * Mark a metadata record as notified for a notifier service
     *
     * @param metadataId    Metadata identifier
     * @param notifierId    Notifier service identifier
     * @param deleteNotification    Indicates if the notification was a delete action
     * @param dbms
     * @throws Exception
     */
    public void setMetadataNotified(String metadataId, String metadataUuid, String notifierId, boolean deleteNotification, Dbms dbms) throws Exception {
        String query = "DELETE FROM MetadataNotifications WHERE metadataId=? AND notifierId=?";
        dbms.execute(query, new Integer(metadataId), new Integer(notifierId));
        dbms.commit();

        if (!deleteNotification) {
            query = "INSERT INTO MetadataNotifications (metadataId, notifierId, metadataUuid, notified, action) VALUES (?,?,?,?,?)";
            dbms.execute(query, new Integer(metadataId), new Integer(notifierId), metadataUuid, "y", "u");
            dbms.commit();
        }


        Log.debug(Geonet.DATA_MANAGER, "setMetadataNotified finished for metadata with id " + metadataId + "and notitifer with id " + notifierId);
    }

    //--------------------------------------------------------------------------

    /**
     * Mark a metadata record as notified for a notifier service
     *
     * @param metadataId    Metadata identifier
     * @param notifierId    Notifier service identifier
     * @param dbms
     * @throws Exception
     */
    public void setMetadataNotifiedError(String metadataId, String metadataUuid, String notifierId, boolean deleteNotification, String error, Dbms dbms) throws Exception {
        System.out.println("setMetadataNotifiedError");
       try {
       String query = "DELETE FROM MetadataNotifications WHERE metadataId=? AND notifierId=?";
       dbms.execute(query, new Integer(metadataId), new Integer(notifierId));

       String action = (deleteNotification == true)?"d":"u";
       query = "INSERT INTO MetadataNotifications (metadataId, notifierId, metadataUuid, notified, action, errormsg) VALUES (?,?,?,?,?,?)";
       dbms.execute(query, new Integer(metadataId), new Integer(notifierId), metadataUuid, "n", action, error);
       dbms.commit();

       Log.debug(Geonet.DATA_MANAGER, "setMetadataNotifiedError finished for metadata with id " + metadataId + "and notitifer with id " + notifierId);
       } catch (Exception ex) {
           ex.printStackTrace();
           throw ex;
       }
    }

    //--------------------------------------------------------------------------

    public List<Element> retrieveNotifierServices(Dbms dbms) throws Exception {
        String query = "SELECT id, url, username, password FROM MetadataNotifiers WHERE enabled = 'y'";
        List<Element> results = dbms.select(query).getChildren();

        return results;
    }

	//--------------------------------------------------------------------------


    /**
     * Applies automatic fixes to the metadata (if that is enabled) and adds some environmental information about
     * this system.
     *
     * @param schema
     * @param md
     * @param env
     * @return
     * @throws Exception
     */
	private Element updateFixedInfo(String schema, Element md, Element env) throws Exception {
        boolean autoFixing = settingMan.getValueAsBool("system/autofixing/enable", true);
        if(autoFixing) {
        	Log.debug(Geonet.DATA_MANAGER, "Autofixing is enabled, applying update-fixed-info");

        	Element result = new Element("root");
        	result.addContent(md);

        	//--- environment common to both existing and new records goes here
			env.addContent(new Element("changeDate").setText(new ISODate().toString()));
			env.addContent(new Element("siteURL")   .setText(getSiteURL()));
			Element system = settingMan.get("system", -1);
			env.addContent(Xml.transform(system, appPath + Geonet.Path.STYLESHEETS+ "/xml/config.xsl"));
	        result.addContent(env);

	        //--- do an XSLT transformation using update-fixed-info.xsl
	        String styleSheet = getSchemaDir(schema) + Geonet.File.UPDATE_FIXED_INFO;
             result = Xml.transform(result, styleSheet);
            return result;
        }
        else {
            Log.debug(Geonet.DATA_MANAGER, "Autofixing is disabled, not applying update-fixed-info");
            return md;
        }
	}

	//--------------------------------------------------------------------------
	
	/**
	 * Update all children of the selected parent. Some elements are protected
	 * in the children according to the stylesheet used in
	 * xml/schemas/[SCHEMA]/update-child-from-parent-info.xsl.
	 * 
	 * Children MUST be editable and also in the same schema of the parent. 
	 * If not, child is not updated. 
	 * 
	 * @param srvContext
	 *            service context
	 * @param parentUuid
	 *            parent uuid
	 * @param params
	 *            parameters
	 * @param children
	 *            children
	 * @return
	 * @throws Exception
	 */
	public Set<String> updateChildren(ServiceContext srvContext,
			String parentUuid, String[] children, Map<String, String> params)
			throws Exception {
		Dbms dbms = (Dbms) srvContext.getResourceManager().open(
				Geonet.Res.MAIN_DB);

		String parentId = params.get(Params.ID);
		String parentSchema = params.get(Params.SCHEMA);

		// --- get parent metadata in read/only mode
		Element parent = getMetadata(srvContext, parentId, false);

		Element env = new Element("update");
		env.addContent(new Element("parentUuid").setText(parentUuid));
		env.addContent(new Element("siteURL").setText(getSiteURL()));
		env.addContent(new Element("parent").addContent(parent));

		// Set of untreated children (out of privileges, different schemas)
		Set<String> untreatedChildSet = new HashSet<String>();

		// only get iso19139 records
		for (String childId : children) {

			// Check privileges
			if (!accessMan.canEdit(srvContext, childId)) {
				untreatedChildSet.add(childId);
				Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
						+ childId + ") because of privileges.");
				continue;
			}

			Element child = getMetadata(srvContext, childId, false);
			String childSchema = child.getChild(Edit.RootChild.INFO,
					Edit.NAMESPACE).getChildText(Edit.Info.Elem.SCHEMA);

			// Check schema matching. CHECKME : this suppose that parent and
			// child are in the same schema (even not profil different)
			if (!childSchema.equals(parentSchema)) {
				untreatedChildSet.add(childId);
				Log.debug(Geonet.DATA_MANAGER, "Could not update child ("
						+ childId + ") because schema (" + childSchema
						+ ") is different from the parent one (" + parentSchema
						+ ").");
				continue;
			}

			Log.debug(Geonet.DATA_MANAGER, "Updating child (" + childId +") ...");

			// --- setup xml element to be processed by XSLT

			Element rootEl = new Element("root");
			Element childEl = new Element("child").addContent(child.detach());
			rootEl.addContent(childEl);
			rootEl.addContent(env.detach());

			// --- do an XSL transformation

			String styleSheet = getSchemaDir(parentSchema)
					+ Geonet.File.UPDATE_CHILD_FROM_PARENT_INFO;
			Element childForUpdate = new Element("root");
			childForUpdate = Xml.transform(rootEl, styleSheet, params);
			
			XmlSerializer.update(dbms, childId, childForUpdate, new ISODate()
					.toString());


            // Notifies the metadata change to metatada notifier service
            notifyMetadataChange(dbms, childForUpdate, childId);

			rootEl = null;
		}

		return untreatedChildSet;
	}

	
	//--------------------------------------------------------------------------

	/**
	 * TODO : buildInfoElem contains similar portion of code with indexMetadataI
	 */
	private Element buildInfoElem(ServiceContext context, String id, String version) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String query ="SELECT schemaId, createDate, changeDate, source, isTemplate, title, "+
									"uuid, isHarvested, harvestUuid, popularity, rating, owner, displayOrder FROM Metadata WHERE id = " + id;

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
                String  displayOrder = rec.getChildText("displayorder");

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
                addElement(info, Edit.Info.Elem.DISPLAY_ORDER,  displayOrder);

		if (isHarvested.equals("y"))
			info.addContent(harvestMan.getHarvestInfo(harvestUuid, id, uuid));

		if (version != null)
			addElement(info, Edit.Info.Elem.VERSION, version);

		// add operations
		Element operations = accessMan.getAllOperations(context, id, context.getIpAddress());
		HashSet<String> hsOper = accessMan.getOperations(context, id, context.getIpAddress(), operations);

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

        if(accessMan.isVisibleToAll(dbms, id)) {
            addElement(info, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, "true");
        }
        else {
            addElement(info, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, "false");
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

        for (Object category1 : categories) {
            Element category = (Element) category1;
            addElement(info, Edit.Info.Elem.CATEGORY, category.getChildText("name"));
        }

		// add subtemplates
		/* -- don't add as we need to investigate indexing for the fields 
		   -- in the metadata table used here
		List subList = getSubtemplates(dbms, schema);
		if (subList != null) {
			Element subs = new Element(Edit.Info.Elem.SUBTEMPLATES);
			subs.addContent(subList);
			info.addContent(subs);
		}
		*/


        // Add validity information
        List<Element> validationInfo = getValidationStatus(dbms, id);
        if (validationInfo == null || validationInfo.size() == 0) {
            addElement(info, Edit.Info.Elem.VALID, "-1");
        } else {
            String isValid = "1";
            for (Object elem : validationInfo) {
                Element vi = (Element) elem;
                String type = vi.getChildText("valtype");
                String status = vi.getChildText("status");
                if ("0".equals(status)) {
                    isValid = "0";
                }
                String ratio = "xsd".equals(type) ? "" : vi.getChildText("failed") + "/" + vi.getChildText("tested");
                
                info.addContent(new Element(Edit.Info.Elem.VALID + "_details").
                        addContent(new Element("type").setText(type)).
                        addContent(new Element("status").setText(status)).
                        addContent(new Element("ratio").setText(ratio))
                        );
            }
            addElement(info, Edit.Info.Elem.VALID, isValid);
        }
        
		// add baseUrl of this site (from settings)
		String host    = settingMan.getValue("system/server/host");
		String port    = settingMan.getValue("system/server/port");
		addElement(info, Edit.Info.Elem.BASEURL, "http://" + host + (port == "80" ? "" : ":" + port) + baseURL);
		addElement(info, Edit.Info.Elem.LOCSERV, "/srv/en" );
		return info;
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
	
	//---------------------------------------------------------------------------

	/**
	 * Add missing namespace (ie. GML) to XML inputs. It should be done by the client side
	 * but add a check in here.
	 * 
	 * @param fragment 		The fragment to be checked and processed.
	 * 
	 * @return 				The updated fragment.
	 */
	private String addNamespaceToFragment(String fragment)
	{
        //add the gml namespace if its missing
        if (fragment.contains("<gml:") && !fragment.contains("xmlns:gml=\"")) {
        	Log.debug(Geonet.DATA_MANAGER, "  Add missing GML namespace.");
        	fragment = fragment.replaceFirst("<gml:([^ >]+)", "<gml:$1 xmlns:gml=\"http://www.opengis.net/gml\"");
        }
		return fragment;
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
        for (Object aNsList : nsList) {
            Namespace aNs = (Namespace) aNsList;
            if (aNs.getPrefix().equals("")) { // found default namespace
                String prefix = mds.getPrefix(aNs.getURI());
                if (prefix == null) {
                    throw new IllegalArgumentException("No prefix - cannot find a namespace to set for element " + md.getQualifiedName() + " - namespace URI " + ns.getURI());
                }
                ns = Namespace.getNamespace(prefix, aNs.getURI());
                setNamespacePrefix(md, ns);
                if (!md.getNamespace().equals(ns)) {
                    md.removeNamespaceDeclaration(aNs);
                    md.addNamespaceDeclaration(ns);
                }
            }
        }
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

    private void notifyMetadataChange(Dbms dbms, Element md, String id) throws Exception {
        String isTemplate = getMetadataTemplate(dbms, id);

        if (isTemplate.equals("n")) {
            GeonetContext gc = (GeonetContext) servContext.getHandlerContext(Geonet.CONTEXT_NAME);

            String uuid = getMetadataUuid(dbms, id);
            gc.getMetadataNotifier().updateMetadata(md, id, uuid, dbms, gc);
        }
    }

    //--------------------------------------------------------------------------

    private void notifyMetadataDelete(Dbms dbms, String id, String uuid) throws Exception {
        GeonetContext gc = (GeonetContext) servContext.getHandlerContext(Geonet.CONTEXT_NAME);
        gc.getMetadataNotifier().deleteMetadata(id, uuid, dbms, gc);        
    }

    //--------------------------------------------------------------------------

	/**
	 * Update group owner when handling privileges during import.
	 * Does not update the index.
	 * 
	 * @param dbms
	 * @param mdId
	 * @param grpId
	 * @throws Exception
	 */
	public void setGroupOwner(Dbms dbms, String mdId, String grpId)
			throws Exception {
		dbms.execute("UPDATE Metadata SET groupOwner=? WHERE id=?", Integer
				.parseInt(grpId), Integer.parseInt(mdId));
	}
	
    public Element getCswCapabilitiesInfo(Dbms dbms)
          throws Exception {

        return dbms.select("SELECT * FROM CswServerCapabilitiesInfo");
    }


    public CswCapabilitiesInfo getCswCapabilitiesInfo(Dbms dbms, String language)
        throws Exception {

        CswCapabilitiesInfo cswCapabilitiesInfo = new CswCapabilitiesInfo();
        cswCapabilitiesInfo.setLangId(language);

        Element capabilitiesInfoRecord = dbms.select("SELECT * FROM CswServerCapabilitiesInfo WHERE langId = ?", language);

        List<Element> records = capabilitiesInfoRecord.getChildren();
        for(Element record : records) {
            String field = record.getChild("field").getText();
            String label = record.getChild("label").getText();

            if (field.equals("title")) {
                cswCapabilitiesInfo.setTitle(label);

            } else if (field.equals("abstract")) {
                cswCapabilitiesInfo.setAbstract(label);

            } else if (field.equals("fees")) {
                cswCapabilitiesInfo.setFees(label);

            } else if (field.equals("accessConstraints")) {
                cswCapabilitiesInfo.setAccessConstraints(label);
            }
        }

        return cswCapabilitiesInfo;
    }

    public void saveCswCapabilitiesInfo(Dbms dbms, CswCapabilitiesInfo cswCapabilitiesInfo)
            throws Exception {

        String langId = cswCapabilitiesInfo.getLangId();

        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getTitle(), langId, "title");
        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getAbstract(), langId, "abstract");
        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?", cswCapabilitiesInfo.getFees(), langId, "fees");
        dbms.execute("UPDATE CswServerCapabilitiesInfo SET label = ? WHERE langId = ? AND field = ?",  cswCapabilitiesInfo.getAccessConstraints(), langId, "accessConstraints");
    }

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private String baseURL;

	private EditLib editLib;

	private AccessManager  accessMan;
	private SearchManager  searchMan;
	private SettingManager settingMan;
	private SchemaManager  schemaMan;
	private HarvestManager harvestMan;
  private String dataDir;
  private ServiceContext servContext;

	private String appPath;
	private boolean rebuilding = false;

	private String stylePath;
	private static String FS = File.separator;
	
	class IncreasePopularityTask implements Runnable {
        private ServiceContext srvContext;
        String id;
        Dbms dbms = null;
        
        public IncreasePopularityTask(ServiceContext srvContext,
				String id) {
        			this.srvContext = srvContext;
        			this.id = id;
    	}

		public void run() {
      try {
       	dbms = (Dbms) srvContext.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
            	
       	String query = "UPDATE Metadata SET popularity = popularity +1 WHERE "+
				"id = ?";

				dbms.execute(query, new Integer(id));
				indexMetadata(dbms, id);
      } catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (dbms != null) srvContext.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
				} catch (Exception e) {
					Log.error(Geonet.DATA_MANAGER, "There may have been an error updating the popularity of the metadata "+id+". Error: " + e.getMessage());
					e.printStackTrace();
				}
			}

    }
	}
}

//=============================================================================

