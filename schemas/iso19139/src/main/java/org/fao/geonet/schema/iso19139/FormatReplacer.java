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

package org.fao.geonet.schema.iso19139;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.fao.geonet.kernel.schema.subtemplate.AbstractReplacer;
import org.fao.geonet.kernel.schema.subtemplate.ConstantsProxy;
import org.fao.geonet.kernel.schema.subtemplate.ManagersProxy;
import org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.util.List;

public class FormatReplacer extends AbstractReplacer {

    private QueryParser tokenizedIndexQueryParser = new QueryParser(Version.LUCENE_4_9, null, new StandardAnalyzer(Version.LUCENE_4_9));

    public FormatReplacer(List<Namespace> namespaces,
                          ManagersProxy managersProxy,
                          ConstantsProxy constantsProxy) {
        super(namespaces, managersProxy, constantsProxy);
    }

    @Override
    public String getAlias() {
        return SubtemplatesByLocalXLinksReplacer.FORMAT;
    }

    @Override
    protected String getElemXPath() {
        return ".//gmd:MD_Format/parent::*";
    }

    @Override
    protected Query queryAddExtraClauses(BooleanQuery query, Element format, String lang) throws JDOMException {
        String name = getFieldValue(format, ".//gmd:name", lang);
        String version = getFieldValue(format, ".//gmd:version", lang);

        query.add(tokenizedIndexQueryParser.createPhraseQuery(constantsProxy.getIndexFieldNamesANY(), name), BooleanClause.Occur.MUST);
        query.add(tokenizedIndexQueryParser.createPhraseQuery(constantsProxy.getIndexFieldNamesANY(), version), BooleanClause.Occur.MUST);

        return query;
    }

    @Override
    protected StringBuffer xlinkAddExtraParams(Element format, StringBuffer params) { return params; }
}
