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

package jeeves.server.local;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LocalServiceRequestTest {

    @Test
    public void testParseLocalURLNoLangNoParams() throws Exception {
        LocalServiceRequest req = LocalServiceRequest.create("local://csw");
        assertNoLangNoParams(req);
    }

    @Test
    public void testParseLocalURLNoLangURLWithLangInParams() throws Exception {
        LocalServiceRequest req = LocalServiceRequest
            .create("local://csw?thesaurus=external._none_.gemet&id=http%3A//www.eionet.europa.eu/gemet/concept/13275&locales=DE,FR,IT,EN");
        assertNull("Expected Null but got: " + req.getLanguage(), req.getLanguage());
        assertEquals("csw", req.getService());
        assertEquals("request", req.getParams().getName());
        assertEquals(3, req.getParams().getChildren().size());
    }

    @Test
    public void testParseRelativeURLNoLangNoParams() throws Exception {
        assertNoLangNoParams(LocalServiceRequest.create("/csw"));
    }

    @Test
    public void testParseRelative2URLNoLangNoParams() throws Exception {
        assertNoLangNoParams(LocalServiceRequest.create("csw"));
    }

    private void assertNoLangNoParams(LocalServiceRequest req) {
        assertNull("Expected Null but got: " + req.getLanguage(), req.getLanguage());
        assertEquals("csw", req.getService());
        assertEquals("request", req.getParams().getName());
        assertEquals(0, req.getParams().getChildren().size());
    }


    @Test
    public void testParseLocalURLLangNoParams() throws Exception {
        LocalServiceRequest req = LocalServiceRequest.create("local://eng/csw");
        assertLangNoParams(req);
    }

    @Test
    public void testParseRelativeURLLangNoParams() throws Exception {
        assertLangNoParams(LocalServiceRequest.create("/eng/csw"));
    }

    @Test
    public void testParseRelative2URLLangNoParams() throws Exception {
        assertLangNoParams(LocalServiceRequest.create("eng/csw"));
    }

    private void assertLangNoParams(LocalServiceRequest req) {
        assertEquals("eng", req.getLanguage());
        assertEquals("csw", req.getService());
        assertEquals("request", req.getParams().getName());
        assertEquals(0, req.getParams().getChildren().size());
    }

    @Test
    public void testParseLocalURLLangParams() throws Exception {
        assertLangParams(LocalServiceRequest.create("local://deu/csw?version=1.0.0"));
    }

    @Test
    public void testParseRelativeURLLangParams() throws Exception {
        assertLangParams(LocalServiceRequest.create("/deu/csw?version=1.0.0"));
    }

    @Test
    public void testParseRelative2URLLangParams() throws Exception {
        assertLangParams(LocalServiceRequest.create("deu/csw?version=1.0.0"));
    }

    private void assertLangParams(LocalServiceRequest req) {
        assertEquals("deu", req.getLanguage());
        assertEquals("csw", req.getService());
        assertEquals("request", req.getParams().getName());
        assertEquals(1, req.getParams().getChildren().size());
        assertEquals("1.0.0", req.getParams().getChildText("version"));
    }

}
