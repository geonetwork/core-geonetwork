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

package org.fao.geonet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common superclass of entities that are have translated labels.
 * <p/>
 * <p/>
 * A class that wants to extend this class has to also override #getLabelTranslations and annotate
 * that method with the table join annotions.  For example:
 * <p/>
 * <pre>
 *             <code> <![CDATA[
 *
 * @Override
 * @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
 * @CollectionTable(joinColumns = @JoinColumn(name = "iddes"), name = "groupsdes")
 * @MapKeyColumn(name = "langid", length = 5)
 * @Column(name = "label", nullable = false, length = 96)
 * public Map<String, String> getLabelTranslations() {
 * return super.getLabelTranslations();
 * }
 * ]]></code>
 * </pre>
 * </p> </p>
 * <p/>
 * User: Jesse Date: 9/9/13 Time: 8:53 AM
 */
public abstract class Localized extends GeonetEntity {

    private Map<String, String> _labelTranslations = new HashMap<String, String>();

    public static Map<String, String> translationXmlToLangMap(List<Element> translations) {
        Map<String, String> labelTranslations = Maps.newHashMap();
        if (translations != null) {
            for (Element translation : translations) {
                String langId = translation.getName();
                String value = translation.getText();
                labelTranslations.put(langId, value);
            }
        }
        return labelTranslations;
    }

    /**
     * Get the map of langid -> label translations for metadata categories. <p> langid is an iso 3
     * character code for the language. For example: eng, ger, fra, etc... </p>
     *
     * @return the map of langid -> label
     */
    @Nonnull
    @JsonProperty(value = "label")
    public Map<String, String> getLabelTranslations() {
        return _labelTranslations;
    }

    /**
     * Set translations of this localized objects to those contained in the list of elements. <p>
     * The translations elements have the form: <code>&lt;langId&gt;labelTranslation&lt;/langId&gt;</code>
     * <p> For example: <code>&lt;eng&gt;Catalog Group&lt;/eng&gt;</code> </p> </p>
     */
    @JsonIgnore
    public void setLabelTranslationsFromElement(List<Element> translations) {
        getLabelTranslations().clear();
        getLabelTranslations().putAll(translationXmlToLangMap(translations));
    }

    /**
     * Get a translation for the given language 3 letter code: ger, fra, eng, etc...
     *
     * @param threeLetterLanguageCode the desired translation.
     * @return the translation for the language code or <code>null</code>.
     */
    public
    @Nullable
    String getLabel(@Nonnull String threeLetterLanguageCode) {
        return _labelTranslations.get(threeLetterLanguageCode);
    }

    /**
     * Set new translations this should only be used for initialization. To add and remove
     * translations use "get" and modify map.
     *
     * @param localizedTranslations the translation map
     */
    public void setLabelTranslations(@Nonnull Map<String, String> localizedTranslations) {
        this._labelTranslations = localizedTranslations;
    }
}
