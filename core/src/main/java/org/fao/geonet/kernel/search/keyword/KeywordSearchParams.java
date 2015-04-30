package org.fao.geonet.kernel.search.keyword;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.kernel.AllThesaurus;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;

import static org.fao.geonet.kernel.AllThesaurus.ALL_THESAURUS_KEY;

public class KeywordSearchParams {

    private final QueryBuilder<KeywordBean> queryBuilder;
    private final LinkedHashSet<String> thesauriNames;
    private final String thesauriDomainName;
    private final Comparator<KeywordBean> comparator;
    private int maxResults;
    public KeywordSearchParams(QueryBuilder<KeywordBean> query, Set<String> thesauriNames, String thesauriDomainName, int maxResults,
                               Comparator<KeywordBean> comparator) {
        this.queryBuilder = query;
        this.thesauriNames = new LinkedHashSet<>(thesauriNames);
        this.thesauriDomainName = thesauriDomainName;
        this.maxResults = maxResults;
        this.comparator = comparator;
    }
    
    /**
     * Create the actual Query object for performing the search.
     * 
     * @return the actual Query object for performing the search.
     */
    public List<KeywordBean> search(ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        if(thesauriNames.isEmpty()) {
            return executeAll(queryBuilder, finder);
        } else if (thesauriNames.contains(ALL_THESAURUS_KEY) && finder.existsThesaurus(ALL_THESAURUS_KEY)) {
            final Thesaurus allThesaurus = finder.getThesaurusByName(ALL_THESAURUS_KEY);
            List<KeywordBean> resultsOriginalThesaurus = executeAll(queryBuilder, finder);
            return Lists.transform(resultsOriginalThesaurus, new Function<KeywordBean, KeywordBean>() {
                @Nullable
                @Override
                public KeywordBean apply(KeywordBean input) {
                    if (input != null) {
                        input.setUriCode(AllThesaurus.buildKeywordUri(input));
                        input.setThesaurusInfo(allThesaurus);
                    }
                    return input;
                }
            });
        } else if(thesauriNames.size() == 1) {
            return executeOne(queryBuilder, finder);
        } else {
            return executeSpecific(queryBuilder, finder);
        }
    }
    private List<KeywordBean> executeOne(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        List<KeywordBean> results = new ArrayList<>();
        int id = 0;
        String thesaurusName = thesauriNames.iterator().next();
        Thesaurus thesaurus = finder.getThesaurusByName(thesaurusName);
        Query<KeywordBean> query = queryBuilder.limit(maxResults-results.size()).build();
        if (thesaurus == null) {
            throw new IllegalArgumentException("The thesaurus "+thesaurusName+" does not exist, there for the query cannot be executed: '"+query+"'" );
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
    private List<KeywordBean> executeSpecific(QueryBuilder<KeywordBean> queryBuilder, final ThesaurusFinder finder)
            throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        return executeAll(queryBuilder, new ThesaurusFinder() {
            @Override
            public boolean existsThesaurus(String name) {
                return thesauriNames.contains(name) && finder.existsThesaurus(name);
            }

            @Override
            public Thesaurus getThesaurusByName(String thesaurusName) {
                if (thesauriNames.contains(thesaurusName)) {
                    return finder.getThesaurusByName(thesaurusName);
                }
                return null;
            }

            @Override
            public Thesaurus getThesaurusByConceptScheme(String conceptSchemeUri) {
                return finder.getThesaurusByName(conceptSchemeUri);
            }

            @Override
            public Map<String, Thesaurus> getThesauriMap() {
                Map<String, Thesaurus> thesaurusMap = Maps.newHashMap();
                for (String name : thesauriNames) {
                    Thesaurus th = finder.getThesaurusByName(name);
                    if (th != null) {
                        thesaurusMap.put(name, th);
                    }
                }
                return thesaurusMap;
            }
        });
    }
    private List<KeywordBean> executeAll(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws
            IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {

        if (comparator != null) {
            return executeAllSorted(queryBuilder, finder);
        } else {
            return executeAllUnsorted(queryBuilder, finder);
        }
    }

    private List<KeywordBean> executeAllUnsorted(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException,
            MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        int id = 0;
        List<KeywordBean> results = new ArrayList<>();
        for (Thesaurus thesaurus : finder.getThesauriMap().values()) {
            if (thesaurus.getKey().equals(ALL_THESAURUS_KEY)) {
                continue;
            }
            if (thesauriDomainName == null || thesauriDomainName.equals(thesaurus.getDname())) {
                Query<KeywordBean> query = queryBuilder.limit(maxResults - results.size()).build();
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

    private List<KeywordBean> executeAllSorted(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException,
            MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        int id = 0;

        TreeSet<KeywordBean> results = new TreeSet<>(this.comparator);
        for (Thesaurus thesaurus : finder.getThesauriMap().values()) {
            if (thesaurus.getKey().equals(ALL_THESAURUS_KEY)) {
                continue;
            }
            Query<KeywordBean> query = queryBuilder.build();
            if(thesauriDomainName==null || thesauriDomainName.equals(thesaurus.getDname())) {
                for (KeywordBean keywordBean : query.execute(thesaurus)) {
                    keywordBean.setId(id);
                    results.add(keywordBean);
                    id++;
                }
            }
        }

        return TreeSetToList(results);
    }

    private ArrayList<KeywordBean> TreeSetToList(TreeSet<KeywordBean> results) {
        ArrayList<KeywordBean> list = Lists.newArrayListWithCapacity(Math.min(maxResults, results.size()));
        for (KeywordBean keywordBean : results) {
            if (list.size() >= maxResults) {
                break;
            }
            list.add(keywordBean);
        }
        return list;
    }

}
