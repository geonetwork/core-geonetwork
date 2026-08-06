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
package org.fao.geonet;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata_;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.TaskExecutionException;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * One-time backfill for the {@code access}/{@code mimetype} columns added to
 * {@code MetadataFileUploads}: walks every metadata record's public and private resources through
 * the configured {@link Store}, filling in {@code access}/{@code mimetype} on existing tracking
 * rows that predate those columns (deriving {@code access} from which visibility the file is
 * physically found under, since that was the sole source of truth before this ledger existed),
 * and creating a tracking row for any physically-stored file that never had one at all.
 * <p>
 * Idempotent and safe to interrupt/re-run: only rows missing {@code access} or {@code mimetype}
 * are touched, and - since Spring Data JPA commits each repository {@code save()} in its own
 * transaction rather than this task wrapping the whole run in one - progress already made survives
 * an interruption, so re-running simply skips everything already backfilled.
 * <p>
 * Invoke via {@code PUT /{portal}/api/tools/migration/steps/org.fao.geonet.MetadataFileUploadBackfillTask}
 * (Administrator only). Not run automatically at startup, since walking every resource of every
 * record can be a slow, remote-store-dependent operation an administrator should trigger
 * deliberately - the same reasoning as GeoNetwork's own admin-triggered full reindex.
 */
public class MetadataFileUploadBackfillTask implements ContextAwareTask {

    private static final int METADATA_BATCH_PAGE_SIZE = 50000;
    private static final String UNKNOWN_USER = "unknown";

    private int recordsProcessed;
    private int recordsSkipped;
    private int rowsCreated;
    private int rowsUpdated;

    @Override
    public void run(ApplicationContext applicationContext) throws TaskExecutionException {
        try {
            ServiceContext serviceContext = createServiceContext((ConfigurableApplicationContext) applicationContext);
            IMetadataUtils metadataUtils = applicationContext.getBean(IMetadataUtils.class);
            Store store = applicationContext.getBean("resourceStore", Store.class);
            MetadataFileUploadRepository uploadRepository = applicationContext.getBean(MetadataFileUploadRepository.class);

            Sort sortById = Sort.by(Sort.Direction.ASC, Metadata_.id.getName());
            int currentPage = 0;
            Page<Pair<Integer, ISODate>> results = metadataUtils.findAllIdsAndChangeDates(
                PageRequest.of(currentPage, METADATA_BATCH_PAGE_SIZE, sortById));

            while (results.getNumberOfElements() > 0) {
                for (Pair<Integer, ISODate> result : results) {
                    backfillRecord(serviceContext, metadataUtils, store, uploadRepository, result.one());
                }
                currentPage++;
                results = metadataUtils.findAllIdsAndChangeDates(
                    PageRequest.of(currentPage, METADATA_BATCH_PAGE_SIZE, sortById));
            }

            Log.info(Geonet.GEONETWORK, String.format(
                "MetadataFileUploadBackfillTask complete: %d record(s) processed (%d skipped due to errors), "
                    + "%d MetadataFileUploads row(s) created, %d updated.",
                recordsProcessed, recordsSkipped, rowsCreated, rowsUpdated));
        } catch (Exception e) {
            throw new TaskExecutionException(e);
        }
    }

    /**
     * Backfill one metadata record's public and private resources. Errors for a single record
     * are logged and skipped rather than aborting the whole run, since one bad/unreachable
     * resource (eg. a transient remote-store error) shouldn't block backfilling everything else.
     */
    private void backfillRecord(ServiceContext context, IMetadataUtils metadataUtils, Store store,
                                MetadataFileUploadRepository uploadRepository, int metadataId) {
        try {
            String metadataUuid = metadataUtils.getMetadataUuid(String.valueOf(metadataId));
            for (MetadataResourceVisibility visibility : MetadataResourceVisibility.values()) {
                List<MetadataResource> resources = store.getResources(context, metadataUuid, visibility, null, true);
                for (MetadataResource resource : resources) {
                    backfillResource(uploadRepository, metadataId, visibility, resource);
                }
            }
            recordsProcessed++;
        } catch (Exception e) {
            recordsSkipped++;
            Log.warning(Geonet.GEONETWORK, String.format(
                "MetadataFileUploadBackfillTask: skipped metadata id %d due to error: %s", metadataId, e.getMessage()));
        }
    }

    private void backfillResource(MetadataFileUploadRepository uploadRepository, int metadataId,
                                  MetadataResourceVisibility visibility, MetadataResource resource) {
        MetadataFileUpload upload;
        try {
            upload = uploadRepository.findByMetadataIdAndFileNameNotDeleted(metadataId, resource.getFilename());
        } catch (EmptyResultDataAccessException e) {
            upload = null;
        }

        if (upload == null) {
            upload = new MetadataFileUpload();
            upload.setMetadataId(metadataId);
            upload.setFileName(resource.getFilename());
            upload.setFileSize((double) resource.getSize());
            // No real upload date is knowable for a file that predates this ledger; the file's
            // own last-modification time is the closest honest substitute.
            upload.setUploadDate(new ISODate(resource.getLastModification().getTime(), false).toString());
            upload.setUserName(UNKNOWN_USER);
            upload.setAccess(visibility);
            upload.setMimeType(resource.getMimeType());
            uploadRepository.save(upload);
            rowsCreated++;
            return;
        }

        boolean changed = false;
        if (upload.getAccess() == null) {
            upload.setAccess(visibility);
            changed = true;
        }
        if (upload.getMimeType() == null) {
            upload.setMimeType(resource.getMimeType());
            changed = true;
        }
        if (changed) {
            uploadRepository.save(upload);
            rowsUpdated++;
        }
    }

    private ServiceContext createServiceContext(ConfigurableApplicationContext applicationContext) {
        ServiceManager serviceManager = applicationContext.getBean(ServiceManager.class);
        ServiceContext serviceContext = serviceManager.createServiceContext("metadataFileUploadBackfill", applicationContext);
        serviceContext.setLanguage("eng");
        loginAsAdmin(applicationContext, serviceContext);
        serviceContext.setAsThreadLocal();
        ApplicationContextHolder.set(applicationContext);
        return serviceContext;
    }

    private void loginAsAdmin(ApplicationContext applicationContext, ServiceContext serviceContext) {
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        List<User> adminUsers = userRepository.findAll(
            UserSpecs.hasProfile(Profile.Administrator), PageRequest.of(0, 1)).getContent();
        if (adminUsers.isEmpty()) {
            throw new IllegalStateException("The system does not have an admin user");
        }
        UserSession session = new UserSession();
        session.loginAs(adminUsers.get(0));
        serviceContext.setUserSession(session);
    }
}
