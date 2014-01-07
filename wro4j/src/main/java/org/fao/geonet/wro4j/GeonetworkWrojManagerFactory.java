package org.fao.geonet.wro4j;

import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;

import java.util.Properties;

/**
 * User: Jesse
 * Date: 11/25/13
 * Time: 8:35 AM
 */
public class GeonetworkWrojManagerFactory extends ConfigurableWroManagerFactory {

    @Override
    protected WroModelFactory newModelFactory() {
        return new GeonetWroModelFactory() {
            @Override
            protected Properties getConfigProperties() {
                return newConfigProperties();
            }
        };
    }
}
