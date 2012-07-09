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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;

import jeeves.exceptions.JeevesException;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ConfigurationOverrides;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Filter;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.function.DocumentBoosting;
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
import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Transaction;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.jdom.Content;
import org.jdom.Element;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;

/**
 * Indexes metadata using Lucene.
 */
public class SearchManager {
	public static final int LUCENE = 1;
	public static final int Z3950 = 2;
	public static final int UNUSED = 3;

    public static final String NON_SPATIAL_DIR = "/nonspatial";
    
	private static final String SEARCH_STYLESHEETS_DIR_PATH = "xml/search";
    private static final String STOPWORDS_DIR_PATH = "resources/stopwords";

	private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();

	private final File _stylesheetsDir;
    private static File _stopwordsDir;
	private final Element _summaryConfig;
	private LuceneConfig _luceneConfig;
	private File _luceneDir;
    private SettingInfo _settingInfo;
    /**
     * Used when adding documents to the Lucene index.
     */
	private static PerFieldAnalyzerWrapper _analyzer;
	/**
     * Used when searching to analyze query terms.
     */
	private static PerFieldAnalyzerWrapper _searchAnalyzer;

    /**
     * Maps languages to Analyzer that contains GeoNetworkAnalyzers initialized with stopwords for this language.
     */
    private static Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
    private static Map<String, Analyzer> searchAnalyzerMap = new HashMap<String, Analyzer>();
    
	private static DocumentBoosting _documentBoostClass;
	private String _luceneTermsToExclude;
	private boolean _logSpatialObject;
	private SchemaManager _scm;
	private static PerFieldAnalyzerWrapper _defaultAnalyzer;
	private String _htmlCacheDir;
    private Spatial _spatial;
	private LuceneIndexReaderFactory _indexReader;
	private LuceneIndexWriterFactory _indexWriter;

    private boolean _inspireEnabled = false;
    private String _thesauriDir;
	private boolean _logAsynch;
	private final LuceneOptimizerManager _luceneOptimizerManager;


    public SettingInfo get_settingInfo() {
        return _settingInfo;
    }

    public void setInspireEnabled(boolean inspireEnabled) {
        this._inspireEnabled = inspireEnabled;
    }

    /**
     * Creates GeoNetworkAnalyzer, using Admin-defined stopwords if there are any.
     *
     * @param stopwords
     * @return
     */
    private static Analyzer createGeoNetworkAnalyzer(Set<String> stopwords) {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Creating GeoNetworkAnalyzer");
        return new GeoNetworkAnalyzer(stopwords);
    }

    /**
     * Creates a default- (hardcoded) configured PerFieldAnalyzerWrapper.
     *
     * @param stopwords
     * @return
     */
	private static PerFieldAnalyzerWrapper createHardCodedPerFieldAnalyzerWrapper(Set<String> stopwords) {
        PerFieldAnalyzerWrapper pfaw;
        Analyzer geoNetworkAnalyzer = SearchManager.createGeoNetworkAnalyzer(stopwords);
		pfaw = new PerFieldAnalyzerWrapper(geoNetworkAnalyzer);
		pfaw.addAnalyzer(LuceneIndexField.UUID, new GeoNetworkAnalyzer());
		pfaw.addAnalyzer(LuceneIndexField.PARENTUUID, new GeoNetworkAnalyzer());
		pfaw.addAnalyzer(LuceneIndexField.OPERATESON, new GeoNetworkAnalyzer());
		pfaw.addAnalyzer(LuceneIndexField.SUBJECT, new KeywordAnalyzer());
        return pfaw;
	}

	/**
	 *
	 * @return	The current analyzer used by the search manager.
	 */
	public static PerFieldAnalyzerWrapper getAnalyzer() {
		return _analyzer;
	}
	public static PerFieldAnalyzerWrapper getSearchAnalyzer() {
		return _searchAnalyzer;
	}
    /**
     * Retrieve per field analyzer according to language and for searching or indexing time.
     * 
     * @param language Language for the analyzer.
     * @param forSearching true to return searching time analyzer, false for indexing time analayzer.
     * @return 
     */
    public static PerFieldAnalyzerWrapper getAnalyzer(String language, boolean forSearching) {
        if(Log.isDebugEnabled(Geonet.LUCENE)) {
            Log.debug(Geonet.LUCENE, "Get analyzer for searching: " + forSearching + " and language: " + language);
        }
        
        Map<String, Analyzer> map = forSearching ? searchAnalyzerMap : analyzerMap;
        
        PerFieldAnalyzerWrapper analyzer = (PerFieldAnalyzerWrapper)map.get(language); 
        if(analyzer != null) {
            return analyzer;
        } else {
            if(Log.isDebugEnabled(Geonet.LUCENE)) {
                Log.debug(Geonet.LUCENE, "Returning default analyzer.");
            }
            return forSearching ? _searchAnalyzer : _analyzer;
        }
    }

	/**
	 * Returns a default- (hardcoded) configured PerFieldAnalyzerWrapper, creating it if necessary.
     *
	 */
	private static void initHardCodedAnalyzers() {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "initializing hardcoded analyzers");
        // no default analyzer instantiated: create one
        if(_defaultAnalyzer == null) {
            // create hardcoded default PerFieldAnalyzerWrapper w/o stopwords
            _defaultAnalyzer = SearchManager.createHardCodedPerFieldAnalyzerWrapper(null);
        }
        if(!_stopwordsDir.exists() || !_stopwordsDir.isDirectory()) {
            Log.warning(Geonet.SEARCH_ENGINE, "Invalid stopwords directory " + _stopwordsDir.getAbsolutePath() + ", not using any stopwords.");
        }
        else {
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "loading stopwords");
            for(File stopwordsFile : _stopwordsDir.listFiles()) {
                String language = stopwordsFile.getName().substring(0, stopwordsFile.getName().indexOf('.'));
                if(language.length() != 2) {
                    Log.warning(Geonet.LUCENE, "invalid iso 639-1 code for language: " + language);
                }
                // look up stopwords for that language
                Set<String> stopwordsForLanguage = StopwordFileParser.parse(stopwordsFile.getAbsolutePath());
                if(stopwordsForLanguage != null) {
                    if(Log.isDebugEnabled(Geonet.LUCENE))
                        Log.debug(Geonet.LUCENE, "loaded # " + stopwordsForLanguage.size() + " stopwords for language " + language);
                    Analyzer languageAnalyzer = SearchManager.createHardCodedPerFieldAnalyzerWrapper(stopwordsForLanguage);
                    analyzerMap.put(language, languageAnalyzer);
                }
                else {
                    if(Log.isDebugEnabled(Geonet.LUCENE))
                        Log.debug(Geonet.LUCENE, "failed to load any stopwords for language " + language);
                }
            }
        }        
	}

    /**
     * Creates an analyzer based on its definition in the Lucene config.
     *
     * @param analyzerClassName Class name of analyzer to create
     * @param field The Lucene field this analyzer is created for
     * @param stopwords Set stop words if analyzer class name equal org.fao.geonet.kernel.search.GeoNetworkAnalyzer.
     * @return
     */
    private Analyzer createAnalyzerFromLuceneConfig(String analyzerClassName, String field, Set<String> stopwords) {
        Analyzer analyzer = null;
        try {
            if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Creating analyzer defined in Lucene config:" + analyzerClassName);
            // GNA analyzer
            if(analyzerClassName.equals("org.fao.geonet.kernel.search.GeoNetworkAnalyzer")) {
                analyzer = SearchManager.createGeoNetworkAnalyzer(stopwords);
            }
            // non-GNA analyzer
            else {
                try {
                    @SuppressWarnings("unchecked")
					Class<? extends Analyzer> analyzerClass = (Class<? extends Analyzer>) Class.forName(analyzerClassName);
                    Class<?>[] clTypesArray = _luceneConfig.getAnalyzerParameterClass((field==null?"":field) + analyzerClassName);
                    Object[] inParamsArray = _luceneConfig.getAnalyzerParameter((field==null?"":field) + analyzerClassName);
                    try {
                        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                            Log.debug(Geonet.SEARCH_ENGINE, " Creating analyzer with parameter");
                        Constructor<? extends Analyzer> c = analyzerClass.getConstructor(clTypesArray);
                        analyzer = c.newInstance(inParamsArray);
                    }
                    catch (Exception x) {
                        Log.warning(Geonet.SEARCH_ENGINE, "   Failed to create analyzer with parameter: " + x.getMessage());
                        x.printStackTrace();
                        // Try using a default constructor without parameter
                        Log.warning(Geonet.SEARCH_ENGINE, "   Now trying without parameter");
                        analyzer = analyzerClass.newInstance();
                    }
                }
                catch (Exception y) {
                    Log.warning(Geonet.SEARCH_ENGINE, "Failed to create analyzer as specified in lucene config, default analyzer will be used for field " + field + ". Exception message is: " + y.getMessage());
                    y.printStackTrace();
                    // abandon and continue with next field defined in lucene config
                }
            }
        }
        catch (Exception z) {
            Log.warning(Geonet.SEARCH_ENGINE, " Error on analyzer initialization: " + z.getMessage() + ". Check your Lucene configuration. Hardcoded default analyzer will be used for field " + field);
            z.printStackTrace();
        }
        finally {
            // creation of analyzer has failed, default to GeoNetworkAnalyzer
            if(analyzer == null) {
                Log.warning(Geonet.SEARCH_ENGINE, "Creating analyzer has failed, defaulting to GeoNetworkAnalyzer");
                analyzer = SearchManager.createGeoNetworkAnalyzer(stopwords);
            }
        }
        return analyzer;
    }

	/**
	 * Creates analyzers. An analyzer is created for each language that has a stopwords file, and a stopword-less
     * default analyzer is also created.
	 *
     * If no analyzer class is specified in Lucene configuration, a default hardcoded PerFieldAnalyzer is created
     * (@see #initHardCodedAnalyzers()).
     *
	 * If an error occurs instantiating an analyzer, GeoNetworkAnalyzer is used.
	 */
	public void createAnalyzer() {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "createAnalyzer start");
		String defaultAnalyzerClass = _luceneConfig.getDefaultAnalyzerClass();
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "defaultAnalyzer defined in Lucene config: " + defaultAnalyzerClass);
        // there is no default analyzer defined in lucene config
		if (defaultAnalyzerClass == null) {
            // create default (hardcoded) analyzer
            SearchManager.initHardCodedAnalyzers();
		}
        // there is an analyzer defined in lucene config
        else {

            if(!_stopwordsDir.exists() || !_stopwordsDir.isDirectory()) {
                Log.warning(Geonet.SEARCH_ENGINE, "Invalid stopwords directory " + _stopwordsDir.getAbsolutePath() + ", not using any stopwords.");
            }
            else {
                if(Log.isDebugEnabled(Geonet.LUCENE))
                    Log.debug(Geonet.LUCENE, "Loading stopwords and creating per field anlayzer ...");
                
                // One per field analyzer is created for each stopword list available using GNA as default analyzer
                // Configuration can't define different analyzer per language.
                // TODO : http://trac.osgeo.org/geonetwork/ticket/900
                for(File stopwordsFile : _stopwordsDir.listFiles()) {
                    String language = stopwordsFile.getName().substring(0, stopwordsFile.getName().indexOf('.'));
                    // TODO check for valid ISO 639-2 codes could be better than this
                    if(language.length() != 3) {
                        Log.warning(Geonet.LUCENE, "Stopwords file with incorrect ISO 639-2 language as filename: " + language);
                    }
                    // look up stopwords for that language
                    Set<String> stopwordsForLanguage = StopwordFileParser.parse(stopwordsFile.getAbsolutePath());
                    if(stopwordsForLanguage != null) {
                        if(Log.isDebugEnabled(Geonet.LUCENE))
                            Log.debug(Geonet.LUCENE, "Loaded # " + stopwordsForLanguage.size() + " stopwords for language " + language);

                        // Configure per field analyzer and register them to language map of pfa
                        // ... for indexing
                        configurePerFieldAnalyzerWrapper(defaultAnalyzerClass, _luceneConfig.getFieldSpecificAnalyzers(),
                                analyzerMap, language, stopwordsForLanguage);
                        
                        // ... for searching
                        configurePerFieldAnalyzerWrapper(defaultAnalyzerClass, _luceneConfig.getFieldSpecificSearchAnalyzers(),
                                searchAnalyzerMap, language, stopwordsForLanguage);

                    }
                    else {
                        if(Log.isDebugEnabled(Geonet.LUCENE))
                            Log.debug(Geonet.LUCENE, "Failed to load any stopwords for language " + language);
                    }
                }
            }
            
            // Configure default per field analyzer
            _analyzer = configurePerFieldAnalyzerWrapper(defaultAnalyzerClass, _luceneConfig.getFieldSpecificAnalyzers(),
                null, null, null);
            
            _searchAnalyzer = configurePerFieldAnalyzerWrapper(defaultAnalyzerClass, _luceneConfig.getFieldSpecificSearchAnalyzers(),
                null, null, null);
        }
    }

	/**
	 * Create, configure and optionnaly register in a list a per field analyzer wrapper.
	 * 
	 * @param defaultAnalyzer The default analyzer to use
	 * @param fieldAnalyzers	The list of extra analyzer per field
	 * @param referenceMap	A map where to reference the per field analyzer
	 * @param referenceKey	The reference key
	 * @param stopwordsForLanguage	The stopwords to use (only for GNA)
	 * 
	 * @return The per field analyzer wrapper
	 */
	private PerFieldAnalyzerWrapper configurePerFieldAnalyzerWrapper(
			String defaultAnalyzerClass, Map<String, String> fieldAnalyzers,
			Map<String, Analyzer> referenceMap, String referenceKey,
			Set<String> stopwordsForLanguage) {

		// Create the default analyzer according to Lucene config
		if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
			Log.debug(Geonet.SEARCH_ENGINE, " Default analyzer class: " + defaultAnalyzerClass);
		Analyzer defaultAnalyzer = createAnalyzerFromLuceneConfig(
				defaultAnalyzerClass, null, stopwordsForLanguage);
		
		
		PerFieldAnalyzerWrapper pfa = new PerFieldAnalyzerWrapper(
				defaultAnalyzer);
		
		
		// now handle the exceptions for each field to the default analyzer as
		// defined in lucene config
		for (Entry<String, String> e : fieldAnalyzers.entrySet()) {
			String field = e.getKey();
			String aClassName = e.getValue();
			if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
				Log.debug(Geonet.SEARCH_ENGINE, " Add analyzer for field: "
						+ field + "=" + aClassName);
			Analyzer analyzer = createAnalyzerFromLuceneConfig(aClassName,
					field, stopwordsForLanguage);
			pfa.addAnalyzer(field, analyzer);
		}
		
		// Register to a reference map if needed
		if (referenceMap != null) {
			referenceMap.put(referenceKey, pfa);
		}
		
		return pfa;
	}

    /**
     * TODO javadoc.
     *
     * @param appPath
     * @param luceneDir
     * @param htmlCacheDir
     * @param thesauriDir
     * @param summaryConfigXmlFile
     * @param lc
     * @param logAsynch
     * @param logSpatialObject
     * @param luceneTermsToExclude
     * @param dataStore
     * @param maxWritesInTransaction
     * @param si
     * @param scm
     * @param servletContext
     * @throws Exception
     */
	public SearchManager(String appPath, String luceneDir, String htmlCacheDir, String thesauriDir,
                         String summaryConfigXmlFile, LuceneConfig lc,  boolean logAsynch, boolean logSpatialObject,
                         String luceneTermsToExclude, DataStore dataStore, int maxWritesInTransaction, SettingInfo si,
                         SchemaManager scm, ServletContext servletContext) throws Exception {
		_scm = scm;
		_thesauriDir = thesauriDir;
		_summaryConfig = Xml.loadStream(new FileInputStream(new File(appPath,summaryConfigXmlFile)));

		if (servletContext != null) {
			ConfigurationOverrides.updateWithOverrides(summaryConfigXmlFile, servletContext, appPath, _summaryConfig);
		}

		_luceneConfig = lc;
        _settingInfo = si;

		_stylesheetsDir = new File(appPath, SEARCH_STYLESHEETS_DIR_PATH);
        _stopwordsDir = new File(appPath + STOPWORDS_DIR_PATH);

        _inspireEnabled = si.getInspireEnabled();
        createAnalyzer();
        createDocumentBoost();
        
		if (!_stylesheetsDir.isDirectory()) {
            throw new Exception("directory " + _stylesheetsDir + " not found");
        }

		File htmlCacheDirTest   = new File(htmlCacheDir);
		if (!htmlCacheDirTest.isDirectory() && !htmlCacheDirTest.mkdirs()) {
            throw new IllegalArgumentException("directory " + htmlCacheDir + " not found");
        }
		_htmlCacheDir = htmlCacheDir;


		_luceneDir = new File(luceneDir + NON_SPATIAL_DIR);

		if (!_luceneDir.isAbsolute()) {
            _luceneDir = new File(luceneDir+ NON_SPATIAL_DIR);
        }

        _luceneDir.getParentFile().mkdirs();
        _spatial = new Spatial(dataStore, maxWritesInTransaction);

     	 _logAsynch = logAsynch;
		 _logSpatialObject = logSpatialObject;
		 _luceneTermsToExclude = luceneTermsToExclude;

		initLucene();
		initZ3950();
		
		_luceneOptimizerManager = new LuceneOptimizerManager(this, si);
	}

    /**
     * TODO javadoc.
     */
	private void createDocumentBoost() {
	    String className = _luceneConfig.getDocumentBoostClass();
	    if (className != null) {
    	    try {
                @SuppressWarnings(value = "unchecked")
    	        Class<? extends DocumentBoosting> clazz = (Class<? extends DocumentBoosting>) Class.forName(className);
                Class<?>[] clTypesArray = _luceneConfig.getDocumentBoostParameterClass();
                Object[] inParamsArray = _luceneConfig.getDocumentBoostParameter();
                try {
                    if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                        Log.debug(Geonet.SEARCH_ENGINE, " Creating document boost object with parameter");
                    Constructor<? extends DocumentBoosting> c = clazz.getConstructor(clTypesArray);
                    _documentBoostClass = c.newInstance(inParamsArray);
                }
                catch (Exception x) {
                    Log.warning(Geonet.SEARCH_ENGINE, "   Failed to create document boost object with parameter: " + x.getMessage());
                    x.printStackTrace();
                    // Try using a default constructor without parameter
                    Log.warning(Geonet.SEARCH_ENGINE, "   Now trying without parameter");
                    _documentBoostClass = clazz.newInstance();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reload Lucene configuration: update analyzer.
     * @param lc
     */
	public void reloadLuceneConfiguration (LuceneConfig lc) {
		_luceneConfig = lc;
		createAnalyzer();
		createDocumentBoost();
	}

	public LuceneConfig getCurrentLuceneConfiguration () {
		return _luceneConfig;
	}

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public void end() throws Exception {
		endZ3950();
		_spatial.end();
		_luceneOptimizerManager.cancel();
	}

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public synchronized void disableOptimizer() throws Exception {
        Log.info(Geonet.INDEX_ENGINE, "Scheduling thread that optimizes lucene index is disabled");
		_luceneOptimizerManager.cancel();
    }

    /**
     * TODO javadoc.
     *
     * @param optimizerBeginAt
     * @param optimizerInterval
     * @throws Exception
     */
	public synchronized void rescheduleOptimizer(Calendar optimizerBeginAt, int optimizerInterval) throws Exception {
		_luceneOptimizerManager.reschedule(optimizerBeginAt, optimizerInterval);
	}

    /**
     * TODO javadoc.
     *
     * @param type
     * @param stylesheetName
     * @return
     * @throws Exception
     */
	public MetaSearcher newSearcher(int type, String stylesheetName) throws Exception {
		switch (type) {
			case LUCENE: return new LuceneSearcher(this, stylesheetName, _summaryConfig, _luceneConfig);
			case Z3950: return new Z3950Searcher(this, _scm, stylesheetName);
			case UNUSED: return new UnusedSearcher();
			default: throw new Exception("unknown MetaSearcher type: " + type);
		}
	}

	/**
	 * Lucene init/end methods. Creates the Lucene index directory.
	 * @throws Exception
	 */
	private void initLucene() throws Exception {
		setupIndex(false);
	}

	// Z39.50 init/end methods

	/**
     * Initializes the Z3950 client searcher.
	 */
	private void initZ3950() {}

	/**
	 * deinitializes the Z3950 client searcher.
	 */
	private void endZ3950() {}

	public String getHtmlCacheDir() {
		return _htmlCacheDir;
	}

	public boolean getLogAsynch() {
		return _logAsynch;
	}

	public boolean getLogSpatialObject() {
		return _logSpatialObject;
	}

	public String getLuceneTermsToExclude() {
		return _luceneTermsToExclude;
	}

	// indexing methods

	/**
	 * Indexes a metadata record.
     *
	 * @param schemaDir
	 * @param metadata
	 * @param id
	 * @param moreFields
	 * @param isTemplate
	 * @param title
	 * @throws Exception
	 */
	public void index(String schemaDir, Element metadata, String id, List<Element> moreFields, String isTemplate,
                      String title) throws Exception {
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "indexing metadata, opening Writer from index");
		_indexWriter.openWriter();
		try {
            List<Pair<String, Document>> docs = buildIndexDocument(schemaDir, metadata, id, moreFields, isTemplate, title, false);
            for( Pair<String, Document> document : docs ) {
                _indexWriter.addDocument(document.one(), document.two());
                if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                    Log.debug(Geonet.INDEX_ENGINE, "adding document in locale " + document.one());
            }
		}
        finally {
            if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Closing Writer from index");
			_indexWriter.closeWriter();
		}
		_spatial.writer().index(schemaDir, id, metadata);
	}

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public void startIndexGroup() throws Exception {
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Opening Writer from startIndexGroup");
		_indexWriter.openWriter();
	}

    /**
     * TODO javadoc.
     *
     * @param schemaDir
     * @param metadata
     * @param id
     * @param moreFields
     * @param isTemplate
     * @param title
     * @throws Exception
     */
	public void indexGroup(String schemaDir, Element metadata, String id, List<Element> moreFields, String isTemplate,
                           String title) throws Exception {
        List<Pair<String, Document>> docs = buildIndexDocument(schemaDir, metadata, id, moreFields, isTemplate, title,
                true);
        for( Pair<String, Document> document : docs ) {
            _indexWriter.addDocument(document.one(), document.two());
        }
        _spatial.writer().index(schemaDir, id, metadata);
	}

    /**
     * TODO javadoc.
     *
     * @throws Exception
     */
	public void endIndexGroup() throws Exception {
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
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
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE,"Deleting document ");
		_indexWriter.deleteDocuments(new Term(fld, txt));

		_spatial.writer().delete(txt);
	}

    /**
     * TODO javadoc.
     *
     * @param schemaDir
     * @param metadata
     * @param id
     * @param moreFields
     * @param isTemplate
     * @param title
     * @param group
     * @return
     * @throws Exception
     */
	private List<Pair<String,Document>> buildIndexDocument(String schemaDir, Element metadata, String id,
                                                           List<Element> moreFields, String isTemplate, String title,
                                                           boolean group) throws Exception {

        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Deleting "+id+" from index");
		if (group) deleteGroup("_id", id);
		else delete("_id", id);
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Finished Delete");

		Element xmlDoc;

		// check for subtemplates
		if (isTemplate.equals("s")) {
			// create empty document with only title and "any" fields
			xmlDoc = new Element("Document");

			Element defaultDoc = new Element("Document");
            defaultDoc.setAttribute(Geonet.LUCENE_LOCALE_KEY, Geonet.DEFAULT_LANGUAGE);
            xmlDoc.addContent(defaultDoc);

           StringBuilder sb = new StringBuilder();
			allText(metadata, sb);
			SearchManager.addField(xmlDoc, LuceneIndexField.TITLE, title, true, true);
			SearchManager.addField(xmlDoc, LuceneIndexField.ANY, sb.toString(), true, true);
		}
        else {
            if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Metadata to index:\n" + Xml.getString(metadata));

            xmlDoc = getIndexFields(schemaDir, metadata);

            if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Indexing fields:\n" + Xml.getString(xmlDoc));
		}

        @SuppressWarnings(value = "unchecked")
        List<Element> documentElements = xmlDoc.getContent();

        List<Pair<String, Document>> documents = new ArrayList<Pair<String, Document>>();
        for( Element doc : documentElements ) {
            // add _id field
            SearchManager.addField(doc, LuceneIndexField.ID, id, true, true);

            // add more fields
            for( Element moreField : moreFields ) {
                doc.addContent((Content) moreField.clone());
            }
            String locale = doc.getAttributeValue("locale");
            if(locale == null || locale.trim().isEmpty()) {
                locale = Geonet.DEFAULT_LANGUAGE;
            }
            documents.add(Pair.read(locale, newDocument(doc)));
        }
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Lucene document:\n" + Xml.getString(xmlDoc));
        return documents;
	}

	/**
	 * Creates a new XML field for the Lucene index and add it to the document.
     *
	 * @param xmlDoc
	 * @param name
	 * @param value
	 * @param store
	 * @param index
	 */
	private static void addField(Element xmlDoc, String name, String value, boolean store, boolean index) {
		Element field = makeField(name, value, store, index);
		xmlDoc.addContent(field);
	}

    /**
    * Creates a new XML field for the Lucene index.
    * 
    * @param name
    * @param value
    * @param store
    * @param index
    * @return
    */
	public static Element makeField(String name, String value, boolean store, boolean index) {
		Element field = new Element("Field");
		field.setAttribute(LuceneFieldAttribute.NAME.toString(), name);
		field.setAttribute(LuceneFieldAttribute.STRING.toString(), value);
		field.setAttribute(LuceneFieldAttribute.STORE.toString(), Boolean.toString(store));
		field.setAttribute(LuceneFieldAttribute.INDEX.toString(), Boolean.toString(index));
		return field;
	}
    private enum LuceneFieldAttribute {
        NAME {
            @Override
            public String toString() {
                return "name";
            }
        },
        STRING  {
            @Override
            public String toString() {
                return "string";
            }
        },
        STORE  {
            @Override
            public String toString() {
                return "store";
            }
        },
        INDEX  {
            @Override
            public String toString() {
                return "index";
            }
        }
    }
	/**
	 * Extracts text from metadata record.
	 *
	 *
     * @param metadata
     * @param sb
     * @return all text in the metadata elements for indexing
	 */
	private void allText(Element metadata, StringBuilder sb) {
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

    /**
     *  deletes a document.
     *
     * @param fld
     * @param txt
     * @throws Exception
     */
	public void delete(String fld, String txt) throws Exception {
		// possibly remove old document
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Opening Writer from delete");
		_indexWriter.openWriter();
		try {
			_indexWriter.deleteDocuments(new Term(fld, txt));
		}
        finally {
            if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                Log.debug(Geonet.INDEX_ENGINE, "Closing Writer from delete");
			_indexWriter.closeWriter();
		}
		_spatial.writer().delete(txt);
	}

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Set<Integer> getDocsWithXLinks() throws Exception {
		IndexReader reader = getIndexReader(null);
		try {
			FieldSelector idXLinkSelector = new FieldSelector() {
				public final FieldSelectorResult accept(String name) {
					if (name.equals("_id") || name.equals("_hasxlinks")) return FieldSelectorResult.LOAD;
					else return FieldSelectorResult.NO_LOAD;
				}
			};

			Set<Integer> docs = new LinkedHashSet<Integer>();
			for (int i = 0; i < reader.maxDoc(); i++) {
				if (reader.isDeleted(i)) continue; // FIXME: strange lucene hack: sometimes it tries to load a deleted document
				Document doc = reader.document(i, idXLinkSelector);
				String id = doc.get("_id");
				String hasxlinks = doc.get("_hasxlinks");
                if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
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
		}
        finally {
			releaseIndexReader(reader);
		}
	}

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Map<String,String> getDocsChangeDate() throws Exception {
		IndexReader reader = getIndexReader(null);
		try {
			FieldSelector idChangeDateSelector = new FieldSelector() {
				public final FieldSelectorResult accept(String name) {
					if (name.equals("_id") || name.equals("_changeDate")) return FieldSelectorResult.LOAD;
					else return FieldSelectorResult.NO_LOAD;
				}
			};

			int capacity = (int)(reader.maxDoc() / 0.75)+1;
			Map<String,String> docs = new HashMap<String,String>(capacity);
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
		}
        finally {
			releaseIndexReader(reader);
		}
	}

	/**
	 * Browses the index and returns all values for the Lucene field.
	 *
	 * @param fld	The Lucene field name
	 * @return	The list of values for the field
	 * @throws Exception
	 */
	public Vector<String> getTerms(String fld) throws Exception {
		Vector<String> terms = new Vector<String>();
		IndexReader reader = getIndexReader(null);
		try {
			TermEnum enu = reader.terms(new Term(fld, ""));
			if (enu.term()==null) return terms;
			do	{
				Term term = enu.term();
				if (!term.field().equals(fld)) break;
				terms.add(enu.term().text());
			}
            while (enu.next());
			return terms;
		}
        finally {
			releaseIndexReader(reader);
		}
	}

	/**
	 * Browses the index for the specified Lucene field and return the list of terms found containing the search value
     * with their frequency.
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
	public List<TermFrequency> getTermsFequency(String fieldName, String searchValue, int maxNumberOfTerms,
                                                int threshold) throws Exception {
		List<TermFrequency> termList = new ArrayList<TermFrequency>();
		IndexReader reader = getIndexReader(null);
		TermEnum term = reader.terms(new Term(fieldName, ""));
		int i = 0;
		try {
			if (term.term()!=null) {
				// Extract terms containing search value.
				do {
					if (!term.term().field().equals(fieldName) || (++i > maxNumberOfTerms)) {
						break;
                    }
					if (term.docFreq() >= threshold && term.term().text().contains(searchValue)) {
						TermFrequency freq = new TermFrequency(term.term().text(), term.docFreq());
						termList.add(freq);
					} 
				}
                while (term.next());
			}
		}
        finally {
			releaseIndexReader(reader);
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
				return new CompareToBuilder().append(frequency, oFreq.frequency).append(term, oFreq.term).toComparison();
			}
            else {
				return 0;
			}
		}
	}

	// utilities

    /**
     * TODO javadoc.
     *
     * @param schemaDir
     * @param xml
     * @return
     * @throws Exception
     */
    Element getIndexFields(String schemaDir, Element xml) throws Exception {
        Element documents = new Element("Documents");
        try {
            String defaultStyleSheet = new File(schemaDir, "index-fields.xsl").getAbsolutePath();
            String otherLocalesStyleSheet = new File(schemaDir, "language-index-fields.xsl").getAbsolutePath();
            Map<String, String> params = new HashMap<String, String>();
            params.put("inspire", Boolean.toString(_inspireEnabled));
            params.put("thesauriDir", _thesauriDir);
            Element defaultLang = Xml.transform(xml, defaultStyleSheet, params);
            if (new File(otherLocalesStyleSheet).exists()) {
                @SuppressWarnings(value = "unchecked")
                List<Element> otherLanguages = Xml.transform(xml, otherLocalesStyleSheet, params).removeContent();
                mergeDefaultLang(defaultLang, otherLanguages);
                documents.addContent(otherLanguages);
            }
            documents.addContent(defaultLang);
        }
        catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE, "Indexing stylesheet contains errors : " + e.getMessage() + "\n\t Marking the metadata as _indexingError=1 in index");
            Element xmlDoc = new Element("Document");
            SearchManager.addField(xmlDoc, "_indexingError", "1", true, true);
            SearchManager.addField(xmlDoc, "_indexingErrorMsg", e.getMessage(), true, false);
            StringBuilder sb = new StringBuilder();
            allText(xml, sb);
            SearchManager.addField(xmlDoc, "any", sb.toString(), false, true);
            documents.addContent(xmlDoc);
        }
        return documents;
    }

	// utilities

    /**
     * If otherLanguages has a document that is the same locale as the default then remove it from otherlanguages and
     * merge the fields with those in defaultLang.
     *
     * @param defaultLang
     * @param otherLanguages
     */
    @SuppressWarnings(value = "unchecked")
    private void mergeDefaultLang( Element defaultLang, List<Element> otherLanguages ) {
        final String langCode;
        if (defaultLang.getAttribute("locale") == null) {
            langCode = "";
        }
        else {
            langCode = defaultLang.getAttributeValue("locale");
        }

        Element toMerge = null;

        for( Element element : otherLanguages ) {
            String clangCode;
            if (element.getAttribute("locale") == null) {
                clangCode = "";
            }
            else {
                clangCode = element.getAttributeValue("locale");
            }

            if (clangCode.equals(langCode)) {
                toMerge = element;
                break;
            }
        }

        SortedSet<Element> toInclude = new TreeSet<Element>(new Comparator<Element>(){
            public int compare( Element o1, Element o2 ) {
                // <Field name="_locale" string="{string($iso3LangId)}" store="true" index="true" token="false"/>
                int name = compare(o1, o2, "name");
                int string = compare(o1, o2, "string");
                int store = compare(o1, o2, "store");
                int index = compare(o1, o2, "index");
                if (name != 0) {
                    return name;
                }
                if (string != 0) {
                    return string;
                }
                if (store != 0) {
                    return store;
                }
                if (index != 0) {
                    return index;
                }
                return 0;
            }
            private int compare( Element o1, Element o2, String attName ) {
                return safeGet(o1, attName).compareTo(safeGet(o2, attName));
            }
            public String safeGet( Element e, String attName ) {
                String att = e.getAttributeValue(attName);
                if (att == null) {
                    return "";
                } else {
                    return att;
                }
            }
        });

        if (toMerge != null) {
            toMerge.detach();
            otherLanguages.remove(toMerge);
            for( Element element : (List<Element>) defaultLang.getChildren() ) {
                toInclude.add(element);
            }
            for( Element element : (List<Element>) toMerge.getChildren() ) {
                toInclude.add(element);
            }
            toMerge.removeContent();
            defaultLang.removeContent();
            defaultLang.addContent(toInclude);
        }
    }

    /**
     * TODO javadoc.
     *
     * @param styleSheetName
     * @param xml
     * @return
     * @throws Exception
     */
	Element transform(String styleSheetName, Element xml) throws Exception {
		try {
			String styleSheetPath = new File(_stylesheetsDir, styleSheetName).getAbsolutePath();
			return Xml.transform(xml, styleSheetPath);
		}
        catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE, "Search stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

    /**
     * Returns a reopened index reader to do operations on an up-to-date index.
     * 
     * @param priorityLocale the locale to prioritize. may be null
     * @return
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    public IndexReader getIndexReader( String priorityLocale ) throws InterruptedException, IOException {
        return _indexReader.getReader(priorityLocale);
    }

	/**
	 * Checks if the reader is current or the index has been updated since it was obtained.
	 *
	 * @param reader
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
    public boolean isUpToDateReader(IndexReader reader) throws IOException, InterruptedException {
        return _indexReader.isUpToDateReader(reader);
    }

    /**
     * TODO javadoc.
     *
     * @param reader
     * @throws IOException
     * @throws InterruptedException
     */
	public void releaseIndexReader(IndexReader reader) throws InterruptedException, IOException {
		_indexReader.releaseReader(reader);
	}

    /**
     * Creates an index in directory luceneDir if not already there.
     *
     * @param rebuild
     * @throws Exception
     */
	private void setupIndex(boolean rebuild) throws Exception {
        if (_indexReader == null) {
            _indexReader = new LuceneIndexReaderFactory(_luceneDir);
        }
        if (_indexWriter == null) {
            _indexWriter = new LuceneIndexWriterFactory(_luceneDir, _analyzer, _luceneConfig);
        }

        // if rebuild forced don't check
		boolean badIndex = true;
		if (!rebuild) {
			try {
                _indexReader = new LuceneIndexReaderFactory(_luceneDir);
                // reason for calling this is a side-effect, probably the call to maybeReopen() inside there
                _indexReader.getReader(null);
                badIndex = false;
            }
            catch (AssertionError e) {
                Log.error(Geonet.INDEX_ENGINE,
                        "Exception while opening lucene index, going to rebuild it: " + e.getMessage());
			}
		}
		// if rebuild forced or bad index then rebuild index
		if (rebuild || badIndex) {
			Log.error(Geonet.INDEX_ENGINE, "Rebuilding lucene index");
			if (_spatial != null) _spatial.writer().reset();
			_indexWriter.createDefaultLocale();
		}
	}

	/**
	 * Optimizes the Lucene index (See {@link IndexWriter#optimize()}).
     *
     * @return
     */
	public boolean optimizeIndex() {
		try {
			_indexWriter.openWriter();
			_indexWriter.optimize();
			_indexWriter.closeWriter();
			return true;
		}
        catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE, "Exception while optimizing lucene index: " + e.getMessage());
			return false;
		}
	}

	/**
	 *  Rebuilds the Lucene index.
	 *
	 *  @param context
	 *  @param xlinks
	 *
     * @return
     * @throws Exception
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
			}
            else {
				dataMan.rebuildIndexXLinkedMetadata(context);
			}
			return true;
		}
        catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE, "Exception while rebuilding lucene index, going to rebuild it: " +
                    e.getMessage());
			return false;
		}
	}

    /**
     * Creates a new {@link Document} for each input fields in xml taking {@link LuceneConfig} and field's attributes
     * for configuration.
     *
     * @param xml	The list of field to be indexed.
     * @return
     */
	private Document newDocument(Element xml) {
		Document doc = new Document();
		
		boolean hasLocaleField = false;
        for (Object o : xml.getChildren()) {
            Element field = (Element) o;
            String name = field.getAttributeValue("name");
            String string = field.getAttributeValue("string"); // Lower case field is handled by Lucene Analyzer.
            if(name.equals(Geonet.LUCENE_LOCALE_KEY)) hasLocaleField = true;
            if (string.trim().length() > 0) {
            	String sStore = field.getAttributeValue("store");
                String sIndex = field.getAttributeValue("index");

                boolean bStore = sStore != null && sStore.equals("true");
                boolean bIndex = sIndex != null && sIndex.equals("true");
                boolean token = _luceneConfig.isTokenizedField(name);
                boolean isNumeric = _luceneConfig.isNumericField(name);
                
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
                if (isNumeric) {
                	addNumericField(doc, name, string, store, bIndex);
                }
                else {
                    Field f = new Field(name, string, store, index);

                    // Boost a particular field according to Lucene config. 
                    Float boost = _luceneConfig.getFieldBoost(name);
                    if (boost != null) {
                        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                            Log.debug(Geonet.INDEX_ENGINE, "Boosting field: " + name + " with boost factor: " + boost);
                        f.setBoost(boost);
                    }
                    doc.add(f);
                }
            }
        }
        
        if(!hasLocaleField) {
           doc.add(new Field(Geonet.LUCENE_LOCALE_KEY,Geonet.DEFAULT_LANGUAGE,Field.Store.YES,Field.Index.NOT_ANALYZED));
        }
        
        // Set boost to promote some types of document selectively according to DocumentBoosting class
        if (_documentBoostClass != null) {
            Float f = (_documentBoostClass).getBoost(doc);
            if (f != null) {
                if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                    Log.debug(Geonet.INDEX_ENGINE, "Boosting document with boost factor: " + f);
                doc.setBoost(f);
            }
        }
		return doc;
	}

	/**
	 * Creates Lucene numeric field.
	 * 
	 * @param doc	The document to add the field
	 * @param name	The field name
	 * @param string	The value to be indexed. It is parsed to its numeric type. If exception occurs
	 * field is not added to the index. 
	 * @param store
	 * @param index
	 * @return
	 */
	private void addNumericField(Document doc, String name, String string, Store store, boolean index) {
		LuceneConfigNumericField fieldConfig = _luceneConfig.getNumericField(name);
		// string = cleanNumericField(string);
		NumericField field = new NumericField(name, fieldConfig.getPrecisionStep(), store, index);
		// TODO : reuse the numeric field for better performance
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Indexing numeric field: " + name + " with value: " + string );
		try {
			String paramType = fieldConfig.getType();
			if ("double".equals(paramType)) {
				double d = Double.valueOf(string);
				field.setDoubleValue(d);
			}
            else if ("float".equals(paramType)) {
				float f = Float.valueOf(string);
				field.setFloatValue(f);
			}
            else if ("long".equals(paramType)) {
				long l = Long.valueOf(string);
				field.setLongValue(l);
			}
            else {
				int i = Integer.valueOf(string);
				field.setIntValue(i);
			}
			doc.add(field);
		}
        catch (Exception e) {
			Log.warning(Geonet.INDEX_ENGINE, "Failed to index numeric field: " + name + " with value: " + string + ", error is:" + e.getMessage());
		}
	}

	public Spatial getSpatial() {
        return _spatial;
    }

    /**
     * TODO javadoc.
     *
     */
	public class Spatial {
		private final DataStore _datastore;
        private static final long 	TIME_BETWEEN_SPATIAL_COMMITS = 10000;
        private final Map<String, Constructor<? extends SpatialFilter>> _types;
        {
            Map<String, Constructor<? extends SpatialFilter>> types = new HashMap<String, Constructor<? extends SpatialFilter>>();
            try {
                types.put(Geonet.SearchResult.Relation.ENCLOSES, constructor(ContainsFilter.class));
                types.put(Geonet.SearchResult.Relation.CROSSES, constructor(CrossesFilter.class));
                types.put(Geonet.SearchResult.Relation.OUTSIDEOF, constructor(IsFullyOutsideOfFilter.class));
                types.put(Geonet.SearchResult.Relation.EQUAL, constructor(EqualsFilter.class));
                types.put(Geonet.SearchResult.Relation.INTERSECTION, constructor(IntersectionFilter.class));
                types.put(Geonet.SearchResult.Relation.OVERLAPS, constructor(OverlapsFilter.class));
                types.put(Geonet.SearchResult.Relation.TOUCHES, constructor(TouchesFilter.class));
                types.put(Geonet.SearchResult.Relation.WITHIN, constructor(WithinFilter.class));
                // types.put(Geonet.SearchResult.Relation.CONTAINS, constructor(BeyondFilter.class));
                // types.put(Geonet.SearchResult.Relation.CONTAINS, constructor(DWithinFilter.class));
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Unable to create types mapping", e);
            }
            _types = Collections.unmodifiableMap(types);
        }
        private final Transaction                     _transaction;
        private final int                             _maxWritesInTransaction;
        private final Timer                           _timer;
        private final Parser                          _gmlParser;
        private final Lock                            _lock;
        private SpatialIndexWriter                    _writer;
        private Committer                             _committerTask;

        /**
         * TODO javadoc.
         *
         * @param dataStore
         * @param maxWritesInTransaction - Number of features to write 
				 * to before commit - set 1 and the transaction will be
				 * autocommit which results in faster loading for some (all?) 
				 * configurations and does not keep a long running transaction 
				 * open. 
         * @throws Exception
         */
        public Spatial(DataStore dataStore, int maxWritesInTransaction) throws Exception {
            _lock = new ReentrantLock();
            _datastore = dataStore;
			if (maxWritesInTransaction > 1) {
            	_transaction = new DefaultTransaction("SpatialIndexWriter");
            }
            else {
            	_transaction = Transaction.AUTO_COMMIT;
            }
            _maxWritesInTransaction = maxWritesInTransaction;
            _timer = new Timer(true);
            _gmlParser = new Parser(new GMLConfiguration());
            boolean rebuildIndex;

            rebuildIndex = createWriter(_datastore);
            if (rebuildIndex) {
                setupIndex(true);
            }
            else{
                // since the index is considered good we will
                // call getIndex to make sure the in-memory index is
                // generated
                _writer.getIndex();
            }
        }

        /**
         * TODO javadoc.
         *
         * @param datastore
         * @return
         * @throws IOException
         */
        private boolean createWriter(DataStore datastore) throws IOException {
            boolean rebuildIndex;
            try {
                _writer = new SpatialIndexWriter(datastore, _gmlParser,_transaction, _maxWritesInTransaction, _lock);
                rebuildIndex = _writer.getFeatureSource().getSchema() == null;
            }
            catch (Exception e) {
                String exceptionString = Xml.getString(JeevesException.toElement(e));
                Log.warning(Geonet.SPATIAL, "Failure to make _writer, maybe a problem but might also not be an issue:" +
                        exceptionString);
                try {
                    _writer.reset();
                }
                catch (Exception e1) {
                    Log.error(Geonet.SPATIAL, "Unable to call reset on Spatial writer: "+e1.getMessage());
                    e1.printStackTrace();
                }
                rebuildIndex = true;
            }
            return rebuildIndex;
        }

        /**
         * Closes spatial index.
         */
		public void end() {
            _lock.lock();
            try {
                _writer.close();
            }
            catch (IOException e) {
                Log.error(Geonet.SPATIAL,"error closing spatial index: "+e.getMessage());
                e.printStackTrace();
            }
            finally {
                _lock.unlock();
            }
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
        public Filter filter(org.apache.lucene.search.Query query, Element filterExpr, String filterVersion)
                throws Exception {
            _lock.lock();
            try {
            	Parser filterParser = getFilterParser(filterVersion);
                Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> accessor = new SpatialIndexAccessor();
                return OgcGenericFilters.create(query, filterExpr, accessor , filterParser);
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
        public SpatialFilter filter(org.apache.lucene.search.Query query,
                Geometry geom, Element request) throws Exception {
            _lock.lock();
            try {
                String relation = Util.getParam(request, Geonet.SearchResult.RELATION,
                        Geonet.SearchResult.Relation.INTERSECTION);
                return _types.get(relation).newInstance(query, geom, new SpatialIndexAccessor());
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
        public SpatialIndexWriter writer() throws Exception {
            _lock.lock();
            try {
                if (_committerTask != null) {
                    _committerTask.cancel();
                }
                _committerTask = new Committer();
                _timer.schedule(_committerTask, TIME_BETWEEN_SPATIAL_COMMITS);
                return writerNoLocking();
            }
            finally {
                _lock.unlock();
            }
        }

        /**
         * TODO javadoc.
         * @return
         * @throws Exception
         */
        private SpatialIndexWriter writerNoLocking() throws Exception {
            if (_writer == null) {
                _writer = new SpatialIndexWriter(_datastore, _gmlParser, _transaction, _maxWritesInTransaction, _lock);
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
        private class Committer extends TimerTask {
            @Override
            public void run() {
                _lock.lock();
                try {
                    if (_committerTask == this) {
                        _writer.commit();
                        _committerTask = null;
                    }
                }
                catch (IOException e) {
                    Log.error(Geonet.SPATIAL, "error writing spatial index "+e.getMessage());
                }
                finally {
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
        return clazz.getConstructor(org.apache.lucene.search.Query.class, Geometry.class, Pair.class);
    }

	LuceneIndexWriterFactory getIndexWriter() {
		return _indexWriter;
	}
}