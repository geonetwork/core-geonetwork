package org.fao.geonet.services.config;

import java.net.URL;

import jeeves.server.context.ServiceContext;

import org.apache.log4j.xml.DOMConfigurator;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.setting.SettingManager;

/**
 * utilities
 * @author bmaire
 *
 */
public class LogUtils {
    private SettingManager settingMan;
    
    public LogUtils(ServiceContext context) {
        GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        settingMan = gc.getBean(SettingManager.class);
    }
    
    public void refreshLogConfiguration() {
        try {
            // get log config from db settings
            String log4jProp = settingMan.getValue(SettingManager.SYSTEM_SERVER_LOG);
            URL url = DoActions.class.getResource("/"+log4jProp);
            if (url != null) {
                // refresh configuration
                DOMConfigurator.configure(url);
            } else {
                throw new OperationAbortedEx("Parameters saved but cannot set level log: file \""+log4jProp+"\" doesn't exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationAbortedEx("Parameters saved but cannot set level log: "+e.getMessage());
        }
    }
}
