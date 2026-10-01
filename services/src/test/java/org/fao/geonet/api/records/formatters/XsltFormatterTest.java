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

package org.fao.geonet.api.records.formatters;

import jeeves.server.context.ServiceContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.fao.geonet.XmlValidationTest;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XsltFormatterTest extends AbstractServiceIntegrationTest {

	@Autowired
	private XsltFormatter toTest;

	@Autowired
	SourceRepository sourceRepository;

	@Autowired
	GroupRepository groupRepository;

	@Autowired
	SettingManager settingManager;

	@Test
	public void transformationSourceGroupLogoWhenPreferringGroupLogo() throws Exception {
		Group adminGroup = groupRepository.findById(0).get();
		adminGroup.setLogo("test.png");
		groupRepository.save(adminGroup);

		buildTransformationSourceSetGrouplogoTo("true",
				"../images/harvesting/test.png");
	}

	@Test
	public void transformationSourceGroupLogoWhenNotPreferringGroupLogo() throws Exception {
		buildTransformationSourceSetGrouplogoTo("false",
				"../images/logos/" + sourceRepository.findAll().get(0).getUuid() + ".png");
	}

	private @NonNull FormatterParams buildFormatterParams() throws Exception {
		FormatterParams params = new FormatterParams();
		ServiceContext serviceContext = createServiceContext();
		loginAsAdmin(serviceContext);
		params.context = serviceContext;
		params.config = mock(ConfigFile.class);
		when(params.config.getLang(anyString())).thenReturn("en");
		params.format = mock(FormatterApi.class);
		when(params.format.getPluginLocResources(any(), any(), anyString())).thenReturn(new Element("mocked_format"));
		params.metadata = Xml.loadFile(XmlValidationTest.class.getResource("kernel/valid-metadata.iso19139.xml"));
		params.metadataInfo = new Metadata();
		params.metadataInfo.getSourceInfo()
				.setSourceId(sourceRepository.findAll().get(0).getUuid())
				.setGroupOwner(0);
		return params;
	}

	private void buildTransformationSourceSetGrouplogoTo(String preferGroupLogo, String expectedGroupLogo) throws Exception {
		settingManager.setValue(Settings.SYSTEM_PREFER_GROUP_LOGO, preferGroupLogo);
		FormatterParams params = buildFormatterParams();

		Element transformed = toTest.buildTransformationSource(params);

		assertEqualsText(expectedGroupLogo, transformed, "*//grouplogo");
	}

}
