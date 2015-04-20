package org.fao.geonet.services.config;

import java.net.URL;

import jeeves.server.context.ServiceContext;

import org.apache.log4j.xml.DOMConfigurator;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * utilities
 * @author bmaire
 *
 */
public class LogUtils {
    private SettingManager settingMan;
    
    private final String DEFAUT_LOG_FILE = "log4j.xml"; 
    
    /** 
     * Constructor with ServiceContext
     * @param context
     */
    public LogUtils(ServiceContext context) {
        GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        settingMan = gc.getBean(SettingManager.class);
    }
    
   /** 
    * Constructor with AppContext
    * @param context
    */
   public LogUtils(ConfigurableApplicationContext context) {
       settingMan = context.getBean(SettingManager.class);
   }
    
    
    
    public void refreshLogConfiguration() {
        try {
            // get log config from db settings
            String log4jProp = settingMan.getValue(SettingManager.SYSTEM_SERVER_LOG);
            if (log4jProp == null) {
            	// on start database is empty so use default file
            	log4jProp = DEFAUT_LOG_FILE;
            }
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
