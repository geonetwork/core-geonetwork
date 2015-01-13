package org.fao.geonet.events.md;

import org.fao.geonet.domain.Metadata;

public class MetadataAdd extends MetadataEvent {

    private static final long serialVersionUID = 324534556246220509L;

    public MetadataAdd(Metadata md) {
        super(md);
    }

}
