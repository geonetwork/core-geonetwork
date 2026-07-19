//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet.v4;

import java.sql.SQLException;
import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.geonet.Group;
import org.jdom.Element;

/**
 * The Geonet40Harvester class is responsible for harvesting metadata from a GeoNetwork 4.x server.
 * It extends the AbstractHarvester to provide specific functionality related to interacting
 * with GeoNetwork version 4.x, including parameter management and storing harvested node details.
 */
public class Geonet40Harvester extends AbstractHarvester<HarvestResult, GeonetParams> {
    public static final String TYPE = "geonetwork40";

    @Override
    protected GeonetParams createParams() {
        return new GeonetParams(dataMan);
    }


    @Override
    protected void storeNodeExtra(GeonetParams params, String path,
                                  String siteId, String optionsId) throws SQLException {
        setParams(params);

        harvesterSettingsManager.add("id:" + siteId, "host", params.host);
        harvesterSettingsManager.add("id:" + siteId, "node", params.getNode());
        harvesterSettingsManager.add("id:" + siteId, "useChangeDateForUpdate", params.useChangeDateForUpdate());
        harvesterSettingsManager.add("id:" + siteId, "createRemoteCategory", params.createRemoteCategory);
        harvesterSettingsManager.add("id:" + siteId, "mefFormatFull", params.mefFormatFull);
        harvesterSettingsManager.add("id:" + siteId, "xslfilter", params.xslfilter);

        //--- store search nodes

        for (Search s : params.getSearches()) {
            String searchID = harvesterSettingsManager.add(path, "search", "");

            harvesterSettingsManager.add("id:" + searchID, "freeText", s.freeText);
            harvesterSettingsManager.add("id:" + searchID, "title", s.title);
            harvesterSettingsManager.add("id:" + searchID, "abstract", s.abstractText);
            harvesterSettingsManager.add("id:" + searchID, "keywords", s.keywords);
            harvesterSettingsManager.add("id:" + searchID, "sourceUuid", s.sourceUuid);
            harvesterSettingsManager.add("id:" + searchID, "categories", s.categories);
            harvesterSettingsManager.add("id:" + searchID, "schemes", s.schemes);
            harvesterSettingsManager.add("id:" + searchID, "groupOwners", s.groupOwners);
        }

        //--- store group mapping

        for (Group g : params.getGroupCopyPolicy()) {
            String groupID = harvesterSettingsManager.add(path, "groupCopyPolicy", "");

            harvesterSettingsManager.add("id:" + groupID, "name", g.name);
            harvesterSettingsManager.add("id:" + groupID, "policy", g.policy);
        }
    }

    @Override
    public void addHarvestInfo(Element info, String id, String uuid) {
        super.addHarvestInfo(info, id, uuid);

        String small = context.getBaseUrl() + "/" + params.getNode()
            + "/api/records/" + uuid + "/attachments/";

        String large = context.getBaseUrl() + "/" + params.getNode()
            + "/api/records/" + uuid + "/attachments/";

        info.addContent(new Element("smallThumbnail").setText(small));
        info.addContent(new Element("largeThumbnail").setText(large));
    }

    public void doHarvest(Logger log) throws Exception {
        Harvester h = new Harvester(cancelMonitor, log, context, params, errors);
        result = h.harvest(log);
    }
}
