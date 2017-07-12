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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.Path;

public class GetNotificationSettings implements Service {

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public Element exec(Element params, ServiceContext context)
        throws Exception {
        Element res = new Element("notification");

        GeonetContext gc = (GeonetContext) context
            .getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settings = gc.getBean(SettingManager.class);
        setProperty(res, settings, "template", "mail/template");
        setProperty(res, settings, "templateError", "mail/templateError");
        setProperty(res, settings, "templateWarning", "mail/templateWarning");
        setProperty(res, settings, "subject", "mail/subject");
        setProperty(res, settings, "enabled", "mail/enabled");
        setProperty(res, settings, "level1", "mail/level1");
        setProperty(res, settings, "level2", "mail/level2");
        setProperty(res, settings, "level3", "mail/level3");
        setProperty(res, settings, "recipient", "mail/recipient", ",");

        return res;
    }

    private void setProperty(Element res, SettingManager settings,
                             String property, String path) {
        setProperty(res, settings, property, path, null);
    }

    private void setProperty(Element res, SettingManager settings,
                             String property, String path, String split) {
        try {
            String value = settings.getValue("system/harvesting/" + path);

            if (split != null && value != null) {
                String[] values = value.split(split);
                for (String v : values) {
                    Element tmp = new Element(property);
                    tmp.setText(v);
                    res.addContent(tmp);
                }
            } else {
                Element tmp = new Element(property);
                tmp.setText(value);
                res.addContent(tmp);
            }
        } catch (Throwable t) {
            Log.error(Geonet.HARVESTER, t.getMessage(), t);
        }
    }

}
