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

package org.fao.geonet.api.records.formatters;

import com.google.common.io.Files;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.SystemInfo;
import org.fao.geonet.TestFunction;
import org.fao.geonet.api.records.formatters.cache.Key;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.ServletWebRequest;

import java.io.File;

import static com.google.common.xml.XmlEscapers.xmlContentEscaper;

/**
 * @author Jesse on 10/23/2014.
 */
public abstract class AbstractFormatterTest extends AbstractServiceIntegrationTest {
    protected static final String UUID = "80f1c261-1495-4e3b-bd22-f32a5f0ad643";
    @Autowired
    protected FormatterApi formatService;
    @Autowired
    protected SystemInfo systemInfo;
    protected int id;
    protected String xml;
    @Autowired
    MetadataRepository metadataRepository;
    @Autowired
    SchemaManager schemaManager;
    @Autowired
    SettingManager settingManager;
    @Autowired
    DataManager dataManager;
    @Autowired
    private IsoLanguagesMapper mapper;


    @Before
    public void setUp() throws Exception {
        final ServiceContext serviceContext = createServiceContext();
        loginAsAdmin(serviceContext);

        this.xml = Files.toString(getTestMetadataFile(), Constants.CHARSET);
        final Element md = Xml.loadString(this.xml, false);
        final String schemaId = schemaManager.autodetectSchema(md);

        Metadata metadata = new Metadata();
        MetadataSourceInfo sourceInfo = new MetadataSourceInfo().setSourceId(settingManager.getSiteId()).setOwner(1);
        metadata.setSourceInfo(sourceInfo);
        MetadataDataInfo dataInfo = new MetadataDataInfo().
            setChangeDate(new ISODate()).
            setSchemaId(schemaId).
            setCreateDate(new ISODate()).
            setType(MetadataType.METADATA).
            setRoot(md.getQualifiedName());
        metadata.setDataInfo(dataInfo);
        metadata.setUuid(UUID);
        MetadataHarvestInfo harvestInfo = new MetadataHarvestInfo().setHarvested(isHarvested());
        metadata.setHarvestInfo(harvestInfo);
        metadata.setData(xml);
        this.id = dataManager.insertMetadata(serviceContext, metadata, metadata.getXmlData(false), IndexingMode.full, false,
            UpdateDatestamp.NO, false, false).getId();
    }

    public boolean isHarvested() {
        return false;
    }

    protected abstract File getTestMetadataFile() throws Exception;


    protected Pair<FormatterImpl, FormatterParams> getFormatterFormatterParamsPair(MockHttpServletRequest request, String formatterId) throws Exception {
        final ServletWebRequest webRequest = new ServletWebRequest(request);
        Key key = new Key(id, getUILang(), getOutputType(), formatterId, false, FormatterWidth._100);
        return this.formatService.loadMetadataAndCreateFormatterAndParams(createServiceContext(), key, webRequest);
    }

    protected void measureFormatterPerformance(final MockHttpServletRequest request, final String formatterId) throws Exception {
        final ServletWebRequest webRequest = new ServletWebRequest(request, new MockHttpServletResponse());
        TestFunction testFunction = new TestFunction() {
            @Override
            public void exec() throws Exception {
                formatService.exec(getUILang(), getOutputType().name(), "" + id, null, formatterId, "true", FormatterWidth._100,
                    webRequest);
            }
        };

        super.measurePerformance(testFunction);
    }

    private FormatType getOutputType() {
        return FormatType.html;
    }

    private String getUILang() {
        return "eng";
    }

    private double round(double num) {
        return Math.round(num * 1000) / 1000;
    }

    protected String escapeXmlText(String text) {
        return xmlContentEscaper().escape(text.replaceAll("\\s+", " ")).replaceAll("\\&", "&amp;").replaceAll("\"", "&quot;");
    }
}
