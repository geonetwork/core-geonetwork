/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.translations;

import org.fao.geonet.ApplicationContextHolder;

import java.util.Optional;

public class TranslationUtil {

    /**
     * Private constructor to avoid instantiate the class.
     */
    private TranslationUtil() {

    }

    public static final String translate(String text, String fromLanguage, String toLanguage) {
        TranslationFactory translationFactory = ApplicationContextHolder.get().getBean(TranslationFactory.class);

        Optional<ITranslationService> translationServiceOptional = translationFactory.getTranslationService();

        if (translationServiceOptional.isPresent()) {
            return translationServiceOptional.get().translate(text, fromLanguage, toLanguage);
        } else {
            return text;
        }

    }
}
