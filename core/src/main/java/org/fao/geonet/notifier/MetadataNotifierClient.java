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
import jeeves.server.context.ServiceContext;
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
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Metadata notifier client to manage the communication with notification servlet.
 *  
 */
public class MetadataNotifierClient {
    /**
     * Uses the notifier update service to handle insertion and updates of metadata.
     *
     *
     * @param metadata
     * @param metadataUuid
     * @param context
     * @throws MetadataNotifierClientException
     */
	public void webUpdate(String serviceUrl, final String username, final String password, String metadata,
                          String metadataUuid, final ServiceContext context) throws MetadataNotifierClientException {

		//RequestEntity requestEntity = new InputStreamRequestEntity(isoDocumentInputStream);

		//method.setRequestEntity(requestEntity);
        List<? extends NameValuePair> data = Arrays.asList(
                new BasicNameValuePair("action", "update"),
                new BasicNameValuePair("uuid", metadataUuid),
                new BasicNameValuePair("XMLFile", metadata)
        );
        execute(serviceUrl, username, password, context, data);
    }

    private void execute(String serviceUrl, final String username, final String password, final ServiceContext context, List<? extends NameValuePair> data) throws MetadataNotifierClientException {
        try {

            // Create a method instance.
            HttpPost method = new HttpPost(serviceUrl);
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data);
            final RequestConfig.Builder configBuilder = RequestConfig.custom();
            configBuilder.setMaxRedirects(3);

            method.setEntity(entity);

            configBuilder.setAuthenticationEnabled(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password));

            method.setConfig(configBuilder.build());

            final GeonetHttpRequestFactory requestFactory = context.getBean(GeonetHttpRequestFactory.class);
            ClientHttpResponse response = requestFactory.execute(method, new Function<HttpClientBuilder, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable HttpClientBuilder input) {
                    final CredentialsProvider provider = Lib.net.setupProxy(context.getBean(SettingManager.class), input);
                    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
                        System.out.println("webUpdate: SET USER");
                        provider.setCredentials( AuthScope.ANY, new UsernamePasswordCredentials(username, password));

                        configBuilder.setAuthenticationEnabled(true);
                    }
                    return null;
                }
            });

			// Execute the method.
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new MetadataNotifierClientException("Method failed: " + response.getStatusText());
			}

			// Read the response body.
			// byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			// System.out.println(new String(responseBody));

		} catch (IOException e) {
			throw new MetadataNotifierClientException(e);
		}
    }

    /**
     * Uses the notifier delete service to handle deletion of metadata.
     *
     * @param metadataUuid
     * @throws MetadataNotifierClientException
     */
	public void webDelete(String serviceUrl, String username, String password,
                          String metadataUuid, ServiceContext context) throws MetadataNotifierClientException {

        List<? extends NameValuePair> data = Arrays.asList(
                new BasicNameValuePair("action", "delete"),
                new BasicNameValuePair("uuid", metadataUuid),
                new BasicNameValuePair("XMLFile", "")
        );

        execute(serviceUrl, username, password, context, data);
	}

}