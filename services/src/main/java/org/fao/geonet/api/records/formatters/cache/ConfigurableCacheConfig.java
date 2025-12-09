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

package org.fao.geonet.api.records.formatters.cache;

import com.google.common.collect.Sets;
import org.fao.geonet.api.records.formatters.FormatType;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jesse on 3/6/2015.
 */
public class ConfigurableCacheConfig extends AbstractCacheConfig {
    private Set<FormatType> allowedTypes = Sets.newHashSet(FormatType.values());
    private Set<String> allowedLanguages = null;
    private Set<String> formatterIds = null;
    private boolean cacheFullMetadata = true;
    private boolean cacheHideWithheld = true;
    private HashSet<FormatType> typeExceptions;
    private HashSet<String> formatterExceptions;
    private HashSet<String> langExceptions;

    public ConfigurableCacheConfig() {
        allowedTypes.remove(FormatType.pdf);
        allowedTypes.remove(FormatType.testpdf);
    }

    @Override
    public boolean extraChecks(Key key) {
        if (typeExceptions != null && typeExceptions.contains(key.formatType)) {
            return false;
        }
        if (formatterExceptions != null && formatterExceptions.contains(key.formatterId)) {
            return false;
        }
        if (langExceptions != null && langExceptions.contains(key.lang)) {
            return false;
        }
        if (!allowedTypes.contains(key.formatType)) {
            return false;
        }
        if (allowedLanguages != null && !allowedLanguages.contains(key.lang)) {
            return false;
        }
        if (formatterIds != null && !formatterIds.contains(key.formatterId)) {
            return false;
        }
        if (formatterIds != null && !formatterIds.contains(key.formatterId)) {
            return false;
        }

        if (key.hideWithheld && !cacheHideWithheld) {
            return false;
        }
        return key.hideWithheld || cacheFullMetadata;
    }

    /**
     * Configure the {@link org.fao.geonet.api.records.formatters.FormatType}s to cache. By default
     * all types except pdf will be cached.
     *
     * @param allowedTypes set of {@link org.fao.geonet.api.records.formatters.FormatType}s
     */
    public void setAllowedTypes(@Nonnull Set<FormatType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    /**
     * The languages to cache.  If null then all languages will be cached.
     * <p>
     * By default all languages will be cached.
     */
    public void setAllowedLanguages(@Nullable Set<String> allowedLanguages) {
        this.allowedLanguages = allowedLanguages;
    }

    /**
     * The formatters to cache.  If null then all formatters will be cached.
     * <p>
     * By default all formatters will be cached.
     */
    public void setFormatterIds(@Nullable Set<String> formatterIds) {
        this.formatterIds = formatterIds;
    }

    /**
     * If false then the full metadata will not be cached (when an editor obtains the metadata).
     */
    public void setCacheFullMetadata(boolean cacheFullMetadata) {
        this.cacheFullMetadata = cacheFullMetadata;
    }

    /**
     * If false then the metadata with hidden elements will not be cached.
     */
    public void setCacheHideWithheld(boolean cacheHideWithheld) {
        this.cacheHideWithheld = cacheHideWithheld;
    }

    public void setTypeExceptions(HashSet<FormatType> typeExceptions) {
        this.typeExceptions = typeExceptions;
    }

    public void setFormatterExceptions(HashSet<String> formatterExceptions) {
        this.formatterExceptions = formatterExceptions;
    }

    public void setLangExceptions(HashSet<String> langExceptions) {
        this.langExceptions = langExceptions;
    }
}
