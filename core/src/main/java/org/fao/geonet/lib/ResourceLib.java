//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.lib;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.OperationNotAllowedEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

/**
 * Utility class to deal with data and removed directory. Also provide user privileges checking
 * method.
 */
@Component
public class ResourceLib {

    /**
     * Get metadata public or private data directory
     *
     * @param access The type of data directory. {@link Params.Access#PUBLIC}
     *               or {@link Params.Access#PRIVATE}
     * @param id     The metadata identifier
     * @return The metadata directory
     */
    public Path getDir(String access, int id) {
        Path mdDir = getMetadataDir(ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class), id);
        String subDir = (access != null && access.equals(Params.Access.PUBLIC)) ? Params.Access.PUBLIC
            : Params.Access.PRIVATE;
        return mdDir.resolve(subDir);
    }

    public Path getMetadataDir(GeonetworkDataDirectory dataDirectory, int id) {
        return getMetadataDir(dataDirectory, id + "");
    }

    /**
     * Get the metadata data directory
     *
     * @param id The metadata identifier
     * @return The metadata data directory
     */
    public Path getMetadataDir(GeonetworkDataDirectory dataDirectory, String id) {
        Path dataDir = dataDirectory.getMetadataDataDir();
        return getMetadataDir(dataDir, id);
    }

    /**
     * Get the metadata data directory
     *
     * @param dataDir The data directory
     * @param id      The metadata identifier
     * @return The metadata data directory
     */
    public Path getMetadataDir(Path dataDir, String id) {
        String group = pad(Integer.parseInt(id) / 100, 3);
        String groupDir = group + "00-" + group + "99";
        return dataDir.resolve(groupDir).resolve(id);
    }

    /**
     * Check that the operation is allowed for current user. See {@link
     * AccessManager#getOperations(ServiceContext, String, String)}.
     *
     * @param id        The metadata identifier
     * @param operation See {@link AccessManager}.
     */
    public void checkPrivilege(ServiceContext context, String id,
                               ReservedOperation operation) throws Exception {
        AccessManager accessMan = context.getBean(AccessManager.class);

        Set<Operation> hsOper = accessMan.getOperations(context, id, context.getIpAddress());

        for (Operation op : hsOper) {
            if (op.is(operation)) {
                return;
            }
        }
        denyAccess(context);
    }

    public void denyAccess(ServiceContext context) throws AccessDeniedException, OperationNotAllowedEx {
        if (context.getUserSession().isAuthenticated()) {
            throw new AccessDeniedException("User is not permitted to access this resource");
        } else {
            throw new OperationNotAllowedEx();
        }
    }

    public void checkEditPrivilege(ServiceContext context, String id)
        throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        AccessManager am = gc.getBean(AccessManager.class);

        if (!am.canEdit(context, id))
            denyAccess(context);
    }

    /**
     * @return the absolute path of the folder choosen to store all deleted metadata
     */
    public Path getRemovedDir(int id) {
        ApplicationContext appContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = appContext.getBean(GeonetworkDataDirectory.class);
        return getRemovedDir(dataDirectory.getBackupDir(), String.valueOf(id));
    }

    /**
     * @return the absolute path of the folder where the given metadata should be stored when it is
     * removed
     */
    public Path getRemovedDir(Path removedDir, String id) {
        String group = pad(Integer.parseInt(id) / 100, 3);
        String groupDir = group + "00-" + group + "99";

        return removedDir.resolve(groupDir);
    }

    // -----------------------------------------------------------------------------
    // ---
    // --- Private methods
    // ---
    // -----------------------------------------------------------------------------

    private String pad(int group, int length) {
        String text = Integer.toString(group);

        while (text.length() < length)
            text = "0" + text;

        return text;
    }
}
