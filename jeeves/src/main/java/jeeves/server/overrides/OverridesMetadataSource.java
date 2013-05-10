package jeeves.server.overrides;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.RegexRequestMatcher;
import org.springframework.security.web.util.RequestMatcher;
import org.springframework.util.ReflectionUtils;

public class OverridesMetadataSource implements FilterInvocationSecurityMetadataSource {
    private final Map<RequestMatcher, Collection<ConfigAttribute>> requestMap;
    private FilterInvocationSecurityMetadataSource baseSource;

    @SuppressWarnings("unchecked")
    public OverridesMetadataSource(FilterInvocationSecurityMetadataSource metadataSource) {
        this.baseSource = metadataSource;
        assertKnownType(metadataSource);
        Field field = ReflectionUtils.findField(baseSource.getClass(), "requestMap");
        if (field == null) {
            throw new IllegalArgumentException("The implementation of " + FilterInvocationSecurityMetadataSource.class.getName()
                    + " has changed an now this class must be updated to work with new implementation");
        }

        field.setAccessible(true);
        requestMap = (Map<RequestMatcher, Collection<ConfigAttribute>>) ReflectionUtils.getField(field, metadataSource);
    }

    private void assertKnownType(FilterInvocationSecurityMetadataSource metadataSource) {
        if (!(baseSource instanceof DefaultFilterInvocationSecurityMetadataSource)) {
            throw new IllegalArgumentException("Modifying the interceptUrls can only be done when the metadataSource is an instanceof "
                    + DefaultFilterInvocationSecurityMetadataSource.class.getName() + ". Instead the metadataSource was a "
                    + metadataSource.getClass().getName());
        }
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return baseSource.getAllConfigAttributes();
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        return baseSource.getAttributes(object);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return baseSource.supports(clazz) || FilterInvocation.class.isAssignableFrom(clazz);
    }

    public void addMapping(RegexRequestMatcher pattern, final String access) {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        Collection<ConfigAttribute> atts = new LinkedList<ConfigAttribute>();
        map.put(pattern, atts);

        atts.add(new SecurityConfig(access));

        ExpressionBasedFilterInvocationSecurityMetadataSource ms = new ExpressionBasedFilterInvocationSecurityMetadataSource(map,
                new DefaultWebSecurityExpressionHandler());
        
        RequestMatcher requestMatcher = findMatchingRequestMatcher(pattern);
        Collection<ConfigAttribute> allAttributes = requestMap.get(requestMatcher);
        if (allAttributes == null) {
            allAttributes = new LinkedList<ConfigAttribute>();
            requestMap.put(requestMatcher, allAttributes);
        }

        allAttributes.addAll(ms.getAllConfigAttributes());
    }

    public void setMapping(RegexRequestMatcher pattern, final String access) {
        removeMapping(pattern);
        addMapping(pattern, access);
    }

    public void removeMapping(RegexRequestMatcher pattern) {
        RequestMatcher toRemove = findMatchingRequestMatcher(pattern);
        if(toRemove == null) {
            throw new IllegalArgumentException(pattern+" has not been found.");
        } else {
            requestMap.remove(toRemove);
        }
    }

    private @Nullable RequestMatcher findMatchingRequestMatcher(RegexRequestMatcher pattern) {
        for (RequestMatcher requestMatcher : requestMap.keySet()) {
            if (requestMatcher instanceof RegexRequestMatcher) {
                RegexRequestMatcher regexMatcher = (RegexRequestMatcher) requestMatcher;
                Object otherPattern = getPattern(regexMatcher);
                if (getPattern(pattern).toString().equals(otherPattern.toString())) {
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

}
