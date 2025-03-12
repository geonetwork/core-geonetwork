//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geonet;

import org.fao.geonet.Logger;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.jdom.Element;

import java.sql.SQLException;

public class GeonetHarvester extends AbstractHarvester<HarvestResult, GeonetParams> {
    public static final String TYPE = "geonetwork";

    @Override
    protected GeonetParams createParams() {
        return new GeonetParams(dataMan);
    }


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
            harvesterSettingsManager.add("id:" + searchID, "abstract", s.abstrac);
            harvesterSettingsManager.add("id:" + searchID, "keywords", s.keywords);
            harvesterSettingsManager.add("id:" + searchID, "digital", s.digital);
            harvesterSettingsManager.add("id:" + searchID, "hardcopy", s.hardcopy);
            harvesterSettingsManager.add("id:" + searchID, "sourceUuid", s.sourceUuid);
            harvesterSettingsManager.add("id:" + searchID, "sourceName", s.sourceName);
            harvesterSettingsManager.add("id:" + searchID, "anyField", s.anyField);
            harvesterSettingsManager.add("id:" + searchID, "anyValue", s.anyValue);
        }

        //--- store group mapping

        for (Group g : params.getGroupCopyPolicy()) {
            String groupID = harvesterSettingsManager.add(path, "groupCopyPolicy", "");

            harvesterSettingsManager.add("id:" + groupID, "name", g.name);
            harvesterSettingsManager.add("id:" + groupID, "policy", g.policy);
        }
    }

    public void addHarvestInfo(Element info, String id, String uuid) {
        super.addHarvestInfo(info, id, uuid);

        String small = context.getBaseUrl() + "/" + params.getNode()
            + "/en/resources.get?access=public&id=" + id + "&fname=";

        String large = context.getBaseUrl() + "/" + params.getNode()
            + "/en/graphover.show?access=public&id=" + id + "&fname=";

        info.addContent(new Element("smallThumbnail").setText(small));
        info.addContent(new Element("largeThumbnail").setText(large));
    }

    public void doHarvest(Logger log) throws Exception {
        Harvester h = new Harvester(cancelMonitor, log, context, params, errors);
        result = h.harvest(log);
    }
}
