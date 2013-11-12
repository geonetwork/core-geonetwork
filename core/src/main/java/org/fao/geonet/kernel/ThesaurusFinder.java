package org.fao.geonet.kernel;

import java.util.Map;


/**
 * Provides access for looking up thesauri.
 * 
 * @author jeichar
 */
public interface ThesaurusFinder {

    
    /**
     * Check if a thesaus with the provided name exists
     * 
     * @param name the name of the thesaurus.
     * 
     * @return true if a thesaurus with the given name exists
     */
    boolean existsThesaurus(String name);

    /**
     * Find a thesaurus by the thesaurus's name.  The {@linkplain #getThesauriMap()} keys 
     * can be used to find the names of the thesauri
     * 
     * @param thesaurusName the name of the thesaurus to look up.
     * 
     * @return the thesaurus identified by the name or null.
     */
    Thesaurus getThesaurusByName(String thesaurusName);

    /**
     * Return a read-only mapping of the thesaurus name/id to Thesauri
     * @return a read-only mapping of the thesaurus name/id to Thesauri
     */
    Map<String, Thesaurus> getThesauriMap();
}
