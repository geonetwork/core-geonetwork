/*
 * Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.security;

import org.jolokia.backend.BackendManager;
import org.jolokia.config.ConfigKey;
import org.jolokia.config.Configuration;
import org.jolokia.http.HttpRequestHandler;
import org.jolokia.restrictor.PolicyRestrictor;
import org.jolokia.util.HttpMethod;
import org.jolokia.util.LogHandler;
import org.jolokia.util.RequestType;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Checks the shipped <code>jolokia-access.xml</code> policy.
 *
 * <p>The Jolokia agent mounted at <code>/jolokia</code> is reachable by any
 * authenticated user (see the filter-chains in
 * <code>config-security-core.xml</code>), and the path based rules there cannot
 * express which MBeans may be read. That is the policy file's job, so these
 * tests pin it down: GeoNetwork's own task MBeans stay readable, and no
 * attribute value outside them can be read.
 *
 * <p>{@link #noCommandIsAllowedInGeneral()} guards a detail of Jolokia's policy
 * model that is easy to trip over - a command named in
 * <code>&lt;commands&gt;</code> is allowed "in general" and is no longer checked
 * against the <code>&lt;mbeans&gt;</code> allow list, so naming one there widens
 * access while the policy still reads as though it were restricted. Deleting the
 * empty element outright is worse still: it allows read, write and exec on every
 * MBean in the JVM. If that test fails, fix the policy rather than the test.
 */
public class JolokiaAccessPolicyTest {

    /**
     * Domain of GeoNetwork's own task MBeans: "geonetwork-" plus the catalogue
     * uuid. See MAnalyseProcess, BatchOpsMetadataReindexer,
     * MInspireEtfValidateProcess, EmptySlot and EmptySlotBatch.
     */
    private static final String GN_DOMAIN = "geonetwork-b1f5c0de-0000-4a1b-9c3d-2f6e7a8b9c01";

    private static final LogHandler SILENT = new LogHandler() {
        @Override
        public void debug(String message) {
            // Nothing to report while testing the policy.
        }

        @Override
        public void info(String message) {
            // Nothing to report while testing the policy.
        }

        @Override
        public void error(String message, Throwable t) {
            // Failures surface as an unexpected status in the response.
        }
    };

    private PolicyRestrictor restrictor;

    /** Non-null once {@link #get(String)} has built an agent, so it can be released. */
    private BackendManager backendManager;

    /** MBean registered by a test, unregistered again afterwards. */
    private ObjectName registeredProbe;

    @Before
    public void loadShippedPolicy() throws Exception {
        try (InputStream policy = getClass().getResourceAsStream("/jolokia-access.xml")) {
            assertNotNull("jolokia-access.xml must be on the classpath", policy);
            restrictor = new PolicyRestrictor(policy);
        }
    }

    @After
    public void releaseAgent() throws Exception {
        if (backendManager != null) {
            backendManager.destroy();
            backendManager = null;
        }
        if (registeredProbe != null) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(registeredProbe);
            registeredProbe = null;
        }
    }

    @Test
    public void geonetworkTaskMBeansRemainReadable() throws Exception {
        // The admin dashboard and the editor board poll these.
        assertReadAllowed(GN_DOMAIN + ":name=url-check,idx=1234", "AnalyseMdDate");
        assertReadAllowed(GN_DOMAIN + ":name=indexing-task,idx=7", "ToProcessCount");
        assertReadAllowed(GN_DOMAIN + ":name=batch-etf-inspire,idx=3", "MetadataAnalysed");
        // Placeholder slots, including the variant registered without a "name" key.
        assertReadAllowed(GN_DOMAIN + ":name=url-check,idx=empty-slot-1", "ObjectName");
        assertReadAllowed(GN_DOMAIN + ":idx=empty-slot-2", "ObjectName");
    }

    @Test
    public void mBeansOutsideGeonetworkDomainAreNotReadable() throws Exception {
        // JVM internals.
        assertReadDenied("java.lang:type=Runtime", "SystemProperties");
        assertReadDenied("java.lang:type=Runtime", "InputArguments");
        assertReadDenied("java.lang:type=Memory", "HeapMemoryUsage");
        assertReadDenied("java.lang:type=Threading", "AllThreadIds");
        assertReadDenied("java.lang:type=OperatingSystem", "Name");
        // Datasource MBeans.
        assertReadDenied("com.mchange.v2.c3p0:type=PooledDataSource,name=main", "properties");
        assertReadDenied("com.zaxxer.hikari:type=Pool (gn)", "PoolName");
        // Servlet container and JMX internals.
        assertReadDenied("Catalina:type=Server", "serverInfo");
        assertReadDenied("JMImplementation:type=MBeanServerDelegate", "MBeanServerId");
        // The hyphen in "geonetwork-*" is load bearing: neither the plain domain
        // used by GeoNetwork 3.x nor a longer prefix is covered by the allow list.
        assertReadDenied("geonetwork:name=indexing-task,idx=1", "ToProcessCount");
        assertReadDenied("geonetworkish:name=indexing-task,idx=1", "ToProcessCount");
    }

    /**
     * Guards the detail described in the class javadoc: no request type may be
     * allowed "in general", otherwise the {@code <mbeans>} allow list stops being
     * the thing that decides access.
     */
    @Test
    public void noCommandIsAllowedInGeneral() {
        for (RequestType type : RequestType.values()) {
            assertFalse("No command may be allowed in general, found: " + type,
                restrictor.isTypeAllowed(type));
        }
    }

    @Test
    public void writeAndExecAreDenied() throws Exception {
        assertFalse(restrictor.isAttributeWriteAllowed(
            new ObjectName(GN_DOMAIN + ":name=url-check,idx=1"), "AnalyseMdDate"));
        assertFalse(restrictor.isAttributeWriteAllowed(
            new ObjectName("java.lang:type=Memory"), "Verbose"));
        assertFalse(restrictor.isOperationAllowed(
            new ObjectName(GN_DOMAIN + ":name=url-check,idx=1"), "reset"));
        assertFalse(restrictor.isOperationAllowed(
            new ObjectName("java.lang:type=Memory"), "gc"));
    }

    /**
     * The policy deliberately carries no {@code <http>} section, which is a
     * trade-off rather than a no-op: see
     * {@link #patternReadEnumeratesMBeanNamesWithoutValues()} for what a POST
     * still reaches. Adding {@code <http><method>get</method></http>} would
     * close that, because the equivalent GET is already blocked by the
     * filter-chains, but it would also reject Jolokia's bulk request form,
     * which generic Jolokia clients use by default. Nothing in GeoNetwork uses
     * POST or the bulk form. This pins the current decision so it is not
     * quietly reversed.
     */
    @Test
    public void httpMethodIsNotRestricted() {
        assertTrue(restrictor.isHttpMethodAllowed(HttpMethod.GET));
        assertTrue(restrictor.isHttpMethodAllowed(HttpMethod.POST));
    }

    /**
     * The admin dashboard and the editor board do not read one MBean at a time:
     * they GET a single pattern, <code>geonetwork-&lt;uuid&gt;:name=&lt;task&gt;,idx=*</code>.
     *
     * <p>That pattern is itself denied by the allow list, because
     * {@link ObjectName#apply} is false whenever its argument is a pattern. The
     * request works only because Jolokia resolves the pattern to concrete MBean
     * names before it consults the restrictor (see
     * <code>ReadHandler.handleAllServersAtOnce</code>). None of the tests above
     * would notice if that ever stopped being true - they would all still pass
     * while both boards silently returned 403 - so drive a real request through
     * the agent's own handler.
     */
    @Test
    public void patternReadUsedByTheWebUiIsAllowed() throws Exception {
        registeredProbe = new ObjectName(GN_DOMAIN + ":name=indexing-task,idx=1");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.registerMBean(new TaskProbe(), registeredProbe);

        JSONObject response = get("/read/" + GN_DOMAIN + ":name=indexing-task,idx=*");

        assertEquals("Pattern read used by the web UI must be allowed: " + response,
            200, status(response));
        JSONObject matched = (JSONObject) response.get("value");
        assertEquals("Exactly the registered probe matched the pattern", 1, matched.size());
        JSONObject attributes = (JSONObject) matched.values().iterator().next();
        assertEquals(TaskProbe.TO_PROCESS_COUNT,
            ((Number) attributes.get("ToProcessCount")).intValue());
    }

    /**
     * The counterpart of {@link #patternReadUsedByTheWebUiIsAllowed()}: what the
     * allow list exists to stop, checked at the same layer. Before the allow list
     * was introduced this returned the full set of JVM system properties to any
     * authenticated user, the bare RegisteredUser profile included.
     */
    @Test
    public void jvmMBeanReadIsDeniedThroughTheAgent() {
        JSONObject response = get("/read/java.lang:type=Runtime/SystemProperties");

        assertEquals("Reading JVM internals must be forbidden: " + response,
            403, status(response));
    }

    /**
     * What the allow list does not bound, pinned at the agent's own handler.
     *
     * <p>GET and POST do not reach the same thing here. The filter-chains in
     * config-security-core.xml narrow GET to
     * <code>/jolokia/read/geonetwork-**</code>; any other GET path falls through
     * to coreFilterChain and is redirected to service-not-allowed. A POST
     * instead lands on the bare <code>/jolokia</code> chain and carries the
     * MBean in the request body, so it can name any MBean or pattern. POST is
     * the wider surface.
     *
     * <p>The allow list still withholds every attribute value there, which is
     * what {@link #jvmMBeanReadIsDeniedThroughTheAgent()} covers. What a POST
     * does still reach is enumeration: Jolokia's ReadHandler resolves an MBean
     * pattern and looks up attribute names <em>before</em> it consults the
     * restrictor, and with the <code>ignoreErrors</code> option set the fault
     * handler reports each denied attribute as an "ERROR: ... is forbidden"
     * string rather than failing the request. The response therefore lists
     * every MBean and attribute name in the JVM, with no values - much of what
     * denying the list and search commands is meant to withhold.
     *
     * <p>This pins that boundary in both directions. A Jolokia upgrade that
     * moves the restrictor check ahead of the pattern resolution would show up
     * here as a failure, and would be the signal that the caveat in
     * jolokia-access.xml can be dropped and the policy tightened.
     */
    @Test
    public void patternReadEnumeratesMBeanNamesWithoutValues() throws Exception {
        JSONObject response = post(
            "{\"type\":\"read\",\"mbean\":\"*:*\",\"config\":{\"ignoreErrors\":\"true\"}}");

        assertEquals("Pattern read is answered rather than refused: " + response,
            200, status(response));

        JSONObject enumerated = (JSONObject) response.get("value");
        JSONObject runtime = (JSONObject) enumerated.get("java.lang:type=Runtime");
        assertNotNull("MBeans outside the allow list are still named: " + enumerated.keySet(),
            runtime);
        assertTrue("Their attribute names are disclosed too: " + runtime.keySet(),
            runtime.containsKey("SystemProperties") && runtime.containsKey("InputArguments"));

        // The values are what the allow list withholds, and it withholds all of
        // them: every attribute of every MBean outside "geonetwork-*" reads back
        // as an error string, never as data.
        for (Object mBean : enumerated.values()) {
            for (Map.Entry<?, ?> attribute : ((Map<?, ?>) mBean).entrySet()) {
                assertTrue("No attribute value may leak, got " + attribute,
                    String.valueOf(attribute.getValue()).startsWith("ERROR: "));
            }
        }
    }

    // ======================================================================

    private void assertReadAllowed(String objectName, String attribute) throws Exception {
        assertTrue("Expected read access to " + objectName + "/" + attribute,
            restrictor.isAttributeReadAllowed(new ObjectName(objectName), attribute));
    }

    private void assertReadDenied(String objectName, String attribute) throws Exception {
        assertFalse("Expected read access to be denied for " + objectName + "/" + attribute,
            restrictor.isAttributeReadAllowed(new ObjectName(objectName), attribute));
    }

    /** GET the given agent path, the way the web UI does, and return the response. */
    private JSONObject get(String path) {
        return (JSONObject) agent().handleGetRequest(
            "http://localhost/geonetwork/jolokia" + path, path,
            Collections.<String, String[]>emptyMap());
    }

    /** POST the given request body, the form the web UI never uses, and return the response. */
    private JSONObject post(String body) throws IOException {
        return (JSONObject) agent().handlePostRequest(
            "http://localhost/geonetwork/jolokia",
            new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "utf-8",
            Collections.<String, String[]>emptyMap());
    }

    /** An agent serving the shipped policy. */
    private HttpRequestHandler agent() {
        if (backendManager != null) {
            // An agent holds MBeans of its own, so never leave two of them around.
            backendManager.destroy();
        }
        Configuration config = new Configuration(ConfigKey.AGENT_ID, "jolokia-access-policy-test");
        backendManager = new BackendManager(config, SILENT, restrictor);
        return new HttpRequestHandler(config, backendManager, SILENT);
    }

    private int status(JSONObject response) {
        return ((Number) response.get("status")).intValue();
    }

    public interface TaskProbeMBean {
        int getToProcessCount();
    }

    /** Stand-in for BatchOpsMetadataReindexer, which the web UI polls by pattern. */
    public static class TaskProbe implements TaskProbeMBean {
        static final int TO_PROCESS_COUNT = 42;

        @Override
        public int getToProcessCount() {
            return TO_PROCESS_COUNT;
        }
    }
}
