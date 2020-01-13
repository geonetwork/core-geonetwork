package org.fao.geonet.events.server;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

public class ServerStartup extends ApplicationEvent {
    private static final long serialVersionUID = 5217757141175170179L;

    public ServerStartup(ConfigurableApplicationContext event) {
        super(event);
    }

    public ConfigurableApplicationContext getContext() {
        return (ConfigurableApplicationContext)super.getSource();
    }
}
