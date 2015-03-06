package org.fao.geonet.services.metadata.format.cache;

import com.google.common.collect.Sets;
import org.fao.geonet.services.metadata.format.FormatType;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Jesse on 3/6/2015.
 */
public class ConfigurableCacheConfig implements CacheConfig {
    private Set<FormatType> allowedTypes = Sets.newHashSet(FormatType.values());
    private Set<String> allowedLanguages = null;
    private Set<String> formatterIds = null;
    private boolean cacheFullMetadata = true;
    private boolean cacheHideWithheld = true;

    public ConfigurableCacheConfig() {
        allowedTypes.remove(FormatType.pdf);
        allowedTypes.remove(FormatType.testpdf);
    }

    @Override
    public boolean allowCaching(Key key) {
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
        if (!key.hideWithheld && !cacheFullMetadata) {
            return false;
        }

        return true;
    }

    /**
     * Configure the {@link org.fao.geonet.services.metadata.format.FormatType}s to cache.
     * By default all types except pdf will be cached.
     *
     * @param allowedTypes set of {@link org.fao.geonet.services.metadata.format.FormatType}s
     */
    public void setAllowedTypes(@Nonnull Set<FormatType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    /**
     * The languages to cache.  If null then all languages will be cached.
     *
     * By default all languages will be cached.
     */
    public void setAllowedLanguages(@Nullable Set<String> allowedLanguages) {
        this.allowedLanguages = allowedLanguages;
    }

    /**
     * The formatters to cache.  If null then all formatters will be cached.
     *
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
}
