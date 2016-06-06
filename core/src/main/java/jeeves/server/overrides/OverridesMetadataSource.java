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

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.lang.Math.max;

public class OverridesMetadataSource implements FilterInvocationSecurityMetadataSource {
    private LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> _requestMap;
    private DefaultFilterInvocationSecurityMetadataSource _baseSource;

    @SuppressWarnings("unchecked")
    public OverridesMetadataSource(FilterInvocationSecurityMetadataSource metadataSource) {
        assertKnownType(metadataSource);
        Field field = ReflectionUtils.findField(metadataSource.getClass(), "requestMap");
        if (field == null) {
            throw new IllegalArgumentException("The implementation of " + FilterInvocationSecurityMetadataSource.class.getName()
                + " has changed an now this class must be updated to work with new implementation");
        }

        field.setAccessible(true);
        _requestMap = (LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>) ReflectionUtils.getField(field, metadataSource);
    }

    private void assertKnownType(FilterInvocationSecurityMetadataSource metadataSource) {
        if (!(metadataSource instanceof DefaultFilterInvocationSecurityMetadataSource)) {
            throw new IllegalArgumentException("Modifying the interceptUrls can only be done when the metadataSource is an instanceof "
                + DefaultFilterInvocationSecurityMetadataSource.class.getName() + ". Instead the metadataSource was a "
                + metadataSource.getClass().getName());
        }
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return getDelegateSource().getAllConfigAttributes();
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        return getDelegateSource().getAttributes(object);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return getDelegateSource().supports(clazz) || FilterInvocation.class.isAssignableFrom(clazz);
    }

    /**
     * Add a new mapping to this metadata source.
     *
     * @param pattern  the url pattern.  It is used to check for existing matchers to this pattern
     *                 and and the new one to the existing matcher
     * @param matcher  the matcher to add if the apping does not exist.
     * @param access   the new access rights for this metcher
     * @param position the position to add the new rule.  if < 1 then it will be first rule.  If >=
     *                 numberOfRules() then it will be the last rule
     */
    public synchronized void addMapping(final String pattern, final RequestMatcher matcher, final String access, final int position) {
        _baseSource = null;
        final Collection<ConfigAttribute> attributes = createAttributes(matcher, access);

        RequestMatcher requestMatcher = findMatchingRequestMatcher(pattern);
        if (requestMatcher == null) {
            requestMatcher = matcher;
        }
        Collection<ConfigAttribute> allAttributes = _requestMap.get(requestMatcher);
        if (allAttributes == null) {
            allAttributes = new LinkedList<ConfigAttribute>();
        }

        allAttributes.addAll(attributes);
        _requestMap.remove(requestMatcher);

        if (position > numberOfRules()) {
            _requestMap.put(requestMatcher, allAttributes);
        } else {
            final int finalPos = max(0, position);
            LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> newMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
            int i = 0;
            for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : _requestMap.entrySet()) {
                if (i == finalPos) {
                    newMap.put(requestMatcher, allAttributes);
                }
                newMap.put(entry.getKey(), entry.getValue());
                i++;
            }
            _requestMap = newMap;
        }
    }

    /**
     * Change the existing mapping.
     *
     * @param pattern the URL pattern to update.  This is used to remove the old matcher and
     *                attributes
     * @param access  the new access permissions
     * @throws IllegalArgumentException thrown in pattern does not find a match
     */
    public synchronized void setMapping(String pattern, final String access) throws IllegalArgumentException {
        _baseSource = null;
        RequestMatcher oldMatcher = findMatchingRequestMatcher(pattern);
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : _requestMap.entrySet()) {
            if (entry.getKey() == oldMatcher) {
                entry.setValue(createAttributes(oldMatcher, access));
                break;
            }
        }
    }

    private synchronized DefaultFilterInvocationSecurityMetadataSource getDelegateSource() {
        if (_baseSource == null) {
            DefaultFilterInvocationSecurityMetadataSource ms = new DefaultFilterInvocationSecurityMetadataSource(_requestMap);
            _baseSource = ms;
        }
        return _baseSource;
    }

    private Collection<ConfigAttribute> createAttributes(RequestMatcher matcher, String access) {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        Collection<ConfigAttribute> atts = new LinkedList<ConfigAttribute>();
        map.put(matcher, atts);

        atts.add(new SecurityConfig(access));

        ExpressionBasedFilterInvocationSecurityMetadataSource ms = new ExpressionBasedFilterInvocationSecurityMetadataSource(map,
            new DefaultWebSecurityExpressionHandler());

        return ms.getAllConfigAttributes();
    }

    public synchronized RequestMatcher removeMapping(String pattern) {
        _baseSource = null;
        RequestMatcher toRemove = findMatchingRequestMatcher(pattern);
        if (toRemove == null) {
            throw new IllegalArgumentException(pattern + " has not been found.");
        } else {
            _requestMap.remove(toRemove);
        }
        return toRemove;
    }

    private
    @Nullable
    RequestMatcher findMatchingRequestMatcher(String pattern) {
        for (RequestMatcher requestMatcher : _requestMap.keySet()) {
            if (requestMatcher instanceof RegexRequestMatcher) {
                RegexRequestMatcher regexMatcher = (RegexRequestMatcher) requestMatcher;
                Object otherPattern = getPattern(regexMatcher);
                if (pattern.equals(otherPattern.toString())) {
                    return regexMatcher;
                }
            }
        }
        return null;
    }

    private Object getPattern(RegexRequestMatcher regexMatcher) {
        Field field = ReflectionUtils.findField(RegexRequestMatcher.class, "pattern");
        if (field == null) {
            throw new IllegalArgumentException("The implementation of " + RegexRequestMatcher.class.getName()
                + " has changed an now this class must be updated to work with new implementation");
        }

        field.setAccessible(true);
        Object otherPattern = ReflectionUtils.getField(field, regexMatcher);
        return otherPattern;
    }

    /**
     * Return the number of rules.
     */
    public synchronized int numberOfRules() {
        return _requestMap.size();
    }
}
