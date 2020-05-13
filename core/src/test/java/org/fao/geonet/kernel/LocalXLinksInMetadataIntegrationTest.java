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

package org.fao.geonet.kernel;

import com.google.common.collect.Lists;
import jeeves.server.context.ServiceContext;
import jeeves.xlink.Processor;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test local:// xlinks.
 *
 * Created by Jesse on 1/30/14.
 */
public class LocalXLinksInMetadataIntegrationTest extends AbstractIntegrationTestWithMockedSingletons {

    @Autowired
    private SettingManager _settingManager;
    @Autowired
    private DataManager _dataManager;

    @Test
    public void testResolveLocalXLink() throws Exception {
        String namespaces = "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\"";
        final String responseTemplate = "<gmd:MD_Keywords " + namespaces + ">\n"
            + "    <gmd:keyword>\n"
            + "        <gco:CharacterString>%s</gco:CharacterString>\n"
            + "    </gmd:keyword>\n"
            + "</gmd:MD_Keywords>\n";
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gmd:MD_Metadata " + namespaces
            + "    gco:isoType=\"gmd:MD_Metadata\">\n"
            + "    <gmd:fileIdentifier xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "        <gco:CharacterString>23b53e29-c0a2-4897-b107-141bb15f929a</gco:CharacterString>\n"
            + "    </gmd:fileIdentifier>\n"
            + "    <gmd:identificationInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
            + "        <gmd:MD_DataIdentification gco:isoType=\"gmd:MD_DataIdentification\">\n"
            + "        <gmd:descriptiveKeywords xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"local://thesaurus" +
            ".keyword?thesaurus=external.place.regions&amp;id=http%3A%2F%2Fgeonetwork-opensource.org%2Fregions%235&amp;lang=eng\">\n"
            + "        </gmd:descriptiveKeywords>\n"
            + "        </gmd:MD_DataIdentification>\n"
            + "    </gmd:identificationInfo>\n"
            + "</gmd:MD_Metadata>";

        final List content = Lists.newArrayList(Xml.loadString(xml, false).getContent());
        for (Object o : content) {
            ((Content) o).detach();
        }
        final Element metadata = getSampleMetadataXml().setContent(content);

        ServiceContext context = createServiceContext();
        context.setAsThreadLocal();
        loginAsAdmin(context);

        _settingManager.setValue(Settings.SYSTEM_XLINKRESOLVER_ENABLE, true);

        String schema = _dataManager.autodetectSchema(metadata);
        String uuid = UUID.randomUUID().toString();
        int owner = context.getUserSession().getUserIdAsInt();
        String groupOwner = "" + ReservedGroup.intranet.getId();
        String source = _settingManager.getSiteId();
        String metadataType = MetadataType.METADATA.codeString;
        String changeDate;
        String createDate = changeDate = new ISODate().getDateAndTime();
        String id = _dataManager.insertMetadata(context, schema, metadata, uuid, owner, groupOwner, source, metadataType, null,
            null, createDate, changeDate, false, false);

        SpringLocalServiceInvoker mockInvoker = resetAndGetMockInvoker();

        String keyword1 = "World";
        Element element1 = new SAXBuilder().build(new StringReader(String.format(responseTemplate, keyword1))).getRootElement();
        when(mockInvoker.invoke(any(String.class))).thenReturn(element1);

        final String xpath = "*//gmd:descriptiveKeywords//gmd:keyword/gco:CharacterString";
        assertNull(Xml.selectElement(metadata, xpath));
        verify(mockInvoker, never()).invoke(any(String.class));

        final Element loadedMetadataNoXLinkAttributesNotEdit = _dataManager.getMetadata(context, id, false, false, false);
        assertEqualsText(keyword1, loadedMetadataNoXLinkAttributesNotEdit, xpath, GCO, GMD);
        verify(mockInvoker, times(1)).invoke(any(String.class));

        final Element loadedMetadataKeepXLinkAttributesNotEdit = _dataManager.getMetadata(context, id, false, false, true);
        assertEqualsText(keyword1, loadedMetadataKeepXLinkAttributesNotEdit, xpath, GCO, GMD);
        verify(mockInvoker, times(2)).invoke(any(String.class));

        final Element loadedMetadataNoXLinkAttributesEdit = _dataManager.getMetadata(context, id, false, true, false);
        assertEqualsText(keyword1, loadedMetadataNoXLinkAttributesEdit, xpath, GCO, GMD);
        verify(mockInvoker, times(3)).invoke(any(String.class));

        final Element loadedMetadataKeepXLinkAttributesEdit = _dataManager.getMetadata(context, id, false, true, true);
        assertEqualsText(keyword1, loadedMetadataKeepXLinkAttributesEdit, xpath, GCO, GMD);
        verify(mockInvoker, times(4)).invoke(any(String.class));

        Processor.clearCache();

        String keyword2 = "Other Word";
        Element element2 = new SAXBuilder().build(new StringReader(String.format(responseTemplate, keyword2))).getRootElement();
        when(mockInvoker.invoke(any(String.class))).thenReturn(element2);

        final Element newLoad = _dataManager.getMetadata(context, id, false, true, true);
        assertEqualsText(keyword2, newLoad, xpath, GCO, GMD);
        verify(mockInvoker, times(5)).invoke(any(String.class));
    }
}
