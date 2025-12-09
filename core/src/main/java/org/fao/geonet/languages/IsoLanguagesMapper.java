//===	Copyright (C) 2012 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.languages;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.repository.IsoLanguageRepository;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.neovisionaries.i18n.LanguageCode;

import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

/**
 * TODO javadoc.
 *
 * @author jose garcía
 */
public class IsoLanguagesMapper {
    /*
     * Stores mapping of ISO 639-1 to ISO 639-2 for all languages defined in IsoLanguages table
     */
    protected BiMap<String, String> _isoLanguagesMap639 = HashBiMap.create();

    /**
     * Flag used to check if the init is valid.
     * It may not be valid during initial database creation.
     */
    private boolean validInit = false;

    @Autowired
    private IsoLanguageRepository _langRepo;

    /**
     * TODO javadoc.
     *
     * @return instance
     * @throws Exception hmm
     */
    public IsoLanguagesMapper() {
    }

    /**
     * Helps ensure singleton-ness.
     *
     * @return nothing
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Creates mapping to of ISO 639-1 to ISO 639-2 for all languages defined in IsoLanguages table
     *
     * Note @PostConstruct may not work on initial setup because the database is may not be loaded when the @PostConstruct is called so it may creates an empty array.
     *      So we will use a verifiedLoaded flag to check if it successfully loaded.
     */
    @PostConstruct
    public void init() {
        validInit = false;
        for (IsoLanguage record : _langRepo.findAll()) {
            final String shortCode = toLowerCase(record.getShortCode());
            final String code = toLowerCase(record.getCode());
            if (shortCode != null && code != null) {
                _isoLanguagesMap639.forcePut(shortCode, code);
            } else {
                Log.info(Geonet.GEONETWORK, "Unable to add IsoLanguage mapping for " + record);
            }
        }
    }

    public void reinit() {
        _isoLanguagesMap639 = HashBiMap.create();
        init();
    }


    /**
     * Verify that the init loaded some data, if so then set verifiedLoaded to true so that we don't need to check again.
     * Otherwise re-run the init() to try loading the object again.
     */
    public void checkInit() {
        if (!validInit) {
            // _langRepo will be null if this is not a bean - in which case the @PostConstruct is not expected to be executed so just mark validInit as true and return.
            // This currenly only occurs in the unit tests.
            if (_langRepo == null) {
                validInit=true;
                return;
            }
            // Re-init if the array size is 0
            if (_isoLanguagesMap639.size() == 0) {
               init();
            }
            // If there is data then mark it as valid.
            if (_isoLanguagesMap639.size() > 0) {
                validInit = true;
            }
        }
    }

    private String toLowerCase(String code) {
        if (code == null) {
            return null;
        }
        return code.toLowerCase();
    }


    /**
     * Retrieves 639-2 code from a 639-1 code
     */
    public String iso639_1_to_iso639_2(String iso639_1) {
        checkInit();
        if (_isoLanguagesMap639.containsValue(iso639_1.toLowerCase())) {
            return iso639_1.toLowerCase();
        } else {
            return _isoLanguagesMap639.get(iso639_1.toLowerCase());
        }
    }

    /**
     * Retrieves 639-1 code from a 639-2 code
     */
    public String iso639_2_to_iso639_1(String iso639_2) {
        checkInit();
        if (_isoLanguagesMap639.containsKey(iso639_2.toLowerCase())) {
            return iso639_2.toLowerCase();
        } else {
            return _isoLanguagesMap639.inverse().get(iso639_2.toLowerCase());
        }
    }

    /**
     * Convert the code to iso639_2 or return the default
     */
    public String iso639_1_to_iso639_2(String iso639_1, String defaultLang) {
        String result = iso639_1_to_iso639_2(iso639_1);
        if (result == null) {
            return defaultLang.toLowerCase();
        } else {
            return result;
        }
    }

    /**
     * Convert the code to iso639_1 or return the default
     */
    public String iso639_2_to_iso639_1(String iso639_2, String defaultLang) {
        String result = iso639_2_to_iso639_1(iso639_2);
        if (result == null) {
            return defaultLang.toLowerCase();
        } else {
            return result;
        }
    }

    /**
     * Convert the iso639_2B to iso639_2T
     */
    public static String iso639_2B_to_iso639_2T(String iso639_2B) {
        LanguageCode code = LanguageCode.getByCode(iso639_2B);
        if (code == null) {
            // If we could not find the code then just return the original code.
            return iso639_2B;
        } else {
            return code.getAlpha3().getAlpha3T().name();
        }
    }

    /**
     * Convert the iso639_2T to iso639_2B
     */
    public static String iso639_2T_to_iso639_2B(String iso639_2T) {
        LanguageCode code = LanguageCode.getByCode(iso639_2T);
        if (code == null) {
            // If we could not find the code then just return the original code.
            return iso639_2T;
        } else {
            return code.getAlpha3().getAlpha3B().name();
        }
    }

}
