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

import jeeves.JeevesCacheManager;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class TranslatorFactory {

    public static final Translator IDENTITY_TRANSLATOR = new Translator() {
        private static final long serialVersionUID = 1L;

        public String translate(String key) {
            return key;
        }
    };

    public Translator getTranslator(String translatorString, final String langCode) {
        try {
            return getTranslatorUnhandled(translatorString, langCode);
        } catch (Exception e) {
            Log.error(
                Geonet.SEARCH_ENGINE,
                "Error creating translator " + translatorString + " (" + langCode + ")",
                e
            );
            return IDENTITY_TRANSLATOR;
        }
    }

    private Translator getTranslatorUnhandled(String translatorString, final String langCode)
        throws Exception {
        if (translatorString == null || translatorString.length() == 0) {
            return IDENTITY_TRANSLATOR;
        }
        String key = translatorString + langCode;

        String[] parts = translatorString.split(":", 2);
        if (parts.length != 2) {
            throw new AssertionError(
                String.format(
                    "Check the translator configuration '%s' of the config-summary.xml. It must be of the form nameOfTranslator:TranslatorParam.",
                    translatorString));
        }
        String type = parts[0];
        final String param = parts[1];

        final ConfigurableApplicationContext context = ApplicationContextHolder.get();
        Translator translator;
        if (TranslatorTypes.CODELIST.toString().equals(type)) {
            translator = new CodeListTranslator(context.getBean(SchemaManager.class), langCode, param);
        } else if (TranslatorTypes.APPLOC.toString().equals(type)) {
            translator = new JSONLocTranslator(context, langCode, param);
        } else if (TranslatorTypes.TERM.toString().equals(type)) {
            translator = JeevesCacheManager.findInEternalCache(key, new Callable<Translator>() {
                public Translator call() {
                    try {
                        Translator termUriTranslator = new TermUriTranslator(context.getBean(ThesaurusManager.class), langCode, param);
                        return new TranslatorCachingWrapper(termUriTranslator);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else if (TranslatorTypes.DB.toString().equals(type)) {
            translator = JeevesCacheManager.findInTenSecondCache(key, new Callable<Translator>() {
                public Translator call() {
                    try {
                        return new DbDescTranslator(context, langCode, param);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            throw new AssertionError(
                String.format("'%s' is not a recognized type of translator. Supported types are: %s",
                    type, Arrays.asList(TranslatorTypes.values())));
        }

        return translator;
    }

    ;

    private static enum TranslatorTypes {
        CODELIST("codelist"),
        APPLOC("apploc"),
        TERM("term"),
        DB("db");

        public final String name;

        private TranslatorTypes(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
