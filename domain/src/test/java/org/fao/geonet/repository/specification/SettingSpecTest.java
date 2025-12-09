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

package org.fao.geonet.repository.specification;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.fao.geonet.domain.Setting;
import org.fao.geonet.repository.AbstractSpringDataTest;
import org.fao.geonet.repository.SettingRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import jakarta.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SettingSpecTest extends AbstractSpringDataTest {

    public static final Function<Setting, String> SETTING_NAME_FUNCTION = new Function<Setting, String>() {
        @Nullable
        @Override
        public String apply(Setting input) {
            return input.getName();
        }
    };
    @Autowired
    private SettingRepository settingRepository;

    @Test
    public void testNameStartsWith() throws Exception {
        final Setting s1 = settingRepository.save(new Setting().setName("NAME/abc").setValue("val"));
        final Setting s2 = settingRepository.save(new Setting().setName("NAME/abc2").setValue("val"));
        final Setting s3 = settingRepository.save(new Setting().setName("OTHER/abc").setValue("val"));

        List<Setting> found = settingRepository.findAll(SettingSpec.nameStartsWith("NAME/"));
        List<String> ids = Lists.transform(found, SETTING_NAME_FUNCTION);
        assertEquals(ids.toString(), 2, found.size());
        assertTrue(ids.contains(s1.getName()));
        assertTrue(ids.contains(s2.getName()));

        found = settingRepository.findAll(SettingSpec.nameStartsWith("OTHER/"));
        ids = Lists.transform(found, SETTING_NAME_FUNCTION);
        assertEquals(1, found.size());
        assertTrue(ids.contains(s3.getName()));

    }

}
