/**
 * Copyright (C) 2013 GeoNetwork
 *
 * This file is part of GeoNetwork
 *
 * GeoNetwork is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * GeoNetwork is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with GeoNetwork.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.fao.geonet.services.harvesting.notifier;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.jdom.Element;

import java.nio.file.Path;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

public class SaveNotificationSettings implements Service {

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context)
        throws Exception {
        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settings = gc.getBean(SettingManager.class);

        String sendTo = "";

        for (Object o : params.getChildren()) {
            Element param = ((Element) o);

            if (param.getName().equalsIgnoreCase("recipient")) {
                if (!param.getValue().trim().isEmpty()) {
                    if (sendTo.isEmpty()) {
                        sendTo = param.getValue().trim();
                    } else {
                        sendTo += "," + param.getValue().trim();
                    }
                }
            } else if (param.getName().equalsIgnoreCase("template")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_TEMPLATE,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("templateError")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_TEMPLATE_ERROR,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("templateWarning")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_TEMPLATE_WARNING,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("subject")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_SUBJECT,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("enabled")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_ENABLED,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("level1")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_LEVEL1,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("level2")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_LEVEL2,
                    param.getValue());
            } else if (param.getName().equalsIgnoreCase("level3")) {
                settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_LEVEL3,
                    param.getValue());
            }
        }

        settings.setValue(Settings.SYSTEM_HARVESTING_MAIL_RECIPIENT, sendTo);

        return new Element("ok");
    }

}
