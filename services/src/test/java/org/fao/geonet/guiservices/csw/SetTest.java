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

package org.fao.geonet.guiservices.csw;

import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;

/**
 * Test Csw Config Set User: Jesse Date: 11/7/13 Time: 8:24 AM
 */
public class SetTest extends AbstractServiceIntegrationTest {

    @Autowired
    CswCapabilitiesInfoFieldRepository _infoRepository;
    @Autowired
    SettingRepository _settingsRepository;

    @Test
    public void testExec() throws Exception {
        final ServiceContext context = createServiceContext();
        loginAsAdmin(context);

        assertEquals("true", _settingsRepository.findOne(Settings.SYSTEM_CSW_ENABLE).getValue());
        assertEquals("-1", _settingsRepository.findOne(Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID).getValue());

        final Element params = createParams(read("csw.enable", "off"),
            read("csw.capabilityRecordId", "2"));

        final Element results = new Set().exec(params, context);

        assertEquals("ok", results.getText());

        assertEquals("false", _settingsRepository.findOne(Settings.SYSTEM_CSW_ENABLE).getValue());
        assertEquals("2", _settingsRepository.findOne(Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID).getValue());
    }
}
