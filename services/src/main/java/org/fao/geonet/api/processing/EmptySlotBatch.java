package org.fao.geonet.api.processing;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

@ManagedResource
public class EmptySlotBatch implements SelfNaming {

    private ObjectName objectName;

    public EmptySlotBatch(String name, int i) {
        try {
            objectName = new ObjectName(String.format("geonetwork:name=' + name + ',idx=empty-slot-%d", i));
        } catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return objectName;
    }
}
