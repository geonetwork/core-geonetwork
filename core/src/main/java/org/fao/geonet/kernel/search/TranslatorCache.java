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

package org.fao.geonet.kernel.search;

import com.google.common.base.Optional;

import org.fao.geonet.domain.Localized;
import org.fao.geonet.entitylistener.GeonetworkEntityListener;
import org.fao.geonet.entitylistener.PersistentEventType;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A in-memory cache for facet translations.  This should be used by any translator that has slow
 * performance (needs to access files or databases, etc...).
 *
 * @author Jesse on 1/3/2015.
 */
public class TranslatorCache {
    private final Map<String, Optional<Localized>> cache = new HashMap<>();

    /**
     * Return null or the optional containing the Optional Localized element containing the
     * translations (or a Optional null if the value was loaded but no value can possibly be
     * loaded).
     *
     * @param key the key for the value.
     */
    @Nullable
    public synchronized Optional<Localized> get(@Nonnull String key) {
        return cache.get(key);
    }

    /**
     * Cache the loaded value (even if the loaded value is null).
     *
     * @param applicationContext the application context
     * @param key                the key used to do the loading
     * @param value              the value loaded or null
     */
    public synchronized void put(@Nonnull ConfigurableApplicationContext applicationContext,
                                 @Nonnull String key,
                                 @Nullable Localized value) {
        if (value != null) {
            final String beanName = "ClearTranslatorCache_" + value.getClass().getSimpleName();
            try {
                applicationContext.getBean(beanName);
            } catch (NoSuchBeanDefinitionException e) {
                applicationContext.getBeanFactory().registerSingleton(beanName, new ClearCacheListener(value.getClass()));
            }

        }
        this.cache.put(key, Optional.fromNullable(value));
    }


    private class ClearCacheListener implements GeonetworkEntityListener {

        private final Class entityClass;

        private ClearCacheListener(Class entityClass) {
            this.entityClass = entityClass;
        }

        @Override
        public Class getEntityClass() {
            return this.entityClass;
        }

        @Override
        public void handleEvent(PersistentEventType type, Object entity) {
            cache.clear();
        }
    }

}
