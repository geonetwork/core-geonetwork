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

package org.fao.geonet.kernel.url;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UrlAllowlistServiceImplTest {

    private UrlAllowlistService service;

    @Before
    public void setUp() {
        service = new UrlAllowlistServiceImpl();
    }

    @Test
    public void allowsAnyUrlWhenAllowlistIsNull() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/vocab", null));
    }

    @Test
    public void allowsAnyUrlWhenAllowlistIsEmpty() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/vocab", ""));
    }

    @Test
    public void allowsAnyUrlWhenAllowlistIsBlank() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/vocab", "   \n  \n"));
    }

    @Test
    public void rejectsBlankUrlWhenAllowlistIsConfigured() {
        assertFalse(service.isUrlAllowed("", "https://example.org/*"));
        assertFalse(service.isUrlAllowed(null, "https://example.org/*"));
    }

    @Test
    public void matchesExactPattern() {
        String allowlist = "https://example.org/vocab.rdf";
        assertTrue(service.isUrlAllowed("https://example.org/vocab.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://example.org/other.rdf", allowlist));
    }

    @Test
    public void matchesTrailingWildcard() {
        String allowlist = "https://example.org/*";
        assertTrue(service.isUrlAllowed("https://example.org/vocab.rdf", allowlist));
        assertTrue(service.isUrlAllowed("https://example.org/thesaurus/deep/path.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://not-example.org/vocab.rdf", allowlist));
    }

    @Test
    public void matchesSubdomainWildcard() {
        String allowlist = "https://*.example.org/*";
        assertTrue(service.isUrlAllowed("https://vocabs.example.org/thesaurus.rdf", allowlist));
        assertTrue(service.isUrlAllowed("https://a.b.example.org/thesaurus.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://example.org/thesaurus.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://evil.org/thesaurus.rdf", allowlist));
    }

    @Test
    public void matchesAnyOfMultipleLines() {
        String allowlist = "https://one.example.org/*\nhttps://two.example.org/*";
        assertTrue(service.isUrlAllowed("https://one.example.org/a.rdf", allowlist));
        assertTrue(service.isUrlAllowed("https://two.example.org/b.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://three.example.org/c.rdf", allowlist));
    }

    @Test
    public void ignoresBlankLinesAndComments() {
        String allowlist = "\n  # comment line, ignored\n\nhttps://example.org/*\n   \n";
        assertTrue(service.isUrlAllowed("https://example.org/a.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://other.org/a.rdf", allowlist));
    }

    @Test
    public void rejectsUrlNotMatchingAnyPattern() {
        String allowlist = "https://trusted.example.org/*";
        assertFalse(service.isUrlAllowed("https://untrusted.example.org/vocab.rdf", allowlist));
    }

    @Test
    public void matchingIsCaseInsensitive() {
        String allowlist = "https://Example.ORG/*";
        assertTrue(service.isUrlAllowed("https://example.org/VOCAB.RDF", allowlist));
    }

    @Test
    public void trimsWhitespaceAroundPatterns() {
        String allowlist = "  https://example.org/*  \r\n  https://other.org/*  ";
        assertTrue(service.isUrlAllowed("https://example.org/a.rdf", allowlist));
        assertTrue(service.isUrlAllowed("https://other.org/b.rdf", allowlist));
    }

    /**
     * A host wildcard must be anchored to the host: the literal that follows {@code *} in the
     * host part appearing later in the path must not make the URL match.
     */
    @Test
    public void hostWildcardDoesNotSpillIntoPath() {
        String allowlist = "https://*.example.org/*";
        assertFalse(service.isUrlAllowed("https://evil.org/.example.org/vocab.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://evil.org/?x=.example.org/", allowlist));
    }

    /**
     * The host is taken from the parsed URL, not the raw authority, so a userinfo prefix cannot
     * be used to make an untrusted host look trusted.
     */
    @Test
    public void rejectsUserinfoHostSpoofing() {
        String allowlist = "https://trusted.example.org/*";
        assertFalse(service.isUrlAllowed("https://trusted.example.org@evil.org/vocab.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://trusted.example.org:pass@evil.org/vocab.rdf", allowlist));
    }

    /**
     * An exact host pattern must not match a host that merely has the allowed host as a prefix
     * or suffix.
     */
    @Test
    public void anchorsHostBoundaries() {
        String allowlist = "https://example.org/*";
        assertFalse(service.isUrlAllowed("https://example.org.evil.com/vocab.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://notexample.org/vocab.rdf", allowlist));
    }

    @Test
    public void matchesSchemeWildcard() {
        String allowlist = "http*://vocabs.example.org/*";
        assertTrue(service.isUrlAllowed("http://vocabs.example.org/a.rdf", allowlist));
        assertTrue(service.isUrlAllowed("https://vocabs.example.org/a.rdf", allowlist));
        assertFalse(service.isUrlAllowed("ftp://vocabs.example.org/a.rdf", allowlist));
    }

    @Test
    public void matchesWhenPatternHasNoPath() {
        String allowlist = "https://vocabs.example.org";
        assertTrue(service.isUrlAllowed("https://vocabs.example.org", allowlist));
        assertTrue(service.isUrlAllowed("https://vocabs.example.org/deep/path.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://other.example.org/a.rdf", allowlist));
    }

    @Test
    public void matchesPortWhenSpecifiedInPattern() {
        String allowlist = "https://vocabs.example.org:8443/*";
        assertTrue(service.isUrlAllowed("https://vocabs.example.org:8443/a.rdf", allowlist));
        assertFalse(service.isUrlAllowed("https://vocabs.example.org:9000/a.rdf", allowlist));
    }

    @Test
    public void bareWildcardAllowsAnyUrl() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/a.rdf", "*"));
        assertTrue(service.isUrlAllowed("ftp://host/path", "*"));
    }

    @Test
    public void rejectsUnparseableUrlWhenAllowlistConfigured() {
        String allowlist = "https://example.org/*";
        assertFalse(service.isUrlAllowed("not a url", allowlist));
        assertFalse(service.isUrlAllowed("https://exa mple.org/a.rdf", allowlist));
    }
}
