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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;
import jeeves.exceptions.JeevesException;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Filter;
import org.apache.lucene.store.FSDirectory;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.spatial.ContainsFilter;
import org.fao.geonet.kernel.search.spatial.CrossesFilter;
import org.fao.geonet.kernel.search.spatial.EqualsFilter;
import org.fao.geonet.kernel.search.spatial.IntersectionFilter;
import org.fao.geonet.kernel.search.spatial.IsFullyOutsideOfFilter;
import org.fao.geonet.kernel.search.spatial.OgcGenericFilters;
import org.fao.geonet.kernel.search.spatial.OverlapsFilter;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.search.spatial.SpatialFilter;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.search.spatial.TouchesFilter;
import org.fao.geonet.kernel.search.spatial.WithinFilter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.domain.IndexLanguage;
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private static final String STOPWORDS_DIR_PATH = "resources/stopwords";

	private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();

	private final File     _stylesheetsDir;
	private final File     _schemasDir;
    private static String _stopwordsDir;
	private final Element  _summaryConfig;
	private final Element  _tokenizedFields;
	private final Element  _numericFields;    
	private File           _luceneDir;
	private static PerFieldAnalyzerWrapper _analyzer;
	private String         _htmlCacheDir;
    private Spatial        _spatial;
	private LuceneIndexReaderFactory    _indexReader;
	private LuceneIndexWriterFactory    _indexWriter;
	private Timer					 _optimizerTimer = null;
	// minutes between optimizations of the lucene index
	private int						 _optimizerInterval;
	private Calendar       _optimizerBeginAt;
	private SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private boolean        _inspireEnabled = false;

    public void setInspireEnabled(boolean inspireEnabled) {
        this._inspireEnabled = inspireEnabled;
    }

	//-----------------------------------------------------------------------------

    /**
     * Initializes the PerFieldAnalyzerWrapper, which is used when adding documents to the Lucene index, and also
     * to analyze query terms at search time.
     *
     * @param dbms dbms
     */
	public void initAnalyzer(Dbms dbms) {
    // Define the default Analyzer

        Set<String> stopwords = null;
        try {
           stopwords = findStopwords(dbms);
        }
        catch(Exception x) {
           Log.warning("SearchManager", "Exception getting stopwords: " + x.getMessage() + ", now creating GeoNetworkAnalyzer without stopwords");
        }
		_analyzer = new PerFieldAnalyzerWrapper(new GeoNetworkAnalyzer(stopwords));
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
		
		// heikki doeleman: UUID must be case insensitive, as its parts are hexadecimal numbers which are not case sensitive.
		_analyzer.addAnalyzer("_uuid", new GeoNetworkAnalyzer());
		_analyzer.addAnalyzer("parentUuid", new GeoNetworkAnalyzer());
		_analyzer.addAnalyzer("operatesOn", new GeoNetworkAnalyzer());
		_analyzer.addAnalyzer("subject", new KeywordAnalyzer());
        _analyzer.addAnalyzer("inspiretheme", new KeywordAnalyzer());

	}

    /**
     * Retrieves stopwords for selected languages.
     * 
     * @return stopwords
     * @throws Exception hmm
     */
    private static Set<String> findStopwords(Dbms dbms) throws Exception {
        Set<String> allStopwords = null;
        // retrieve index languages defined by administrator
        IndexLanguagesDAO idxLanguagesDAO = new IndexLanguagesDAO();
        Set<IndexLanguage> languages = idxLanguagesDAO.retrieveSelectedIndexLanguages(dbms);
        if(languages != null) {
            for(IndexLanguage language : languages) {
                Log.debug(Geonet.SEARCH_ENGINE,"Loading stopwords for " + language.getName());
                // look up stopwords for that language
                Set<String> stopwords = StopwordFileParser.parse(_stopwordsDir + File.separator + language.getName());
                if(stopwords != null) {
                    if (allStopwords == null) {
                        allStopwords = new HashSet<String>();
                    }
                    allStopwords.addAll(stopwords);                    
                }
            }
        }
        return allStopwords;
    }


	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @return
     */
	public static PerFieldAnalyzerWrapper getAnalyzer() {
		return _analyzer;
	}

	//-----------------------------------------------------------------------------

	/**
	 * @param appPath
	 * @param luceneDir
	 * @param summaryConfigXmlFile
	 * @param dataStore
     * @param settingMan
     * @param dbms
     * 
	 * @throws Exception
	 */
	public SearchManager(String appPath, String luceneDir, String htmlCacheDir, 
                             String summaryConfigXmlFile, String luceneConfigXmlFile, DataStore dataStore, SettingManager settingMan, Dbms dbms) throws Exception
	{
        Dbms _dbms = dbms;
        SettingManager _settingMan = settingMan;
        SettingInfo si = new SettingInfo(_settingMan);


		_summaryConfig = Xml.loadStream(new FileInputStream(new File(appPath,summaryConfigXmlFile)));

		Element luceneConfig = Xml.loadStream(new FileInputStream(new File(appPath,luceneConfigXmlFile)));
		_tokenizedFields = luceneConfig.getChild("tokenized");
        _numericFields = luceneConfig.getChild("numeric");

		_stylesheetsDir = new File(appPath, SEARCH_STYLESHEETS_DIR_PATH);
		_schemasDir     = new File(appPath, SCHEMA_STYLESHEETS_DIR_PATH);
        _stopwordsDir = appPath + STOPWORDS_DIR_PATH;

        initAnalyzer(dbms);


        _inspireEnabled = si.getInspireEnabled();

		if (!_stylesheetsDir.isDirectory())
			throw new Exception("directory " + _stylesheetsDir + " not found");

		File htmlCacheDirTest   = new File(appPath, htmlCacheDir);
		if (!htmlCacheDirTest.isDirectory())
			throw new IllegalArgumentException("directory " + htmlCacheDir + " not found");
		_htmlCacheDir = htmlCacheDir;


        _luceneDir = new File(luceneDir+ "/nonspatial");
		
		if (!_luceneDir.isAbsolute()) _luceneDir = new File(appPath + luceneDir+ "/nonspatial");

     _luceneDir.getParentFile().mkdirs();
        
     _spatial = new Spatial(dataStore);

		initLucene();
		initZ3950();

		if (si.getLuceneIndexOptimizerSchedulerEnabled()) {
      _optimizerBeginAt  = si.getLuceneIndexOptimizerSchedulerAt();
      _optimizerInterval = si.getLuceneIndexOptimizerSchedulerInterval();
      scheduleOptimizerThread();
    } else {
      Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index is disabled");
    }	
	}

	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @throws Exception
     */
	public void end() throws Exception
	{
		endZ3950();
		_optimizerTimer.cancel();
	}

	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public synchronized void disableOptimizer() throws Exception
  {
    Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index is disabled");
    if (_optimizerTimer != null) {
      _optimizerTimer.cancel();
    }
  }

	//-----------------------------------------------------------------------------
	
    /**
     * TODO javadoc.
     *
     * @param optimizerBeginAt
     * @param optimizerInterval
     * @throws Exception
     */
	public synchronized void rescheduleOptimizer(Calendar optimizerBeginAt, int optimizerInterval) throws Exception
	{
		if (_dateFormat.format(optimizerBeginAt.getTime()).equals(_dateFormat.format(_optimizerBeginAt.getTime())) && (optimizerInterval == _optimizerInterval)) return; // do nothing unless at and interval has changed

		_optimizerInterval = optimizerInterval;
		_optimizerBeginAt  = optimizerBeginAt;
		if (_optimizerTimer != null) _optimizerTimer.cancel();
		scheduleOptimizerThread();
	}

	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	private void scheduleOptimizerThread() throws Exception {
		_optimizerTimer = new Timer(true);

		// at _optimizerBeginAt and again at every _optimizerInterval minutes
		Date beginAt = getBeginAt(_optimizerBeginAt);
		_optimizerTimer.schedule(new OptimizeTask(), beginAt, _optimizerInterval * 60 * 1000); 

		Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index to run at "+_dateFormat.format(beginAt)+" and every "+_optimizerInterval+" minutes afterwards");
	}

	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param timeToStart
     * @return
     */
	private Date getBeginAt(Calendar timeToStart) {
		Calendar now = Calendar.getInstance();
		Calendar ts  = Calendar.getInstance();

		ts.set(Calendar.DAY_OF_MONTH,	now.get(Calendar.DAY_OF_MONTH));
		ts.set(Calendar.MONTH,				now.get(Calendar.MONTH));
		ts.set(Calendar.YEAR,					now.get(Calendar.YEAR));
		ts.set(Calendar.HOUR,					timeToStart.get(Calendar.HOUR));
		ts.set(Calendar.MINUTE,				timeToStart.get(Calendar.MINUTE));
		ts.set(Calendar.SECOND,				timeToStart.get(Calendar.SECOND));

		// if the starttime has already past then schedule for tommorrow
		if (now.after(ts)) ts.add(Calendar.DAY_OF_MONTH, 1);

		return ts.getTime();
	}

	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     */
	class OptimizeTask extends TimerTask {
		public void run() {
			try {
				_indexWriter.openWriter();
			} catch (Exception e) {
				Log.error(Geonet.INDEX_ENGINE, "Optimize task failed to open the index: "+e.getMessage());
				e.printStackTrace();
			}

			try {
				Log.info(Geonet.INDEX_ENGINE, "Index Optimization Thread Task...started");
				_indexWriter.optimize();
				Log.info(Geonet.INDEX_ENGINE, "Index Optimization Thread Task...ended");
			} catch (Exception e) {
				Log.error(Geonet.INDEX_ENGINE, "Optimize task failed: "+e.getMessage());
				e.printStackTrace();
			} finally {
				try {
					_indexWriter.closeWriter();
				} catch (Exception e) {
					Log.error(Geonet.INDEX_ENGINE, "Optimize task failed to close the index: "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	//-----------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param type
     * @param stylesheetName
     * @return
     * @throws Exception
     */
	public MetaSearcher newSearcher(int type, String stylesheetName)
		throws Exception
	{
		switch (type)
		{
			case LUCENE: return new LuceneSearcher(this, stylesheetName, _summaryConfig, _tokenizedFields, _numericFields);
			case Z3950:  return new Z3950Searcher(this, stylesheetName);
			case UNUSED: return new UnusedSearcher();

			default:     throw new Exception("unknown MetaSearcher type: " + type);
		}
	}

	/**
	 * Lucene init/end methods. Creates the Lucene index directory.
	 * @throws Exception
	 */
	private void initLucene()
		throws Exception
	{
		setupIndex(false); 
	}

	//-----------------------------------------------------------------------------
	// Z39.50 init/end methods

	/**
         * Initializes the Z3950 client searcher.
	 */
	private void initZ3950() {}

	/**
	 * deinitializes the Z3950 client searcher.
	 */
	private void endZ3950() {
//		if (_hssSearchable != null) {
//      // nothing done ??
//		}
	}
	
	//-----------------------------------------------------------------------------

	public String getHtmlCacheDir() {
		return _htmlCacheDir;
	}

	//--------------------------------------------------------------------------------
	// indexing methods

	/**
	 * Indexes a metadata record.
     *
	 * @param type
	 * @param metadata
	 * @param id
	 * @param moreFields
	 * @param isTemplate
	 * @param title
	 * @throws Exception
	 */
	public void index(String type, Element metadata, String id, List<Element> moreFields, String isTemplate, String title) throws Exception
	{
		Log.debug(Geonet.INDEX_ENGINE, "Opening Writer from index");
		_indexWriter.openWriter();
		try {
			Document doc = buildIndexDocument(type, metadata, id, moreFields, isTemplate, title, false);
			_indexWriter.addDocument(doc);
		} finally {
			Log.debug(Geonet.INDEX_ENGINE, "Closing Writer from index");
			_indexWriter.closeWriter();
		}

		_spatial.writer().index(_schemasDir.getPath(), type, id, metadata);
	}

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public void startIndexGroup() throws Exception {
		Log.debug(Geonet.INDEX_ENGINE, "Opening Writer from startIndexGroup");
		_indexWriter.openWriter();
	}

    /**
     * TODO javadoc.
     *
     * @param type
     * @param metadata
     * @param id
     * @param moreFields
     * @param isTemplate
     * @param title
     * @throws Exception
     */
	public void indexGroup(String type, Element metadata, String id, List<Element> moreFields, String isTemplate, String title) throws Exception
	{
		Document doc = buildIndexDocument(type, metadata, id, moreFields, isTemplate, title, true);
		_indexWriter.addDocument(doc);

		_spatial.writer().index(_schemasDir.getPath(), type, id, metadata);
	}

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public void endIndexGroup() throws Exception {
		Log.debug(Geonet.INDEX_ENGINE, "Closing Writer from endIndexGroup");
		_indexWriter.closeWriter();
	}

    /**
     * TODO javadoc.
     *
     * @param fld
     * @param txt
     * @throws Exception
     */
	public void deleteGroup(String fld, String txt) throws Exception {
		// possibly remove old document
		Log.debug(Geonet.INDEX_ENGINE,"Deleting document ");
		_indexWriter.deleteDocuments(new Term(fld, txt));

		_spatial.writer().delete(txt);
	}

    /**
     * TODO javadoc.
     *
     * @param type
     * @param metadata
     * @param id
     * @param moreFields
     * @param isTemplate
     * @param title
     * @param group
     * @return
     * @throws Exception
     */
    private Document buildIndexDocument(String type, Element metadata, String id, List<Element> moreFields, String isTemplate, String title, boolean group) throws Exception
	{

		Log.debug(Geonet.INDEX_ENGINE, "Deleting "+id+" from index");
		if (group) deleteGroup("_id", id);
		else delete("_id", id);
		Log.debug(Geonet.INDEX_ENGINE, "Finished Delete");

		Element xmlDoc;

		// check for subtemplates
		if (isTemplate.equals("s")) {
			// create empty document with only title and "any" fields
			xmlDoc = new Element("Document");
			StringBuffer sb = new StringBuffer();
			allText(metadata, sb);
			addField(xmlDoc, "title", title, true, true, true);
			addField(xmlDoc, "any", sb.toString(), true, true, true);
		}
        else {
			Log.debug(Geonet.INDEX_ENGINE, "Metadata to index:\n" + Xml.getString(metadata));
            xmlDoc = getIndexFields(type, metadata);
			Log.debug(Geonet.INDEX_ENGINE, "Indexing fields:\n" + Xml.getString(xmlDoc));
		}
		// add _id field
		addField(xmlDoc, "_id", id, true, true, false);

		// add more fields
        for (Element moreField : moreFields) {
            xmlDoc.addContent(moreField);
        }

		Log.debug(Geonet.INDEX_ENGINE, "Lucene document:\n" + Xml.getString(xmlDoc));
        return newDocument(xmlDoc);
	}

	/**
	 * Creates a new field for the Lucene index.
     *
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
            for (Object aChildren : children) {
                allText((Element) aChildren, sb);
            }
		}
	}

	//--------------------------------------------------------------------------------

    /**
     *  deletes a document.
     * 
     * @param fld
     * @param txt
     * @throws Exception
     */
	public void delete(String fld, String txt) throws Exception {
		
		// possibly remove old document
		Log.debug(Geonet.INDEX_ENGINE, "Opening Writer from delete");
		_indexWriter.openWriter();
		try {
			_indexWriter.deleteDocuments(new Term(fld, txt));
		} finally {
			Log.debug(Geonet.INDEX_ENGINE, "Closing Writer from delete");
			_indexWriter.closeWriter();
		}
		_spatial.writer().delete(txt);
	}

	//--------------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public ArrayList<Integer> getDocsWithXLinks() throws Exception
	{
		IndexSearcher searcher = getNewIndexSearcher().two();
		IndexReader reader = searcher.getIndexReader();

		try {
			FieldSelector idXLinkSelector = new FieldSelector() {
				public final FieldSelectorResult accept(String name) {
					if (name.equals("_id") || name.equals("_hasxlinks")) return FieldSelectorResult.LOAD;
					else return FieldSelectorResult.NO_LOAD;
				}
			};
	
			ArrayList<Integer> docs = new ArrayList<Integer>();
			for (int i = 0; i < reader.maxDoc(); i++) {
				if (reader.isDeleted(i)) continue; // FIXME: strange lucene hack: sometimes it tries to load a deleted document
				Document doc = reader.document(i, idXLinkSelector);
				String id = doc.get("_id");
				String hasxlinks = doc.get("_hasxlinks");
				Log.debug(Geonet.INDEX_ENGINE, "Got id "+id+" : '"+hasxlinks+"'");
				if (id == null) {
					Log.error(Geonet.INDEX_ENGINE, "Document with no _id field skipped! Document is "+doc);
					continue;
				}
				if (hasxlinks.trim().equals("1")) {
					docs.add(new Integer(id));
				}
			}
			return docs;
		} finally {
			releaseIndexSearcher(searcher);
		}
	}

	//--------------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public HashMap<String,String> getDocsChangeDate() throws Exception
	{
		IndexSearcher searcher = getNewIndexSearcher().two();
		IndexReader reader = searcher.getIndexReader();

		try {
			FieldSelector idChangeDateSelector = new FieldSelector() {
				public final FieldSelectorResult accept(String name) {
					if (name.equals("_id") || name.equals("_changeDate")) return FieldSelectorResult.LOAD;
					else return FieldSelectorResult.NO_LOAD;
				}
			};
	
			int capacity = (int)(reader.maxDoc() / 0.75)+1;
			HashMap<String,String> docs = new HashMap<String,String>(capacity);
			for (int i = 0; i < reader.maxDoc(); i++) {
				if (reader.isDeleted(i)) continue; // FIXME: strange lucene hack: sometimes it tries to load a deleted document
                Document doc = reader.document(i, idChangeDateSelector);
				String id = doc.get("_id");
				if (id == null) {
					Log.error(Geonet.INDEX_ENGINE, "Document with no _id field skipped! Document is "+doc);
					continue;
				}
				docs.put(id, doc.get("_changeDate"));
			}
			return docs;
		} finally {
			releaseIndexSearcher(searcher);
		}
	
	}

	/**
	 * Browses the index and returns all values for the Lucene field.
	 * 
	 * @param fld	The Lucene field name 
	 * @return	The list of values for the field
	 * @throws Exception
	 */
	public Vector<String> getTerms(String fld) throws Exception
	{
		Vector<String> terms = new Vector<String>();

		IndexSearcher searcher = getNewIndexSearcher().two();
		IndexReader reader = searcher.getIndexReader();

		try {
			TermEnum enu = reader.terms(new Term(fld, ""));
			if (enu.term()==null) return terms;
			do	{
				Term term = enu.term();
				if (term == null || !term.field().equals(fld)) break;
				terms.add(enu.term().text());
			} while (enu.next());
			return terms;
		} finally {
			releaseIndexSearcher(searcher);
		}
	}

	/**
	 * Browse the index for the specified Lucene field and return the list
	 * of terms found containing the search value with their frequency.
	 * 
	 * @param fieldName	The Lucene field name 
	 * @param searchValue	The value to search for. Could be "".
	 * @param maxNumberOfTerms	Max number of term's values to look in the index. For large catalogue
	 * this value should be increased in order to get better results. If this
	 * value is too high, then looking for terms could take more times. The use
	 * of good analyzer should allow to reduce the number of useless values like
	 * (a, the, ...).
	 * @param threshold	Minimum frequency for a term to be returned.
	 * @return	An unsorted and unordered list of terms with their frequency.
	 * @throws Exception
	 */
	public List<TermFrequency> getTermsFequency(String fieldName, String searchValue, int maxNumberOfTerms, int threshold) throws Exception
	{
		List<TermFrequency> termList = new ArrayList<TermFrequency>();

		IndexSearcher searcher = getNewIndexSearcher().two();
		IndexReader reader = searcher.getIndexReader();
		TermEnum term = reader.terms();
		int i = 0;
		// TODO : we should apply the same Analyzer used for field indexing
		// to the term searched.

		try {
			// Extract terms containing search value.
			while (term.next()) {
				if (++i > maxNumberOfTerms)
					break;

				if (term.docFreq() >= threshold
						&& term.term().field().equals(fieldName)
						&& term.term().text().contains(searchValue)) {
					TermFrequency freq = new TermFrequency(term.term().text(), term
							.docFreq());
					termList.add(freq);
				}
			}
		} finally {
			releaseIndexSearcher(searcher);
		}
		return termList;
	}
	
	
	/**
	 * Frequence of terms.
     *
	 */
	public static class TermFrequency implements Comparable<Object> {
		String term;
		int frequency;

		public TermFrequency(String term, int frequency) {
			this.term = term;
			this.frequency = frequency;
		}

		public String getTerm() {
			return this.term;
		}

		public int getFrequency() {
			return this.frequency;
		}
		
		public int compareTo(Object o) {
			if (o instanceof TermFrequency) {
				TermFrequency oFreq = (TermFrequency) o;
				return new CompareToBuilder()
						.append(frequency, oFreq.frequency).append(term,
								oFreq.term).toComparison();
			} else {
				return 0;
			}
		}
	}
	
	//-----------------------------------------------------------------------------
	// utilities

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param xml
     * @return
     * @throws Exception
     */
	Element getIndexFields(String schema, Element xml) throws Exception {
		File schemaDir = new File(_schemasDir, schema);

		try {
			String styleSheet = new File(schemaDir, "index-fields.xsl")
					.getAbsolutePath();
            Map<String,String> params = new HashMap<String, String>();
            params.put("inspire", Boolean.toString(_inspireEnabled));

			return Xml.transform(xml, styleSheet, params);
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Indexing stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

	//-----------------------------------------------------------------------------
	// utilities

    /**
     * TODO javadoc.
     *
     * @param styleSheetName
     * @param xml
     * @return
     * @throws Exception
     */
	Element transform(String styleSheetName, Element xml) throws Exception 
	{
		try {
			String styleSheetPath = new File(_stylesheetsDir, styleSheetName)
					.getAbsolutePath();
			return Xml.transform(xml, styleSheetPath);
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Search stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

	//----------------------------------------------------------------------------
	/**
	 * Return a (refreshed) IndexSearcher to do search operations on.
	 * 
	 * @return
	 */
	public Pair<Long,IndexSearcher> getIndexSearcher(long token) throws IOException, InterruptedException {
		Pair<Long,IndexSearcher> result = _indexReader.getReader(token);
		Log.debug(Geonet.INDEX_ENGINE,"Got index reader token "+result.one()+" index reader object: "+result.two());
		return result;
	}

	//----------------------------------------------------------------------------
	/**
	 * Return a new IndexSearcher to do search operations on.
	 * 
	 * @return
	 */
	public Pair<Long,IndexSearcher> getNewIndexSearcher() throws IOException, InterruptedException {
		Log.debug(Geonet.INDEX_ENGINE,"Ask for new searcher");
		return getIndexSearcher(-1);
	}

	//----------------------------------------------------------------------------

	public void releaseIndexSearcher(IndexSearcher searcher) throws IOException {
		Log.debug(Geonet.INDEX_ENGINE,"Closing index reader object: "+searcher);
	  _indexReader.releaseReader(searcher);
	}

	//-----------------------------------------------------------------------------
	// private methods

    /**
     *  Creates an index in directory luceneDir if not already there. 
     */
	private void setupIndex(boolean rebuild) throws Exception {
		// if rebuild forced don't check
		boolean badIndex = true;
		if (!rebuild) {
			try {
				IndexReader indexReader = IndexReader.open(FSDirectory.open(_luceneDir));
				indexReader.close();
				badIndex = false;
			} catch (Exception e) {
				Log.error(Geonet.INDEX_ENGINE,
						"Exception while opening lucene index, going to rebuild it: "
								+ e.getMessage());
			}
		}
		// if rebuild forced or bad index then rebuild index
		if (rebuild || badIndex) {
			Log.error(Geonet.INDEX_ENGINE, "Rebuilding lucene index");
			if (_spatial != null) _spatial.writer().reset();
			IndexWriter writer = new IndexWriter(FSDirectory.open(_luceneDir), _analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
			writer.close();
		}
		
    _indexReader = new LuceneIndexReaderFactory(_luceneDir);
		_indexWriter = new LuceneIndexWriterFactory(_luceneDir, _analyzer);
	}

	//----------------------------------------------------------------------------
	/*
	 *  Optimizes the Lucene index.
	 *  
	 */
	public boolean optimizeIndex() {
		try {
			_indexWriter.openWriter();
			_indexWriter.optimize();
			_indexWriter.closeWriter();
			return true;
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Exception while optimizing lucene index: "
							+ e.getMessage());
			return false;
		}
	}
	
	//----------------------------------------------------------------------------
	/*
	 *  Rebuilds the Lucene index.
	 *  
	 *  @param context
	 *  @param xlinks
	 *  
	 */
	public boolean rebuildIndex(ServiceContext context, boolean xlinks) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		try {
			if (_indexWriter.isOpen()) {
				throw new Exception("Cannot rebuild index while it is being updated - please wait till later");
			}
			if (!xlinks) {
				setupIndex(true);
				dataMan.init(context, dbms, true);
			} else {
				dataMan.rebuildIndexXLinkedMetadata(context);
			}
			return true;
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Exception while rebuilding lucene index, going to rebuild it: "
							+ e.getMessage());
			return false;
		}
	}
	
    /**
     * Creates a new document.
     *
     * @param xml
     * @return
     */
	private Document newDocument(Element xml)
	{
		Document doc = new Document();
        for (Object o : xml.getChildren()) {
            Element field = (Element) o;
            String name = field.getAttributeValue("name");
            String string = field.getAttributeValue("string"); // Lower case field is handled by Lucene Analyzer.
            if (string.trim().length() > 0) {
                String sStore = field.getAttributeValue("store");
                String sIndex = field.getAttributeValue("index");
                String sToken = field.getAttributeValue("token");
                String sNumeric = field.getAttributeValue("numeric");

                boolean bStore = sStore != null && sStore.equals("true");
                boolean bIndex = sIndex != null && sIndex.equals("true");
                boolean token = sToken != null && sToken.equals("true");
                boolean bNumberic = sNumeric != null;
                
                Field.Store store;
                if (bStore) {
                    store = Field.Store.YES;
                }
                else {
                    store = Field.Store.NO;
                }
                Field.Index index = null;
                if (bIndex && token) {
                    index = Field.Index.ANALYZED;
                }
                if (bIndex && !token) {
                    index = Field.Index.NOT_ANALYZED;
                }
                if (!bIndex) {
                    index = Field.Index.NO;
                }

                if(bNumberic) {
					NumericField nf = new NumericField(name, store, bIndex);

					if (sNumeric.equals("int")) {
						nf.setIntValue(new Integer(string).intValue());
					} else if (sNumeric.equals("long")) {
						nf.setLongValue(new Long(string).longValue());
					} else if (sNumeric.equals("float")) {
						nf.setFloatValue(new Float(string).floatValue());
					} else if (sNumeric.equals("double")) {
						nf.setDoubleValue(new Double(string).doubleValue());
					}

					doc.add(nf);

				} else {
                doc.add(new Field(name, string, store, index));
            }
        }
        }
		return doc;
	}

	//--------------------------------------------------------------------------------

	public Spatial getSpatial()
    {
        return _spatial;
    }
	
    /**
     * TODO javadoc.
     *
     */
	public class Spatial 
	{

				private final DataStore _datastore;
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
                        constructor(IsFullyOutsideOfFilter.class));
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
        private final Transaction                                       _transaction;
        private final Timer                                             _timer;
        private final Parser                                            _gmlParser;
        private final Lock                                              _lock;
        private SpatialIndexWriter                                      _writer;
        private Committer                                               _committerTask;

        /**
         * TODO javadoc.
         *
         * @param dataStore
         * @throws Exception
         */
        public Spatial(DataStore dataStore) throws Exception
        {
            _lock = new ReentrantLock();
            _datastore = dataStore;
            _transaction = new DefaultTransaction("SpatialIndexWriter");
            _timer = new Timer(true);
            _gmlParser = new Parser(new GMLConfiguration());
            boolean rebuildIndex;

            rebuildIndex = createWriter(_datastore);
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

        /**
         * TODO javadoc.
         *
         * @param datastore
         * @return
         * @throws IOException
         */
        private boolean createWriter(DataStore datastore) throws IOException
        {
            boolean rebuildIndex;
            try {
                _writer = new SpatialIndexWriter(datastore, _gmlParser,
                        _transaction, _lock);
                rebuildIndex = _writer.getFeatureSource().getSchema() == null;
            } catch (Exception e) {
								String exceptionString = Xml.getString(JeevesException.toElement(e));
                Log.warning(Geonet.SPATIAL, "Failure to make _writer, maybe a problem but might also not be an issue:"+exceptionString);
                try {
          				_writer.reset();
								} catch (Exception e1) {
          				Log.error(Geonet.SPATIAL, "Unable to call reset on Spatial writer: "+e1.getMessage());
									e1.printStackTrace();
        				}
                rebuildIndex = true;
            }
            return rebuildIndex;
        }

        /**
         * TODO javadoc.
         */
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
                        Log.error(Geonet.SPATIAL,"error writing spatial index: "+e.getMessage());
												e.printStackTrace();
                    } finally {
                        _lock.unlock();
                    }
                }
            });
        }

        /**
         * TODO javadoc.
         *
         * @param query
         * @param filterExpr
         * @param filterVersion
         * @return
         * @throws Exception
         */
        public Filter filter(org.apache.lucene.search.Query query, int numHits, Element filterExpr, String filterVersion)
                throws Exception {
            _lock.lock();
            try {
            	Parser filterParser = getFilterParser(filterVersion);
                Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> accessor = new SpatialIndexAccessor();
                return OgcGenericFilters.create(query, numHits, filterExpr, accessor , filterParser);
            }
            catch (Exception e) {
            	// TODO Handle NPE creating spatial filter (due to constraint language version).
    			throw new NoApplicableCodeEx("Error when parsing spatial filter (version: " + filterVersion + "):" +
                        Xml.getString(filterExpr) + ". Error is: " + e.toString());
            }
            finally {
                _lock.unlock();
            }
        }

        /**
         * TODO javadoc.
         *
         * @param query
         * @param geom
         * @param request
         * @return
         * @throws Exception
         */
        public SpatialFilter filter(org.apache.lucene.search.Query query, int numHits,
                Geometry geom, Element request) throws Exception {
            _lock.lock();
            try {
                String relation = Util.getParam(request,
                        Geonet.SearchResult.RELATION,
                        Geonet.SearchResult.Relation.INTERSECTION);
                return _types.get(relation).newInstance(query, numHits, geom, new SpatialIndexAccessor());
            }
            finally {
                _lock.unlock();
            }
        }

        /**
         * TODO javadoc.
         *
         * @return
         * @throws Exception
         */
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

        /**
         * TODO javadoc.
         * @return
         * @throws Exception
         */
        private SpatialIndexWriter writerNoLocking() throws Exception
        {
            if (_writer == null) {
                _writer = new SpatialIndexWriter(_datastore, _gmlParser,
                        _transaction, _lock);
            }
            return _writer;
        }
        
        /**
         * TODO javadoc.
         *
         * @param filterVersion
         * @return
         */
        private Parser getFilterParser(String filterVersion) {
			Configuration config;
			config = filterVersion.equals(Csw.FILTER_VERSION_1_0) ? FILTER_1_0_0  : FILTER_1_1_0;
			return new Parser(config);
		}

        private final class SpatialIndexAccessor
				extends
				Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> {
			@Override
			public FeatureSource<SimpleFeatureType, SimpleFeature> one() {
			    return _writer.getFeatureSource();
			}

			@Override
			public SpatialIndex two() {
			    try {
			        return _writer.getIndex();
			    } catch (IOException e) {
			        throw new RuntimeException(e);
			    }
			}
		}

		/**
         * TODO javadoc.
         *
         */
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
                    Log.error(Geonet.SPATIAL, "error writing spatial index "+e.getMessage());
                } finally {
                    _lock.unlock();
                }
            }

        }


    }

    /**
     * TODO javadoc.
     *
     * @param clazz
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     */
    private static Constructor<? extends SpatialFilter> constructor(Class<? extends SpatialFilter> clazz)
            throws SecurityException, NoSuchMethodException {
        return clazz.getConstructor(org.apache.lucene.search.Query.class, int.class, Geometry.class, Pair.class);
    }

}
