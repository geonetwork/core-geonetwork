//==============================================================================
//===
//=== SchemaManager
//===
//==============================================================================
//===	Copyright (C) GeoNetwork
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

import jeeves.server.context.ServiceContext;
import jeeves.constants.Jeeves;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import jeeves.server.dispatchers.guiservices.XmlFile;

import org.apache.commons.lang.StringUtils;

import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaLoader;
import org.fao.geonet.kernel.search.spatial.Pair;

import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.Namespace;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


//=============================================================================

public class SchemaManager
{
	private Map<String, Schema> hmSchemas = new HashMap<String, Schema>();
	private String[] fnames = { "labels.xml", "codelists.xml", "strings.xml" };
	private String   schemaPluginsDir;
	private String   schemaPluginsCat;
	private String	 defaultLang;
	private String	 defaultSchema;
	private String 	 FS         = File.separator;
	private	String	 basePath;

	private static final int MODE_NEEDLE = 0;
	private static final int MODE_ROOT = 1;
	private static final int MODE_NEEDLEWITHVALUE = 2;

	private static final String GEONET_SCHEMA_URI = "http://www.osgeo.org/geonetwork/schemas/geonet_schema";
	private static final Namespace GEONET_SCHEMA_PREFIX_NS = Namespace.getNamespace("gns", GEONET_SCHEMA_URI);
	private static final Namespace GEONET_SCHEMA_NS = Namespace.getNamespace(GEONET_SCHEMA_URI);

	/** Active readers count */
	private static int activeReaders = 0;
	/** Active writers count */
	private static int activeWriters = 0;

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------
	/**	Constructor
		*
		* @param basePath the web app base path
		* @param defaultLang the default language (taken from context)
		* @param defaultSchema the default schema (taken from config.xml)
	  */

	public SchemaManager(String basePath, String sPDir, String defaultLang, String defaultSchema) throws Exception {

		hmSchemas .clear();

		this.basePath = basePath;
		this.schemaPluginsDir  = sPDir;
		this.defaultLang = defaultLang;
		this.defaultSchema = defaultSchema;

		schemaPluginsCat = basePath + Jeeves.Path.WEBINF + FS + Geonet.File.SCHEMA_PLUGINS_CATALOG;

		Element schemaPluginCatRoot = getSchemaPluginCatalog();

		// -- add any of the fixed schemas supplied with GeoNetwork

		String schemasDir = basePath + Geonet.Path.SCHEMAS;
		String saSchemas[] = new File(schemasDir).list();
		if (saSchemas == null) {
			throw new IllegalArgumentException("Cannot scan schemas directory : " +schemasDir);
		} else {
			processSchemas(Geonet.Path.SCHEMAS, schemasDir, saSchemas, false, schemaPluginCatRoot);
		}

		// -- now check the plugin directory and add any schemas in there

		schemasDir = basePath + this.schemaPluginsDir + FS;
		saSchemas = new File(schemasDir).list();
		if (saSchemas == null) {
			Log.error(Geonet.SCHEMA_MANAGER, "Cannot scan plugin schemas directory : " +schemasDir);
		} else {
			processSchemas(schemaPluginsDir, schemasDir, saSchemas, true, schemaPluginCatRoot);
		}

		writeSchemaPluginCatalog(schemaPluginCatRoot);

	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/**	Return a boolean indicating whether the schema is a plugin schema or
	  * not
		*
		* @param schema name of schema to check to see whether it is a plugin schema
	  */

	public boolean isPluginSchema(String name) {

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);

			if (schema == null)
				throw new IllegalArgumentException("Schema not registered : " + name);

			return schema.isPluginSchema();
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the MetadataSchema object
		*
		* @param schema the metadata schema we want the MetadataSchema for
	  */

	public MetadataSchema getSchema(String name) {

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);

			if (schema == null)
				throw new IllegalArgumentException("Schema not registered : " + name);

			final MetadataSchema mds = schema.getMetadataSchema();
			return mds;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/** Add a plugin schema to the list of schemas registered here
		*
		* @param schema the metadata schema we want to add
		* @param in stream containing a zip archive of the schema to add
	  */

	public void addPluginSchema(String name, InputStream in) throws Exception {

		beforeWrite();
		try {
			realAddPluginSchema(name, in);
		} finally {
			afterWrite();
		}
	}

	//--------------------------------------------------------------------------
	/** Update a plugin schema in the list of schemas registered here
		*
		* @param schema the metadata schema we want to update
		* @param in stream containing a zip archive of the schema to update
	  */

	public void updatePluginSchema(String name, InputStream in) throws Exception {

		beforeWrite();
		try {
			// -- delete schema 
			realDeletePluginSchema(name);
			
			// -- add the new one
		} finally {
			afterWrite();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the schema directory
		*
		* @param schema the metadata schema we want the directory for
	  */

	public String getSchemaDir(String name) {

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);	

			if (schema == null)
				throw new IllegalArgumentException("Schema not registered : " + name);

			return schema.getDir();
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the schema location as a JDOM attribute - this can be 
	  * either an xsi:schemaLocation or xsi:noNamespaceSchemaLocation
		* depending on the schema
		*
		* @param schema the metadata schema we want the schemaLocation for
	  */

	public Attribute getSchemaLocation(String name, ServiceContext context) {

		Attribute out = null;

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);	

			if (schema == null)
				throw new IllegalArgumentException("Schema not registered : " + name);

			String nsUri = schema.getMetadataSchema().getPrimeNS();
			String schemaLoc  = schema.getSchemaLocation();
			String schemaFile = schema.getDir() + "schema.xsd";
      if (schemaLoc.equals("")) {
				if (new File(schemaFile).exists()) { // build one 
					String schemaUrl = getSchemaUrl(context, schemaFile);
					if (nsUri == null || nsUri.equals("")) {
						out = new Attribute("noNamespaceSchemaLocation", schemaUrl, Csw.NAMESPACE_XSI);
					} else {
						schemaLoc = nsUri +" "+ schemaUrl;
						out = new Attribute("schemaLocation", schemaLoc, Csw.NAMESPACE_XSI);
          }
				} // else return null - no schema xsd exists - could be dtd
      } else {
				if (nsUri == null || nsUri.equals("")) {
					out = new Attribute("noNamespaceSchemaLocation", schemaLoc, Csw.NAMESPACE_XSI);
				} else {
        	out = new Attribute("schemaLocation", schemaLoc, Csw.NAMESPACE_XSI);
				}
      }
			return out;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the schema templatesdirectory
		*
		* @param schema the metadata schema we want the templates directory for
	  */

	public String getSchemaTemplatesDir(String name) {

		beforeRead();
		try {
			String dir = getSchemaDir(name);

			dir = dir + FS + "templates";
			if (!new File(dir).exists()) { 
				return null;	
			} 
			return dir;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the schema sample data directory
		*
		* @param schema the metadata schema we want the sample data directory for
	  */

	public String getSchemaSampleDataDir(String name) {

		beforeRead();
		try {
			String dir = getSchemaDir(name);

			dir = dir + FS + "sample-data";
			if (!new File(dir).exists()) { 
				return null;	
			} 
			return dir;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the schema csw presentation directory
		*
		* @param schema the metadata schema we want the csw present info directory
	  */

	public String getSchemaCSWPresentDir(String name) {

		beforeRead();
		try {
			String dir = getSchemaDir(name);

			dir = dir +"present"+ FS +"csw";

			return dir;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the schema information (usually localized codelists, labels etc)
	  * XmlFile objects
		*
		* @param schema the metadata schema we want schema info for 
	  */

	public Map<String, XmlFile> getSchemaInfo(String name) {

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);

			if (schema == null)
				throw new IllegalArgumentException("Schema not registered : " + name);

			final Map<String, XmlFile> xfInfo = schema.getInfo();
			return xfInfo;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Return the list of schema names that have been registered 
		*
	  */

	public Set<String> getSchemas()	{

		beforeRead();
		try {
			return hmSchemas.keySet();
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Does the schema named in the parameter exist?
		*
		* @param schema the metadata schema we want to check existence of
	  */

	public boolean existsSchema(String name) {

		beforeRead();
		try {
			return hmSchemas.containsKey(name);
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Delete the schema from the schema information hash tables
		*
		* @param schema the metadata schema we want to delete - can only
		*        be a plugin schema
	  */

	public boolean deletePluginSchema(String name) throws Exception {

		beforeWrite();
		try {
			return realDeletePluginSchema(name);

		} finally {
			afterWrite();
		}
	}

	//--------------------------------------------------------------------------
	/**	Get the SchemaSuggestions class for the supplied schema name
		*
		* @param schema the metadata schema whose suggestions class we want
	  */

	public SchemaSuggestions getSchemaSuggestions(String name) {

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);

			if (schema == null)
				throw new IllegalArgumentException("Schema suggestions not registered : " + name);

			final SchemaSuggestions sugg = schema.getSuggestions();
			return sugg;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/**	Get the namespace URI from the schema information (XSD) 
	  * for the supplied prefix
		*
		* @param name the metadata schema whose namespaces we are searching
		* @param prefix the namespace prefix we want the URI for
	  */

	public String getNamespaceURI(String name, String prefix) {

		beforeRead();
		try {
			Schema schema = hmSchemas.get(name);

			if (schema == null)
				throw new IllegalArgumentException("Schema suggestions not registered : " + name);

			MetadataSchema mds = schema.getMetadataSchema();
			return mds.getNS(prefix);
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	/** 
 		* Used to detect the schema of an imported metadata file
 		* 
 		* @param md the imported metadata file
 		*/

	public String autodetectSchema(Element md) {			

		beforeRead();
		try {
			String schema = null;
				
			// -- check the autodetect elements
			schema  = compareElements(md, MODE_NEEDLE);
			if (schema != null) {
				Log.debug(Geonet.SCHEMA_MANAGER,"  => Found schema "+schema+" using AUTODETECT(needle) examination");
			}
		
			if (schema == null) {
				schema = compareElements(md, MODE_ROOT);
				if (schema != null) {
					Log.debug(Geonet.SCHEMA_MANAGER,"  => Found schema "+schema+" using AUTODETECT(root element) examination");
				}
			}

			if (schema == null) {
				schema = compareElements(md, MODE_NEEDLEWITHVALUE);
				if (schema != null) {
					Log.debug(Geonet.SCHEMA_MANAGER,"  => Found schema "+schema+" using AUTODETECT(needle with value) examination");
				}
			}

			if (schema == null) {
				schema = checkAllNSForMatch(md);
				if (schema != null) {
					Log.debug(Geonet.SCHEMA_MANAGER,"  => Found schema "+schema+" using NAMESPACE examination");
				}
			}

			// -- If nothing has matched by this point choose defaultSchema from 
			// -- config.xml
			if (schema == null) {
				Log.debug(Geonet.SCHEMA_MANAGER, "  No schema detected using default schema "+defaultSchema);
				schema = defaultSchema;
			}

			return schema;
		} finally {
			afterRead();
		}
	}

	//--------------------------------------------------------------------------
	// -- Private methods
	//--------------------------------------------------------------------------
  /**
   * Invoked just before reading, waits until reading is allowed.
   */
  private synchronized void beforeRead() {
    while (activeWriters > 0) {
      try {
        wait();
      } catch (InterruptedException iex) {}
		}
    ++activeReaders;
  }

	//--------------------------------------------------------------------------
  /**
   * Invoked just after reading.
   */
  private synchronized void afterRead() {
    --activeReaders;
    notifyAll();
  }

	//--------------------------------------------------------------------------
  /**
   * Invoked just before writing, waits until writing is allowed.
   */
  private synchronized void beforeWrite() {
    while (activeReaders > 0 || activeWriters > 0) {
      try {
        wait();
      } catch (InterruptedException iex) {}
		}
    ++activeWriters;
  }

	//--------------------------------------------------------------------------
  /**
   * Invoked just after writing.
   */
  private synchronized void afterWrite() {
    --activeWriters;
    notifyAll();
  }

	//--------------------------------------------------------------------------
	/**	Really delete the schema from the schema information hash tables
	 *
	 * @param schema the metadata schema we want to delete - can only
	 *        be a plugin schema
	 */
	private boolean realDeletePluginSchema(String name) throws Exception {

		Schema schema = hmSchemas.get(name);
		if (schema != null) {
			if (schema.isPluginSchema()) {
				removeSchemaInfo(name);
				return true;
			}
		}
		return false;
	}

	//--------------------------------------------------------------------------
	/** Really add a plugin schema to the list of schemas registered here
		*
		* @param schema the metadata schema we want to add
		* @param in stream containing a zip archive of the schema to add
	  */

	private void realAddPluginSchema(String name, InputStream in) throws Exception {
		Element schemaPluginCatRoot = getSchemaPluginCatalog();

		// -- create schema directory 
		String schemasDir = basePath + schemaPluginsDir + FS;
		File dir = new File(schemasDir + name);
		dir.mkdirs();

		try {
			unpackSchemaZipArchive(dir, in);
	
			String[] saSchemas = new String[]{ name };

			// -- add schema using the addSchema method
			int added = processSchemas(schemaPluginsDir, schemasDir, saSchemas, true, schemaPluginCatRoot);
			if (added == 0) throw new OperationAbortedEx("Failed to add schema "+name);

			writeSchemaPluginCatalog(schemaPluginCatRoot);
		} catch (Exception e) {
			deleteDir(dir);
			throw e;
		}
	}

	//--------------------------------------------------------------------------
	/** helper to copy zipentry to on disk file
  	*
		* @param in the InputStream to copy from (usually a zipEntry)
		* @param out the OutputStream to copy to (eg. file output stream)
		*/
	private static final void copyInputStream(InputStream in, OutputStream out) throws Exception
  {
    byte[] buffer = new byte[1024];
    int len;

    while((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    out.close();
  }
		
	//--------------------------------------------------------------------------
	/** unpack the schema supplied as a zip archive into the appropriate dir.
	  *
		* @param dir the directory into which the zip archive will be unpacked
		* @param schemaZipArchive the schema zip archive
	  */

	private void unpackSchemaZipArchive(File dir, InputStream in) throws Exception {

		// -- unpack the schema zip archive into it
		ZipInputStream zipStream = new ZipInputStream(in);

		ZipEntry entry = zipStream.getNextEntry();
		while (entry != null) {

			if (entry.isDirectory()) {
				Log.debug(Geonet.SCHEMA_MANAGER, "Creating directory "+entry.getName());
				(new File(dir, entry.getName())).mkdir();
			} else {
				Log.debug(Geonet.SCHEMA_MANAGER, "Creating file "+entry.getName());
				copyInputStream(zipStream, 
					new BufferedOutputStream(new FileOutputStream(new File(dir, entry.getName()))));
			}
			entry = zipStream.getNextEntry();
		}
		zipStream.close();
	}

	//--------------------------------------------------------------------------
	/** Loads the metadata schema from disk and adds it to the pool
	  *
		* @param fromAppPath webapp path
		* @param name schema name
		* @param isPluginSchema boolean that is set to true if schema is plugin
		* @param xmlSchemaFile name of XML schema file (usually schema.xsd)
		* @param xmlSuggestFile name of schema suggestions file
		* @param xmLSubstitutionsFile name schema substitutions file
		* @param xmlIdFile name of XML file that identifies the schema
		* @param oasisCatFile name of XML OASIS catalog file 
		*/

	private void addSchema(String fromAppPath, String name, boolean isPluginSchema, Element schemaPluginCatRoot, String xmlSchemaFile, String xmlSuggestFile, String xmlSubstitutionsFile, String xmlIdFile, String oasisCatFile) throws Exception {
		String path = new File(xmlSchemaFile).getParent();

		MetadataSchema mds = new SchemaLoader().load(xmlSchemaFile, xmlSubstitutionsFile);
		mds.setName(name);
		mds.setSchemaDir(path);
		mds.loadSchematronRules();

		// -- add cached xml files (schema codelists and label files) 
		// -- as Jeeves XmlFile objects (they need not exist)
		
		String base = fromAppPath + FS + name + FS + "loc";
		Map<String, XmlFile> xfMap = new HashMap<String, XmlFile>();

		for (String fname : fnames) {	
			Log.info(Geonet.SCHEMA_MANAGER, "Searching for "+path+"/loc/"+defaultLang+"/"+fname);
			if (new File(path+ FS +"loc"+ FS +defaultLang+ FS +fname).exists()) {
				Element config = new Element("xml");
				config.setAttribute("name",name);
				config.setAttribute("base",base);
				config.setAttribute("file",fname);
				Log.info(Geonet.SCHEMA_MANAGER, "Adding XmlFile "+Xml.getString(config));
				XmlFile xf = new XmlFile(config, defaultLang, true);
				xfMap.put(fname, xf);
			} else {
				Log.info(Geonet.SCHEMA_MANAGER, "Unable to load this file");
			}
		}

		// -- add any oasis catalog files to xml.catalog.files system property
		// -- for resolver to pick up

		if (new File(oasisCatFile).exists()) {
			String catalogProp = System.getProperty("xml.catalog.files");
			if (catalogProp == null) catalogProp = ""; // shouldn't happen
			if (catalogProp.equals("")) {
				catalogProp = oasisCatFile;
			} else {
				catalogProp = catalogProp + ";" + oasisCatFile;
			}
			System.setProperty("xml.catalog.files", catalogProp);
			System.setProperty("xml.catalog.staticCatalog", "no");
		}

		Pair<String, String> idInfo = extractIdInfo(xmlIdFile, name);

		putSchemaInfo(name,
									idInfo.one(), // uuid of schema
									idInfo.two(), // version of schema
									mds, 
									path + FS,
									new SchemaSuggestions(xmlSuggestFile),
									extractADElements(xmlIdFile),
									xfMap,
									isPluginSchema,
									extractSchemaLocation(xmlIdFile));

		Log.debug(Geonet.SCHEMA_MANAGER, "Property xml.catalog.files is "+System.getProperty("xml.catalog.files"));

		// -- Add entry for presentation xslt to schemaPlugins catalog
		// -- if this schema is a plugin schema
		if (isPluginSchema) {
			int baseNrInt = getHighestSchemaPluginCatalogId(name, schemaPluginCatRoot);
			if (baseNrInt != -1) {
				createUriEntryInSchemaPluginCatalog(name, baseNrInt, schemaPluginCatRoot);		
			}
		}
	}

	//--------------------------------------------------------------------------
	/** Read the elements from the schema plugins catalog for use by other
	  * methods
	  *
		* @param name the name of the schema to use
		*/

	private Element getSchemaPluginCatalog() throws Exception {
		// -- open schemaPlugins catalog, get children named uri
		Element root = Xml.loadFile(schemaPluginsCat);		
		return root;
	}

	//--------------------------------------------------------------------------
	/** Build a path to the schema plugin presentation xslt
	  *
		* @param name the name of the schema to use
		*/

	private String buildSchemaPresentXslt(String name) {
		return "../" + schemaPluginsDir + "/" + name + "/present/metadata-" + name + ".xsl";
	}

	//--------------------------------------------------------------------------
	/** Deletes the presentation xslt from the schemaplugin oasis catalog
	  *
		* @param root the list of elements from the schemaplugin-uri-catalog
		* @param name the name of the schema to use
		*/

	private Element deleteSchemaFromPluginCatalog(String name, Element root) throws Exception {
		List<Content> contents = root.getContent();

		String ourUri =  buildSchemaPresentXslt(name);

		int index = -1;
		for (Content content : contents) {
			Element uri = null;

			if (content instanceof Element) uri = (Element)content;
			else continue; // skip this

		  if (!uri.getName().equals("uri") || !uri.getNamespace().equals(Geonet.OASIS_CATALOG_NAMESPACE)) {
				Log.debug(Geonet.SCHEMA_MANAGER, "Skipping element "+uri.getQualifiedName()+":"+uri.getNamespace());
				continue;
			}

			// -- if already mapped then exit
			if (uri.getAttributeValue("uri").equals(ourUri)) index = root.indexOf(uri); 
		}

		if (index != -1) root.removeContent(index);
		return root;
	}

	//--------------------------------------------------------------------------
	/** Gets the next available blank number that can be used to map the
	  * presentation xslt used by the schema (see metadata-utils.xsl and 
		* Geonet.File.METADATA_MAX_BLANKS). If the presentation xslt is 
		* already mapped then we exit early with return value -1
	  *
		* @param root the list of elements from the schemaplugin-uri-catalog
		* @param name the name of the schema to use
		*/

	private int getHighestSchemaPluginCatalogId(String name, Element root) throws Exception {
		List<Content> contents = root.getContent();

		String baseBlank = Geonet.File.METADATA_BASEBLANK;
		String ourUri =  buildSchemaPresentXslt(name);

		for (Content content : contents) {
			Element uri = null;

			if (content instanceof Element) uri = (Element)content;
			else continue; // skip this

		  if (!uri.getName().equals("uri") || !uri.getNamespace().equals(Geonet.OASIS_CATALOG_NAMESPACE)) {
				Log.debug(Geonet.SCHEMA_MANAGER, "Skipping element "+uri.getQualifiedName()+":"+uri.getNamespace());
				continue;
			}

			// -- if already mapped then exit
			if (uri.getAttributeValue("uri").equals(ourUri)) return -1; 
			
			String nameAttr = uri.getAttributeValue("name");
			if (nameAttr.startsWith(Geonet.File.METADATA_BLANK)) {
				if (nameAttr.compareTo(baseBlank) > 0) baseBlank = nameAttr;
			}
		}

		// -- get highest appropriate number
		String baseNr = baseBlank.replace(Geonet.File.METADATA_BLANK,""); 
		baseNr = baseNr.replace(".xsl",""); 
		int baseNrInt = 0;
		try {
			baseNrInt = Integer.parseInt(baseNr);
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			throw new IllegalArgumentException("Cannot decode blank number from "+baseBlank);
		}
		return baseNrInt;
	}

	//--------------------------------------------------------------------------
	/** Creates a uri remap entry in the schema plugins catalog for the 
	  * presentation xslt used by the schema
	  *
		* @param name the name of the schema to use
		* @param baseNrInt the number of the plugin schema to map the presentation
		*        xslt to
		* @param root the list of elements from the schemaplugin-uri-catalog
		*/

	private void createUriEntryInSchemaPluginCatalog(String name, int baseNrInt, Element root) throws Exception {

		baseNrInt = baseNrInt + 1;
		Element newBlank = new Element("uri", Geonet.OASIS_CATALOG_NAMESPACE);
		if (baseNrInt <= Geonet.File.METADATA_MAX_BLANKS) {
			String zero = "";
			if (baseNrInt < 10) zero = "0";
			newBlank.setAttribute("name", Geonet.File.METADATA_BLANK + zero + baseNrInt + ".xsl");
			newBlank.setAttribute("uri",  buildSchemaPresentXslt(name)); // main presentation xslt
		} else {
			throw new IllegalArgumentException("Exceeded maximum number of plugin schemas "+Geonet.File.METADATA_MAX_BLANKS);
		}

		// -- write out new schemaPlugins catalog and re-init the resolvers that
		// -- use this catalog

		root.addContent(newBlank);
	}

	//--------------------------------------------------------------------------
	/** Write the schema plugin catalog out
	  *
		* @param root the list of elements from the schemaplugin-uri-catalog
		*/
	private void writeSchemaPluginCatalog(Element root) throws Exception {
		Xml.writeResponse(new Document((Element)root.detach()), 
	new BufferedOutputStream(new FileOutputStream(new File(schemaPluginsCat))));
		Xml.resetResolver();	
		Xml.clearTransformerFactoryStylesheetCache();
	}

	//--------------------------------------------------------------------------
	/** Puts information into the schema information hashtables
	  *
		* @param id schema id (uuid)
		* @param version schema version
		* @param name schema name
		* @param mds MetadataSchema object with details of XML schema info 
		* @param schemaDir path name of schema directory
		* @param sugg SchemaSuggestions object
		* @param adElems List of autodetect XML elements (as JDOM Elements)
		* @param xfMap Map containing XML localized info files (as Jeeves XmlFiles)
		* @param isPlugin true if schema is a plugin schema 
		* @param schemaLocation namespaces and URLs of their xsds
		*/
	private void putSchemaInfo(String name, String id, String version, MetadataSchema mds, String schemaDir, SchemaSuggestions sugg, List<Element> adElems, Map<String, XmlFile> xfMap, boolean isPlugin, String schemaLocation) { 
		
		Schema schema = new Schema();

		schema.setId(id);
		schema.setVersion(version);
		schema.setMetadataSchema(mds);
		schema.setDir(schemaDir);
		schema.setSuggestions(sugg);
		schema.setAutodetectElements(adElems);
		schema.setInfo(xfMap);
		schema.setPluginSchema(isPlugin);
		schema.setSchemaLocation(schemaLocation);

		hmSchemas.put(name, schema);
	}

	//--------------------------------------------------------------------------
	/** Deletes information from the schema information hashtables, the schema
	  * directory itself and the mapping for the schema presentation xslt
		* from the schemaplugins oasis catalog
	  *
		* @param name schema name
		*/

	private void removeSchemaInfo(String name) throws Exception { 
		Schema schema = hmSchemas.get(name);

		removeSchemaDir(schema.getDir());
		hmSchemas.remove(name);

		Element schemaPluginCatRoot = getSchemaPluginCatalog();
		schemaPluginCatRoot = deleteSchemaFromPluginCatalog(name, schemaPluginCatRoot);
		writeSchemaPluginCatalog(schemaPluginCatRoot);
	}

	//--------------------------------------------------------------------------
	/** Deletes information into the schema directory 
	  *
		* @param dir schema directory to remove
		*/

	private void removeSchemaDir(String dir) { 
		// -- FIXME: get schema directory and zip it up into the deleted metadata 
		// -- directory?

		Log.debug(Geonet.SCHEMA_MANAGER, "Removing schema directory "+dir);
		boolean deleteOp = deleteDir(new File(dir));
		Log.debug(Geonet.SCHEMA_MANAGER, "Delete operation returned "+deleteOp);

	}

	//--------------------------------------------------------------------------
	/** Processes schemas in either web/xml/schemas or schema plugin directory
	  *
		* @param fromAppPath web app path
		* @param schemasDir path name of directory containing schemas
		* @param saSchemas list of schemas in schemasDir to process 
		* @param isPluginSchema boolean that is set to true if schema is plugin
		*/

	private int processSchemas(String fromAppPath, String schemasDir, String[] saSchemas, boolean isPluginSchema, Element schemaPluginCatRoot) {

		int numberOfSchemasAdded = 0;

		for (int i=0; i<saSchemas.length; i++) {
			if (!saSchemas[i].equals("CVS") && !saSchemas[i].startsWith(".")) {
			    File schemaDir = new File(schemasDir + saSchemas[i]);
                if (schemaDir.isDirectory()) {
    				Log.info(Geonet.SCHEMA_MANAGER, "    Adding xml schema : " +saSchemas[i]);
    				String schemaFile  = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA;
    				String suggestFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_SUGGESTIONS;
    				String substitutesFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_SUBSTITUTES;
    				String idFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_ID;
    				String oasisCatFile = schemasDir + saSchemas[i] +"/"+ Geonet.File.SCHEMA_OASIS;
    
    				try {
    					if (hmSchemas.containsKey(saSchemas[i])) { // exists so ignore it
    						Log.error(Geonet.SCHEMA_MANAGER, "Schema "+saSchemas[i]+" already exists - cannot add!");
    					} else {
    						addSchema(fromAppPath, saSchemas[i], isPluginSchema, schemaPluginCatRoot, schemaFile, suggestFile, substitutesFile, idFile, oasisCatFile);
    						numberOfSchemasAdded++;
    					}
    				} catch (Exception e) {
    					Log.error(Geonet.SCHEMA_MANAGER, "Failed. "+e.getMessage());
    					Log.debug(Geonet.SCHEMA_MANAGER, e);
    					continue; // skip this one
    				}
                }
			}
		}

		return numberOfSchemasAdded;
	}

	//--------------------------------------------------------------------------
	/** Extract schema version and uuid info from identification file and 
	  * compare specified name with name in identification file
	  *
		* @param xmlidFile name of schema XML identification file
		*/
	private Pair<String, String> extractIdInfo(String xmlIdFile, String name) throws Exception {
		// FIXME: should be validating parser
		Element root = Xml.loadFile(xmlIdFile);

		Element id = root.getChild("id", GEONET_SCHEMA_NS);
		if (id == null) id = root.getChild("id", GEONET_SCHEMA_PREFIX_NS);

		Element version = root.getChild("version", GEONET_SCHEMA_NS);
		if (version == null) version = root.getChild("version", GEONET_SCHEMA_PREFIX_NS);

		Element idName = root.getChild("name", GEONET_SCHEMA_NS);
		if (idName == null) idName = root.getChild("name", GEONET_SCHEMA_PREFIX_NS);

		if (!idName.getText().equals(name)) throw new IllegalArgumentException("Schema name supplied "+name+" does not match the name of the schema in the schema-id.xml file "+idName.getText());

		return Pair.read(id.getText(), version.getText());
	}

	//--------------------------------------------------------------------------
	/** Extract schema autodetect info from identification file
	  *
		* @param xmlidFile name of schema XML identification file
		*/
	private List<Element> extractADElements(String xmlIdFile) throws Exception {
		Element root = Xml.loadFile(xmlIdFile);
		Element autodetect = root.getChild("autodetect", GEONET_SCHEMA_NS);
		if (autodetect == null) autodetect = root.getChild("autodetect", GEONET_SCHEMA_PREFIX_NS);

		List<Element> elements = new ArrayList<Element>();

		List list = autodetect.getChildren();
		for (Object aList : list) {
			Element el = (Element) aList;

			if (el.getName().equals("elements")) {
				elements.add(el);
			}
		}
		return elements;
	}

	//--------------------------------------------------------------------------
	/** Extract schemaLocation info from identification file
	  *
		* @param xmlidFile name of schema XML identification file
		*/
	private String extractSchemaLocation(String xmlIdFile) throws Exception {
		Element root = Xml.loadFile(xmlIdFile);
		Element schemaLocElem = root.getChild("schemaLocation", GEONET_SCHEMA_NS);
		if (schemaLocElem == null) schemaLocElem = root.getChild("schemaLocation", GEONET_SCHEMA_PREFIX_NS);
		return schemaLocElem.getText();
	}

	//--------------------------------------------------------------------------
	/** Search all available schemas for one which contains
 	  * the element(s) specified in the autodetect info
		*
		* @param md the XML record whose schema we are trying to find
 	  */
	private String compareElements(Element md, int mode) {
		String returnVal = null;	
		Set<String> allSchemas = getSchemas();
		 Log.debug(Geonet.SCHEMA_MANAGER, "Schema autodetection using mode: " + mode + "...");
		
		for (String schemaName : allSchemas) {		
			Schema schema = hmSchemas.get(schemaName);
			List<Element> adElems = schema.getAutodetectElements();
 			
			for (Element elem : adElems) {			
				List<Element> elemKids = elem.getChildren();
				boolean match = false;
 				
				for (Element kid : elemKids) {
					Attribute type = elem.getAttribute("type");
					// --- is the kid the same as the root of the md
					if (mode==MODE_ROOT && type != null && "root".equals(type.getValue())) {
						Log.debug(Geonet.SCHEMA_MANAGER, "  Comparing "+Xml.getString(kid)+" with "+md.getName()+" with namespace "+md.getNamespace()+" : "+(kid.getName().equals(md.getName()) && kid.getNamespace().equals(md.getNamespace())));
						if (kid.getName().equals(md.getName()) && 
								kid.getNamespace().equals(md.getNamespace())) {
									match = true;
									break;
						}
					// --- try and find the kid in the md (kid only, not value)
					} else if (mode==MODE_NEEDLE && type != null  && "has".equals(type.getValue())) {
						Log.debug(Geonet.SCHEMA_MANAGER, "Comparing "+Xml.getString(kid)+" with "+md.getName()+" with namespace "+md.getNamespace()+" : "+(kid.getName().equals(md.getName()) && kid.getNamespace().equals(md.getNamespace())));

						if (isMatchingElementInMetadata(kid, md, false)) {
							match = true;
						} else {
							match = false;
							break;
						}
					// --- try and find the kid in the md (kid + value)
					} else if (mode==MODE_NEEDLEWITHVALUE) {
						if (isMatchingElementInMetadata(kid, md, true)) {
							match = true;
						} else {
							match = false;
							break;							
						}
 					}
				}
				if (match) return schemaName;
			}
		}
		return returnVal;
	}
	
	//--------------------------------------------------------------------------
	/** This method searches an entire metadata file for an element that
	  *  matches the "needle" metadata element arg - A matching element
	  *  has the same name, namespace and value. 
		*
	  * @param needle the XML element we are trying to find
	  * @param haystack the XML metadata record we are searching
	  * @param checkValue compare the value of the needle with the value of the 
		*                   element we find in the md
 	  */

	private boolean isMatchingElementInMetadata(Element needle, Element haystack, boolean checkValue) {
		boolean returnVal = false;
		Iterator<Element> haystackIterator = haystack.getDescendants(new ElementFilter());
		
		String needleName = needle.getName();
		Namespace needleNS = needle.getNamespace();
		
		while(haystackIterator.hasNext()){
			Element tempElement = haystackIterator.next();
			
			if(tempElement.getName().equals(needleName) && tempElement.getNamespace().equals(needleNS)){
				if (checkValue) {
				    Log.debug(Geonet.SCHEMA_MANAGER, "  Searching value for element: " + tempElement.getName());
		            
					String needleVal = needle.getValue();
					String[] needleToken = needleVal.trim().split("\\|");
                    String tempVal = tempElement.getValue();
                    
					for (String t : needleToken) {
	                    if(tempVal!=null && needleVal!=null){
	                        returnVal = t.equals(tempVal.trim());
	                        if (returnVal) {
	                            Log.debug(Geonet.SCHEMA_MANAGER, "    Found value: " + t + " for needle: " + needleName);
	                            break;
	                        }
	                    }
					}
				} else {
					returnVal = true;
					break;
				}
 			}
 		}
		return returnVal;
	}
	
	//--------------------------------------------------------------------------
	/**	This method searches through every namespace attached to the root 
	  * element for one that matches the prime namespace of a schema 
		*
		* @param md the metadata record that whose namespaces we are searching
	  */

	private String checkAllNSForMatch(Element md) {
		Set<String> allSchemas = getSchemas();
		List<Namespace> metadataAdditionalNS = md.getAdditionalNamespaces();
		
		for (String schema : allSchemas) {		
			String primeNs = getSchema(schema).getPrimeNS();
			Log.debug(Geonet.SCHEMA_MANAGER,"  primeNs "+primeNs+" for schema "+schema);

			if (md.getNamespace().getURI().equals(primeNs) &&
				!md.getNamespace().equals(Csw.NAMESPACE_GMD)) {
				Log.debug(Geonet.SCHEMA_MANAGER,"  schema primeNs "+primeNs+" matches "+md.getNamespace().getURI());
					return schema;
			}
			
			//if any additional namespace matches, return it!
			for (Namespace ns : metadataAdditionalNS) {
				if (ns.getURI().equals(primeNs) &&
					md.getNamespace().equals(Csw.NAMESPACE_GMD)) {
					Log.debug(Geonet.SCHEMA_MANAGER,"  schema primeNs "+primeNs+" is one of additional namespaces "+ns.getURI());
					return schema;
				}
			}
		}
		return null;
	}

	//--------------------------------------------------------------------------
	/**	This method deletes all the files and directories inside another the
	  * schema dir and then the schema dir itself                    
		*
		* @param dir the dir whose contents are to be deleted 
	  */

	private boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) { 
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) return false;
			}
		}

		return dir.delete();
	}

	//--------------------------------------------------------------------------
	/**	Create a URL that can be used to point to a schema XSD delivered by 
	  * GeoNetwork
		*
		* @param context the ServiceContext used to get setting manager and appPath
		* @param schemaDir the schema directory
	  */

	public String getSchemaUrl(ServiceContext context, String schemaDir) {
		SettingInfo si = new SettingInfo(context);

		schemaDir = schemaDir.replace('\\','/');
		String appPath = context.getAppPath().replace('\\','/');
		String relativePath = StringUtils.substringAfter(schemaDir,context.getAppPath()); 
		return si.getSiteUrl() + context.getBaseUrl() + "/" + relativePath;
	}

	//--------------------------------------------------------------------------


}

//=============================================================================

