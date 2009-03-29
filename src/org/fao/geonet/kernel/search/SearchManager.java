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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.Context;
import javax.naming.InitialContext;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Filter;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.spatial.ContainsFilter;
import org.fao.geonet.kernel.search.spatial.CrossesFilter;
import org.fao.geonet.kernel.search.spatial.EqualsFilter;
import org.fao.geonet.kernel.search.spatial.FullScanFilter;
import org.fao.geonet.kernel.search.spatial.IntersectionFilter;
import org.fao.geonet.kernel.search.spatial.OgcGenericFilters;
import org.fao.geonet.kernel.search.spatial.OverlapsFilter;
import org.fao.geonet.kernel.search.spatial.SpatialFilter;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.search.spatial.TouchesFilter;
import org.fao.geonet.kernel.search.spatial.WithinFilter;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.jdom.Element;

import com.k_int.IR.Searchable;
import com.k_int.hss.HeterogeneousSetOfSearchable;
import com.k_int.util.LoggingFacade.LogContextFactory;
import com.k_int.util.LoggingFacade.LoggingContext;
import com.k_int.util.Repository.CollectionDirectory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;

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

	private final File     _stylesheetsDir;
	private final File     _schemasDir;
	private final Element  _summaryConfig;
	private File           _luceneDir;
	private PerFieldAnalyzerWrapper _analyzer;
	private LoggingContext _cat;
	private Searchable     _hssSearchable;
	private Spatial        _spatial;

	//-----------------------------------------------------------------------------

	/**
	 * @param appPath
	 * @param luceneDir
	 * @throws Exception
	 */
	public SearchManager(String appPath, String luceneDir, String summaryConfigXmlFile) throws Exception
	{
		_summaryConfig = Xml.loadStream(new FileInputStream(new File(appPath,summaryConfigXmlFile)));
		_stylesheetsDir = new File(appPath, SEARCH_STYLESHEETS_DIR_PATH);
		_schemasDir     = new File(appPath, SCHEMA_STYLESHEETS_DIR_PATH);

		if (!_stylesheetsDir.isDirectory())
			throw new Exception("directory " + _stylesheetsDir + " not found");
		
		_luceneDir = new File(luceneDir+ "/nonspatial");
		
		if (!_luceneDir.isAbsolute())
            _luceneDir = new File(appPath + luceneDir+ "/nonspatial");

        _luceneDir.getParentFile().mkdirs();

        _spatial = new Spatial(_luceneDir.getParent() + "/spatial");

        // Define the default Analyzer
		_analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer());
		// Here you could define specific analyzer for each fields stored in the index.
		//
		// For example adding a different analyzer for any (ie. full text search) 
		// could be better than a standard analyzer which has a particular way of 
		// creating tokens.
		// In that situation, when field is "mission AD-T" is tokenized to "mission" "AD" & "T"
		// using StandardAnalyzer.
		// A WhiteSpaceTokenizer tokenized to "mission" "AD-T"
		// which could be better in some situation.
		// But when field is "mission AD-34T" is tokenized to "mission" "AD-34T" using StandardAnalyzer due to number.
		// analyzer.addAnalyzer("any", new WhitespaceAnalyzer());
		// 
		// Uuid stored using a standard analyzer will be change to lower case.
		// Whitespace will not.
		_analyzer.addAnalyzer("_uuid", new WhitespaceAnalyzer());
		_analyzer.addAnalyzer("operatesOn", new WhitespaceAnalyzer());
		_analyzer.addAnalyzer("subject", new KeywordAnalyzer());
		
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
			case LUCENE: return new LuceneSearcher(this, stylesheetName, _summaryConfig);
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
		//--- the lucene dir cannot be inside the CVS so it is better to create 
		// it here
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
			_cat = LogContextFactory.getContext("GeoNetwork"); // FIXME: maybe
																// it should use
																// the webapp
																// path

			String configClass = "com.k_int.util.Repository.XMLDataSource";
			String configUrl = "file:///" + appPath
					+ jeeves.constants.Jeeves.Path.XML + "/repositories.xml";
			String directoryNamingLocation = "/Services/IR/Directory"; // RGFIX:
																		// change
																		// to
																		// use
																		// servlet
																		// context

			Properties props = new Properties();
			props.setProperty("CollectionDataSourceClassName", configClass);
			props.setProperty("RepositoryDataSourceURL", configUrl);
			props.setProperty("DirectoryServiceName", directoryNamingLocation); // RGFIX:
																				// check
																				// this
			// set up the collection directory and register it with the naming
			// service in the
			// default way
			// RGFIX: this could not work for different servlet instances,
			// should be changed to use servlet context
			CollectionDirectory cd = new CollectionDirectory(configClass,
					configUrl);
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

	/**
	 * deinitializes the Z3950 client searcher
	 */
	private void endZ3950() {
		if (_hssSearchable != null) {
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
	public synchronized void index(String type, Element metadata, String id,
			List moreFields, String isTemplate, String title) throws Exception 
	{
		delete("_id", id);

		Element xmlDoc;

		// check for subtemplates
		if (isTemplate.equals("s")) {
			// create empty document with only title and "any" fields
			xmlDoc = new Element("Document");

			StringBuffer sb = new StringBuffer();
			allText(metadata, sb);
			addField(xmlDoc, "title", title, true, true, true);
			addField(xmlDoc, "any", sb.toString(), true, true, true);
		} else {
			Log.debug(Geonet.INDEX_ENGINE, "Metadata to index:\n"
					+ Xml.getString(metadata));

			xmlDoc = getIndexFields(type, metadata);

			Log.debug(Geonet.INDEX_ENGINE, "Indexing fields:\n"
					+ Xml.getString(xmlDoc));
		}
		// add _id field
		addField(xmlDoc, "_id", id, true, true, false);

		// add more fields
		for (Iterator iter = moreFields.iterator(); iter.hasNext();) {
			Element field = (Element) iter.next();
			xmlDoc.addContent(field);
		}

		Log.debug(Geonet.INDEX_ENGINE, "Lucene document:\n"
				+ Xml.getString(xmlDoc));

		Document doc = newDocument(xmlDoc);
		
		
		IndexWriter writer = new IndexWriter(_luceneDir, _analyzer, false);
		try {
			writer.addDocument(doc);
			lazyOptimize(writer);
		} finally {
			writer.close();
		}
		
		_spatial.writer().index(_schemasDir.getPath(), type, id, metadata);
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
	 * 
	 * @param metadata
	 * @param sb
	 * @return all text in the metadata elements for indexing
	 */
	private void allText(Element metadata, StringBuffer sb) {
		String text = metadata.getText().trim();
		if (text.length() > 0) {
			if (sb.length() > 0)
				sb.append(" ");
			sb.append(text);
		}
		List children = metadata.getChildren();
		if (children.size() > 0) {
			for (Iterator i = children.iterator(); i.hasNext();)
				allText((Element) i.next(), sb);
		}
	}

	//--------------------------------------------------------------------------------
	//  delete a document

	public synchronized void delete(String fld, String txt) throws Exception {
		_spatial.writer().delete(txt);
		// possibly remove old document
		IndexReader reader = IndexReader.open(_luceneDir);
		try {
			reader.deleteDocuments(new Term(fld, txt));

			// RGFIX: should I optimize here, or at least increase updateCount?
		} finally {
			reader.close();
		}
	}

	//--------------------------------------------------------------------------------

	public Hashtable getDocs() throws Exception
	{
		IndexReader reader = IndexReader.open(_luceneDir);
		try {
			Hashtable docs = new Hashtable();
			for (int i = 0; i < reader.numDocs(); i++) {
				if (reader.isDeleted(i))
					continue; // FIXME: strange lucene hack: sometimes it tries
								// to load a deleted document
				Hashtable record = new Hashtable();
				Document doc = reader.document(i);
				String id = doc.get("_id");
				List<Field> fields = doc.getFields();
				for (Iterator<Field> j = fields.iterator(); j.hasNext(); ) {
					Field field = j.next();
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

	Element getIndexFields(String schema, Element xml) throws Exception {
		File schemaDir = new File(_schemasDir, schema);

		try {
			String styleSheet = new File(schemaDir, "index-fields.xsl")
					.getAbsolutePath();
			return Xml.transform(xml, styleSheet);
		} catch (Exception e) {
			Log.error(Geonet.SEARCH_ENGINE,
					"Indexing stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

	//-----------------------------------------------------------------------------
	// utilities

	Element transform(String styleSheetName, Element xml) throws Exception 
	{
		try {
			String styleSheetPath = new File(_stylesheetsDir, styleSheetName)
					.getAbsolutePath();
			return Xml.transform(xml, styleSheetPath);
		} catch (Exception e) {
			Log.error(Geonet.SEARCH_ENGINE,
					"Search stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

	public File getLuceneDir() 
	{
		return _luceneDir;
	}

	Searchable getSearchable() 
	{
		return _hssSearchable;
	}

	//-----------------------------------------------------------------------------
	// private methods

	// creates an index in directory luceneDir with StandardAnalyzer if not present
	private void setupIndex(boolean rebuild) throws Exception 
	{
		// if rebuild forced don't check
		boolean badIndex = true;
		if (rebuild) {
			try {
				IndexReader reader = IndexReader.open(_luceneDir);
				reader.close();
				badIndex = false;
			} catch (Exception e) {
				Log.error(Geonet.SEARCH_ENGINE,
						"Exception while opening lucene index, going to rebuild it: "
								+ e.getMessage());
			}
		}
		// if rebuild forced or bad index then rebuild index
		if (rebuild || badIndex) {
			Log.error(Geonet.SEARCH_ENGINE, "Rebuilding lucene index");

			_spatial.writer().reset();
			IndexWriter writer = new IndexWriter(_luceneDir, _analyzer, true);
			writer.close();
		}
	}

	/*
	 *  Rebuild the Lucene index
	 *  
	 *  @param dataMan
	 *  @param dbms
	 *  
	 */
	public boolean rebuildIndex(DataManager dataMan, Dbms dbms) {
		try {
			setupIndex(true);
			dataMan.init(dbms, true);
			return true;
		} catch (Exception e) {
			Log.error(Geonet.SEARCH_ENGINE,
					"Exception while rebuilding lucene index, going to rebuild it: "
							+ e.getMessage());
			return false;
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
			String string = field.getAttributeValue("string"); // Lower case field is handled by Lucene Analyzer.
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
	
	public Spatial getSpatial()
    {
        return _spatial;
    }
	
	public class Spatial 
	{

        private static final long 	TIME_BETWEEN_SPATIAL_COMMITS = 10000;
        private final Map<String, Constructor<? extends SpatialFilter>> _types;
        {
            HashMap<String, Constructor<? extends SpatialFilter>> types = new HashMap<String, Constructor<? extends SpatialFilter>>();

            try {
                types.put(Geonet.SearchResult.Relation.ENCLOSES,
                        constructor(ContainsFilter.class));
                types.put(Geonet.SearchResult.Relation.CROSSES,
                        constructor(CrossesFilter.class));
                types.put(Geonet.SearchResult.Relation.OUTSIDEOF,
                        constructor(FullScanFilter.class));
                types.put(Geonet.SearchResult.Relation.EQUAL,
                        constructor(EqualsFilter.class));
                types.put(Geonet.SearchResult.Relation.INTERSECTION,
                        constructor(IntersectionFilter.class));
                types.put(Geonet.SearchResult.Relation.OVERLAPS,
                        constructor(OverlapsFilter.class));
                types.put(Geonet.SearchResult.Relation.TOUCHES,
                        constructor(TouchesFilter.class));
                types.put(Geonet.SearchResult.Relation.WITHIN,
                        constructor(WithinFilter.class));
                // types.put(Geonet.SearchResult.Relation.CONTAINS,
                // constructor(BeyondFilter.class));
                // types.put(Geonet.SearchResult.Relation.CONTAINS,
                // constructor(DWithinFilter.class));
            } catch (Exception e) {
                throw new RuntimeException("Unable to create types mapping", e);
            }
            _types = Collections.unmodifiableMap(types);
        }
        private final String                                            _appPath;
        private final Transaction                                       _transaction;
        private final Timer                                             _timer;
        private final Parser                                            _gmlParser;
        private final Lock                                              _lock;
        private SpatialIndexWriter                                      _writer;
        private Committer                                               _committerTask;

        public Spatial(String appPath) throws Exception
        {
            _lock = new ReentrantLock();
            _appPath = appPath;
            _transaction = new DefaultTransaction("SpatialIndexWriter");
            _timer = new Timer(true);
            _gmlParser = new Parser(new GMLConfiguration());
            boolean rebuildIndex = false;

            // This must be before createWriter because createWriter will create
            // the file
            // and therefore the test will not be worthwhile
            if (!SpatialIndexWriter.createDataStoreFile(appPath).exists()) {
                rebuildIndex = true;
            }
            rebuildIndex = createWriter(appPath);
            if (rebuildIndex) {
                setupIndex(true);
            }else{
                // since the index is considered good we will 
                // call getIndex to make sure the in-memory index is 
                // generated
                _writer.getIndex();
            }
            addShutdownHook();
        }

        private boolean createWriter(String appPath) throws IOException
        {
            boolean rebuildIndex;
            try {
                _writer = new SpatialIndexWriter(appPath, _gmlParser,
                        _transaction, _lock);
                rebuildIndex = _writer.getFeatureSource().getSchema() == null;
            } catch (Exception e) {
                _writer.delete();
                rebuildIndex = true;
            }
            return rebuildIndex;
        }

        private void addShutdownHook()
        {
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                @Override
                public void run()
                {
                    _lock.lock();
                    try {
                        _writer.close();
                    } catch (IOException e) {
                        _cat.error("error writing spatial index", e);
                    } finally {
                        _lock.unlock();
                    }
                }
            });
        }
        public Filter filter(org.apache.lucene.search.Query query, Element filterExpr, String filterVersion)
                throws Exception
        {

            _lock.lock();
            try {
                Parser filterParser = getFilterParser(filterVersion);
            	FeatureSource featureSource = _writer.getFeatureSource();
            	SpatialIndex index = _writer.getIndex();
                return OgcGenericFilters.create(query, filterExpr,
                        featureSource, index, filterParser);
            } finally {
                _lock.unlock();
            }
        }

		public SpatialFilter filter(org.apache.lucene.search.Query query,
                Geometry geom, Element request) throws Exception
        {
            _lock.lock();
            try {
                String relation = Util.getParam(request,
                        Geonet.SearchResult.RELATION,
                        Geonet.SearchResult.Relation.INTERSECTION);
                SpatialIndexWriter writer = writerNoLocking();
                SpatialIndex index = writer.getIndex();
                FeatureSource featureSource = writer.getFeatureSource();
                return _types.get(relation).newInstance(query, request, geom,
                        featureSource, index);
            } finally {
                _lock.unlock();
            }
        }

        public SpatialIndexWriter writer() throws Exception
        {
            _lock.lock();
            try {
                if (_committerTask != null) {
                    _committerTask.cancel();
                }
                _committerTask = new Committer();
                _timer.schedule(_committerTask, TIME_BETWEEN_SPATIAL_COMMITS);
                return writerNoLocking();
            } finally {
                _lock.unlock();
            }
        }

        private SpatialIndexWriter writerNoLocking() throws Exception
        {
            if (_writer == null) {
                _writer = new SpatialIndexWriter(_appPath, _gmlParser,
                        _transaction, _lock);
            }
            return _writer;
        }
        
        private Parser getFilterParser(String filterVersion) {
			Configuration config;
			config = filterVersion.equals(Csw.FILTER_VERSION_1_0) ? new org.geotools.filter.v1_0.OGCConfiguration()
					: new org.geotools.filter.v1_1.OGCConfiguration();
			return new Parser(config);
		}

        private class Committer extends TimerTask
        {

            @Override
            public void run()
            {
                _lock.lock();
                try {
                    if (_committerTask == this) {
                        _writer.commit();
                        _committerTask = null;
                    }
                } catch (IOException e) {
                    _cat.error("error writing spatial index", e);
                } finally {
                    _lock.unlock();
                }
            }

        }


    }

    private static Constructor<? extends SpatialFilter> constructor(
            Class<? extends SpatialFilter> clazz) throws SecurityException,
            NoSuchMethodException
    {
        return clazz.getConstructor(org.apache.lucene.search.Query.class,
                Element.class, Geometry.class, FeatureSource.class,
                SpatialIndex.class);
    }
}

