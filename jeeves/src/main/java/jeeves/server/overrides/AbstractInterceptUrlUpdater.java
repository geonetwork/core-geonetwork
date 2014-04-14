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
     * Retrieve a OverridesMetadataSource from a FilterSecurityInterceptor. If the metadata source is not a OverridesMetadataSource then set
     * it on the FilterSecurityInterceptor.
     */
    private static final Function<? super FilterSecurityInterceptor, OverridesMetadataSource> TRANSFORMER = new Function<FilterSecurityInterceptor, OverridesMetadataSource>() {

        @Override
        @Nullable
        public OverridesMetadataSource apply(@Nullable FilterSecurityInterceptor interceptor) {
            if(interceptor == null) {
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