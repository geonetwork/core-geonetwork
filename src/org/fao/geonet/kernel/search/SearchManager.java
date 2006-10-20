//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.search;

import org.fao.geonet.constants.Geonet;

import jeeves.utils.Xml;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;

import com.k_int.IR.Searchable;
import com.k_int.util.Repository.CollectionDirectory;
import com.k_int.hss.HeterogeneousSetOfSearchable;
import com.k_int.util.LoggingFacade.*;

import org.jdom.Element;

import java.io.*;
import java.util.*;
import javax.naming.*;

//--------------------------------------------------------------------------------
// index metadata using lucene
//--------------------------------------------------------------------------------

public class SearchManager
{
	public static final int LUCENE = 1;
	public static final int Z3950  = 2;
	public static final int UNUSED = 3;

	private static final String SEARCH_STYLESHEETS_DIR_PATH = "xml/search";

	private File           _stylesheetsDir;
	private File           _luceneDir;
	private LoggingContext _cat;
	private Searchable     _hssSearchable;

	//-----------------------------------------------------------------------------

	public SearchManager(String appPath, String luceneDir) throws Exception
	{
		_stylesheetsDir = new File(appPath, SEARCH_STYLESHEETS_DIR_PATH);
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

	//-----------------------------------------------------------------------------
	// lucene init/end methods

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

	/** initializes the Z3950 client searcher
	 */
	private void initZ3950(String appPath)
		throws Exception
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

	public synchronized void index(String type, Element metadata, String id, List moreFields) throws Exception
	{
		delete("_id", id);

		// get metadata fields
		String stylesheetName = type + ".xsl";
		Element xmlDoc = transform(stylesheetName, metadata);

//System.out.println("XML DOC for id:"+id+"\n" + jeeves.utils.Xml.getString(xmlDoc)); // DEBUG

		// add _id field
		Element idField = new Element("Field");
		idField.setAttribute("name",   "_id");
		idField.setAttribute("string", id);
		idField.setAttribute("store",  "true");
		idField.setAttribute("index",  "true");
		idField.setAttribute("token",  "false");
		xmlDoc.addContent(idField);

		// add more fields
		for (Iterator iter = moreFields.iterator(); iter.hasNext(); )
		{
			Element field = (Element)iter.next();
			xmlDoc.addContent(field);
		}

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

	//--------------------------------------------------------------------------------
	//  delete a document

	public synchronized void delete(String fld, String txt) throws Exception
	{
		// possibly remove old document
		IndexReader reader = IndexReader.open(_luceneDir);
		try
		{
			reader.delete(new Term(fld, txt));

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

	Element transform(String styleSheetName, Element xml)
		throws Exception
	{
		String styleSheetPath = new File(_stylesheetsDir, styleSheetName).getAbsolutePath();
		return Xml.transform(xml, styleSheetPath);
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
				boolean store = sStore != null && sStore.equals("true");
				boolean index = sIndex != null && sIndex.equals("true");
				boolean token = sToken != null && sToken.equals("true");
				doc.add(new Field(name, string, store, index, token));
			}
		}
		return doc;
	}

	//--------------------------------------------------------------------------------
	// lazy optimization: optimize index if
	// at least TIME_BETWEEN_OPTS time passed or
	// at least UPDTATES_BETWEEN_OPTS updates were performed
	// since last optimization

	private static final long TIME_BETWEEN_OPTS     = 1000; // time between two optimizations in ms
	private static final int  UPDTATES_BETWEEN_OPTS = 10;   // number of updates between two optimizations

	private long    lastOptTime = 0; // time since last optimization
	private int     updateCount = UPDTATES_BETWEEN_OPTS - 1; // number of updates since last uptimization
	private boolean optimizing = false; // true iff optimization is in progress
	private Object  mutex = new Object(); // RGFIX: check concurrent access from multiple servlets
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

