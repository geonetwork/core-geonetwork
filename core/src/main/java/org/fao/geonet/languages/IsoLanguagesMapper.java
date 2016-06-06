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
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================
package org.fao.geonet.languages;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.repository.IsoLanguageRepository;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

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
     */
    @PostConstruct
    public void init() {
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

}
