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
package org.fao.geonet.api.records.events;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDataInfo;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetadataRemovePublicationDateListenerTest {

    private MetadataRemovePublicationDateListener listener;
    private MetadataUpdatePublicationDateService service;
    private SettingManager settingManager;
    private IMetadataUtils metadataUtils;

    @Before
    public void setUp() throws Exception {
        listener = new MetadataRemovePublicationDateListener();
        service = mock(MetadataUpdatePublicationDateService.class);
        settingManager = mock(SettingManager.class);
        metadataUtils = mock(IMetadataUtils.class);

        setField("metadataUpdatePublicationDateService", service);
        setField("settingManager", settingManager);
        setField("metadataUtils", metadataUtils);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = MetadataRemovePublicationDateListener.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(listener, value);
    }

    private Metadata metadataWithType(MetadataType type, int id, String uuid) {
        Metadata md = mock(Metadata.class);
        MetadataDataInfo dataInfo = mock(MetadataDataInfo.class);
        when(dataInfo.getType()).thenReturn(type);
        when(md.getDataInfo()).thenReturn(dataInfo);
        when(md.getId()).thenReturn(id);
        when(md.getUuid()).thenReturn(uuid);
        return md;
    }

    @Test
    public void doesNothingWhenSettingDisabled() {
        when(settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)).thenReturn(false);

        Metadata md = metadataWithType(MetadataType.METADATA, 1, "uuid-1");
        listener.onApplicationEvent(new MetadataUnpublished(md));

        verify(service, never()).removePublicationDate(any());
    }

    @Test
    public void doesNothingForTemplates() {
        when(settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)).thenReturn(true);

        Metadata md = metadataWithType(MetadataType.TEMPLATE, 1, "uuid-1");
        listener.onApplicationEvent(new MetadataUnpublished(md));

        verify(service, never()).removePublicationDate(any());
    }

    @Test
    public void removesPublicationDateForNormalMetadata() {
        when(settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)).thenReturn(true);

        Metadata md = metadataWithType(MetadataType.METADATA, 1, "uuid-1");
        listener.onApplicationEvent(new MetadataUnpublished(md));

        verify(service).removePublicationDate(eq(md));
    }

    @Test
    public void resolvesApprovedRecordWhenEventCarriesADraft() {
        when(settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)).thenReturn(true);

        MetadataDraft draft = mock(MetadataDraft.class);
        when(draft.getUuid()).thenReturn("uuid-1");

        Metadata approved = metadataWithType(MetadataType.METADATA, 42, "uuid-1");
        when(metadataUtils.findOneByUuid("uuid-1")).thenReturn(approved);

        listener.onApplicationEvent(new MetadataUnpublished(draft));

        // The approved record must be used, not the draft itself.
        verify(service).removePublicationDate(eq(approved));
    }

    @Test
    public void doesNothingWhenDraftCannotBeResolved() {
        when(settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)).thenReturn(true);

        MetadataDraft draft = mock(MetadataDraft.class);
        when(draft.getUuid()).thenReturn("uuid-1");
        when(metadataUtils.findOneByUuid("uuid-1")).thenReturn(null);

        listener.onApplicationEvent(new MetadataUnpublished(draft));

        verify(service, never()).removePublicationDate(any());
    }

    @Test
    public void serviceFailureDoesNotPropagate() {
        when(settingManager.getValueAsBool(Settings.SYSTEM_METADATAPRIVS_PUBLICATION_MANAGEPUBLICATIONDATE)).thenReturn(true);

        Metadata md = metadataWithType(MetadataType.METADATA, 1, "uuid-1");
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(service).removePublicationDate(any());

        // Must not throw: a failure removing the publication date must not break the unpublish action.
        listener.onApplicationEvent(new MetadataUnpublished(md));
    }
}
