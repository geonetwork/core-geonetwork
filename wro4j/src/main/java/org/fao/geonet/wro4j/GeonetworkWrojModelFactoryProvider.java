package org.fao.geonet.wro4j;

import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.util.provider.ConfigurableProviderSupport;

import java.util.Collections;
import java.util.Map;

/**
 * The strategy used in WRO4J (Web Resource Optimizer for Java) that is responsible
 * for locating the javascript files and configuring them into groups/views.
 * <p/>
 * User: Jesse
 * Date: 11/20/13
 * Time: 8:53 AM
 */
public class GeonetworkWrojModelFactoryProvider extends ConfigurableProviderSupport {
    @Override
    public Map<String, WroModelFactory> provideModelFactories() {
        return Collections.<String, WroModelFactory>singletonMap("geonetwork", new GeonetWroModelFactory());
    }
}
