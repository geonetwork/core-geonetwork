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
        if (existsThesaurus(thesaurusName)) {
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
