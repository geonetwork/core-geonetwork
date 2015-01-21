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
        setLanguageToIndex(indexLabel?langCode:null);
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
        return thesaurus.hasKeywordWithLabel(value, langCode);
    }

    private String getUriFor(String value) {
        Thesaurus thesaurus = getThesaurus();
        KeywordBean term = thesaurus.getKeywordWithLabel(value, langCode);
        return term.getUriCode();
    }

}
