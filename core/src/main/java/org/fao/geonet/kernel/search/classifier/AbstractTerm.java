//=============================================================================
//===    Copyright (C) 2010 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.classifier;

import static org.fao.geonet.kernel.search.facet.CategoryHelper.addSubCategory;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;

public abstract class AbstractTerm implements Classifier {

    private ThesaurusFinder finder;

    private String conceptScheme;

    private String languageToIndex;

    protected AbstractTerm(ThesaurusFinder finder, String conceptScheme) {
        this.finder = finder;
        this.conceptScheme = conceptScheme;
    }

    protected void setLanguageToIndex(String language) {
        this.languageToIndex = language;
    }

    protected List<CategoryPath> classifyUri(String uri) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        Thesaurus thesaurus = getThesaurus();

        if (thesaurus.hasKeyword(uri)) {
            KeywordBean term = thesaurus.getKeyword(uri, getLanguages());
            result.addAll(classify(term));
        }

        return result;
    }

    protected Thesaurus getThesaurus() {
        return finder.getThesaurusByConceptScheme(conceptScheme);
    }

    private List<CategoryPath> classify(KeywordBean term) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();
    
        Thesaurus thesaurus = getThesaurus();

        if (thesaurus.hasBroader(term.getUriCode())) {
            result.addAll(classifyTermWithBroaderTerms(term));
        } else {
            result.add(classifyTermWithNoBroaderTerms(term));
        }
    
        return result;
    }

    private List<CategoryPath> classifyTermWithBroaderTerms(KeywordBean term) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        for (CategoryPath categoryPathToBroaderTerm: classifyBroaderTerms(term)) {
            CategoryPath categoryPathToTerm = addSubCategory(
                categoryPathToBroaderTerm,
                getCategoryToIndex(term)
            );
    
            result.add(categoryPathToTerm);
        }
    
        return result;
    }

    private List<CategoryPath> classifyBroaderTerms(KeywordBean term) {
        List<CategoryPath> result = new ArrayList<CategoryPath>();

        Thesaurus thesaurus = getThesaurus();

        for (KeywordBean broaderTerm: thesaurus.getBroader(term.getUriCode(), getLanguages())) {
            result.addAll(classify(broaderTerm));
        }

        return result;
    }

    private CategoryPath classifyTermWithNoBroaderTerms(KeywordBean term) {
        return new CategoryPath(getCategoryToIndex(term));
    }

    private String getCategoryToIndex(KeywordBean term) {
        if (indexUri()) {
            return term.getUriCode();
        } else {
            return term.getPreferredLabel(languageToIndex);
        }
    }

    private boolean indexUri() {
        return languageToIndex == null || languageToIndex.isEmpty();
    }

    private String[] getLanguages() {
        if (indexUri()) {
            return new String[]{};
        } else {
            return new String[]{languageToIndex};
        }
    }
}
