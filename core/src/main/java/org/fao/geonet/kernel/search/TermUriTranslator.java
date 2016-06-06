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

import java.util.Map;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.LabelNotFoundException;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.utils.Log;

public class TermUriTranslator implements Translator {

    private static final long serialVersionUID = 1L;
    private final transient ThesaurusFinder finder;
    private final String conceptSchemeUri;
    private final String langCode;
    private transient Thesaurus thesaurus = null;
    private boolean firstWarning = true; // used to display verbose info only once

    public TermUriTranslator(ThesaurusFinder finder, String langCode, String conceptSchemeUri) {
        this.finder = finder;
        this.langCode = langCode;
        this.conceptSchemeUri = conceptSchemeUri;
        setThesaurus();
    }

    private void setThesaurus() {
        this.thesaurus = finder.getThesaurusByConceptScheme(conceptSchemeUri);
        if (thesaurus == null) {
            Log.warning(Geonet.THESAURUS, "No thesaurus found for concept scheme " + conceptSchemeUri + " (lang=" + langCode + ")");
            if (Log.isDebugEnabled(Geonet.THESAURUS) && firstWarning) {
                firstWarning = false;

                StringBuilder sb = new StringBuilder();
                sb.append("Available thesauri: [");
                for (Map.Entry<String, Thesaurus> entrySet : finder.getThesauriMap().entrySet()) {
                    sb.append("{");
                    sb.append(entrySet.getKey()).append(" -> ").append(entrySet.getValue().getConceptSchemes());
                    sb.append("}");
                }
                sb.append("]");
                Log.debug(Geonet.THESAURUS, sb.toString());
            }
        }
    }

    @Override
    public String translate(String key) {
        KeywordBean keyword;

        if (thesaurus == null) {
            // try to retrieve a theasurus (it may not have been loaded yet)
            setThesaurus();
        }

        if (thesaurus == null) {
            // a warning has already been logged by setThesaurus
            if (Log.isTraceEnabled(Geonet.THESAURUS)) {
                Log.trace(Geonet.THESAURUS, "Thesaurus not available [uri:" + conceptSchemeUri + ", lang:" + langCode + ", " + key + "]");
            }
            return key;
        }

        try {
            keyword = thesaurus.getKeyword(key, langCode);
        } catch (TermNotFoundException e) {
            Log.error(Geonet.THESAURUS, "Term not found: " + key);
            return key;
        } catch (NullPointerException e) {
            Log.error(Geonet.THESAURUS, "NPE searching term: " + key);
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
