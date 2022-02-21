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

package org.fao.geonet.kernel.schema.subtemplate;

import org.fao.geonet.index.es.EsRestClient;
import org.fao.geonet.kernel.schema.subtemplate.Status.Failure;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer.CONTACT;
import static org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer.EXTENT;
import static org.fao.geonet.kernel.schema.subtemplate.SubtemplatesByLocalXLinksReplacer.FORMAT;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class SubtemplatesByLocalXLinksReplacerTest {

    private static String EXPECTED_XLINK_URL_PREFIX = "local://srv/api/registries/entries/";

    Element dataXml;
    ManagersProxy proxy;
    EsRestClient esRestClient;
    SubtemplatesByLocalXLinksReplacer toTest;
    AbstractReplacer formatReplacer;
    AbstractReplacer extentReplacer;
    AbstractReplacer contactReplacer;

    @Before
    public void prepareToTest() {
        dataXml = mock(Element.class);
        proxy = mock(ManagersProxy.class);
        esRestClient = mock(EsRestClient.class);
        when(proxy.getIso1LangCode(anyString())).thenReturn("en");
        formatReplacer = mock(AbstractReplacer.class);
        extentReplacer = mock(AbstractReplacer.class);
        contactReplacer = mock(AbstractReplacer.class);
        when(formatReplacer.getAlias()).thenReturn(FORMAT);
        when(extentReplacer.getAlias()).thenReturn(EXTENT);
        when(contactReplacer.getAlias()).thenReturn(CONTACT);
        when(formatReplacer.replaceAll(any(Element.class), anyString(), any(EsRestClient.class),anyString(), anyString(), anyString(), anyList()))
                .thenReturn(new Status());
        when(extentReplacer.replaceAll(any(Element.class), anyString(), any(EsRestClient.class),anyString(),  anyString(), anyString(), anyList()))
                .thenReturn(new Status());
        when(contactReplacer.replaceAll(any(Element.class), anyString(), any(EsRestClient.class),anyString(),  anyString(), anyString(), anyList()))
                .thenReturn(new Status());
        toTest = new SubtemplatesByLocalXLinksReplacer(Collections.emptyList(), proxy, esRestClient, "records") {
            @Override
            public List<String> getLocalesAsHrefParam(Element dataXml) {
                return Collections.emptyList();
            }
        };
        toTest.addReplacer(formatReplacer);
        toTest.addReplacer(extentReplacer);
        toTest.addReplacer(contactReplacer);
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void replaceAll() {
        String templatesToOperateOn = FORMAT + ";" + EXTENT + ";" + CONTACT;

        toTest.replaceSubtemplatesByLocalXLinks(dataXml, templatesToOperateOn);

        Mockito.verify(formatReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
        Mockito.verify(extentReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
        Mockito.verify(contactReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
    }

    @Test
    public void replaceOne() {
        String templatesToOperateOn = EXTENT;

        toTest.replaceSubtemplatesByLocalXLinks(dataXml, templatesToOperateOn);

        Mockito.verify(formatReplacer, never()).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
        Mockito.verify(contactReplacer, never()).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
        Mockito.verify(extentReplacer, times(1)).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
    }

    @Test
    public void statusCollectorExceptions() {
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage("|A|C");
        when(formatReplacer.replaceAll(any(Element.class), anyString(), any(EsRestClient.class), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(new Failure("A"));
        when(extentReplacer.replaceAll(any(Element.class), anyString(), any(EsRestClient.class), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(new Status());
        when(contactReplacer.replaceAll(any(Element.class), anyString(), any(EsRestClient.class), anyString(), anyString(), anyString(), anyList()))
                .thenReturn(new Failure("C"));
        String templatesToOperateOn = FORMAT + ";" + EXTENT + ";" + CONTACT;

        toTest.replaceSubtemplatesByLocalXLinks(dataXml, templatesToOperateOn);

        Mockito.verify(formatReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, anyString(), "#EN", "eng", Collections.emptyList());
        Mockito.verify(extentReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, anyString(), "#EN", "eng", Collections.emptyList());
        Mockito.verify(contactReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, anyString(), "#EN","eng", Collections.emptyList());
    }

    @Test
    public void nonExistingKeyProvidedInSettings() {
        String templatesToOperateOn = "I don't exist;" + FORMAT + ";" + EXTENT + ";" + CONTACT;

        toTest.replaceSubtemplatesByLocalXLinks(dataXml, templatesToOperateOn);

        Mockito.verify(formatReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
        Mockito.verify(extentReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
        Mockito.verify(contactReplacer).replaceAll(dataXml, EXPECTED_XLINK_URL_PREFIX, esRestClient, "records", "#EN", "eng", Collections.emptyList());
    }
}
