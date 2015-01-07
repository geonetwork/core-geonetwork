package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jeeves.server.context.ServiceContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.jdom.Element;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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
    private final Metadata metadataInfo;
    private final String locUrl;
    private final Element jdomMetadata;
    private final ServiceContext serviceContext;
    private Multimap<String, String> indexInfo = null;

    public EnvironmentImpl(FormatterParams fparams, IsoLanguagesMapper mapper) {
        jdomMetadata = fparams.metadata;
        this.lang3 = fparams.context.getLanguage();
        this.lang2 = mapper.iso639_2_to_iso639_1(lang3, "en");

        this.formatType = fparams.formatType;
        this.resourceUrl = fparams.getResourceUrl();
        this.locUrl = fparams.getLocUrl();
        this.metadataInfo = fparams.metadataInfo;
        for (Map.Entry<String, String[]> entry : fparams.params.entrySet()) {
            for (String value : entry.getValue()) {
                this.params.put(entry.getKey(), new ParamValue(value));
            }
        }

        this.serviceContext = fparams.context;
    }

    /**
     * Return the map of all parameters passed to the Format service.
     */
    public Multimap<String, ParamValue> params() {
        return this.params;
    }

    /**
     * Return the value of the first parameter with the provided name.  Null is returned if there is no parameter with the given name.
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

    @Override
    public <T> T getBean(Class<T> clazz) {
        return this.serviceContext.getBean(clazz);
    }

    @Override
    public MapConfig getMapConfiguration() {
        final SettingManager settingManager = this.serviceContext.getBean(SettingManager.class);
        final String background = settingManager.getValue("region/getmap/background");
        final String mapproj = settingManager.getValue("region/getmap/mapproj");
        final Integer width = settingManager.getValueAsInt("region/getmap/width");
        final Integer thumbnailWidth = settingManager.getValueAsInt("region/getmap/summaryWidth");
        return new MapConfig(background, mapproj, width, thumbnailWidth);
    }

    @Override
    public boolean canEdit() throws Exception {
        final AccessManager bean = serviceContext.getBean(AccessManager.class);
        return bean.isOwner(serviceContext, this.metadataInfo.getSourceInfo())
               || bean.hasEditPermission(serviceContext, String.valueOf(this.metadataInfo.getId()));
    }
}
