package org.fao.geonet.events.md;

import org.fao.geonet.domain.Metadata;

public class MetadataRemove extends MetadataEvent {

    private static final long serialVersionUID = 324534556246220509L;

    public MetadataRemove(Metadata md) {
        super(md);
    }

}
