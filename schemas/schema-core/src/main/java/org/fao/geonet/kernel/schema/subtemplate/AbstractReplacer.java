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

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.elasticsearch.action.search.SearchResponse;
import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.schema.subtemplate.Status.Failure;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

public abstract class AbstractReplacer implements Replacer{

    public static final float MIN_FIT_TO_REPLACE = 1f;

    protected List<Namespace> namespaces;
    protected ManagersProxy managersProxy;
    protected ConstantsProxy constantsProxy;

    public AbstractReplacer(List<Namespace> namespaces,
                            ManagersProxy managersProxy,
                            ConstantsProxy constantsProxy) {
        this.constantsProxy = constantsProxy;
        this.managersProxy = managersProxy;
        this.namespaces = namespaces;
    }

    @Override
    public Status replaceAll(Element dataXml,
                             String localXlinkUrlPrefix,
                             EsRestClient esRestClient,
                             String defaultIndex,
                             String localisedCharacterStringLanguageCode,
                             String lang,
                             List<String> localesAsHrefParam) {
        List<?> nodes;
        try {
            nodes = Xml.selectNodes(dataXml, getElemXPath(), namespaces);
        } catch (JDOMException e) {
            return new Failure(String.format("%s- selectNodes JDOMEx: %s", getAlias(), getElemXPath()));
        }
        return nodes.stream()
                .map((element) -> replace((Element) element,
                        localXlinkUrlPrefix, esRestClient, defaultIndex,
                        localisedCharacterStringLanguageCode, localesAsHrefParam))
                .collect(Status.STATUS_COLLECTOR);
    }

    protected Status replace(Element element,
                             String localXlinkUrlPrefix,
                             EsRestClient esRestClient,
                             String defaultIndex,
                             String localisedCharacterStringLanguageCode,
                             List<String> localesAsHrefParam) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("+isTemplate:s");
            stringBuilder.append(" +valid:1");
            int extraClauseAdded = queryAddExtraClauses(stringBuilder, element, localisedCharacterStringLanguageCode);

            SearchResponse response = esRestClient.query(defaultIndex, stringBuilder.toString(), null, Collections.emptySet(), 0, 10000);

            if (response.getHits().getMaxScore() >= getMinFitToReplace(extraClauseAdded)) {
                String uuid = response.getHits().getAt(0).getId();

                StringJoiner params = new StringJoiner("&", "?", "");
                xlinkAddExtraParams(element, params);
                params.add("lang=" + String.join(",", localesAsHrefParam));

                StringBuffer href = new StringBuffer(localXlinkUrlPrefix);
                href.append(uuid);
                if (params.length() > 1) {
                    href.append(params);
                }

                element.removeContent();
                element.setAttribute("uuidref", uuid);
                element.setAttribute("href", href.toString(), constantsProxy.getNAMESPACE_XLINK());
                return new Status();
            }
            return new Failure(String.format("%s-found no match for query: %s", getAlias(), stringBuilder));
        } catch (Exception e) {
            return new Failure(String.format("%s-exception %s: %s", getAlias(), e, stringBuilder));
        }
    }

    public abstract String getAlias();

    protected abstract String getElemXPath();

    protected abstract int queryAddExtraClauses(StringBuilder stringBuilder, Element element, String lang) throws Exception;

    protected void xlinkAddExtraParams(Element element, StringJoiner params) throws JDOMException {
    }

    protected String getFieldValue(Element elem, String path, String localisedCharacterStringLanguageCode) throws JDOMException {
        String value = Xml.selectString(elem, String.format("%s/gco:CharacterString", path), namespaces);
        if (value.length() > 0) {
            return value;
        }
        return Xml.selectString(elem, String.format("%s/gmd:PT_FreeText/gmd:textGroup/gmd:LocalisedCharacterString[@locale='%s']", path, localisedCharacterStringLanguageCode), namespaces);
    }

    private float getMinFitToReplace(int nbExtraClause) {
        return MIN_FIT_TO_REPLACE + (Integer.max(nbExtraClause - 1, 0)) * 0.3f;
    }
}
