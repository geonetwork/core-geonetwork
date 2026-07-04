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

public class UrlWhitelistServiceImplTest {

    private UrlWhitelistService service;

    @Before
    public void setUp() {
        service = new UrlWhitelistServiceImpl();
    }

    @Test
    public void allowsAnyUrlWhenWhitelistIsNull() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/vocab", null));
    }

    @Test
    public void allowsAnyUrlWhenWhitelistIsEmpty() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/vocab", ""));
    }

    @Test
    public void allowsAnyUrlWhenWhitelistIsBlank() {
        assertTrue(service.isUrlAllowed("https://anything.example.org/vocab", "   \n  \n"));
    }

    @Test
    public void rejectsBlankUrlWhenWhitelistIsConfigured() {
        assertFalse(service.isUrlAllowed("", "https://example.org/*"));
        assertFalse(service.isUrlAllowed(null, "https://example.org/*"));
    }

    @Test
    public void matchesExactPattern() {
        String whitelist = "https://example.org/vocab.rdf";
        assertTrue(service.isUrlAllowed("https://example.org/vocab.rdf", whitelist));
        assertFalse(service.isUrlAllowed("https://example.org/other.rdf", whitelist));
    }

    @Test
    public void matchesTrailingWildcard() {
        String whitelist = "https://example.org/*";
        assertTrue(service.isUrlAllowed("https://example.org/vocab.rdf", whitelist));
        assertTrue(service.isUrlAllowed("https://example.org/thesaurus/deep/path.rdf", whitelist));
        assertFalse(service.isUrlAllowed("https://not-example.org/vocab.rdf", whitelist));
    }

    @Test
    public void matchesSubdomainWildcard() {
        String whitelist = "https://*.example.org/*";
        assertTrue(service.isUrlAllowed("https://vocabs.example.org/thesaurus.rdf", whitelist));
        assertTrue(service.isUrlAllowed("https://a.b.example.org/thesaurus.rdf", whitelist));
        assertFalse(service.isUrlAllowed("https://example.org/thesaurus.rdf", whitelist));
        assertFalse(service.isUrlAllowed("https://evil.org/thesaurus.rdf", whitelist));
    }

    @Test
    public void matchesAnyOfMultipleLines() {
        String whitelist = "https://one.example.org/*\nhttps://two.example.org/*";
        assertTrue(service.isUrlAllowed("https://one.example.org/a.rdf", whitelist));
        assertTrue(service.isUrlAllowed("https://two.example.org/b.rdf", whitelist));
        assertFalse(service.isUrlAllowed("https://three.example.org/c.rdf", whitelist));
    }

    @Test
    public void ignoresBlankLinesAndComments() {
        String whitelist = "\n  # comment line, ignored\n\nhttps://example.org/*\n   \n";
        assertTrue(service.isUrlAllowed("https://example.org/a.rdf", whitelist));
        assertFalse(service.isUrlAllowed("https://other.org/a.rdf", whitelist));
    }

    @Test
    public void rejectsUrlNotMatchingAnyPattern() {
        String whitelist = "https://trusted.example.org/*";
        assertFalse(service.isUrlAllowed("https://untrusted.example.org/vocab.rdf", whitelist));
    }

    @Test
    public void matchingIsCaseInsensitive() {
        String whitelist = "https://Example.ORG/*";
        assertTrue(service.isUrlAllowed("https://example.org/VOCAB.RDF", whitelist));
    }

    @Test
    public void trimsWhitespaceAroundPatterns() {
        String whitelist = "  https://example.org/*  \r\n  https://other.org/*  ";
        assertTrue(service.isUrlAllowed("https://example.org/a.rdf", whitelist));
        assertTrue(service.isUrlAllowed("https://other.org/b.rdf", whitelist));
    }
}
