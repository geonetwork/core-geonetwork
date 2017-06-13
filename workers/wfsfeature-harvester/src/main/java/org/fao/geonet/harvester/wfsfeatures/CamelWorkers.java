package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.spring.Main;

public class CamelWorkers {

    private Main main;

    public CamelWorkers() {
        main = new Main();
        main.setApplicationContextUri("/config-spring-geonetwork-jms.xml");
    }

    public void start() throws Exception {
        main.start();
    }

    public void stop() throws Exception {
        main.stop();
    }
}
