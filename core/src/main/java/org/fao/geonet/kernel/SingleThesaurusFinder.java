package org.fao.geonet.kernel;

import java.util.Collections;
import java.util.Map;

public class SingleThesaurusFinder implements ThesaurusFinder {


    private final Thesaurus thesaurus;

    public SingleThesaurusFinder(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean existsThesaurus(String name) {
        return thesaurus.getKey().equals(name);
    }

    @Override
    public Thesaurus getThesaurusByName(String thesaurusName) {
        if(existsThesaurus(thesaurusName)) {
            return thesaurus;
        }
        return null;
    }

    @Override
    public Thesaurus getThesaurusByConceptScheme(String conceptSchemeUri) {
        if (thesaurus.hasConceptScheme(conceptSchemeUri)) {
            return thesaurus;
        }
        return null;
    }

    @Override
    public Map<String, Thesaurus> getThesauriMap() {
        return Collections.singletonMap(thesaurus.getKey(), thesaurus);
    }


}
