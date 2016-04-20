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

package org.fao.geonet.kernel.csw.services.getrecords;

import jeeves.server.context.ServiceContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.StreamingResponseCallback;
import org.apache.solr.common.SolrDocument;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.services.getrecords.solr.CswFilter2Solr;
import org.fao.geonet.kernel.search.SearchManagerUtils;
import org.fao.geonet.kernel.search.SolrAuth;
import org.fao.geonet.kernel.search.SolrSearchManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.opengis.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

public class SolrSearchController implements ISearchController {
    @Autowired
    private SolrSearchManager searchManager;

    @Autowired
    private FieldMapper fieldMapper;

    @Override
    public Pair<Element, Element> search(ServiceContext context, int startPos, int maxRecords,
                                         ResultType resultType, String outSchema, ElementSetName setName,
                                         Element filterExpr, String filterVersion, Element request,
                                         Set<String> elemNames, String typeName, int maxHitsFromSummary,
                                         String cswServiceSpecificContraint, String strategy) throws CatalogException {

        final SolrClient client = searchManager.getClient();
        final SolrQuery params = new SolrQuery("*:*");
        addFilter(params, convertCswFilter(filterExpr, filterVersion));
        addFilter(params, cswServiceSpecificContraint);
        try {
            addFilter(params, SolrAuth.getPermissions());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        params.setStart(startPos - 1);
        params.setRows(maxRecords);
        params.setFields(SolrSearchManager.ID);
        try {

            final MyStreamingResponseCallback callback = new MyStreamingResponseCallback(context, outSchema, setName, resultType, elemNames, typeName, strategy);
            client.queryAndStreamResponse(params, callback);
            final Element results = callback.results;
            results.setAttribute("numberOfRecordsMatched", Long.toString(callback.numMatches));
            results.setAttribute("numberOfRecordsReturned", Long.toString(callback.counter));
            results.setAttribute("elementSet", setName.toString());

            if (callback.numMatches > callback.counter) {
                results.setAttribute("nextRecord", Long.toString(callback.counter + startPos));
            } else {
                results.setAttribute("nextRecord", "0");
            }
            return Pair.read(null, results);
        } catch (SolrServerException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFilter(SolrQuery query, String filter) {
        if (filter == null || filter.isEmpty()) {
            return;
        }
        query.addFilterQuery(filter);
    }

    private Filter parseFilter(Element xml, String filterVersion) {
        final Parser parser = SearchManagerUtils.createFilterParser(filterVersion);
        parser.setValidating(true);
        parser.setFailOnValidationError(true);
        String string = Xml.getString(xml);
        try {
            final Object parseResult = parser.parse(new StringReader(string));
            if (parseResult instanceof Filter) {
                return (Filter) parseResult;
            } else {
                return null;
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Log.error(Geonet.CSW_SEARCH, "Errors occurred when trying to parse a filter", e);
            return null;
        }
    }

    private String convertCswFilter(Element xml, String filterVersion) {
        String result = CswFilter2Solr.translate(parseFilter(xml, filterVersion), fieldMapper);
        if (result != null && !result.contains("_isTemplate:")) {
            result += " AND (_isTemplate:n)";
        }
        return result;
    }

    private static class MyStreamingResponseCallback extends StreamingResponseCallback {
        private final Element results = new Element("SearchResults", Csw.NAMESPACE_CSW);
        private final ServiceContext context;
        private final String outSchema;
        private final ElementSetName setName;
        private final ResultType resultType;
        private final Set<String> elemNames;
        private final String typeName;
        private final String strategy;
        private final MetadataRepository metadataRepository;
        private final SchemaManager scm;
        private long numMatches = 0;
        private long counter = 0;

        public MyStreamingResponseCallback(ServiceContext context, String outSchema, ElementSetName setName, ResultType resultType, Set<String> elemNames, String typeName, String strategy) {
            this.context = context;
            this.outSchema = outSchema;
            this.setName = setName;
            this.resultType = resultType;
            this.elemNames = elemNames;
            this.typeName = typeName;
            this.strategy = strategy;
            metadataRepository = context.getBean(MetadataRepository.class);
            scm = context.getBean(SchemaManager.class);
        }

        @Override
        public void streamSolrDocument(SolrDocument doc) {
            final Element searchResult;
            try {
                searchResult = createSearchResult(doc);
            } catch (IOException | JDOMException | InvalidParameterValueEx e) {
                throw new RuntimeException(e);
            }
            if (searchResult != null) {
                results.addContent(searchResult);
                counter++;
            }
        }

        @Override
        public void streamDocListInfo(long numFound, long start, Float maxScore) {
            this.numMatches = numFound;
        }

        private Element createSearchResult(SolrDocument doc) throws IOException, JDOMException, InvalidParameterValueEx {
            final int id = Integer.valueOf(doc.getFieldValue(SolrSearchManager.ID).toString());
            final Metadata metadata = getMetaData(id);
            if (metadata == null) {
                return null;
            }
            final String schema = metadata.getDataInfo().getSchemaId();
            String displayLanguage = context.getLanguage();
            Element result = SearchController.applyElementSetName(context, scm, schema,
                metadata.getXmlData(false), outSchema, setName, resultType, Integer.toString(id), displayLanguage);
            return SearchController.applyElementNames(context, elemNames, typeName, scm, schema, result, resultType, null, strategy);
        }

        private Metadata getMetaData(int id) {
            Metadata md = metadataRepository.findOne(id);
            if (md == null) {
                return null;
            }
            return md;
        }
    }
}
