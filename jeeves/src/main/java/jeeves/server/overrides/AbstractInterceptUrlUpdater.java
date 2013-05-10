package jeeves.server.overrides;

import java.util.Properties;

import jeeves.config.springutil.GeonetworkFilterSecurityInterceptor;

import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.RegexRequestMatcher;

abstract class AbstractInterceptUrlUpdater extends BeanUpdater {

    protected final RegexRequestMatcher pattern;

    public AbstractInterceptUrlUpdater(Element element) {
        setBeanName(element);
        this.pattern = new RegexRequestMatcher(element.getAttributeValue("pattern"), element.getAttributeValue("httpMethod"), Boolean.parseBoolean(element.getAttributeValue("caseInsensitive")));
    }

    @Override
    public final Object update(ApplicationContext applicationContext, Properties properties, Object bean) {
        OverridesMetadataSource overrideSource = getOverrideMetadataSource(bean);
        
        update(overrideSource);
        return null;
    }

    protected abstract void update(OverridesMetadataSource overrideSource);

    /**
     * Find or create the OverridesMetadataSource to use for modifying the intercept urls
     * 
     * @param bean the bean to be modified
     */
    public static OverridesMetadataSource getOverrideMetadataSource(Object bean) {
        GeonetworkFilterSecurityInterceptor interceptor = (GeonetworkFilterSecurityInterceptor) bean;
        
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