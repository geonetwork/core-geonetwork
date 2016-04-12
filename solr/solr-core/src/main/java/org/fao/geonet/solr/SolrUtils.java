package org.fao.geonet.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.response.AnalysisResponseBase;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;
import org.apache.solr.client.solrj.response.FieldStatsInfo;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.AnalysisParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to run Solr requests like field analysis.
 *
 * Created by francois on 30/09/14.
 */
public class SolrUtils {
    private static String PHASE_INDEX = "index";
    private static String PHASE_QUERY = "query";
    private static String DEFAULT_FILTER_CLASS = "org.apache.lucene.analysis.synonym.SynonymFilter";


    /**
     * Return the number of rows matching the query.
     *
     * @param query The query
     * @return
     */
    public static Double getNumFound(String query, String... filterQuery) {
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            if (filterQuery != null) {
                solrQuery.setFilterQueries(filterQuery);
            }
            solrQuery.setRows(0);

            SolrJProxy serverBean = SolrJProxy.get();
            SolrClient solrServer = serverBean.getServer();

            QueryResponse solrResponse = solrServer.query(solrQuery);
            return (double)solrResponse.getResults().getNumFound();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Double getStats(String query, String[] filterQuery, String statsField, String stats) {
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            if (filterQuery != null) {
                solrQuery.setFilterQueries(filterQuery);
            }
            solrQuery.setRows(0);
            solrQuery.setGetFieldStatistics(true);
            solrQuery.setGetFieldStatistics(statsField);

            SolrJProxy serverBean = SolrJProxy.get();
            SolrClient solrServer = serverBean.getServer();

            QueryResponse solrResponse = solrServer.query(solrQuery);
            FieldStatsInfo fieldStatsInfo = solrResponse.getFieldStatsInfo().get(statsField);

            if (fieldStatsInfo != null) {
                if ("min".equals(stats)) {
                    return (Double)fieldStatsInfo.getMin();
                } else if ("max".equals(stats)) {
                    return (Double)fieldStatsInfo.getMax();
                } else if ("count".equals(stats)) {
                    return fieldStatsInfo.getCount().doubleValue();
                } else if ("missing".equals(stats)) {
                    return fieldStatsInfo.getMissing().doubleValue();
                } else if ("mean".equals(stats)) {
                    return (Double)fieldStatsInfo.getMean();
                } else if ("sum".equals(stats)) {
                    return (Double) fieldStatsInfo.getSum();
                } else if ("stddev".equals(stats)) {
                    return fieldStatsInfo.getStddev();
                } else if ("countDistinct".equals(stats)) {
                    return fieldStatsInfo.getCountDistinct().doubleValue();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Analyze a field and a value against the index phase
     * and return the first value generated
     * by the {@see DEFAULT_FILTER_CLASS}.
     *
     * The field tested MUST use a DEFAULT_FILTER_CLASS
     * in the analyzer chain.
     *
     * See {@see analyzeField}.
     *
     * @param fieldName
     * @param fieldValue
     * @return
     */
    public static String analyzeField(String fieldName,
                                      String fieldValue) {
        return analyzeField(fieldName, fieldValue, PHASE_INDEX, DEFAULT_FILTER_CLASS, 0);
    }

    /**
     * Analyze a field and a value against the index
     * or query phase and return the first value generated
     * by the specified filterClass.
     *
     * If an exception occured, the field value is returned.
     *
     * Equivalent to: {@linkplain http://localhost:8983/solr/analysis/field?analysis.fieldname=inspireTheme_syn&q=hoogte}
     *
     * TODO: Logger.
     *
     * @param fieldName The field name
     * @param fieldValue    The field value to analyze
     * @param analysisPhaseName The analysis phase (ie. "index" or "query")
     * @param filterClass   The filter class the response should be extracted from
     * @param tokenPosition The position of the token to extract
     *
     * @return  The analyzed string value if found or the field value if not found.
     */
    public static String analyzeField(String fieldName,
                                      String fieldValue,
                                      String analysisPhaseName,
                                      String filterClass,
                                      int tokenPosition) {

        try {
            SolrClient server = SolrJProxy.get().getServer();

            ModifiableSolrParams params = new ModifiableSolrParams();
            params.set(AnalysisParams.FIELD_NAME, fieldName);
            params.set(AnalysisParams.FIELD_VALUE, fieldValue);

            FieldAnalysisRequest request = new FieldAnalysisRequest();
            List<String> fieldNames = new ArrayList<String>();
            fieldNames.add(fieldName);
            request.setFieldNames(fieldNames);
            request.setFieldValue(fieldValue);

            FieldAnalysisResponse res = new FieldAnalysisResponse();
            try {
                res.setResponse(server.request(request));
            } catch (SolrServerException e) {
                e.printStackTrace();
                return fieldValue;
            } catch (IOException e) {
                e.printStackTrace();
                return fieldValue;
            }
            FieldAnalysisResponse.Analysis analysis =
                res.getFieldNameAnalysis(fieldName);

            Iterable<AnalysisResponseBase.AnalysisPhase> phases =
                PHASE_INDEX.equals(analysisPhaseName) ?
                    analysis.getIndexPhases() : analysis.getQueryPhases();
            if (phases != null) {
                Iterator<AnalysisResponseBase.AnalysisPhase> iterator =
                    phases.iterator();
                while (iterator.hasNext()) {
                    AnalysisResponseBase.AnalysisPhase analysisPhase = iterator.next();
                    if (analysisPhase.getClassName()
                        .equals(filterClass) &&
                        analysisPhase.getTokens().size() > 0) {
                        AnalysisResponseBase.TokenInfo token =
                            analysisPhase.getTokens().get(tokenPosition);
                        return token.getText();
                    }
                }
            }
            return fieldValue;
        } catch (Exception e) {
            e.printStackTrace();
            return fieldValue;
        }
    }
}
