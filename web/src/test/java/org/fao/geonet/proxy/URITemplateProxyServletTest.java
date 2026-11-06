/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.proxy;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Checks that the hosts excluded from proxy access are recognised whichever way they are written.
 */
public class URITemplateProxyServletTest {

    /**
     * The default of the proxy.excludeHosts property, kept in sync with the value in the root pom.xml.
     */
    private static final String DEFAULT_EXCLUDE_HOSTS =
        "^(localhost|127\\..*|0\\..*|10\\..*|172\\.(1[6-9]|2[0-9]|3[01])\\..*|192\\.168\\..*|169\\.254\\..*|100\\.(6[4-9]|[7-9][0-9]|1[01][0-9]|12[0-7])\\..*|198\\.1[89]\\..*|2(2[4-9]|3[0-9])\\..*|255\\.255\\.255\\.255|.*\\.local|.*\\.localhost|0:0:0:0:0:0:.*|::1|::|64:ff9b:.*|f[cd][0-9a-f]{2}:.*|fe[89ab][0-9a-f]:.*|fe[c-f][0-9a-f]:.*)$";

    private URITemplateProxyServlet servlet;

    @Before
    public void setUp() {
        servlet = new URITemplateProxyServlet();
        servlet.setExcludeHosts(DEFAULT_EXCLUDE_HOSTS);
    }

    /**
     * Read the host the way the servlet does, so the tests exercise the string it really matches on.
     */
    private String hostOf(String url) throws Exception {
        return new URI(url).getHost();
    }

    private void assertExcluded(String url) throws Exception {
        assertTrue(url + " should be excluded", servlet.isExcludedHost(hostOf(url)));
    }

    private void assertNotExcluded(String url) throws Exception {
        assertFalse(url + " should not be excluded", servlet.isExcludedHost(hostOf(url)));
    }

    @Test
    public void uriGetHostKeepsTheBracketsOfAnIpv6Literal() throws Exception {
        // The reason the bare IPv6 entries of the pattern never matched anything.
        assertEquals("[::1]", hostOf("http://[::1]/"));
    }

    @Test
    public void ipv6LoopbackIsExcludedHoweverItIsSpelled() throws Exception {
        assertExcluded("http://[::1]/");
        assertExcluded("http://[0:0:0:0:0:0:0:1]/");
        assertExcluded("http://[0000:0000:0000:0000:0000:0000:0000:0001]/");
        assertExcluded("http://[::0001]/");
    }

    @Test
    public void ipv6UnspecifiedIsExcluded() throws Exception {
        assertExcluded("http://[::]/");
        assertExcluded("http://[0:0:0:0:0:0:0:0]/");
    }

    @Test
    public void ipv4MappedLoopbackIsExcluded() throws Exception {
        // Canonicalises to 127.0.0.1, which the existing 127\..* entry already covers.
        assertExcluded("http://[::ffff:127.0.0.1]/");
    }

    @Test
    public void ipv4LoopbackIsStillExcluded() throws Exception {
        assertExcluded("http://127.0.0.1/");
        assertExcluded("http://127.1.2.3/");
        assertExcluded("http://localhost/");
    }

    @Test
    public void privateRangesAreExcluded() throws Exception {
        assertExcluded("http://10.0.0.5/");
        assertExcluded("http://10.255.255.255/");
        assertExcluded("http://172.16.4.9/");
        assertExcluded("http://172.31.255.254/");
        assertExcluded("http://192.168.1.1/");
        assertExcluded("http://192.168.117.2/");
    }

    @Test
    public void linkLocalRangeIsExcluded() throws Exception {
        assertExcluded("http://169.254.169.254/");
        assertExcluded("http://169.254.0.1/");
    }

    @Test
    public void addressesJustOutsideThePrivateRangesAreNotExcluded() throws Exception {
        // 172.16/12 runs from 172.16 to 172.31, the neighbours of that range stay reachable.
        assertNotExcluded("http://172.15.0.1/");
        assertNotExcluded("http://172.32.0.1/");
        assertNotExcluded("http://11.0.0.1/");
        assertNotExcluded("http://9.255.255.255/");
        assertNotExcluded("http://192.167.0.1/");
        assertNotExcluded("http://192.169.0.1/");
        assertNotExcluded("http://169.253.0.1/");
        assertNotExcluded("http://169.255.0.1/");
    }

    @Test
    public void addressWrittenAsASingleNumberIsExcluded() throws Exception {
        assertExcluded("http://2130706433/");   // 127.0.0.1
        assertExcluded("http://3232261122/");   // 192.168.100.2
        assertExcluded("http://2852039166/");   // 169.254.169.254
        assertExcluded("http://167772161/");    // 10.0.0.1
        assertExcluded("http://4294967295/");   // 255.255.255.255
    }

    @Test
    public void addressWrittenAsASingleNumberIsNotExcludedWhenItIsPublic() throws Exception {
        assertNotExcluded("http://134744072/");  // 8.8.8.8
    }

    @Test
    public void aNumberTooLargeForAnAddressIsLeftAlone() {
        // Not an address, so nothing is added and nothing throws.
        assertEquals(1, servlet.getHostVariants("4294967296").size());
        assertEquals(1, servlet.getHostVariants("99999999999999999999").size());
    }

    @Test
    public void hostVariantsCarryTheDottedFormOfANumericAddress() {
        assertTrue(servlet.getHostVariants("2130706433").contains("127.0.0.1"));
        assertTrue(servlet.getHostVariants("2130706433").contains("2130706433"));
    }

    @Test
    public void ipv6UniqueLocalRangeIsExcluded() throws Exception {
        assertExcluded("http://[fc00::1]/");
        assertExcluded("http://[fd00::1]/");
        assertExcluded("http://[fd12:3456:789a::1]/");
        assertExcluded("http://[fdff:ffff::1]/");
        assertExcluded("http://[FD00::1]/");
    }

    @Test
    public void ipv6LinkLocalRangeIsExcluded() throws Exception {
        assertExcluded("http://[fe80::1]/");
        assertExcluded("http://[fe8f::1]/");
        assertExcluded("http://[febf::1]/");
    }

    @Test
    public void ipv6SiteLocalRangeIsExcluded() throws Exception {
        // Deprecated by RFC 3879, still private where it is in use.
        assertExcluded("http://[fec0::1]/");
        assertExcluded("http://[feff::1]/");
    }

    @Test
    public void addressesJustOutsideThePrivateIpv6RangesAreNotExcluded() throws Exception {
        assertNotExcluded("http://[fbff::1]/");   // below fc00::/7
        assertNotExcluded("http://[fe00::1]/");   // below fe80::/10
        assertNotExcluded("http://[fe7f::1]/");   // below fe80::/10
    }

    @Test
    public void documentedIpv6EntriesCoverEverySpellingOfTheAddress() throws Exception {
        // The example given in the maintainer guide: one host, and one whole /48 prefix, both
        // written in the canonical form of the address.
        servlet.setExcludeHosts("^(2001:db8:1:0:0:0:0:10|2001:db8:2:.*)$");

        assertExcluded("http://[2001:db8:1::10]/");
        assertExcluded("http://[2001:db8:1:0:0:0:0:10]/");
        assertExcluded("http://[2001:0db8:0001::0010]/");
        assertExcluded("http://[2001:DB8:1::10]/");

        assertExcluded("http://[2001:db8:2::1]/");
        assertExcluded("http://[2001:db8:2:ffff::99]/");
    }

    @Test
    public void documentedIpv6EntriesLeaveTheNeighbouringAddressesAlone() throws Exception {
        servlet.setExcludeHosts("^(2001:db8:1:0:0:0:0:10|2001:db8:2:.*)$");

        assertNotExcluded("http://[2001:db8:1::11]/");
        // The colon at the end of the prefix keeps 2a and 20 out of a /48 written for 2.
        assertNotExcluded("http://[2001:db8:2a::1]/");
        assertNotExcluded("http://[2001:db8:20::1]/");
        assertNotExcluded("http://[2001:db8:3::1]/");
    }

    @Test
    public void anExcludedHostIsRecognisedInAnyCase() throws Exception {
        // A host name is not case sensitive, and URI.getHost() keeps the case the caller wrote.
        assertExcluded("http://LOCALHOST/");
        assertExcluded("http://LocalHost/");
        assertExcluded("http://MYHOST.LOCAL/");
        assertExcluded("http://X.LOCALHOST/");
    }

    @Test
    public void anEntryWrittenInUpperCaseAlsoMatches() {
        servlet.setExcludeHosts("^(INTERNAL\\.EXAMPLE\\.ORG)$");
        assertTrue(servlet.isExcludedHost("internal.example.org"));
    }

    @Test
    public void ipv4CompatibleIpv6IsExcluded() throws Exception {
        // ::/96 carries an IPv4 address in its low bits, deprecated but still spellable.
        assertExcluded("http://[::7f00:1]/");
        assertExcluded("http://[::a00:1]/");
    }

    @Test
    public void nat64PrefixIsExcluded() throws Exception {
        assertExcluded("http://[64:ff9b::7f00:1]/");
        assertNotExcluded("http://[64:ff9c::1]/");
    }

    @Test
    public void carrierGradeNatBenchmarkAndMulticastRangesAreExcluded() throws Exception {
        assertExcluded("http://100.64.0.1/");
        assertExcluded("http://100.127.255.255/");
        assertExcluded("http://198.18.0.1/");
        assertExcluded("http://198.19.255.1/");
        assertExcluded("http://224.0.0.1/");
        assertExcluded("http://239.255.255.255/");
    }

    @Test
    public void addressesJustOutsideTheAddedRangesAreNotExcluded() throws Exception {
        assertNotExcluded("http://100.63.255.255/");
        assertNotExcluded("http://100.128.0.1/");
        assertNotExcluded("http://198.17.0.1/");
        assertNotExcluded("http://198.20.0.1/");
        assertNotExcluded("http://223.255.255.255/");
        assertNotExcluded("http://240.0.0.1/");
        // The multicast entry must not swallow 2.x, 22.x or 23.x.
        assertNotExcluded("http://2.2.2.2/");
        assertNotExcluded("http://22.5.1.1/");
        assertNotExcluded("http://23.1.1.1/");
    }

    /**
     * Checks the value as the target is built from it, then through the exclusion list.
     */
    private boolean isQueryExcluded(String queryString) {
        String url = servlet.getRequestedUrl(queryString);
        if (url == null) {
            return false;
        }
        try {
            String host = new URI(url).getHost();
            return host == null || servlet.isExcludedHost(host);
        } catch (java.net.URISyntaxException e) {
            return true;
        }
    }

    @Test
    public void theUrlParameterIsReadAsTheTargetUsesIt() {
        assertEquals("http://example.com/", servlet.getRequestedUrl("url=http%3A%2F%2Fexample.com%2F"));
        // Repeated, the target joins them with a comma, so that is what has to be checked.
        assertEquals("http://a,@127.0.0.1/", servlet.getRequestedUrl("url=http%3A%2F%2Fa&url=%40127.0.0.1%2F"));
        assertEquals(null, servlet.getRequestedUrl("other=1"));
        assertEquals(null, servlet.getRequestedUrl(null));
    }

    @Test
    public void aSecondUrlParameterCannotSlipPastTheFirst() {
        // The first value names a host no entry excludes, the join denotes the loopback interface.
        assertTrue(isQueryExcluded("url=http%3A%2F%2Fa&url=%40127.0.0.1%2F"));
        assertTrue(isQueryExcluded("url=http%3A%2F%2Fuser&url=%40localhost%2F"));
        assertTrue(isQueryExcluded("url=http%3A%2F%2Fa&url=%40%5B%3A%3A1%5D%2F"));
        assertTrue(isQueryExcluded("url=http%3A%2F%2Fa&url=%402130706433%2F"));
    }

    @Test
    public void aSingleUrlParameterIsUnaffected() {
        assertFalse(isQueryExcluded("url=http%3A%2F%2Fexample.com%2F"));
        assertTrue(isQueryExcluded("url=http%3A%2F%2F127.0.0.1%2F"));
    }

    /**
     * Drive the redirect strategy with a 302 towards the given location.
     */
    private URI followRedirectTo(String location) throws Exception {
        DefaultRedirectStrategy strategy = servlet.createRedirectStrategy();
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 302, "Found");
        response.addHeader("Location", location);
        return strategy.getLocationURI(
            new BasicHttpRequest("GET", "http://example.com/"), response, HttpClientContext.create());
    }

    private void assertRedirectRefused(String location) throws Exception {
        try {
            URI followed = followRedirectTo(location);
            fail("a redirect to " + location + " should be refused, it returned " + followed);
        } catch (ProtocolException expected) {
            // The destination is not one the caller could have named directly.
        }
    }

    @Test
    public void aRedirectToAnAllowedHostIsFollowed() throws Exception {
        assertEquals(new URI("http://example.org/data"), followRedirectTo("http://example.org/data"));
    }

    @Test
    public void aRedirectToAnExcludedHostIsRefused() throws Exception {
        assertRedirectRefused("http://127.0.0.1/");
        assertRedirectRefused("http://localhost/");
        assertRedirectRefused("http://LOCALHOST/");
        assertRedirectRefused("http://192.168.1.1/");
        assertRedirectRefused("http://169.254.169.254/latest/meta-data/");
        assertRedirectRefused("http://[::1]/");
        assertRedirectRefused("http://[fd00::1]/");
        assertRedirectRefused("http://2130706433/");
    }

    @Test
    public void aRedirectToADisallowedPortIsRefused() throws Exception {
        assertRedirectRefused("http://example.org:8081/");
    }

    @Test
    public void aRedirectToAnAllowedPortIsFollowed() throws Exception {
        assertEquals(new URI("https://example.org:443/x"), followRedirectTo("https://example.org:443/x"));
    }

    /**
     * Ask the redirect strategy whether it follows a 302 from the given URL to the given location.
     */
    private boolean isRedirectFollowed(String from, String location) throws Exception {
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 302, "Found");
        response.addHeader("Location", location);
        return servlet.createRedirectStrategy().isRedirected(
            new BasicHttpRequest("GET", from), response, HttpClientContext.create());
    }

    @Test
    public void withoutFollowRedirectsOnlyAnHttpsUpgradeIsFollowed() throws Exception {
        servlet.followAllRedirects = false;

        assertTrue(isRedirectFollowed("http://example.org/wms?a=1", "https://example.org/wms?a=1"));
        assertTrue(isRedirectFollowed("http://example.org", "https://EXAMPLE.org/"));
        assertTrue(isRedirectFollowed("http://example.org:80/x", "https://example.org:443/x"));

        assertFalse(isRedirectFollowed("http://example.org/wms", "https://example.com/wms"));
        assertFalse(isRedirectFollowed("http://example.org/wms", "https://example.org/other"));
        assertFalse(isRedirectFollowed("http://example.org/wms?a=1", "https://example.org/wms?a=2"));
        assertFalse(isRedirectFollowed("http://example.org/wms", "https://example.org:8443/wms"));
        assertFalse(isRedirectFollowed("http://example.org/wms", "http://example.org/wms/"));
        assertFalse(isRedirectFollowed("https://example.org/wms", "http://example.org/wms"));
    }

    @Test
    public void withFollowRedirectsAnyRedirectIsFollowed() throws Exception {
        assertTrue(isRedirectFollowed("http://example.org/wms", "https://example.com/elsewhere"));
    }

    /**
     * Resolve a name through the servlet's resolver, with the lookup answered by the given addresses
     * instead of the name service.
     */
    private InetAddress[] resolveAs(String host, String... addresses) throws Exception {
        InetAddress[] answer = new InetAddress[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            answer[i] = InetAddress.getByName(addresses[i]);
        }
        return servlet.createDnsResolver(name -> answer).resolve(host);
    }

    private void assertResolutionRefused(String host, String... addresses) throws Exception {
        try {
            resolveAs(host, addresses);
            fail(host + " resolving to " + Arrays.toString(addresses) + " should be refused");
        } catch (URITemplateProxyServlet.ExcludedAddressException expected) {
            // The name leads to an address the exclusion list keeps out of reach.
        }
    }

    @Test
    public void aNameResolvingToAnExcludedAddressIsRefused() throws Exception {
        assertResolutionRefused("localtest.me", "127.0.0.1");
        assertResolutionRefused("internal.example.com", "10.0.0.5");
        assertResolutionRefused("metadata.example.com", "169.254.169.254");
        assertResolutionRefused("v6.example.com", "::1");
        assertResolutionRefused("ula.example.com", "fd00::1");
        assertResolutionRefused("mapped.example.com", "::ffff:192.168.1.1");
    }

    @Test
    public void aNameWithAnyExcludedAddressIsRefused() throws Exception {
        assertResolutionRefused("mixed.example.com", "93.184.215.14", "127.0.0.1");
    }

    @Test
    public void aNameResolvingToPublicAddressesIsResolved() throws Exception {
        assertEquals(2, resolveAs("example.com", "93.184.215.14", "2606:2800:21f:cb07:6820:80da:af6b:8b2c").length);
    }

    @Test(expected = URITemplateProxyServlet.ExcludedAddressException.class)
    public void theClientRefusesToConnectToAnExcludedAddress() throws Exception {
        InetAddress loopback = InetAddress.getByName("127.0.0.1");
        try (CloseableHttpClient client = HttpClientBuilder.create()
            .setDnsResolver(servlet.createDnsResolver(name -> new InetAddress[]{loopback}))
            .build()) {
            client.execute(new HttpGet("http://public-looking.example.com/"));
        }
    }

    @Test
    public void theOutboundProxyIsNotChecked() throws Exception {
        String previous = System.getProperty("http.proxyHost");
        System.setProperty("http.proxyHost", "outbound-proxy.example.com");
        try {
            assertEquals(1, resolveAs("OUTBOUND-PROXY.example.com", "10.0.0.1").length);
            assertResolutionRefused("other.example.com", "10.0.0.1");
        } finally {
            if (previous == null) {
                System.clearProperty("http.proxyHost");
            } else {
                System.setProperty("http.proxyHost", previous);
            }
        }
    }

    @Test
    public void publicHostsAreNotExcluded() throws Exception {
        assertNotExcluded("http://example.com/");
        assertNotExcluded("http://8.8.8.8/");
        assertNotExcluded("http://[2001:4860:4860::8888]/");
    }

    @Test
    public void hostVariantsCoverTheSpellingsOfAnIpv6Literal() {
        assertTrue(servlet.getHostVariants("[::1]").contains("[::1]"));
        assertTrue(servlet.getHostVariants("[::1]").contains("::1"));
        assertTrue(servlet.getHostVariants("[::1]").contains("0:0:0:0:0:0:0:1"));
    }

    @Test
    public void hostVariantsLeaveANameAlone() {
        assertEquals(1, servlet.getHostVariants("example.com").size());
        assertTrue(servlet.getHostVariants("example.com").contains("example.com"));
    }

    @Test
    public void hostVariantsLeaveAnIpv4LiteralAlone() {
        assertEquals(1, servlet.getHostVariants("127.0.0.1").size());
    }

    @Test
    public void aBracketedValueThatIsNotAnAddressIsNotMatched() {
        // Must not throw, and must not silently become an excluded host.
        assertFalse(servlet.isExcludedHost("[not-an-address]"));
    }

    @Test
    public void anExclusionEntryWrittenWithBracketsStillWorks() {
        servlet.setExcludeHosts("^(\\[::1\\])$");
        assertTrue(servlet.isExcludedHost("[::1]"));
    }
}
