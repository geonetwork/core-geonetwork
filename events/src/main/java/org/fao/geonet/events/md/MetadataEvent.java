package org.fao.geonet.events.md;

import org.fao.geonet.domain.Metadata;
import org.springframework.context.ApplicationEvent;

/**
 * Abstract class for metadata events. Should not be used directly
 * 
 * @author delawen
 *
 */
public abstract class MetadataEvent extends ApplicationEvent {

    private static final long serialVersionUID = 456874566246220509L;

    private Metadata md;

    public MetadataEvent(Metadata md) {
        super(md);
        if (md == null) {
            throw new NullPointerException("Metadata cannot be null");
        }
        this.md = md;
    }

    public Metadata getMd() {
        return md;
    }

}
