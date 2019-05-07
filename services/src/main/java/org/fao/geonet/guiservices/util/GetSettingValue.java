package org.fao.geonet.guiservices.util;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.nio.file.Path;

public class GetSettingValue implements Service {

    private String setting;

    public void init(Path appPath, ServiceConfig params) throws Exception {
        setting = params.getValue("setting");
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        String settingValue = "";

        if (StringUtils.isNotEmpty(setting)) {
            SettingManager settingManager = context.getBean(SettingManager.class);

            settingValue = settingManager.getValue(setting);

        }

        return new Element("a").setText(settingValue);
    }

}
