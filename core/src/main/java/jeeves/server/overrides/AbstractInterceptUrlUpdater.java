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

package jeeves.server.overrides;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.jdom.Element;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.util.RegexRequestMatcher;

import javax.annotation.Nullable;

import java.util.Map;
import java.util.Properties;

abstract class AbstractInterceptUrlUpdater implements Updater {

    /**
     * Retrieve a OverridesMetadataSource from a FilterSecurityInterceptor. If the metadata source
     * is not a OverridesMetadataSource then set it on the FilterSecurityInterceptor.
     */
    private static final Function<? super FilterSecurityInterceptor, OverridesMetadataSource> TRANSFORMER = new Function<FilterSecurityInterceptor, OverridesMetadataSource>() {

        @Override
        @Nullable
        public OverridesMetadataSource apply(@Nullable FilterSecurityInterceptor interceptor) {
            if (interceptor == null) {
                throw new IllegalArgumentException();
            } else {
                FilterInvocationSecurityMetadataSource metadataSource = interceptor.getSecurityMetadataSource();

                OverridesMetadataSource overrideSource;
                if (metadataSource instanceof OverridesMetadataSource) {
                    overrideSource = (OverridesMetadataSource) metadataSource;
                } else {
                    overrideSource = new OverridesMetadataSource(metadataSource);
                    interceptor.setSecurityMetadataSource(overrideSource);
                }
                return overrideSource;
            }

        }
    };
    protected final RegexRequestMatcher pattern;
    protected String patternString;

    public AbstractInterceptUrlUpdater(Element element) {
        this.pattern = new RegexRequestMatcher(element.getAttributeValue("pattern"), element.getAttributeValue("httpMethod"),
            Boolean.parseBoolean(element.getAttributeValue("caseInsensitive")));
        this.patternString = element.getAttributeValue("pattern");
    }

    @Override
    public boolean runOnFinish() {
        return true;
    }

    /**
     * Update the FilterInvocationSecurityMetadataSource beans
     */
    protected abstract void update(Iterable<OverridesMetadataSource> sources);

    @Override
    public void update(ConfigurableListableBeanFactory beanFactory, Properties properties) {
        Map<String, FilterSecurityInterceptor> beansOfType = beanFactory.getBeansOfType(FilterSecurityInterceptor.class);
        Iterable<OverridesMetadataSource> sources = Iterables.transform(beansOfType.values(), TRANSFORMER);
        update(sources);
    }
}
