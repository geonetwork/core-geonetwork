package org.fao.geonet;

import jakarta.servlet.ServletContextEvent;
import org.apache.commons.jcs3.JCS;

public class JeevesJCSServletContextListener implements jakarta.servlet.ServletContextListener {

    @Override
    public void contextDestroyed( final ServletContextEvent arg0 )
    {
        JCS.shutdown();
    }
}
