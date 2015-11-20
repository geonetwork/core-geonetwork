package org.fao.geonet.kernel.search;

import org.fao.geonet.kernel.*;
import java.util.Collections;
import java.util.Map;

public class NullableThesaurusFinder implements ThesaurusFinder {


    private Thesaurus thesaurus;

    public NullableThesaurusFinder() {
    }

    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public boolean existsThesaurus(String name) {
        if (thesaurus == null) {
            return false;
        } else {
            return thesaurus.getKey().equals(name);
        }
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

        if (thesaurus != null && thesaurus.hasConceptScheme(conceptSchemeUri)) {
            return thesaurus;
        }
        return null;
    }

    @Override
    public Map<String, Thesaurus> getThesauriMap() {
        if (thesaurus != null) {
            return Collections.singletonMap(thesaurus.getKey(), thesaurus);
        } else {
            return Collections.EMPTY_MAP;
        }
    }

}
