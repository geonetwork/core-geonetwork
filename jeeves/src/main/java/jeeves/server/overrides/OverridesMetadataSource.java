package jeeves.server.overrides;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.ExpressionBasedFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.RegexRequestMatcher;
import org.springframework.security.web.util.RequestMatcher;

public class OverridesMetadataSource implements FilterInvocationSecurityMetadataSource {
    private final Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = Collections.synchronizedMap(new HashMap<RequestMatcher, Collection<ConfigAttribute>>());
    private FilterInvocationSecurityMetadataSource baseSource;

    public OverridesMetadataSource(FilterInvocationSecurityMetadataSource metadataSource) {
        this.baseSource = metadataSource;
    }

    
    
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>(baseSource.getAllConfigAttributes());
				
				synchronized( requestMap ) {

        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }

				}

        return allAttributes;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        Collection<ConfigAttribute> attributes = baseSource.getAttributes(object);

        if(attributes == null) {

					synchronized( requestMap ) {

            final HttpServletRequest request = ((FilterInvocation) object).getRequest();
            for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
                if (entry.getKey().matches(request)) {
                    attributes = entry.getValue();
                    break;
                }
            }

					}
        }
        return attributes;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return baseSource.supports(clazz) || FilterInvocation.class.isAssignableFrom(clazz);
    }



    public void addMapping(RegexRequestMatcher pattern, final String access) {
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> map = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        Collection<ConfigAttribute> atts = new LinkedList<ConfigAttribute>();
        map.put(pattern, atts );
        
        atts.add(new SecurityConfig(access));

				synchronized( requestMap ) {
        
        ExpressionBasedFilterInvocationSecurityMetadataSource ms = new ExpressionBasedFilterInvocationSecurityMetadataSource(map, new DefaultWebSecurityExpressionHandler());
        Collection<ConfigAttribute> allAttributes = requestMap.get(pattern);
        if(allAttributes == null) {
            allAttributes = new LinkedList<ConfigAttribute>();
            requestMap.put(pattern, allAttributes);
        }
        
        allAttributes.addAll(ms.getAllConfigAttributes());

				}
    }
}
