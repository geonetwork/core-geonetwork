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
package org.fao.geonet.api.userfeedback.service;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.userfeedback.UserFeedback;
import org.fao.geonet.domain.userfeedback.UserFeedback.UserRatingStatus;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.userfeedback.RatingRepository;
import org.fao.geonet.repository.userfeedback.UserFeedbackRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserFeedbackDatabaseServiceTest {

    @Mock
    private IMetadataUtils dataManager;

    @Mock
    private MetadataRepository metadataRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private UserFeedbackRepository userFeedbackRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFeedbackDatabaseService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void removeUserFeedbackDeletesChildrenBeforeParent() throws Exception {
        Metadata metadata = new Metadata();
        metadata.setId(12);
        metadata.setUuid("metadata-uuid");

        UserFeedback parent = new UserFeedback();
        parent.setUuid("parent");
        parent.setMetadata(metadata);

        UserFeedback child = new UserFeedback();
        child.setUuid("child");
        child.setMetadata(metadata);

        UserFeedback grandChild = new UserFeedback();
        grandChild.setUuid("grand-child");
        grandChild.setMetadata(metadata);

        when(userFeedbackRepository.findByUuid("parent")).thenReturn(parent);
        when(userFeedbackRepository.findByParent_Uuid("parent")).thenReturn(Collections.singletonList(child));
        when(userFeedbackRepository.findByParent_Uuid("child")).thenReturn(Collections.singletonList(grandChild));
        when(userFeedbackRepository.findByParent_Uuid("grand-child")).thenReturn(Collections.emptyList());
        when(userFeedbackRepository.findByMetadata_UuidAndStatusOrderByCreationDateDesc(
            eq("metadata-uuid"), eq(UserRatingStatus.PUBLISHED), isNull()))
            .thenReturn(Collections.emptyList());

        service.removeUserFeedback("parent", "127.0.0.1");

        org.mockito.InOrder inOrder = inOrder(userFeedbackRepository);
        inOrder.verify(userFeedbackRepository).findByUuid("parent");
        inOrder.verify(userFeedbackRepository).findByParent_Uuid("parent");
        inOrder.verify(userFeedbackRepository).findByParent_Uuid("child");
        inOrder.verify(userFeedbackRepository).findByParent_Uuid("grand-child");
        inOrder.verify(userFeedbackRepository).delete(grandChild);
        inOrder.verify(userFeedbackRepository).delete(child);
        inOrder.verify(userFeedbackRepository).delete(parent);
        inOrder.verify(userFeedbackRepository).findByMetadata_UuidAndStatusOrderByCreationDateDesc(
            "metadata-uuid", UserRatingStatus.PUBLISHED, null);

        verify(dataManager).rateMetadata(12, "127.0.0.1", 0);
    }
}
