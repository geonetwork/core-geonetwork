/**
 * Copyright (C) 2013 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoNetwork.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.services.harvesting.notifier;

import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

public class SaveNotificationSettings implements Service {

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context)
            throws Exception {
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager settings = gc.getBean(SettingManager.class);
        Dbms dbms = (Dbms) context.getResourceManager()
                .open(Geonet.Res.MAIN_DB);
        
        String sendTo = "";
        
        for (Object o : params.getChildren()) {
            Element param = ((Element) o);

            if (param.getName().equalsIgnoreCase("recipient")) {
                if (!param.getValue().trim().isEmpty()) {
                    if(sendTo.isEmpty()) {
                    	sendTo = param.getValue().trim();
                    } else {
                    	sendTo += "," + param.getValue().trim();
                    }
                }
            } else if (param.getName().equalsIgnoreCase("template")) {
                settings.setValue(dbms, "system/harvesting/mail/template",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("templateError")) {
                settings.setValue(dbms, "system/harvesting/mail/templateError",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("templateWarning")) {
                settings.setValue(dbms, "system/harvesting/mail/templateWarning",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("subject")) {
                settings.setValue(dbms, "system/harvesting/mail/subject",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("enabled")) {
                settings.setValue(dbms, "system/harvesting/mail/enabled",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("level1")) {
                settings.setValue(dbms, "system/harvesting/mail/level1",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("level2")) {
                settings.setValue(dbms, "system/harvesting/mail/level2",
                        param.getValue());
            } else if (param.getName().equalsIgnoreCase("level3")) {
                settings.setValue(dbms, "system/harvesting/mail/level3",
                        param.getValue());
            }
        }

        settings.setValue(dbms, "system/harvesting/mail/recipient", sendTo);

        return new Element("ok");
    }

}
