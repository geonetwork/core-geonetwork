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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.fao.geonet.auditable.model.RevisionFieldChange;
import org.fao.geonet.auditable.model.RevisionInfo;
import org.fao.geonet.domain.auditable.AuditableEntity;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.BaseAuditableRepository;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.util.StringUtils;

import java.util.*;

public abstract class BaseAuditableService<U> {
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

    protected BaseAuditableRepository<U> repository;
    protected SettingManager settingManager;

    public void auditSave(U auditableEntity) {
        if (!isAuditableEnabled()) return;

        repository.save(auditableEntity);
    }

    public void auditDelete(U auditableEntity) {
        if (!isAuditableEnabled()) return;

        repository.delete(auditableEntity);
    }

    public String getEntityHistoryAsString(Integer entityIdentifier, ResourceBundle messages) {
        if (!isAuditableEnabled()) return "";

        Revisions<Integer, U> revisions = repository.findRevisions(entityIdentifier);

        return retrieveRevisionHistoryAsString(revisions, messages);
    }

    public List<RevisionInfo> getEntityHistory(Integer entityIdentifier) {
        if (!isAuditableEnabled()) return new ArrayList<>();

        Revisions<Integer, U> revisions = repository.findRevisions(entityIdentifier);

        return retrieveRevisionHistory(revisions);
    }

    public abstract String getEntityType();

    protected String retrieveRevisionHistoryAsString(Revisions<Integer, U> revisions, ResourceBundle messages) {
        List<RevisionInfo> revisionInfoList = retrieveRevisionHistory(revisions);

        List<String> diffs = new ArrayList<>();

        revisionInfoList.stream().forEach(revision -> {
            List<String> revisionChanges = new ArrayList<>();
            revisionChanges.add(revision.getValue());

            revision.getChanges().forEach(change -> {
                boolean oldValueIsDefined = StringUtils.hasLength(change.getOldValue());
                boolean newValueIsDefined = StringUtils.hasLength(change.getNewValue());

                if (oldValueIsDefined && newValueIsDefined) {
                    revisionChanges.add(String.format(messages.getString("audit.revision.field.updated"),
                        change.getName(), change.getOldValue(), change.getNewValue()));
                } else if (!oldValueIsDefined && newValueIsDefined) {
                    revisionChanges.add(String.format(messages.getString("audit.revision.field.set"),
                        change.getName(), change.getNewValue()));
                } else if (oldValueIsDefined && !newValueIsDefined) {
                    revisionChanges.add(String.format(messages.getString("audit.revision.field.unset"), change.getName()));
                }
            });

            String revisionInfo = String.format(messages.getString("audit.revision"),
                revision.getUser(),
                revision.getDate(),
                String.join(LINE_SEPARATOR, revisionChanges));

            diffs.add(revisionInfo);

        });

        return String.join(LINE_SEPARATOR, diffs);
    }

    protected List<RevisionInfo> retrieveRevisionHistory(Revisions<Integer, U> revisions) {
        String idFieldName = "id";
        List<Revision<Integer, U>> revisionList = revisions.toList();
        int numRevisions = revisions.toList().size();

        List<RevisionInfo> revisionInfoList = new ArrayList<>();

        if (numRevisions > 0) {
            Revision<Integer, U> initialRevision = revisionList.get(0);
            AuditableEntity initialRevisionEntity = (AuditableEntity) initialRevision.getEntity();

            // Initial revision
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> revisionMap =  objectMapper.convertValue(initialRevision.getEntity(), Map.class);
            // Remove empty values and id
            revisionMap.values().removeAll(Arrays.asList("", null));
            revisionMap.remove(idFieldName);

            RevisionInfo initialRevisionInfo = new RevisionInfo(
                initialRevision.getMetadata().getRequiredRevisionNumber(),
                initialRevisionEntity.getCreatedBy(),
                initialRevisionEntity.getCreatedDate(), revisionMap.toString());

            revisionInfoList.add(initialRevisionInfo);

            int i = 0;
            while (i+1 < numRevisions) {
                Revision<Integer, U> revision1 = revisionList.get(i);
                Revision<Integer, U> revision2 = revisionList.get(i+1);

                Map<String, Object> revision1Map =  objectMapper.convertValue(revision1.getEntity(), Map.class);
                revision1Map.remove(idFieldName);
                Map<String, Object> revision2Map =  objectMapper.convertValue(revision2.getEntity(), Map.class);
                revision2Map.remove(idFieldName);

                MapDifference<String, Object> diff = Maps.difference(revision1Map, revision2Map);

                revision2Map.values().removeAll(Arrays.asList("", null));

                final RevisionInfo revisionInfo = new RevisionInfo(
                    revision2.getMetadata().getRequiredRevisionNumber(),
                    ((AuditableEntity) revision2.getEntity()).getLastModifiedBy(),
                    ((AuditableEntity) revision2.getEntity()).getLastModifiedDate(),
                    revision2Map.toString());

                diff.entriesDiffering().forEach((key, entry) -> {
                    String oldValueAsString = (entry.leftValue() != null) ? entry.leftValue().toString() : "";
                    String newValueAsString = (entry.rightValue() != null) ? entry.rightValue().toString() : "";

                    RevisionFieldChange revisionFieldChange = new RevisionFieldChange(key, oldValueAsString, newValueAsString);

                    revisionInfo.addChange(revisionFieldChange);

                });


                revisionInfoList.add(revisionInfo);
                i++;
            }
        }

        Collections.reverse(revisionInfoList);
        return revisionInfoList;
    }

    protected boolean isAuditableEnabled() {
        return settingManager.getValueAsBool(Settings.SYSTEM_AUDITABLE_ENABLE, false);
    }
}
