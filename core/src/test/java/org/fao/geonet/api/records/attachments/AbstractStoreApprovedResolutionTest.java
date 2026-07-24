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
package org.fao.geonet.api.records.attachments;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataDraftRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the approved-version resolution used by the attachments store (issue #9433).
 *
 * <p>The {@code approved} flag reported for a resource must reflect whether an approved copy of the
 * record actually exists (i.e. the resolved record is not a draft), not merely the {@code approved}
 * request parameter. The internal id is taken from the draft table when a draft/working copy
 * exists, otherwise from the approved metadata table, and its draft state is evaluated with
 * {@link IMetadataUtils#isMetadataDraft(int)}. These tests cover the decision in isolation;
 * {@link FilesystemStoreTest} covers the end-to-end store behaviour.</p>
 *
 * <p>{@link Proxy}-backed beans and a real {@link StaticApplicationContext} are used instead of
 * Mockito so the test has no bytecode-instrumentation dependency.</p>
 */
public class AbstractStoreApprovedResolutionTest {

    // Approved record: present in the approved table (id 1), not a draft.
    private static final String APPROVED_UUID = "approved-uuid";
    private static final int APPROVED_ID = 1;
    // Working copy: present in the draft table (id 2), is a draft.
    private static final String DRAFT_TABLE_UUID = "draft-table-uuid";
    private static final int DRAFT_TABLE_ID = 2;
    // New, never-approved record: present in the approved table (id 3) but with draft state.
    private static final String DRAFT_STATUS_UUID = "draft-status-uuid";
    private static final int DRAFT_STATUS_ID = 3;
    // Unknown record: absent from both tables.
    private static final String MISSING_UUID = "missing-uuid";

    @Before
    public void setUp() {
        MetadataDraftRepository draftRepository = proxy(MetadataDraftRepository.class, (proxy, method, args) -> {
            if ("findOneByUuid".equals(method.getName())) {
                return DRAFT_TABLE_UUID.equals(args[0]) ? (MetadataDraft) new MetadataDraft().setId(DRAFT_TABLE_ID) : null;
            }
            return objectMethodDefault(proxy, method, args);
        });

        MetadataRepository metadataRepository = proxy(MetadataRepository.class, (proxy, method, args) -> {
            if ("findOneByUuid".equals(method.getName())) {
                if (APPROVED_UUID.equals(args[0])) {
                    return (Metadata) new Metadata().setId(APPROVED_ID);
                }
                if (DRAFT_STATUS_UUID.equals(args[0])) {
                    return (Metadata) new Metadata().setId(DRAFT_STATUS_ID);
                }
                return null;
            }
            return objectMethodDefault(proxy, method, args);
        });

        IMetadataUtils metadataUtils = proxy(IMetadataUtils.class, (proxy, method, args) -> {
            if ("isMetadataDraft".equals(method.getName())) {
                int id = (Integer) args[0];
                // The working copy and the never-approved record are drafts; the approved one is not.
                return id == DRAFT_TABLE_ID || id == DRAFT_STATUS_ID;
            }
            return objectMethodDefault(proxy, method, args);
        });

        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("metadataDraftRepository", draftRepository);
        context.getBeanFactory().registerSingleton("metadataRepository", metadataRepository);
        context.getBeanFactory().registerSingleton("metadataUtils", metadataUtils);
        ApplicationContextHolder.set(context);
    }

    @After
    public void tearDown() {
        ApplicationContextHolder.clear();
    }

    @Test
    public void approvedCopyExistsWhenResolvedRecordIsNotADraft() throws Exception {
        assertTrue("An approved (non-draft) record has an approved copy",
            AbstractStore.approvedCopyExists(APPROVED_UUID));
    }

    @Test
    public void noApprovedCopyWhenOnlyAWorkingCopyExists() throws Exception {
        // The id is taken from the draft table first; that draft is not approved.
        assertFalse("A working copy resolved from the draft table is a draft, so no approved copy",
            AbstractStore.approvedCopyExists(DRAFT_TABLE_UUID));
    }

    @Test
    public void noApprovedCopyWhenRecordHasNeverBeenApproved() throws Exception {
        // Present in the approved table but still in a draft state (never approved).
        assertFalse("A never-approved record is a draft, so no approved copy",
            AbstractStore.approvedCopyExists(DRAFT_STATUS_UUID));
    }

    @Test
    public void noApprovedCopyWhenRecordIsMissing() throws Exception {
        assertFalse("A record absent from both tables has no approved copy",
            AbstractStore.approvedCopyExists(MISSING_UUID));
    }

    @Test
    public void resolveApprovedIsTrueOnlyWhenApprovedCopyExists() throws Exception {
        assertTrue("approved=true must be honoured when an approved copy exists",
            AbstractStore.resolveApproved(APPROVED_UUID, true));
        assertFalse("approved=true must NOT be honoured for a draft/working copy (#9433)",
            AbstractStore.resolveApproved(DRAFT_TABLE_UUID, true));
        assertFalse("approved=true must NOT be honoured for a never-approved record (#9433)",
            AbstractStore.resolveApproved(DRAFT_STATUS_UUID, true));
    }

    @Test
    public void resolveApprovedIsFalseWhenNotRequested() throws Exception {
        assertFalse("approved=false must never resolve to approved",
            AbstractStore.resolveApproved(APPROVED_UUID, false));
        assertFalse("a null approved parameter must never resolve to approved",
            AbstractStore.resolveApproved(APPROVED_UUID, null));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }

    private static Object objectMethodDefault(Object proxy, java.lang.reflect.Method method, Object[] args) {
        switch (method.getName()) {
            case "toString":
                return method.getDeclaringClass().getSimpleName() + "Stub";
            case "hashCode":
                return System.identityHashCode(proxy);
            case "equals":
                return proxy == args[0];
            default:
                return null;
        }
    }
}
