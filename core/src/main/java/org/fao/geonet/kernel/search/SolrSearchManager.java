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

package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.solr.SolrConfig;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SolrSearchManager implements ISearchManager {
    public static final String ID = "id";
    public static final String DOC_TYPE = "docType";

    @Autowired
    private SolrConfig config;

    private SolrClient client;

    @Override
    public void init(ServiceConfig handlerConfig) throws Exception {
        client = config.createClient();
    }

    @Override
    public void end() throws Exception {
        client.close();
    }

    @Override
    public MetaSearcher newSearcher(SearcherType type, String stylesheetName) throws Exception {
        return null;
    }

    @Override
    public void index(Path schemaDir, Element metadata, String id, List<Element> moreFields, MetadataType metadataType, String root, boolean forceRefreshReaders) throws Exception {
        final SolrInputDocument doc = new SolrInputDocument();
        doc.addField(ID, id);
        doc.addField(DOC_TYPE, "metadata");
        addMDFields(doc, schemaDir, metadata, root);
        addMoreFields(doc, moreFields);
        client.add(doc);
        client.commit();
    }

    private static void addMDFields(SolrInputDocument doc, Path schemaDir, Element metadata, String root) {
        final Path styleSheet = SearchManagerUtils.getIndexFieldsXsl(schemaDir, root, "solr-");
        try {
            Element fields = Xml.transform(metadata, styleSheet);
            /* Generates something like that:
            <doc>
              <field name="toto">Contenu</field>
            </doc>*/
            for (Element field: (List<Element>)fields.getChildren("field")) {
                doc.addField(field.getAttributeValue("name"), field.getValue());
            }
        } catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE,
                    String.format("Indexing stylesheet contains errors: %s \n\t Marking the metadata as _indexingError=1 in index",
                            e.getMessage()));
            doc.addField(SearchManagerUtils.INDEXING_ERROR_FIELD, "1");
            doc.addField(SearchManagerUtils.INDEXING_ERROR_MSG, "GNIDX-XSL||" + e.getMessage());
            StringBuilder sb = new StringBuilder();
            SearchManagerUtils.allText(metadata, sb);
            doc.addField(Geonet.IndexFieldNames.ANY, sb.toString());
        }
    }

    private static void addMoreFields(SolrInputDocument doc, List<Element> fields) {
        for (Element field: fields) {
            doc.addField(field.getAttributeValue(SearchManager.LuceneFieldAttribute.NAME.toString()),
                    field.getAttributeValue(SearchManager.LuceneFieldAttribute.STRING.toString()));
        }
    }

    @Override
    public IndexAndTaxonomy getNewIndexReader(String preferredLang) throws IOException, InterruptedException {
        return null;
    }

    @Override
    public IndexAndTaxonomy getIndexReader(String preferredLang, long versionToken) throws IOException {
        return null;
    }

    @Override
    public void forceIndexChanges() throws IOException {

    }

    @Override
    public void releaseIndexReader(IndexAndTaxonomy reader) throws InterruptedException, IOException {

    }

    @Override
    public ISpatial getSpatial() {
        return null;
    }

    @Override
    public boolean rebuildIndex(ServiceContext context, boolean xlinks, boolean reset, boolean fromSelection) throws Exception {
        DataManager dataMan = context.getBean(DataManager.class);
        try {
            if (reset) {
                clearIndex();
            }
            if (fromSelection) {
                dataMan.rebuildIndexForSelection(context, xlinks);
            } else if (xlinks) {
                dataMan.rebuildIndexXLinkedMetadata(context);
            } else {
                clearIndex();
                dataMan.init(context, true);
            }
            client.commit();
            return true;
        }
        catch (Exception e) {
            Log.error(Geonet.INDEX_ENGINE, "Exception while rebuilding solr index, going to rebuild it: " +
                    e.getMessage(), e);
            return false;
        }
    }

    private void clearIndex() throws SolrServerException, IOException {
        client.deleteByQuery(DOC_TYPE + ":metadata");
    }

    @Override
    public Map<String, String> getDocsChangeDate() throws Exception {
        return new HashMap<>();
    }

    @Override
    public ISODate getDocChangeDate(String mdId) throws Exception {
        return null;
    }

    @Override
    public Set<Integer> getDocsWithXLinks() throws Exception {
        return null;
    }

    @Override
    public void delete(String fld, String txt) throws Exception {

    }

    @Override
    public void delete(String fld, List<String> txts) throws Exception {

    }

    @Override
    public void deleteGroup(String fld, String txt) throws Exception {

    }

    @Override
    public void rescheduleOptimizer(Calendar optimizerBeginAt, int optimizerInterval) throws Exception {

    }

    @Override
    public void disableOptimizer() throws Exception {

    }

    /**
     * Only for UTs
     */
    void setClient(SolrClient client) {
        this.client = client;
    }
}
