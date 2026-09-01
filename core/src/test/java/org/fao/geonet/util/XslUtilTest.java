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

package org.fao.geonet.util;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class XslUtilTest {

    private static final String UUID = "test-uuid";
    private static final String METADATA_ID = "42";

    @After
    public void tearDown() {
        ApplicationContextHolder.clear();
    }

    private DataManager mockDataManagerWithRecord() throws Exception {
        ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
        DataManager dataManager = mock(DataManager.class);
        when(applicationContext.getBean(DataManager.class)).thenReturn(dataManager);
        when(dataManager.getMetadataId(UUID)).thenReturn(METADATA_ID);
        when(dataManager.getMetadataSchema(METADATA_ID)).thenReturn("iso19139");
        when(dataManager.getMetadata(METADATA_ID)).thenReturn(new Element("MD_Metadata"));

        ApplicationContextHolder.set(applicationContext);
        return dataManager;
    }

    private ServiceContext mockServiceContextGrantingOperations(Set<Operation> operations) throws Exception {
        ServiceContext context = mock(ServiceContext.class);
        AccessManager accessManager = mock(AccessManager.class);
        UserSession userSession = mock(UserSession.class);

        when(context.getBean(AccessManager.class)).thenReturn(accessManager);
        when(context.getIpAddress()).thenReturn("127.0.0.1");
        when(context.getUserSession()).thenReturn(userSession);
        when(userSession.isAuthenticated()).thenReturn(false);
        when(accessManager.getOperations(eq(context), anyString(), anyString())).thenReturn(operations);

        return context;
    }

    private static Set<Operation> viewAllowed() {
        return Collections.singleton(new Operation().setId(ReservedOperation.view.getId()));
    }

    @Test
    public void testGetRecordDoesNotCheckPrivileges() throws Exception {
        DataManager dataManager = mockDataManagerWithRecord();

        // No ServiceContext at all bound to the thread (eg. async indexing) -
        // the unchecked getRecord() must still return the record.
        try (MockedStatic<ServiceContext> serviceContextMock = mockStatic(ServiceContext.class)) {
            serviceContextMock.when(ServiceContext::get).thenReturn(null);

            org.w3c.dom.Node node = XslUtil.getRecord(UUID);

            assertNotNullRecord(node);
        }
    }

    @Test
    public void testGetRecordUnknownUuidReturnsNull() throws Exception {
        mockDataManagerWithRecord();

        assertNull(XslUtil.getRecord("does-not-exist"));
        assertNull(XslUtil.getRecordIfViewable("does-not-exist"));
    }

    @Test
    public void testGetRecordIfViewableReturnsRecordWhenViewAllowed() throws Exception {
        DataManager dataManager = mockDataManagerWithRecord();
        ServiceContext context = mockServiceContextGrantingOperations(viewAllowed());

        try (MockedStatic<ServiceContext> serviceContextMock = mockStatic(ServiceContext.class)) {
            serviceContextMock.when(ServiceContext::get).thenReturn(context);

            org.w3c.dom.Node node = XslUtil.getRecordIfViewable(UUID);

            assertNotNullRecord(node);
        }
    }

    @Test
    public void testGetRecordIfViewableReturnsNullWhenViewNotAllowed() throws Exception {
        DataManager dataManager = mockDataManagerWithRecord();
        ServiceContext context = mockServiceContextGrantingOperations(Collections.emptySet());

        try (MockedStatic<ServiceContext> serviceContextMock = mockStatic(ServiceContext.class)) {
            serviceContextMock.when(ServiceContext::get).thenReturn(context);

            org.w3c.dom.Node node = XslUtil.getRecordIfViewable(UUID);

            assertNull(node);
            verify(dataManager, never()).getMetadata(anyString());
        }
    }

    @Test
    public void testGetRecordIfViewableReturnsNullWhenNoServiceContext() throws Exception {
        DataManager dataManager = mockDataManagerWithRecord();

        try (MockedStatic<ServiceContext> serviceContextMock = mockStatic(ServiceContext.class)) {
            serviceContextMock.when(ServiceContext::get).thenReturn(null);

            org.w3c.dom.Node node = XslUtil.getRecordIfViewable(UUID);

            assertNull(node);
            verify(dataManager, never()).getMetadata(anyString());
        }
    }

    private static void assertNotNullRecord(org.w3c.dom.Node node) {
        org.junit.Assert.assertNotNull(node);
        org.w3c.dom.Document domDocument = (org.w3c.dom.Document) node;
        assertEquals("MD_Metadata", domDocument.getDocumentElement().getTagName());
    }

    @Test
    public void testHtml2text() {
        String html = "<div><span>Sample text</span><br/><span>Sample text 2</span><br/><span>Sample text 3</span></div>";
        String expectedText = "Sample text\nSample text 2\nSample text 3";
        String text = XslUtil.html2text(html);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textSubstituteHtmlToTextLayoutElementWithBr() {
        String html = "<div><span>Sample text</span><br/><span>Sample text 2</span><br/><span>Sample text 3</span></div>";
        String expectedText = "Sample text\nSample text 2\nSample text 3";
        String text = XslUtil.html2text(html, true);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textSubstituteHtmlToTextLayoutWithP() {
        String html = "<div><p>Sample text</p><p>Sample text 2</p><p>Sample text 3</p></div>";
        String expectedText = "Sample text\n\nSample text 2\n\nSample text 3";
        String text = XslUtil.html2text(html, true);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textSubstituteHtmlToTextLayoutWithPWithNewLine() {
        String html = "<div><p>Sample text</p>\n<p>Sample text 2</p>\n<p>Sample text 3</p></div>";
        String expectedText = "Sample text\n\nSample text 2\n\nSample text 3";
        String text = XslUtil.html2text(html, true);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textSubstituteHtmlToTextLayoutWithLi() {
        String html = "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>";
        String expectedText = "* Item 1\n* Item 2\n* Item 3";
        String text = XslUtil.html2text(html, true);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textSubstituteHtmlToTextLayoutWithA() {
        String html = "<div><a href=\"https://geonetwork-opensource.org/\">Link to GN</a></div>";
        String expectedText = "Link to GN (https://geonetwork-opensource.org/)";
        String text = XslUtil.html2text(html, true);

        assertEquals(expectedText, text);
    }

    @Test
    public void testHtml2textNormalized() {
        String html = "<div><span>Sample text</span><br/><span>Sample text 2</span></div>";
        String expectedText = "Sample text Sample text 2";
        String text = XslUtil.html2textNormalized(html);

        assertEquals(expectedText, text);
    }

    @Test
    public void testToUiConfigArgValidObject() {
        String result = XslUtil.toUiConfigArg("{\"a\":1}");
        assertTrue(result.startsWith("JSON.parse("));
        assertTrue(result.contains("a"));
    }

    @Test
    public void testToUiConfigArgCodeInjectionPayload() {
        assertEquals("", XslUtil.toUiConfigArg("(alert(1),{})"));
    }

    @Test
    public void testToUiConfigArgScriptBreakout() {
        String result = XslUtil.toUiConfigArg("{\"x\":\"</script>\"}");
        assertFalse(result.isEmpty());
        assertFalse(result.contains("</script>"));
    }

    @Test
    public void testToUiConfigArgArrayReturnsEmpty() {
        assertEquals("", XslUtil.toUiConfigArg("[1,2]"));
    }

    @Test
    public void testToUiConfigArgStringReturnsEmpty() {
        assertEquals("", XslUtil.toUiConfigArg("\"5\""));
    }

    @Test
    public void testToUiConfigArgBooleanReturnsEmpty() {
        assertEquals("", XslUtil.toUiConfigArg("true"));
    }

    @Test
    public void testToUiConfigArgTrailingTokensReturnsEmpty() {
        assertEquals("", XslUtil.toUiConfigArg("{}whatever"));
    }

    @Test
    public void testToUiConfigArgBlankReturnsEmpty() {
        assertEquals("", XslUtil.toUiConfigArg(""));
        assertEquals("", XslUtil.toUiConfigArg("   "));
        assertEquals("", XslUtil.toUiConfigArg(null));
    }

    @Test
    public void testToUiConfigArgLineSeparatorEscaped() {
        String lineSep = String.valueOf((char) 0x2028);
        String input = "{\"k\":\"val" + lineSep + "ue\"}";
        String result = XslUtil.toUiConfigArg(input);
        assertFalse(result.isEmpty());
        assertFalse(result.contains(lineSep));
        assertTrue(result.contains("\\u2028"));
    }

}
