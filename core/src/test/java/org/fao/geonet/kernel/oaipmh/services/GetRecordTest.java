//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.oaipmh.services;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.oaipmh.exceptions.IdDoesNotExistException;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Access control tests for {@link GetRecord#buildRecordStat}.
 *
 * A record the caller is not allowed to view must be reported as
 * {@code idDoesNotExist} (so it is indistinguishable from an unknown record and
 * {@code ListRecords} can skip it), while an unrelated failure during the
 * privilege check must keep propagating rather than being turned into a denial.
 */
public class GetRecordTest {

    private static final String PREFIX = "iso19139";

    /**
     * A {@link ServiceContext} whose metadata lookup returns a single record and
     * whose {@link AccessManager} and authentication state are those provided.
     */
    @SuppressWarnings("unchecked")
    private ServiceContext mockContext(AccessManager accessManager, boolean authenticated) throws Exception {
        ServiceContext context = mock(ServiceContext.class);

        GeonetContext gc = mock(GeonetContext.class);
        when(context.getHandlerContext(Geonet.CONTEXT_NAME)).thenReturn(gc);

        AbstractMetadata metadata = mock(AbstractMetadata.class);
        when(metadata.getId()).thenReturn(1);
        when(metadata.getUuid()).thenReturn("restricted-uuid");

        IMetadataUtils metadataUtils = mock(IMetadataUtils.class);
        when(metadataUtils.findOne(any(Specification.class))).thenReturn(metadata);

        when(context.getBean(IMetadataUtils.class)).thenReturn(metadataUtils);
        when(context.getBean(AccessManager.class)).thenReturn(accessManager);

        UserSession session = mock(UserSession.class);
        when(session.isAuthenticated()).thenReturn(authenticated);
        when(context.getUserSession()).thenReturn(session);

        return context;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void anonymousCallerWithoutViewPrivilegeGetsIdDoesNotExist() throws Exception {
        AccessManager accessManager = mock(AccessManager.class);
        when(accessManager.getOperations(any(), any(), any())).thenReturn(Collections.emptySet());

        ServiceContext context = mockContext(accessManager, false);

        assertThrows(IdDoesNotExistException.class, () ->
            GetRecord.buildRecordStat(context, mock(Specification.class), PREFIX));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void authenticatedCallerWithoutViewPrivilegeGetsIdDoesNotExist() throws Exception {
        AccessManager accessManager = mock(AccessManager.class);
        when(accessManager.getOperations(any(), any(), any())).thenReturn(Collections.emptySet());

        ServiceContext context = mockContext(accessManager, true);

        assertThrows(IdDoesNotExistException.class, () ->
            GetRecord.buildRecordStat(context, mock(Specification.class), PREFIX));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void unexpectedErrorDuringPrivilegeCheckIsNotMaskedAsIdDoesNotExist() throws Exception {
        AccessManager accessManager = mock(AccessManager.class);
        when(accessManager.getOperations(any(), any(), any()))
            .thenThrow(new IllegalStateException("boom"));

        ServiceContext context = mockContext(accessManager, false);

        assertThrows(IllegalStateException.class, () ->
            GetRecord.buildRecordStat(context, mock(Specification.class), PREFIX));
    }
}
