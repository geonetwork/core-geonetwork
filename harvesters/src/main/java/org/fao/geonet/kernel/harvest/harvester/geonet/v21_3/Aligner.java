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

package org.fao.geonet.kernel.harvest.harvester.geonet.v21_3;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;

import org.fao.geonet.kernel.harvest.harvester.geonet.BaseGeoNetworkAligner;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class Aligner extends BaseGeoNetworkAligner<GeonetParams> {

    private XmlRequest request;

    public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, XmlRequest req,
                   GeonetParams params, Element remoteInfo) {
        super(cancelMonitor, log, context, params);
        this.request = req;

        //--- save remote categories and groups into hashmaps for fast access

        // Before 2.11 response contains groups. Now group is used.
        Element groups = remoteInfo.getChild("groups");
        if (groups == null) {
            groups = remoteInfo.getChild("group");
        }
        if (groups != null) {
            @SuppressWarnings("unchecked")
            List<Element> list = groups.getChildren("group");
            setupLocEntity(list, hmRemoteGroups);
        }
    }

    //--------------------------------------------------------------------------

    private void setupLocEntity(List<Element> list, Map<String, Map<String, String>> hmEntity) {

        for (Element entity : list) {
            String name = entity.getChildText("name");

            Map<String, String> hm = new HashMap<>();
            hmEntity.put(name, hm);

            @SuppressWarnings("unchecked")
            List<Element> labels = entity.getChild("label").getChildren();

            for (Element el : labels) {
                hm.put(el.getName(), el.getText());
            }
        }
    }

    @Override
    protected Path retrieveMEF(String uuid) throws URISyntaxException, IOException {
        request.clearParams();
        request.addParam("uuid", uuid);
        request.addParam("format", (params.mefFormatFull ? "full" : "partial"));

        // Request MEF2 format - if remote node is old
        // it will ignore this parameter and return a MEF1 format
        // which will be handle in addMetadata/updateMetadata.
        request.addParam("version", "2");
        request.addParam("relation", "false");
        request.setAddress(params.getServletPath() + "/" + params.getNode()
            + "/eng/" + Geonet.Service.MEF_EXPORT);

        Path tempFile = Files.createTempFile("temp-", ".dat");
        request.executeLarge(tempFile);

        return tempFile;
    }
}
