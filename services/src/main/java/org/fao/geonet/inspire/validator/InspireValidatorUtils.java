/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.inspire.validator;

import com.google.common.base.Function;
import com.google.common.io.CharStreams;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.BasicResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.exceptions.ServiceNotFoundEx;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpResponse;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_INSPIRE_REMOTE_VALIDATION_APIKEY;

/**
 * Utility class to access methods in Inspire Service.
 *
 * Based on ETF Web API v.2
 * See https://docs.etf-validator.net/v2.0/Developer_manuals/WEB-API.html
 */
public class InspireValidatorUtils {

    @Autowired
    private GeonetHttpRequestFactory requestFactory;

    @Autowired
    SettingManager settingManager;

    /**
     * The Constant USER_AGENT.
     */
    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * The Constant ACCEPT.
     */
    private static final String ACCEPT = "application/json";

    /**
     * The Constant CheckStatus_URL.
     */
    private static final String CHECKSTATUS_URL = "/v2/status";

    /**
     * The Constant ExecutableTestSuites_URL.
     */
    private static final String EXECUTABLETESTSUITES_URL = "/v2/ExecutableTestSuites";

    /**
     * The Constant TestObjects_URL.
     */
    private static final String TESTOBJECTS_URL = "/v2/TestObjects";

    /**
     * The Constant TestRuns_URL.
     */
    private static final String TESTRUNS_URL = "/v2/TestRuns";

    /**
     * Test status PASSED.
     */
    public static final String TEST_STATUS_PASSED = "PASSED";

    /**
     * Test status FAILED.
     */
    public static final String TEST_STATUS_FAILED = "FAILED";

    /**
     * Test status PASSED_MANUAL.
     */
    public static final String TEST_STATUS_PASSED_MANUAL = "PASSED_MANUAL";

    /**
     * Test status UNDEFINED.
     */
    public static final String TEST_STATUS_UNDEFINED = "UNDEFINED";

    /**
     * Test status NOT_APPLICABLE.
     */
    public static final String TEST_STATUS_NOT_APPLICABLE = "NOT_APPLICABLE";

    /**
     * Test status INTERNAL_ERROR.
     */
    public static final String TEST_STATUS_INTERNAL_ERROR = "INTERNAL_ERROR";

    @Value("#{validatorAdditionalConfig['defaultTestSuite']}")
    public String defaultTestSuite;

    // Using @Resource instead of @Autowired+@Qualifier for injecting java.util.Map instances
    // Check https://stackoverflow.com/a/13914052/1140558
    @Resource(name = "inspireEtfValidatorTestsuites")
    private Map<String, String[]> testsuites;

    @Resource(name = "inspireEtfValidatorTestsuitesConditions")
    private Map<String, String> testsuitesConditions;

    @Value("#{validatorAdditionalConfig['maxNumberOfEtfChecks']}")
    private Integer maxNumberOfEtfChecks;

    @Value("#{validatorAdditionalConfig['intervalBetweenEtfChecks']}")
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

    public Map<String, String[]> getTestsuites() {
        return testsuites;
    }

    public void setTestsuitesConditions(Map<String, String> testsuitesConditions) {
        this.testsuitesConditions = testsuitesConditions;
    }

    public Map<String, String> getTestsuitesConditions() {
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
    public boolean checkServiceStatus(ServiceContext context, String endPoint) {
        HttpGet request = new HttpGet(StringUtils.removeEnd(endPoint, "/") + CHECKSTATUS_URL);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        try (ClientHttpResponse response = this.execute(context, request)) {
            if (response.getStatusCode().value() == HttpStatus.SC_OK) {
                return true;
            } else {
                Log.warning(Log.SERVICE, "INSPIRE service not available: " + endPoint + CHECKSTATUS_URL);
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
     * @param xml      the xml
     * @return the string
     */
    private String uploadMetadataFile(ServiceContext context, String endPoint, InputStream xml) throws InspireValidatorException {

        HttpPost request = new HttpPost(StringUtils.removeEnd(endPoint, "/") + TESTOBJECTS_URL + "?action=upload");

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        ClientHttpResponse response = null;

        try {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("fileupload", xml, ContentType.TEXT_XML, "file.xml");
            HttpEntity entity = builder.build();

            request.setEntity(entity);

            response = this.execute(context, request);

            if (response.getStatusCode().value() == HttpStatus.SC_OK) {

                new BasicResponseHandler();
                String body = CharStreams.toString(new InputStreamReader(response.getBody()));
                JSONObject jsonRoot = new JSONObject(body);
                return jsonRoot.getJSONObject("testObject").getString("id");
            } else {
                Log.error(Log.SERVICE,
                    "INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + TESTOBJECTS_URL);
                throw new InspireValidatorException(String.format("INSPIRE service - error uploading file: %s, error code: %d", response.getStatusText(), response.getStatusCode().value()));
            }
        } catch (InspireValidatorException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Error calling INSPIRE service: " + endPoint, e);
            throw new InspireValidatorException(String.format("Error calling INSPIRE service: %s, %s", endPoint, e.getMessage()));
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * Gets the tests.
     *
     * @param endPoint  the end point
     * @param testsuite
     * @return the tests
     */
    private List<String> getTests(ServiceContext context, String endPoint, String testsuite) {
        if (testsuite == null) {
            testsuite = getDefaultTestSuite();
        }

        HttpGet request = new HttpGet(StringUtils.removeEnd(endPoint, "/") + EXECUTABLETESTSUITES_URL);

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        try(ClientHttpResponse response = this.execute(context, request)) {
            String[] tests = testsuites.get(testsuite);

            if (response.getStatusCode().value() == HttpStatus.SC_OK) {

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
                    "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + EXECUTABLETESTSUITES_URL);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            return Collections.emptyList();
        }
    }

    /**
     * Test run.
     *
     *
     * @param endPoint the end point
     * @param fileId   the file id
     * @param testList the test list
     * @return the string
     * @throws IOException   Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    private String testRun(ServiceContext context, String endPoint, String fileId, List<String> testList, String testTitle) throws InspireValidatorException {

        HttpPost request = new HttpPost(StringUtils.removeEnd(endPoint, "/") + TESTRUNS_URL);
        request.setHeader("Content-type", ACCEPT);
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);
        addApiKey(request);

        ClientHttpResponse response = null;

        try {
            JSONObject json = new JSONObject();
            JSONArray tests = new JSONArray();
            JSONObject arguments = new JSONObject();
            JSONObject testObject = new JSONObject();

            json.put("label", "TEST " + testTitle + " - " + System.currentTimeMillis());
            json.put("executableTestSuiteIds", tests);
            json.put("arguments", arguments);
            json.put("testObject", testObject);

            for (String test : testList) {
                tests.put(test);
            }

            arguments.put("files_to_test", ".*");
            arguments.put("tests_to_execute", ".*");

            if (fileId.startsWith("http")) {
                JSONObject resourceObject = new JSONObject();
                resourceObject.put("data", fileId);
                testObject.put("resources", resourceObject);
            } else {
                testObject.put("id", fileId);
            }

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);


            response = this.execute(context, request);

            if (response.getStatusCode().value() == HttpStatus.SC_CREATED) {

                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);
                String testId = jsonRoot.getJSONObject("EtfItemCollection").getJSONObject("testRuns").getJSONObject("TestRun")
                    .getString("id");

                return testId;
            } else {
                Log.warning(Log.SERVICE,
                    "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + TESTRUNS_URL);
                throw new InspireValidatorException(String.format("Error while creating test on validator side. Status is: %d (%s). Error: %s",
                    response.getStatusCode().value(),
                    response.getStatusText(),
                    response.getBody() != null
                        ? CharStreams.toString(new InputStreamReader(response.getBody())) : ""
                    ));
            }

        } catch (InspireValidatorException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw new InspireValidatorException(String.format("Error calling INSPIRE service: %s, %s", endPoint, e.getMessage()));
        } finally {
            IOUtils.closeQuietly(response);
        }
    }

    /**
     * See https://github.com/INSPIRE-MIF/helpdesk-validator/issues/594
     */
    private void addApiKey(HttpUriRequestBase request) {
        String apikey =
            settingManager.getValue(SYSTEM_INSPIRE_REMOTE_VALIDATION_APIKEY);
        if (StringUtils.isNotEmpty(apikey)) {
            request.addHeader("X-API-key", apikey);
        }
    }

    /**
     * Checks if is ready.
     *
     * @param endPoint the end point
     * @param testId   the test id
     * @return true, if is ready
     * @throws Exception
     */
    public boolean isReady(ServiceContext context, String endPoint, String testId) throws ResourceNotFoundException, InspireValidatorException {
        if (testId == null) {
            return false;
        }

        HttpGet request = new HttpGet(StringUtils.removeEnd(endPoint, "/") + TESTRUNS_URL + "/" + testId + "/progress");

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);
        addApiKey(request);

        try (ClientHttpResponse response = this.execute(context, request)) {
            if (response.getStatusCode().value() == HttpStatus.SC_OK) {
                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);

                // Completed when estimated number of Test Steps is equal to completed Test Steps
                // Somehow this condition is necessary but not sufficient
                // so another check on real value of test is evaluated
                return jsonRoot.getInt("val") == jsonRoot.getInt("max") && isPassed(context, endPoint, testId) != null;
            } else if (response.getStatusCode().value() == HttpStatus.SC_NOT_FOUND) {
                throw new ResourceNotFoundException("Test not found");
            } else {
                Log.warning(Log.SERVICE,
                    "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + TESTRUNS_URL
                        + "?view=progress");
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw new InspireValidatorException(String.format("Exception in INSPIRE service: %s, %s", endPoint, e));
        }

        return false;
    }

    /**
     * Checks if is passed.
     *
     * @param endPoint the end point
     * @param testId   the test id
     * @return the string
     * @throws Exception
     */
    public String isPassed(ServiceContext context, String endPoint, String testId) throws InspireValidatorException {

        if (testId == null) {
            throw new InspireValidatorException(String.format("Exception in INSPIRE service: %s, test not provided", endPoint));
        }

        HttpGet request = new HttpGet(StringUtils.removeEnd(endPoint, "/") + TESTRUNS_URL + "/" + testId);

        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);
        addApiKey(request);

        try (ClientHttpResponse response = this.execute(context, request)) {
            if (response.getStatusCode().value() == HttpStatus.SC_OK) {
                String body = CharStreams.toString(new InputStreamReader(response.getBody()));

                JSONObject jsonRoot = new JSONObject(body);

                try {
                    return jsonRoot.getJSONObject("EtfItemCollection").getJSONObject("testRuns").getJSONObject("TestRun")
                        .getString("status");
                } catch (JSONException e) {
                    return null;
                }
            } else if (response.getStatusCode().value() == HttpStatus.SC_NOT_FOUND) {
                throw new ResourceNotFoundException("Test not found");
            } else {
                Log.warning(Log.SERVICE,
                    "WARNING: INSPIRE service HTTP response: " + response.getStatusCode().value() + " for " + TESTRUNS_URL
                        + "?view=progress");
            }
        } catch (Exception e) {
            Log.error(Log.SERVICE, "Exception in INSPIRE service: " + endPoint, e);
            throw new InspireValidatorException(String.format("Exception in INSPIRE service: %s, %s", endPoint, e));
        }

        return null;
    }

    /**
     * Gets the report url.
     *
     * @param endPoint the end point
     * @param testId   the test id
     * @return the report url
     */
    public String getReportUrl(String endPoint, String testId) {

        return endPoint + TESTRUNS_URL + "/" + testId + ".html";
    }

    /**
     * Gets the report url in JSON format.
     *
     * @param endPoint the end point
     * @param testId   the test id
     * @return the report url
     */
    public static String getReportUrlJSON(String endPoint, String testId) {

        return endPoint + TESTRUNS_URL + "/" + testId + ".json";
    }

    /**
     * Gets the report url in XML format.
     *
     * @param endPoint the end point
     * @param testId   the test id
     * @return the report url
     */
    public static String getReportUrlXML(String endPoint, String testId) {

        return endPoint + TESTRUNS_URL + "/" + testId + ".xml";
    }

    /**
     * Submit file to the external ETF validator.
     *
     * @param metadataRecord    the metadata record
     * @param testsuite
     * @return the string
     * @throws IOException   Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    public String submitFile(ServiceContext context, String serviceEndpoint, String serviceQueryEndpoint, InputStream metadataRecord, String testsuite, String testTitle)
        throws InspireValidatorException {

        if (checkServiceStatus(context, serviceQueryEndpoint)) {
            // Get the tests to execute
            List<String> tests = getTests(context, serviceQueryEndpoint, testsuite);
            // Upload file to test
            String testFileId = uploadMetadataFile(context, serviceQueryEndpoint, metadataRecord);

            if (testFileId == null) {
                Log.error(Log.SERVICE, "File not valid.", new IllegalArgumentException());
                return null;
            }

            if (tests.isEmpty()) {
                Log.error(Log.SERVICE,
                    "Default test sequence not supported. Check org.fao.geonet.inspire.validator.InspireValidatorUtils.TESTS_TO_RUN_TG13.",
                    new Exception());
                return null;
            }
            // Return test id from Inspire service
            return testRun(context, serviceEndpoint, testFileId, tests, testTitle);

        } else {
            Log.error(Log.SERVICE, String.format("INSPIRE service end-point unavailable: %s", serviceEndpoint), new InspireValidatorException());
            throw new InspireValidatorException(String.format("INSPIRE service end-point unavailable: %s", serviceEndpoint));
        }
    }

    /**
     * Submit URL to the external ETF validator.
     *
     * @param testsuite
     * @return the string
     * @throws IOException   Signals that an I/O exception has occurred.
     * @throws JSONException the JSON exception
     */
    public String submitUrl(ServiceContext context, String serviceEndpoint, String serviceEndpointQuery, String getRecordById, String testsuite, String testTitle)
        throws InspireValidatorException {

        if (checkServiceStatus(context, serviceEndpointQuery)) {
            // Get the tests to execute
            List<String> tests = getTests(context, serviceEndpoint, testsuite);
            if (tests.isEmpty()) {
                Log.error(Log.SERVICE,
                    "Default test sequence not supported. Check org.fao.geonet.inspire.validator.InspireValidatorUtils.TESTS_TO_RUN_TG13.",
                    new Exception());
                return null;
            }
            // Return test id from Inspire service
            return testRun(context, serviceEndpoint, getRecordById, tests, testTitle);

        } else {
            ServiceNotFoundEx ex = new ServiceNotFoundEx(serviceEndpoint);
            Log.error(Log.SERVICE, "Service unavailable.", ex);
            throw ex;
        }
    }

    public String retrieveReport(ServiceContext context, String endPoint) throws InspireValidatorException {

        HttpGet request = new HttpGet(endPoint);

        // add request header
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Accept", ACCEPT);

        try (ClientHttpResponse response = this.execute(context, request)) {
            return IOUtils.toString(response.getBody(), StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            Log.warning(Log.SERVICE, "Error calling INSPIRE service to retrieve the result report: " + endPoint, e);
            throw new InspireValidatorException(String.format("Error calling INSPIRE service to retrieve the result report: %s, %s",  endPoint, e.getMessage()));
        }
    }

    public void waitUntilReady(ServiceContext context, String endPoint, String testId) throws
        ResourceNotFoundException, InterruptedException, InspireValidatorException {
        int checkCounter = 1;

        while (checkCounter++ <= maxNumberOfEtfChecks) {
            if (isReady(context, endPoint, testId)) {
                return;
            }

            Thread.sleep(intervalBetweenEtfChecks);
        }

        throw new InspireValidatorException(String.format("ETF validation task hasn't finish after %d checks.", maxNumberOfEtfChecks));
    }

    /**
     * Calculates the metadata validation status in GeoNetwork
     * based on the INSPIRE validator result.
     * - UNDEFINED, INTERNAL_ERROR --> MetadataValidationStatus.NEVER_CALCULATED
     * - PASSED, PASSED_MANUAL --> MetadataValidationStatus.VALID
     * - NOT_APPLICABLE --> MetadataValidationStatus.DOES_NOT_APPLY
     * - Other cases --> MetadataValidationStatus.INVALID
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
            boolean isValid =
                validationStatus.equalsIgnoreCase(TEST_STATUS_PASSED) || validationStatus.equalsIgnoreCase(TEST_STATUS_PASSED_MANUAL);

            metadataValidationStatus = (isValid ? MetadataValidationStatus.VALID : MetadataValidationStatus.INVALID);
        } else {
            metadataValidationStatus = MetadataValidationStatus.NEVER_CALCULATED;
        }

        return metadataValidationStatus;
    }

    /**
     * Calculate the test suites to apply:
     * - Checks if any rule for the schema, otherwise
     * - checks if any rule in the schema dependency hierarchy.
     *
     * @param schemaid
     * @param metadataSchemaUtils
     * @return
     */
    public Map<String, String> calculateTestsuitesToApply(String schemaid, IMetadataSchemaUtils metadataSchemaUtils) {
        Map<String, String> allTestsuitesConditions = getTestsuitesConditions();

        // Check for rules for the schema
        Map<String, String> testsuitesConditionsForSchema = allTestsuitesConditions.entrySet().stream()
            .filter(x -> x.getKey().split("::")[0].equalsIgnoreCase(schemaid))
            .collect(Collectors.toMap(map -> map.getKey().split("::")[1], Map.Entry::getValue));

        // If no rules found, check the rules in the dependencies of the schema
        if (testsuitesConditionsForSchema.isEmpty()) {
            MetadataSchema metadataSchema = metadataSchemaUtils.getSchema(schemaid);

            Set<String> schemasProcessed = new HashSet<>();

            String schemaDependsOn = metadataSchema.getDependsOn();

            boolean conditionsFound = false;

            while (StringUtils.isNotEmpty(schemaDependsOn) && !schemasProcessed.contains(schemaDependsOn) && !conditionsFound) {

                schemasProcessed.add(schemaDependsOn);

                String schemaDependsOnFilter = schemaDependsOn;
                testsuitesConditionsForSchema = allTestsuitesConditions.entrySet().stream()
                    .filter(x -> x.getKey().split("::")[0].equalsIgnoreCase(schemaDependsOnFilter))
                    .collect(Collectors.toMap(map -> map.getKey().split("::")[1], Map.Entry::getValue));

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
     * Executes the HttpUriRequest
     *
     * @param request
     * @return response of execute method
     * @throws IOException
     */
    private ClientHttpResponse execute(ServiceContext context, HttpUriRequest request) throws IOException {

        final Function<HttpClientBuilder, Void> proxyConfiguration = new Function<HttpClientBuilder, Void>() {
            @Nullable @Override public Void apply(@Nonnull HttpClientBuilder input) {
                Lib.net.setupProxy(context, input, request.getURI().getHost());
                return null;
            }
        };
        return requestFactory.execute(request, proxyConfiguration);
    }

}
