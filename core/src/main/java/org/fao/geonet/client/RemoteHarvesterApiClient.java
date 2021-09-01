package org.fao.geonet.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

public class RemoteHarvesterApiClient {
    public static final String SETTING_REMOTE_HARVESTER_API =
        "system/harvester/remoteHarvesterApiUrl";

    private GeonetHttpRequestFactory requestFactory;
    private String url;

    public RemoteHarvesterApiClient(String url) {
        this.url = url;

        requestFactory =
            ApplicationContextHolder.get().getBean(GeonetHttpRequestFactory.class);
    }


    public String startHarvest(RemoteHarvesterConfiguration harvesterConfiguration)
        throws RemoteHarvesterApiClientException {
        String harvestUrl = url + "/startHarvest";

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String body = gson.toJson(harvesterConfiguration);

        ClientHttpResponse httpResponse = null;
        HttpPost postMethod = null;

        String processId = "";

        try {
            postMethod = new HttpPost(harvestUrl);

            ((HttpUriRequest) postMethod).addHeader( new BasicHeader("Content-Type",  "application/json") );

            StringEntity requestEntity = new StringEntity(
                body,
                "application/json",
                "UTF-8");

            postMethod.setEntity(requestEntity);


            httpResponse = requestFactory.execute(postMethod);

            int status = httpResponse.getRawStatusCode();

            if (status != HttpStatus.SC_OK) {
                String message = String.format(
                    "Failed to create '%s' with '%s'. Status is %d. Error is %s.",
                    url, body, status,
                    httpResponse.getStatusText());


                throw new Exception(message);
            } else {
                String response = IOUtils.toString(httpResponse.getBody(), "UTF-8");
                JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();

                processId = jsonObject.get("processID").getAsString();
            }
        } catch (Exception ex) {
            throw new RemoteHarvesterApiClientException(ex.getMessage());

        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }

        return processId;
    }


    public RemoteHarvesterStatus retrieveProgress(String processId) throws RemoteHarvesterApiClientException {
        String harvesterProgressUrl = url + String.format("/getstatus/%s", processId);

        ClientHttpResponse httpResponse = null;
        HttpGet getMethod = null;

        RemoteHarvesterStatus harvesterStatus = null;

        try {
            getMethod = new HttpGet(harvesterProgressUrl);

            ((HttpUriRequest) getMethod).addHeader( new BasicHeader("Content-Type",  "application/json") );

            httpResponse = requestFactory.execute(getMethod);

            int status = httpResponse.getRawStatusCode();

            if (status != HttpStatus.SC_OK) {
                String message = String.format(
                    "Failed to retrieve harvester progress '%s'. Status is %d. Error is %s.",
                    url, status,
                    httpResponse.getStatusText());


                throw new RemoteHarvesterApiClientException(message);
            } else {
                /*
                {"processID":"54c1329f-7bc6-4e4a-89f5-a44a709fd5ae","url":"http://geoservices.wallonie.be/metawal/csw-inspire",
                 "longTermTag":"fcfbda28-3ac1-4bc3-a9cd-5d4f29ed88a8","state":"DETERMINING_WORK",
                  "createTimeUTC":"2021-06-22T12:43:49.533Z","lastUpdateUTC":"2021-06-22T12:43:50.152Z","endpoints":[]}
                 */
                String response = IOUtils.toString(httpResponse.getBody(), "UTF-8");

                Gson gson = new Gson();
                harvesterStatus = gson.fromJson(response, RemoteHarvesterStatus.class);
            }
        } catch (Exception ex) {
            throw new RemoteHarvesterApiClientException(ex.getMessage());

        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
            // Release the connection.
            IOUtils.closeQuietly(httpResponse);
        }

        return harvesterStatus;
    }
}
