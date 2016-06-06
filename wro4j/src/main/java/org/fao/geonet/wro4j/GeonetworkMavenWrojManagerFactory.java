package org.fao.geonet.wro4j;

import ro.isdc.wro.maven.plugin.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;

import java.util.Properties;

/**
 * User: Jesse Date: 11/25/13 Time: 8:35 AM
 */
public class GeonetworkMavenWrojManagerFactory extends ConfigurableWroManagerFactory {

    @Override
    protected WroModelFactory newModelFactory() {
        return new GeonetWroModelFactory() {
            @Override
            protected Properties getConfigProperties() {
                return createProperties();
            }
        };
    }

    @Override
    protected UriLocatorFactory newUriLocatorFactory() {
        return super.newUriLocatorFactory();
    }


}
