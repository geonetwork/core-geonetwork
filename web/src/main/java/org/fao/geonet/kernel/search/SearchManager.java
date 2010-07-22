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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

import jeeves.exceptions.JeevesException;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Filter;
import org.apache.lucene.store.Directory;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
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
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.spring.StringUtils;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;

import org.jdom.Element;

import org.jzkit.search.provider.iface.Searchable;

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

	private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();

	private final File     _stylesheetsDir;
	private final File     _schemasDir;
	private final Element  _summaryConfig;
	private final Element  _tokenizedFields;
	private File           _luceneDir;
	private static PerFieldAnalyzerWrapper _analyzer;
	private String         _htmlCacheDir;
	private String         _dataDir;
	private Searchable     _hssSearchable;
	private Spatial        _spatial;
	private LuceneIndexReaderFactory    _indexReader;
	private LuceneIndexWriterFactory    _indexWriter;
	private Timer					 _optimizerTimer = null;
	// minutes between optimizations of the lucene index
	private int						 _optimizerInterval;
	private Calendar       _optimizerBeginAt;
	private SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

  private boolean        _inspireEnabled = false;

	//-----------------------------------------------------------------------------
	static {
    // Define the default Analyzer
		_analyzer = new PerFieldAnalyzerWrapper(new StandardAnalyzer(new HashSet<String>()));
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
		
		// heikki doeleman: UUID must be case insensitive, as its parts are hexadecimal numbers which
		// are not case sensitive.
		_analyzer.addAnalyzer("_uuid", new StandardAnalyzer(new HashSet<String>()));
		_analyzer.addAnalyzer("parentUuid", new StandardAnalyzer(new HashSet<String>()));
		_analyzer.addAnalyzer("operatesOn", new StandardAnalyzer(new HashSet<String>()));
		_analyzer.addAnalyzer("subject", new KeywordAnalyzer());
	}

	//-----------------------------------------------------------------------------

	public static PerFieldAnalyzerWrapper getAnalyzer() {
		return _analyzer;
	}

	//-----------------------------------------------------------------------------

	/**
	 * @param appPath
	 * @param luceneDir
	 * @param summaryConfigXmlFile
     * @param guiConfigXmlFile
	 * @param dataStore
	 * @throws Exception
	 */
	public SearchManager(String appPath, String luceneDir, String htmlCacheDir, String dataDir, String summaryConfigXmlFile,  String guiConfigXmlFile, String luceneConfigXmlFile, DataStore dataStore, SettingInfo si) throws Exception
	{
		_summaryConfig = Xml.loadStream(new FileInputStream(new File(appPath,summaryConfigXmlFile)));

		Element luceneConfig = Xml.loadStream(new FileInputStream(new File(appPath,luceneConfigXmlFile)));
		_tokenizedFields = luceneConfig.getChild("tokenized");

		_stylesheetsDir = new File(appPath, SEARCH_STYLESHEETS_DIR_PATH);
		_schemasDir     = new File(appPath, SCHEMA_STYLESHEETS_DIR_PATH);

        checkInspireConfig(appPath, guiConfigXmlFile);

		if (!_stylesheetsDir.isDirectory())
			throw new Exception("directory " + _stylesheetsDir + " not found");

		File htmlCacheDirTest   = new File(appPath, htmlCacheDir);
		if (!htmlCacheDirTest.isDirectory())
			throw new IllegalArgumentException("directory " + htmlCacheDir + " not found");
		_htmlCacheDir = htmlCacheDir;
		_dataDir        = dataDir;

		
		_luceneDir = new File(luceneDir+ "/nonspatial");
		
		if (!_luceneDir.isAbsolute()) _luceneDir = new File(appPath + luceneDir+ "/nonspatial");

     _luceneDir.getParentFile().mkdirs();
        
     _spatial = new Spatial(dataStore);

		initLucene(appPath, luceneDir);
		initZ3950(appPath);

		if (si.getLuceneIndexOptimizerSchedulerEnabled()) {
      _optimizerBeginAt  = si.getLuceneIndexOptimizerSchedulerAt();
      _optimizerInterval = si.getLuceneIndexOptimizerSchedulerInterval();
      scheduleOptimizerThread();
    } else {
      Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index is disabled");
    }	
	}

	//-----------------------------------------------------------------------------

	public void end() throws Exception
	{
		endZ3950();
		_optimizerTimer.cancel();
	}

	//-----------------------------------------------------------------------------

	public synchronized void disableOptimizer() throws Exception
  {
    Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index is disabled");
    if (_optimizerTimer != null) {
      _optimizerTimer.cancel();
    }
  }

	//-----------------------------------------------------------------------------
	
	public synchronized void rescheduleOptimizer(Calendar optimizerBeginAt, int optimizerInterval) throws Exception
	{
		if (_dateFormat.format(optimizerBeginAt.getTime()).equals(_dateFormat.format(_optimizerBeginAt.getTime())) && (optimizerInterval == _optimizerInterval)) return; // do nothing unless at and interval has changed

		_optimizerInterval = optimizerInterval;
		_optimizerBeginAt  = optimizerBeginAt;
		if (_optimizerTimer != null) _optimizerTimer.cancel();
		scheduleOptimizerThread();
	}

	//-----------------------------------------------------------------------------

	private void scheduleOptimizerThread() throws Exception {
		_optimizerTimer = new Timer(true);

		// at _optimizerBeginAt and again at every _optimizerInterval minutes
		Date beginAt = getBeginAt(_optimizerBeginAt);
		_optimizerTimer.schedule(new OptimizeTask(), beginAt, _optimizerInterval * 60 * 1000); 

		Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index to run at "+_dateFormat.format(beginAt)+" and every "+_optimizerInterval+" minutes afterwards");
	}

	//-----------------------------------------------------------------------------

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

	public MetaSearcher newSearcher(int type, String stylesheetName)
		throws Exception
	{
		switch (type)
		{
			case LUCENE: return new LuceneSearcher(this, stylesheetName, _summaryConfig, _tokenizedFields);
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
		setupIndex(false); 
	}

	//-----------------------------------------------------------------------------
	// Z39.50 init/end methods

	/**
         * Initializes the Z3950 client searcher.
	 * @param appPath
	 * @throws Exception
	 */
	private void initZ3950(String appPath) {}

	/**
	 * deinitializes the Z3950 client searcher
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
	 * @param type
	 * @param metadata
	 * @param id
	 * @param moreFields
	 * @param isTemplate
	 * @param title
	 * @throws Exception
	 */
	public void index(String type, Element metadata, String id, List moreFields, String isTemplate, String title) throws Exception 
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

	public void startIndexGroup() throws Exception {
		Log.debug(Geonet.INDEX_ENGINE, "Opening Writer from startIndexGroup");
		_indexWriter.openWriter();
	}

	public void indexGroup(String type, Element metadata, String id, List moreFields, String isTemplate, String title) throws Exception 
	{
		Document doc = buildIndexDocument(type, metadata, id, moreFields, isTemplate, title, true);
		_indexWriter.addDocument(doc);

		_spatial.writer().index(_schemasDir.getPath(), type, id, metadata);
	}

	public void endIndexGroup() throws Exception {
		Log.debug(Geonet.INDEX_ENGINE, "Closing Writer from endIndexGroup");
		_indexWriter.closeWriter();
	}

	public void deleteGroup(String fld, String txt) throws Exception {
		// possibly remove old document
		Log.debug(Geonet.INDEX_ENGINE,"Deleting document ");
		_indexWriter.deleteDocuments(new Term(fld, txt));

		_spatial.writer().delete(txt);
	}

	public void commitIndexChanges() throws Exception {
		Log.debug(Geonet.INDEX_ENGINE,"Committing index changes");
		_indexWriter.commit();
	}

	private Document buildIndexDocument(String type, Element metadata, String id, List moreFields, String isTemplate, String title, boolean group) throws Exception 
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
		} else {
			Log.debug(Geonet.INDEX_ENGINE, "Metadata to index:\n"
					+ Xml.getString(metadata));

			String mdDataDir = Lib.resource.getDir(_dataDir, Params.Access.PRIVATE, id);
			xmlDoc = getIndexFields(type, metadata, mdDataDir);

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
		return doc;	
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

	public ArrayList<Integer> getDocsWithXLinks() throws Exception
	{
		IndexReader reader = getIndexReader();

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
			releaseIndexReader(reader);
		}
	}

	//--------------------------------------------------------------------------------

	public HashMap<String,String> getDocsChangeDate() throws Exception
	{
		IndexReader reader = getIndexReader();

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
				Hashtable record = new Hashtable();
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
			releaseIndexReader(reader);
		}
	
	}

	/**
	 * Browse the index and return all values for the Lucene field.
	 * 
	 * @param fld	The Lucene field name 
	 * @return	The list of values for the field
	 * @throws Exception
	 */
	public Vector getTerms(String fld) throws Exception
	{
		Vector terms = new Vector();

		IndexReader reader = getIndexReader();

		try {
			TermEnum enu = reader.terms(new Term(fld, ""));
			while (enu.next())
			{
				Term term = enu.term();
				if (term.field().equals(fld))
					terms.add(enu.term().text());
			}
			return terms;
		} finally {
			releaseIndexReader(reader);
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
		IndexReader reader = getIndexReader();
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
			releaseIndexReader(reader);
		}
		return termList;
	}
	
	
	/**
	 * Frequence of terms
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

	Element getIndexFields(String schema, Element xml, String mdDataDir) throws Exception {
		File schemaDir = new File(_schemasDir, schema);

		try {
			String styleSheet = new File(schemaDir, "index-fields.xsl")
					.getAbsolutePath();
            Map<String,String> params = new HashMap();
            params.put("inspire", new Boolean(_inspireEnabled).toString());

			return Xml.transform(xml, styleSheet, params);
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
					"Indexing stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

    private void checkInspireConfig(String appPath, String guiConfigXmlFile) {
        try {
            Element  guiConfig = Xml.loadStream(new FileInputStream(new File(appPath, guiConfigXmlFile)));

            String inspireParam = guiConfig.getChild("inspire").getValue();
            if (StringUtils.hasText(inspireParam)) {
               _inspireEnabled = (inspireParam.equals("1"));
            } else {
                _inspireEnabled = false;
            }
        } catch (Exception ex) {
            // If not defined the parameter, then inspire is disabled
            _inspireEnabled = false;
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
			Log.error(Geonet.INDEX_ENGINE,
					"Search stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

	public File getLuceneDir() 
	{
		return _luceneDir;
	}

	//-----------------------------------------------------------------------------
	/**
	 * Return a reopened index reader to do operations on
	 * an up-to-date index.
	 * 
	 * @return
	 */

	public IndexReader getIndexReader() throws InterruptedException, CorruptIndexException, IOException {
		return _indexReader.getReader();
	}

	//-----------------------------------------------------------------------------
	
	public void releaseIndexReader(IndexReader reader) throws IOException {
		_indexReader.releaseReader(reader);
	}

	//-----------------------------------------------------------------------------
	
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
		if (!rebuild) {
			try {
				IndexReader indexReader = IndexReader.open(_luceneDir);
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
			IndexWriter writer = new IndexWriter(_luceneDir, _analyzer, true);
			writer.close();
		}
		
    _indexReader = new LuceneIndexReaderFactory(_luceneDir);
		_indexWriter = new LuceneIndexWriterFactory(_luceneDir, _analyzer);
	}

	//----------------------------------------------------------------------------
	/*
	 *  Optimize the Lucene index
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
	 *  Rebuild the Lucene index
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
				dataMan.rebuildIndexXLinkedMetadata(context, dbms);
			}
			return true;
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE,
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

	public Spatial getSpatial()
    {
        return _spatial;
    }
	
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
        private final Transaction                                       _transaction;
        private final Timer                                             _timer;
        private final Parser                                            _gmlParser;
        private final Lock                                              _lock;
        private SpatialIndexWriter                                      _writer;
        private Committer                                               _committerTask;

        public Spatial(DataStore dataStore) throws Exception
        {
            _lock = new ReentrantLock();
            _datastore = dataStore;
            _transaction = new DefaultTransaction("SpatialIndexWriter");
            _timer = new Timer(true);
            _gmlParser = new Parser(new GMLConfiguration());
            boolean rebuildIndex = false;

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
            } catch (Exception e) {
            	// TODO Handle NPE creating spatial filter (due to constraint language version). 
    			throw new NoApplicableCodeEx("Error when parsing spatial filter (version: " + 
            			filterVersion + "):" + Xml.getString(filterExpr) + ". Error is: " + e.toString());
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
                _writer = new SpatialIndexWriter(_datastore, _gmlParser,
                        _transaction, _lock);
            }
            return _writer;
        }
        
        private Parser getFilterParser(String filterVersion) {
			Configuration config;
			config = filterVersion.equals(Csw.FILTER_VERSION_1_0) ? FILTER_1_0_0  : FILTER_1_1_0;
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
                    Log.error(Geonet.SPATIAL, "error writing spatial index "+e.getMessage());
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

