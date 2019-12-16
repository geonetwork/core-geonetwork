/*
 * =============================================================================
 * ===	Copyright (C) 2019 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.api.records.attachments;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStore implements Store {
    @Override
    public final List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid, final Sort sort,
            final String filter) throws Exception {
        return getResources(context, metadataUuid, sort, filter, true);
    }

    @Override
    public List<MetadataResource> getResources(final ServiceContext context, final String metadataUuid,
            final MetadataResourceVisibility metadataResourceVisibility, final String filter) throws Exception {
        return getResources(context, metadataUuid, metadataResourceVisibility, filter, true);
    }

    @Override
    public List<MetadataResource> getResources(ServiceContext context, String metadataUuid, Sort sort, String filter, Boolean approved)
            throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        boolean canEdit = getAccessManager(context).canEdit(context, String.valueOf(metadataId));

        List<MetadataResource> resourceList = new ArrayList<>(
                getResources(context, metadataUuid, MetadataResourceVisibility.PUBLIC, filter, approved));
        if (canEdit) {
            resourceList.addAll(getResources(context, metadataUuid, MetadataResourceVisibility.PRIVATE, filter, approved));
        }

        if (sort == Sort.name) {
            resourceList.sort(MetadataResourceVisibility.sortByFileName);
        }

        return resourceList;
    }

    @Override
    public final ResourceHolder getResource(ServiceContext context, String metadataUuid, String resourceId) throws Exception {
        return getResource(context, metadataUuid, resourceId, true);
    }

    @Override
    public final ResourceHolder getResource(ServiceContext context, String metadataUuid, String resourceId, Boolean approved)
            throws Exception {
        try {
            return getResource(context, metadataUuid, MetadataResourceVisibility.PUBLIC, resourceId, approved);
        } catch (ResourceNotFoundException ignored) {
        }
        return getResource(context, metadataUuid, MetadataResourceVisibility.PRIVATE, resourceId, approved);
    }

    protected static AccessManager getAccessManager(final ServiceContext context) {
        return context.getBean(AccessManager.class);
    }

    public static int getAndCheckMetadataId(String metadataUuid, Boolean approved) throws Exception {
        final ApplicationContext _appContext = ApplicationContextHolder.get();
        final AbstractMetadata metadata;
        if (approved) {
            metadata = _appContext.getBean(MetadataRepository.class).findOneByUuid(metadataUuid);
        } else {
            metadata = _appContext.getBean(IMetadataUtils.class).findOneByUuid(metadataUuid);
        }
        if (metadata == null) {
            throw new ResourceNotFoundException(String.format("Metadata with UUID '%s' not found.", metadataUuid));
        }
        return metadata.getId();
    }

    protected int canEdit(ServiceContext context, String metadataUuid, Boolean approved) throws Exception {
        return canEdit(context, metadataUuid, null, approved);
    }

    protected int canEdit(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        boolean canEdit = getAccessManager(context).canEdit(context, String.valueOf(metadataId));
        if ((visibility == null && !canEdit) || (visibility == MetadataResourceVisibility.PRIVATE && !canEdit)) {
            throw new SecurityException(String.format("User '%s' does not have privileges to access '%s' resources for metadata '%s'.",
                                                      context.getUserSession() != null ?
                                                              context.getUserSession().getUsername() + "/" + context.getUserSession()
                                                                      .getProfile() :
                                                              "anonymous", visibility == null ? "any" : visibility, metadataUuid));
        }
        return metadataId;
    }

    protected int canDownload(ServiceContext context, String metadataUuid, MetadataResourceVisibility visibility, Boolean approved)
            throws Exception {
        int metadataId = getAndCheckMetadataId(metadataUuid, approved);
        if (visibility == MetadataResourceVisibility.PRIVATE) {
            boolean canDownload = getAccessManager(context).canDownload(context, String.valueOf(metadataId));
            if (!canDownload) {
                throw new SecurityException(String.format(
                        "Current user can't download resources for metadata '%s' and as such can't access the requested resource.",
                        metadataUuid));
            }
        }
        return metadataId;
    }

    protected String getFilenameFromUrl(final URL fileUrl) {
        String fileName = FilenameUtils.getName(fileUrl.getPath());
        if (fileName.contains("?")) {
            fileName = fileName.substring(0, fileName.indexOf("?"));
        }
        return fileName;
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final MultipartFile file,
            final MetadataResourceVisibility visibility) throws Exception {
        return putResource(context, metadataUuid, file.getOriginalFilename(), file.getInputStream(), null, visibility, true);
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final MultipartFile file,
            final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        return putResource(context, metadataUuid, file.getOriginalFilename(), file.getInputStream(), null, visibility, approved);
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final Path file,
            final MetadataResourceVisibility visibility) throws Exception {
        return putResource(context, metadataUuid, file, visibility, true);
    }

    @Override
    public final MetadataResource putResource(final ServiceContext context, final String metadataUuid, final Path file,
            final MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        final InputStream is = new BufferedInputStream(Files.newInputStream(file));
        return putResource(context, metadataUuid, file.getFileName().toString(), is, null, visibility, approved);
    }

    @Override
    public final MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl,
            MetadataResourceVisibility visibility) throws Exception {
        return putResource(context, metadataUuid, fileUrl, visibility, true);
    }

    @Override
    public final MetadataResource putResource(ServiceContext context, String metadataUuid, URL fileUrl,
            MetadataResourceVisibility visibility, Boolean approved) throws Exception {
        return putResource(context, metadataUuid, getFilenameFromUrl(fileUrl), fileUrl.openStream(), null, visibility, approved);
    }

    @Override
    public String delResources(final ServiceContext context, final String metadataUuid) throws Exception {
        return delResources(context, metadataUuid, true);
    }

    @Override
    public String delResource(final ServiceContext context, final String metadataUuid, final String resourceId) throws Exception {
        return delResource(context, metadataUuid, resourceId, true);
    }

    @Override
    public MetadataResource patchResourceStatus(final ServiceContext context, final String metadataUuid, final String resourceId,
            final MetadataResourceVisibility metadataResourceVisibility) throws Exception {
        return patchResourceStatus(context, metadataUuid, resourceId, metadataResourceVisibility, true);
    }

    protected String getFilename(final String metadataUuid, final String resourceId) {
        // It's not always clear when we get a resourceId or a filename
        String prefix = metadataUuid + "/attachements/";
        if (resourceId.startsWith(prefix)) {
            // It was a resourceId
            return resourceId.substring(prefix.length());
        } else {
            // It was a filename
            return resourceId;
        }
    }

    protected void checkResourceId(final String resourceId) {
        if (resourceId.contains("..") || resourceId.startsWith("/") || resourceId.startsWith("file:/")) {
            throw new SecurityException(String.format("Invalid resource identifier '%s'.", resourceId));
        }
    }
}
