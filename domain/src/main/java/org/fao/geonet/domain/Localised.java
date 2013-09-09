package org.fao.geonet.domain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Common superclass of entities that are have translated labels.
 *
 * User: Jesse
 * Date: 9/9/13
 * Time: 8:53 AM
 */
public abstract class Localised {

    private Map<String, String> _labelTranslations = new HashMap<String, String>();

    /**
     * Get the map of langid -> label translations for metadata categories.
     * <p>
     *     langid is an iso 3 character code for the language. For example:
     *     eng, ger, fra, etc...
     * </p>
     *
     * @return the map of langid -> label
     */
    @Nonnull
    public Map<String, String> getLabelTranslations() {
        return _labelTranslations;
    }

    /**
     * Get a translation for the given language 3 letter code: ger, fra, eng, etc...
     *
     * @param threeLetterLanguageCode the desired translation.
     *
     * @return the translation for the language code or <code>null</code>.
     */
    public @Nullable String getLabel(@Nonnull String threeLetterLanguageCode) {
        return _labelTranslations.get(threeLetterLanguageCode);
    }
    /**
     * Set new translations this should only be used for initialization. To add and remove translations use "get" and modify map.
     *
     * @param localizedTranslations the translation map
     */
    protected void setLabelTranslations(@Nonnull Map<String, String> localizedTranslations) {
        this._labelTranslations = localizedTranslations;
    }
}
