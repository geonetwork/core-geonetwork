/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
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

package org.fao.geonet.harvester.wfsfeatures.worker;

import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.data.wfs.internal.WFSConfig;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.http.HTTPClient;
import org.geotools.ows.ServiceException;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.PackedCoordinateSequenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * WFSDataStore which use the DescribeFeatureType request to determine
 * which WFSStrategy to user. Call init, before createDataStore.
 */
public class WFSDataStoreWithStrategyInvestigator extends WFSDataStoreFactory {

    private static Logger LOGGER =  LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    private String describeFeatureTypeUrl;

    public void init (String url, String typeName) throws Exception {
        this.describeFeatureTypeUrl = new OwsUtils().getDescribeFeatureTypeUrl(url, typeName, "1.1.0");
    }

    @Override
    public WFSDataStore createDataStore(Map params) throws IOException {
        final WFSConfig config = WFSConfig.fromParams(params);

        {
            String user = config.getUser();
            String password = config.getPassword();
            if (((user == null) && (password != null))
                || ((config.getPassword() == null) && (config.getUser() != null))) {
                throw new IOException(
                    "Cannot define only one of USERNAME or PASSWORD, must define both or neither");
            }
        }

        final URL capabilitiesURL = (URL) URL.lookUp(params);

        final HTTPClient http = getHttpClient(params);
        http.setTryGzip(config.isTryGZIP());
        http.setUser(config.getUser());
        http.setPassword(config.getPassword());
        int timeoutMillis = config.getTimeoutMillis();
        http.setConnectTimeout(timeoutMillis / 1000);
        http.setReadTimeout(timeoutMillis / 1000);


        // WFSClient performs version negotiation and selects the correct strategy
        WFSClientWithStrategyInvestigator wfsClient;
        try {
            wfsClient = new WFSClientWithStrategyInvestigator(capabilitiesURL, http, config, describeFeatureTypeUrl);
        } catch (ServiceException e) {
            throw new IOException(e);
        }

        WFSDataStore dataStore = new WFSDataStore(wfsClient);
        // factories
        dataStore.setFilterFactory(CommonFactoryFinder.getFilterFactory(null));
        dataStore.setGeometryFactory(
            new GeometryFactory(PackedCoordinateSequenceFactory.DOUBLE_FACTORY));
        dataStore.setFeatureTypeFactory(new FeatureTypeFactoryImpl());
        dataStore.setFeatureFactory(CommonFactoryFinder.getFeatureFactory(null));
        dataStore.setDataStoreFactory(this);
        dataStore.setNamespaceURI(config.getNamespaceOverride());

        return dataStore;
    }
}
