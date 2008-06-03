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

package org.fao.geonet.kernel.search;

import com.k_int.IR.Searchable;
import com.k_int.hss.HeterogeneousSetOfSearchable;
import com.k_int.util.LoggingFacade.LogContextFactory;
import com.k_int.util.LoggingFacade.LoggingContext;
import com.k_int.util.Repository.CollectionDirectory;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.InitialContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

/**
 * Indexes metadata using Lucene.
 */
public class SearchManager
{
	public static final int LUCENE = 1;
	public static final int Z3950  = 2;
	public static final int UNUSED = 3;

	private static final String SEARCH_STYLESHEETS_DIR_PATH = "xml/search";
	private static final String SCHEMA_STYLESHEETS_DIR_PATH = "xml/schemas";

	private File           _stylesheetsDir;
	private File           _schemasDir;
	private File           _luceneDir;
	private LoggingContext _cat;
	private Searchable     _hssSearchable;

	//-----------------------------------------------------------------------------

	/**
	 * @param appPath
	 * @param luceneDir
	 * @throws Exception
	 */
	public SearchManager(String appPath, String luceneDir) throws Exception
	{
		_stylesheetsDir = new File(appPath, SEARCH_STYLESHEETS_DIR_PATH);
		_schemasDir     = new File(appPath, SCHEMA_STYLESHEETS_DIR_PATH);

		if (!_stylesheetsDir.isDirectory())
			throw new Exception("directory " + _stylesheetsDir + " not found");

		initLucene(appPath, luceneDir);
		initZ3950(appPath);
	}

	//-----------------------------------------------------------------------------

	public void end() throws Exception
	{
		endZ3950();
	}

	//-----------------------------------------------------------------------------

	public MetaSearcher newSearcher(int type, String stylesheetName)
		throws Exception
	{
		switch (type)
		{
			case LUCENE: return new LuceneSearcher(this, stylesheetName);
			case Z3950:  return new Z3950Searcher(this, stylesheetName);
			case UNUSED: return new UnusedSearcher();

			default:     throw new Exception("unknown MetaSearcher type: " + type);
		}
	}

	/**
	 * Lucene init/end methods. Creates the Lucene index directory.
	 * @param appPath
	 * @param luceneDir
	 * @throws Exception
	 */
	private void initLucene(String appPath, String luceneDir)
		throws Exception
	{
		_luceneDir = new File(luceneDir);

		if (!_luceneDir.isAbsolute())
			_luceneDir = new File(appPath + luceneDir);

		//--- the lucene dir cannot be inside the CVS so it is better to create it here

		_luceneDir.mkdirs();

		setupIndex(false); // RGFIX: check if this is correct
	}

	//-----------------------------------------------------------------------------
	// Z39.50 init/end methods

	/**
         * Initializes the Z3950 client searcher.
	 * @param appPath
	 * @throws Exception
	 */
	private void initZ3950(String appPath)
		throws Exception
	{
		try
		{
			_cat = LogContextFactory.getContext("GeoNetwork"); // FIXME: maybe it should use the webapp path

			String configClass = "com.k_int.util.Repository.XMLDataSource";
			String configUrl   = "file:///" + appPath + jeeves.constants.Jeeves.Path.XML + "/repositories.xml";
			String directoryNamingLocation = "/Services/IR/Directory"; // RGFIX: change to use servlet context

			Properties props = new Properties();
			props.setProperty("CollectionDataSourceClassName", configClass);
			props.setProperty("RepositoryDataSourceURL",       configUrl);
			props.setProperty("DirectoryServiceName",          directoryNamingLocation); // RGFIX: check this
			// set up the collection directory and register it with the naming service in the
			// default way
			// RGFIX: this could not work for different servlet instances, should be changed to use servlet context
			CollectionDirectory cd = new CollectionDirectory(configClass, configUrl);
			Context context = new InitialContext();
			Context services_context = context.createSubcontext("Services");
			Context ir_context = services_context.createSubcontext("IR");
			ir_context.bind("Directory", cd);

			// pull in the repository
			_hssSearchable = new HeterogeneousSetOfSearchable();
			_hssSearchable.init(props);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/** deinitializes the Z3950 client searcher
	 */
	private void endZ3950()
	{
		if (_hssSearchable != null)
		{
			_hssSearchable.destroy();
			_hssSearchable = null;
		}
	}

	//--------------------------------------------------------------------------------
	// indexing methods

	/**
	 * Indexes a metadata record.
	 * @param type
	 * @param metadata
	 * @param id
	 * @param moreFields
	 * @param isTemplate
	 * @param title
	 * @throws Exception
	 */
	public synchronized void index(String type, Element metadata, String id, List moreFields, String isTemplate, String title) throws Exception
	{
		delete("_id", id);

		Element xmlDoc;

		// check for subtemplates
		if (isTemplate.equals("s"))
		{
			// create empty document with only title  and "any" fields
			xmlDoc = new Element("Document");

			StringBuffer sb = new StringBuffer();
			allText(metadata, sb);
			addField(xmlDoc, "title", title,           true, true, true);
			addField(xmlDoc, "any",   sb.toString(),   true, true, true);
		}
		else
		{
			Log.debug(Geonet.INDEX_ENGINE, "Metadata to index:\n"+ Xml.getString(metadata));

			xmlDoc = getIndexFields(type, metadata);

			Log.debug(Geonet.INDEX_ENGINE, "Indexing fields:\n"+ Xml.getString(xmlDoc));
		}
		// add _id field
		addField(xmlDoc, "_id", id, true, true, false);

		// add more fields
		for (Iterator iter = moreFields.iterator(); iter.hasNext(); )
		{
			Element field = (Element)iter.next();
			xmlDoc.addContent(field);
		}

		Log.debug(Geonet.INDEX_ENGINE, "Lucene document:\n"+ Xml.getString(xmlDoc));

		Document doc = newDocument(xmlDoc);
		IndexWriter writer = new IndexWriter(_luceneDir, new StandardAnalyzer(new String[] {}), false);
		try
		{
			writer.addDocument(doc);
			lazyOptimize(writer);
		}
		finally
		{
			writer.close();
		}
	}

	/**
	 * Creates a new field for the Lucene index.
	 * @param xmlDoc
	 * @param name
	 * @param value
	 * @param store
	 * @param index
	 * @param token
	 */
	private void addField(Element xmlDoc, String name, String value, boolean store, boolean index, boolean token)
	{
		Element field = new Element("Field");
		field.setAttribute("name",   name);
		field.setAttribute("string", value);
		field.setAttribute("store",  store+"");
		field.setAttribute("index",  index+"");
		field.setAttribute("token",  token+"");
		xmlDoc.addContent(field);
	}

	/**
	 * Extracts text from metadata record.
	 * @param metadata
	 * @param sb
	 * @return all text in the metadata elements for indexing
	 */
	private void allText(Element metadata, StringBuffer sb)
	{
		String text = metadata.getText().trim();
		if (text.length() > 0)
		{
			if (sb.length() > 0) sb.append(" ");
			sb.append(text);
		}
		List children = metadata.getChildren();
		if (children.size() > 0)
		{
			for (Iterator i = children.iterator(); i.hasNext(); )
				allText((Element)i.next(), sb);
		}
	}

	//--------------------------------------------------------------------------------
	//  delete a document

	public synchronized void delete(String fld, String txt) throws Exception
	{
		// possibly remove old document
		IndexReader reader = IndexReader.open(_luceneDir);
		try
		{
			reader.deleteDocuments(new Term(fld, txt));

			// RGFIX: should I optimize here, or at least increase updateCount?
		}
		finally
		{
			reader.close();
		}
	}

	//--------------------------------------------------------------------------------

	public Hashtable getDocs() throws Exception
	{
		IndexReader reader = IndexReader.open(_luceneDir);
		try
		{
			Hashtable docs = new Hashtable();
			for (int i = 0; i < reader.numDocs(); i++)
			{
				if (reader.isDeleted(i)) continue; // FIXME: strange lucene hack: sometimes it tries to load a deleted document

				Hashtable record = new Hashtable();
				Document doc = reader.document(i);
				String id = doc.get("_id");
				for (Enumeration j = doc.fields(); j.hasMoreElements(); )
				{
					Field field = (Field)j.nextElement();
					record.put(field.name(), field.stringValue());
				}
				docs.put(id, record);
			}
			return docs;
		}
		finally
		{
			reader.close();
		}
	}

	//--------------------------------------------------------------------------------

	public Vector getTerms(String fld) throws Exception
	{
		Vector terms = new Vector();

		IndexReader reader = IndexReader.open(_luceneDir);
		try
		{
			TermEnum enu = reader.terms(new Term(fld, ""));
			while (enu.next())
			{
				Term term = enu.term();
				if (term.field().equals(fld))
					terms.add(enu.term().text());
			}
		}
		finally
		{
			reader.close();
		}
		return terms;
	}

	//-----------------------------------------------------------------------------
	// utilities

	Element getIndexFields(String schema, Element xml)
		throws Exception
	{
		File schemaDir = new File(_schemasDir, schema);

		try
		{
			String styleSheet = new File(schemaDir, "index-fields.xsl").getAbsolutePath();
			return Xml.transform(xml, styleSheet);
		}
		catch(Exception e)
		{
			Log.error(Geonet.SEARCH_ENGINE, "Indexing stylesheet contains errors : "+ e.getMessage());
			throw e;
		}
	}

	//-----------------------------------------------------------------------------
	// utilities

	Element transform(String styleSheetName, Element xml)
		throws Exception
	{
		try
		{
			String styleSheetPath = new File(_stylesheetsDir, styleSheetName).getAbsolutePath();
			return Xml.transform(xml, styleSheetPath);
		}
		catch(Exception e)
		{
			Log.error(Geonet.SEARCH_ENGINE, "Search stylesheet contains errors : "+ e.getMessage());
			throw e;
		}
	}

	public File getLuceneDir() { return _luceneDir; }

	Searchable getSearchable() { return _hssSearchable; }

	//-----------------------------------------------------------------------------
	// private methods

	// creates an index in directory luceneDir with StandardAnalyzer if not present
	private void setupIndex(boolean rebuild)
		throws Exception
	{
		// if rebuild forced don't check
		boolean badIndex = true;
		if (!rebuild)
		{
			try
			{
				IndexReader reader = IndexReader.open(_luceneDir);
				reader.close();
				badIndex = false;
			}
			catch (Exception e)
			{
				System.err.println("exception while opening lucene index, going to rebuild it: " + e.getMessage());
			}
		}
		// if rebuild forced or bad index then rebuild index
		if (rebuild || badIndex)
		{
			System.err.println("rebuilding lucene index");

			IndexWriter writer = new IndexWriter(_luceneDir, new StandardAnalyzer(new String[] {}), true);
			writer.close();
		}
	}

	// creates a new document
	private Document newDocument(Element xml)
	{
		Document doc = new Document();
		for (Iterator iter = xml.getChildren().iterator(); iter.hasNext(); )
		{
			Element field = (Element)iter.next();
			String name   = field.getAttributeValue("name");
			String string = field.getAttributeValue("string").toLowerCase(); // RGFIX: should be only needed for non-tokenized fields
			if (string.trim().length() > 0)
			{
				String sStore = field.getAttributeValue("store");
				String sIndex = field.getAttributeValue("index");
				String sToken = field.getAttributeValue("token");
				boolean bStore = sStore != null && sStore.equals("true");
				boolean bIndex = sIndex != null && sIndex.equals("true");
				boolean token = sToken != null && sToken.equals("true");
				Field.Store store = null;
				if(bStore) {
					store = Field.Store.YES;
				}
				else {
					store = Field.Store.NO;
				}
				Field.Index index = null;
				if(bIndex && token) {
					index = Field.Index.TOKENIZED;
				}
				if(bIndex && !token) {
					index = Field.Index.UN_TOKENIZED;
				}
				if(!bIndex) {
					index = Field.Index.NO;
				}
				doc.add(new Field(name, string, store, index));
			}
		}
		return doc;
	}

	//--------------------------------------------------------------------------------

	private static final long TIME_BETWEEN_OPTS     = 1000; // time between two optimizations in ms
	private static final int  UPDTATES_BETWEEN_OPTS = 10;   // number of updates between two optimizations

	private long    lastOptTime = 0; // time since last optimization
	private int     updateCount = UPDTATES_BETWEEN_OPTS - 1; // number of updates since last uptimization
	private boolean optimizing = false; // true iff optimization is in progress
	private Object  mutex = new Object(); // RGFIX: check concurrent access from multiple servlets
	/**
	 * lazy optimization: optimize index if
     * at least TIME_BETWEEN_OPTS time passed or
     * at least UPDTATES_BETWEEN_OPTS updates were performed
     * since last optimization
	 * @param writer
	 * @throws Exception
	 */
	private void lazyOptimize(IndexWriter writer)
		throws Exception
	{
		if (optimizing) return;

		boolean doOptimize;
		synchronized (mutex)
		{
			if (System.currentTimeMillis() - lastOptTime < TIME_BETWEEN_OPTS
				 && ++updateCount < UPDTATES_BETWEEN_OPTS)
				doOptimize = false;
			else
			{
				doOptimize  = true;
 				optimizing  = true;
				updateCount = 0;
			}
		}
		if (doOptimize)
		{
			// System.out.println("**** OPTIMIZING"); // DEBUG

			writer.optimize();
			lastOptTime = System.currentTimeMillis();
			optimizing = false;
		}
	}
}

