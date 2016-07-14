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
     * @return true if a thesaurus with the given name exists
     */
    boolean existsThesaurus(String name);

    /**
     * Find a thesaurus by the thesaurus's name.  The {@linkplain #getThesauriMap()} keys can be
     * used to find the names of the thesauri
     *
     * @param thesaurusName the name of the thesaurus to look up.
     * @return the thesaurus identified by the name or null.
     */
    Thesaurus getThesaurusByName(String thesaurusName);

    /**
     * Find a thesaurus containing a concept scheme.
     *
     * @param conceptSchemeUri the concept scheme to find.
     * @return the thesaurus containing the concept scheme or null.
     */
    Thesaurus getThesaurusByConceptScheme(String conceptSchemeUri);

    /**
     * Return a read-only mapping of the thesaurus name/id to Thesauri
     *
     * @return a read-only mapping of the thesaurus name/id to Thesauri
     */
    Map<String, Thesaurus> getThesauriMap();
}
