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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.DocValuesOrdinalsReader;
import org.apache.lucene.facet.taxonomy.OrdinalsReader;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
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
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.SearchManager.TermFrequency;
import org.fao.geonet.kernel.search.facet.Format;
import org.fao.geonet.kernel.search.facet.ItemBuilder;
import org.fao.geonet.kernel.search.facet.ItemConfig;
import org.fao.geonet.kernel.search.facet.SummaryType;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.search.lucenequeries.DateRangeQuery;
import org.fao.geonet.kernel.search.spatial.SpatialFilter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.languages.LanguageDetector;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.text.CharacterIterator;
import java.text.NumberFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * search metadata locally using lucene.
 */
public class LuceneSearcher extends MetaSearcher implements MetadataRecordSelector {
    private static Logger LOGGER = LoggerFactory.getLogger(Geonet.SEARCH_ENGINE);
    private SearchManager _sm;
    private String _styleSheetName;

    private Query _query;
    private Query _loggerQuery;

    private Filter _filter;
    private Sort _sort;
    private Element _elSummary;

    private int _numHits;
    private LanguageSelection _language;

    private Set<String> _tokenizedFieldSet;
    private LuceneConfig _luceneConfig;
    private String _boostQueryClass;


    /**
     * Filter geometry object WKT, used in the logger ugly way to store this object, as
     * ChainedFilter API is a little bit cryptic to me...
     */
    private String _geomWKT = null;
    private long _versionToken = -1;
    private SummaryType _summaryConfig;
    private boolean _logSearch = true;

    /**
     * constructor TODO javadoc.
     */
    public LuceneSearcher(SearchManager sm, String styleSheetName, LuceneConfig luceneConfig) {
        _sm = sm;
        _styleSheetName = styleSheetName;

        // build _tokenizedFieldSet
        _luceneConfig = luceneConfig;
        _boostQueryClass = _luceneConfig.getBoostQueryClass();
        _tokenizedFieldSet = luceneConfig.getTokenizedField();
    }

    //
    // MetaSearcher API
    //

    public static void logSearch(ServiceContext srvContext, ServiceConfig config, Query query, int numHits, Sort sort, String geomWKT,
                                 SearchManager sm) {
        SettingInfo si = srvContext.getBean(SettingInfo.class);
        if (si.isSearchStatsEnabled()) {
            LOGGER.debug("Log search in asynch mode - start.");
            GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
            SearchLoggerTask logTask = srvContext.getBean(SearchLoggerTask.class);
            logTask.configure(srvContext, query, numHits, sort, geomWKT,
                config.getValue(Jeeves.Text.GUI_SERVICE, "n"));

            gc.getThreadPool().runTask(logTask);
            LOGGER.debug( "Log search in asynch mode - end.");
        }
    }

    @VisibleForTesting
    static void buildPrivilegesMetadataInfo(ServiceContext context, Document doc, Element infoEl) throws Exception {
        final Integer owner = Integer.valueOf(doc.get(Geonet.IndexFieldNames.OWNER));
        final String groupOwnerString = doc.get(Geonet.IndexFieldNames.GROUP_OWNER);

        MetadataSourceInfo sourceInfo = new MetadataSourceInfo();
        sourceInfo.setOwner(owner);
        if (groupOwnerString != null) {
            sourceInfo.setGroupOwner(Integer.valueOf(groupOwnerString));
        }
        final AccessManager accessManager = context.getBean(AccessManager.class);
        boolean isOwner = accessManager.isOwner(context, sourceInfo);

        HashSet<ReservedOperation> operations;
        boolean canEdit = false;
        if (isOwner) {
            operations = Sets.newHashSet(Arrays.asList(ReservedOperation.values()));
            if (owner != null) {
                addElement(infoEl, "ownerId", owner.toString());
            }
        } else {
            final Collection<Integer> groups = accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
            final Collection<Integer> editingGroups = accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), true);
            operations = Sets.newHashSet();
            for (ReservedOperation operation : ReservedOperation.values()) {
                IndexableField[] opFields = doc.getFields(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());

                for (IndexableField field : opFields) {
                    Integer groupId = Integer.valueOf(field.stringValue());
                    if (operation == ReservedOperation.editing &&
                        editingGroups.contains(groupId)) {
                        canEdit = true;
                        break;
                    }

                    if (groups.contains(groupId)) {
                        operations.add(operation);
                        break;
                    }
                }
            }
        }
        if (isOwner || canEdit) {
            addElement(infoEl, Edit.Info.Elem.EDIT, "true");
        }

        if (isOwner) {
            addElement(infoEl, Edit.Info.Elem.OWNER, "true");
        }

        addElement(infoEl, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, hasOperation(doc, ReservedGroup.all, ReservedOperation.view));
        addOperationsElement(infoEl, ReservedOperation.view.name(), operations.contains(ReservedOperation.view));
        addOperationsElement(infoEl, ReservedOperation.notify.name(), operations.contains(ReservedOperation.notify));
        addOperationsElement(infoEl, ReservedOperation.download.name(), operations.contains(ReservedOperation.download));
        addOperationsElement(infoEl, ReservedOperation.dynamic.name(), operations.contains(ReservedOperation.dynamic));
        addOperationsElement(infoEl, ReservedOperation.featured.name(), operations.contains(ReservedOperation.featured));

        if (!operations.contains(ReservedOperation.download)) {
            addElement(infoEl, Edit.Info.Elem.GUEST_DOWNLOAD, hasOperation(doc, ReservedGroup.guest, ReservedOperation.download));
        }
    }

    private static String hasOperation(Document doc, ReservedGroup group, ReservedOperation operation) {
        String groupId = String.valueOf(group.getId());
        final IndexableField[] fields = doc.getFields(Geonet.IndexFieldNames.OP_PREFIX + operation.getId());
        for (IndexableField field : fields) {
            if (groupId.equals(field.stringValue())) {
                return Boolean.TRUE.toString();
            }
        }
        return Boolean.FALSE.toString();
    }

    private static void addOperationsElement(Element root, String name, Object value) {
        root.addContent(new Element(name).setText(value == null ? "" : value.toString()));
    }

    /**
     * Determines requested language as follows: - uses value of requestedLanguage search parameter,
     * if it is present; - else uses autodetection, if that is enabled; - else uses servicecontext
     * (GUI) language, if available; - else uses GeoNetwork Default language.
     */
    public static LanguageSelection determineLanguage(
        @Nullable ServiceContext srvContext,
        @Nonnull Element request,
        @Nonnull SettingInfo settingInfo) {
        String finalDetectedLanguage = null;
        if (settingInfo != null && settingInfo.getRequestedLanguageOnly() == SettingInfo.SearchRequestLanguage.OFF) {
            LOGGER.debug("requestedlanguage ignored, using default one ");

            //Return default language defined on config.xml
            finalDetectedLanguage = srvContext.getLanguage();
            return new LanguageSelection(finalDetectedLanguage, finalDetectedLanguage);

        }
        String requestedLanguage = request.getChildText("requestedLanguage");
        // requestedLanguage in request
        if (StringUtils.isNotEmpty(requestedLanguage)) {
            LOGGER.debug("found requestedlanguage in request: {}", requestedLanguage);
            finalDetectedLanguage = requestedLanguage;
        } else {
            // no requestedLanguage in request
            boolean detected = false;
            // autodetection is enabled
            if (settingInfo.getAutoDetect()) {
                LOGGER.debug("auto-detecting request language is enabled");

                StringBuilder test = new StringBuilder();

                LuceneQueryInput luceneQueryInput = new LuceneQueryInput(request);
                Map<String, Set<String>> searchCriteria = luceneQueryInput.getTextCriteria();
                if (!searchCriteria.isEmpty()) {
                    for (Set<String> value : searchCriteria.values()) {
                        for (String v : value) {
                            test.append(" ").append(v);
                        }
                    }
                } else {
                    try {
                        final List<Content> termQueries = (List<Content>) Xml.selectNodes(request, "*//ogc:Literal", Arrays.asList
                            (Geonet.Namespaces.OGC));
                        for (Content literals : termQueries) {
                            if (literals instanceof Element) {
                                test.append(" ").append(((Element) literals).getText());
                            }
                        }
                    } catch (JDOMException e) {
                        // can't do the query, so try another method
                    }
                }
                try {
                    if (test.length() > 0) {
                        String detectedLanguage = LanguageDetector.getInstance().detect(srvContext, test.toString());
                        LOGGER.debug( "automatic language detection: '{}' is in language {}", test, detectedLanguage);
                        finalDetectedLanguage = detectedLanguage;
                        detected = true;
                    }
                } catch (Exception x) {
                    LOGGER.error("Error auto-detecting language: {}", x.getMessage(), x);
                }


            } else {
                LOGGER.debug( "auto-detecting request language is disabled");
            }
            // autodetection is disabled or detection failed
            if (!detected) {
                LOGGER.debug( "autodetection is disabled or detection failed");

                // servicecontext available
                if (srvContext != null) {
                    LOGGER.debug("taking language from servicecontext");
                    finalDetectedLanguage = srvContext.getLanguage();
                } else {
                    // no servicecontext available
                    LOGGER.debug("taking GeoNetwork default language");
                    finalDetectedLanguage = Geonet.DEFAULT_LANGUAGE; // TODO : set default not language in config
                }
            }
        }
        LOGGER.debug( "determined language is: {}", finalDetectedLanguage);

        String presentationLanguage = finalDetectedLanguage == null ?
            srvContext.getLanguage() :
            finalDetectedLanguage;
        if (settingInfo.getRequestedLanguageOnly() == SettingInfo.SearchRequestLanguage.ONLY_UI_DOC_LOCALE ||
            settingInfo.getRequestedLanguageOnly() == SettingInfo.SearchRequestLanguage.ONLY_UI_LOCALE ||
            settingInfo.getRequestedLanguageOnly() == SettingInfo.SearchRequestLanguage.PREFER_UI_DOC_LOCALE ||
            settingInfo.getRequestedLanguageOnly() == SettingInfo.SearchRequestLanguage.PREFER_UI_LOCALE) {
            presentationLanguage = requestedLanguage == null ?
                srvContext.getLanguage() :
                requestedLanguage;
        }

        return new LanguageSelection(finalDetectedLanguage, presentationLanguage);
    }

    /**
     * Creates the Sort to use in the search.
     */
    public static Sort makeSort(List<Pair<String, Boolean>> fields, String requestLanguage,
                                boolean sortRequestedLanguageOnTop) {
        List<SortField> sortFields = new ArrayList<SortField>();
        if (sortRequestedLanguageOnTop && requestLanguage != null) {
            // Add a sort so the metadata defined in the requested language are the first metadata in results
            sortFields.add(new LangSortField(requestLanguage));
        }
        for (Pair<String, Boolean> sortBy : fields) {
            LOGGER.debug( "Sorting by : ", sortBy);
            SortField sortField = LuceneSearcher.makeSortField(sortBy.one(), sortBy.two(), requestLanguage);
            if (sortField != null) sortFields.add(sortField);
        }
        sortFields.add(SortField.FIELD_SCORE);
        return new Sort(sortFields.toArray(new SortField[sortFields.size()]));
    }

    /**
     * Defines sort field. By default, the field is assumed to be a string. Only popularity and
     * rating are sorted based on integer type. In order to works well sort field needs to be not
     * tokenized in Lucene index.
     *
     * Relevance is the default Lucene sorting mechanism.
     *
     * @param sortBy     sort field
     * @param sortOrder  sort order
     * @param searchLang if non-null then the sorter will take into account translation (if
     *                   possible)
     * @return sortfield
     */
    private static SortField makeSortField(String sortBy, boolean sortOrder, String searchLang) {
        SortField.Type sortType = SortField.Type.STRING;

        if (sortBy.equals(Geonet.SearchResult.SortBy.RELEVANCE)) {
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
        LOGGER.debug( "Sort by: {} order: {} type: {}", new Object[] {sortBy, sortOrder, sortType});
        if (sortType == org.apache.lucene.search.SortField.Type.STRING) {
            if (searchLang != null) {
                return new SortField(sortBy, new CaseInsensitiveFieldComparatorSource(searchLang), sortOrder);
            } else {
                return new SortField(sortBy, CaseInsensitiveFieldComparatorSource.languageInsensitiveInstance(), sortOrder);
            }
        }
        return new SortField(sortBy, sortType, sortOrder);
    }

    /**
     * TODO javadoc.
     */
    public static Query makeLocalisedQuery(Element xmlQuery, PerFieldAnalyzerWrapper analyzer,
                                           LuceneConfig luceneConfig, String langCode,
                                           SettingInfo.SearchRequestLanguage requestedLanguageOnly)
        throws Exception {
        Query returnValue = LuceneSearcher.makeQuery(xmlQuery, analyzer, luceneConfig);
        if (StringUtils.isNotEmpty(langCode)) {
            returnValue = LuceneQueryBuilder.addLocaleTerm(returnValue, langCode, requestedLanguageOnly);
        }
        LOGGER.debug("Lucene Query: {}", returnValue);
        return returnValue;
    }

    /**
     * Makes a new lucene query.
     *
     * If the field to be queried is tokenized then this method applies the appropriate analyzer
     * (see SearchManager) to the field.
     */
    private static Query makeQuery(Element xmlQuery, PerFieldAnalyzerWrapper analyzer, LuceneConfig luceneConfig) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MakeQuery input XML:\n{}", Xml.getString(xmlQuery));
        }
        String name = xmlQuery.getName();
        Query returnValue;

        Set<String> tokenizedFieldSet = luceneConfig.getTokenizedField();
        Map<String, LuceneConfigNumericField> numericFieldSet = luceneConfig.getNumericFields();
        if (name.equals("TermQuery")) {
            String fld = xmlQuery.getAttributeValue("fld");
            returnValue = LuceneSearcher.textFieldToken(luceneConfig, xmlQuery.getAttributeValue("txt"), fld, xmlQuery.getAttributeValue("sim"), analyzer, tokenizedFieldSet);
        } else if (name.equals("FuzzyQuery")) {
            String fld = xmlQuery.getAttributeValue("fld");
            returnValue = LuceneSearcher.textFieldToken(luceneConfig, xmlQuery.getAttributeValue("txt"), fld, xmlQuery.getAttributeValue("sim"), analyzer, tokenizedFieldSet);
        } else if (name.equals("PrefixQuery")) {
            String fld = xmlQuery.getAttributeValue("fld");
            String txt = LuceneSearcher.analyzeQueryText(fld, xmlQuery.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
            returnValue = new PrefixQuery(new Term(fld, txt));
        } else if (name.equals("MatchAllDocsQuery")) {
            return new MatchAllDocsQuery();
        } else if (name.equals("WildcardQuery")) {
            String fld = xmlQuery.getAttributeValue("fld");
            returnValue = LuceneSearcher.textFieldToken(luceneConfig, xmlQuery.getAttributeValue("txt"), fld, xmlQuery.getAttributeValue("sim"), analyzer, tokenizedFieldSet);
        } else if (name.equals("PhraseQuery")) {
            PhraseQuery query = new PhraseQuery();
            for (Object o : xmlQuery.getChildren()) {
                Element xmlTerm = (Element) o;
                String fld = xmlTerm.getAttributeValue("fld");
                String txt = LuceneSearcher.analyzeQueryText(fld, xmlTerm.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
                if (txt.length() > 0) {
                    query.add(new Term(fld, txt));
                }
            }
            returnValue = query;
        } else if (name.equals("RangeQuery")) {
            String fld = xmlQuery.getAttributeValue("fld");
            String lowerTxt = xmlQuery.getAttributeValue("lowerTxt");
            String upperTxt = xmlQuery.getAttributeValue("upperTxt");
            String sInclusive = xmlQuery.getAttributeValue("inclusive");
            boolean inclusive = "true".equals(sInclusive);

            LuceneConfigNumericField fieldConfig = numericFieldSet.get(fld);
            if (fieldConfig != null) {
                returnValue = LuceneQueryBuilder.buildNumericRangeQueryForType(fld, lowerTxt, upperTxt, inclusive, inclusive, fieldConfig.getType());
            } else {
                lowerTxt = (lowerTxt == null ? null : LuceneSearcher.analyzeQueryText(fld, lowerTxt, analyzer, tokenizedFieldSet));
                upperTxt = (upperTxt == null ? null : LuceneSearcher.analyzeQueryText(fld, upperTxt, analyzer, tokenizedFieldSet));

                returnValue = TermRangeQuery.newStringRange(fld, lowerTxt, upperTxt, inclusive, inclusive);
            }
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
                returnValue = LuceneQueryBuilder.buildNumericRangeQueryForType(fld, lowerTxt, upperTxt, inclusive, inclusive, fieldConfig.getType(), fieldConfig.getPrecisionStep());
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
        } else throw new Exception("unknown lucene query type: " + name);

        LOGGER.debug( "Lucene Query: {}", returnValue);
        return returnValue;
    }

    /**
     * TODO javadoc.
     */
    private static Query textFieldToken(LuceneConfig luceneConfig, String string, String luceneIndexField, String similarity,
                                        PerFieldAnalyzerWrapper analyzer, Set<String> tokenizedFieldSet) {
        if (string == null) {
            throw new IllegalArgumentException("Cannot create Lucene query for null string");
        }
        Query query = null;

        String analyzedString = "";
        // wildcards - preserve them by analyzing the parts of the search string around them separately
        // (this is because Lucene's StandardTokenizer would remove wildcards, but that's not what we want)
        if (string.indexOf('*') >= 0 || string.indexOf('?') >= 0) {
            WildCardStringAnalyzer wildCardStringAnalyzer = new WildCardStringAnalyzer();
            analyzedString = wildCardStringAnalyzer.analyze(string, luceneIndexField, analyzer, tokenizedFieldSet);
        }
        // no wildcards
        else {
            analyzedString = LuceneSearcher.analyzeQueryText(luceneIndexField, string, analyzer, tokenizedFieldSet);
        }

        return LuceneQueryBuilder.constructQueryFromAnalyzedString(luceneConfig, string, luceneIndexField, similarity, query, analyzedString, tokenizedFieldSet);
    }

    /**
     * Do Lucene search and optionally build a summary for the search.
     *
     * @param numHits        the maximum number of hits to collect
     * @param startHit       the start hit to return in the TopDocs if not building summary
     * @param endHit         the end hit to return in the TopDocs if not building summary
     * @param langCode       the language code used by SummaryComparator
     * @param summaryConfig  the summary configuration
     * @param reader         reader
     * @param query          query
     * @param cFilter        filter
     * @param sort           the sort criteria
     * @param taxonomyReader A {@link TaxonomyReader} use to compute facets (ie. summary)
     * @param buildSummary   true to build query summary element. Summary is stored in the second
     *                       element of the returned Pair.
     * @throws Exception hmm
     * @return the topDocs for the search. When building summary, topDocs will contains all search
     * hits and need to be filtered to return only required hits according to search parameters.
     */
    public static Pair<TopDocs, Element> doSearchAndMakeSummary(int numHits, int startHit, int endHit, String langCode,
                                                                SummaryType summaryConfig, LuceneConfig luceneConfig, IndexReader reader,
                                                                Query query, Filter cFilter, Sort sort, TaxonomyReader taxonomyReader, boolean buildSummary) throws Exception {
        FacetsConfig facetConfiguration = luceneConfig.getTaxonomyConfiguration();
        boolean trackDocScores = luceneConfig.isTrackDocScores();
        boolean trackMaxScore = luceneConfig.isTrackMaxScore();
        boolean docsScoredInOrder = luceneConfig.isDocsScoredInOrder();
        LOGGER.debug("Build summary: {}", buildSummary);
        LOGGER.debug("Setting up the TFC with numHits {}", numHits);
        TopFieldCollector tfc = TopFieldCollector.create(sort, numHits, true, trackDocScores, trackMaxScore, docsScoredInOrder);

        LOGGER.debug("Lucene query: ", query);
        // too dangerous to do this only for logging, as it may throw NPE if Query was not constructed correctly
        // However if you need to see what Lucene queries are really used, print the rewritten query instead
        // Query rw = query.rewrite(reader);
        // System.out.println("Lucene query: " + rw.toString());

        IndexSearcher searcher = new IndexSearcher(reader);

        Element elSummary = new Element("summary");

        if (taxonomyReader != null && buildSummary) {
            // configure facets from configuration file
            FacetsCollector facetCollector = new FacetsCollector();

            searcher.search(query, cFilter, MultiCollector.wrap(tfc, facetCollector));

            try {
                buildFacetSummary(elSummary, summaryConfig, facetConfiguration, facetCollector, taxonomyReader, langCode);
            } catch (Exception e) {
                LOGGER.warn("BuildFacetSummary error. {}" ,e.getMessage(), e);
            }

        } else {
            searcher.search(query, cFilter, tfc);
        }
        elSummary.setAttribute("count", tfc.getTotalHits() + "");
        elSummary.setAttribute("type", "local");
        LOGGER.debug(" Get top docs from {} ... {} (total: {})", new Object[] {startHit, endHit, tfc.getTotalHits()});
        TopDocs tdocs = tfc.topDocs(startHit, endHit - startHit);

        return Pair.read(tdocs, elSummary);
    }

    //
    // private setup, index, delete and search functions
    //

    /**
     * Create an XML summary from the search facets collector.
     *
     * @param elSummary           The element in which to add the facet report
     * @param summaryConfigValues The summary configuration
     */
    private static void buildFacetSummary(Element elSummary,
                                          SummaryType summaryConfigValues,
                                          FacetsConfig facetConfiguration,
                                          FacetsCollector facetCollector, TaxonomyReader taxonomyReader,
                                          String langCode) throws IOException {
        Format format = summaryConfigValues.getFormat();
        Map<String, ArrayIndexOutOfBoundsException> configurationErrors = Maps.newHashMap();
        for (ItemConfig itemConfig : summaryConfigValues.getItems()) {
            try {
                OrdinalsReader ordsReader = new DocValuesOrdinalsReader(itemConfig.getDimension().getFacetFieldName(langCode));
                Facets facets = new TaxonomyFacetCounts(ordsReader, taxonomyReader, facetConfiguration, facetCollector);
                ItemBuilder builder = new ItemBuilder(itemConfig, langCode, facets, format);
                Element facetSummary = builder.build();
                elSummary.addContent(facetSummary);
            } catch (ArrayIndexOutOfBoundsException e) {
                configurationErrors.put(itemConfig.getDimension().getFacetFieldName(langCode), e);
            }
        }

        if (!configurationErrors.isEmpty()) {
            final StringBuilder message = new StringBuilder();
            message.append("Check facet configuration. \n").append(ArrayIndexOutOfBoundsException.class.getSimpleName()).
                append(" errors are often caused when a facet is configured but does not exist in the taxonomy index. ").
                append("The facets that have raised this error are: ");

            for (String facet : configurationErrors.keySet()) {
                message.append("\n  * ").append(facet);
            }
            LOGGER.error("{}", message);
            configurationErrors.values().iterator().next().printStackTrace();
        }
    }

    /**
     * Retrieves metadata from the index . Used in metadata selection pdf print.
     *
     * @param dumpFields dump only the fields define in {@link LuceneConfig#getDumpFields()}.
     */
    private static Element getMetadataFromIndexForPdf(UserSession us, Set<Integer> userGroups, Document doc, String id, String
        searchLang, Set<String> multiLangSearchTerm, Map<String, String> dumpFields, Set<String> extraDumpFields) {
        Element md = LuceneSearcher.getMetadataFromIndex(doc, id, true, searchLang, multiLangSearchTerm, dumpFields, extraDumpFields);

        // Add download/dynamic privileges
        Element info = md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);

        if (us.getProfile() == Profile.Administrator) {
            info.addContent(new Element(ReservedOperation.download.name()).setText("true"));
            info.addContent(new Element(ReservedOperation.dynamic.name()).setText("true"));

        } else {
            // Owner
            boolean isOwner = false;
            IndexableField[] values = doc.getFields(LuceneIndexField.OWNER);

            if (us.isAuthenticated()) {
                for (IndexableField f : values) {
                    if (f != null) {
                        if (us.getUserId().equals(f.stringValue())) {
                            isOwner = true;
                            break;
                        }
                    }
                }
            }

            if (isOwner) {
                info.addContent(new Element(ReservedOperation.download.name()).setText("true"));
                info.addContent(new Element(ReservedOperation.dynamic.name()).setText("true"));

            } else {
                // Download
                values = doc.getFields(LuceneIndexField._OP1);
                for (IndexableField f : values) {
                    if (f != null) {
                        if (userGroups.contains(Integer.parseInt(f.stringValue()))) {
                            info.addContent(new Element(ReservedOperation.download.name()).setText("true"));
                            break;
                        }
                    }
                }

                // Dynamic
                values = doc.getFields(LuceneIndexField._OP5);
                for (IndexableField f : values) {
                    if (f != null) {
                        if (userGroups.contains(Integer.parseInt(f.stringValue()))) {
                            info.addContent(new Element(ReservedOperation.dynamic.name()).setText("true"));
                            break;
                        }
                    }
                }
            }

        }


        return md;
    }

    /**
     * Retrieves metadata from the index.
     *
     * @param dumpAllField If dumpFields is null and dumpAllField set to true, dump all index
     *                     content.
     * @param dumpFields   If not null, dump only the fields define in {@link
     *                     LuceneConfig#getDumpFields()}.
     */
    private static Element getMetadataFromIndex(Document doc, String id, boolean dumpAllField, String searchLang,
                                                Set<String> multiLangSearchTerm, Map<String, String> dumpFields,
                                                Set<String> extraDumpFields) {
        // Retrieve the info element
        String schema = doc.get("_schema");
        String source = doc.get("_source");
        String uuid = doc.get("_uuid");

        String createDate = doc.get("_createDate");
        if (createDate != null) createDate = createDate.toUpperCase();
        String changeDate = doc.get("_changeDate");
        if (changeDate != null) changeDate = changeDate.toUpperCase();

        // Root element is using root element name if not using only the index content (ie. dumpAllField)
        // probably because the XSL need that info later ?
        Element md = new Element("metadata");

        Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);

        addElement(info, Edit.Info.Elem.ID, id);
        addElement(info, Edit.Info.Elem.UUID, uuid);
        addElement(info, Edit.Info.Elem.SCHEMA, schema);
        addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
        addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
        addElement(info, Edit.Info.Elem.SOURCE, source);

        HashSet<String> addedTranslation = new LinkedHashSet<String>();
        if ((dumpAllField || dumpFields != null) && searchLang != null && multiLangSearchTerm != null) {
            // get the translated fields and dump those instead of the non-translated
            for (String fieldName : multiLangSearchTerm) {
                IndexableField[] values = doc.getFields(LuceneConfig.multilingualSortFieldName(fieldName, searchLang));
                for (IndexableField f : values) {
                    if (f != null) {
                        String stringValue = f.stringValue();
                        if (!stringValue.trim().isEmpty()) {
                            addedTranslation.add(fieldName);
                            md.addContent(new Element(dumpFields.get(fieldName)).setText(stringValue));
                        }
                    }
                }
            }
        }
        if (addedTranslation.isEmpty()) {
            addedTranslation = null;
        }
        if (dumpFields != null) {
            for (Map.Entry<String, String> entry : dumpFields.entrySet()) {
                String fieldName = entry.getKey();
                addIndexValues(doc, md, addedTranslation, entry.getValue(), fieldName);
            }
            if (extraDumpFields != null) {
                for (String fieldName : extraDumpFields) {
                    if (fieldName.contains("*")) {
                        fieldName = fieldName.replace("*", ".*");
                        for (IndexableField indexableField : doc) {
                            if (indexableField.name().matches(fieldName)) {
                                addIndexValue(md, addedTranslation, indexableField.name(), indexableField.name(), indexableField);
                            }
                        }
                    } else {
                        addIndexValues(doc, md, addedTranslation, fieldName, fieldName);
                    }
                }
            }
        } else {
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

    private static void addIndexValues(Document doc, Element md, HashSet<String> addedTranslation, String outputName,
                                       String fieldName) {
        IndexableField[] values = doc.getFields(fieldName);
        for (IndexableField f : values) {
            addIndexValue(md, addedTranslation, outputName, fieldName, f);
        }
    }

    private static void addIndexValue(Element md, HashSet<String> addedTranslation, String outputName, String fieldName, IndexableField
        f) {
        if (f != null) {
            if (addedTranslation == null || !addedTranslation.contains(fieldName)) {
                md.addContent(new Element(outputName).setText(f.stringValue()));
            }
        }
    }

    /**
     * Searches in Lucene index and return Lucene index field value. Metadata records is retrieved
     * based on its uuid.
     */
    public static String getMetadataFromIndex(String priorityLang, String id, String fieldname) throws Exception {
        return LuceneSearcher.getMetadataFromIndex(priorityLang, id, Collections.singleton(fieldname)).get(fieldname);
    }

    /**
     * TODO javadoc.
     */
    public static String getMetadataFromIndexById(String priorityLang, String id, String fieldname) throws Exception {
        return LuceneSearcher.getMetadataFromIndex(priorityLang, "_id", id, Collections.singleton(fieldname)).get(fieldname);
    }

    /**
     * TODO javadoc.
     */
    private static Map<String, String> getMetadataFromIndex(String priorityLang, String uuid, Set<String> fieldnames) throws Exception {
        return LuceneSearcher.getMetadataFromIndex(priorityLang, "_uuid", uuid, fieldnames);
    }

    public static Map<String, String> getMetadataFromIndex(String priorityLang, String idField, String id, Set<String> fieldnames) throws Exception {
        Map<String, Map<String, String>> results = LuceneSearcher.getAllMetadataFromIndexFor(priorityLang, idField, id, fieldnames, false);
        if (results.size() == 1) {
            return results.values().iterator().next();
        } else {
            return new HashMap<String, String>();
        }
    }

    /**
     * Get Lucene index fields for matching records
     *
     * @param priorityLang Preferred index language to use.
     * @param field        Field to search for (eg. _uuid)
     * @param value        Value to search for
     * @param returnFields Fields to return
     * @param checkAllHits If false, only the first match is analyzed for returned field. Set to
     *                     true when searching on uuid field and only one record is expected.
     */
    public static Map<String, Map<String, String>> getAllMetadataFromIndexFor(String priorityLang, String field, String value, Set<String> returnFields, boolean checkAllHits) throws Exception {
        final IndexAndTaxonomy indexAndTaxonomy;
        final SearchManager searchmanager;
        ServiceContext context = ServiceContext.get();
        GeonetworkMultiReader reader;
        if (context != null) {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            searchmanager = gc.getBean(SearchManager.class);
            indexAndTaxonomy = searchmanager.getNewIndexReader(priorityLang);
            reader = indexAndTaxonomy.indexReader;
        } else {
            throw new IllegalStateException("There needs to be a ServiceContext in the thread local for this thread");
        }

        Map<String, Map<String, String>> records = new HashMap<String, Map<String, String>>();

        try {
            IndexSearcher searcher = new IndexSearcher(reader);
            TermQuery query = new TermQuery(new Term(field, value));
            SettingInfo settingInfo = searchmanager.getSettingInfo();
            boolean sortRequestedLanguageOnTop = settingInfo.getRequestedLanguageOnTop();
            LOGGER.debug("sortRequestedLanguageOnTop: {}", sortRequestedLanguageOnTop);

            int numberOfHits = 1;
            int counter = 0;
            if (checkAllHits) {
                numberOfHits = Integer.MAX_VALUE;
            }
            Sort sort = LuceneSearcher.makeSort(Collections.<Pair<String, Boolean>>emptyList(), priorityLang, sortRequestedLanguageOnTop);
            Filter filter = NoFilterFilter.instance();
            TopDocs tdocs = searcher.search(query, filter, numberOfHits, sort);

            for (ScoreDoc sdoc : tdocs.scoreDocs) {
                Map<String, String> values = new HashMap<String, String>();

                DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor(returnFields);
                reader.document(sdoc.doc, docVisitor);
                Document doc = docVisitor.getDocument();

                for (String fieldname : returnFields) {
                    values.put(fieldname, doc.get(fieldname));
                }

                records.put(String.valueOf(counter), values);
                counter++;
            }

        } catch (CorruptIndexException e) {
            // TODO: handle exception
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            // TODO: handle exception
            LOGGER.error(e.getMessage());
        } finally {
            searchmanager.releaseIndexReader(indexAndTaxonomy);
        }
        return records;
    }

    /**
     * TODO javadoc.
     */
    protected static String analyzeQueryText(String field, String aText, PerFieldAnalyzerWrapper analyzer, Set<String> tokenizedFieldSet) {
        LOGGER.debug("Analyze field {} : {}", field, aText);
        if (tokenizedFieldSet.contains(field)) {
            String analyzedText = LuceneSearcher.analyzeText(field, aText, analyzer);
            LOGGER.debug("Analyzed text is {}", analyzedText);
            return analyzedText;
        } else return aText;
    }

    /**
     * Splits text into tokens using the Analyzer that is matched to the field.
     */
    public static String analyzeText(String field, String requestStr, PerFieldAnalyzerWrapper a) {

        boolean phrase = false;
        if ((requestStr.startsWith("\"") && requestStr.endsWith("\""))) {
            phrase = true;
        }


        List<String> tokenList = new ArrayList<String>();
        TokenStream ts = null;
        try {
            ts = a.tokenStream(field, new StringReader(requestStr));
            ts.reset();
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
                String string = termAtt.toString();
                tokenList.add(string);
            }
        } catch (Exception e) {
            // TODO why swallow
            LOGGER.error(Geonet.SEARCH_ENGINE, "analyzeText error:" + e.getMessage(), e);
        } finally {
            if (ts != null) {
                try {
                    ts.close();
                } catch (IOException e) {
                    LOGGER.error(Geonet.SEARCH_ENGINE, "analyzeText error closing TokenStream:" + e.getMessage(), e);
                }
            }
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < tokenList.size(); i++) {
            if (i > 0) {
                result.append(" ");
                result.append(tokenList.get(i));
            } else {
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
     */
    public static String escapeLuceneChars(String aText, String excludes) {
        final StringBuilder result = new StringBuilder();
        final StringCharacterIterator iterator = new StringCharacterIterator(aText);
        char character = iterator.current();
        while (character != CharacterIterator.DONE) {
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
        LOGGER.debug("Escaped: {}", result);
        return result.toString();
    }

    /**
     * TODO javadoc.
     */
    public void search(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        // Open the IndexReader first, and then the TaxonomyReader.
        LOGGER.debug("LuceneSearcher search()");

        String sBuildSummary = request.getChildText(Geonet.SearchResult.BUILD_SUMMARY);
        boolean buildSummary = sBuildSummary == null || sBuildSummary.equals("true");
        _language = determineLanguage(srvContext, request, _sm.getSettingInfo());

        LOGGER.debug("LuceneSearcher initializing search range");
        initSearchRange(srvContext);

        LOGGER.debug("LuceneSearcher computing query");
        computeQuery(srvContext, request, config);

        LOGGER.debug("LuceneSearcher performing query");
        performQuery(srvContext, getFrom() - 1, getTo(), buildSummary);
        updateSearchRange(request);

        if (_logSearch) {
            logSearch(srvContext, config, _loggerQuery, _numHits, _sort, _geomWKT, _sm);
        }
    }

    /**
     * TODO javadoc.
     */
    public List<org.jdom.Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        throw new UnsupportedOperationException("Not supported by Lucene searcher");
    }

    /**
     * TODO javadoc.
     *
     * @return An empty response if no result or a list of results. Return only geonet:info element
     * in fast mode.
     */
    public Element present(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
        updateSearchRange(request);
        GeonetContext gc = null;
        if (srvContext != null)
            gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

        String sFast = request.getChildText(Geonet.SearchResult.FAST);
        boolean fast = sFast != null && sFast.equals("true");
        boolean inFastMode = fast || "index".equals(sFast) || "indexpdf".equals(sFast);

        Set<String> extraDumpFields = Sets.newHashSet();
        if (inFastMode) {
            String[] fields = Util.getParam(request, Geonet.SearchResult.EXTRA_DUMP_FIELDS, "").split(",");
            for (String field : fields) {
                if (!field.trim().isEmpty()) {
                    extraDumpFields.add(field);
                }
            }
            extraDumpFields.addAll(Arrays.asList(fields));
        }

        // build response
        Element response = new Element("response");
        response.setAttribute("from", getFrom() + "");
        response.setAttribute("to", getTo() + "");
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(Xml.getString(response));

        // Add summary if required and exists
        String sBuildSummary = request.getChildText(Geonet.SearchResult.BUILD_SUMMARY);
        boolean buildSummary = sBuildSummary == null || sBuildSummary.equals("true");

        if (buildSummary && _elSummary != null)
            response.addContent((Element) _elSummary.clone());

        if (getTo() > 0) {
            TopDocs tdocs = performQuery(srvContext, getFrom() - 1, getTo(), false); // get enough hits to show a page

            int nrHits = getTo() - (getFrom() - 1);
            if (tdocs.scoreDocs.length >= nrHits) {
                Set<Integer> userGroups = null;
                try (IndexAndTaxonomy indexAndTaxonomy = _sm.getIndexReader(_language.presentationLanguage, _versionToken);) {
                    _versionToken = indexAndTaxonomy.version;

                    for (int i = 0; i < nrHits; i++) {
                        Document doc;
                        if (inFastMode) {
                            // no selector
                            doc = indexAndTaxonomy.indexReader.document(tdocs.scoreDocs[i].doc);
                        } else {
                            DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor("_id");
                            indexAndTaxonomy.indexReader.document(tdocs.scoreDocs[i].doc, docVisitor);
                            doc = docVisitor.getDocument();
                        }
                        String id = doc.get("_id");
                        Element md = null;

                        if (fast) {
                            md = LuceneSearcher.getMetadataFromIndex(doc, id, false, null, null, null, extraDumpFields);
                        } else if ("indexpdf".equals(sFast)) {
                            if (userGroups == null) {
                                userGroups = gc.getBean(AccessManager.class).getUserGroups(srvContext.getUserSession(), srvContext.getIpAddress(), false);

                            }

                            // Retrieve information from the index for the record
                            md = LuceneSearcher.getMetadataFromIndexForPdf(srvContext.getUserSession(), userGroups, doc, id,
                                _language.presentationLanguage, _luceneConfig.getMultilingualSortFields(), _luceneConfig.getDumpFields(), extraDumpFields);
                        } else if ("index".equals(sFast)) {
                            // Retrieve information from the index for the record
                            md = LuceneSearcher.getMetadataFromIndex(doc, id, true, _language.presentationLanguage, _luceneConfig.getMultilingualSortFields(), _luceneConfig.getDumpFields(), extraDumpFields);

                            buildPrivilegesMetadataInfo(srvContext, doc, md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE));
                        } else if (srvContext != null) {
                            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
                            md = gc.getBean(DataManager.class).getMetadata(srvContext, id, forEditing, withValidationErrors, keepXlinkAttributes);
                        }

                        //--- a metadata could have been deleted just before showing
                        //--- search results

                        if (md != null) {
                            // Calculate score and add it to info elem
                            if (_luceneConfig.isTrackDocScores()) {
                                Float score = tdocs.scoreDocs[i].score;
                                Element info = md.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
                                addElement(info, Edit.Info.Elem.SCORE, score.toString());
                            }
                            response.addContent(md);
                        }
                    }
                }
            } else {
                throw new Exception("Failed: Not enough search results (" + tdocs.scoreDocs.length + ") available to meet request for " + nrHits + ".");
            }
        }

        return response;
    }

    /**
     * Perform a query, loop over results in order to find values containing the search value for a
     * specific field.
     *
     * If the field is not stored in the index, an empty collection is returned.
     *
     * @param searchField      The field to search in
     * @param searchValue      The value contained in field's value (case is ignored)
     * @param maxNumberOfTerms The maximum number of terms to search for
     * @param threshold        The minimum frequency for terms to be returned
     */
    public void getSuggestionForFields(ServiceContext srvContext,
                                       final String searchField, final String searchValue, ServiceConfig config, int maxNumberOfTerms,
                                       int threshold, Collection<SearchManager.TermFrequency> suggestions) throws Exception {
        LOGGER.debug("Get suggestion on field: '{}'\tsearching: '{}'\tthreshold: '{}'\tmaxNumberOfTerms: '{}'",
            new Object[] {searchField, searchValue, threshold, maxNumberOfTerms});

        String searchValueWithoutWildcard = searchValue.replaceAll("[*?]", "");

        // To count the number of values added and stop if maxNumberOfTerms reach
        if (_language == null) {
            final Element request = new Element("request").addContent(new Element("any").setText(searchValue));
            _language = determineLanguage(srvContext, request, _sm.getSettingInfo());
        }

        _logSearch = false;

        // Search for all current session could search for
        // Do a like query to limit the size of the results
        Element elData = new Element(Jeeves.Elem.REQUEST); // SearchDefaults.getDefaultSearch(srvContext, null);
        elData.addContent(new Element("fast").addContent("index"));
        elData.addContent(new Element(Geonet.SearchResult.BUILD_SUMMARY).addContent(Boolean.toString(true)));

        if (!searchValue.equals("")) {
            elData.addContent(new Element(searchField).setText(searchValue));
        }
        elData.addContent(new Element("from").setText("1"));
        elData.addContent(new Element("to").setText(Integer.MAX_VALUE + ""));
        elData.addContent(new Element(Geonet.SearchResult.RESULT_TYPE).setText(Geonet.SearchResult.ResultType.SUGGESTIONS));
        elData.addContent(new Element(Geonet.SearchResult.SUMMARY_ITEMS).setText(searchField));
        search(srvContext, elData, config);

        if (getTo() > 0) {
            Set<String> encountered = new LinkedHashSet<String>();
            final Iterator descendants = _elSummary.getDescendants();
            while (descendants.hasNext()) {
                Content next = (Content) descendants.next();

                if (next instanceof Element) {
                    Element element = (Element) next;

                    if (element.getContentSize() == 0 && element.getAttribute("name") != null) {
                        final String value = element.getAttributeValue("name");

                        if (!encountered.contains(value)) {
                            encountered.add(value);
                            int count = Integer.parseInt(element.getAttributeValue("count"));

                            if (value.toLowerCase().contains(searchValueWithoutWildcard.toLowerCase())) {
                                TermFrequency term = new TermFrequency(value, count);
                                suggestions.add(term);
                            }
                        }
                    }
                }
            }
        }

        // Filter values which does not reach the threshold
        if (threshold > 1) {
            int size = suggestions.size();
            Iterator<TermFrequency> it = suggestions.iterator();
            while (it.hasNext()) {
                TermFrequency term = it.next();
                if (term.getFrequency() < threshold) {
                    it.remove();
                }
            }
            LOGGER.debug("  {}/{} above threshold: {}", new Object[]{suggestions.size(), size, threshold});
        }
        LOGGER.debug("  {} returned.", suggestions.size());
    }

    public int getSize() {
        return _numHits;
    }

    /**
     * TODO javadoc.
     */
    public Element getSummary() throws Exception {
        Element response = new Element("response");
        response.addContent((Element) _elSummary.clone());
        return response;
    }

    /**
     * TODO javadoc.
     */
    public synchronized void close() {
        // TODO remove method
    }

    /**
     * TODO javadoc.
     */
    private void computeQuery(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {

//		resultType is not specified in search params - it's in config?
        Content child = request.getChild(Geonet.SearchResult.RESULT_TYPE);
        String resultType;
        if (child == null) {
            resultType = config.getValue(Geonet.SearchResult.RESULT_TYPE, Geonet.SearchResult.ResultType.HITS);
        } else {
            resultType = child.getValue();
            child.detach();
        }

        _summaryConfig = _luceneConfig.getSummaryTypes().get(resultType);

        final Element summaryItemsEl = request.getChild(Geonet.SearchResult.SUMMARY_ITEMS);
        if (summaryItemsEl != null) {
            summaryItemsEl.detach();

            List<ItemConfig> requestedItems = new ArrayList<ItemConfig>();
            String[] items = summaryItemsEl.getValue().split(",");

            for (String item : items) {
                if (item.startsWith("any")) {
                    requestedItems.addAll(_summaryConfig.getItems());
                    break;
                }
                requestedItems.add(_summaryConfig.get(item.trim()));
            }

            _summaryConfig = new SummaryType(_summaryConfig.getName(), requestedItems);
        }

        if (srvContext != null) {
            @SuppressWarnings("unchecked")
            List<Element> requestedGroups = request.getChildren(SearchParameter.GROUP);

            Set<Integer> userGroups = srvContext.getBean(AccessManager.class).getUserGroups(srvContext.getUserSession(), srvContext.getIpAddress(), false);
            UserSession userSession = srvContext.getUserSession();
            // unless you are logged in as Administrator, check if you are allowed to query the groups in the query
            if (userSession == null || userSession.getProfile() == null ||
                (userSession.getProfile() != Profile.Administrator && userSession.isAuthenticated())) {
                if (!CollectionUtils.isEmpty(requestedGroups)) {
                    for (Element group : requestedGroups) {
                        if (!"".equals(group.getText())
                            && !userGroups.contains(Integer.valueOf(group.getText()))) {
                            throw new UnAuthorizedException("You are not authorized to do this.", null);
                        }
                    }
                }
            }

            // remove elements from user input that compromise this request
            for (String fieldName : UserQueryInput.SECURITY_FIELDS) {
                request.removeChildren(fieldName);
            }

            // if 'restrict to' is set then don't add any other user/group info
            if ((request.getChild(SearchParameter.GROUP) == null) ||
                (StringUtils.isEmpty(request.getChild(SearchParameter.GROUP).getText().trim()))) {
                for (Integer group : userGroups) {
                    request.addContent(new Element(SearchParameter.GROUP).addContent("" + group));
                }
                String owner = null;
                if (userSession != null) {
                    owner = userSession.getUserId();
                }
                if (owner != null) {
                	LOGGER.trace("Search using user \"" + owner + "\".");
                    request.addContent(new Element(SearchParameter.OWNER).addContent(owner));
                    
                    //If the user is editor or more, fill the editorGroup
                    Specification<UserGroup> hasUserIdAndProfile = 
                    		where(
                    				where(UserGroupSpecs.hasProfile(Profile.Reviewer))
                    					.or(UserGroupSpecs.hasProfile(Profile.Editor))
                    					.or(UserGroupSpecs.hasProfile(Profile.UserAdmin)))
                            .and(UserGroupSpecs.hasUserId(userSession.getUserIdAsInt()));

                    List<Integer> editableGroups = srvContext.getBean(UserGroupRepository.class).findGroupIds(hasUserIdAndProfile);

                    LOGGER.trace(" > User has " + editableGroups.size() + " groups with editing privileges.");
					for (Integer group : editableGroups) {
						LOGGER.trace("   > Group: " + group);
                        request.addContent(new Element(SearchParameter.GROUPEDIT).addContent("" + group));
                    }
                }
                //--- in case of an admin show all results
                if (userSession != null) {
                    if (userSession.isAuthenticated()) {
                        if (userSession.getProfile() == Profile.Administrator) {
                            request.addContent(new Element(SearchParameter.ISADMIN).addContent("true"));
                        } else if (userSession.getProfile() == Profile.Reviewer) {
                            request.addContent(new Element(SearchParameter.ISREVIEWER).addContent("true"));
                        }
                    }
                }
            }

            //--- handle the time elements

            processTimeRange(request.getChild(SearchParameter.DATEFROM), "0000-01-01", request.getChild(SearchParameter.DATETO), "9999-01-01");

            processTimeRange(request.getChild(SearchParameter.CREATIONDATEFROM), "0000-01-01", request.getChild(SearchParameter.CREATIONDATETO), "9999-01-01");
            processTimeRange(request.getChild(SearchParameter.REVISIONDATEFROM), "0000-01-01", request.getChild(SearchParameter.REVISIONDATETO), "9999-01-01");
            processTimeRange(request.getChild(SearchParameter.PUBLICATIONDATEFROM), "0000-01-01", request.getChild(SearchParameter.PUBLICATIONDATETO), "9999-01-01");

            //--- some other stuff

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("CRITERIA: {}\n", Xml.getString(request));

            SettingInfo settingInfo = _sm.getSettingInfo();
            SettingInfo.SearchRequestLanguage requestedLanguageOnly = settingInfo.getRequestedLanguageOnly();
            LOGGER.debug("requestedLanguageOnly: {}", requestedLanguageOnly);

            // --- operation parameter
            List<Content> operations = new ArrayList<Content>(request.getChildren("operation"));
            // removes the parameter from the request
            request.removeChildren("operation");

            // Handles operation (filter by download / dynamic visible to the
            // current user)
            if (operations.size() > 0) {
                StringBuilder grpList = new StringBuilder();
                for (Integer g : userGroups) {
                    if (grpList.length() > 0)
                        grpList.append(" or ");
                    grpList.append(g);
                }
                String grps = grpList.toString();
                for (Content elem : operations) {
                    if (elem.getValue().equalsIgnoreCase("view")) {
                        request.addContent(new Element("_operation0").addContent(grps));
                    } else if (elem.getValue().equalsIgnoreCase("download")) {
                        request.addContent(new Element("_operation1").addContent(grps));
                    } else if (elem.getValue().equalsIgnoreCase("editing")) {
                        request.addContent(new Element("_operation2").addContent(grps));
                    } else if (elem.getValue().equalsIgnoreCase("notify")) {
                        request.addContent(new Element("_operation3").addContent(grps));
                    } else if (elem.getValue().equalsIgnoreCase("dynamic")) {
                        request.addContent(new Element("_operation5").addContent(grps));
                    } else if (elem.getValue().equalsIgnoreCase("featured")) {
                        request.addContent(new Element("_operation6").addContent(grps));
                    }
                }
            }

            if (_styleSheetName.equals(Geonet.File.SEARCH_Z3950_SERVER)) {
                // Construct Lucene query by XSLT, not Java, for Z3950 anyway :-)
                Element xmlQuery = _sm.transform(_styleSheetName, request);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("XML QUERY: {}\n", Xml.getString(xmlQuery));
                _query = LuceneSearcher.makeLocalisedQuery(xmlQuery, SearchManager.getAnalyzer(_language.analyzerLanguage, true), _luceneConfig, _language.presentationLanguage, requestedLanguageOnly);
            } else {
                LOGGER.debug("LuceneSearcher constructing Lucene query (LQB)");
                LuceneQueryInput luceneQueryInput = new LuceneQueryInput(request);
                luceneQueryInput.setRequestedLanguageOnly(requestedLanguageOnly);

                _query = new LuceneQueryBuilder(_luceneConfig, _tokenizedFieldSet, SearchManager.getAnalyzer(_language.analyzerLanguage, true), _language.presentationLanguage).build(luceneQueryInput);
                LOGGER.debug("Lucene query: {}", _query);

                _query = appendPortalFilter(_query, _luceneConfig);
                
                try (IndexAndTaxonomy indexReader = _sm.getIndexReader(_language.presentationLanguage, _versionToken)) {
                    // Rewrite the drilldown query to a query that can be used by the search logger
                    _loggerQuery = _query.rewrite(indexReader.indexReader);
                    //if(LOGGER.isDebugEnabled()) LOGGER.debug("Rewritten Lucene query: {}", _loggerQuery);
                    //System.out.println("** rewritten:\n"+ rw);
                } catch (Throwable x) {
                    LOGGER.warn("Error rewriting Lucene query: {}", _query);
                    //System.out.println("** error rewriting query: "+x.getMessage());
                }
            }

            // Boosting query
            if (_boostQueryClass != null) {
                try {
                    LOGGER.debug("Create boosting query: {}", _boostQueryClass);

                    @SuppressWarnings("unchecked")
                    Class<Query> boostClass = (Class<Query>) Class.forName(_boostQueryClass);
                    Class<?>[] clTypesArray = _luceneConfig.getBoostQueryParameterClass();
                    Object[] inParamsArray = _luceneConfig.getBoostQueryParameter();

                    Class<?>[] clTypesArrayAll = new Class[clTypesArray.length + 1];
                    clTypesArrayAll[0] = Class.forName("org.apache.lucene.search.Query");

                    System.arraycopy(clTypesArray, 0, clTypesArrayAll, 1, clTypesArray.length);
                    Object[] inParamsArrayAll = new Object[inParamsArray.length + 1];
                    inParamsArrayAll[0] = _query;
                    System.arraycopy(inParamsArray, 0, inParamsArrayAll, 1, inParamsArray.length);
                    try {
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug("Creating boost query with parameters: {}", Arrays.toString(inParamsArrayAll));
                        Constructor<Query> c = boostClass.getConstructor(clTypesArrayAll);
                        _query = c.newInstance(inParamsArrayAll);
                    } catch (Exception e) {
                        LOGGER.warn(" Failed to create boosting query: {}. Check Lucene configuration", e.getMessage(), e);
                    }
                } catch (Exception e1) {
                    LOGGER.warn(" Error on boosting query initialization: {}. Check Lucene configuration", e1.getMessage(), e1);
                }
            }

            // Use RegionsData rather than fetching from the DB everytime
            //
            //request.addContent(Lib.db.select(dbms, "Regions", "region"));
            //RegionsDAO dao = srvContext.getApplicationContext().getBean(RegionsDAO.class);
            //request.addContent(dao.getAllRegionsAsXml(srvContext));
        }

        Collection<Geometry> geometry = getGeometry(srvContext, request);
        SpatialFilter spatialfilter = null;
        if (geometry != null) {
            StringBuilder wkt = new StringBuilder();
            for (Geometry geom : geometry) {
                wkt.append("geom:").append(geom.toText()).append("\n");
            }
            _geomWKT = wkt.toString();
            spatialfilter = _sm.getSpatial().filter(_query, Integer.MAX_VALUE, geometry, request);
        }

        Filter duplicateRemovingFilter = new DuplicateDocFilter(_query);
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
        LOGGER.debug("Sorting by : {}", sortBy);

        SettingInfo settingInfo = _sm.getSettingInfo();
        boolean sortRequestedLanguageOnTop = settingInfo.getRequestedLanguageOnTop();
        LOGGER.debug("sortRequestedLanguageOnTop: {}", sortRequestedLanguageOnTop);
        _sort = LuceneSearcher.makeSort(Collections.singletonList(Pair.read(sortBy, sortOrder)), _language.presentationLanguage, sortRequestedLanguageOnTop);

    }

    public static Query appendPortalFilter(Query q, LuceneConfig luceneConfig) throws ParseException, QueryNodeException {
        // If the requested portal define a filter
        // Add it to the request.
        NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
        SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Source portal = sourceRepository.findOne(node.getId());
            if (portal == null) {
                LOGGER.warn("Null portal " + node);
            }
            else if (StringUtils.isNotEmpty(portal.getFilter())) {
                Query portalFilterQuery = null;
                // Parse Lucene query
                portalFilterQuery = parseLuceneQuery(portal.getFilter(), luceneConfig);
                LOGGER.info("Portal filter is :\n" + portalFilterQuery);

                BooleanQuery query = new BooleanQuery();
                BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
                query.add(q, occur);

                if (portalFilterQuery != null) {
                    query.add(portalFilterQuery, occur);
                }
                q = query;
                LOGGER.debug("Lucene query (with portal filter): {}", q);
            }
        }
        return q;
    }

    /**
     * TODO javadoc.
     */
    private void processTimeRange(Element fromTime, String defaultFromTime, Element toTime, String defaultToTime) {
        if (fromTime != null && toTime != null) {
            if (fromTime.getTextTrim().equals("") &&
                toTime.getTextTrim().equals("")) {
                fromTime.detach();
                toTime.detach();
            } else {
                if (fromTime.getTextTrim().equals("")) {
                    fromTime.setText(defaultFromTime);
                } else if (toTime.getTextTrim().equals("")) {
                    toTime.setText(defaultToTime);
                }
                String newFromTime = ISODate.parseISODateTime(fromTime.getText());
                fromTime.setText(newFromTime);
                String newToTime = ISODate.parseISODateTime(toTime.getText());
                toTime.setText(newToTime);
            }
        }
    }

    /**
     * Executes Lucene query with sorting option.
     *
     * Default sort by option is RELEVANCE. Default sort order option is not reverse order. Reverse
     * order is active if sort order option is set and not null
     *
     * @param startHit     start
     * @param endHit       end
     * @param buildSummary Compute summary. If true, checks if not already generated (by previous
     *                     search)
     * @return topdocs
     * @throws Exception hmm
     */
    private TopDocs performQuery(ServiceContext context, int startHit, int endHit, boolean buildSummary) throws Exception {
        IndexAndTaxonomy indexAndTaxonomy = _sm.getIndexReader(_language.presentationLanguage, _versionToken);
        _versionToken = indexAndTaxonomy.version;
        Pair<TopDocs, Element> results;
        try {
            results = doSearchAndMakeSummary(endHit, startHit, endHit,
                _language.presentationLanguage,
                _summaryConfig, _luceneConfig,
                indexAndTaxonomy.indexReader,
                _query, _filter, _sort, indexAndTaxonomy.taxonomyReader,
                buildSummary);
        } finally {
            _sm.releaseIndexReader(indexAndTaxonomy);
        }

        TopDocs hits = results.one();
        _elSummary = results.two();
        _numHits = Integer.parseInt(_elSummary.getAttributeValue("count"));

        LOGGER.debug("Hits found : {}", _numHits );

        return hits;
    }

    /**
     * Parses the {@link Geonet.SearchResult#GEOMETRY} parameter. Allowed values are:
     * <ul>
     *  <li>A list of region identifiers prefixed by <code>region:</code> separated by spaces.</li>
     *  <li>A geometry expressed in Well Kown Text (WKT).</li>
     * </ul>
     *
     *
     * @throws IllegalArgumentException if the geometry param is prefixed by <code>region:</code> but the region is not
     * found in the available sources.
     * @throws com.vividsolutions.jts.io.ParseException if geometry is not a valid WKT string and it doesn't start by
     * <code>region:</code>
     * @return a collection of {@link Geometry} obtained of the {@link Geonet.SearchResult#GEOMETRY} parameter or null if the
     * parameter is not present in the request.
     */
    private Collection<Geometry> getGeometry(ServiceContext context, Element request) throws Exception {
        String geomWKT = Util.getParam(request, Geonet.SearchResult.GEOMETRY, null);
        final String prefix = "region:";
        if (StringUtils.startsWithIgnoreCase(geomWKT, prefix)) {
            boolean isWithinFilter = Geonet.SearchResult.Relation.WITHIN.equalsIgnoreCase(Util.getParam(request, Geonet.SearchResult.RELATION, null));
            Collection<RegionsDAO> regionDAOs = context.getApplicationContext().getBeansOfType(RegionsDAO.class).values();
            if (regionDAOs.isEmpty()) {
                throw new IllegalArgumentException(
                    "Found search with a regions geometry prefix but no RegionsDAO objects are registered!\nThis is probably a configuration error.  Make sure the RegionsDAO objects are registered in spring");
            }
            String[] regionIds = geomWKT.substring(prefix.length()).split("\\s*,\\s*");
            Geometry unionedGeom = null;
            List<Geometry> foundGeometries = new ArrayList<>();
            for (String regionId : regionIds) {
                if (regionId.startsWith(prefix)) {
                    regionId = regionId.substring(0, prefix.length());
                }
                for (RegionsDAO dao : regionDAOs) {
                    Geometry geom = dao.getGeom(context, regionId, false, Region.WGS84);
                    if (geom != null) {
                        foundGeometries.add(geom);
                        if (isWithinFilter) {
                            if (unionedGeom == null) {
                                unionedGeom = geom;
                            } else {
                                unionedGeom = unionedGeom.union(geom);
                            }
                        }
                        break; // break out of looking through all RegionDAOs
                    }
                }
            }
            if (regionIds.length > 1 && isWithinFilter) {
                foundGeometries.add(0, unionedGeom);
            }
            if (foundGeometries.size() == 0) {
                throw new IllegalArgumentException(String.format("Geometry %s not found in the available source RegionDAO objects", geomWKT));
            }
            return foundGeometries;
        } else if (geomWKT != null) {
            WKTReader reader = new WKTReader();
            return Arrays.asList(reader.read(geomWKT));
        } else {
            return null;
        }
    }

    /**
     * <p> Gets all metadata uuids in current searcher. </p>
     *
     * @param maxHits max hits
     * @return current searcher result in "fast" mode
     * @throws Exception hmm
     */
    public List<String> getAllUuids(int maxHits, ServiceContext context) throws Exception {
        List<String> response = new ArrayList<String>();
        TopDocs tdocs = performQuery(context, 0, maxHits, false);

        IndexAndTaxonomy indexAndTaxonomy = _sm.getIndexReader(_language.presentationLanguage, _versionToken);
        _versionToken = indexAndTaxonomy.version;
        try {
            for (ScoreDoc sdoc : tdocs.scoreDocs) {
                DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor("_uuid");
                indexAndTaxonomy.indexReader.document(sdoc.doc, docVisitor);
                Document doc = docVisitor.getDocument();
                String uuid = doc.get("_uuid");
                if (uuid != null) response.add(uuid);
            }
        } finally {
            _sm.releaseIndexReader(indexAndTaxonomy);
        }
        return response;
    }

    /**
     * <p> Gets all metadata info as a int Map in current searcher. </p>
     */
    public Map<Integer, AbstractMetadata> getAllMdInfo(ServiceContext context, int maxHits) throws Exception {

        Map<Integer, AbstractMetadata> response = new HashMap<Integer, AbstractMetadata>();
        TopDocs tdocs = performQuery(context, 0, maxHits, false);
        IndexAndTaxonomy indexAndTaxonomy = _sm.getIndexReader(_language.presentationLanguage, _versionToken);
        _versionToken = indexAndTaxonomy.version;
        try {
            for (ScoreDoc sdoc : tdocs.scoreDocs) {
                DocumentStoredFieldVisitor docVisitor = new DocumentStoredFieldVisitor("_id", "_root", "_schema", "_createDate", "_changeDate",
                    "_source", "_isTemplate", "_title", "_uuid", "_isHarvested", "_owner", "_groupOwner");
                indexAndTaxonomy.indexReader.document(sdoc.doc, docVisitor);
                Document doc = docVisitor.getDocument();

                Metadata mdInfo = Metadata.createFromLuceneIndexDocument(doc);
                response.put(mdInfo.getId(), mdInfo);
            }
        } finally {
            _sm.releaseIndexReader(indexAndTaxonomy);
        }
        return response;
    }

    public static class LanguageSelection {
        public final String analyzerLanguage;
        public final String presentationLanguage;

        public LanguageSelection(String analyzerLanguage, String presentationLanguage) {
            this.analyzerLanguage = analyzerLanguage;
            this.presentationLanguage = presentationLanguage;
        }
    }
    
    /**
     * <p> Gets the Lucene version token. Can be used as ETag. </p>
     */    
    public long getVersionToken() {
    	return _versionToken;
    };


    /**
     * Creates a lucene Query object from a lucene query string using Lucene query syntax.
     */
    public static Query parseLuceneQuery(
        String cswServiceSpecificConstraint, LuceneConfig _luceneConfig)
        throws ParseException, QueryNodeException {
//        MultiFieldQueryParser parser = new MultiFieldQueryParser(Geonet.LUCENE_VERSION, fields , SearchManager.getAnalyzer());
        StandardQueryParser parser = new StandardQueryParser(SearchManager.getAnalyzer());
        Map<String, NumericConfig> numericMap = new HashMap<String, NumericConfig>();
        for (LuceneConfigNumericField field : _luceneConfig.getNumericFields().values()) {
            String name = field.getName();
            int precisionStep = field.getPrecisionStep();
            NumberFormat format = NumberFormat.getNumberInstance();
            FieldType.NumericType type = FieldType.NumericType.valueOf(field.getType().toUpperCase());
            NumericConfig config = new NumericConfig(precisionStep, format, type);
            numericMap.put(name, config);
        }
        parser.setNumericConfigMap(numericMap);
        Query q = parser.parse(cswServiceSpecificConstraint, "title");

        // List of lucene fields which MUST not be control by user, to be removed from the CSW service specific constraint
        List<String> SECURITY_FIELDS = Arrays.asList(
            LuceneIndexField.OWNER,
            LuceneIndexField.GROUP_OWNER);

        BooleanQuery bq;
        if (q instanceof BooleanQuery) {
            bq = (BooleanQuery) q;
            List<BooleanClause> clauses = bq.clauses();

            Iterator<BooleanClause> it = clauses.iterator();
            while (it.hasNext()) {
                BooleanClause bc = it.next();

                for (String fieldName : SECURITY_FIELDS) {
                    if (bc.getQuery().toString().contains(fieldName + ":")) {
                        if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH, "LuceneSearcher getCswServiceSpecificConstraintQuery removed security field: " + fieldName);
                        it.remove();

                        break;
                    }
                }
            }
        }
        return q;
    }
}
