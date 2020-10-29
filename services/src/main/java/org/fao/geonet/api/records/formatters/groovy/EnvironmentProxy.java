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

package org.fao.geonet.api.records.formatters.groovy;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterParams;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.jdom.Element;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.Map;

/**
 * This class is the actual object passed to the Groovy script.  It looks up in the current
 * ThreadLocal the environment object for the current request.
 *
 * @author Jesse on 10/20/2014.
 */
public class EnvironmentProxy implements Environment {
    private static final ThreadLocal<Environment> currentEnvironment = new InheritableThreadLocal<Environment>();

    public static void setCurrentEnvironment(FormatterParams fparams) {
        currentEnvironment.set(new EnvironmentImpl(fparams, fparams.context.getBean(IsoLanguagesMapper.class)));
    }

    public static void clearContext() {
        currentEnvironment.set(null);
    }

    private static Environment get() {
        final Environment env = currentEnvironment.get();
        if (env == null) {
            throw new AssertionError(
                "The Environment object cannot be used at the moment, it can only be used during XML processing," +
                    " not during the configuration stage.  It is accessible here only so that it is in scope in all" +
                    " handlers without having to pass it in as a parameter to all handlers and sorters and similar" +
                    " object used for XML processing");
        }

        return env;
    }

    @Override
    public String getLang3() {
        return get().getLang3();
    }

    @Override
    public String getLang2() {
        return get().getLang2();
    }

    @Override
    public int getMetadataId() {
        return get().getMetadataId();
    }

    @Override
    public String getMetadataUUID() {
        return get().getMetadataUUID();
    }

    @Override
    public String getResourceUrl() {
        return get().getResourceUrl();
    }

    @Override
    public String getLocalizedUrl() {
        return get().getLocalizedUrl();
    }

    @Override
    public Authentication getAuth() {
        return get().getAuth();
    }

    @Override
    public FormatType getFormatType() {
        return get().getFormatType();
    }

    @Override
    public FormatterWidth getEmbeddingWidth() {
        return get().getEmbeddingWidth();
    }

    @Override
    public Multimap<String, ParamValue> params() {
        return get().params();
    }

    @Override
    public ParamValue param(String paramName) {
        return get().param(paramName);
    }

    @Override
    public Collection<ParamValue> paramValues(String paramName) {
        return get().paramValues(paramName);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return get().getHeaders(name);
    }

    @Override
    public Optional<String> getHeader(String name) {
        return get().getHeader(name);
    }

    @Override
    public Element getMetadataElement() {
        return get().getMetadataElement();
    }

    @Override
    public Map<String, Collection<String>> getIndexInfo() throws Exception {
        return get().getIndexInfo();
    }

    @Override
    public ServiceContext getContext() {
        return get().getContext();
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return get().getBean(clazz);
    }

    @Override
    public <T> T getBean(final String name, final Class<T> clazz) {
        return get().getBean(name, clazz);
    }

    @Override
    public MapConfig getMapConfiguration() {
        return get().getMapConfiguration();
    }

    @Override
    public boolean canEdit() throws Exception {
        return get().canEdit();
    }
}
