package org.fao.geonet.api.links;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

@ManagedResource
public class EmptySlot implements SelfNaming {

    private ObjectName objectName;

    public EmptySlot(int i) {
        try {
            objectName = new ObjectName(String.format("geonetwork:name=url-check,idx=empty-slot-%d", i));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return objectName;
    }
}