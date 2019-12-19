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
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class StoreUtils {
    /**
     * Copy the attachments of one metadata to the other.
     * @param context
     * @param oldMetadataId               The source metadata ID
     * @param newMetadataId               The destination metadata ID
     * @throws Exception
     */
    public static void copyDataDir(ServiceContext context, int oldMetadataId, int newMetadataId, boolean approved) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String oldUuid = metadataUtils.getMetadataUuid(String.valueOf(oldMetadataId));
        final String newUuid = metadataUtils.getMetadataUuid(String.valueOf(newMetadataId));
        for (MetadataResourceVisibility visibility: MetadataResourceVisibility.values()) {
            final List<MetadataResource> resources = store.getResources(context, oldUuid, visibility, null, approved);
            for (MetadataResource resource: resources) {
                try (Store.ResourceHolder holder = store.getResource(context, oldUuid, visibility, resource.getFilename(), approved)) {
                    store.putResource(context, newUuid, holder.getPath(), visibility, approved);
                }
            }
        }
    }

    /**
     * Copy the attachments of one metadata to the other.
     * @param context
     * @param oldUuid               The source metadata ID
     * @param newUuid               The destination metadata ID
     * @throws Exception
     */
    public static void copyDataDir(ServiceContext context, String oldUuid, String newUuid, boolean approved) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        for (MetadataResourceVisibility visibility: MetadataResourceVisibility.values()) {
            final List<MetadataResource> resources = store.getResources(context, oldUuid, visibility, null, approved);
            for (MetadataResource resource: resources) {
                try (Store.ResourceHolder holder = store.getResource(context, oldUuid, visibility, resource.getFilename(), approved)) {
                    store.putResource(context, newUuid, holder.getPath(), visibility, approved);
                }
            }
        }
    }

    /**
     * Copy all the given attachments to the target directory.
     * @param context
     * @param metadataUuid The source metadata UUID
     * @param resources The attachments to copy
     * @param destinationDir The destination
     * @throws Exception
     */
    public static void extract(final ServiceContext context, final String metadataUuid, final List<MetadataResource> resources,
            final Path destinationDir, boolean approved) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        Files.createDirectories(destinationDir);
        for (MetadataResource resource: resources) {
            try (Store.ResourceHolder holder = store.getResource(context, metadataUuid, resource.getVisibility(), resource.getFilename(),
                    approved)) {
                Files.copy(Files.newInputStream(holder.getPath()), destinationDir.resolve(resource.getFilename()));
            }
        }
    }
}
