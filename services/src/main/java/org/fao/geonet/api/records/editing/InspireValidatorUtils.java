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
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.exceptions.ServiceNotFoundEx;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.base.Function;
import com.google.common.io.CharStreams;

import jeeves.server.context.ServiceContext;

// Utility class to access methods in Inspire Service.
// Based on ETF Web API v.2 BETA
public class InspireValidatorUtils {

    @Autowired
    private GeonetHttpRequestFactory requestFactory;

    /**
     * The Constant USER_AGENT.
     */
    private final static String USER_AGENT = "Mozilla/5.0";

    /**
     * The Constant ACCEPT.
     */
    private final static String ACCEPT = "application/json";

    /**
     * The Constant CheckStatus_URL.
     */
    private final static String CheckStatus_URL = "/v2/status";

    /**
     * The Constant ExecutableTestSuites_URL.
     */
    private final static String ExecutableTestSuites_URL = "/v2/ExecutableTestSuites";

    /**
     * The Constant TestObjects_URL.
     */
    private final static String TestObjects_URL = "/v2/TestObjects";

    /**
     * The Constant TestRuns_URL.
     */
    private final static String TestRuns_URL = "/v2/TestRuns";

    /**
     * Test status PASSED.
     */
    public final static String TEST_STATUS_PASSED = "PASSED";

    /**
     * Test status FAILED.
     */
    public final static String TEST_STATUS_FAILED = "FAILED";

    /**
     * Test status PASSED_MANUAL.
     */
    public final static String TEST_STATUS_PASSED_MANUAL = "PASSED_MANUAL";

     /**
     * Test status UNDEFINED.
     */
     public final static String TEST_STATUS_UNDEFINED = "UNDEFINED";

    /**
     * Test status NOT_APPLICABLE.
     */
    public final static String TEST_STATUS_NOT_APPLICABLE = "NOT_APPLICABLE";

    /**
     * Test status INTERNAL_ERROR.
     */
    public final static String TEST_STATUS_INTERNAL_ERROR = "INTERNAL_ERROR";


    public String defaultTestSuite;

    private Map<String, String[]> testsuites;

    private Map<String, String> testsuitesConditions;

    private Integer maxNumberOfEtfChecks;

    private Long intervalBetweenEtfChecks;


    public String getDefaultTestSuite() {
        return defaultTestSuite;
    }

    public void setDefaultTestSuite(String defaultTestSuite) {
        this.defaultTestSuite = defaultTestSuite;
    }

    public void setTestsuites(Map<String, String[]> testsuites) {
        this.testsuites = testsuites;
    }

    public Map getTestsuites() {
        return testsuites;
    }

    public void setTestsuitesConditions(Map<String, String> testsuitesConditions) {
        this.testsuitesConditions = testsuitesConditions;
    }

    public Map getTestsuitesConditions() {
        return testsuitesConditions;
    }

    public Integer getMaxNumberOfEtfChecks() {
        return maxNumberOfEtfChecks;
    }

    public void setMaxNumberOfEtfChecks(Integer maxNumberOfEtfChecks) {
        this.maxNumberOfEtfChecks = maxNumberOfEtfChecks;
    }

    public Long getIntervalBetweenEtfChecks() {
        return intervalBetweenEtfChecks;
    }

    public void setIntervalBetweenEtfChecks(Long intervalBetweenEtfChecks) {
        this.intervalBetweenEtfChecks = intervalBetweenEtfChecks;
    }

    public InspireValidatorUtils() {
    }

    /**
     * Check service status.
     *
     * @param endPoint the end point
     * @return true, if successful
     * @throws IOException
     */
    public boolean checkServiceStatus(ServiceContext context, String endPoint) throws IOException {

        HttpGet request = new HttpGet(endPoint + CheckStatus_URL);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        try (ClientHttpResponse response = this.execute(context, request)) {
            if (response.getStatusCode().value() == 200) {
                return true;
            } else {
                Log.warning(Log.SERVICE, "INSPIRE service not available: " + endPoint + CheckStatus_URL);
                return false;
            }
        } catch (Exception e) {
            Log.warning(Log.SERVICE, "Error calling INSPIRE service: " + endPoint, e);
            return false;
        }
    }

    /**
     * Upload metadata file.
     *
     * @param endPoint the end point
     * @param xml the xml
     * @return the string
     */
    private String uploadMetadataFile(ServiceContext context, String endPoint, InputStream xml) {

        HttpPost request = new HttpPost(endPoint + TestObjects_URL + "?action=upload");

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        ClientHttpResponse response = null;

        try {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("fileupload", xml, ContentType.TEXT_XML, "file.xml");
            HttpEntity entity = builder.build();

            request.setEntity(entity);

            response = this.execute(context, request);

            if (response.getStatusCode().value() == 200) {

                new BasicResponseHandler();
                String body = CharStreams.toString(new InputStreamReader(response.getBody()));
                JSONObject jsonRoot = new JSONObject(body);
                return jsonRoot.getJSONObject("testObject").getString("id");
            } else {
                Log.warning(Log.SERVICE,
                        "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + TestObjects_URL);
                return null;
            }
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Error calling INSPIRE service: " + endPoint, e);
            return null;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * Gets the tests.
     *
     * @param endPoint the end point
     * @param testsuite
     * @return the tests
     */
    private List<String> getTests(ServiceContext context, String endPoint, String testsuite) {
        if (testsuite == null) {
            testsuite = getDefaultTestSuite();
        }

        HttpGet request = new HttpGet(endPoint + ExecutableTestSuites_URL);

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);
        ClientHttpResponse response = null;

        try {
            String[] tests = testsuites.get(testsuite);

            response = this.execute(context, request);

            if (response.getStatusCode().value() == 200) {

                List<String> testList = new ArrayList<>();

                new BasicResponseHandler();
                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);

                JSONObject etfItemCollection = jsonRoot.getJSONObject("EtfItemCollection");
                JSONObject executableTestSuites = etfItemCollection.getJSONObject("executableTestSuites");
                JSONArray executableTestSuiteArray = executableTestSuites.getJSONArray("ExecutableTestSuite");

                for (int i = 0; i < executableTestSuiteArray.length(); i++) {
                    JSONObject test = executableTestSuiteArray.getJSONObject(i);

                    boolean ok = false;

                    for (String testToRun : tests) {
                        ok = ok || testToRun.equals(test.getString("label"));
                    }

                    if (ok) {
                        testList.add(test.getString("id"));
                    }
                }

                return testList;
            } else {
                Log.warning(Log.SERVICE,
                        "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + ExecutableTestSuites_URL);
                return null;
            }
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            return null;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * Test run.
     *
     * @param endPoint the end point
     * @param fileId the file id
     * @param testList the test list
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    private String testRun(ServiceContext context, String endPoint, String fileId, List<String> testList, String testTitle) {

        HttpPost request = new HttpPost(endPoint + TestRuns_URL);
        request.setHeader("Content-type", ACCEPT);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);
        ClientHttpResponse response = null;

        try {
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

            response = this.execute(context, request);

            if (response.getStatusCode().value() == 201) {

                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);
                String testId = jsonRoot.getJSONObject("EtfItemCollection").getJSONObject("testRuns").getJSONObject("TestRun")
                        .getString("id");

                return testId;
            } else {
                Log.warning(Log.SERVICE,
                        "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + TestRuns_URL);
                return null;
            }

        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            return null;
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * Checks if is ready.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @return true, if is ready
     * @throws Exception
     */
    public boolean isReady(ServiceContext context, String endPoint, String testId) throws Exception {
        if (testId == null) {
            return false;
        }

        HttpGet request = new HttpGet(endPoint + TestRuns_URL + "/" + testId + "/progress");

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        try (ClientHttpResponse response = this.execute(context, request)) {
            if (response.getStatusCode().value() == 200) {
                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);

                // Completed when estimated number of Test Steps is equal to completed Test Steps
                // Somehow this condition is necessary but not sufficient
                // so another check on real value of test is evaluated
                return jsonRoot.getInt("val") == jsonRoot.getInt("max") & isPassed(context, endPoint, testId) != null;

            } else if (response.getStatusCode().value() == 404) {

                throw new ResourceNotFoundException("Test not found");

            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for "
                        + TestRuns_URL + "?view=progress");
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw e;
        }

        return false;
    }

    /**
     * Checks if is passed.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @return the string
     * @throws Exception
     */
    public String isPassed(ServiceContext context, String endPoint, String testId) throws Exception {

        if (testId == null) {
            throw new Exception("");
        }

        HttpGet request = new HttpGet(endPoint + TestRuns_URL + "/" + testId);

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        try (ClientHttpResponse response = this.execute(context, request)) {

            if (response.getStatusCode().value() == 200) {

                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);

                try {
                    return jsonRoot.getJSONObject("EtfItemCollection").getJSONObject("testRuns").getJSONObject("TestRun")
                            .getString("status");
                } catch (JSONException e) {
                    return null;
                }

            } else if (response.getStatusCode().value() == 404) {

                throw new ResourceNotFoundException("Test not found");

            } else {
                Log.warning(Log.SERVICE, "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for "
                        + TestRuns_URL + "?view=progress");
            }
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw e;
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
    public String getReportUrl(String endPoint, String testId) {

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
     * Gets the report url in XML format.
     *
     * @param endPoint the end point
     * @param testId the test id
     * @return the report url
     */
    public static String getReportUrlXML(String endPoint, String testId) {

        return endPoint + TestRuns_URL + "/" + testId + ".xml";
    }

    /**
     * Submit file.
     *
     * @param record the record
     * @param testsuite
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    public String submitFile(ServiceContext context, String serviceEndpoint, InputStream record, String testsuite, String testTitle)
            throws IOException, JSONException {

        try {
            if (checkServiceStatus(context, serviceEndpoint)) {
                // Get the tests to execute
                List<String> tests = getTests(context, serviceEndpoint, testsuite);
                // Upload file to test
                String testFileId = uploadMetadataFile(context, serviceEndpoint, record);

                if (testFileId == null) {
                    Log.error(Log.SERVICE, "File not valid.", new IllegalArgumentException());
                    return null;
                }

                if (tests == null || tests.size() == 0) {
                    Log.error(Log.SERVICE,
                            "Default test sequence not supported. Check org.fao.geonet.api.records.editing.InspireValidatorUtils.TESTS_TO_RUN_TG13.",
                            new Exception());
                    return null;
                }
                // Return test id from Inspire service
                return testRun(context, serviceEndpoint, testFileId, tests, testTitle);

            } else {
                ServiceNotFoundEx ex = new ServiceNotFoundEx(serviceEndpoint);
                Log.error(Log.SERVICE, "Service unavailable.", ex);
                throw ex;
            }
        } finally {
            // client.close();
        }
    }

    public String retrieveReport(ServiceContext context, String endPoint) throws Exception {

        HttpGet request = new HttpGet(endPoint);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);


        try (ClientHttpResponse response = this.execute(context, request)) {
            return IOUtils.toString(response.getBody(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            Log.warning(Log.SERVICE, "Error calling INSPIRE service to retrieve the result report: " + endPoint, e);
            throw e;
        }
    }

    public void waitUntilReady(ServiceContext context, String endPoint, String testId) throws Exception {
        int checkCounter = 1;

        while (checkCounter++ <= maxNumberOfEtfChecks) {
            if (isReady(context, endPoint, testId)) {
               return;
            }

            Thread.sleep(intervalBetweenEtfChecks);
        }

        throw new Exception("ETF validation task hasn't finish after " + maxNumberOfEtfChecks + " checks.");
    }

    /**
     * Calculates the metadata validation status in GeoNetwork
     * based on the INSPIRE validator result.
     *  - UNDEFINED, INTERNAL_ERROR --> MetadataValidationStatus.NEVER_CALCULATED
     *  - PASSED, PASSED_MANUAL --> MetadataValidationStatus.VALID
     *  - NOT_APPLICABLE --> MetadataValidationStatus.DOES_NOT_APPLY
     *  - Other cases --> MetadataValidationStatus.INVALID
     *
     * @param validationStatus
     * @return
     */
    public MetadataValidationStatus calculateValidationStatus(String validationStatus) {
        boolean isUndefined = validationStatus.equalsIgnoreCase(TEST_STATUS_UNDEFINED);
        boolean isNotApplicable = validationStatus.equalsIgnoreCase(TEST_STATUS_NOT_APPLICABLE);
        boolean executed = !validationStatus.equalsIgnoreCase(TEST_STATUS_INTERNAL_ERROR);

        MetadataValidationStatus metadataValidationStatus;

        if (isNotApplicable) {
            metadataValidationStatus = MetadataValidationStatus.DOES_NOT_APPLY;
        } else if (!isUndefined && executed) {
            boolean isValid = validationStatus.equalsIgnoreCase(TEST_STATUS_PASSED) ||
                validationStatus.equalsIgnoreCase(TEST_STATUS_PASSED_MANUAL);

            metadataValidationStatus =
                (isValid?MetadataValidationStatus.VALID:MetadataValidationStatus.INVALID);
        } else {
            metadataValidationStatus = MetadataValidationStatus.NEVER_CALCULATED;
        }

        return metadataValidationStatus;
    }

    /**
     * Calculate the test suites to apply:
     *  - Checks if any rule for the schema, otherwise
     *  - checks if any rule in the schema dependency hierarchy.
     *
     * @param schemaid
     * @param metadataSchemaUtils
     * @return
     */
    public Map<String, String> calculateTestsuitesToApply(String schemaid,
                                                          IMetadataSchemaUtils metadataSchemaUtils) {
        Map<String, String> testsuitesConditions = getTestsuitesConditions();

        // Check for rules for the schema
        Map<String, String> testsuitesConditionsForSchema = testsuitesConditions.entrySet().stream()
            .filter(x-> x.getKey().split("::")[0].equalsIgnoreCase(schemaid))
            .collect(Collectors.toMap(map -> map.getKey().split("::")[1], map -> map.getValue()));

        // If no rules found, check the rules in the dependencies of the schema
        if (testsuitesConditionsForSchema.isEmpty()) {
            MetadataSchema metadataSchema = metadataSchemaUtils.getSchema(schemaid);

            Set<String> schemasProcessed = new HashSet<>();

            String schemaDependsOn = metadataSchema.getDependsOn();

            boolean conditionsFound = false;

            while (StringUtils.isNotEmpty(schemaDependsOn) &&
                !schemasProcessed.contains(schemaDependsOn) &&
                !conditionsFound) {

                schemasProcessed.add(schemaDependsOn);

                String schemaDependsOnFilter = schemaDependsOn;
                testsuitesConditionsForSchema = testsuitesConditions.entrySet().stream()
                    .filter(x-> x.getKey().split("::")[0].equalsIgnoreCase(schemaDependsOnFilter))
                    .collect(Collectors.toMap(map -> map.getKey().split("::")[1], map -> map.getValue()));

                conditionsFound = !testsuitesConditionsForSchema.isEmpty();

                // If no conditions found, check the schema dependency (if defined)
                if (!conditionsFound) {
                    metadataSchema = metadataSchemaUtils.getSchema(schemaDependsOn);
                    schemaDependsOn = metadataSchema.getDependsOn();
                }
            }
        }

        return testsuitesConditionsForSchema;
    }


    /**
     *  Executes the HttpUriRequest
     * @param request
     * @return response of execute method
     * @throws IOException
     */
    private ClientHttpResponse execute(ServiceContext context, HttpUriRequest request) throws IOException {

        final Function<HttpClientBuilder, Void> proxyConfiguration = new Function<HttpClientBuilder, Void>() {
            @Nullable
            @Override
            public Void apply(@Nonnull HttpClientBuilder input) {
                Lib.net.setupProxy(context, input, request.getURI().getHost());
                return null;                }
        };
        return requestFactory.execute(request, proxyConfiguration);
    }

}
