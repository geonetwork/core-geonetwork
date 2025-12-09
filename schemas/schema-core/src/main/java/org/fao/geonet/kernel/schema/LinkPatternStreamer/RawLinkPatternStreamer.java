/*
 * Copyright (C) 2001-2019 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.schema.LinkPatternStreamer;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RawLinkPatternStreamer <L, M> {
    private static final Pattern SEARCH_URL_IN_STRING_REGEX = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?", Pattern.CASE_INSENSITIVE);

    private Pattern pattern;
    private Pattern excludePattern;
    private ILinkBuilder<L, M> linkBuilder;
    private List<Namespace> namespaces;
    private String rawTextXPath;

    public RawLinkPatternStreamer(ILinkBuilder linkBuilder, String excludePattern)
    {
        this.pattern = SEARCH_URL_IN_STRING_REGEX;
        this.linkBuilder = linkBuilder;
        if (!ObjectUtils.isEmpty(excludePattern)) {
            this.excludePattern = Pattern.compile(excludePattern);
        }
    }

    public void setRawTextXPath(String rawTextXPath) {
        this.rawTextXPath = rawTextXPath;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        this.namespaces = namespaces;
    }

    public void processAllRawText(Element metadata, M ref) throws JDOMException {
        List<Element> encounteredLinks = (List<Element>) Xml.selectNodes(metadata, rawTextXPath, namespaces);
        encounteredLinks.stream().forEach(rawTextElem -> processOneRawText(rawTextElem, ref));
    }

    private void processOneRawText(Element rawTextElem, M ref) {
        for (Matcher m = this.pattern.matcher(rawTextElem.getValue()); m.find(); ) {
            String url = m.toMatchResult().group();
            if (this.excludePattern == null
                || !this.excludePattern.matcher(url).find()) {
                L link = linkBuilder.found(url);
                linkBuilder.persist(link, ref);
            }
        }
    }
}

