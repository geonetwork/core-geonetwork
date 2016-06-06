//=============================================================================
//===   Copyright (C) 2001-2010 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.notifier;

import com.google.common.base.Function;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.fao.geonet.domain.MetadataNotifier;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Metadata notifier client to manage the communication with notification servlet.
 */
public class MetadataNotifierClient {
    @Autowired
    SettingManager settingManager;
    @Autowired
    GeonetHttpRequestFactory requestFactory;

    /**
     * Uses the notifier update service to handle insertion and updates of metadata.
     */
    public void webUpdate(MetadataNotifier notifier, String metadataXml, String metadataUuid) throws MetadataNotifierClientException {
        List<? extends NameValuePair> data = Arrays.asList(
            new BasicNameValuePair("action", "update"),
            new BasicNameValuePair("uuid", metadataUuid),
            new BasicNameValuePair("XMLFile", metadataXml)
        );
        execute(notifier, data);
    }

    private void execute(final MetadataNotifier notifier, List<? extends NameValuePair> data) throws MetadataNotifierClientException {
        try {

            // Create a method instance.
            HttpPost method = new HttpPost(notifier.getUrl());
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data);
            final RequestConfig.Builder configBuilder = RequestConfig.custom();
            configBuilder.setMaxRedirects(3);

            method.setEntity(entity);

            final boolean authenticationEnabled = StringUtils.isNotBlank(notifier.getUsername()) && notifier.getPassword() != null &&
                notifier.getPassword().length > 0;
            configBuilder.setAuthenticationEnabled(authenticationEnabled);

            method.setConfig(configBuilder.build());

            final String requestHost = method.getURI().getHost();
            ClientHttpResponse response = requestFactory.execute(method, new Function<HttpClientBuilder, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable HttpClientBuilder requestBuilder) {
                    final CredentialsProvider provider = Lib.net.setupProxy(settingManager, requestBuilder, requestHost);
                    if (authenticationEnabled) {
                        Log.debug("MetadataNotifierClient", "webUpdate: SET USER -> " + notifier.getUsername());
                        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(notifier.getUsername(), String.copyValueOf(notifier.getPassword())));

                        configBuilder.setAuthenticationEnabled(true);
                    }
                    return null;
                }
            });
            try {

                // Execute the method.
                if (response.getStatusCode() != HttpStatus.OK) {
                    throw new MetadataNotifierClientException("Method failed: " + response.getStatusText());
                }

                try {
                    // Free the connection closing the body input stream
                    final InputStream instream = response.getBody();
                    IOUtils.closeQuietly(instream);
                } catch (final IOException ignore) {
                    // Silently ignore
                }

            } finally {
                method.releaseConnection();
            }

        } catch (IOException e) {
            throw new MetadataNotifierClientException(e);
        }
    }

    /**
     * Uses the notifier delete service to handle deletion of metadata.
     *
     * @param metadataUuid medatada UUID identifier
     */
    public void webDelete(MetadataNotifier notifier, String metadataUuid) throws MetadataNotifierClientException {

        List<? extends NameValuePair> data = Arrays.asList(
            new BasicNameValuePair("action", "delete"),
            new BasicNameValuePair("uuid", metadataUuid),
            new BasicNameValuePair("XMLFile", "")
        );

        execute(notifier, data);
    }

}
