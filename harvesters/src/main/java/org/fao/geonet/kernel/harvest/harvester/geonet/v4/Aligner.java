//=============================================================================
//===    Copyright (C) 2001-2025 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.geonet.v4;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkAligner;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.GeoNetwork4ApiClient;

/**
 * The Aligner class provides functionality for alignment with a GeoNetwork instance.
 * It extends the {@code BaseGeoNetworkAligner} class to implement specific behavior
 * for working with GeoNetwork version 4 API.
 * <p>
 * This class is responsible for:
 * - Managing the retrieval of Metadata Exchange Format (MEF) files from a remote GeoNetwork instance.
 * - Setting up local group information for efficient access when working with GeoNetwork groups.
 */
public class Aligner extends BaseGeoNetworkAligner<GeonetParams> {

    private final GeoNetwork4ApiClient geoNetwork4ApiClient;

    /**
     * Constructs an instance of the Aligner class that manages the alignment process
     * with a remote GeoNetwork instance and sets up the local groups for fast access.
     *
     * @param cancelMonitor an {@code AtomicBoolean} object to monitor and handle cancellation of the alignment process.
     * @param log           a {@code Logger} instance used to log messages for debugging, information, warnings, or errors.
     * @param context       a {@code ServiceContext} object that provides access to the application context, containing necessary services.
     * @param params        a {@code GeonetParams} object containing configuration settings and connection details for the GeoNetwork instance.
     * @param groups        a {@code List} of {@code org.fao.geonet.domain.Group} objects representing available groups to set up for alignment.
     */
    public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context,
                   GeonetParams params, List<org.fao.geonet.domain.Group> groups) {
        super(cancelMonitor, log, context, params);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        geoNetwork4ApiClient = gc.getBean(GeoNetwork4ApiClient.class);

        //--- save remote categories and groups into hashmaps for fast access
        if (groups != null) {
            setupLocalGroup(groups, hmRemoteGroups);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Sets up a local group information mapping by populating the provided map
     * with group names and their corresponding label translations.
     *
     * @param list     a {@code List} of {@code org.fao.geonet.domain.Group} objects, each representing a group to be processed.
     * @param hmEntity a {@code Map} where the group names (as {@code String}) are used as keys,
     *                 and their corresponding label translations (as {@code Map<String, String>}) are stored as values.
     */
    private void setupLocalGroup(List<org.fao.geonet.domain.Group> list, Map<String, Map<String, String>> hmEntity) {

        for (org.fao.geonet.domain.Group group : list) {
            String name = group.getName();

            Map<String, String> hm = new HashMap<>();
            hmEntity.put(name, hm);
            hm.putAll(group.getLabelTranslations());
        }
    }

    /**
     * Retrieves a Metadata Exchange Format (MEF) file for a specific record identified by its UUID
     * from a GeoNetwork 4.x server. The MEF file is fetched using the {@code geoNetworkApiClient} and
     * stored temporarily on the filesystem. Authentication can be configured via the provided parameters.
     *
     * @param uuid the unique identifier (UUID) of the record for which the MEF file is to be retrieved
     * @return a {@code Path} representing the location of the downloaded MEF file
     * @throws URISyntaxException if the server URL is malformed
     * @throws IOException        if an error occurs during network or file operations
     */
    @Override
    protected Path retrieveMEF(String uuid) throws URISyntaxException, IOException {
        String username;
        String password;

        if (params.isUseAccount()) {
            username = params.getUsername();
            password = params.getPassword();
        } else {
            username = "";
            password = "";
        }

        return geoNetwork4ApiClient.retrieveMEF(params.host + "/" + params.getNode(), uuid, username, password);
    }

}
