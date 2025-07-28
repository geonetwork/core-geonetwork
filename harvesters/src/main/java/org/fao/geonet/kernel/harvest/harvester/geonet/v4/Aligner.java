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


import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkAligner;
import org.fao.geonet.kernel.harvest.harvester.geonet.v4.client.GeoNetwork4ApiClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Aligner extends BaseGeoNetworkAligner<GeonetParams> {

    private final GeoNetwork4ApiClient geoNetworkApiClient;

    public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context,
                   GeonetParams params, List<org.fao.geonet.domain.Group> groups) {
        super(cancelMonitor, log, context, params);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        geoNetworkApiClient = gc.getBean(GeoNetwork4ApiClient.class);

        //--- save remote categories and groups into hashmaps for fast access
        if (groups != null) {
            setupLocalGroup(groups, hmRemoteGroups);
        }
    }

    //--------------------------------------------------------------------------

    private void setupLocalGroup(List<org.fao.geonet.domain.Group> list, Map<String, Map<String, String>> hmEntity) {

        for (org.fao.geonet.domain.Group group : list) {
            String name = group.getName();

            Map<String, String> hm = new HashMap<>();
            hmEntity.put(name, hm);
            hm.putAll(group.getLabelTranslations());
        }
    }

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

        return geoNetworkApiClient.retrieveMEF( params.host + "/" + params.getNode(), uuid, username, password);
    }

}
