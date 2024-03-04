/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.fao.geonet.Constants;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.Log;
import org.locationtech.jts.util.Assert;
import org.openrdf.model.GraphException;
import org.openrdf.model.URI;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.query.QueryResultsTable;
import org.openrdf.sesame.repository.local.LocalRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jesse on 2/27/2015.
 */
public class AllThesaurus extends Thesaurus {
    public static final String FNAME = "allThesaurus";
    public static final String SEPARATOR = "@@@";
    static final String TYPE = "external";
    static final String DNAME = "none";
    public static final String ALL_THESAURUS_KEY = TYPE + "." + DNAME + "." + FNAME;
    static final String TITLE = "All Keywords";
    private static final String URI_CODE_PREFIX = "http://org.fao.geonet.thesaurus.all/";

    @Autowired
    private ThesaurusFinder thesaurusFinder;

    @Autowired
    private IsoLanguagesMapper isoLangMapper;

    private String downloadUrl;
    private String keywordUrl;
    private List<String> allThesaurusExclude = new ArrayList<>();

    public void init(String siteUrl) {
        this.downloadUrl = buildDownloadUrl(FNAME, TYPE, DNAME, siteUrl);
        this.keywordUrl = buildKeywordUrl(FNAME, TYPE, DNAME, siteUrl);
    }

    public static String buildKeywordUri(KeywordBean actualWord) {
        return buildKeywordUri(actualWord.getThesaurusKey(), actualWord.getUriCode());
    }

    @VisibleForTesting
    static String buildKeywordUri(String thesaurusKey, String uri) {
        try {
            thesaurusKey = URLEncoder.encode(thesaurusKey, Constants.ENCODING);
//            uri = URLEncoder.encode(uri, Constants.ENCODING);
            return URI_CODE_PREFIX + thesaurusKey + SEPARATOR + uri;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setThesaurusFinder(ThesaurusFinder thesaurusFinder) {
        this.thesaurusFinder = thesaurusFinder;
    }

    public void setIsoLangMapper(IsoLanguagesMapper isoLangMapper) {
        this.isoLangMapper = isoLangMapper;
    }

    public List<String> getAllThesaurusExclude() {
        return allThesaurusExclude;
    }

    public void setAllThesaurusExclude(List<String> allThesaurusExclude) {
        this.allThesaurusExclude = allThesaurusExclude;
    }

    @Override
    public String getKey() {
        return ALL_THESAURUS_KEY;
    }

    @Override
    public String getDname() {
        return DNAME;
    }

    @Override
    public String getFname() {
        return FNAME;
    }

    @Override
    public Path getFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public String getDate() {
        return "";
    }

    @Override
    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    @Override
    public String getKeywordUrl() {
        return this.keywordUrl;
    }

    @Override
    public void retrieveThesaurusInformation() {
        // nothing to do
    }

    @Override
    public synchronized LocalRepository getRepository() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus setRepository(LocalRepository repository) {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus initRepository() throws ConfigurationException, IOException {
        // do nothing
        return this;
    }

    @Override
    public synchronized QueryResultsTable performRequest(final String query) throws IOException, MalformedQueryException,
        QueryEvaluationException, AccessDeniedException {
        final Map<Thesaurus, QueryResultsTable> allResults = Maps.newIdentityHashMap();
        onThesauri(null, new Function<Thesaurus, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull Thesaurus input) {
                final QueryResultsTable queryResultsTable;
                try {
                    queryResultsTable = input.performRequest(query);
                    if (queryResultsTable.getRowCount() > 0) {
                        allResults.put(input, queryResultsTable);
                    }
                } catch (IOException | AccessDeniedException | QueryEvaluationException | MalformedQueryException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

        return new AllQueryResultsTable(allResults);
    }

    @Override
    public boolean hasConceptScheme(String uri) {
        return false;
    }

    @Override
    public synchronized URI addElement(KeywordBean keyword) throws IOException, AccessDeniedException, GraphException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus removeElement(KeywordBean keyword) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus removeElement(String namespace, String code) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus removeElement(String uri) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized URI updateElement(KeywordBean keyword, boolean replace) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized boolean isFreeCode(final String namespace, final String code) throws AccessDeniedException {
        return onThesauri(true, new Function<Thesaurus, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(Thesaurus thesaurus) {
                try {
                    if (!thesaurus.isFreeCode(namespace, code)) {
                        return false;
                    }
                } catch (AccessDeniedException e) {
                    // skip
                }
                return null;
            }
        });
    }

    @Override
    public Thesaurus updateCode(KeywordBean bean, String newcode) throws AccessDeniedException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus updateCode(String namespace, String oldcode, String newcode) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Thesaurus updateCodeByURI(String olduri, String newuri) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeConceptScheme(String thesaurusTitle, String namespace) throws IOException, AccessDeniedException, GraphException {
        throw new UnsupportedOperationException();
    }


    @Override
    public IsoLanguagesMapper getIsoLanguageMapper() {
        return isoLangMapper;
    }

    @Override
    public synchronized void addRelation(String subject, KeywordRelation related, String relatedSubject) throws AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public KeywordBean getKeyword(String uri, String... languages) {
        final DecomposedAllUri decomposedAllUri = new DecomposedAllUri(uri);
        final Thesaurus thesaurus = this.thesaurusFinder.getThesaurusByName(decomposedAllUri.thesaurusKey);

        final KeywordBean keyword = thesaurus.getKeyword(decomposedAllUri.keywordUri, languages);
        updateKeywordBeanThesaurusInfo(keyword);
        return keyword;
    }

    private KeywordBean updateKeywordBeanThesaurusInfo(KeywordBean keyword) {
        keyword.setUriCode(buildKeywordUri(keyword));
        keyword.setThesaurusInfo(this);

        return keyword;
    }

    @Override
    public boolean hasKeyword(String uri) {
        final DecomposedAllUri decomposedAllUri = new DecomposedAllUri(uri);
        final Thesaurus thesaurus = this.thesaurusFinder.getThesaurusByName(decomposedAllUri.thesaurusKey);

        return thesaurus.hasKeyword(decomposedAllUri.keywordUri);
    }

    @Override
    public List<KeywordBean> getRelated(final String uri, final KeywordRelation request, final String... languages) {
        final DecomposedAllUri decomposedAllUri = new DecomposedAllUri(uri);

        final Thesaurus thesaurus = this.thesaurusFinder.getThesaurusByName(decomposedAllUri.thesaurusKey);

        return Lists.transform(thesaurus.getRelated(decomposedAllUri.keywordUri, request, languages), new Function<KeywordBean,
            KeywordBean>() {
            @Nullable
            @Override
            public KeywordBean apply(@Nullable KeywordBean keywordBean) {
                return updateKeywordBeanThesaurusInfo(keywordBean);
            }
        });
    }

    @Override
    public boolean hasKeywordWithLabel(final String label, final String langCode) {
        return onThesauri(false, new Function<Thesaurus, Boolean>() {

            @Nullable
            @Override
            public Boolean apply(Thesaurus thesaurus) {
                if (thesaurus.hasKeywordWithLabel(label, langCode)) {
                    return true;
                }
                return null;
            }
        });
    }

    private <R> R onThesauri(R defaultVal, Function<Thesaurus, R> function) {
        for (Thesaurus thesaurus : this.thesaurusFinder.getThesauriMap().values()) {
            if (ALL_THESAURUS_KEY.equals(thesaurus.getKey())
                || allThesaurusExclude.contains(thesaurus.getKey())) {
                continue;
            }
            final R result = function.apply(thesaurus);
            if (result != null) {
                return result;
            }
        }

        return defaultVal;
    }

    @Override
    public KeywordBean getKeywordWithLabel(final String label, final String langCode) {
        KeywordBean result = onThesauri(null, new Function<Thesaurus, KeywordBean>() {
            @Nullable
            @Override
            public KeywordBean apply(@Nonnull Thesaurus thesaurus) {
                try {
                    return updateKeywordBeanThesaurusInfo(thesaurus.getKeywordWithLabel(label, langCode));
                } catch (TermNotFoundException e) {
                    return null;
                }
            }
        });

        if (result == null) {
            throw new TermNotFoundException(label);
        }

        return result;
    }

    @Override
    public synchronized void clear() throws IOException, AccessDeniedException {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public FileTime getLastModifiedTime() {
        long lastModified = -1;
        for (Thesaurus thesaurus : this.thesaurusFinder.getThesauriMap().values()) {
            if (thesaurus.getKey().equals(ALL_THESAURUS_KEY)) {
                continue;
            }
            long thesLastModified = thesaurus.getLastModifiedTime().toMillis();
            if (thesLastModified > lastModified) {
                lastModified = thesLastModified;
            }
        }

        if (Log.isDebugEnabled(Geonet.THESAURUS)) {
            Log.debug(Geonet.THESAURUS, ALL_THESAURUS_KEY + " has lastModified of: " + lastModified);
        }

        return FileTime.fromMillis(lastModified);
    }

    @Override
    public String getDefaultNamespace() {
        try {
            final Iterator<Thesaurus> iterator = thesaurusFinder.getThesauriMap().values().iterator();
            Thesaurus thesaurus = null;
            while (iterator.hasNext()) {
                thesaurus = iterator.next();
                if (!AllThesaurus.ALL_THESAURUS_KEY.equals(thesaurus.getKey())) {
                    break;
                } else {
                    thesaurus = null;
                }
            }

            if (thesaurus != null) {
                return URI_CODE_PREFIX + URLEncoder.encode(thesaurus.getDefaultNamespace(), Constants.ENCODING);
            } else {
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class DecomposedAllUri {
        private static final Pattern URI_DECOMPOSER = Pattern.compile(Pattern.quote(URI_CODE_PREFIX) + "(.+)" + SEPARATOR + "(.+)");
        public final String thesaurusKey, keywordUri;

        public DecomposedAllUri(String allUri) {
            final Matcher matcher = URI_DECOMPOSER.matcher(allUri);
            Assert.isTrue(matcher.matches(), allUri + "is not an 'all' keyword");
            try {
                thesaurusKey = URLDecoder.decode(matcher.group(1), Constants.ENCODING);
                keywordUri = URLDecoder.decode(matcher.group(2), Constants.ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "DecomposedAllUri{" +
                "thesaurusKey='" + thesaurusKey + '\'' +
                ", keywordUri='" + keywordUri + '\'' +
                '}';
        }
    }

}
