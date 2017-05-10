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

package org.fao.geonet.services.register;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;
import org.springframework.beans.factory.annotation.Autowired;

@Deprecated
public class SelfRegisterTest extends AbstractServiceIntegrationTest {

    @Autowired
    private SettingManager settingManager;

    final SelfRegister sfController = new SelfRegister();

    @Test
    public void selfRegisterTest() throws Exception {
        ServiceContext svcCtx = createServiceContext();

        settingManager.setValue(Settings.SYSTEM_USERSELFREGISTRATION_ENABLE, true);

        Element params = createParams(Pair.write("surname", "john"),
            Pair.write("name", "Doe"),
            Pair.write("email", "root@localhost"),
            Pair.write("profile", Profile.RegisteredUser));


        Element ret = sfController.exec(params, svcCtx);

        assertTrue(ret.getAttribute("surname").getValue().equals("john"));
        assertTrue(ret.getAttribute("name").getValue().equals("Doe"));
        assertTrue(ret.getAttribute("email").getValue().equals("root@localhost"));
        assertTrue(ret.getAttribute("username").getValue().equals("root@localhost"));

    }

    @Test
    public void badParametersSelfRegisterTest() throws Exception {
        ServiceContext svcCtx = createServiceContext();

        settingManager.setValue(Settings.SYSTEM_USERSELFREGISTRATION_ENABLE, true);

        Element params = createParams(
            Pair.write("notExpectedParameter", "NotExpectedValue")
        );


        try {
            sfController.exec(params, svcCtx);
        } catch (Throwable e) {
            assertTrue(e instanceof MissingParameterEx);
        }

    }

    @Test
    public void highProfileSelfRegisterTest() throws Exception {
        ServiceContext svcCtx = createServiceContext();

        settingManager.setValue(Settings.SYSTEM_USERSELFREGISTRATION_ENABLE, true);


        Element params = createParams(Pair.write("surname", "john"),
            Pair.write("name", "Doe"),
            Pair.write("email", "root@localhost"),
            Pair.write("profile", Profile.Administrator));


        Element ret = sfController.exec(params, svcCtx);

        assertTrue(ret.getAttribute("surname").getValue().equals("john"));
        assertTrue(ret.getAttribute("name").getValue().equals("Doe"));
        assertTrue(ret.getAttribute("email").getValue().equals("root@localhost"));
        assertTrue(ret.getAttribute("username").getValue().equals("root@localhost"));

        // Checks that the user has the  expected requested profile
        final UserRepository userRepository = svcCtx.getBean(UserRepository.class);
        User newUsr = userRepository.findOneByEmail("root@localhost");

        // The profil requested is sent by email
        assertTrue(newUsr.getProfile() == Profile.RegisteredUser);
    }
}
