/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import org.fao.geonet.auditable.model.UserAuditable;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.javers.core.Javers;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class AuditableService implements IAuditableService {
    private SettingManager settingManager;
    private Javers javers;

    public AuditableService(SettingManager settingManager, Javers javers) {
        this.settingManager = settingManager;
        this.javers = javers;
    }

    public void auditSave(AuditableEntity auditableEntity, String username) {
        if (!isAuditableEnabled()) return;

        javers.commit(username, auditableEntity);
    }

    public void auditDelete(AuditableEntity auditableEntity, String username) {
        if (!isAuditableEnabled()) return;

        javers.commitShallowDelete(username, auditableEntity);
    }

    public String getEntityHistory(String entityType, Integer entityIdentifier) {
        if (!isAuditableEnabled()) return "";

        Class<? extends AuditableEntity> classz = getEntityTypeClass(entityType);
        JqlQuery query = QueryBuilder.byInstanceId(entityIdentifier, classz).build();
        return javers.findChanges(query).prettyPrint();
    }

    private boolean isAuditableEnabled() {
        return settingManager.getValueAsBool(Settings.SYSTEM_AUDITABLE_ENABLE, false);
    }

    private Class<? extends AuditableEntity> getEntityTypeClass(String entityType) throws IllegalArgumentException{
        if (entityType.equalsIgnoreCase("user")) {
            return UserAuditable.class;
        }

        throw new IllegalArgumentException("Auditable entity not valid.");
    }

}
