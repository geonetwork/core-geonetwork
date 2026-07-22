//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.metadata;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.api.exception.NotAllowedException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the scheduled-publication authorization check
 * {@link MetadataPublicationService#checkUserCanPublishMetadata(ServiceContext, AbstractMetadata)}.
 *
 * <p>Scheduling a publication must require exactly the same authorization as an immediate
 * publication, because the scheduled publication job later performs the publication while running
 * with administrator privileges. These tests verify both gates are enforced: review permission on
 * the record and the configured publication profile on the record's group owner.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataPublicationServiceTest {

    private static final int METADATA_ID = 123;
    private static final String METADATA_UUID = "uuid-123";
    private static final int GROUP_OWNER = 12;

    @Mock
    private AccessManager accessManager;

    @Mock
    private SettingManager sm;

    @InjectMocks
    private MetadataPublicationService service;

    private AbstractMetadata mockMetadata() {
        AbstractMetadata metadata = mock(AbstractMetadata.class);
        when(metadata.getId()).thenReturn(METADATA_ID);
        MetadataSourceInfo sourceInfo = new MetadataSourceInfo();
        sourceInfo.setGroupOwner(GROUP_OWNER);
        when(metadata.getSourceInfo()).thenReturn(sourceInfo);
        return metadata;
    }

    private ServiceContext mockContext(UserSession session) {
        ServiceContext context = mock(ServiceContext.class);
        when(context.getUserSession()).thenReturn(session);
        return context;
    }

    @Test
    public void rejectsWhenUserHasNoReviewPermission() throws Exception {
        ServiceContext context = mock(ServiceContext.class);
        AbstractMetadata metadata = mock(AbstractMetadata.class);
        when(metadata.getId()).thenReturn(METADATA_ID);
        when(metadata.getUuid()).thenReturn(METADATA_UUID);

        when(accessManager.hasReviewPermission(context, String.valueOf(METADATA_ID))).thenReturn(false);

        assertThrows(NotAllowedException.class,
            () -> service.checkUserCanPublishMetadata(context, metadata));

        // The publication-profile gate must not even be reached when review permission is missing.
        verify(accessManager, never()).isProfileOnGroup(any(), any(), anyInt());
    }

    @Test
    public void rejectsWhenUserLacksConfiguredPublicationProfile() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getProfile()).thenReturn(Profile.Editor);
        ServiceContext context = mockContext(session);
        AbstractMetadata metadata = mockMetadata();

        when(accessManager.hasReviewPermission(context, String.valueOf(METADATA_ID))).thenReturn(true);
        when(sm.getValue(Settings.METADATA_PUBLISH_USERPROFILE)).thenReturn(Profile.Reviewer.name());
        when(accessManager.isProfileOnGroup(session, Profile.Reviewer, GROUP_OWNER)).thenReturn(false);

        assertThrows(NotAllowedException.class,
            () -> service.checkUserCanPublishMetadata(context, metadata));
    }

    @Test
    public void allowsWhenUserHasReviewPermissionAndConfiguredPublicationProfile() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getProfile()).thenReturn(Profile.Editor);
        ServiceContext context = mockContext(session);
        AbstractMetadata metadata = mockMetadata();

        when(accessManager.hasReviewPermission(context, String.valueOf(METADATA_ID))).thenReturn(true);
        when(sm.getValue(Settings.METADATA_PUBLISH_USERPROFILE)).thenReturn(Profile.Reviewer.name());
        when(accessManager.isProfileOnGroup(session, Profile.Reviewer, GROUP_OWNER)).thenReturn(true);

        // Should not throw.
        service.checkUserCanPublishMetadata(context, metadata);

        verify(accessManager).hasReviewPermission(context, String.valueOf(METADATA_ID));
        verify(accessManager).isProfileOnGroup(session, Profile.Reviewer, GROUP_OWNER);
    }

    @Test
    public void allowsAdministratorWithoutCheckingPublicationProfile() throws Exception {
        UserSession session = mock(UserSession.class);
        when(session.getProfile()).thenReturn(Profile.Administrator);
        ServiceContext context = mockContext(session);
        AbstractMetadata metadata = mockMetadata();

        when(accessManager.hasReviewPermission(context, String.valueOf(METADATA_ID))).thenReturn(true);

        // Should not throw.
        service.checkUserCanPublishMetadata(context, metadata);

        // Administrators bypass the configured publication-profile check.
        verify(accessManager, never()).isProfileOnGroup(any(), any(), anyInt());
    }
}
