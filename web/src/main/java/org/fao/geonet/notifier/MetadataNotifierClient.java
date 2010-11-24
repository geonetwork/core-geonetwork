package org.fao.geonet.notifier;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.util.spring.StringUtils;
import ucar.nc2.util.net.EasySSLProtocolSocketFactory;

import java.io.IOException;

/**
 * Metadata notifier client to manage the communication with notification servlet
 *  
 */
public class MetadataNotifierClient {
    /**
     * Uses the notifier update service to handle insertion and updates of metadata
     *
     * @param metadata
     * @param metadataUuid
     * @throws MetadataNotifierClientException
     */
	public void webUpdate(String serviceUrl, String username, String password, String metadata,
                          String metadataUuid, GeonetContext gc) throws MetadataNotifierClientException {
        HttpClient client;

        Protocol.registerProtocol("https",
        new Protocol("https", new EasySSLProtocolSocketFactory(), 443));

		// Create a method instance.
		PostMethod method = new PostMethod(serviceUrl);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        NameValuePair[] data = {
          new NameValuePair("action", "update"),
          new NameValuePair("uuid", metadataUuid),
          new NameValuePair("XMLFile", metadata)
        };

        method.setRequestBody(data);

		//RequestEntity requestEntity = new InputStreamRequestEntity(isoDocumentInputStream);

		//method.setRequestEntity(requestEntity);
		try {
            // Create an instance of HttpClient.
            client = new HttpClient();

            if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
                System.out.println("webUpdate: SET USER");
                client.getState().setCredentials(
                               AuthScope.ANY,
                               new UsernamePasswordCredentials(username, password)
                               );

                method.setDoAuthentication( true );
            }         

            System.out.println("settingMan: " + (gc.getSettingManager() != null));
            if (gc.getSettingManager() != null) Lib.net.setupProxy(gc.getSettingManager(), client);


			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new MetadataNotifierClientException("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			// byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			// System.out.println(new String(responseBody));

		} catch (HttpException e) {
			throw new MetadataNotifierClientException(e);
		} catch (IOException e) {
			throw new MetadataNotifierClientException(e);
		} finally {
			// Release the connection.
			method.releaseConnection(); 
            client = null;
		}
	}

    /**
     * Uses the notifier delete service to handle deletion of metadata
     *
     * @param metadataUuid
     * @throws MetadataNotifierClientException
     */
	public void webDelete(String serviceUrl, String username, String password,
                          String metadataUuid, GeonetContext gc) throws MetadataNotifierClientException {
        HttpClient client;

        Protocol.registerProtocol("https",
        new Protocol("https", new EasySSLProtocolSocketFactory(), 443));

		// Create a method instance.
		PostMethod method = new PostMethod(serviceUrl);

		// Provide custom retry handler is necessary
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

		try {
            // Create an instance of HttpClient.
            client = new HttpClient();

             if (StringUtils.hasLength(username) && StringUtils.hasLength(password)) {
                client.getState().setCredentials(
                               AuthScope.ANY,
                               new UsernamePasswordCredentials(username, password)
                               );
                method.setDoAuthentication( true ); 
            }
            
            if (gc.getSettingManager() != null) Lib.net.setupProxy(gc.getSettingManager(), client);

            NameValuePair[] data = {
              new NameValuePair("action", "delete"),
              new NameValuePair("uuid", metadataUuid),
              new NameValuePair("XMLFile", "")
            };

            method.setRequestBody(data);

			//RequestEntity requestEntity = new StringRequestEntity(metadataUuid, null, null);

			//method.setRequestEntity(requestEntity);

			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {
				throw new MetadataNotifierClientException("Method failed: " + method.getStatusLine());
			}

			// Read the response body.
			// byte[] responseBody = method.getResponseBody();

			// Deal with the response.
			// Use caution: ensure correct character encoding and is not binary data
			// System.out.println(new String(responseBody));

		} catch (HttpException e) {
			throw new MetadataNotifierClientException(e);
		} catch (IOException e) {
			throw new MetadataNotifierClientException(e);
		} finally {
			// Release the connection.
			method.releaseConnection();
            client = null;
		}
	}

}
