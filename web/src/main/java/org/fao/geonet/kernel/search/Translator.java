//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

import jeeves.JeevesCacheManager;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;

/**
 * Takes a key and looks up a translation for that key.
 * 
 * @author jesse
 */
public abstract class Translator implements Serializable {

    private static final long serialVersionUID = 1L;
    public static Translator NULL_TRANSLATOR = new Translator() {
        private static final long serialVersionUID = 1L;
        public String translate(String key) {
            return null;
        }
    };

    public abstract String translate(String key);

    public static Translator createTranslator(String translatorString, final ServiceContext context, final String langCode)
            throws Exception {
        if (translatorString == null || translatorString.length() == 0) {
            return Translator.NULL_TRANSLATOR;
        }
        String key = translatorString + langCode;

        String[] parts = translatorString.split(":", 2);
        if (parts.length != 2) {
            throw new AssertionError(
                    "the 'translation' element of the config-summary.xml must be of the form nameOfTranslator:TranslatorParam");
        }
        String type = parts[0];
        final String param = parts[1];

        final GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        Translator translator;
        if (type.equals("codelist")) {
                return new CodeListTranslator(gc.getSchemamanager(), langCode, param);
        } else if (type.equals("db")) {
            translator = JeevesCacheManager.findInTenSecondCache(key, new Callable<Translator>() {
                public Translator call() {
                    try {
                        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
                        return new DbDescTranslator(dbms, langCode, param);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            throw new AssertionError(type + " is not a recognized type of translator");
        }

        return translator;
    }

}
