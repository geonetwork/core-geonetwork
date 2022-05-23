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

package org.fao.geonet.services.harvesting;

import com.google.common.primitives.Longs;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.harvest.Common.OperResult;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.csw2.CswHarvester2;
import org.fao.geonet.kernel.harvest.harvester.csw2.CswParams2;
import org.fao.geonet.kernel.setting.HarvesterSettingsManager;
import org.jdom.Element;

import java.nio.file.Path;

import static org.fao.geonet.repository.HarvesterSettingRepository.ID_PREFIX;

//=============================================================================

public class Run implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        HarvestManager hm = gc.getBean(HarvestManager.class);
        HarvesterSettingsManager settingMan = context.getBean(HarvesterSettingsManager.class);

        String id = params.getChildText("id");

        String harvestUuid = settingMan.getValue("harvesting/id:" + id + "/site/uuid");
        AbstractHarvester ah = hm.getHarvester(harvestUuid);

        if ((ah != null) && (ah instanceof CswHarvester2)) {
            String paramValueSkipHarvesting = params.getChildText("skipHarvesting");
            boolean skipHarvesting = ((paramValueSkipHarvesting != null) && (paramValueSkipHarvesting.equals("true")))?true:false;

            settingMan.setValue("harvesting/id:" + id + "/options/skipHarvesting", skipHarvesting);
        }

        return Util.exec(params, context, new Util.Job() {
            public OperResult execute(HarvestManager hm, String id) throws Exception {
                HarvesterSettingsManager harvesterSettingsManager = context.getBean(HarvesterSettingsManager.class);

                String harvesterUuid =  harvesterSettingsManager.getValue("harvesting/id:" + id + "/site/uuid");

                AbstractHarvester ah = hm.getHarvester(harvesterUuid);
                if (ah instanceof CswHarvester2) {
                    boolean skipHarvesting = org.fao.geonet.Util.getParam(params, "skipHarvesting", false);
                    ((CswParams2) ah.getParams()).skipHarvesting = skipHarvesting;
                }

                return hm.run(id);
            }
        });
    }
}

//=============================================================================

