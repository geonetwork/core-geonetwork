//==============================================================================
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

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.facet.search.FacetsCollector;
import org.apache.lucene.facet.search.params.CountFacetRequest;
import org.apache.lucene.facet.search.params.FacetRequest;
import org.apache.lucene.facet.search.params.FacetRequest.SortOrder;
import org.apache.lucene.facet.search.params.FacetSearchParams;
import org.apache.lucene.facet.search.params.FacetRequest.SortBy;
import org.apache.lucene.facet.search.results.FacetResult;
import org.apache.lucene.facet.search.results.FacetResultNode;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiCollector;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.MdInfo;
import org.fao.geonet.kernel.search.LuceneConfig.Facet;
import org.fao.geonet.kernel.search.LuceneConfig.FacetConfig;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.search.log.SearcherLogger;
import org.fao.geonet.kernel.search.lucenequeries.DateRangeQuery;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.search.spatial.SpatialFilter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.util.JODAISODate;
import org.jdom.Element;

import java.util.Iterator;

import com.google.common.collect.Maps;
import com.google.common.collect.Ranges;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * search metadata locally using lucene.
 */
public class LuceneSearcher extends MetaSearcher {
	private static SearchManager _sm;
	private String        _styleSheetName;

	private Query         _query;
	private Filter        _filter;
	private Sort          _sort;
	private Element       _elSummary;
	
	private IndexAndTaxonomy _indexAndTaxonomy;

	private int           _maxHitsInSummary;
	private int           _numHits;
	private String        _resultType;
    private String        _language;

	private Set<String>	_tokenizedFieldSet;
	private LuceneConfig _luceneConfig;
	private String _boostQueryClass;

	
	/**
     * Filter geometry object WKT, used in the logger ugly way to store this object, as ChainedFilter API is a little bit cryptic to me...
	 */
	private String _geomWKT = null;

    /**
     * constructor
     * TODO javadoc.
     *
     * @param sm
     * @param styleSheetName
     * @param summaryConfig
     * @param luceneConfig
     */
	public LuceneSearcher (SearchManager sm, String styleSheetName, LuceneConfig luceneConfig) {
		_sm             = sm;
		_styleSheetName = styleSheetName;

		// build _tokenizedFieldSet
		_luceneConfig = luceneConfig;
		_boostQueryClass = _luceneConfig.getBoostQueryClass();
		_tokenizedFieldSet = luceneConfig.getTokenizedField();
	}

	//
	// MetaSearcher API
    //

    /**
     * TODO javadoc.
     *
     * @param srvContext
     * @param request
     * @param config
     * @throws Exception
     */
	public void search(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
		// Open the IndexReader first, and then the TaxonomyReader.
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "LuceneSearcher search()");
        
        String sBuildSummary = request.getChildText(Geonet.SearchResult.BUILD_SUMMARY);
		boolean buildSummary = sBuildSummary == null || sBuildSummary.equals("true");
		_language = determineLanguage(srvContext, request, _sm.get_settingInfo());
		_indexAndTaxonomy = _sm.getNewIndexReader(_language);

        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "LuceneSearcher initializing search range");

        initSearchRange(srvContext);
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "LuceneSearcher computing query");
        computeQuery(srvContext, getTo() - 1, request, config);
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "LuceneSearcher performing query");
		performQuery(getFrom()-1, getTo(), buildSummary);
		updateSearchRange(request);
		
		SettingInfo si = new SettingInfo(srvContext);
		if (si.isSearchStatsEnabled()) {
			if (_sm.getLogAsynch()) {
				// Run asynch
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                    Log.debug(Geonet.SEARCH_ENGINE,"Log search in asynch mode - start.");
				GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
				gc.getThreadPool().runTask(new SearchLoggerTask(srvContext, _sm.getLogSpatialObject(), _sm.getLuceneTermsToExclude(), _query, _numHits, _sort, _geomWKT, config.getValue(Jeeves.Text.GUI_SERVICE,"n")));
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                    Log.debug(Geonet.SEARCH_ENGINE,"Log search in asynch mode - end.");
			} else {
				// Run synch - alter search performance
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                    Log.debug(Geonet.SEARCH_ENGINE,"Log search in synch mode - start.");
				SearcherLogger searchLogger = new SearcherLogger(srvContext, _sm.getLogSpatialObject(), _sm.getLuceneTermsToExclude());
				searchLogger.logSearch(_query, _numHits, _sort, _geomWKT, config.getValue(Jeeves.Text.GUI_SERVICE,"n"));
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                    Log.debug(Geonet.SEARCH_ENGINE,"Log search in synch mode - end.");
			}
		}
	}

    /**
     * TODO javadoc.
     *
     * @param srvContext
     * @param request
     * @param config
     * @return
     * @throws Exception
     */
	public List<org.jdom.Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
		throw new UnsupportedOperationException("Not supported by Lucene searcher");
	}

    /**
     * TODO javadoc.
     *
     * @param srvContext
     * @param request
     * @param config
     * @return An empty response if no result or a list of results. Return only geonet:info element in fast mode.
     * @throws Exception
     */
	public Element present(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
		updateSearchRange(request);

		GeonetContext gc = null;
		if (srvContext != null)
			gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

		String sFast = request.getChildText(Geonet.SearchResult.FAST);
		boolean fast = sFast != null && sFast.equals("true");
		boolean inFastMode = fast || "index".equals(sFast);
		
		// build response
		Element response =  new Element("response");
		response.setAttribute("from",  getFrom()+"");
		response.setAttribute("to",    getTo()+"");
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, Xml.getString(response));

		// Add summary if required and exists
		String sBuildSummary = request.getChildText(Geonet.SearchResult.BUILD_SUMMARY);
		boolean buildSummary = sBuildSummary == null || sBuildSummary.equals("true");
		
		if (buildSummary && _elSummary != null)
			response.addContent((Element)_elSummary.clone());

		if (getTo() > 0) {
			TopDocs tdocs = performQuery(getFrom()-1, getTo(), false); // get enough hits to show a page	

			int nrHits = getTo() - (getFrom()-1);
			if (tdocs.scoreDocs.length >= nrHits) {
				for (int i = 0; i < nrHits; i++) {
					Document doc;
					if (inFastMode) {
						doc = _indexAndTaxonomy.indexReader.document(tdocs.scoreDocs[i].doc); // no selector
					}
                    else {
                        DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor("_id");
						_indexAndTaxonomy.indexReader.document(tdocs.scoreDocs[i].doc, docVisitor);
						doc = docVisitor.getDocument();
					}
					String id = doc.get("_id");
					Element md = null;
	
					if (fast) {
						md = LuceneSearcher.getMetadataFromIndex(doc, id, false, null, null, null);
					}
                    else if ("index".equals(sFast)) {
					    // Retrieve information from the index for the record
						md = LuceneSearcher.getMetadataFromIndex(doc, id, true, _language == null ? srvContext.getLanguage() : _language, _luceneConfig.getMultilingualSortFields(), _luceneConfig.getDumpFields());
					    
						// Retrieve dynamic properties according to context (eg. editable)
                        gc.getDataManager().buildExtraMetadataInfo(srvContext, id, md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE));
                    }
                    else if (srvContext != null) {
                        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
                        md = gc.getDataManager().getMetadata(srvContext, id, forEditing, withValidationErrors, keepXlinkAttributes);
					}
	
					//--- a metadata could have been deleted just before showing 
					//--- search results
	
					if (md != null) {
						// Calculate score and add it to info elem
						if (_luceneConfig.isTrackDocScores()) {
							Float score = tdocs.scoreDocs[i].score;
							Element info = md.getChild (Edit.RootChild.INFO, Edit.NAMESPACE);
							addElement(info, Edit.Info.Elem.SCORE, score.toString());
						}
						response.addContent(md);
					}
				}
			} else {
				throw new Exception("Failed: Not enough search results ("+tdocs.scoreDocs.length+") available to meet request for "+nrHits+".");
			}
		}
		
		return response;
	}

	/**
	 * Perform a query, loop over results in order to find values containing the search value for a specific field.
	 * 
	 * If the field is not stored in the index, an empty collection is returned.
	 * 
	 * @param srvContext
	 * @param searchField	The field to search in
	 * @param searchValue	The value contained in field's value (case is ignored)
	 * @param maxNumberOfTerms	The maximum number of terms to search for
	 * @param threshold	The minimum frequency for terms to be returned
	 * @return
	 * @throws Exception
	 */
	public Collection<String> getSuggestionForFields(ServiceContext srvContext, 
								final String searchField, final String searchValue, 
								ServiceConfig config,
								int maxNumberOfTerms, int threshold) throws Exception {
		if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
			Log.debug(Geonet.SEARCH_ENGINE, "Get suggestion on field: '"
					+ searchField + "'" + "\tsearching: '" + searchValue + "'"
					+ "\tthreshold: '" + threshold + "'"
					+ "\tmaxNumberOfTerms: '" + maxNumberOfTerms + "'");
		}
		
		// To count the number of values added and stop if maxNumberOfTerms reach
		int counter = 0;
		
		// A collection or a map if threshold is set
		Collection <String> finalValues = new HashSet<String>();
		Map <String, Integer> finalValuesMap = new HashMap<String, Integer>();
		
		GeonetContext gc = null;
		if (srvContext != null) {
			gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
		}
		
		// Search for all current session could search for
		// Do a like query to limit the size of the results
		Element elData = new Element(Jeeves.Elem.REQUEST); // SearchDefaults.getDefaultSearch(srvContext, null);
		elData.addContent(new Element("fast").addContent("index")).
		    addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).addContent(Boolean.toString(false)));
		// FIXME : need more work on LQB
//		if (!searchValue.equals("")) {
//			elData.addContent(new Element(searchField).setText("*" + searchValue + "*"));
//		// TODO : filter template ?
//		}
		search(srvContext, elData, config);

		elData.addContent(new Element("from").setText("1"));
		elData.addContent(new Element("to").setText(getSize() + ""));

		if (getTo() > 0) {
			TopDocs tdocs = performQuery(1, getSize(), false);

			for (int i = 0; i < tdocs.scoreDocs.length; i++) {
				if (counter >= maxNumberOfTerms) {
					break;
				}
				Document doc;

                DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor(Collections.singleton(searchField));
				_indexAndTaxonomy.indexReader.document(tdocs.scoreDocs[i].doc, docVisitor);
				doc = docVisitor.getDocument();

				String[] values = doc.getValues(searchField);
				
				for (int j = 0; j < values.length; ++j) {
					if (searchValue.equals("") || StringUtils.containsIgnoreCase(values[j], searchValue)) {
						if (threshold > 1) {
							// Use a map to save values frequency
							Integer valueFrequency = finalValuesMap.get(values[j]);
							//Log.debug(Geonet.SEARCH_ENGINE, "  " + values[j] + ":" + valueFrequency);
							finalValuesMap.put(values[j], (valueFrequency != null ? ++ valueFrequency : 1));
						} else {
							finalValues.add(values[j]);
						}
						counter ++;
					}
				}
			}
		}
		
		// Filter values which does not reach the threshold
		if (threshold > 1) {
			Map<String, Integer> filteredMap = Maps.filterValues(finalValuesMap, Ranges.atLeast(threshold));
			// Push map content to collection
			finalValues.addAll(filteredMap.keySet());
			
			if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
				Log.debug(Geonet.SEARCH_ENGINE, "  "
						+ filteredMap.size() + "/" + finalValuesMap.size() + " above threshold: " + threshold);
			}
		}

		if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
			Log.debug(Geonet.SEARCH_ENGINE, "  "
					+ finalValues.size() + " returned.");
		}
		return finalValues;
	}
	public int getSize() {
		return _numHits;
	}

    /**
     * TODO javadoc.
     *
     * @return
     * @throws Exception
     */
	public Element getSummary() throws Exception {
		Element response =  new Element("response");
		response.addContent((Element)_elSummary.clone());
		return response;
	}

    private boolean closed = false;

    /**
     * TODO javadoc.
     */
	public synchronized void close() {
        if(!closed) {
            try {
                closed = true;
                if (_indexAndTaxonomy != null) {
                    _sm.releaseIndexReader(_indexAndTaxonomy);
                }
            } catch (IOException e) {
                Log.error(Geonet.SEARCH_ENGINE,"Failed to close Index Reader: "+e.getMessage());
                e.printStackTrace();
            } catch (InterruptedException e) {
                Log.error(Geonet.SEARCH_ENGINE,"Failed to close Index Reader: "+e.getMessage());
                e.printStackTrace();
            }
        }
	}

	//
	// private setup, index, delete and search functions
    //

    /**
     *
     * Determines requested language as follows:
     * - uses value of requestedLanguage search parameter, if it is present;
     * - else uses autodetection, if that is enabled;
     * - else uses servicecontext (GUI) language, if available;
     * - else uses GeoNetwork Default language.
     *
     * @param srvContext
     * @param request
     */
    public static String determineLanguage(ServiceContext srvContext, Element request, SettingInfo settingInfo) {
        if(settingInfo != null && settingInfo.getIgnoreRequestedLanguage()) {
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "requestedlanguage ignored");
            return null;
        }
        String requestedLanguage = request.getChildText("requestedLanguage");
        String finalDetectedLanguage = null;
        // requestedLanguage in request
        if(StringUtils.isNotEmpty(requestedLanguage)) {
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "found requestedlanguage in request: " + requestedLanguage);
            finalDetectedLanguage = requestedLanguage;
        }
        // no requestedLanguage in request
        else {
            boolean detected = false;
            // autodetection is enabled
            if(settingInfo.getAutoDetect()) {
                if(Log.isDebugEnabled(Geonet.LUCENE))
                    Log.debug(Geonet.LUCENE, "auto-detecting request language is enabled");
                    try {
                        String test = request.getChildText("any");
                        if(StringUtils.isNotEmpty(test)) {
                        String detectedLanguage = LanguageDetector.getInstance().detect(test);
                            if(Log.isDebugEnabled(Geonet.LUCENE))
                                Log.debug(Geonet.LUCENE, "automatic language detection: '" + request.getChildText("any") + "' is in language " + detectedLanguage);
                        finalDetectedLanguage = detectedLanguage;
                            detected = true;
                        }
                    }
                catch (Exception x) {
                        Log.error(Geonet.LUCENE, "Error auto-detecting language: " + x.getMessage());
                        x.printStackTrace();
                    }
                }
            else {
                if(Log.isDebugEnabled(Geonet.LUCENE))
                    Log.debug(Geonet.LUCENE, "auto-detecting request language is disabled");
            }
            // autodetection is disabled or detection failed
            if(!detected) {
                if(Log.isDebugEnabled(Geonet.LUCENE))
                    Log.debug(Geonet.LUCENE, "autodetection is disabled or detection failed");


                // servicecontext available
                if (srvContext != null) {
                    if(Log.isDebugEnabled(Geonet.LUCENE))
                        Log.debug(Geonet.LUCENE, "taking language from servicecontext");
                    finalDetectedLanguage = srvContext.getLanguage();
                }
                // no servicecontext available
                else {
                    if(Log.isDebugEnabled(Geonet.LUCENE))
                        Log.debug(Geonet.LUCENE, "taking GeoNetwork default language");
                    finalDetectedLanguage = Geonet.DEFAULT_LANGUAGE; // TODO : set default not language in config
                }
            }
        }
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "determined language is: " + finalDetectedLanguage);
        return finalDetectedLanguage;
    }

    /**
     * TODO javadoc.
     *
     * @param srvContext
     * @param request
     * @param config
     * @throws Exception
     */
	private void computeQuery(ServiceContext srvContext, int endHits, Element request, ServiceConfig config) throws Exception {

        _language = determineLanguage(srvContext, request, _sm.get_settingInfo());
        
		if (srvContext != null) {
			GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
	
			Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);

            @SuppressWarnings("unchecked")
            List<Element> requestedGroups = request.getChildren(SearchParameter.GROUP);
            Set<String> userGroups = gc.getAccessManager().getUserGroups(dbms, srvContext.getUserSession(), srvContext.getIpAddress(), false);
            UserSession userSession = srvContext.getUserSession();
            // unless you are logged in as Administrator, check if you are allowed to query the groups in the query
            if (userSession == null || userSession.getProfile() == null ||
                    ! (userSession.getProfile().equals(Geonet.Profile.ADMINISTRATOR) && userSession.isAuthenticated())) {
            	if(!CollectionUtils.isEmpty(requestedGroups)) {
                    for(Element group : requestedGroups) {
                        if(! "".equals(group.getText()) 
                        		&& ! userGroups.contains(group.getText())) {
                            throw new UnAuthorizedException("You are not authorized to do this.", null);
                        }
                    }
                }
            }

            // remove elements from user input that compromise this request
            for (String fieldName : UserQueryInput.SECURITY_FIELDS){
                request.removeChildren(fieldName);
            }

			// if 'restrict to' is set then don't add any other user/group info
			if ((request.getChild(SearchParameter.GROUP) == null) ||
                (StringUtils.isEmpty(request.getChild(SearchParameter.GROUP).getText().trim()))) {
				for (String group : userGroups) {
					request.addContent(new Element(SearchParameter.GROUP).addContent(group));
                }
                String owner = null;
                if (userSession != null) {
                    owner = userSession.getUserId();
                }
                if (owner != null) {
					request.addContent(new Element(SearchParameter.OWNER).addContent(owner));
                }
			    //--- in case of an admin show all results
                if (userSession != null) {
                    if (userSession.isAuthenticated()) {
                        if (userSession.getProfile().equals(Geonet.Profile.ADMINISTRATOR)) {
                            request.addContent(new Element(SearchParameter.ISADMIN).addContent("true"));
}
                        else if (userSession.getProfile().equals(Geonet.Profile.REVIEWER)) {
                            request.addContent(new Element(SearchParameter.ISREVIEWER).addContent("true"));
}
                    }
                }
            }

			//--- handle the time elements

			processTimeRange(request.getChild(SearchParameter.DATEFROM), "0000-01-01", request.getChild(SearchParameter.DATETO), "9999-01-01");

			//--- some other stuff

            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "CRITERIA:\n"+ Xml.getString(request));

            SettingInfo settingInfo = _sm.get_settingInfo();
            String requestedLanguageOnly = settingInfo.getRequestedLanguageOnly();
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "requestedLanguageOnly: " + requestedLanguageOnly);


            if (_styleSheetName.equals(Geonet.File.SEARCH_Z3950_SERVER)) {
				// Construct Lucene query by XSLT, not Java, for Z3950 anyway :-)
				Element xmlQuery = _sm.transform(_styleSheetName, request);
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                    Log.debug(Geonet.SEARCH_ENGINE, "XML QUERY:\n"+ Xml.getString(xmlQuery));
				_query = LuceneSearcher.makeLocalisedQuery(xmlQuery, SearchManager.getAnalyzer(_language, true), _luceneConfig, _language, requestedLanguageOnly);
			} 
            else {
		        // Construct Lucene query (Java)
                if(Log.isDebugEnabled(Geonet.LUCENE))
                    Log.debug(Geonet.LUCENE, "LuceneSearcher constructing Lucene query (LQB)");
                LuceneQueryInput luceneQueryInput = new LuceneQueryInput(request);
                luceneQueryInput.setRequestedLanguageOnly(requestedLanguageOnly);
                _query = new LuceneQueryBuilder(_tokenizedFieldSet, _luceneConfig.getNumericFields(), SearchManager.getAnalyzer(_language, true), _language).build(luceneQueryInput);
                if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                    Log.debug(Geonet.SEARCH_ENGINE,"Lucene query: " + _query);

                try {
                    // only for debugging -- might cause NPE is query was wrongly constructed
                    //Query rw = _query.rewrite(_indexAndTaxonomy.indexReader);
                    //if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) Log.debug(Geonet.SEARCH_ENGINE,"Rewritten Lucene query: " + _query);
                    //System.out.println("** rewritten:\n"+ rw);
                }
                catch(Throwable x){
                    Log.warning(Geonet.SEARCH_ENGINE,"Error rewriting Lucene query: " + _query);
                    //System.out.println("** error rewriting query: "+x.getMessage());
                }
			}
		    
			// Boosting query
			if (_boostQueryClass != null) {
				try {
                    if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                        Log.debug(Geonet.SEARCH_ENGINE, "Create boosting query:" + _boostQueryClass);
					Class boostClass = Class.forName(_boostQueryClass);
					Class[] clTypesArray = _luceneConfig.getBoostQueryParameterClass();				
					Object[] inParamsArray = _luceneConfig.getBoostQueryParameter(); 

					Class[] clTypesArrayAll = new Class[clTypesArray.length + 1];
					clTypesArrayAll[0] = Class.forName("org.apache.lucene.search.Query");

                    System.arraycopy(clTypesArray, 0, clTypesArrayAll, 1, clTypesArray.length);
					Object[] inParamsArrayAll = new Object[inParamsArray.length + 1];
					inParamsArrayAll[0] = _query;
                    System.arraycopy(inParamsArray, 0, inParamsArrayAll, 1, inParamsArray.length);
					try {
                        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                            Log.debug(Geonet.SEARCH_ENGINE, "Creating boost query with parameters:" + Arrays.toString(inParamsArrayAll));
						Constructor c = boostClass.getConstructor(clTypesArrayAll);
						_query = (Query) c.newInstance(inParamsArrayAll);
					} catch (Exception e) {
						Log.warning(Geonet.SEARCH_ENGINE, " Failed to create boosting query: " + e.getMessage() 
								+ ". Check Lucene configuration");
						e.printStackTrace();
					}	
				} catch (Exception e1) {
					Log.warning(Geonet.SEARCH_ENGINE, " Error on boosting query initialization: " + e1.getMessage()
							+ ". Check Lucene configuration");
				}
			}
			
		    // Use RegionsData rather than fetching from the DB everytime
		    //
		    //request.addContent(Lib.db.select(dbms, "Regions", "region"));
//		    Element regions = RegionsData.getRegions(dbms);
//		    request.addContent(regions);
		}

        /*
        TODO heikki why was it set again here? Seems wrong
		if (srvContext != null)
        	_language = srvContext.getLanguage();
        else
        	_language = Geonet.DEFAULT_LANGUAGE; // TODO : set default not language in config

		*/

		Geometry geometry = getGeometry(request);
		SpatialFilter spatialfilter = null;
        if (geometry != null) {
            if (_sm.getLogSpatialObject()) {
                _geomWKT = geometry.toText();
            }
            spatialfilter = _sm.getSpatial().filter(_query, Integer.MAX_VALUE, geometry, request);
        }

        Filter duplicateRemovingFilter = new DuplicateDocFilter(_query, 1000000);
        Filter filter;
        if (spatialfilter == null) {
            filter = duplicateRemovingFilter;
        } else {
            Filter[] filters = new Filter[]{duplicateRemovingFilter, spatialfilter};
            filter = new ChainedFilter(filters, ChainedFilter.AND);
        }

        _filter = new CachingWrapperFilter(filter);

        String sortBy = Util.getParam(request, Geonet.SearchResult.SORT_BY, Geonet.SearchResult.SortBy.RELEVANCE);
		boolean sortOrder = (Util.getParam(request, Geonet.SearchResult.SORT_ORDER, "").equals(""));
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Sorting by : " + sortBy);

        SettingInfo settingInfo = _sm.get_settingInfo();
        boolean sortRequestedLanguageOnTop = settingInfo.getRequestedLanguageOnTop();
        if(Log.isDebugEnabled(Geonet.LUCENE))
            Log.debug(Geonet.LUCENE, "sortRequestedLanguageOnTop: " + sortRequestedLanguageOnTop);

        _sort = LuceneSearcher.makeSort(Collections.singletonList(Pair.read(sortBy, sortOrder)), _language, sortRequestedLanguageOnTop);
		
		_resultType = config.getValue(Geonet.SearchResult.RESULT_TYPE, Geonet.SearchResult.ResultType.HITS);
		/* resultType is not specified in search params - it's in config?
		Content child = request.getChild(Geonet.SearchResult.RESULT_TYPE);
        if (child == null) {
            _resultType = Geonet.SearchResult.ResultType.HITS;
        }
        else {
            _resultType = child.getValue();
        }
			*/
	}

    /**
     * TODO javadoc.
     *
     * @param fromTime
     * @param defaultFromTime
     * @param toTime
     * @param defaultToTime
     */
	private void processTimeRange(Element fromTime, String defaultFromTime, Element toTime, String defaultToTime) {
		if (fromTime != null && toTime != null) { 
			if (fromTime.getTextTrim().equals("") && 
								 toTime.getTextTrim().equals("")) {
				fromTime.detach(); toTime.detach();
			} else {
				if (fromTime.getTextTrim().equals("")) {
					fromTime.setText(defaultFromTime);
				} else if (toTime.getTextTrim().equals("")) {
					toTime.setText(defaultToTime);
				}
				String newFromTime = JODAISODate.parseISODateTime(fromTime.getText());
				fromTime.setText(newFromTime);	
				String newToTime = JODAISODate.parseISODateTime(toTime.getText());
				toTime.setText(newToTime);	
			}
		}
	}

	/**
	 * Executes Lucene query with sorting option.
	 * 
	 * Default sort by option is RELEVANCE.
	 * Default sort order option is not reverse order. Reverse order is active 
	 * if sort order option is set and not null
     * @param startHit start
	 * @param endHit end
	 * @param buildSummary Compute summary. If true, checks if not already generated (by previous search)
     * @return topdocs
     * @throws Exception hmm
     */
	private TopDocs performQuery(int startHit, int endHit, boolean buildSummary) throws Exception {

		int numHits;
		
		boolean computeSummary = false;
		if (buildSummary) {
			computeSummary = _elSummary == null;
			if (computeSummary) {
				// get as many results as instructed or enough for search summary
				numHits = Math.max(_maxHitsInSummary,endHit);
			} else {
				numHits = endHit;
			}	
		} else {
			numHits = endHit;
		}
		
		Pair<TopDocs,Element> results = doSearchAndMakeSummary( endHit, startHit, endHit, 
				_language, _luceneConfig.getTaxonomy().get(_resultType), _indexAndTaxonomy.indexReader, 
				_query, _filter, _sort, _indexAndTaxonomy.taxonomyReader, buildSummary, _luceneConfig.isTrackDocScores(),
				_luceneConfig.isTrackMaxScore(), _luceneConfig.isDocsScoredInOrder()
		);
		
		TopDocs hits = results.one();
		_elSummary = results.two();
		_numHits = Integer.parseInt(_elSummary.getAttributeValue("count"));

        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Hits found : "+_numHits+"");
		
		return hits;
	}

    /**
     * TODO javadoc.
     *
     * @param request
     * @return
     * @throws Exception
     */
	private Geometry getGeometry(Element request) throws Exception {
        String geomWKT = Util.getParam(request, Geonet.SearchResult.GEOMETRY, null);
        if (geomWKT != null) {
            WKTReader reader = new WKTReader();
            return reader.read(geomWKT);
        }
        return null;
    }

    /**
     * Creates the Sort to use in the search.
     *
     * @param fields
     * @param requestLanguage
     * @param sortRequestedLanguageOnTop
     * @return
     */
    public static Sort makeSort(List<Pair<String, Boolean>> fields, String requestLanguage,
                                boolean sortRequestedLanguageOnTop) {
        List<SortField> sortFields = new ArrayList<SortField>();
        if (sortRequestedLanguageOnTop && requestLanguage != null) {
            // Add a sort so the metadata defined in the requested language are the first metadata in results
            sortFields.add(new LangSortField(requestLanguage));
        }
        for (Pair<String, Boolean> sortBy : fields) {
            if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Sorting by : " + sortBy);
            SortField sortField = LuceneSearcher.makeSortField(sortBy.one(), sortBy.two(), requestLanguage);
            if( sortField!=null ) sortFields.add(sortField);
        }
        sortFields.add(SortField.FIELD_SCORE);
        return new Sort(sortFields.toArray(new SortField[sortFields.size()]));
    }
    
    /**
     * Defines sort field. By default, the field is assumed to be a string.
     * Only popularity and rating are sorted based on integer type.
     * In order to works well sort field needs to be not tokenized in Lucene index.
     * 
     * Relevance is the default Lucene sorting mechanism.
     * 
     * @param sortBy sort field
     * @param sortOrder sort order
     * @param searchLang if non-null then the sorter will take into account translation (if possible)
     * @return sortfield
     */
    private static SortField makeSortField(String sortBy, boolean sortOrder, String searchLang) {
        SortField.Type sortType = SortField.Type.STRING;

        if( sortBy.equals(Geonet.SearchResult.SortBy.RELEVANCE) ){
            return null;
        }
        
        // FIXME : here we should be able to define field type ?
        // Add "_" prefix for internal fields. Maybe we should
        // update that in DataManager indexMetadata to have the list of
        // internal Lucene fields (ie. not defined in index-fields.xsl).
        if (sortBy.equals(Geonet.SearchResult.SortBy.POPULARITY)
        		|| sortBy.equals(Geonet.SearchResult.SortBy.RATING)) {
            sortType = SortField.Type.INT;
            sortBy = "_" + sortBy;
        } else if (sortBy.equals(Geonet.SearchResult.SortBy.SCALE_DENOMINATOR)) {
            sortType = SortField.Type.INT;
        } else if (sortBy.equals(Geonet.SearchResult.SortBy.DATE) 
        		|| sortBy.equals(Geonet.SearchResult.SortBy.TITLE)) {
            sortBy = "_" + sortBy;
        }
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Sort by: " + sortBy + " order: " + sortOrder + " type: " + sortType);
        if (sortType == org.apache.lucene.search.SortField.Type.STRING) {
            if(searchLang != null) {
                return new SortField(sortBy, new CaseInsensitiveFieldComparatorSource(searchLang), sortOrder);
            } else {
                return new SortField(sortBy, CaseInsensitiveFieldComparatorSource.languageInsensitiveInstance(), sortOrder);
            }
        }
        return new SortField(sortBy, sortType, sortOrder);
    }

    /**
     * TODO javadoc.
     *
     * @param xmlQuery
     * @param analyzer
     * @param tokenizedFieldSet
     * @param numericFieldSet
     * @param langCode
     * @param requestedLanguageOnly
     * @return
     * @throws Exception
     */
    public static Query makeLocalisedQuery( Element xmlQuery, PerFieldAnalyzerWrapper analyzer,
                                            LuceneConfig luceneConfig, String langCode,
                                            String requestedLanguageOnly)
            throws Exception {
        Query returnValue = LuceneSearcher.makeQuery(xmlQuery, analyzer, luceneConfig);
        if(StringUtils.isNotEmpty(langCode)) {
            returnValue = LuceneQueryBuilder.addLocaleTerm(returnValue, langCode, requestedLanguageOnly);
        }
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Lucene Query: " + returnValue.toString());
        return returnValue;
    }

    /**
     * Makes a new lucene query.
     *
     *  If the field to be queried is tokenized then this method applies
     *  the appropriate analyzer (see SearchManager) to the field.
     *
     * @param xmlQuery
     * @param analyzer
     * @param tokenizedFieldSet
     * @param numericFieldSet
     * @return
     * @throws Exception
     */
	@SuppressWarnings({"deprecation"})
    private static Query makeQuery(Element xmlQuery, PerFieldAnalyzerWrapper analyzer, LuceneConfig luceneConfig) throws Exception {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "MakeQuery input XML:\n" + Xml.getString(xmlQuery));
		String name = xmlQuery.getName();
		Query returnValue;
		
		Set<String> tokenizedFieldSet = luceneConfig.getTokenizedField();
        Map<String, LuceneConfigNumericField> numericFieldSet = luceneConfig.getNumericFields();
        if (name.equals("TermQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
            returnValue = LuceneSearcher.textFieldToken(xmlQuery.getAttributeValue("txt"), fld, xmlQuery.getAttributeValue("sim"), analyzer, tokenizedFieldSet);
		}
		else if (name.equals("FuzzyQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
            returnValue = LuceneSearcher.textFieldToken(xmlQuery.getAttributeValue("txt"), fld, xmlQuery.getAttributeValue("sim"), analyzer, tokenizedFieldSet);
		}
		else if (name.equals("PrefixQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = LuceneSearcher.analyzeQueryText(fld, xmlQuery.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
			returnValue = new PrefixQuery(new Term(fld, txt));
		}
		else if (name.equals("MatchAllDocsQuery"))
		{
            return new MatchAllDocsQuery();
		}
		else if (name.equals("WildcardQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
            returnValue = LuceneSearcher.textFieldToken(xmlQuery.getAttributeValue("txt"), fld, xmlQuery.getAttributeValue("sim"), analyzer, tokenizedFieldSet);
		}
		else if (name.equals("PhraseQuery"))
		{
			PhraseQuery query = new PhraseQuery();
            for (Object o : xmlQuery.getChildren()) {
                Element xmlTerm = (Element) o;
                String fld = xmlTerm.getAttributeValue("fld");
                String txt = LuceneSearcher.analyzeQueryText(fld, xmlTerm.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
				if(txt.length() > 0) { 
					query.add(new Term(fld, txt));
				} 				
            }
			returnValue = query;
		}
		else if (name.equals("RangeQuery"))
		{
			String  fld        = xmlQuery.getAttributeValue("fld");
			String  lowerTxt   = xmlQuery.getAttributeValue("lowerTxt");
			String  upperTxt   = xmlQuery.getAttributeValue("upperTxt");
			String  sInclusive = xmlQuery.getAttributeValue("inclusive");
			boolean inclusive  = "true".equals(sInclusive);

			LuceneConfigNumericField fieldConfig = numericFieldSet .get(fld);
			if (fieldConfig != null) {
				returnValue = LuceneQueryBuilder.buildNumericRangeQueryForType(fld, lowerTxt, upperTxt, inclusive, inclusive, fieldConfig.getType());
			} else {
				lowerTxt = (lowerTxt == null ? null : LuceneSearcher.analyzeQueryText(fld, lowerTxt, analyzer, tokenizedFieldSet));
				upperTxt = (upperTxt == null ? null : LuceneSearcher.analyzeQueryText(fld, upperTxt, analyzer, tokenizedFieldSet));

				returnValue = TermRangeQuery.newStringRange(fld, lowerTxt, upperTxt, inclusive, inclusive);
			}
		}
		else if (name.equals("DateRangeQuery"))
		{
			String  fld        = xmlQuery.getAttributeValue("fld");
			String  lowerTxt   = xmlQuery.getAttributeValue("lowerTxt");
			String  upperTxt   = xmlQuery.getAttributeValue("upperTxt");
			String  sInclusive = xmlQuery.getAttributeValue("inclusive");
			returnValue = new DateRangeQuery(fld, lowerTxt, upperTxt, sInclusive);
		}
		else if (name.equals("BooleanQuery"))
		{
			BooleanQuery query = new BooleanQuery();
            for (Object o : xmlQuery.getChildren()) {
                Element xmlBooleanClause = (Element) o;
                String sRequired = xmlBooleanClause.getAttributeValue("required");
                String sProhibited = xmlBooleanClause.getAttributeValue("prohibited");
                boolean required = sRequired != null && sRequired.equals("true");
                boolean prohibited = sProhibited != null && sProhibited.equals("true");
                BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(required, prohibited);
                @SuppressWarnings(value = "unchecked")
                List<Element> subQueries = xmlBooleanClause.getChildren();
                Element xmlSubQuery;
                if (subQueries != null && subQueries.size() != 0) {
                    xmlSubQuery = subQueries.get(0);

                     Query subQuery = LuceneSearcher.makeQuery(xmlSubQuery, analyzer, luceneConfig);

                    // If xmlSubQuery contains only a stopword the query produced is null. Protect against this
                    if (subQuery != null) {
                        query.add(subQuery, occur);
                    }
                }
            }
			BooleanQuery.setMaxClauseCount(16384); // FIXME: quick fix; using Filters should be better
			
			returnValue = query;
		}
		else throw new Exception("unknown lucene query type: " + name);

        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Lucene Query: " + ((returnValue != null)?returnValue.toString():""));
		return returnValue;
	}

    /**
     * TODO javadoc.
     *
     * @param string
     * @param luceneIndexField
     * @param similarity
     * @param analyzer
     * @param tokenizedFieldSet
     * @return
     */
    private static Query textFieldToken(String string, String luceneIndexField, String similarity,
                                    PerFieldAnalyzerWrapper analyzer, Set<String> tokenizedFieldSet) {
            if(string == null) {
                throw new IllegalArgumentException("Cannot create Lucene query for null string");
            }
            Query query = null;

            String analyzedString = "";
            // wildcards - preserve them by analyzing the parts of the search string around them separately
            // (this is because Lucene's StandardTokenizer would remove wildcards, but that's not what we want)
            if(string.indexOf('*') >= 0 || string.indexOf('?') >= 0) {
                String starsPreserved = "";
                String[] starSeparatedList = string.split("\\*");
                for(String starSeparatedPart : starSeparatedList) {
                    String qPreserved = "";
                    // ? present
                    if(starSeparatedPart.indexOf('?') >= 0) {
                        String[] qSeparatedList = starSeparatedPart.split("\\?");
                        for(String qSeparatedPart : qSeparatedList) {
                            String analyzedPart = LuceneSearcher.analyzeQueryText(luceneIndexField, qSeparatedPart, analyzer, tokenizedFieldSet);
                            qPreserved += '?' + analyzedPart;
                        }
                        // remove leading ?
                        qPreserved = qPreserved.substring(1);
                        starsPreserved += '*' + qPreserved;
                    }
                    // no ? present
                    else {
                        starsPreserved += '*' + LuceneSearcher.analyzeQueryText(luceneIndexField, starSeparatedPart, analyzer, tokenizedFieldSet);
                    }
                }
                // remove leading *
                if (!org.apache.commons.lang.StringUtils.isEmpty(starsPreserved)) {
                    starsPreserved = starsPreserved.substring(1);
                }

                // restore ending wildcard
                if (string.endsWith("*")) {
                    starsPreserved += "*";
                } else if (string.endsWith("?")) {
                    starsPreserved += "?";
                }

                analyzedString = starsPreserved;
            }
            // no wildcards
            else {
                analyzedString = LuceneSearcher.analyzeQueryText(luceneIndexField, string, analyzer, tokenizedFieldSet);
            }

            return LuceneQueryBuilder.constructQueryFromAnalyzedString(string, luceneIndexField, similarity, query, analyzedString, tokenizedFieldSet);
        }


	
	/**
	 * Do Lucene search and optionally build a summary for the search.
	 * 
	 * @param numHits	the maximum number of hits to collect
	 * @param startHit	the start hit to return in the TopDocs if not building summary
	 * @param endHit	the end hit to return in the TopDocs if not building summary
	 * @param langCode	the language code used by SummaryComparator
	 * @param summaryConfig	the summary configuration
	 * @param reader  reader
	 * @param query   query
	 * @param cFilter filter
	 * @param sort	the sort criteria
	 * @param taxonomyReader	A {@link TaxonomyReader} use to compute facets (ie. summary)
	 * @param buildSummary	true to build query summary element. Summary is stored in the second element of the returned Pair.
	 * @param trackDocScores	specifies whether document scores should be tracked and set on the results. 
	 * @param trackMaxScore	specifies whether the query's maxScore should be tracked and set on the resulting TopDocs.
	 * @param docsScoredInOrder	specifies whether documents are scored in doc Id order or not by the given Scorer
	 * @return	the topDocs for the search. When building summary, topDocs will contains all search hits
	 * and need to be filtered to return only required hits according to search parameters.
	 * 
	 * @throws Exception hmm
	 */
	public static Pair<TopDocs, Element> doSearchAndMakeSummary(int numHits, int startHit, int endHit, String langCode, 
			Map<String, FacetConfig> summaryConfig, IndexReader reader, 
			Query query, Filter cFilter, Sort sort, TaxonomyReader taxonomyReader, boolean buildSummary, boolean trackDocScores,
			boolean trackMaxScore, boolean docsScoredInOrder) throws Exception
	{
		Log.debug(Geonet.SEARCH_ENGINE, "Build summary: " + buildSummary);
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Setting up the TFC with numHits "+numHits);
		TopFieldCollector tfc = TopFieldCollector.create(sort, numHits, true, trackDocScores, trackMaxScore, docsScoredInOrder);

        if(query != null && reader != null ){
            // too dangerous to do this only for logging, as it may throw NPE if Query was not constructed correctly
            // However if you need to see what Lucene queries are really used, print the rewritten query instead
            // Query rw = query.rewrite(reader);
            // System.out.println("Lucene query: " + rw.toString());
            if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Lucene query: " + query.toString());
        }
        IndexSearcher searcher = new IndexSearcher(reader);

        Element elSummary= new Element("summary");

        if (taxonomyReader != null && buildSummary) {
        	// configure facets from configuration file
        	FacetSearchParams fsp = buildFacetSearchParams(summaryConfig);
        	FacetsCollector facetCollector = new FacetsCollector(fsp,
        			reader, taxonomyReader);
        	
        	searcher.search(query, cFilter, MultiCollector.wrap(tfc, facetCollector));
        	try {

            	buildFacetSummary(elSummary, summaryConfig, facetCollector, langCode);
			} catch (Exception e) {
				e.printStackTrace();
				Log.warning(Geonet.FACET_ENGINE, "BuildFacetSummary error. " + e.getMessage());
			}
			
        } else {
            searcher.search(query, cFilter, tfc);
        }
		elSummary.setAttribute("count", tfc.getTotalHits()+"");
		elSummary.setAttribute("type", "local");
		if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE)) {
            Log.debug(Geonet.SEARCH_ENGINE, " Get top docs from " + startHit + " ... " + endHit + " (total: " + tfc.getTotalHits() + ")");
		}
		TopDocs tdocs = tfc.topDocs(startHit, endHit);

		return Pair.read(tdocs,elSummary);
	}

	/**
	 * Create an XML summary from the search facets collector.
	 * 
	 * @param elSummary	The element in which to add the facet report
	 * @param summaryConfigValues	The summary configuration
	 * @param facetCollector
	 * @param langCode 
	 * @throws IOException
	 */
    private static void buildFacetSummary(Element elSummary,
            Map<String, FacetConfig> summaryConfigValues,
            FacetsCollector facetCollector, String langCode) throws IOException {
        DecimalFormat doubleFormat = new DecimalFormat("0");

        try {
            for (Iterator<FacetResult> iterator = facetCollector
                    .getFacetResults().iterator(); iterator.hasNext();) {
                FacetResult result = (FacetResult) iterator.next();

                String label = result.getFacetResultNode().getLabel()
                        .toString();
                FacetConfig config = summaryConfigValues.get(label);
                String facetName = config.getPlural();


                Translator translator;
                if (ServiceContext.get() != null) {
                    try {
                        ServiceContext context = ServiceContext.get();
                        
                        translator = config.getTranslator(context, langCode);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    translator = Translator.NULL_TRANSLATOR;
                }
                
                Element facets = new Element(facetName);
                FacetResultNode frn = result.getFacetResultNode();
                if (frn.getNumSubResults() != 0) {

                    Map<String, Double> facetValues = new LinkedHashMap<String, Double>();

                    // facetValues = new TreeMap<String, Double>(comparator)
                    for (Iterator subresults = frn.getSubResults().iterator(); subresults
                            .hasNext();) {
                        FacetResultNode node = (FacetResultNode) subresults
                                .next();
                        facetValues.put(node.getLabel().lastComponent(),
                                node.getValue());
                    }
                    List<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(
                            facetValues.entrySet());

                    if (Log.isDebugEnabled(Geonet.FACET_ENGINE)) {
                        Log.debug(Geonet.FACET_ENGINE, facetName
                                + ":\tSorting facet by " + config.getSortBy().toString()
                                + " (" + config.getSortOrder().toString() + ")");
                    }

                    // No need for a custom comparator Lucene facet request is
                    // made by count descending order
                    if (Facet.SortBy.COUNT != config.getSortBy()) {
                        Comparator c = null;
                        if (Facet.SortBy.NUMVALUE == config.getSortBy()) {
                            // Create a numeric comparator
                            c = new Comparator<Entry<String, Double>>() {
                                public int compare(
                                        final Entry<String, Double> e1,
                                        final Entry<String, Double> e2) {
                                    try {
                                        Double d1 = Double.valueOf(e1.getKey());
                                        Double d2 = Double.valueOf(e2.getKey());

                                        return d1.compareTo(d2);
                                    } catch (NumberFormatException e) {
                                        // String comparison
                                        Log.warning(
                                                Geonet.FACET_ENGINE,
                                                "Failed to compare numeric values ("
                                                        + e1.getKey()
                                                        + " / "
                                                        + e2.getKey()
                                                        + ") for facet. Check sortBy option in summary configuration.");
                                        return e1.getKey().compareTo(
                                                e2.getKey());
                                    }
                                }
                            };
                        } else {
                            c = new Comparator<Entry<String, Double>>() {
                                public int compare(
                                        final Entry<String, Double> e1,
                                        final Entry<String, Double> e2) {
                                    return e1.getKey().compareTo(e2.getKey());
                                }
                            };
                        }
                        Collections.sort(entries, c);

                        if (Facet.SortOrder.DESCENDING == config.getSortOrder()) {
                            Collections.reverse(entries);
                        }
                    }
                    for (Entry<String, Double> entry : entries) {
                        String facetValue = entry.getKey();
                        String facetCount = doubleFormat.format(entry
                                .getValue());

                        if (Log.isDebugEnabled(Geonet.FACET_ENGINE)) {
                            Log.debug(Geonet.FACET_ENGINE, " - " + facetValue
                                    + " (" + facetCount + ")");
                        }
                        
                        String translatedValue = translator.translate(facetValue);
                        
                        Element facet = new Element(config.getName());
                        facet.setAttribute("count", facetCount);
                        facet.setAttribute("name", facetValue);
                        if (translatedValue != null) {
                            facet.setAttribute("label", translatedValue);
                        }
                        facets.addContent(facet);
                    }
                }
                elSummary.addContent(facets);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.error(
                    Geonet.FACET_ENGINE,
                    "Check facet configuration. This may happen when a facet is configured"
                            + " but does not exist in the taxonomy index. Error is: "
                            + e.getMessage(), e);
            e.printStackTrace();
        }
    }

	/**
	 * Build facet search params according to the summary configuration file.
	 * 
	 * FacetRequest sort order and sort by option is the default (ie. by count descending).
	 * Then the results may be sorted when creating the summary {@link #buildFacetSummary(Element, Map, FacetsCollector)}.
	 * 
	 * @param summaryConfigValues
	 * @return
	 */
	private static FacetSearchParams buildFacetSearchParams(
			Map<String, FacetConfig> summaryConfigValues) {
		FacetSearchParams fsp = new FacetSearchParams();
		
		for (String key : summaryConfigValues.keySet()) {
			FacetConfig config = summaryConfigValues.get(key);
			
			int max = config.getMax();
			
			FacetRequest facetRequest = new CountFacetRequest(
					new CategoryPath(key), max);
			facetRequest.setSortBy(SortBy.VALUE);
			facetRequest.setSortOrder(SortOrder.DESCENDING);
			fsp.addFacetRequest(facetRequest);
		}
		return fsp;
	}

	/**
	 * Retrieves metadata from the index.
	 * 
	 * @param doc
	 * @param id
	 * @param dumpAllField	If dumpFields is null and dumpAllField set to true, dump all index content.
	 * @param dumpFields	If not null, dump only the fields define in {@link LuceneConfig#getDumpFields()}.
	 * @return
	 */
	private static Element getMetadataFromIndex(Document doc, String id, boolean dumpAllField, String searchLang, Set<String> multiLangSearchTerm, Map<String, String> dumpFields){
        // Retrieve the info element
        String root       = doc.get("_root");
        String schema     = doc.get("_schema");
        String source     = doc.get("_source");
        String uuid       = doc.get("_uuid");
        
        String createDate = doc.get("_createDate");
        if (createDate != null) createDate = createDate.toUpperCase();
        String changeDate = doc.get("_changeDate");
        if (changeDate != null) changeDate = changeDate.toUpperCase();
        
        // Root element is using root element name if not using only the index content (ie. dumpAllField)
        // probably because the XSL need that info later ?
        Element md = new Element("metadata");
        
        Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);
        
        addElement(info, Edit.Info.Elem.ID,          id);
        addElement(info, Edit.Info.Elem.UUID,        uuid);
        addElement(info, Edit.Info.Elem.SCHEMA,      schema);
        addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
        addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
        addElement(info, Edit.Info.Elem.SOURCE,      source);

        HashSet<String> addedTranslation = new HashSet<String>();
        if ((dumpAllField || dumpFields != null) && searchLang != null && multiLangSearchTerm != null) {
            // get the translated fields and dump those instead of the non-translated
            for (String fieldName : multiLangSearchTerm) {
                IndexableField[] values = doc.getFields(LuceneConfig.multilingualSortFieldName(fieldName, searchLang));
                for (IndexableField f : values) {
                    if(f != null) {
                        String stringValue = f.stringValue();
                        if(!stringValue.trim().isEmpty()) {
                            addedTranslation.add(fieldName);
                            md.addContent(new Element(dumpFields.get(fieldName)).setText(stringValue));
                        }
                    }
                }
            }
        }
        if(addedTranslation.isEmpty()) {
            addedTranslation = null;
        }
        if (dumpFields != null) {
            for (String fieldName : dumpFields.keySet()) {
                IndexableField[] values = doc.getFields(fieldName);
                for (IndexableField f : values) {
                    if (f != null) {
                        if(addedTranslation == null || !addedTranslation.contains(fieldName)) {
                            md.addContent(new Element(dumpFields.get(fieldName)).setText(f.stringValue()));
                        }
                    }
                }
            }
        }
        else {
	        List<IndexableField> fields = doc.getFields();
            for (IndexableField field : fields) {
                String fieldName = field.name();
                String fieldValue = field.stringValue();

                // Dump the categories to the info element
                if (fieldName.equals("_cat")) {
                    addElement(info, Edit.Info.Elem.CATEGORY, fieldValue);
                } else if (dumpAllField && (addedTranslation == null || !addedTranslation.contains(fieldName))) {
                    // And all other field to the root element in dump all mode
                    md.addContent(new Element(fieldName).setText(fieldValue));
                }
            }
        }	
        md.addContent(info);
        return md;
	}

	/**
	 * <p>
	 * Gets all metadata uuids in current searcher.
	 * </p>
	 * 
	 * @param maxHits max hits
	 * @return current searcher result in "fast" mode
	 * 
	 * @throws Exception hmm
	 */
    public List<String> getAllUuids(int maxHits) throws Exception {
        List<String> response = new ArrayList<String>();
		TopDocs tdocs = performQuery(0, maxHits, false);

        for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
            DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor("_uuid");
            _indexAndTaxonomy.indexReader.document(sdoc.doc, docVisitor);
            Document doc = docVisitor.getDocument();
            String uuid = doc.get("_uuid");
            if (uuid != null) response.add(uuid);
        }
        return response;
    }

    /**
     * <p>
     * Gets all metadata info as a int Map in current searcher.
     * </p>
     *
     * @param maxHits
     * @return
     * @throws Exception
     */
    public Map<Integer,MdInfo> getAllMdInfo(int maxHits) throws Exception {

      Map<Integer,MdInfo> response = new HashMap<Integer,MdInfo>();
			TopDocs tdocs = performQuery(0, maxHits, false);

      for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
          DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor("_id", "_root", "_schema", "_createDate", "_changeDate",
                  "_source", "_isTemplate", "_title", "_uuid", "_isHarvested", "_owner", "_groupOwner");
          _indexAndTaxonomy.indexReader.document(sdoc.doc, docVisitor);
          Document doc = docVisitor.getDocument();

          MdInfo mdInfo = new MdInfo();
          mdInfo.id           = doc.get("_id");
          mdInfo.uuid         = doc.get("_uuid");
          mdInfo.schemaId     = doc.get("_schema");
          String isTemplate   = doc.get("_isTemplate");
          if (isTemplate.equals("y")) {
              mdInfo.template = MdInfo.Template.TEMPLATE;
          }
          else if (isTemplate.equals("s")) {
              mdInfo.template = MdInfo.Template.SUBTEMPLATE;
          }
          else {
              mdInfo.template = MdInfo.Template.METADATA;
          }
          String isHarvested  = doc.get("_isHarvested");
          if (isHarvested != null) {
              mdInfo.isHarvested  = doc.get("_isHarvested").equals("y");
          }
          else {
              mdInfo.isHarvested  = false;
          }
          mdInfo.createDate   = doc.get("_createDate");
          mdInfo.changeDate   = doc.get("_changeDate");
          mdInfo.source       = doc.get("_source");
          mdInfo.title        = doc.get("_title");
          mdInfo.root         = doc.get("_root");
          mdInfo.owner        = doc.get("_owner");
          mdInfo.groupOwner   = doc.get("_groupOwner");

          response.put(Integer.parseInt(mdInfo.id), mdInfo);
      }
      return response;
    }

    /**
     * Searches in Lucene index and return Lucene index field value. Metadata records is retrieved based on its uuid.
     *
     * @param webappName
     * @param priorityLang
     * @param id
     * @param fieldname
     * @return
     * @throws Exception
     */
    public static String getMetadataFromIndex(String webappName, String priorityLang, String id, String fieldname) throws Exception {
            return LuceneSearcher.getMetadataFromIndex(webappName, priorityLang, id, Collections.singleton(fieldname)).get(fieldname);
    }

    /**
     * TODO javadoc.
     *
     * @param webappName
     * @param priorityLang
     * @param id
     * @param fieldname
     * @return
     * @throws Exception
     */
    public static String getMetadataFromIndexById(String webappName, String priorityLang, String id, String fieldname) throws Exception {
            return LuceneSearcher.getMetadataFromIndex(webappName, priorityLang, "_id", id, Collections.singleton(fieldname)).get(fieldname);
    }

    /**
     * TODO javadoc.
     *
     * @param webappName
     * @param priorityLang
     * @param uuid
     * @param fieldnames
     * @return
     * @throws Exception
     */
    private static Map<String,String> getMetadataFromIndex(String webappName, String priorityLang, String uuid, Set<String> fieldnames) throws Exception {
        return LuceneSearcher.getMetadataFromIndex(webappName, priorityLang, "_uuid", uuid, fieldnames);
    }

    /**
     * TODO javadoc.
     *
     * @param webappName
     * @param priorityLang
     * @param idField
     * @param id
     * @param fieldnames
     * @return
     * @throws Exception
     */
    private static Map<String,String> getMetadataFromIndex(String webappName, String priorityLang, String idField, String id, Set<String> fieldnames) throws Exception {
        final IndexAndTaxonomy indexAndTaxonomy;
        final SearchManager searchmanager;
        ServiceContext context = ServiceContext.get();
        GeonetworkMultiReader reader;
        if (context != null) {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            searchmanager = gc.getSearchmanager();
            indexAndTaxonomy = searchmanager.getNewIndexReader(priorityLang);
            reader = indexAndTaxonomy.indexReader;
        } else {
            throw new IllegalStateException("There needs to be a ServiceContext in the thread local for this thread");
        }
        IndexSearcher searcher = new IndexSearcher(reader);

        Map<String, String> values = new HashMap<String, String>();

        try {
            TermQuery query = new TermQuery(new Term(idField, id));
            SettingInfo settingInfo = _sm.get_settingInfo();
            boolean sortRequestedLanguageOnTop = settingInfo.getRequestedLanguageOnTop();
            if(Log.isDebugEnabled(Geonet.LUCENE))
                Log.debug(Geonet.LUCENE, "sortRequestedLanguageOnTop: " + sortRequestedLanguageOnTop);
            
            Sort sort = LuceneSearcher.makeSort(Collections.<Pair<String, Boolean>>emptyList(), priorityLang, sortRequestedLanguageOnTop);
            Filter filter = NoFilterFilter.instance();
            TopDocs tdocs = searcher.search(query, filter, 1, sort);

            for( ScoreDoc sdoc : tdocs.scoreDocs ) {
                DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor(fieldnames);
                reader.document(sdoc.doc, docVisitor);
                Document doc = docVisitor.getDocument();
                
                for( String fieldname : fieldnames ) {
                    values.put(fieldname, doc.get(fieldname));
                }
            }

        } catch (CorruptIndexException e) {
            // TODO: handle exception
            Log.error(Geonet.LUCENE, e.getMessage());
        }
        catch (IOException e) {
            // TODO: handle exception
            Log.error(Geonet.LUCENE, e.getMessage());
        } finally {
            searchmanager.releaseIndexReader(indexAndTaxonomy);
        }
        return values;
    }

    /**
     * TODO javadoc.
     *
     * @param field
     * @param aText
     * @param analyzer
     * @param tokenizedFieldSet
     * @return
     */
	protected static String analyzeQueryText(String field, String aText, PerFieldAnalyzerWrapper analyzer, Set<String> tokenizedFieldSet) {
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Analyze field "+field+" : "+aText);
		if (tokenizedFieldSet.contains(field)) {
			String analyzedText = LuceneSearcher.analyzeText(field, aText, analyzer);
            if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Analyzed text is "+analyzedText);
			return analyzedText;
		}
        else return aText;
	}

    /**
     * Splits text into tokens using the Analyzer that is matched to the field.
     * @param field
     * @param requestStr
     * @param a
     * @return
     */
	private static String analyzeText(String field, String requestStr, PerFieldAnalyzerWrapper a) {

		boolean phrase = false;
		if ((requestStr.startsWith("\"") && requestStr.endsWith("\""))) {
            phrase = true;
        }
		

		List<String> tokenList = new ArrayList<String>();
		try {
		    TokenStream ts = a.tokenStream(field, new StringReader(requestStr));
		    ts.reset();
		    CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
			while (ts.incrementToken()) {
				String string = termAtt.toString();
                tokenList.add(string);
			}
		}
        catch (Exception e) {
            // TODO why swallow
			e.printStackTrace();
		}

		StringBuilder result = new StringBuilder();

		for (int i = 0;i < tokenList.size();i++) {
			if (i > 0) {
				result.append(" ");
				result.append(tokenList.get(i));
			}
            else {
				result.append(tokenList.get(i));
			}
		}
		String outStr = result.toString();
		if (phrase) {
            outStr = "\"" + outStr + "\"";
        }
		return outStr;
	}

    /**
     * Unused at the moment - but might be useful later.
     * @param aText
     * @param excludes
     * @return
     */
    public static String escapeLuceneChars(String aText, String excludes) {
     final StringBuilder result = new StringBuilder();
     final StringCharacterIterator iterator = new StringCharacterIterator(aText);
     char character =  iterator.current();
     while (character != CharacterIterator.DONE ) {
       if (character == '\\' && !excludes.contains("\\")) {
         result.append("\\");
       } else if (character == '!' && !excludes.contains("!")) {
         result.append("\\");
       } else if (character == '(' && !excludes.contains("(")) {
         result.append("\\");
       } else if (character == ')' && !excludes.contains(")")) {
         result.append("\\");
       } else if (character == '*' && !excludes.contains("*")) {
         result.append("\\");
       } else if (character == '+' && !excludes.contains("+")) {
         result.append("\\");
       } else if (character == '-' && !excludes.contains("-")) {
         result.append("\\");
       } else if (character == ':' && !excludes.contains(":")) {
         result.append("\\");
       } else if (character == '?' && !excludes.contains("?")) {
         result.append("\\");
       } else if (character == '[' && !excludes.contains("[")) {
         result.append("\\");
       } else if (character == ']' && !excludes.contains("]")) {
         result.append("\\");
       } else if (character == '^' && !excludes.contains("^")) {
         result.append("\\");
       } else if (character == '{' && !excludes.contains("{")) {
         result.append("\\");
       } else if (character == '}' && !excludes.contains("}")) {
         result.append("\\");
       } 
       result.append(character);
       character = iterator.next();
     }
        if(Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Escaped: "+result.toString());
     return result.toString();
  }
    /**
     * Task to launch a new thread for search logging.
     * 
     * Other idea: Another approach could be to use JMS, to send an 
		 * asynchronous message with search info in order to log them.
     * 
     * @author francois
     */
    class SearchLoggerTask implements Runnable {
        private ServiceContext srvContext;
        boolean logSpatialObject;
        String luceneTermsToExclude;
		Query query; 
		int numHits; 
		Sort sort;
		String geomWKT;
		String value;


        public SearchLoggerTask(ServiceContext srvContext,
				boolean logSpatialObject, String luceneTermsToExclude,
				Query query, int numHits, Sort sort, String geomWKT,
				String value) {
        			this.srvContext = srvContext;
        			this.logSpatialObject = logSpatialObject;
        			this.luceneTermsToExclude = luceneTermsToExclude;
        			this.query = query;
        			this.numHits = numHits;
        			this.sort = sort;
        			this.geomWKT = geomWKT;
        			this.value = value;
    	}

		public void run() {
            try {
            	SearcherLogger searchLogger = new SearcherLogger(srvContext, logSpatialObject, luceneTermsToExclude);
        		searchLogger.logSearch(query, numHits, sort, geomWKT, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
