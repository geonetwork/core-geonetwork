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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class StoreUtils {
    /**
     * Copy the attachments of one metadata to the other.
     * @param context
     * @param oldMetadataId               The source metadata ID
     * @param newMetadataId               The destination metadata ID
     * @param newApproved                 New approved flag to use on the destination.
     *                                    Used when creating a working copy where the destination will have the approved flag to false.
     * @throws Exception
     */
    public static void copyDataDir(ServiceContext context, int oldMetadataId, int newMetadataId, boolean newApproved) throws Exception {
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String oldUuid = metadataUtils.getMetadataUuid(String.valueOf(oldMetadataId));
        final String newUuid = metadataUtils.getMetadataUuid(String.valueOf(newMetadataId));

        copyDataDir(context, oldUuid, newUuid, newApproved);
    }

    /**
     * Copy the attachments of one metadata to the other.
     * @param context
     * @param oldUuid               The source metadata ID
     * @param newUuid               The destination metadata ID
     * @param newApproved           New approved flag to use on the destination.
     *                              Used when creating a working copy where the destination will have the approved flag to false.
     * @throws Exception
     */
    public static void copyDataDir(ServiceContext context, String oldUuid, String newUuid, boolean newApproved) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        for (MetadataResourceVisibility visibility: MetadataResourceVisibility.values()) {
            // Copy from approved copy to working copy
            boolean oldApproved = true;
            store.copyResources(context, oldUuid, newUuid, visibility, oldApproved, newApproved);
        }
    }

    /**
     * Used to remove items from a metadataResource list
     *
     * @param l left list
     * @param r right list
     * @return l except r
     */
    private static List<MetadataResource> exceptMetadataResource(List<MetadataResource> l, List<MetadataResource> r)  {
        Map<String,MetadataResource> rMap = new HashMap<>();
        for (MetadataResource mr : r) rMap.put(mr.getId(),mr);

        // Start the results with a copy of the l list.
        List<MetadataResource> results = new ArrayList<>(l);

        Iterator<MetadataResource> itResults = results.iterator();
        // remove all that exist in rMap
        while (itResults.hasNext()) {
            MetadataResource m = itResults.next();
            if (rMap.containsKey(m.getId())) {
                MetadataResource mCheck = rMap.get(m.getId());
                if (mCheck.getFilename().equals(m.getFilename())
                    && mCheck.getMetadataUuid().equals(m.getMetadataUuid())
                    && mCheck.getVisibility().equals(m.getVisibility())) {
                    itResults.remove();
                }
            }
        }
        return results;
    }

    /**
     * Replace the attachments of one source metadata to the other target metadata
     * Used for moving draft metadata resources to approved metadata resources
     *
     *
     * @param context
     * @param sourceUuid            The source metadata ID
     * @param targetUuid            The destination metadata ID
     * @param sourceApproved        Old approved flag to use on the destination.
     *                              Used when creating a working copy where the source will have the approved flag to false.
     * @param targetApproved        New approved flag to use on the destination.
     *                              Used when creating a working copy where the destination will have the approved flag to false.
     * @throws Exception
     */

    public static void replaceDataDir(ServiceContext context, String sourceUuid, String targetUuid, boolean sourceApproved, boolean targetApproved) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        for (MetadataResourceVisibility visibility: MetadataResourceVisibility.values()) {
            final List<MetadataResource> sourceResources = store.getResources(context, sourceUuid, visibility, null, sourceApproved);
            final List<MetadataResource> targetResources = store.getResources(context, targetUuid, visibility, null, targetApproved);

            // In order to sync the 2 folders, we need to identify the records to be added, deleted and updated.
            List<MetadataResource> targetDeleteResources  = exceptMetadataResource(targetResources, sourceResources);

            // copy records from source to target
            store.copyResources(context, sourceUuid, targetUuid, visibility, sourceApproved, targetApproved);

            // delete old records
            for (MetadataResource resource: targetDeleteResources) {
                store.delResource(context, targetUuid, visibility, resource.getFilename(), targetApproved);
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
            try (
              Store.ResourceHolder holder = store.getResource(context, metadataUuid, resource.getVisibility(),
                    resource.getFilename(), approved);
              InputStream inputStream = holder.getResource().getInputStream();
            ) {
                Files.copy(inputStream, destinationDir.resolve(resource.getFilename()));
            }
        }
    }
}
