package org.fao.geonet.wro4j;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.LazyProcessorDecorator;
import ro.isdc.wro.util.LazyInitializer;
import ro.isdc.wro.util.provider.ConfigurableProviderSupport;

import java.util.Collections;
import java.util.Map;

/**
 * The strategy used in WRO4J (Web Resource Optimizer for Java) that is responsible for locating the
 * javascript files and configuring them into groups/views.
 * <p/>
 * User: Jesse Date: 11/20/13 Time: 8:53 AM
 */
public class GeonetworkWrojModelFactoryProvider extends ConfigurableProviderSupport {
    @Override
    public Map<String, WroModelFactory> provideModelFactories() {
        return Collections.<String, WroModelFactory>singletonMap("geonetwork", new GeonetWroModelFactory());
    }

    @Override
    public Map<String, UriLocator> provideLocators() {
        final Map<String, UriLocator> locatorMap = super.provideLocators();
        locatorMap.put(ClosureDependencyUriLocator.URI_LOCATOR_ID, new ClosureDependencyUriLocator());
        locatorMap.put(TemplatesUriLocator.URI_LOCATOR_ID, new TemplatesUriLocator());
        return locatorMap;
    }

    @Override
    public Map<String, ResourcePreProcessor> providePreProcessors() {
        final Map<String, ResourcePreProcessor> preProcessorMap = super.providePreProcessors();
        preProcessorMap.put(StripGoogProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
            @Override
            protected ResourcePreProcessor initialize() {
                return new StripGoogProcessor();
            }
        }));
        preProcessorMap.put(RemoveSourceMapUrlProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
            @Override
            protected ResourcePreProcessor initialize() {
                return new RemoveSourceMapUrlProcessor();
            }
        }));
        preProcessorMap.put(AddFileUriCommentProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
            @Override
            protected ResourcePreProcessor initialize() {
                return new AddFileUriCommentProcessor();
            }
        }));
        preProcessorMap.put(GeonetLessCompilerProcessor.ALIAS, new LazyProcessorDecorator(new LazyInitializer<ResourcePreProcessor>() {
            @Override
            protected ResourcePreProcessor initialize() {
                return new GeonetLessCompilerProcessor();
            }
        }));
        return preProcessorMap;
    }
}
