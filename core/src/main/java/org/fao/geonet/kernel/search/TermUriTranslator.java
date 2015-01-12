//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.search;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.LabelNotFoundException;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.utils.Log;

public class TermUriTranslator implements Translator {

    private static final long serialVersionUID = 1L;

    private transient Thesaurus thesaurus;

    private String langCode;

    public TermUriTranslator(ThesaurusFinder finder, String langCode, String conceptSchemeUri) {
        this.thesaurus = finder.getThesaurusByConceptScheme(conceptSchemeUri);
        this.langCode = langCode;
    }

    @Override
    public String translate(String key) {
        KeywordBean keyword;

        try {
            keyword = thesaurus.getKeyword(key, langCode);
        } catch (TermNotFoundException e) {
            Log.error(Geonet.THESAURUS, "Term not found: " + key);
            return key;
        }

        String label;
        
        try {
            label = keyword.getPreferredLabel(langCode);
        } catch (LabelNotFoundException e) {
            return keyword.getUriCode();
        }

        return label;
    }

}
