package org.fao.geonet.jms.message.sysconfig;

import org.jdom.Element;

import java.util.HashSet;
import java.util.Set;

/**
 * @author heikki doeleman
 */
public class NodeConfigurations {
    
    private Set<Element> nodeConfigurations = new HashSet<Element>();

    public synchronized boolean add(Element element) {
        return nodeConfigurations.add(element);
    }

    public void clear() {
        nodeConfigurations.clear();
    }

    public Set<Element> getNodeConfigurations() {
        return nodeConfigurations;
    }

    private static NodeConfigurations instance;
    
    private NodeConfigurations(){}
    
    public static synchronized NodeConfigurations getInstance() {
        if(instance == null) {
            instance = new NodeConfigurations();
        }
        return instance;
    }
    
    public NodeConfigurations clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}