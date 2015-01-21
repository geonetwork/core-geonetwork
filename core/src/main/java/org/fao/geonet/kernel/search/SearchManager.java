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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.util.Assert;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.BytesRef;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.classifier.Classifier;
import org.fao.geonet.kernel.search.facet.Dimension;
import org.fao.geonet.kernel.search.function.DocumentBoosting;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.search.index.IndexInformation;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;
import org.fao.geonet.kernel.search.spatial.ContainsFilter;
import org.fao.geonet.kernel.search.spatial.CrossesFilter;
import org.fao.geonet.kernel.search.spatial.EqualsFilter;
import org.fao.geonet.kernel.search.spatial.IntersectionFilter;
import org.fao.geonet.kernel.search.spatial.IsFullyOutsideOfFilter;
import org.fao.geonet.kernel.search.spatial.OgcGenericFilters;
import org.fao.geonet.kernel.search.spatial.OrSpatialFilter;
import org.fao.geonet.kernel.search.spatial.OverlapsFilter;
import org.fao.geonet.kernel.search.spatial.SpatialFilter;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.search.spatial.TouchesFilter;
import org.fao.geonet.kernel.search.spatial.WithinFilter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
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
import org.opengis.filter.capability.FilterCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PreDestroy;

/**
 * Indexes metadata using Lucene.
 */
public class SearchManager {
    private static final String INDEXING_ERROR_MSG = "_indexingErrorMsg";
	private static final String INDEXING_ERROR_FIELD = "_indexingError";

    private static final String SEARCH_STYLESHEETS_DIR_PATH = "xml/search";
    private static final String STOPWORDS_DIR_PATH = "resources/stopwords";

	private static final Configuration FILTER_1_0_0 = new org.geotools.filter.v1_0.OGCConfiguration();
    private static final Configuration FILTER_1_1_0 = new org.geotools.filter.v1_1.OGCConfiguration();
    private static final Configuration FILTER_2_0_0 = new org.geotools.filter.v2_0.FESConfiguration();
    private Path _stylesheetsDir;
    private static Path _stopwordsDir;

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
	private static PerFieldAnalyzerWrapper _defaultAnalyzer;
	private Path _htmlCacheDir;
    private Spatial _spatial;

	private boolean _logAsynch;
	private LuceneOptimizerManager _luceneOptimizerManager;

    @Autowired
    private LuceneIndexLanguageTracker _tracker;
    @Autowired
    private ApplicationContext _applicationContext;
    @Autowired
    private SettingInfo _settingInfo;
    @Autowired
    private SchemaManager _schemaManager;
    @Autowired
    private GeonetworkDataDirectory _geonetworkDataDirectory;
    @Autowired
    private LuceneConfig _luceneConfig;
    @Qualifier("timerThreadPool")
    @Autowired
    private ScheduledThreadPoolExecutor timer;

    public SettingInfo getSettingInfo() {
        return _settingInfo;
    }


    /**
     * Creates GeoNetworkAnalyzer, using Admin-defined stopwords if there are any.
     *
     * @param stopwords the set of words considered "stop" words.  These are the words in the language that are not considered important
     *                  for indexing.  For example in english some stop words are 'the', 'it', 'and', etc...
     * @param ignoreChars characters that should be ignored.  For example ' or -.
     * @return
     */
    private static Analyzer createGeoNetworkAnalyzer(final Set<String> stopwords, final char[] ignoreChars) {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
            Log.debug(Geonet.SEARCH_ENGINE, "Creating GeoNetworkAnalyzer");
        }
        return new GeoNetworkAnalyzer(stopwords, ignoreChars);
    }

    /**
     * Creates a default- (hardcoded) configured PerFieldAnalyzerWrapper.
     *
     * @param stopwords
     * @return
     */
	private static PerFieldAnalyzerWrapper createHardCodedPerFieldAnalyzerWrapper(Set<String> stopwords, char[] ignoreChars) {
        PerFieldAnalyzerWrapper pfaw;
        Analyzer geoNetworkAnalyzer = SearchManager.createGeoNetworkAnalyzer(stopwords, ignoreChars);
		Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
		analyzers.put(LuceneIndexField.UUID, new GeoNetworkAnalyzer());
		analyzers.put(LuceneIndexField.PARENTUUID, new GeoNetworkAnalyzer());
		analyzers.put(LuceneIndexField.OPERATESON, new GeoNetworkAnalyzer());
		analyzers.put(LuceneIndexField.SUBJECT, new KeywordAnalyzer());
		
		pfaw = new PerFieldAnalyzerWrapper(geoNetworkAnalyzer, analyzers );
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
	private static void initHardCodedAnalyzers(char[] ignoreChars) throws IOException {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "initializing hardcoded analyzers");
        // no default analyzer instantiated: create one
        if(_defaultAnalyzer == null) {
            // create hardcoded default PerFieldAnalyzerWrapper w/o stopwords
            _defaultAnalyzer = SearchManager.createHardCodedPerFieldAnalyzerWrapper(null, ignoreChars);
        }
        if (_stopwordsDir == null || !Files.isDirectory(_stopwordsDir)) {
            Log.warning(Geonet.SEARCH_ENGINE, "Invalid stopwords directory " + _stopwordsDir +
                                              ", not using any stopwords.");
        } else {
            if (Log.isDebugEnabled(Geonet.LUCENE)) {
                Log.debug(Geonet.LUCENE, "loading stopwords");
            }

            try (DirectoryStream<Path> paths = Files.newDirectoryStream(_stopwordsDir)) {
                for (Path stopwordsFile : paths) {
                    final String fileName = stopwordsFile.getFileName().toString();
                    String language = fileName.substring(0, fileName.indexOf('.'));
                    if (language.length() != 2) {
                        Log.info(Geonet.LUCENE, "invalid iso 639-1 code for language: " + language);
                    }
                    // look up stopwords for that language
                    Set<String> stopwordsForLanguage = StopwordFileParser.parse(stopwordsFile.toAbsolutePath());
                    if (stopwordsForLanguage != null) {
                        if (Log.isDebugEnabled(Geonet.LUCENE)) {
                            Log.debug(Geonet.LUCENE, "loaded # " + stopwordsForLanguage.size() + " stopwords for language " + language);
                        }
                        Analyzer languageAnalyzer = SearchManager.createHardCodedPerFieldAnalyzerWrapper(stopwordsForLanguage, ignoreChars);
                        analyzerMap.put(language, languageAnalyzer);
                    } else {
                        if (Log.isDebugEnabled(Geonet.LUCENE)) {
                            Log.debug(Geonet.LUCENE, "failed to load any stopwords for language " + language);
                        }
                    }

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
    private Analyzer createAnalyzerFromLuceneConfig(final String analyzerClassName, final String field, final Set<String> stopwords) {
        final char[] ignoreChars = _settingInfo.getAnalyzerIgnoreChars();
        Analyzer analyzer = null;
        try {
            if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
                Log.debug(Geonet.SEARCH_ENGINE, "Creating analyzer defined in Lucene config:" + analyzerClassName);
            }
            // GNA analyzer
            if (analyzerClassName.equals("org.fao.geonet.kernel.search.GeoNetworkAnalyzer")) {
                analyzer = SearchManager.createGeoNetworkAnalyzer(stopwords, ignoreChars);
            } else {
                // non-GNA analyzer
                try {
                    @SuppressWarnings("unchecked")
					Class<? extends Analyzer> analyzerClass = (Class<? extends Analyzer>) Class.forName(analyzerClassName);
                    Class<?>[] clTypesArray = _luceneConfig.getAnalyzerParameterClass((field == null ? "" : field) + analyzerClassName);
                    Object[] inParamsArray = _luceneConfig.getAnalyzerParameter((field == null ? "" : field) + analyzerClassName);

                    try {
                        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
                            Log.debug(Geonet.SEARCH_ENGINE, " Creating analyzer with parameter");
                        }
                        Constructor<? extends Analyzer> c = analyzerClass.getConstructor(clTypesArray);
                        analyzer = c.newInstance(inParamsArray);
                    } catch (Exception x) {
                        Log.warning(Geonet.SEARCH_ENGINE, "   Failed to create analyzer with parameter: " + x.getMessage());
                        x.printStackTrace();
                        // Try using a default constructor without parameter
                        Log.warning(Geonet.SEARCH_ENGINE, "   Now trying without parameter");
                        analyzer = analyzerClass.newInstance();
                    }
                } catch (Exception y) {
                    Log.warning(Geonet.SEARCH_ENGINE, "Failed to create analyzer as specified in lucene config, default analyzer will " +
                                                      "be used for field " + field + ". Exception message is: " + y.getMessage());
                    y.printStackTrace();
                    // abandon and continue with next field defined in lucene config
                }
            }
        } catch (Exception z) {
            Log.warning(Geonet.SEARCH_ENGINE, " Error on analyzer initialization: " + z.getMessage() + ". Check your Lucene " +
                                              "configuration. Hardcoded default analyzer will be used for field " + field);
            z.printStackTrace();
        } finally {
            // creation of analyzer has failed, default to GeoNetworkAnalyzer
            if(analyzer == null) {
                Log.warning(Geonet.SEARCH_ENGINE, "Creating analyzer has failed, defaulting to GeoNetworkAnalyzer");
                analyzer = SearchManager.createGeoNetworkAnalyzer(stopwords, ignoreChars);
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
	public void createAnalyzer() throws IOException {
        String defaultAnalyzerClass = _luceneConfig.getDefaultAnalyzerClass();
        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
            Log.debug(Geonet.SEARCH_ENGINE, "createAnalyzer start");
            Log.debug(Geonet.SEARCH_ENGINE, "defaultAnalyzer defined in Lucene config: " + defaultAnalyzerClass);
        }
        // there is no default analyzer defined in lucene config

        char[] ignoreChars = _settingInfo.getAnalyzerIgnoreChars();
		if (defaultAnalyzerClass == null) {
            // create default (hardcoded) analyzer
            SearchManager.initHardCodedAnalyzers(ignoreChars);
		} else {
            // there is an analyzer defined in lucene config

            if (_stopwordsDir == null || !Files.isDirectory(_stopwordsDir)) {
                Log.warning(Geonet.SEARCH_ENGINE, "Invalid stopwords directory " + _stopwordsDir +
                                                  ", not using any stopwords.");
            } else {
                if (Log.isDebugEnabled(Geonet.LUCENE)) {
                    Log.debug(Geonet.LUCENE, "Loading stopwords and creating per field anlayzer ...");
                }
                
                // One per field analyzer is created for each stopword list available using GNA as default analyzer
                // Configuration can't define different analyzer per language.
                // TODO : http://trac.osgeo.org/geonetwork/ticket/900
                try (DirectoryStream<Path> files = Files.newDirectoryStream(_stopwordsDir)) {
                    for (Path stopwordsFile : files) {
                        final String fileName = stopwordsFile.getFileName().toString();
                        String language = fileName.substring(0, fileName.indexOf('.'));
                        // TODO check for valid ISO 639-2 codes could be better than this
                        if (language.length() != 3) {
                            Log.warning(Geonet.LUCENE, "Stopwords file with incorrect ISO 639-2 language as filename: " + language);
                        }
                        // look up stopwords for that language
                        Set<String> stopwordsForLanguage = StopwordFileParser.parse(stopwordsFile.toAbsolutePath());
                        if (stopwordsForLanguage != null) {
                            if (Log.isDebugEnabled(Geonet.LUCENE)) {
                                Log.debug(Geonet.LUCENE, "Loaded # " + stopwordsForLanguage.size() + " stopwords for language " + language);

                            }

                            // Configure per field analyzer and register them to language map of pfa
                            // ... for indexing
                            configurePerFieldAnalyzerWrapper(defaultAnalyzerClass, _luceneConfig.getFieldSpecificAnalyzers(),
                                    analyzerMap, language, stopwordsForLanguage);

                            // ... for searching
                            configurePerFieldAnalyzerWrapper(defaultAnalyzerClass, _luceneConfig.getFieldSpecificSearchAnalyzers(),
                                    searchAnalyzerMap, language, stopwordsForLanguage);

                        } else {
                            if (Log.isDebugEnabled(Geonet.LUCENE)) {
                                Log.debug(Geonet.LUCENE, "Failed to load any stopwords for language " + language);
                            }
                        }
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
	 * @param defaultAnalyzerClass The default analyzer to use
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
		if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
            Log.debug(Geonet.SEARCH_ENGINE, " Default analyzer class: " + defaultAnalyzerClass);
        }
		Analyzer defaultAnalyzer = createAnalyzerFromLuceneConfig(defaultAnalyzerClass, null, stopwordsForLanguage);
		
		Map<String, Analyzer> extraFieldAnalyzers = new HashMap<String, Analyzer>();
		
		// now handle the exceptions for each field to the default analyzer as
		// defined in lucene config
		for (Entry<String, String> e : fieldAnalyzers.entrySet()) {
			String field = e.getKey();
			String aClassName = e.getValue();
			if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
                Log.debug(Geonet.SEARCH_ENGINE, " Add analyzer for field: " + field + "=" + aClassName);
            }
			Analyzer analyzer = createAnalyzerFromLuceneConfig(aClassName, field, stopwordsForLanguage);
			extraFieldAnalyzers.put(field, analyzer);
		}

		PerFieldAnalyzerWrapper pfa = new PerFieldAnalyzerWrapper( defaultAnalyzer, extraFieldAnalyzers);
		

		// Register to a reference map if needed
		if (referenceMap != null) {
			referenceMap.put(referenceKey, pfa);
		}
		
		return pfa;
	}

    /**
     * TODO javadoc.
     *
     *
     *
     *
     *
     *
     *
     *
     * @param logAsynch
     * @param logSpatialObject
     * @param luceneTermsToExclude
     * @param maxWritesInTransaction
     * @throws Exception
     */
	public void init(boolean logAsynch, boolean logSpatialObject, String luceneTermsToExclude,
                     int maxWritesInTransaction) throws Exception {
        _stopwordsDir = _geonetworkDataDirectory.resolveWebResource(STOPWORDS_DIR_PATH);

        createAnalyzer();
        createDocumentBoost();

        initNonStaticData(logAsynch, logSpatialObject, luceneTermsToExclude, maxWritesInTransaction);
    }

    @VisibleForTesting
    public void initNonStaticData(boolean logAsynch, boolean logSpatialObject, String luceneTermsToExclude,
                                     int maxWritesInTransaction) throws Exception {
         _stylesheetsDir = _geonetworkDataDirectory.resolveWebResource(SEARCH_STYLESHEETS_DIR_PATH);

         if (_stylesheetsDir == null || !Files.isDirectory(_stylesheetsDir)) {
             throw new Exception("directory " + _stylesheetsDir + " not found");
         }

         Path htmlCacheDirTest = _geonetworkDataDirectory.getHtmlCacheDir();
         Files.createDirectories(htmlCacheDirTest);
         _htmlCacheDir = htmlCacheDirTest.toAbsolutePath();

         _spatial = new Spatial(_applicationContext.getBean(DataStore.class), maxWritesInTransaction);

         _logAsynch = logAsynch;
         _logSpatialObject = logSpatialObject;
         _luceneTermsToExclude = luceneTermsToExclude;

         initLucene();
         initZ3950();

         _luceneOptimizerManager = new LuceneOptimizerManager(this, _settingInfo);
    }

    @PreDestroy
    public void end() throws Exception {
        endZ3950();
        _spatial.end();
        _luceneOptimizerManager.cancel();
        _tracker.close(TimeUnit.MINUTES.toMillis(1), true);
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
     */
	public void reloadLuceneConfiguration () throws IOException {
		createAnalyzer();
		createDocumentBoost();
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
	public MetaSearcher newSearcher(SearcherType type, String stylesheetName) throws Exception {
		switch (type) {
			case LUCENE:
                return new LuceneSearcher(this, stylesheetName, _luceneConfig);
			case Z3950: return new Z3950Searcher(this, _schemaManager, stylesheetName);
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

	public Path getHtmlCacheDir() {
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
     * Force the index to wait until all changes are processed and the next reader obtained will get the latest data.
     * @throws IOException
     */
    public void forceIndexChanges() throws IOException {
        _tracker.commit();
        _tracker.maybeRefreshBlocking();
    }

    /**
	 * Indexes a metadata record.
     *
	 * @param schemaDir
	 * @param metadata
	 * @param id
	 * @param moreFields
     * @param forceRefreshReaders if true then block all searches until they can obtain a up-to-date reader
	 * @throws Exception
	 */
	public void index(Path schemaDir, Element metadata, String id, List<Element> moreFields,
                      MetadataType metadataType, String root, boolean forceRefreshReaders)
            throws Exception {
        // Update spatial index first and if error occurs, record it to Lucene index
        indexGeometry(schemaDir, metadata, id, moreFields);
        
        // Update Lucene index
        List<IndexInformation> docs = buildIndexDocument(schemaDir, metadata, id, moreFields, metadataType, root);
        _tracker.deleteDocuments(new Term(Geonet.IndexFieldNames.ID, id));
        for(IndexInformation document : docs ) {
            _tracker.addDocument(document);
        }
        if (forceRefreshReaders) {
            forceIndexChanges();
        }
	}

    private void indexGeometry(Path schemaDir, Element metadata, String id,
            List<Element> moreFields) throws Exception {
        try {
            _spatial.writer().delete(id);
            _spatial.writer().index(schemaDir, id, metadata);
        } catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE, "Failed to properly index geometry of metadata " + id + ". Error: " + e.getMessage(), e);
            moreFields.add(SearchManager.makeField(INDEXING_ERROR_FIELD, "1", true, true));
            moreFields.add(SearchManager.makeField(INDEXING_ERROR_MSG, "GNIDX-GEOWRITE||" + e.getMessage(), true, false));
        }
        Map<String, String> errors = _spatial.writer().getErrorMessage();
        if (errors.size() > 0) {
            for (Entry<String, String> e : errors.entrySet()) {
            moreFields.add(SearchManager.makeField(INDEXING_ERROR_FIELD, "1", true, true));
            moreFields.add(SearchManager.makeField(INDEXING_ERROR_MSG, "GNIDX-GEO|" + e.getKey() + "|" + e.getValue(), true, false));
            }
        }
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
        _tracker.deleteDocuments(new Term(fld, txt));
		
		_spatial.writer().delete(txt);
	}
	
    /**
     * TODO javadoc.
     *  @param schemaDir
     * @param metadata
     * @param id
     * @param moreFields
     * @param metadataType
     * @param root @return
     * @throws Exception
     */
     private List<IndexInformation> buildIndexDocument(Path schemaDir, Element metadata, String id,
                                                       List<Element> moreFields, MetadataType metadataType,
                                                       String root) throws Exception
     {
        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "Metadata to index:\n" + Xml.getString(metadata));
        }
         Path defaultLangStyleSheet = getIndexFieldsXsl(schemaDir, root, "");
         Path otherLocalesStyleSheet = getIndexFieldsXsl(schemaDir, root, "language-");

        Element xmlDoc = getIndexFields(metadata, defaultLangStyleSheet, otherLocalesStyleSheet);

        if (Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
            Log.debug(Geonet.INDEX_ENGINE, "Indexing fields:\n" + Xml.getString(xmlDoc));
        }

        @SuppressWarnings(value = "unchecked")
        List<Element> documentElements = xmlDoc.getContent();
        Collection<Field> multilingualSortFields = findMultilingualSortElements(documentElements);

        List<IndexInformation> documents = Lists.newArrayList();
        for( Element doc : documentElements ) {
            // add _id field
            SearchManager.addField(doc, LuceneIndexField.ID, id, true, true);

            // add more fields
            for( Element moreField : moreFields ) {
                doc.addContent((Content) moreField.clone());
            }

            String locale = getLocaleFromIndexDoc(doc);
            documents.add(newDocument(locale, doc, multilingualSortFields));
        }
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Lucene document:\n" + Xml.getString(xmlDoc));
        return documents;
	}

    private Path getIndexFieldsXsl(Path schemaDir, String root, String indexName) {
        if (root == null) {
            root = "";
        }
        root = root.toLowerCase();
        if (root.contains(":")) {
            root = root.split(":",2)[1];
        }

        final String basicName = "index-fields";
        Path defaultLangStyleSheet = schemaDir.resolve(basicName).resolve(indexName + root + ".xsl");
        if (!Files.exists(defaultLangStyleSheet)) {
            defaultLangStyleSheet = schemaDir.resolve(basicName).resolve(indexName + "default.xsl");
        }
        if (!Files.exists(defaultLangStyleSheet)) {
            // backward compatibility
            defaultLangStyleSheet = schemaDir.resolve(indexName + basicName + ".xsl");
        }
        return defaultLangStyleSheet;
    }

    private Collection<Field> findMultilingualSortElements(List<Element> documentElements) {
        Map<String, Field> multilingualSortFields = new HashMap<String, Field>();
        
        for (Element doc : documentElements) {
            String locale = getLocaleFromIndexDoc(doc);

            List<?> fields = doc.getChildren("Field");
            Set<String> configuredMultilingualSortFields = _luceneConfig.getMultilingualSortFields();
            for (Object object : fields) {
                Element field = (Element) object;
                String fieldName = field.getAttributeValue("name");
                if (configuredMultilingualSortFields.contains(fieldName)) {
                    String nameWithLocale = LuceneConfig.multilingualSortFieldName(fieldName, locale);
                    if (!multilingualSortFields.containsKey(nameWithLocale)) {
                        String fieldValue = field.getAttributeValue("string");
                        FieldType fieldType = new FieldType();
                        fieldType.setIndexed(true);
                        fieldType.setIndexOptions(IndexOptions.DOCS_ONLY);
                        fieldType.setOmitNorms(true);
                        fieldType.setTokenized(false);
                        fieldType.setStored(true);
                        multilingualSortFields.put(nameWithLocale, new Field(nameWithLocale, fieldValue, fieldType));
                    }
                }
            }
        }
        return multilingualSortFields.values();
    }

	private String getLocaleFromIndexDoc(Element doc) {
		String locale = doc.getAttributeValue(Geonet.IndexFieldNames.LOCALE);
		if(locale == null || locale.trim().isEmpty()) {
		    locale = Geonet.DEFAULT_LANGUAGE;
		}
		return locale;
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
		field.setAttribute(LuceneFieldAttribute.STRING.toString(), value == null ? "" : value);
		field.setAttribute(LuceneFieldAttribute.STORE.toString(), Boolean.toString(store));
		field.setAttribute(LuceneFieldAttribute.INDEX.toString(), Boolean.toString(index));
		return field;
	}


    public enum LuceneFieldAttribute {
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
		@SuppressWarnings("unchecked")
        List<Element> children = metadata.getChildren();
        for (Element aChildren : children) {
            allText(aChildren, sb);
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
		_tracker.deleteDocuments(new Term(fld, txt));
		_spatial.writer().delete(txt);
	}
	
    /**
     *  deletes a list of documents.
     *
     * @param fld
     * @param txts
     * @throws Exception
     */
    public void delete(String fld, List<String> txts) throws Exception {
        // possibly remove old document
        for(String txt : txts) {
            _tracker.deleteDocuments(new Term(fld, txt));
        }
        _spatial.writer().delete(txts);
    }


    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Set<Integer> getDocsWithXLinks() throws Exception {
        IndexAndTaxonomy indexAndTaxonomy= getNewIndexReader(null);
        
		try {
		    GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

			Set<Integer> docs = new LinkedHashSet<Integer>();
			for (int i = 0; i < reader.maxDoc(); i++) {
				// Commented this out for lucene 4.0 and NRT indexing.  It shouldn't be needed I would guess but leave it here
				// for a bit longer:  Commented out since: Dec 10 2012
				// FIXME: strange lucene hack: sometimes it tries to load a deleted document
				// if (reader.isDeleted(i)) continue; 
				
				DocumentStoredFieldVisitor idXLinkSelector = new DocumentStoredFieldVisitor(Geonet.IndexFieldNames.ID, "_hasxlinks");
				reader.document(i, idXLinkSelector);
				Document doc = idXLinkSelector.getDocument();
				String id = doc.get(Geonet.IndexFieldNames.ID);
				String hasxlinks = doc.get("_hasxlinks");
                if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                    Log.debug(Geonet.INDEX_ENGINE, "Got id "+id+" : '"+hasxlinks+"'");
				if (id == null) {
					Log.error(Geonet.INDEX_ENGINE, "Document with no _id field skipped! Document is "+doc);
					continue;
				}
				if (hasxlinks.trim().equals("1")) {
					docs.add(Integer.valueOf(id));
				}
			}
			return docs;
		}
        finally {
			releaseIndexReader(indexAndTaxonomy);
		}
	}

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Map<String,String> getDocsChangeDate() throws Exception {
        IndexAndTaxonomy indexAndTaxonomy= getNewIndexReader(null);
		try {
		    GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

			int capacity = (int)(reader.maxDoc() / 0.75)+1;
			Map<String,String> docs = new HashMap<String,String>(capacity);
			for (int i = 0; i < reader.maxDoc(); i++) {
				// Commented this out for lucene 4.0 and NRT indexing.  It shouldn't be needed I would guess but leave it here
				// for a bit longer:  Commented out since: Dec 10 2012
				// FIXME: strange lucene hack: sometimes it tries to load a deleted document
				// if (reader.isDeleted(i)) continue;
				
				DocumentStoredFieldVisitor idChangeDateSelector = new DocumentStoredFieldVisitor(Geonet.IndexFieldNames.ID, "_changeDate");
                reader.document(i, idChangeDateSelector);
                Document doc = idChangeDateSelector.getDocument();
				String id = doc.get(Geonet.IndexFieldNames.ID);
				if (id == null) {
					Log.error(Geonet.INDEX_ENGINE, "Document with no _id field skipped! Document is "+doc);
					continue;
				}
				docs.put(id, doc.get("_changeDate"));
			}
			return docs;
		}
        finally {
			releaseIndexReader(indexAndTaxonomy);
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
        Vector<String> foundTerms = new Vector<String>();
        IndexAndTaxonomy indexAndTaxonomy = getNewIndexReader(null);
        try {
            @SuppressWarnings("resource")
            AtomicReader reader = SlowCompositeReaderWrapper.wrap(indexAndTaxonomy.indexReader);
            Terms terms = reader.terms(fld);
            if (terms != null) {
                TermsEnum enu = terms.iterator(null);
                BytesRef term = enu.next();
                while (term != null) {
                    if (!term.utf8ToString().equals(fld)) {
                        break;
                    }
                    foundTerms.add(term.utf8ToString());
                    term = enu.next();
                }
            }
            return foundTerms;
        } finally {
            releaseIndexReader(indexAndTaxonomy);
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
	public Collection<TermFrequency> getTermsFequency(String fieldName, String searchValue, int maxNumberOfTerms,
	                                            int threshold, ServiceContext context) throws Exception {
        Collection<TermFrequency> termList = new ArrayList<TermFrequency>();
        IndexAndTaxonomy indexAndTaxonomy = getNewIndexReader(null);
        String searchValueWithoutWildcard = searchValue.replaceAll("[*?]", "");

        final Element request = new Element("request").addContent(new Element(Geonet.IndexFieldNames.ANY).setText(searchValue));
        String language = LuceneSearcher.determineLanguage(context, request, _settingInfo).analyzerLanguage;
        final PerFieldAnalyzerWrapper analyzer = SearchManager.getAnalyzer(language, true);
        String analyzedSearchValue = LuceneSearcher.analyzeText(fieldName, searchValueWithoutWildcard, analyzer);
        boolean startsWithOnly = !searchValue.startsWith("*") && searchValue.endsWith("*");
        
        try {
            GeonetworkMultiReader multiReader = indexAndTaxonomy.indexReader;
            for (AtomicReaderContext atomicReaderContext : multiReader.getContext().leaves()) {
                final AtomicReader reader = atomicReaderContext.reader();
                Terms terms = reader.terms(fieldName);
                if (terms != null) {
                    TermsEnum termEnum = terms.iterator(null);
                    int i = 1;
                        BytesRef term = termEnum.next();
                        while (term != null && i++ < maxNumberOfTerms) {
                            String text = term.utf8ToString();
                            if (termEnum.docFreq() >= threshold) {
                                String analyzedText = LuceneSearcher.analyzeText(fieldName, text, analyzer);
                                if ((startsWithOnly && StringUtils.startsWithIgnoreCase(analyzedText, analyzedSearchValue))
                                        || (!startsWithOnly && StringUtils.containsIgnoreCase(analyzedText, analyzedSearchValue))
                                        || (startsWithOnly && StringUtils.startsWithIgnoreCase(text, searchValueWithoutWildcard))
                                        || (!startsWithOnly && StringUtils.containsIgnoreCase(text, searchValueWithoutWildcard))) {
                                    TermFrequency freq = new TermFrequency(text, termEnum.docFreq());
                                    termList.add(freq);
                                }
                            }
                            term = termEnum.next();
                        }
                }
            }
        } finally {
            releaseIndexReader(indexAndTaxonomy);
        }
        return termList;
    }

	/**
	 * Frequence of terms.
     *
	 */
	public static class TermFrequency implements Comparable<Object> {
		private String term;
		private int frequency;

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
		
        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }
        
		public int compareTo(Object o) {
			if (o instanceof TermFrequency) {
				TermFrequency oFreq = (TermFrequency) o;
				return term.compareTo(oFreq.term);
			} else {
				return 0;
			}
		}

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + frequency;
            result = prime * result + ((term == null) ? 0 : term.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TermFrequency other = (TermFrequency) obj;
            return compareTo(other) == 0;
        }
		
		
	}
	// utilities

    /**
     * TODO javadoc.
     *
     * @param xml
     * @param defaultLangStyleSheet
     *@param otherLocalesStyleSheet @return
     * @throws Exception
     */
    Element getIndexFields(Element xml,
                           Path defaultLangStyleSheet,
                           Path otherLocalesStyleSheet) throws Exception {
        Element documents = new Element("Documents");
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("inspire", Boolean.toString(isInspireEnabled()));
            params.put("thesauriDir", _geonetworkDataDirectory.getThesauriDir().toAbsolutePath());

            Element defaultLang = Xml.transform(xml, defaultLangStyleSheet, params);
            if (Files.exists(otherLocalesStyleSheet)) {
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
            SearchManager.addField(xmlDoc, INDEXING_ERROR_FIELD, "1", true, true);
            SearchManager.addField(xmlDoc, INDEXING_ERROR_MSG, "GNIDX-XSL||" + e.getMessage(), true, false);
            StringBuilder sb = new StringBuilder();
            allText(xml, sb);
            SearchManager.addField(xmlDoc, Geonet.IndexFieldNames.ANY, sb.toString(), false, true);
            documents.addContent(xmlDoc);
        }
        return documents;
    }

    private boolean isInspireEnabled() {
        return _settingInfo.getInspireEnabled();
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
        if (defaultLang.getAttribute(Geonet.IndexFieldNames.LOCALE) == null) {
            langCode = "";
        }
        else {
            langCode = defaultLang.getAttributeValue(Geonet.IndexFieldNames.LOCALE);
        }

        Element toMerge = null;

        for( Element element : otherLanguages ) {
            Assert.isTrue(element.getName().equals("Document"));
            String clangCode;
            if (element.getAttribute(Geonet.IndexFieldNames.LOCALE) == null) {
                clangCode = "";
            }
            else {
                clangCode = element.getAttributeValue(Geonet.IndexFieldNames.LOCALE);
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
			Path styleSheetPath = _stylesheetsDir.resolve(styleSheetName).toAbsolutePath();
			return Xml.transform(xml, styleSheetPath);
		} catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE, "Search stylesheet contains errors : " + e.getMessage());
			throw e;
		}
	}

    public IndexAndTaxonomy getIndexReader(String preferedLang, long versionToken) throws IOException {
        return _tracker.acquire(preferedLang, versionToken);
    }
    public IndexAndTaxonomy getNewIndexReader(String preferedLang) throws IOException, InterruptedException {
       Log.debug(Geonet.INDEX_ENGINE,"Ask for new reader");
       return getIndexReader(preferedLang, -1L);
    }
	public void releaseIndexReader(IndexAndTaxonomy reader) throws InterruptedException, IOException {
	    reader.indexReader.releaseToNRTManager();
	}

    /**
     * Creates an index in directory luceneDir if not already there.
     *
     * @param rebuild
     * @throws Exception
     */
	private void setupIndex(boolean rebuild) throws Exception {
	    boolean badIndex = false;

	        try {
                IndexAndTaxonomy reader = _tracker.acquire(null, -1);
                reader.indexReader.releaseToNRTManager();
            } catch (Throwable e) {
                badIndex = true;
                Log.error(Geonet.INDEX_ENGINE,
                        "Exception while opening lucene index, going to rebuild it: " , e);
            }

	    // if rebuild forced or bad index then rebuild index
		if (rebuild || badIndex) {
			Log.error(Geonet.INDEX_ENGINE, "Rebuilding lucene index");

			_tracker.reset(TimeUnit.MINUTES.toMillis(5));
			if (_spatial != null){
				try {
				_spatial.writer().reset();
				} catch (Exception e) {
					Log.error(Geonet.INDEX_ENGINE, "Failure resetting spatial index.", e);
				}
			}
			_tracker.open(Geonet.DEFAULT_LANGUAGE);
		}
	}

    /**
	 *  Rebuilds the Lucene index. If xlink or from selection parameters
     *  are defined, reindex a subset of record. Otherwise reindex all records.
	 *
	 *  @param context
	 *  @param xlinks   Search all docs with XLinks, clear the XLinks cache and index all records found.
     *  @param reset
     *  @param fromSelection    Reindex all records from selection.
	 *
     * @return
     * @throws Exception
     */
	public boolean rebuildIndex(ServiceContext context,
                                boolean xlinks,
                                boolean reset,
                                boolean fromSelection) throws Exception {
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getBean(DataManager.class);

		try {
			if (reset) {
				synchronized (_tracker) {
			        setupIndex(false);
                }
			}
            if (fromSelection) {
                dataMan.rebuildIndexForSelection(context, xlinks);
            } else if (xlinks) {
                dataMan.rebuildIndexXLinkedMetadata(context);
            } else {
                synchronized (_tracker) {
                    setupIndex(true);
                }
                dataMan.init(context, true);
            }
			return true;
		}
        catch (Exception e) {
			Log.error(Geonet.INDEX_ENGINE, "Exception while rebuilding lucene index, going to rebuild it: " +
                    e.getMessage(), e);
			return false;
		}
	}

    /**
     * Creates a new {@link Document} for each input fields in xml taking {@link LuceneConfig} and field's attributes
     * for configuration.
     *
     *
     * @param xml    The list of field to be indexed.
     */
	private IndexInformation newDocument(String language, Element xml, Collection<Field> multilingualSortFields)
	{
		Document doc = new Document();
		Collection<CategoryPath> categories = new HashSet<CategoryPath>();
    	
		
		for (Field field : multilingualSortFields) {
            doc.add(field);
        }
		final FieldType storeNotTokenizedFieldType = new FieldType();
		storeNotTokenizedFieldType.setIndexed(true);
		storeNotTokenizedFieldType.setTokenized(false);
		storeNotTokenizedFieldType.setStored(true);

		final FieldType storeNotIndexedFieldType = new FieldType();
		storeNotIndexedFieldType.setIndexed(true);
		storeNotIndexedFieldType.setTokenized(false);
		storeNotIndexedFieldType.setStored(true);
		float documentBoost = 1;
        // Set boost to promote some types of document selectively according to DocumentBoosting class
        if (_documentBoostClass != null) {
            Float f = (_documentBoostClass).getBoost(xml);
            if (f != null) {
                if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                    Log.debug(Geonet.INDEX_ENGINE, "Boosting document with boost factor: " + f);
                documentBoost = f;
            }
        }

		boolean hasLocaleField = false;
        for (Object o : xml.getChildren()) {
            Element field = (Element) o;
            String name = field.getAttributeValue(LuceneFieldAttribute.NAME.toString());
            String string = field.getAttributeValue(LuceneFieldAttribute.STRING.toString()); // Lower case field is handled by Lucene Analyzer.
            if(name.equals(Geonet.LUCENE_LOCALE_KEY)) hasLocaleField = true;
            if (string.trim().length() > 0) {
            	String sStore = field.getAttributeValue(LuceneFieldAttribute.STORE.toString());
                String sIndex = field.getAttributeValue(LuceneFieldAttribute.INDEX.toString());

                boolean bStore = sStore != null && sStore.equals("true");
                boolean bIndex = sIndex != null && sIndex.equals("true");
                boolean token = _luceneConfig.isTokenizedField(name);
                boolean isNumeric = _luceneConfig.isNumericField(name);

                FieldType fieldType = new FieldType();
                fieldType.setStored(bStore);
                fieldType.setIndexed(bIndex);
                fieldType.setTokenized(token);
                Field f;
                List<Field> fFacets = new ArrayList<Field>();
                if (isNumeric) {
                    try {
                        f = addNumericField(name, string, fieldType);
                    } catch (Exception e) {
                        String msg = "Invalid value. Field '" + name + "' is not added to the document. Error is: " + e.getMessage();

                        Field idxError = new Field(INDEXING_ERROR_FIELD, "1", storeNotTokenizedFieldType);
                        Field idxMsg = new Field(INDEXING_ERROR_MSG, "GNIDX-BADNUMVALUE|" + name + "|" +  e.getMessage(), storeNotIndexedFieldType);

                        doc.add(idxError);
                        doc.add(idxMsg);

                        Log.warning(Geonet.INDEX_ENGINE, msg);
                        // If an exception occur, the field is not added to the document
                        // and to the taxonomy
                        continue;
                    }
                } else {
                    f = new Field(name, string, fieldType);
                    fFacets.addAll(getFacetFieldsFor(name, string));
                }

                // As of lucene 4.0 to boost a document all field boosts must be premultiplied by documentBoost
                // because there is no doc.setBoost method anymore.
                // Boost a particular field according to Lucene config.

                // You cannot set an index-time boost on an unindexed field, or one that omits norms
                if (bIndex && !f.fieldType().omitNorms()) {
                    Float boost = _luceneConfig.getFieldBoost(name);
                    if (boost != null) {
                        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
                            Log.debug(Geonet.INDEX_ENGINE, "Boosting field: " + name + " with boost factor: " + boost + " x " + documentBoost);
                        f.setBoost(documentBoost * boost);
                    } else if(documentBoost != 1) {
                        f.setBoost(documentBoost);
                    }
                }
                doc.add(f);

                for (Field fFacet: fFacets) {
                    if(Log.isDebugEnabled(Geonet.INDEX_ENGINE)) {
                        Log.debug(Geonet.INDEX_ENGINE, "Facet field: " + fFacet.toString());
                    }
                   doc.add(fFacet);
                }
            }
        }
        
        if(!hasLocaleField) {
           doc.add(new Field(Geonet.LUCENE_LOCALE_KEY,Geonet.DEFAULT_LANGUAGE, storeNotTokenizedFieldType));
        }
        
        return new IndexInformation(language, doc, categories);
    }

    private List<Field> getFacetFieldsFor(String indexKey, String value) {
        List<Field> result = new ArrayList<Field>();

        for (Dimension dimension : _luceneConfig.getDimensionsUsing(indexKey)) {
            result.addAll(getFacetFieldsFor(dimension, value));
        }

        return result;
    }

    private List<Field> getFacetFieldsFor(Dimension dimension, String value) {
        List<Field> result = new ArrayList<Field>();

        Classifier classifier = dimension.getClassifier();

        for (CategoryPath categoryPath: classifier.classify(value)) {
            result.add(new FacetField(dimension.getName(), categoryPath.components));
        }

        return result;
    }

	/**
	 * Creates Lucene numeric field.
	 * 
	 * @param name	The field name
	 * @param string	The value to be indexed. It is parsed to its numeric type. If exception occurs
	 * field is not added to the index. 
	 * @param fieldType
	 * @return
	 * @throws Exception 
	 */
	private Field addNumericField(String name, String string, FieldType fieldType) throws Exception {
        LuceneConfigNumericField fieldConfig = _luceneConfig.getNumericField(name);
		
		Field field;
		// TODO : reuse the numeric field for better performance
        if(Log.isDebugEnabled(Geonet.INDEX_ENGINE))
            Log.debug(Geonet.INDEX_ENGINE, "Indexing numeric field: " + name + " with value: " + string );
		try {
			String paramType = fieldConfig.getType();
			if ("double".equals(paramType)) {
				double d = Double.valueOf(string);
				fieldType.setNumericType(NumericType.DOUBLE);
				field = new DoubleField(name, d, fieldType);
			}
            else if ("float".equals(paramType)) {
				float f = Float.valueOf(string);
				fieldType.setNumericType(NumericType.FLOAT);
				field = new FloatField(name, f, fieldType);
			}
            else if ("long".equals(paramType)) {
				long l = Long.valueOf(string);
				fieldType.setNumericType(NumericType.LONG);
				field = new LongField(name, l, fieldType);
			}
            else {
				int i = Integer.valueOf(string);
				fieldType.setNumericType(NumericType.INT);
				field = new IntField(name, i, fieldType);
			}
			return field;
		}
        catch (Exception e) {
			Log.warning(Geonet.INDEX_ENGINE, "Failed to index numeric field: " + name + " with value: " + string + ", error is: " + e.getMessage());
			throw e;
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
        private static final long 	TIME_BETWEEN_SPATIAL_COMMITS_IN_SECONDS = 10;
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
        private final Parser                          _gmlParser;
        private final Lock                            _lock;
        private SpatialIndexWriter                    _writer;
        private volatile Committer                    _committerTask;

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
            catch (Throwable e) {

				if (_writer == null) {
					throw new RuntimeException(e);
				}
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
        public Filter filter(org.apache.lucene.search.Query query, int numHits, Element filterExpr, String filterVersion)
                throws Exception {
            _lock.lock();
            try {
            	Parser filterParser = getFilterParser(filterVersion);
                Pair<FeatureSource<SimpleFeatureType, SimpleFeature>, SpatialIndex> accessor = new SpatialIndexAccessor();
                return OgcGenericFilters.create(query, numHits, filterExpr, accessor, filterParser);
            }
            catch (Exception e) {
            	// TODO Handle NPE creating spatial filter (due to constraint language version).
    			throw new IllegalArgumentException("Error when parsing spatial filter (version: " + filterVersion + "):" +
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
                Collection<Geometry> geom, Element request) throws Exception {
            _lock.lock();
            try {
                String relation = Util.getParam(request, Geonet.SearchResult.RELATION,
                        Geonet.SearchResult.Relation.INTERSECTION);
                if(geom.size() == 1) {
                    return _types.get(relation.toLowerCase()).newInstance(query, numHits, geom.iterator().next(), new SpatialIndexAccessor());
                } else {
                    Collection<SpatialFilter> filters = new ArrayList<SpatialFilter>(geom.size());
                    Envelope bounds = null;
                    for (Geometry geometry : geom) {
                        if(bounds == null) {
                            bounds = geometry.getEnvelopeInternal();
                        } else {
                            bounds.expandToInclude(geometry.getEnvelopeInternal());
                        }
                        filters.add(_types.get(relation).newInstance(query, numHits, geometry, new SpatialIndexAccessor()));
                    }
                    return new OrSpatialFilter(query, numHits, bounds, new SpatialIndexAccessor(), filters);
                }
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
                timer.schedule(_committerTask, TIME_BETWEEN_SPATIAL_COMMITS_IN_SECONDS, TimeUnit.SECONDS);
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
			if (filterVersion.equals(FilterCapabilities.VERSION_100)) {
			    config = FILTER_1_0_0;
			} else if (filterVersion.equals(FilterCapabilities.VERSION_200)) {
			    config = FILTER_2_0_0;
			} else if (filterVersion.equals(FilterCapabilities.VERSION_110)) {
			    config = FILTER_1_1_0;
			} else {
			    throw new IllegalArgumentException("UnsupportFilterVersion: "+filterVersion);
			}
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
        private class Committer implements Runnable {
            private AtomicBoolean cancelled = new AtomicBoolean(false);

            @Override
            public void run() {
                if (cancelled.get()) {
                    return;
                }
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

            public void cancel() {
                this.cancelled.set(true);
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

    LuceneIndexLanguageTracker getIndexTracker() {
		return _tracker;
	}

    public boolean optimizeIndex() {
        try {
            _tracker.optimize();
            return true;
        } catch (Throwable e) {
            Log.error(Geonet.INDEX_ENGINE, "Exception while optimizing lucene index: " + e.getMessage());
            return false;
        }
    }
}
