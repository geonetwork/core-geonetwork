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

package org.fao.geonet.kernel.search.classifier;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;

import java.util.ArrayList;
import java.util.List;

public class TermLabel extends AbstractTerm {
    private String langCode;

    public TermLabel(ThesaurusFinder finder, String conceptScheme, String langCode) {
        super(finder, conceptScheme);
        this.langCode = langCode;
    }

    public void setIndexLabel(boolean indexLabel) {
        setLanguageToIndex(indexLabel ? langCode : null);
    }

    @Override
    public List<CategoryPath> classify(String value) {
        if (hasUriFor(value)) {
            return classifyUri(getUriFor(value));
        } else {
            return new ArrayList<>();
        }
    }

    private boolean hasUriFor(String value) {
        Thesaurus thesaurus = getThesaurus();
        if (thesaurus == null) {
            return false;
        } else {
            return thesaurus.hasKeywordWithLabel(value, langCode);
        }
    }

    private String getUriFor(String value) {
        Thesaurus thesaurus = getThesaurus();
        KeywordBean term = thesaurus.getKeywordWithLabel(value, langCode);
        return term.getUriCode();
    }

}
