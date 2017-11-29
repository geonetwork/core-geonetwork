/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.api.records.editing;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.fao.geonet.exceptions.ServiceNotFoundEx;
import org.fao.geonet.utils.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javassist.NotFoundException;

// A static style interface for methods in Inspire Service.
// Based on ETF Web API v.2 BETA
public class InspireValidatorUtils {

    /** The Constant USER_AGENT. */
    private final static String USER_AGENT = "Mozilla/5.0";

    /** The Constant ACCEPT. */
    private final static String ACCEPT = "application/json";

    /** The Constant CheckStatus_URL. */
    private final static String CheckStatus_URL = "/v2/status";

    /** The Constant ExecutableTestSuites_URL. */
    private final static String ExecutableTestSuites_URL = "/v2/ExecutableTestSuites";

    /** The Constant TestObjects_URL. */
    private final static String TestObjects_URL = "/v2/TestObjects";

    /** The Constant TestRuns_URL. */
    private final static String TestRuns_URL = "/v2/TestRuns";

    /** The Constant TESTS_TO_RUN. */
    private final static String[] TESTS_TO_RUN = {"Conformance class: INSPIRE Profile based on EN ISO 19115 and EN ISO 19119", "Conformance class: XML encoding of ISO 19115/19119 metadata"};

    /**
     * Check service status.
     *
     * @param endPoint the end point
     * @param client the client (optional) (optional)
     * @return true, if successful
     */
    public static boolean checkServiceStatus(String endPoint, CloseableHttpClient client) {

        boolean close = false;
        if(client == null) {
            client = HttpClients.createDefault();
            close = true;
        }
        HttpGet request = new HttpGet(endPoint + CheckStatus_URL);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);
        HttpResponse response;

        try {
            response = client.execute(request);
        } catch(Exception e) {
            Log.warning(Log.SERVICE, "Error calling INSPIRE service: " + endPoint, e);
            return false;
        } finally {
            if(close) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.error(Log.SERVICE, "Error closing CloseableHttpClient: " + endPoint, e);
                }
            }
        }

        if(response.getStatusLine().getStatusCode() == 200) {
            return true;
        } else {
            Log.warning(Log.SERVICE, "INSPIRE service not available: " + endPoint + CheckStatus_URL);
            return false;
        }
    }

    /**
     * Upload metadata file.
     *
     * @param endPoint the end point
     * @param xml the xml
     * @param client the client (optional)
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    private static String uploadMetadataFile(String endPoint, InputStream xml, CloseableHttpClient client) throws IOException, JSONException {

        boolean close = false;
        if(client == null) {
            client = HttpClients.createDefault();
            close = true;
        }

        try {

            HttpPost request = new HttpPost(endPoint + TestObjects_URL + "?action=upload");

            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", ACCEPT);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("fileupload", xml, ContentType.TEXT_XML, "file.xml");
            HttpEntity entity = builder.build();

            request.setEntity(entity);

            HttpResponse response = client.execute(request);

            if(response.getStatusLine().getStatusCode() == 200) {

                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);
                JSONObject jsonRoot = new JSONObject(body);
                return jsonRoot.getJSONObject("testObject").getString("id");
            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusLine().getStatusCode() + " for " + TestObjects_URL);
                return null;
            }
        } catch(Exception e) {
            Log.error(Log.SERVICE, "Error calling INSPIRE service: " + endPoint, e);
            return null;
        } finally {
            if(close) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.error(Log.SERVICE, "Error closing CloseableHttpClient: " + endPoint, e);
                }
            }
        }
    }

    /**
     * Gets the tests.
     *
     * @param endPoint the end point
     * @param client the client (optional)
     * @return the tests
     */
    private static List<String> getTests(String endPoint, CloseableHttpClient client) {

        boolean close = false;
        if(client == null) {
            client = HttpClients.createDefault();
            close = true;
        }

        try {

            HttpGet request = new HttpGet(endPoint + ExecutableTestSuites_URL);

            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", ACCEPT);
            HttpResponse response;

            response = client.execute(request);

            if(response.getStatusLine().getStatusCode() == 200) {

                List<String> testList = new ArrayList<>();

                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);

                JSONObject jsonRoot = new JSONObject(body);

                JSONObject etfItemCollection = jsonRoot.getJSONObject("EtfItemCollection");
                JSONObject executableTestSuites = etfItemCollection.getJSONObject("executableTestSuites");
                JSONArray executableTestSuiteArray = executableTestSuites.getJSONArray("ExecutableTestSuite");

                for(int i=0; i < executableTestSuiteArray.length(); i++) {
                    JSONObject test = executableTestSuiteArray.getJSONObject(i);

                    boolean ok = false;

                    for (String testToRun : TESTS_TO_RUN) {
                        ok = ok || testToRun.equals(test.getString("label"));
                    }

                    if(ok) {
                        testList.add(test.getString("id"));
                    }
                }

                return testList;
            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusLine().getStatusCode() + " for " + ExecutableTestSuites_URL);
                return null;
            }
        } catch(Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            return null;
        } finally {
            if(close) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.error(Log.SERVICE, "Error closing CloseableHttpClient: " + endPoint, e);
                }
            }
        }
    }

    /**
     * Test run.
     *
     * @param endPoint the end point
     * @param fileId the file id
     * @param testList the test list
     * @param client the client (optional)
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    private static String testRun(String endPoint, String fileId, List<String> testList, String testTitle, CloseableHttpClient client)
            throws IOException, JSONException {

        boolean close = false;
        if(client == null) {
            client = HttpClients.createDefault();
            close = true;
        }

        try {
            HttpPost request = new HttpPost(endPoint + TestRuns_URL);

            JSONObject json = new JSONObject();
            JSONArray tests = new JSONArray();
            JSONObject argumets = new JSONObject();
            JSONObject testObject = new JSONObject();

            json.put("label", "TEST " + testTitle + " - " + System.currentTimeMillis());
            json.put("executableTestSuiteIds", tests);
            json.put("argumets", argumets);
            json.put("testObject", testObject);

            for (String test : testList) {
                tests.put(test);
            }

            argumets.put("files_to_test", ".*");
            argumets.put("tests_to_execute", ".*");

            testObject.put("id", fileId);

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            request.setHeader("Content-type", ACCEPT);
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", ACCEPT);
            HttpResponse response;

            response = client.execute(request);

            if(response.getStatusLine().getStatusCode() == 201) {

                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);

                JSONObject jsonRoot = new JSONObject(body);
                String testId = jsonRoot.getJSONObject("EtfItemCollection").getJSONObject("testRuns").getJSONObject("TestRun").getString("id");

                return testId;
            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusLine().getStatusCode() + " for " + TestRuns_URL);
                return null;
            }

        } catch(Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            return null;
        } finally {
            if(close) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.error(Log.SERVICE, "Error closing CloseableHttpClient: " + endPoint, e);
                }
            }
        }
    }

    /**
     * Checks if is ready.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @param client the client (optional)
     * @return true, if is ready
     * @throws Exception
     */
    public static boolean isReady(String endPoint, String testId, CloseableHttpClient client) throws Exception {

        if(testId == null) {
            return false;
        }

        boolean close = false;
        if(client == null) {
            client = HttpClients.createDefault();
            close = true;
        }

        try {

            HttpGet request = new HttpGet(endPoint + TestRuns_URL + "/" + testId + "/progress");

            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", ACCEPT);
            HttpResponse response;

            response = client.execute(request);

            if(response.getStatusLine().getStatusCode() == 200) {

                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);

                JSONObject jsonRoot = new JSONObject(body);

                // Completed when estimated number of Test Steps is equal to completed Test Steps
                // Somehow this condition is necessary but not sufficient
                // so another check on real value of test is evaluated
                return jsonRoot.getInt("val") == jsonRoot.getInt("max") & InspireValidatorUtils.isPassed(endPoint, testId, client) != null;

            } else if (response.getStatusLine().getStatusCode() == 404) {

                throw new NotFoundException("Test not found");

            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusLine().getStatusCode() + " for " + TestRuns_URL + "?view=progress");
            }
        } catch (NotFoundException e) {
            throw e;
        } catch(Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw e;
        } finally {
            if(close) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.error(Log.SERVICE, "Error closing CloseableHttpClient: " + endPoint, e);
                }
            }
        }

        return false;
    }

    /**
     * Checks if is passed.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @param client the client (optional)
     * @return the string
     * @throws Exception
     */
    public static String isPassed(String endPoint, String testId, CloseableHttpClient client) throws Exception {

        if(testId == null) {
            throw new Exception("");
        }

        boolean close = false;
        if(client == null) {
            client = HttpClients.createDefault();
            close = true;
        }

        try {

            HttpGet request = new HttpGet(endPoint + TestRuns_URL + "/" + testId);

            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept", ACCEPT);
            HttpResponse response;

            response = client.execute(request);

            if(response.getStatusLine().getStatusCode() == 200) {

                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);

                JSONObject jsonRoot = new JSONObject(body);

                try {
                    return jsonRoot.getJSONObject("EtfItemCollection").getJSONObject("testRuns").getJSONObject("TestRun").getString("status");
                } catch (JSONException e) {
                    return null;
                }

            }  else if (response.getStatusLine().getStatusCode() == 404) {

                throw new NotFoundException("Test not found");

            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusLine().getStatusCode() + " for " + TestRuns_URL + "?view=progress");
            }
        } catch(Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw e;
        } finally {
            if(close) {
                try {
                    client.close();
                } catch (IOException e) {
                    Log.error(Log.SERVICE, "Error closing CloseableHttpClient: " + endPoint, e);
                }
            }
        }

        return null;
    }


    /**
     * Gets the report url.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @return the report url
     */
    public static String getReportUrl(String endPoint, String testId) {

        return endPoint + TestRuns_URL + "/" + testId + ".html";
    }

    /**
     * Gets the report url in JSON format.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @return the report url
     */
    public static String getReportUrlJSON(String endPoint, String testId) {

        return endPoint + TestRuns_URL + "/" + testId + ".json";
    }

    /**
     * Submit file.
     *
     * @param record the record
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    public static String submitFile(String serviceEndpoint, InputStream record, String testTitle) throws IOException, JSONException {

        CloseableHttpClient client = HttpClients.createDefault();

        try {
            if (InspireValidatorUtils.checkServiceStatus(serviceEndpoint, client)) {
                // Get the tests to execute
                List<String> tests = InspireValidatorUtils.getTests(serviceEndpoint, client);
                // Upload file to test
                String testFileId = InspireValidatorUtils.uploadMetadataFile(serviceEndpoint, record, client);

                if(testFileId == null) {
                    Log.error(Log.SERVICE, "File not valid.", new Exception());
                    return null;
                }

                if(tests==null || tests.size()==0) {
                    Log.error(Log.SERVICE, "Default test sequence not supported. Check org.fao.geonet.api.records.editing.InspireValidatorUtils.TESTS_TO_RUN.", new Exception());
                    return null;
                }
                // Return test id from Inspire service
                return InspireValidatorUtils.testRun(serviceEndpoint, testFileId, tests, testTitle, client);

            } else {
                ServiceNotFoundEx ex = new ServiceNotFoundEx(serviceEndpoint);
                Log.error(Log.SERVICE, "Service unavailable.", ex);
                throw ex;
            }
        } finally {
            client.close();
        }
    }

}

