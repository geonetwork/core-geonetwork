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

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterParams;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import jeeves.server.context.ServiceContext;

/**
 * The actual Environment implementation.
 *
 * @author Jesse on 10/20/2014.
 */
public class EnvironmentImpl implements Environment {
    private final String lang3;
    private final String lang2;
    private final String resourceUrl;
    private final Multimap<String, ParamValue> params = ArrayListMultimap.create();
    private final FormatType formatType;
    private final AbstractMetadata metadataInfo;
    private final String locUrl;
    private final Element jdomMetadata;
    private final ServiceContext serviceContext;
    private final WebRequest webRequest;
    private final FormatterWidth width;
    private Multimap<String, String> indexInfo = null;

    public EnvironmentImpl(FormatterParams fparams, IsoLanguagesMapper mapper) {
        jdomMetadata = fparams.metadata;
        this.width = fparams.width;
        this.lang3 = fparams.context.getLanguage();
        this.lang2 = mapper.iso639_2_to_iso639_1(lang3, "en");

        this.formatType = fparams.formatType;
        this.resourceUrl = fparams.getResourceUrl();
        this.locUrl = fparams.getLocUrl();
        this.metadataInfo = fparams.metadataInfo;
        for (Map.Entry<String, String[]> entry : fparams.webRequest.getParameterMap().entrySet()) {
            for (String value : entry.getValue()) {
                this.params.put(entry.getKey(), new ParamValue(value));
            }
        }

        this.webRequest = fparams.webRequest;
        this.serviceContext = fparams.context;
    }

    /**
     * Return the map of all parameters passed to the Format service.
     */
    public Multimap<String, ParamValue> params() {
        return this.params;
    }

    /**
     * Return the value of the first parameter with the provided name.  Null is returned if there is
     * no parameter with the given name.
     */
    public ParamValue param(String paramName) {
        final Collection<ParamValue> paramValues = this.params.get(paramName);
        if (paramValues.isEmpty()) {
            return new ParamValue("") {
                @Override
                public String toString() {
                    return "Null Value";
                }

                @Override
                public boolean toBool() {
                    return false;
                }

                @Override
                public int toInt() {
                    return -1;
                }

                @Override
                public Double toDouble() {
                    return -1.0;
                }
            };
        }
        return paramValues.iterator().next();
    }

    /**
     * Return ALL values of parameter with the provided name.
     */
    public Collection<ParamValue> paramValues(String paramName) {
        return this.params.get(paramName);
    }

    public String getLang3() {
        return this.lang3;
    }

    public String getLang2() {
        return this.lang2;
    }

    @Override
    public int getMetadataId() {
        return this.metadataInfo.getId();
    }

    @Override
    public String getMetadataUUID() {
        return this.metadataInfo.getUuid();

    }

    @Override
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    @Override
    public String getLocalizedUrl() {
        return this.locUrl;
    }

    @Override
    public Authentication getAuth() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null) {
            return context.getAuthentication();
        }
        return null;
    }

    @Override
    public FormatType getFormatType() {
        return this.formatType;
    }

    @Override
    public FormatterWidth getEmbeddingWidth() {
        return this.width;
    }

    @Override
    public Element getMetadataElement() {
        return this.jdomMetadata;
    }

    @Override
    public synchronized Map<String, Collection<String>> getIndexInfo() throws Exception {
        if (this.indexInfo == null) {
            final SearchManager searchManager = getBean(SearchManager.class);

            try (IndexAndTaxonomy newIndexReader = searchManager.getNewIndexReader(getLang3())) {
                TopFieldCollector collector = TopFieldCollector.create(Sort.RELEVANCE, 1, true, false, false, false);
                IndexSearcher searcher = new IndexSearcher(newIndexReader.indexReader);
                Query query = new TermQuery(new Term("_id", String.valueOf(getMetadataId())));
                searcher.search(query, collector);
                ScoreDoc[] topDocs = collector.topDocs().scoreDocs;

                Multimap<String, String> fields = HashMultimap.create();
                for (ScoreDoc scoreDoc : topDocs) {
                    Document doc = searcher.doc(scoreDoc.doc);
                    for (IndexableField field : doc) {
                        fields.put(field.name(), field.stringValue());
                    }
                }
                this.indexInfo = fields;
            }

        }
        return Collections.unmodifiableMap(this.indexInfo.asMap());
    }

    @Override public ServiceContext getContext() {
        return this.serviceContext;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return this.serviceContext.getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        return this.serviceContext.getBean(name, clazz);
    }

    @Override
    public MapConfig getMapConfiguration() {
        final SettingManager settingManager = this.serviceContext.getBean(SettingManager.class);
        final String background = settingManager.getValue(Settings.REGION_GETMAP_BACKGROUND);
        final String mapproj = settingManager.getValue(Settings.REGION_GETMAP_MAPPROJ);
        final Integer width = settingManager.getValueAsInt(Settings.REGION_GETMAP_WIDTH);
        final Integer thumbnailWidth = settingManager.getValueAsInt(Settings.REGION_GETMAP_SUMMARY_WIDTH);
        return new MapConfig(background, mapproj, width, thumbnailWidth);
    }

    @Override
    public boolean canEdit() throws Exception {
        final AccessManager bean = serviceContext.getBean(AccessManager.class);
        return bean.isOwner(serviceContext, this.metadataInfo.getSourceInfo())
            || bean.hasEditPermission(serviceContext, String.valueOf(this.metadataInfo.getId()));
    }

    @Override
    public Optional<String> getHeader(String name) {
        return Optional.fromNullable(webRequest.getHeader(name));
    }

    public Collection<String> getHeaders(final String name) {
        return Arrays.asList(webRequest.getHeaderValues(name));
    }
}
