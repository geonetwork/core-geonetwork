/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.search.keyword;

import static org.fao.geonet.kernel.AllThesaurus.ALL_THESAURUS_KEY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import jakarta.annotation.Nullable;

import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
        if (thesauriNames.isEmpty()) {
            return executeAll(queryBuilder, finder);
        } else if (thesauriNames.contains(ALL_THESAURUS_KEY) && finder.existsThesaurus(ALL_THESAURUS_KEY)) {
            return executeAll(queryBuilder, finder);
        } else if (thesauriNames.size() == 1) {
            if (comparator != null) {
                return executeOneSorted(queryBuilder, finder);
            } else {
                return executeOne(queryBuilder, finder);
            }
        } else {
            return executeSpecific(queryBuilder, finder);
        }
    }

    private List<KeywordBean> executeOne(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
    	LinkedHashSet<KeywordBean> results = new LinkedHashSet<>();
        AtomicInteger id = new AtomicInteger();
        String thesaurusName = thesauriNames.iterator().next();
        Thesaurus thesaurus = finder.getThesaurusByName(thesaurusName);
        Query<KeywordBean> query = queryBuilder.limit(maxResults).build();
        if (thesaurus == null) {
            throw new IllegalArgumentException("The thesaurus " + thesaurusName + " does not exist, there for the query cannot be executed: '" + query + "'");
        }

        id = executeQuery(id, results, thesaurus, query, maxResults);
        return setToList(results);
    }

    private List<KeywordBean> executeOneSorted(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        TreeSet<KeywordBean> orderedResults = new TreeSet<>(this.comparator);
        AtomicInteger id = new AtomicInteger();
        String thesaurusName = thesauriNames.iterator().next();
        Thesaurus thesaurus = finder.getThesaurusByName(thesaurusName);
        Query<KeywordBean> query = queryBuilder.limit(maxResults).build();
        if (thesaurus == null) {
            throw new IllegalArgumentException("The thesaurus " + thesaurusName + " does not exist, there for the query cannot be executed: '" + query + "'");
        }

        id = executeQuery(id, orderedResults, thesaurus, query, -1);
        return setToList(orderedResults);
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
    	AtomicInteger id = new AtomicInteger();
    	LinkedHashSet<KeywordBean> results = new LinkedHashSet<>();

        for (Thesaurus thesaurus : getThesaurusListToSearchInto(finder)) {
            if (thesauriDomainName == null || thesauriDomainName.equals(thesaurus.getDname())) {
                Query<KeywordBean> query = queryBuilder.limit(maxResults - results.size()).build();
                id = executeQuery(id, results, thesaurus, query, maxResults);
            }
        }

        return  setToList(results);
    }

	private AtomicInteger executeQuery(AtomicInteger id, Collection<KeywordBean> results, Thesaurus thesaurus, Query<KeywordBean> query, Integer maxResults)
			throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
		for (KeywordBean keywordBean : query.execute(thesaurus)) {
		    if (maxResults > -1 && results.size() >= maxResults) {
		        break;
		    }
		    keywordBean.setId(id.getAndIncrement());
		    results.add(keywordBean);
		}
		return id;
	}

    private List<KeywordBean> executeAllSorted(QueryBuilder<KeywordBean> queryBuilder, ThesaurusFinder finder) throws IOException,
        MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        AtomicInteger id = new AtomicInteger();

        TreeSet<KeywordBean> results = new TreeSet<>(this.comparator);

        for (Thesaurus thesaurus : getThesaurusListToSearchInto(finder)) {
            Query<KeywordBean> query = queryBuilder.build();
            if (thesauriDomainName == null || thesauriDomainName.equals(thesaurus.getDname())) {
                id = executeQuery(id, results, thesaurus, query, -1);
            }
        }

        return setToList(results);
    }

    private Collection<Thesaurus> getThesaurusListToSearchInto(ThesaurusFinder finder) {
        Map<String, Thesaurus> thesauri = finder.getThesauriMap();
        if (!thesauri.containsKey(ALL_THESAURUS_KEY)) {
            return thesauri.values();
        }
        if (thesauriNames.contains(ALL_THESAURUS_KEY)) {
            return Collections.singletonList(thesauri.get(ALL_THESAURUS_KEY));
        }
        return thesauri.values().stream().filter(t -> !(t.getKey().equals(ALL_THESAURUS_KEY))).collect(Collectors.toList());
    }

    private ArrayList<KeywordBean> setToList(Set<KeywordBean> results) {
    	ArrayList<KeywordBean> list = null;
    	if(maxResults < 0) {
            list = Lists.newArrayListWithCapacity(results.size());
    	} else {
            list = Lists.newArrayListWithCapacity(Math.min(maxResults, results.size()));
    	}
        for (KeywordBean keywordBean : results) {
            if  (maxResults > -1 && list.size() >= maxResults) {
                break;
            }
            list.add(keywordBean);
        }
        return list;
    }

}
