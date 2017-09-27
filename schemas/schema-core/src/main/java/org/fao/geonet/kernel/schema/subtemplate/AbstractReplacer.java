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

package org.fao.geonet.kernel.schema.subtemplate;

import org.apache.lucene.analysis.standard.UAX29URLEmailAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.fao.geonet.kernel.schema.subtemplate.Status.Failure;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.util.List;

public abstract class AbstractReplacer {

    protected List<Namespace> namespaces;
    protected SchemaManagerProxy schemaManagerProxy;
    protected ConstantsProxy constantsProxy;

    public AbstractReplacer(List<Namespace> namespaces,
                            SchemaManagerProxy schemaManagerProxy,
                            ConstantsProxy constantsProxy) {
        this.constantsProxy = constantsProxy;
        this.schemaManagerProxy = schemaManagerProxy;
        this.namespaces = namespaces;
    }

    public Status replaceAll(Element dataXml, String localXlinkUrlPrefix, IndexReader indexReader) {
        List<?> nodes = null;
        try {
            nodes = Xml.selectNodes(dataXml, getElemXPath(), namespaces);
        } catch (JDOMException e) {
            return new Failure(String.format("%s- selectNodes JDOMEx: %s", getAlias(), getElemXPath()));
        }
        return nodes.stream()
                .map((element) -> replace((Element) element, localXlinkUrlPrefix, indexReader))
                .collect(Status.STATUS_COLLECTOR);
    }

    protected Status replace(Element element, String localXlinkUrlPrefix, IndexReader indexReader) {
        BooleanQuery query = new BooleanQuery();
        try {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            query.add(new TermQuery(new Term(constantsProxy.getIndexFieldNamesIS_TEMPLATE(), "s")), BooleanClause.Occur.MUST);
            TopDocs docs = searcher.search(queryAddExtraClauses(query, element), 1000);

            if (docs.totalHits == 1) {
                Document document = indexReader.document(docs.scoreDocs[0].doc);
                String uuid = document.getField("_uuid").stringValue();
                element.removeContent();

                StringBuffer params = new StringBuffer(localXlinkUrlPrefix);
                params.append(uuid);
                params = xlinkAddExtraParams(element, params);

                element.setAttribute("uuidref", uuid);
                element.setAttribute("href",
                        params.toString(),
                        constantsProxy.getNAMESPACE_XLINK());
                return new Status();
            }
            if (docs.totalHits > 1) {
                return new Failure(String.format("%s-found too many matches for query: %s", getAlias(), query.toString()));
            }
            return new Failure(String.format("%s-found no match for query: %s", getAlias(), query.toString()));
        } catch (Exception e) {
            return new Failure(String.format("%s-exception %s: %s", getAlias(), e.toString(), query.toString()));
        }
    }

    protected Query createSubQuery(String indexFieldNames, String value) {
        return new TermQuery(new Term(indexFieldNames, value));
    }

    public abstract String getAlias();

    protected abstract String getElemXPath();

    protected abstract Query queryAddExtraClauses(BooleanQuery query, Element element) throws Exception;

    protected abstract StringBuffer xlinkAddExtraParams(Element element, StringBuffer params) throws JDOMException;
}
