package org.fao.geonet.kernel.search.keyword;

import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KeywordSearchParams {

    private final QueryBuilder<KeywordBean> queryBuilder;
    private final LinkedHashSet<String> thesauriNames;
    private final String thesauriDomainName;
    private int maxResults;
    public KeywordSearchParams(QueryBuilder<KeywordBean> query, Set<String> thesauriNames, String thesauriDomainName, int maxResults) {
        this.queryBuilder = query;
        this.thesauriNames = new LinkedHashSet<String>(thesauriNames);
        this.thesauriDomainName = thesauriDomainName;
        this.maxResults = maxResults;
    }
    
    /**
     * Create the actual Query object for performing the search.
     * 
     * @return the actual Query object for performing the search.
     */
    public List<KeywordBean> search(ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        if(thesauriNames.isEmpty()) {
            return executeAll(queryBuilder, finder);
        } else if(thesauriNames.size() == 1) {
            return executeOne(queryBuilder, finder);
        } else {
            return executeSpecific(queryBuilder, finder);
        }
    }
    private List<KeywordBean> executeOne(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        List<KeywordBean> results = new ArrayList<KeywordBean>();
        int id = 0;
        String thesaurusName = thesauriNames.iterator().next();
        Thesaurus thesaurus = finder.getThesaurusByName(thesaurusName);
        Query<KeywordBean> query = queryBuilder.limit(maxResults-results.size()).build();
        if (thesaurus == null) {
            throw new IllegalArgumentException("The thesaurus "+thesaurusName+" does not exist, there for the query cannot be excuted: '"+query+"'" );
        }
        for (KeywordBean keywordBean : query.execute(thesaurus)) {
            if (maxResults > -1 && results.size() >= maxResults) {
                break;
            }
            keywordBean.setId(id);
            results.add(keywordBean);
            id++;
        }
        return results;
    }
    private List<KeywordBean> executeSpecific(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        List<KeywordBean> results = new ArrayList<KeywordBean>();
        int id = 0;

        for (Thesaurus thesaurus : finder.getThesauriMap().values()) {
            Query<KeywordBean> query = queryBuilder.limit(maxResults-results.size()).build();
            if(thesauriNames.contains(thesaurus.getKey())) {
                for (KeywordBean keywordBean : query.execute(thesaurus)) {
                    keywordBean.setId(id);
                    results.add(keywordBean);
                    id++;
                }
            }
        }
        return results;
    }
    private List<KeywordBean> executeAll(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        int id = 0;
        List<KeywordBean> results = new ArrayList<KeywordBean>();
        for (Thesaurus thesaurus : finder.getThesauriMap().values()) {
            if(thesauriDomainName==null || thesauriDomainName.equals(thesaurus.getDname())) {
                Query<KeywordBean> query = queryBuilder.limit(maxResults-results.size()).build();
                for (KeywordBean keywordBean : query.execute(thesaurus)) {
                    if (maxResults > -1 && results.size() >= maxResults) {
                        break;
                    }
                    keywordBean.setId(id);
                    results.add(keywordBean);
                    id++;
                }
            }
        }

        return results;
    }
    
}
