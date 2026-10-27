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
package org.fao.geonet.auditable;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.fao.geonet.domain.auditable.UserAuditable;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.UserAuditableRepository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UserAuditableServiceTest {

    private final List<UserAuditable> saved = new ArrayList<>();
    private final List<Integer> lookedUp = new ArrayList<>();

    /** A repository that only supports findById and save, returning the given stored entry. */
    private UserAuditableRepository repository(UserAuditable stored) {
        return (UserAuditableRepository) Proxy.newProxyInstance(
            UserAuditableRepository.class.getClassLoader(), new Class[]{UserAuditableRepository.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "findById":
                        lookedUp.add((Integer) args[0]);
                        return Optional.ofNullable(stored);
                    case "save":
                        saved.add((UserAuditable) args[0]);
                        return args[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });
    }

    private UserAuditableService service(boolean auditingEnabled, UserAuditable stored) {
        SettingManager settingManager = new SettingManager() {
            @Override
            public boolean getValueAsBool(String key, boolean defaultValue) {
                return auditingEnabled;
            }
        };
        return new UserAuditableService(settingManager, repository(stored));
    }

    private static UserAuditable userAuditable(int id) {
        UserAuditable userAuditable = new UserAuditable();
        userAuditable.setId(id);
        userAuditable.setUsername("username");
        userAuditable.setProfile("RegisteredUser");
        return userAuditable;
    }

    @Test
    public void updateKeepsTheCreationUserAndDate() {
        Date created = new Date(1_000_000L);
        UserAuditable stored = userAuditable(7);
        stored.setCreatedBy("creator");
        stored.setCreatedDate(created);

        // Built from scratch by UserAuditable.build, so without the creation data
        UserAuditable update = userAuditable(7);

        service(true, stored).auditSave(update);

        assertEquals(1, saved.size());
        assertEquals("creator", saved.get(0).getCreatedBy());
        assertEquals(created, saved.get(0).getCreatedDate());
    }

    @Test
    public void newEntryIsSavedAsIs() {
        UserAuditable first = userAuditable(8);

        service(true, null).auditSave(first);

        assertEquals(1, saved.size());
        assertEquals(Integer.valueOf(8), lookedUp.get(0));
        // The creation user and date are set by the auditing listener when the entry is inserted
        assertNull(saved.get(0).getCreatedBy());
        assertNull(saved.get(0).getCreatedDate());
    }

    @Test
    public void nothingIsDoneWhenAuditingIsDisabled() {
        UserAuditable stored = userAuditable(9);
        stored.setCreatedBy("creator");

        service(false, stored).auditSave(userAuditable(9));

        assertTrue(saved.isEmpty());
        assertTrue(lookedUp.isEmpty());
    }
}
